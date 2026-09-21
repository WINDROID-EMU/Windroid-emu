package com.windroid.emu.activities;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.view.WindowManager.LayoutParams.*;
import static com.windroid.emu.activities.GeneralSettingsActivity.DISPLAY_STRETCH;
import static com.windroid.emu.activities.GeneralSettingsActivity.DISPLAY_STRETCH_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.FPS_LIMIT;
import static com.windroid.emu.activities.MainActivity.enableCpuCounter;
import static com.windroid.emu.activities.MainActivity.enableRamCounter;
import static com.windroid.emu.activities.MainActivity.memoryStats;
import static com.windroid.emu.activities.MainActivity.preferences;
import static com.windroid.emu.activities.MainActivity.setSharedVars;
import static com.windroid.emu.activities.MainActivity.totalCpuUsage;
import static com.windroid.emu.activities.RatManagerActivity.generateMangoHUDConfFile;
import static com.windroid.emu.adapters.AdapterGame.selectedGameName;
import static com.windroid.emu.controller.ControllerUtils.connectController;
import static com.windroid.emu.controller.ControllerUtils.disconnectController;
import static com.windroid.emu.controller.ControllerUtils.prepareControllersMappings;
import static com.windroid.emu.controller.ControllerUtils.stopInputServer;
import static com.windroid.emu.controller.ControllerUtils.updateAxisState;
import static com.windroid.emu.core.ShellLoader.cleanup;
import static com.windroid.emu.core.ShellLoader.runCommand;
import static com.windroid.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.windroid.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.windroid.emu.fragments.ShortcutsFragment.getEnableDInput;
import static com.windroid.emu.fragments.ShortcutsFragment.getEnableXInput;
import static com.windroid.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.windroid.emu.fragments.ShortcutsFragment.getVirtualControllerXInput;
import static com.windroid.emu.activities.GeneralSettingsActivity.VIRTUAL_CONTROL_OPACITY;
import static com.windroid.emu.activities.GeneralSettingsActivity.VIRTUAL_CONTROL_OPACITY_DEFAULT_VALUE;
import static com.windroid.emu.activities.MainActivity.virtualControlOpacity;
import static com.windroid.emu.views.VirtualControllerInputView.virtualXInputControllerId;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_SCALING_FILTER;
import static com.windroid.emu.activities.GeneralSettingsActivity.SCALING_FILTER_LINEAR;
import static com.windroid.emu.activities.GeneralSettingsActivity.SCALING_FILTER_FSR;
import static com.windroid.emu.activities.GeneralSettingsActivity.SCALING_FILTER_CAS;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_FRAME_GENERATION;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAME_GENERATION_OFF;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAME_GENERATION_SMOOTHING;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_FRAMESKIP;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAMESKIP_0;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAMESKIP_1;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAMESKIP_2;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAMESKIP_3;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAMESKIP_4;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAMESKIP_5;
import static com.windroid.emu.activities.GeneralSettingsActivity.COLOR_PROFILE;
import static com.windroid.emu.activities.GeneralSettingsActivity.COLOR_PROFILE_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_VIBRATION;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_VIBRATION_DEFAULT_VALUE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;
import com.windroid.emu.views.GameSurfaceView;
import com.windroid.emu.R;
import com.windroid.emu.controller.ControllerUtils;
import com.windroid.emu.core.WineWrapper;
import com.windroid.emu.fragments.ControllerSettingsFragment;
import com.windroid.emu.fragments.LogViewerFragment;
import com.windroid.emu.fragments.TaskManagerFragment;
import com.windroid.emu.fragments.VirtualControllerSettingsFragment;
import com.windroid.emu.input.InputEventSender;
import com.windroid.emu.input.TouchInputHandler;
import com.windroid.emu.views.VirtualControllerInputView;
import com.windroid.emu.views.VirtualKeyboardInputView;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import android.app.ActivityManager;

public class EmulationActivity extends AppCompatActivity implements View.OnApplyWindowInsetsListener {
    public static Handler handler = new Handler();
    private TouchInputHandler mInputHandler;
    static InputMethodManager inputMethodManager;
    public static boolean externalKeyboardConnected = false;
    private View.OnKeyListener mLorieKeyListener;
    private final SharedPreferences.OnSharedPreferenceChangeListener preferencesChangedListener = (__,
            key) -> onPreferencesChanged();

