package com.windroid.emu.views;

import static com.windroid.emu.activities.GeneralSettingsActivity.DISPLAY_STRETCH;
import static com.windroid.emu.activities.GeneralSettingsActivity.DISPLAY_STRETCH_DEFAULT_VALUE;
import static com.windroid.emu.activities.MainActivity.preferences;
import static com.windroid.emu.activities.MainActivity.selectedResolution;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.windroid.emu.activities.EmulationActivity;
import com.windroid.emu.input.InputStub;
import com.windroid.emu.input.TouchInputHandler;

import java.io.File;

@Keep
@SuppressLint("WrongConstant")
public class GameSurfaceView extends SurfaceView implements InputStub {
    private static final String TAG = "GameSurfaceView";

    public interface Callback {
        void changed(Surface sfc, int surfaceWidth, int surfaceHeight, int screenWidth, int screenHeight);
    }

    private Callback mCallback;
    private final Point p = new Point();
    private static boolean ipcServerStarted = false;

    private final SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            Log.i(TAG, "surfaceCreated: surface=" + holder.getSurface());
            nativeSurfaceCreated(holder.getSurface());
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            getDimensionsFromSettings();

            Log.i(TAG, "surfaceChanged: " + width + "x" + height + " target=" + p.x + "x" + p.y);
            nativeSurfaceChanged(holder.getSurface(), width, height);

            if (mCallback != null) {
                mCallback.changed(holder.getSurface(), width, height, p.x, p.y);
            }
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            Log.w(TAG, "surfaceDestroyed");
            nativeSurfaceDestroyed(holder.getSurface());
            if (mCallback != null) {
                mCallback.changed(holder.getSurface(), 0, 0, 0, 0);
            }
        }
    };

    public GameSurfaceView(Context context) {
        super(context);
        init();
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        getHolder().setFormat(PixelFormat.RGBA_8888);
        getHolder().addCallback(mSurfaceCallback);

        // Inicia o servidor IPC nativo para comunicar superfícies e input com o Wine
        if (!ipcServerStarted) {
            ipcServerStarted = nativeStartIpcServer(com.windroid.emu.activities.MainActivity.WINDROID_IPC_SOCKET_PATH);
            Log.i(TAG, "nativeStartIpcServer returned: " + ipcServerStarted + " path=" + com.windroid.emu.activities.MainActivity.WINDROID_IPC_SOCKET_PATH);
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
        triggerCallback();
    }

    public void regenerate() {
        triggerCallback();
    }

    public void triggerCallback() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        setBackground(new ColorDrawable(Color.TRANSPARENT) {
            @Override
            public boolean isStateful() {
                return true;
            }

            @Override
            public boolean hasFocusStateSpecified() {
                return true;
            }
        });

        Rect r = getHolder().getSurfaceFrame();
        Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(() -> mSurfaceCallback.surfaceChanged(getHolder(), PixelFormat.RGBA_8888, r.width(), r.height()));
        }
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    void getDimensionsFromSettings() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        String[] resolution = (selectedResolution != null ? selectedResolution : "1280x720").split("x");
        int w = Integer.parseInt(resolution[0]);
        int h = Integer.parseInt(resolution[1]);

        if ((width < height && w > h) || (width > height && w < h)) {
            p.set(h, w);
        } else {
            p.set(w, h);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (preferences != null) {
            if (preferences.getBoolean(DISPLAY_STRETCH, DISPLAY_STRETCH_DEFAULT_VALUE)) {
                getHolder().setSizeFromLayout();
                return;
            }
        }

        getDimensionsFromSettings();
        if (p.x <= 0 || p.y <= 0) return;

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if ((width < height && p.x > p.y) || (width > height && p.x < p.y)) {
            p.set(p.y, p.x);
        }

        if (width > height * p.x / p.y) {
            width = height * p.x / p.y;
        } else {
            height = width * p.y / p.x;
        }

        if (getHolder().getSurfaceFrame().width() != p.x || getHolder().getSurfaceFrame().height() != p.y) {
            getHolder().setFixedSize(p.x, p.y);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void sendMouseEvent(float x, float y, int whichButton, boolean buttonDown, boolean relative) {
        nativeSendMouseEvent(x, y, whichButton, buttonDown, relative);
    }

    @Override
    public void sendMouseWheelEvent(float deltaX, float deltaY) {
        nativeSendMouseWheel(deltaX, deltaY);
    }

    @Override
    public boolean sendKeyEvent(int scanCode, int keyCode, boolean keyDown) {
        nativeSendKeyEvent(scanCode, keyCode, keyDown);
        return true;
    }

    @Override
    public void sendTextEvent(byte[] utf8Bytes) {
        // Envia os bytes através do evento de teclado/texto
        if (utf8Bytes != null && utf8Bytes.length > 0) {
            for (byte b : utf8Bytes) {
                nativeSendKeyEvent(0, b & 0xFF, true);
                nativeSendKeyEvent(0, b & 0xFF, false);
            }
        }
    }

    @Override
    public void sendTouchEvent(int action, int pointerId, int x, int y) {
        nativeSendTouchEvent(action, pointerId, x, y);
    }

    @Override
    public void sendStylusEvent(float x, float y, int pressure, int tiltX, int tiltY, int orientation, int buttons, boolean eraser, boolean mouseMode) {
        nativeSendMouseEvent(x, y, buttons > 0 ? BUTTON_LEFT : BUTTON_UNDEFINED, buttons > 0, false);
    }

    public void reloadPreferences() {
        TouchInputHandler.refreshInputDevices();
    }

    public void setScalingFilter(int filter) {}
    public void setFrameGeneration(int mode) {}
    public void setColorProfile(int profile) {}
    public void setFrameSkip(int skip) {}
    public void setVsync(int vsync) {}

    // Nativos de ponte de superfície e IPC
    public static native boolean nativeStartIpcServer(String socketPath);
    public static native void nativeStopIpcServer();
    public static native boolean nativeIsSurfaceAvailable();
    private native void nativeSurfaceCreated(Surface surface);
    private native void nativeSurfaceChanged(Surface surface, int width, int height);
    private native void nativeSurfaceDestroyed(Surface surface);

    private native void nativeSendMouseEvent(float x, float y, int button, boolean down, boolean relative);
    private native void nativeSendMouseWheel(float deltaX, float deltaY);
    private native void nativeSendKeyEvent(int scanCode, int keyCode, boolean down);
    private native void nativeSendTouchEvent(int action, int pointerId, float x, float y);

    static {
        System.loadLibrary("windroid");
    }
}
