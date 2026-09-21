package com.windroid.emu.views;

import static com.windroid.emu.activities.MainActivity.getNativeResolution;
import static com.windroid.emu.controller.ControllerUtils.DOWN;
import static com.windroid.emu.controller.ControllerUtils.LEFT;
import static com.windroid.emu.controller.ControllerUtils.LEFT_DOWN;
import static com.windroid.emu.controller.ControllerUtils.LEFT_UP;
import static com.windroid.emu.controller.ControllerUtils.RIGHT;
import static com.windroid.emu.controller.ControllerUtils.RIGHT_DOWN;
import static com.windroid.emu.controller.ControllerUtils.RIGHT_UP;
import static com.windroid.emu.controller.ControllerUtils.UP;
//import static com.windroid.emu.controller.ControllerUtils.connectedVirtualControllers;
import static com.windroid.emu.controller.ControllerUtils.getAxisStatus;
import static com.windroid.emu.controller.ControllerUtils.updateAxisStateNative;
import static com.windroid.emu.controller.ControllerUtils.updateButtonsStateNative;
import static com.windroid.emu.input.InputStub.BUTTON_UNDEFINED;
import static com.windroid.emu.views.VirtualKeyboardInputCreatorView.GRID_SIZE;
import static com.windroid.emu.views.VirtualKeyboardInputView.SHAPE_CIRCLE;
import static com.windroid.emu.views.VirtualKeyboardInputView.SHAPE_DPAD;
import static com.windroid.emu.views.VirtualKeyboardInputView.SHAPE_RECTANGLE;
import static com.windroid.emu.views.VirtualKeyboardInputView.SHAPE_SQUARE;
import static com.windroid.emu.views.VirtualKeyboardInputView.detectClick;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.windroid.emu.views.GameSurfaceView;
import com.windroid.emu.R;
import com.windroid.emu.controller.ControllerUtils;

import static com.windroid.emu.activities.GeneralSettingsActivity.VIRTUAL_CONTROL_OPACITY;
import static com.windroid.emu.activities.MainActivity.preferences;

import java.util.ArrayList;

import android.content.SharedPreferences;

public class VirtualControllerInputView extends View {
    public VirtualControllerInputView(Context context) {
        super(context);
        init();
    }

    public VirtualControllerInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VirtualControllerInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Paint paint;
    private Paint textPaint;
    private final GameSurfaceView lorieView = new GameSurfaceView(getContext());
    private final Path dpadUp = new Path();
    private final Path dpadDown = new Path();
    private final Path dpadLeft = new Path();
    private final Path dpadRight = new Path();
    private final Path startButton = new Path();
    private final Path selectButton = new Path();
    private final ArrayList<VirtualControllerButton> buttonList = new ArrayList<>();
    private VirtualXInputDPad dpad;
    private VirtualXInputAnalog leftAnalog;
    private VirtualXInputAnalog rightAnalog;

    private byte buttonsStateA = 0;
    private byte buttonsStateB = 0;
    private float lt = 0;
    private float rt = 0;
    public boolean isEditing = false;

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(16F);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(120F);
        textPaint.setTypeface(getContext().getResources().getFont(R.font.quicksand));

        addButton(A_BUTTON, 2065F, 910F, 180F, SHAPE_CIRCLE);
        addButton(B_BUTTON, 2205F, 735F, 180F, SHAPE_CIRCLE);
        addButton(X_BUTTON, 1925F, 735F, 180F, SHAPE_CIRCLE);
        addButton(Y_BUTTON, 2065F, 560F, 180F, SHAPE_CIRCLE);
        addButton(START_BUTTON, 1330F, 980F, 130F, SHAPE_CIRCLE);
        addButton(SELECT_BUTTON, 1120F, 980F, 130F, SHAPE_CIRCLE);
        addButton(LB_BUTTON, 280F, 300F, 260F, SHAPE_RECTANGLE);
        addButton(LT_BUTTON, 280F, 140F, 260F, SHAPE_RECTANGLE);
        addButton(RB_BUTTON, 2065F, 300F, 260F, SHAPE_RECTANGLE);
        addButton(RT_BUTTON, 2065F, 140F, 260F, SHAPE_RECTANGLE);
        addButton(LS_BUTTON, 880F, 980F, 180F, SHAPE_CIRCLE);
        addButton(RS_BUTTON, 1560F, 980F, 180F, SHAPE_CIRCLE);

        leftAnalog = new VirtualXInputAnalog(LEFT_ANALOG, 280F, 840F, 275F);
        rightAnalog = new VirtualXInputAnalog(RIGHT_ANALOG, 1750F, 480F, 275F);
        dpad = new VirtualXInputDPad(0, 640F, 480F, 200F);