    @SuppressLint("StaticFieldLeak")
    private static EmulationActivity instance;

    public EmulationActivity() {
        instance = this;
    }

    public static EmulationActivity getInstance() {
        return instance;
    }

    private boolean emulationPaused = false;
    private DrawerLayout drawerLayout;
    private VirtualKeyboardInputView virtualKeyboardInputView;
    private VirtualControllerInputView virtualControllerInputView;

    @Override
    @SuppressLint({ "AppCompatMethod", "ObsoleteSdkInt", "ClickableViewAccessibility", "WrongConstant",
            "UnspecifiedRegisterReceiverFlag", "SetTextI18n" })
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (preferences != null) {
            preferences.registerOnSharedPreferenceChangeListener(preferencesChangedListener);
        }

        ControllerUtils.startInputServer();
        ControllerUtils.nativeInitVibration();

        boolean vibrationEnabled = preferences.getBoolean(ENABLE_VIBRATION, ENABLE_VIBRATION_DEFAULT_VALUE);
        ControllerUtils.setVibrationEnabled(vibrationEnabled);

        ControllerUtils.setEnableXInputNative(getEnableXInput(selectedGameName));
        ControllerUtils.setEnableDInputNative(getEnableDInput(selectedGameName));

        prepareControllersMappings();

        getWindow().setFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | FLAG_KEEP_SCREEN_ON | FLAG_TRANSLUCENT_STATUS, 0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_emulation);

        drawerLayout = findViewById(R.id.DrawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        LogViewerFragment logViewerFragment = new LogViewerFragment();

        transaction.replace(R.id.logViewerContent, logViewerFragment);
        transaction.commit();

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                DrawerLayout.LayoutParams layoutParams = (DrawerLayout.LayoutParams) drawerView.getLayoutParams();

                if ((layoutParams.gravity & GravityCompat.END) == GravityCompat.END) {
                    logViewerFragment.populate();
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                logViewerFragment.cleanup();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        virtualKeyboardInputView = findViewById(R.id.overlayView);
        virtualKeyboardInputView.loadPreset(getSelectedVirtualControllerPreset(selectedGameName));
        virtualKeyboardInputView.setVisibility(View.INVISIBLE);

        virtualControllerInputView = findViewById(R.id.xInputOverlayView);
        virtualControllerInputView.setVisibility(View.INVISIBLE);

        NavigationView navigationView = findViewById(R.id.NavigationView);
        View headerViewMain = navigationView.getHeaderView(0);

        MaterialButton exitButton = headerViewMain.findViewById(R.id.exitButton);
        exitButton.setOnClickListener((v) -> {
            WineWrapper.killAll();

            disconnectController(virtualXInputControllerId);
            stopInputServer();
            cleanup();

            finishAffinity();
        });

        MaterialButton openKeyboardButton = headerViewMain.findViewById(R.id.openKeyboardButton);
        openKeyboardButton.setOnClickListener((v) -> {
            getLorieView().requestFocus();
            inputMethodManager.showSoftInput(getLorieView(), 0);
            drawerLayout.closeDrawers();
        });

        MaterialSwitch enableMangoHudSwitch = headerViewMain.findViewById(R.id.enableMangoHudSwitch);
        enableMangoHudSwitch
                .setChecked(preferences != null ? preferences.getBoolean(ENABLE_MANGOHUD, ENABLE_MANGOHUD_DEFAULT_VALUE)
                        : ENABLE_MANGOHUD_DEFAULT_VALUE);
        enableMangoHudSwitch.setOnClickListener((v) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ENABLE_MANGOHUD, enableMangoHudSwitch.isChecked());
            editor.apply();

            setSharedVars(this);
            generateMangoHUDConfFile();
        });

        MaterialSwitch openCloseVirtualControllerSwitch = headerViewMain
                .findViewById(R.id.openCloseVirtualControllerSwitch);
        openCloseVirtualControllerSwitch.setOnClickListener((v) -> {
            boolean isControllerInput = getVirtualControllerXInput(selectedGameName);

            if (openCloseVirtualControllerSwitch.isChecked()) {
                if (isControllerInput) {
                    virtualKeyboardInputView.setVisibility(View.INVISIBLE);
                    virtualControllerInputView.setVisibility(View.VISIBLE);

                    if (virtualXInputControllerId == -1) {
                        virtualXInputControllerId = connectController();
                    }
                } else {
                    virtualKeyboardInputView.setVisibility(View.VISIBLE);
                    virtualControllerInputView.setVisibility(View.INVISIBLE);
                }
            } else {
                virtualKeyboardInputView.setVisibility(View.INVISIBLE);
                virtualControllerInputView.setVisibility(View.INVISIBLE);

                if (isControllerInput) {
                    disconnectController(virtualXInputControllerId);
                    virtualXInputControllerId = -1;
                }
            }
        });

        MaterialButton openLogView = headerViewMain.findViewById(R.id.openLogViewer);
        openLogView.setOnClickListener((v) -> {
            drawerLayout.openDrawer(GravityCompat.END);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        MaterialButton editVirtualControllerMappingButton = headerViewMain
                .findViewById(R.id.editVirtualControllerMapping);
        editVirtualControllerMappingButton.setOnClickListener(
                (v) -> new VirtualControllerSettingsFragment().show(getSupportFragmentManager(), ""));

        MaterialButton editControllerMappingButton = headerViewMain.findViewById(R.id.editControllerMapping);
        editControllerMappingButton
                .setOnClickListener((v) -> new ControllerSettingsFragment().show(getSupportFragmentManager(), ""));

        MaterialButton startControllerPresetManagerButton = headerViewMain
                .findViewById(R.id.startControllerPresetManager);
        startControllerPresetManagerButton.setOnClickListener((v) -> {
            Intent intent = new Intent(this, PresetManagerActivity.class);
            intent.putExtra("presetType", CONTROLLER_PRESET);
            startActivity(intent);
        });

        MaterialButton startVirtualControllerPresetManagerButton = headerViewMain
                .findViewById(R.id.startVirtualControllerPresetManager);
        startVirtualControllerPresetManagerButton.setOnClickListener((v) -> {
            Intent intent = new Intent(this, PresetManagerActivity.class);
            intent.putExtra("presetType", VIRTUAL_CONTROLLER_PRESET);
            startActivity(intent);
        });

        MaterialButton pauseEmulationButton = headerViewMain.findViewById(R.id.pauseEmulation);
        pauseEmulationButton.setOnClickListener((v) -> {
            if (emulationPaused) {
                runCommand("pkill -SIGCONT -f .exe", false);
                pauseEmulationButton.setText(R.string.pause_emulation);
                pauseEmulationButton.setIcon(AppCompatResources.getDrawable(this, android.R.drawable.ic_media_pause));
            } else {
                runCommand("pkill -SIGSTOP -f .exe", false);
                pauseEmulationButton.setText(R.string.continue_emulation);
                pauseEmulationButton.setIcon(AppCompatResources.getDrawable(this, android.R.drawable.ic_media_play));
            }
            emulationPaused = !emulationPaused;
        });

        MaterialButton openTaskMgrButton = headerViewMain.findViewById(R.id.openTaskMgr);
        openTaskMgrButton.setOnClickListener((v) -> new TaskManagerFragment().show(getSupportFragmentManager(), ""));

        MaterialButton saveLayout = findViewById(R.id.saveLayout);
        saveLayout.setOnClickListener((v) -> {
            virtualControllerInputView.saveLayout();
            virtualControllerInputView.setEditing(false);
            saveLayout.setVisibility(View.GONE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        });

        MaterialButton editVirtualControllerLayout = headerViewMain.findViewById(R.id.editVirtualControllerLayout);
        editVirtualControllerLayout.setOnClickListener((v) -> {
            drawerLayout.closeDrawers();
            virtualControllerInputView.setEditing(true);
            saveLayout.setVisibility(View.VISIBLE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        });

        TextView virtualControllerOpacityText = headerViewMain.findViewById(R.id.virtualControllerOpacityText);
        SeekBar virtualControllerOpacitySeekbar = headerViewMain.findViewById(R.id.virtualControllerOpacitySeekbar);

        virtualControllerOpacitySeekbar.setMin(0);
        virtualControllerOpacitySeekbar.setMax(255);
        virtualControllerOpacitySeekbar.setProgress(virtualControlOpacity);

        virtualControllerOpacityText.setText(virtualControllerOpacitySeekbar.getProgress() + "");

        virtualControllerOpacitySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                virtualControllerOpacityText.setText(i + "");
                virtualKeyboardInputView.setAlpha(i / 255f);
                virtualControllerInputView.setAlpha(i / 255f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt(VIRTUAL_CONTROL_OPACITY, seekBar.getProgress());
                editor.apply();

                new Thread(() -> setSharedVars(EmulationActivity.this)).start();
            }
        });

        GameSurfaceView lorieView = findViewById(R.id.lorieView);
        View lorieParent = (View) lorieView.getParent();

        Set<Integer> pressedKeys = new HashSet<>();
        mInputHandler = new TouchInputHandler(this, new InputEventSender(lorieView));
        mLorieKeyListener = (v, k, e) -> {
            if (e.getAction() == KeyEvent.ACTION_DOWN) {
                pressedKeys.add(k);
            } else if (e.getAction() == KeyEvent.ACTION_UP) {
                pressedKeys.remove(k);
            }

            if (pressedKeys.contains(KeyEvent.KEYCODE_ALT_LEFT) && pressedKeys.contains(KeyEvent.KEYCODE_Q)) {
                if (lorieView.hasPointerCapture()) {
                    lorieView.releasePointerCapture();
                }
            }

            if (k == KeyEvent.KEYCODE_ESCAPE && !(lorieView.hasPointerCapture())) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    drawerLayout.closeDrawers();
                }
            }

            if (k == KeyEvent.KEYCODE_BACK) {
                if (e.getScanCode() == 153 && e.getDevice().getKeyboardType() != InputDevice.KEYBOARD_TYPE_ALPHABETIC
                        || e.getScanCode() == 0) {
                    boolean pointerCaptured = lorieView.hasPointerCapture();
                    if (pointerCaptured) {
                        lorieView.releasePointerCapture();
                    }
                    if (e.getAction() == KeyEvent.ACTION_UP) {
                        if (virtualControllerInputView.isEditing) {
                            virtualControllerInputView.setEditing(false);
                            findViewById(R.id.saveLayout).setVisibility(View.GONE);
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                            return true;
                        }

                        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.openDrawer(GravityCompat.START);
                        } else {
                            drawerLayout.closeDrawers();
                        }
                    }

                    inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                    return true;
                }
            } else if (k == KeyEvent.KEYCODE_VOLUME_DOWN) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI);
                return true;
            } else if (k == KeyEvent.KEYCODE_VOLUME_UP) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI);
                return true;
            }

            ControllerUtils.updateButtonsState(e);

            return mInputHandler.sendKeyEvent(e);
        };

        lorieParent.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                lorieParent.requestUnbufferedDispatch(e);
            }

            switch (e.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY: {
                    lorieView.requestPointerCapture();
                    Toast.makeText(this, getString(R.string.mouse_captured), Toast.LENGTH_SHORT).show();
                    break;
                }
                case MotionEvent.BUTTON_SECONDARY: {
                    if (!lorieView.hasPointerCapture()) {
                        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.openDrawer(GravityCompat.START);
                        } else {
                            drawerLayout.closeDrawers();
                        }

                        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                        return true;
                    }
                }
            }

            return mInputHandler.handleTouchEvent(lorieParent, lorieView, e);
        });
        lorieParent.setOnHoverListener((v, e) -> mInputHandler.handleTouchEvent(lorieParent, lorieView, e));
        lorieParent.setOnGenericMotionListener((v, e) -> mInputHandler.handleTouchEvent(lorieParent, lorieView, e));
        lorieView.setOnCapturedPointerListener((v, e) -> mInputHandler.handleTouchEvent(lorieView, lorieView, e));
        lorieParent.setOnCapturedPointerListener((v, e) -> mInputHandler.handleTouchEvent(lorieView, lorieView, e));
        lorieView.setOnKeyListener(mLorieKeyListener);

        lorieView.setCallback((sfc, surfaceWidth, surfaceHeight, screenWidth, screenHeight) -> {
            mInputHandler.handleHostSizeChanged(surfaceWidth, surfaceHeight);
            mInputHandler.handleClientSizeChanged(screenWidth, screenHeight);
        });

        lorieView.setOnFocusChangeListener((view, b) -> {
            if (!lorieView.isInLayout()) {
                lorieView.requestLayout();
            }
        });

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        tryConnect();
        onPreferencesChanged();

        if (SDK_INT >= VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PERMISSION_GRANTED
                && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, 0);
        }

        onReceiveConnection(getIntent());

        getSupportFragmentManager().setFragmentResultListener("invalidateControllerType", this,
                (requestKey, result) -> {
                    boolean isControllerInput = getVirtualControllerXInput(selectedGameName);

                    virtualKeyboardInputView.loadPreset(getSelectedVirtualControllerPreset(selectedGameName));
                    virtualKeyboardInputView.invalidate();

                    if (openCloseVirtualControllerSwitch.isChecked()) {
                        if (isControllerInput) {
                            virtualKeyboardInputView.setVisibility(View.INVISIBLE);
                            virtualControllerInputView.setVisibility(View.VISIBLE);
                        } else {
                            virtualKeyboardInputView.setVisibility(View.VISIBLE);
                            virtualControllerInputView.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        getLorieView().requestFocus();

        mLorieKeyListener.onKey(null, keyCode, event);

        return true;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event != null) {
            updateAxisState(event);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        // Unregister preference change listener
        if (preferences != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(preferencesChangedListener);
        }
        
        super.onDestroy();
    }

    public void onReceiveConnection(Intent intent) {
        tryConnect();
    }

    public void tryConnect() {
        if (GameSurfaceView.nativeIsSurfaceAvailable()) {
            getLorieView().triggerCallback();
            clientConnectedStateChanged();
        }
    }

    public void onPreferencesChanged() {
        handler.removeCallbacks(this::onPreferencesChangedCallback);
        handler.postDelayed(this::onPreferencesChangedCallback, 100);
    }

    @SuppressLint("UnsafeIntentLaunch")
    public void onPreferencesChangedCallback() {
        onWindowFocusChanged(hasWindowFocus());
        GameSurfaceView lorieView = getLorieView();

        String filter = preferences.getString(SELECTED_SCALING_FILTER, SCALING_FILTER_LINEAR);
        int filterId = 0;
        if (filter.equals(SCALING_FILTER_FSR)) filterId = 1;
        else if (filter.equals(SCALING_FILTER_CAS)) filterId = 2;
        lorieView.setScalingFilter(filterId);

        String frameGen = preferences.getString(SELECTED_FRAME_GENERATION, FRAME_GENERATION_OFF);
        int frameGenId = 0;
        if (frameGen.equals(FRAME_GENERATION_SMOOTHING)) frameGenId = 1;
        lorieView.setFrameGeneration(frameGenId);

        int fpsLimit = preferences.getInt(FPS_LIMIT, 0);
        lorieView.setVsync(fpsLimit > 0 ? 1 : 0);

        String frameSkipStr = preferences.getString(SELECTED_FRAMESKIP, FRAMESKIP_0);
        int skip = 0;
        if (frameSkipStr.equals(FRAMESKIP_1)) skip = 1;
        else if (frameSkipStr.equals(FRAMESKIP_2)) skip = 2;
        else if (frameSkipStr.equals(FRAMESKIP_3)) skip = 3;
        else if (frameSkipStr.equals(FRAMESKIP_4)) skip = 4;
        else if (frameSkipStr.equals(FRAMESKIP_5)) skip = 5;
        lorieView.setFrameSkip(skip);

        String colorProfile = preferences.getString(COLOR_PROFILE, COLOR_PROFILE_DEFAULT_VALUE);
        int colorProfileId = 0;
        if (colorProfile.equals("Vivid")) colorProfileId = 1;
        else if (colorProfile.equals("Warm")) colorProfileId = 2;
        else if (colorProfile.equals("Cool")) colorProfileId = 3;
        lorieView.setColorProfile(colorProfileId);

        // Update vibration setting
        boolean vibrationEnabled = preferences.getBoolean(ENABLE_VIBRATION, ENABLE_VIBRATION_DEFAULT_VALUE);
        ControllerUtils.setVibrationEnabled(vibrationEnabled);

        lorieView.triggerCallback();

        lorieView.requestLayout();
        lorieView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

        getLorieView().requestFocus();

        virtualKeyboardInputView.loadPreset(getSelectedVirtualControllerPreset(selectedGameName));

        virtualKeyboardInputView.setAlpha(virtualControlOpacity / 255f);
        virtualControllerInputView.setAlpha(virtualControlOpacity / 255f);

        prepareControllersMappings();

        statsHandler.post(statsRunnable);
    }

    @Override
    public void onPause() {
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);

        super.onPause();
        statsHandler.removeCallbacks(statsRunnable);
    }

    public GameSurfaceView getLorieView() {
        return findViewById(R.id.lorieView);
    }

    public Surface getSurface() {
        return getLorieView().getHolder().getSurface();
    }

    public boolean handleKey(KeyEvent e) {
        return mLorieKeyListener.onKey(getLorieView(), e.getKeyCode(), e);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView());

        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        if (hasFocus) {
            getLorieView().regenerate();
        }

        getLorieView().requestFocus();
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
        handler.postDelayed(() -> getLorieView().triggerCallback(), 100);
        return insets;
    }

    private void clientConnectedStateChanged() {
        runOnUiThread(() -> {
            boolean connected = GameSurfaceView.nativeIsSurfaceAvailable();

            getLorieView().setVisibility(connected ? View.VISIBLE : View.INVISIBLE);
            getLorieView().regenerate();

            if (!connected)
                tryConnect();
        });
    }

    private Handler statsHandler = new Handler();
    private ActivityManager cachedActivityManager = null;
    private final ActivityManager.MemoryInfo cachedMemoryInfo = new ActivityManager.MemoryInfo();

    private Runnable statsRunnable = new Runnable() {
        @Override
        public void run() {
            if (enableRamCounter) {
                updateMemoryInfo();
            }
            if (enableCpuCounter) {
                updateCpuInfo();
            }
            statsHandler.postDelayed(this, 2500); // 2.5s for less GC and IPC overhead
        }
    };

    private void updateMemoryInfo() {
        if (cachedActivityManager == null) {
            cachedActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        }
        cachedActivityManager.getMemoryInfo(cachedMemoryInfo);

        long totalMemory = cachedMemoryInfo.totalMem / (1024 * 1024);
        long availableMemory = cachedMemoryInfo.availMem / (1024 * 1024);
        long usedMemory = totalMemory - availableMemory;

        memoryStats = usedMemory + "/" + totalMemory;
    }

    private long prevTotalCpuTime = 0;
    private long prevIdleCpuTime = 0;

    private void updateCpuInfo() {
        try {
            List<String> lines = Files.readAllLines(new File("/proc/stat").toPath());
            String[] cpuStats = lines.get(0).split("\\s+");

            // /proc/stat columns: cpu (0), user(1), nice(2), system(3), idle(4), iowait(5),
            // irq(6), softirq(7), steal(8), guest(9), guest_nice(10)
            long user = Long.parseLong(cpuStats[1]);
            long nice = Long.parseLong(cpuStats[2]);
            long system = Long.parseLong(cpuStats[3]);
            long idle = Long.parseLong(cpuStats[4]);
            long iowait = Long.parseLong(cpuStats[5]);
            long irq = Long.parseLong(cpuStats[6]);
            long softirq = Long.parseLong(cpuStats[7]);
            long steal = Long.parseLong(cpuStats[8]);

            long totalCpuTime = user + nice + system + idle + iowait + irq + softirq + steal;
            long idleCpuTime = idle + iowait;

            if (prevTotalCpuTime != 0) {
                long totalDiff = totalCpuTime - prevTotalCpuTime;
                long idleDiff = idleCpuTime - prevIdleCpuTime;
                long usage = totalDiff - idleDiff;

                // Avoid division by zero
                if (totalDiff > 0) {
                    int usagePercent = (int) ((usage * 100) / totalDiff);
                    totalCpuUsage = usagePercent + "%";
                }
            }

            prevTotalCpuTime = totalCpuTime;
            prevIdleCpuTime = idleCpuTime;

        } catch (IOException | NumberFormatException ignored) {
        }
    }
}