        boolean isPreferencesLoaded = preferences != null;

        if (isPreferencesLoaded) {
            buttonList.forEach((i) -> {
                i.x = preferences.getFloat("VC_BUTTON_" + i.id + "_X", i.x);
                i.y = preferences.getFloat("VC_BUTTON_" + i.id + "_Y", i.y);
            });

            leftAnalog.x = preferences.getFloat("VC_BUTTON_" + LEFT_ANALOG + "_X", leftAnalog.x);
            leftAnalog.y = preferences.getFloat("VC_BUTTON_" + LEFT_ANALOG + "_Y", leftAnalog.y);

            rightAnalog.x = preferences.getFloat("VC_BUTTON_" + RIGHT_ANALOG + "_X", rightAnalog.x);
            rightAnalog.y = preferences.getFloat("VC_BUTTON_" + RIGHT_ANALOG + "_Y", rightAnalog.y);

            dpad.x = preferences.getFloat("VC_BUTTON_DPAD_X", dpad.x);
            dpad.y = preferences.getFloat("VC_BUTTON_DPAD_Y", dpad.y);
        }

        adjustButtons();
    }

    private void adjustButtons() {
        String nativeResolution = getNativeResolution(getContext());
        String baseResolution = "2400x1080"; // My Device Resolution

        if (!nativeResolution.equals(baseResolution)) {
            String[] nativeResolutionSplit = nativeResolution.split("x");
            String[] baseResolutionSplit = baseResolution.split("x");

            float nativeResolutionWidth = Float.parseFloat(nativeResolutionSplit[0]);
            float nativeResolutionHeight = Float.parseFloat(nativeResolutionSplit[1]);

            float baseResolutionWidth = Float.parseFloat(baseResolutionSplit[0]);
            float baseResolutionHeight = Float.parseFloat(baseResolutionSplit[1]);

            float multiplierWidth = (nativeResolutionWidth / baseResolutionWidth) * 100F;
            float multiplierHeight = (nativeResolutionHeight / baseResolutionHeight) * 100F;

            buttonList.forEach((i) -> {
                if (preferences == null || !preferences.contains("VC_BUTTON_" + i.id + "_X")) {
                    i.x = Math.round(i.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
                    i.y = Math.round(i.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;
                }
            });

            if (preferences == null || !preferences.contains("VC_BUTTON_" + LEFT_ANALOG + "_X")) {
                leftAnalog.x = Math.round(leftAnalog.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
                leftAnalog.y = Math.round(leftAnalog.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;
            }

            if (preferences == null || !preferences.contains("VC_BUTTON_" + RIGHT_ANALOG + "_X")) {
                rightAnalog.x = Math.round(rightAnalog.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
                rightAnalog.y = Math.round(rightAnalog.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;
            }

            if (preferences == null || !preferences.contains("VC_BUTTON_DPAD_X")) {
                dpad.x = Math.round(dpad.x / 100F * multiplierWidth / GRID_SIZE) * (float) GRID_SIZE;
                dpad.y = Math.round(dpad.y / 100F * multiplierHeight / GRID_SIZE) * (float) GRID_SIZE;
            }
        }
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
        invalidate();
    }

    public void saveLayout() {
        if (preferences == null) return;

        new Thread(() -> {
            SharedPreferences.Editor editor = preferences.edit();

            buttonList.forEach((i) -> {
                editor.putFloat("VC_BUTTON_" + i.id + "_X", i.x);
                editor.putFloat("VC_BUTTON_" + i.id + "_Y", i.y);
            });

            editor.putFloat("VC_BUTTON_" + LEFT_ANALOG + "_X", leftAnalog.x);
            editor.putFloat("VC_BUTTON_" + LEFT_ANALOG + "_Y", leftAnalog.y);

            editor.putFloat("VC_BUTTON_" + RIGHT_ANALOG + "_X", rightAnalog.x);
            editor.putFloat("VC_BUTTON_" + RIGHT_ANALOG + "_Y", rightAnalog.y);

            editor.putFloat("VC_BUTTON_DPAD_X", dpad.x);
            editor.putFloat("VC_BUTTON_DPAD_Y", dpad.y);

            editor.apply();
        }).start();
    }

    private void addButton(int id, float x, float y, float radius, int shape) {
        buttonList.add(
                new VirtualControllerButton(id, x, y, radius, shape));
    }

    private String getButtonName(int id) {
        return switch (id) {
            case A_BUTTON -> "A";
            case B_BUTTON -> "B";
            case X_BUTTON -> "X";
            case Y_BUTTON -> "Y";
            case RB_BUTTON -> "RB";
            case LB_BUTTON -> "LB";
            case RT_BUTTON -> "RT";
            case LT_BUTTON -> "LT";
            case RS_BUTTON -> "RS";
            case LS_BUTTON -> "LS";
            default -> "";
        };
    }

    private void drawDPad(Path path, boolean isPressed, Canvas canvas) {
        paint.setStyle(isPressed ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        paint.setAlpha(isEditing ? 200 : (int) (getAlpha() * 200));

        canvas.drawPath(path, paint);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        buttonList.forEach((i) -> {
            if (i.isPressed) {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                textPaint.setColor(Color.BLACK);
            } else {
                paint.setStyle(Paint.Style.STROKE);
                textPaint.setColor(Color.WHITE);
            }
            paint.setColor(Color.WHITE);
            paint.setAlpha(isEditing ? 255 : (int) (getAlpha() * 255));
            paint.setStrokeWidth(16F);
            textPaint.setAlpha(isEditing ? 255 : (int) (getAlpha() * 255));

            paint.setTextSize(i.radius / 4F);

            float offset = (textPaint.getFontMetrics().ascent + textPaint.getFontMetrics().descent) / 2F;

            switch (i.shape) {
                case SHAPE_CIRCLE -> canvas.drawCircle(i.x, i.y, i.radius / 2F, paint);
                case SHAPE_RECTANGLE -> canvas.drawRoundRect(i.x - i.radius / 2F,
                        i.y - i.radius / 4F,
                        i.x + i.radius / 2F,
                        i.y + i.radius / 4F,
                        32F,
                        32F,
                        paint);
            }

            switch (i.id) {
                case START_BUTTON -> {
                    paint.setStrokeWidth(12F);

                    startButton.reset();
                    startButton.moveTo(i.x - i.radius / 3, i.y - i.radius / 8);
                    startButton.lineTo(i.x - i.radius / 3 + i.radius - i.radius / 3, i.y - i.radius / 8);
                    startButton.moveTo(i.x - i.radius / 3, i.y);
                    startButton.lineTo(i.x - i.radius / 3 + i.radius - i.radius / 3, i.y);
                    startButton.moveTo(i.x - i.radius / 3, i.y + i.radius / 8);
                    startButton.lineTo(i.x - i.radius / 3 + i.radius - i.radius / 3, i.y + i.radius / 8);
                    startButton.close();

                    paint.setColor(i.isPressed ? Color.BLACK : Color.WHITE);
                    paint.setAlpha(isEditing ? 200 : (int) (getAlpha() * 200));

                    canvas.drawPath(startButton, paint);
                }
                case SELECT_BUTTON -> {
                    paint.setStrokeWidth(12F);

                    selectButton.reset();
                    selectButton.moveTo(i.x - i.radius / 4F + 4F, i.y - i.radius / 4 + 40F);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F, i.y - i.radius / 4);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F + 40F, i.y - i.radius / 4);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F + 40F, i.y - i.radius / 4 + 20F);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F + 40F, i.y - i.radius / 4);
                    selectButton.lineTo(i.x - i.radius / 4F + 4F, i.y - i.radius / 4);
                    selectButton.close();
                    selectButton.moveTo(i.x - i.radius / 4F + 20F, i.y - i.radius / 4 + 30F);
                    selectButton.lineTo(i.x - i.radius / 4F + 60F, i.y - i.radius / 4 + 30F);
                    selectButton.lineTo(i.x - i.radius / 4F + 60F, i.y - i.radius / 4 + 70F);
                    selectButton.lineTo(i.x - i.radius / 4F + 20F, i.y - i.radius / 4 + 70F);
                    selectButton.lineTo(i.x - i.radius / 4F + 20F, i.y - i.radius / 4 + 24F);
                    selectButton.lineTo(i.x - i.radius / 4F + 20F, i.y - i.radius / 4 + 70F);
                    selectButton.lineTo(i.x - i.radius / 4F + 60F, i.y - i.radius / 4 + 70F);
                    selectButton.lineTo(i.x - i.radius / 4F + 60F, i.y - i.radius / 4 + 30F);
                    selectButton.close();

                    paint.setColor(i.isPressed ? Color.BLACK : Color.WHITE);
                    paint.setAlpha(isEditing ? 200 : (int) (getAlpha() * 200));

                    canvas.drawPath(selectButton, paint);
                }
                default -> canvas.drawText(getButtonName(i.id), i.x, i.y - offset - 4, textPaint);
            }
        });

        // Left Analog
        float analogX = leftAnalog.x + leftAnalog.fingerX;
        float analogY = leftAnalog.y + leftAnalog.fingerY;

        float distSquared = (leftAnalog.fingerX * leftAnalog.fingerX) + (leftAnalog.fingerY * leftAnalog.fingerY);
        float maxDist = (leftAnalog.radius / 4F) * (leftAnalog.radius / 4F);

        if (distSquared > maxDist) {
            float dist = (float) Math.sqrt(distSquared);
            float scale = (leftAnalog.radius / 4F) / dist;
            analogX = leftAnalog.x + (leftAnalog.fingerX * scale);
            analogY = leftAnalog.y + (leftAnalog.fingerY * scale);
        }

        paint.setColor(Color.WHITE);
        paint.setAlpha(isEditing ? 200 : (int) (getAlpha() * 200));

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(leftAnalog.x, leftAnalog.y, leftAnalog.radius / 2F, paint);

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(analogX, analogY, leftAnalog.radius / 4F, paint);

        paint.setColor(Color.WHITE);
        paint.setAlpha(isEditing ? 200 : (int) (getAlpha() * 200));
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(leftAnalog.x, leftAnalog.y, leftAnalog.radius / 2F, paint);

        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(analogX, analogY, leftAnalog.radius / 4F, paint);

        // Right Analog
        float rightAnalogX = rightAnalog.x + rightAnalog.fingerX;
        float rightAnalogY = rightAnalog.y + rightAnalog.fingerY;

        float rightDistSquared = (rightAnalog.fingerX * rightAnalog.fingerX)
                + (rightAnalog.fingerY * rightAnalog.fingerY);
        float rightMaxDist = (rightAnalog.radius / 4F) * (rightAnalog.radius / 4F);

        if (rightDistSquared > rightMaxDist) {
            float dist = (float) Math.sqrt(rightDistSquared);
            float scale = (rightAnalog.radius / 4F) / dist;
            rightAnalogX = rightAnalog.x + (rightAnalog.fingerX * scale);
            rightAnalogY = rightAnalog.y + (rightAnalog.fingerY * scale);
        }

        paint.setColor(Color.WHITE);
        paint.setAlpha(isEditing ? 200 : (int) (getAlpha() * 200));

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(rightAnalog.x, rightAnalog.y, rightAnalog.radius / 2F, paint);

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(rightAnalogX, rightAnalogY, rightAnalog.radius / 4F, paint);

        paint.setColor(Color.WHITE);
        paint.setAlpha(isEditing ? 200 : (int) (getAlpha() * 200));
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(rightAnalog.x, rightAnalog.y, rightAnalog.radius / 2F, paint);

        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(rightAnalogX, rightAnalogY, rightAnalog.radius / 4F, paint);

        // D-Pad Left
        dpadLeft.reset();
        dpadLeft.moveTo(dpad.x - 20F, dpad.y);
        dpadLeft.lineTo(dpad.x - 20F - dpad.radius / 4F, dpad.y - dpad.radius / 4F);
        dpadLeft.lineTo(dpad.x - 20F - dpad.radius / 4F - dpad.radius / 2F, dpad.y - dpad.radius / 4F);
        dpadLeft.lineTo(
                dpad.x - 20F - dpad.radius / 4F - dpad.radius / 2F,
                dpad.y - dpad.radius / 4F + dpad.radius / 2F);
        dpadLeft.lineTo(dpad.x - 20F - dpad.radius / 4F, dpad.y - dpad.radius / 4F + dpad.radius / 2F);
        dpadLeft.lineTo(dpad.x - 20F, dpad.y);
        dpadLeft.close();

        // D-Pad Up
        dpadUp.reset();
        dpadUp.moveTo(dpad.x, dpad.y - 20F);
        dpadUp.lineTo(dpad.x - dpad.radius / 4F, dpad.y - 20F - dpad.radius / 4F);
        dpadUp.lineTo(dpad.x - dpad.radius / 4F, dpad.y - 20F - dpad.radius / 4F - dpad.radius / 2F);
        dpadUp.lineTo(
                dpad.x - dpad.radius / 4F + dpad.radius / 2F,
                dpad.y - 20F - dpad.radius / 4F - dpad.radius / 2F);
        dpadUp.lineTo(dpad.x - dpad.radius / 4 + dpad.radius / 2F, dpad.y - 20F - dpad.radius / 4F);
        dpadUp.lineTo(dpad.x, dpad.y - 20F);
        dpadUp.close();

        // D-Pad Right
        dpadRight.reset();
        dpadRight.moveTo(dpad.x + 20F, dpad.y);
        dpadRight.lineTo(dpad.x + 20F + dpad.radius / 4F, dpad.y - dpad.radius / 4F);
        dpadRight.lineTo(dpad.x + 20F + dpad.radius / 4F + dpad.radius / 2F, dpad.y - dpad.radius / 4F);
        dpadRight.lineTo(
                dpad.x + 20 + dpad.radius / 4 + dpad.radius / 2,
                dpad.y - dpad.radius / 4 + dpad.radius / 2);
        dpadRight.lineTo(dpad.x + 20F + dpad.radius / 4F, dpad.y - dpad.radius / 4F + dpad.radius / 2F);
        dpadRight.lineTo(dpad.x + 20F, dpad.y);
        dpadRight.close();

        // D-Pad Down
        dpadDown.reset();
        dpadDown.moveTo(dpad.x, dpad.y + 20F);
        dpadDown.lineTo(dpad.x - dpad.radius / 4F, dpad.y + 20F + dpad.radius / 4F);
        dpadDown.lineTo(dpad.x - dpad.radius / 4F, dpad.y + 20F + dpad.radius / 4F + dpad.radius / 2F);
        dpadDown.lineTo(
                dpad.x - dpad.radius / 4F + dpad.radius / 2F,
                dpad.y + 20F + dpad.radius / 4F + dpad.radius / 2F);
        dpadDown.lineTo(dpad.x - dpad.radius / 4F + dpad.radius / 2F, dpad.y + 20F + dpad.radius / 4F);
        dpadDown.lineTo(dpad.x, dpad.y + 20F);
        dpadDown.close();

        drawDPad(dpadUp, dpad.dpadStatus == UP || dpad.dpadStatus == RIGHT_UP || dpad.dpadStatus == LEFT_UP, canvas);
        drawDPad(dpadDown, dpad.dpadStatus == DOWN || dpad.dpadStatus == RIGHT_DOWN || dpad.dpadStatus == LEFT_DOWN,
                canvas);
        drawDPad(dpadLeft, dpad.dpadStatus == LEFT || dpad.dpadStatus == LEFT_DOWN || dpad.dpadStatus == LEFT_UP,
                canvas);
        drawDPad(dpadRight, dpad.dpadStatus == RIGHT || dpad.dpadStatus == RIGHT_DOWN || dpad.dpadStatus == RIGHT_UP,
                canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEditing) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                    for (VirtualControllerButton i : buttonList) {
                        if (detectClick(event, event.getActionIndex(), i.x, i.y, i.radius, i.shape)) {
                            i.fingerId = event.getPointerId(event.getActionIndex());
                            i.isPressed = true;
                            break;
                        }
                    }

                    if (detectClick(event, event.getActionIndex(), leftAnalog.x, leftAnalog.y, leftAnalog.radius,
                            SHAPE_CIRCLE)) {
                        leftAnalog.fingerId = event.getPointerId(event.getActionIndex());
                        leftAnalog.isPressed = true;
                    }

                    if (detectClick(event, event.getActionIndex(), rightAnalog.x, rightAnalog.y, rightAnalog.radius,
                            SHAPE_CIRCLE)) {
                        rightAnalog.fingerId = event.getPointerId(event.getActionIndex());
                        rightAnalog.isPressed = true;
                    }

                    if (detectClick(event, event.getActionIndex(), dpad.x, dpad.y, dpad.radius, SHAPE_DPAD)) {
                        dpad.fingerId = event.getPointerId(event.getActionIndex());
                        dpad.isPressed = true;
                    }
                }
                case MotionEvent.ACTION_MOVE -> {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        for (VirtualControllerButton v : buttonList) {
                            if (v.fingerId == event.getPointerId(i) && v.isPressed) {
                                v.x = Math.round(event.getX(i) / GRID_SIZE) * (float) GRID_SIZE;
                                v.y = Math.round(event.getY(i) / GRID_SIZE) * (float) GRID_SIZE;
                                break;
                            }
                        }

                        if (leftAnalog.fingerId == event.getPointerId(i) && leftAnalog.isPressed) {
                            leftAnalog.x = Math.round(event.getX(i) / GRID_SIZE) * (float) GRID_SIZE;
                            leftAnalog.y = Math.round(event.getY(i) / GRID_SIZE) * (float) GRID_SIZE;
                        }

                        if (rightAnalog.fingerId == event.getPointerId(i) && rightAnalog.isPressed) {
                            rightAnalog.x = Math.round(event.getX(i) / GRID_SIZE) * (float) GRID_SIZE;
                            rightAnalog.y = Math.round(event.getY(i) / GRID_SIZE) * (float) GRID_SIZE;
                        }

                        if (dpad.fingerId == event.getPointerId(i) && dpad.isPressed) {
                            dpad.x = Math.round(event.getX(i) / GRID_SIZE) * (float) GRID_SIZE;
                            dpad.y = Math.round(event.getY(i) / GRID_SIZE) * (float) GRID_SIZE;
                        }
                    }
                    invalidate();
                }
                case MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                    int actionIndex = event.getActionIndex();
                    int pointerId = event.getPointerId(actionIndex);

                    for (VirtualControllerButton i : buttonList) {
                        if (i.fingerId == pointerId) {
                            i.fingerId = -1;
                            i.isPressed = false;
                        }
                    }

                    if (leftAnalog.fingerId == pointerId) {
                        leftAnalog.fingerId = -1;
                        leftAnalog.isPressed = false;
                    }

                    if (rightAnalog.fingerId == pointerId) {
                        rightAnalog.fingerId = -1;
                        rightAnalog.isPressed = false;
                    }

                    if (dpad.fingerId == pointerId) {
                        dpad.fingerId = -1;
                        dpad.isPressed = false;
                    }
                }
            }
            return true;
        }

        if (virtualXInputControllerId == -1)
            return true;

        float lx = leftAnalog.isPressed ? (leftAnalog.fingerX / (leftAnalog.radius / 4)) : 0F;
        float ly = leftAnalog.isPressed ? (leftAnalog.fingerY / (leftAnalog.radius / 4)) : 0F;
        float rx = rightAnalog.isPressed ? (rightAnalog.fingerX / (rightAnalog.radius / 4)) : 0F;
        float ry = rightAnalog.isPressed ? (rightAnalog.fingerY / (rightAnalog.radius / 4)) : 0F;
        byte dpadStatus = (byte) dpad.dpadStatus;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                for (VirtualControllerButton i : buttonList) {
                    if (detectClick(event, event.getActionIndex(), i.x, i.y, i.radius, i.shape)) {
                        i.fingerId = event.getPointerId(event.getActionIndex());
                        i.isPressed = true;
                        handleButton(i, true);
                        break;
                    }
                }

                if (detectClick(event, event.getActionIndex(), leftAnalog.x, leftAnalog.y, leftAnalog.radius,
                        SHAPE_CIRCLE)) {
                    float posX = event.getX(event.getActionIndex()) - leftAnalog.x;
                    float posY = event.getY(event.getActionIndex()) - leftAnalog.y;

                    leftAnalog.fingerId = event.getPointerId(event.getActionIndex());

                    float maxDist = leftAnalog.radius / 4F;
                    float dist = (float) Math.sqrt(posX * posX + posY * posY);

                    if (dist > maxDist) {
                        float scale = maxDist / dist;
                        posX *= scale;
                        posY *= scale;
                    }

                    leftAnalog.fingerX = posX;
                    leftAnalog.fingerY = posY;
                    leftAnalog.isPressed = true;

                    lx = (posX / maxDist);
                    ly = (posY / maxDist);
                }

                if (detectClick(event, event.getActionIndex(), rightAnalog.x, rightAnalog.y, rightAnalog.radius,
                        SHAPE_CIRCLE)) {
                    float posX = event.getX(event.getActionIndex()) - rightAnalog.x;
                    float posY = event.getY(event.getActionIndex()) - rightAnalog.y;

                    rightAnalog.fingerId = event.getPointerId(event.getActionIndex());

                    float maxDist = rightAnalog.radius / 4F;
                    float dist = (float) Math.sqrt(posX * posX + posY * posY);

                    if (dist > maxDist) {
                        float scale = maxDist / dist;
                        posX *= scale;
                        posY *= scale;
                    }

                    rightAnalog.fingerX = posX;
                    rightAnalog.fingerY = posY;
                    rightAnalog.isPressed = true;

                    rx = (posX / maxDist);
                    ry = (posY / maxDist);
                }

                if (detectClick(event, event.getActionIndex(), dpad.x, dpad.y, dpad.radius, SHAPE_DPAD)) {
                    float posX = event.getX(event.getActionIndex()) - dpad.x;
                    float posY = event.getY(event.getActionIndex()) - dpad.y;

                    dpad.fingerId = event.getPointerId(event.getActionIndex());
                    dpad.fingerX = posX;
                    dpad.fingerY = posY;
                    dpad.isPressed = true;
                    dpad.dpadStatus = getAxisStatus(posX / dpad.radius, posY / dpad.radius, 0.25F);

                    dpadStatus = (byte) dpad.dpadStatus;
                }

                invalidate();
            }
            case MotionEvent.ACTION_MOVE -> {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    boolean isFingerPressingButton = false;

                    for (VirtualControllerButton v : buttonList) {
                        if (v.fingerId == event.getPointerId(i)) {
                            isFingerPressingButton = true;
                            break;
                        }
                    }

                    if (leftAnalog.isPressed && leftAnalog.fingerId == event.getPointerId(i)) {
                        float posX = event.getX(i) - leftAnalog.x;
                        float posY = event.getY(i) - leftAnalog.y;

                        float maxDist = leftAnalog.radius / 4F;
                        float dist = (float) Math.sqrt(posX * posX + posY * posY);

                        if (dist > maxDist) {
                            float scale = maxDist / dist;
                            posX *= scale;
                            posY *= scale;
                        }

                        leftAnalog.fingerX = posX;
                        leftAnalog.fingerY = posY;

                        lx = (posX / maxDist);
                        ly = (posY / maxDist);

                        isFingerPressingButton = true;
                    }

                    if (rightAnalog.isPressed && rightAnalog.fingerId == event.getPointerId(i)) {
                        float posX = event.getX(i) - rightAnalog.x;
                        float posY = event.getY(i) - rightAnalog.y;

                        float maxDist = rightAnalog.radius / 4F;
                        float dist = (float) Math.sqrt(posX * posX + posY * posY);

                        if (dist > maxDist) {
                            float scale = maxDist / dist;
                            posX *= scale;
                            posY *= scale;
                        }

                        rightAnalog.fingerX = posX;
                        rightAnalog.fingerY = posY;

                        rx = (posX / maxDist);
                        ry = (posY / maxDist);

                        isFingerPressingButton = true;
                    }

                    if (dpad.isPressed && dpad.fingerId == event.getPointerId(i)) {
                        float posX = event.getX(i) - dpad.x;
                        float posY = event.getY(i) - dpad.y;

                        dpad.fingerX = posX;
                        dpad.fingerY = posY;
                        dpad.dpadStatus = getAxisStatus(posX / dpad.radius, posY / dpad.radius, 0.25F);

                        dpadStatus = (byte) dpad.dpadStatus;

                        isFingerPressingButton = true;
                    }

                    if (!isFingerPressingButton && event.getHistorySize() > 0) {
                        float deltaX = event.getX(i) - event.getHistoricalX(i, 0);
                        float deltaY = event.getY(i) - event.getHistoricalY(i, 0);

                        lorieView.sendMouseEvent(deltaX, deltaY, BUTTON_UNDEFINED, false, true);
                    }
                }

                invalidate();
            }
            case MotionEvent.ACTION_POINTER_UP -> {
                for (VirtualControllerButton i : buttonList) {
                    if (i.fingerId == event.getPointerId(event.getActionIndex())) {
                        i.fingerId = -1;
                        handleButton(i, false);
                    }
                }

                if (leftAnalog.fingerId == event.getPointerId(event.getActionIndex())) {
                    leftAnalog.fingerId = -1;
                    leftAnalog.fingerX = 0F;
                    leftAnalog.fingerY = 0F;
                    leftAnalog.isPressed = false;

                    lx = 0F;
                    ly = 0F;
                }

                if (rightAnalog.fingerId == event.getPointerId(event.getActionIndex())) {
                    rightAnalog.fingerId = -1;
                    rightAnalog.fingerX = 0F;
                    rightAnalog.fingerY = 0F;
                    rightAnalog.isPressed = false;

                    rx = 0F;
                    ry = 0F;
                }

                if (dpad.fingerId == event.getPointerId(event.getActionIndex())) {
                    dpad.fingerId = -1;
                    dpad.fingerX = 0F;
                    dpad.fingerY = 0F;
                    dpad.isPressed = false;
                    dpad.dpadStatus = 0;

                    dpadStatus = 0;
                }

                invalidate();
            }
            case MotionEvent.ACTION_UP -> {
                for (VirtualControllerButton i : buttonList) {
                    if (i.isPressed) {
                        i.fingerId = -1;
                        handleButton(i, false);
                    }
                }

                // Left Analog
                leftAnalog.fingerId = -1;
                leftAnalog.fingerX = 0F;
                leftAnalog.fingerY = 0F;
                leftAnalog.isPressed = false;

                lx = 0F;
                ly = 0F;

                // Right Analog
                rightAnalog.fingerId = -1;
                rightAnalog.fingerX = 0F;
                rightAnalog.fingerY = 0F;
                rightAnalog.isPressed = false;

                rx = 0F;
                ry = 0F;

                // D-Pad
                dpad.fingerId = -1;
                dpad.fingerX = 0F;
                dpad.fingerY = 0F;
                dpad.isPressed = false;
                dpad.dpadStatus = 0;

                dpadStatus = 0;

                invalidate();
            }
        }

        updateAxisStateNative(virtualXInputControllerId, lx, ly, rx, ry, lt, rt, dpadStatus);
        updateButtonsStateNative(virtualXInputControllerId, buttonsStateA, buttonsStateB);

        return true;
    }

    private void handleButton(VirtualControllerButton button, boolean isPressed) {
        button.isPressed = isPressed;

        switch (button.id) {
            case A_BUTTON -> {
                if (isPressed)
                    buttonsStateA |= ControllerUtils.A_BUTTON;
                else
                    buttonsStateA &= ~ControllerUtils.A_BUTTON;
            }
            case B_BUTTON -> {
                if (isPressed)
                    buttonsStateA |= ControllerUtils.B_BUTTON;
                else
                    buttonsStateA &= ~ControllerUtils.B_BUTTON;
            }
            case X_BUTTON -> {
                if (isPressed)
                    buttonsStateA |= ControllerUtils.X_BUTTON;
                else
                    buttonsStateA &= ~ControllerUtils.X_BUTTON;
            }
            case Y_BUTTON -> {
                if (isPressed)
                    buttonsStateA |= ControllerUtils.Y_BUTTON;
                else
                    buttonsStateA &= ~ControllerUtils.Y_BUTTON;
            }
            case START_BUTTON -> {
                if (isPressed)
                    buttonsStateB |= ControllerUtils.START_BUTTON;
                else
                    buttonsStateB &= ~ControllerUtils.START_BUTTON;
            }
            case SELECT_BUTTON -> {
                if (isPressed)
                    buttonsStateB |= ControllerUtils.SELECT_BUTTON;
                else
                    buttonsStateB &= ~ControllerUtils.SELECT_BUTTON;
            }
            case LB_BUTTON -> {
                if (isPressed)
                    buttonsStateA |= ControllerUtils.LB_BUTTON;
                else
                    buttonsStateA &= ~ControllerUtils.LB_BUTTON;
            }
            case LT_BUTTON -> lt = isPressed ? 1F : 0F;
            case RB_BUTTON -> {
                if (isPressed)
                    buttonsStateA |= ControllerUtils.RB_BUTTON;
                else
                    buttonsStateA &= ~ControllerUtils.RB_BUTTON;
            }
            case RT_BUTTON -> rt = isPressed ? 1F : 0F;
            case LS_BUTTON -> {
                if (isPressed)
                    buttonsStateA |= ControllerUtils.LS_BUTTON;
                else
                    buttonsStateA &= ~ControllerUtils.LS_BUTTON;
            }
            case RS_BUTTON -> {
                if (isPressed)
                    buttonsStateA |= (byte) ControllerUtils.RS_BUTTON;
                else
                    buttonsStateA &= (byte) ~ControllerUtils.RS_BUTTON;
            }
        }
    }

    public static class VirtualControllerButton {
        public int id;
        public float x;
        public float y;
        public float radius;
        public int shape;
        public int fingerId = -1;
        public boolean isPressed = false;

        public VirtualControllerButton(int id, float x, float y, float radius, int shape) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.shape = shape;
        }
    }

    public static class VirtualXInputDPad {
        public int id;
        public float x;
        public float y;
        public float radius;
        public int shape;
        public int fingerId = -1;
        public boolean isPressed = false;
        public float fingerX = 0F;
        public float fingerY = 0F;
        public int dpadStatus = 0;

        public VirtualXInputDPad(int id, float x, float y, float radius) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    public static class VirtualXInputAnalog {
        public int id;
        public float x;
        public float y;
        public float radius;
        public int shape;
        public int fingerId = -1;
        public boolean isPressed = false;
        public float fingerX = 0F;
        public float fingerY = 0F;

        public VirtualXInputAnalog(int id, float x, float y, float radius) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    public static class VirtualXInputTouchPad {
        public int id;
        public float x;
        public float y;
        public float radius;
        public int shape;
        public int fingerId = -1;
        public boolean isPressed = false;

        public VirtualXInputTouchPad(int id, float x, float y, float radius) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    private final static int A_BUTTON = 1;
    private final static int B_BUTTON = 2;
    private final static int X_BUTTON = 3;
    private final static int Y_BUTTON = 4;
    private final static int START_BUTTON = 5;
    private final static int SELECT_BUTTON = 6;
    private final static int LB_BUTTON = 7;
    private final static int LT_BUTTON = 8;
    private final static int RB_BUTTON = 9;
    private final static int RT_BUTTON = 10;
    private final static int LEFT_ANALOG = 11;
    private final static int LS_BUTTON = 12;
    private final static int RS_BUTTON = 13;
    private final static int RIGHT_ANALOG = 14;

    public static int virtualXInputControllerId = -1;
}