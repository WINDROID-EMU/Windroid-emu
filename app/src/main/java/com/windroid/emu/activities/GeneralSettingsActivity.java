package com.windroid.emu.activities;

import static android.os.Build.VERSION.SDK_INT;

import static com.windroid.emu.activities.MainActivity.setSharedVars;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.windroid.emu.R;
import com.windroid.emu.databinding.ActivityGeneralSettingsBinding;
import com.windroid.emu.fragments.Box64SettingsFragment;
import com.windroid.emu.fragments.FEXCoreSettingsFragment;
import com.windroid.emu.fragments.DebugSettingsFragment;
import com.windroid.emu.fragments.DriverInfoFragment;
import com.windroid.emu.fragments.DriversSettingsFragment;
import com.windroid.emu.fragments.DriverSettingsContainerFragment;
import com.windroid.emu.fragments.PackageManagerContainerFragment;
import com.windroid.emu.fragments.EnvVarsSettingsFragment;
import com.windroid.emu.fragments.GeneralSettingsFragment;
import com.windroid.emu.fragments.SoundSettingsFragment;
import com.windroid.emu.fragments.WineSettingsFragment;
import com.windroid.emu.fragments.WinetricksFragment;
import com.windroid.emu.fragments.GraphicEngineSettingsFragment;

public class GeneralSettingsActivity extends AppCompatActivity {
    private Toolbar generalSettingsToolbar;
    private boolean openedDirectly = false;
    private String initialPreference = null;
    private boolean isAtContainerLevel = false;
    private final Box64SettingsFragment box64SettingsFragment = new Box64SettingsFragment();
    private final FEXCoreSettingsFragment fexcoreSettingsFragment = new FEXCoreSettingsFragment();
    private final DebugSettingsFragment debugSettingsFragment = new DebugSettingsFragment();
    private final DriverInfoFragment driverInfoFragment = new DriverInfoFragment();
    private final DriversSettingsFragment driversSettingsFragment = new DriversSettingsFragment();
    private final DriverSettingsContainerFragment driverSettingsContainerFragment = new DriverSettingsContainerFragment();
    private final PackageManagerContainerFragment packageManagerContainerFragment = new PackageManagerContainerFragment();
    private final EnvVarsSettingsFragment envVarsSettingsFragment = new EnvVarsSettingsFragment();
    private final SoundSettingsFragment soundSettingsFragment = new SoundSettingsFragment();
    private final WineSettingsFragment wineSettingsFragment = new WineSettingsFragment();
    private final WinetricksFragment winetricksFragment = new WinetricksFragment();
    private final GraphicEngineSettingsFragment graphicEngineSettingsFragment = new GraphicEngineSettingsFragment();
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String preference = intent.getStringExtra("preference");
            if (preference == null)
                return;

            generalSettingsToolbar.setTitle(preference);

            if (ACTION_PREFERENCE_SELECT.equals(intent.getAction())) {
                if (preference.equals(getString(R.string.box64_settings_title))) {
                    fragmentLoader(box64SettingsFragment, false);
                } else if (preference.equals(getString(R.string.fexcore_settings_title))) {
                    fragmentLoader(fexcoreSettingsFragment, false);
                } else if (preference.equals(getString(R.string.debug_settings_title))) {
                    fragmentLoader(debugSettingsFragment, false);
                } else if (preference.equals(getString(R.string.driver_settings_container_title))) {
                    fragmentLoader(driverSettingsContainerFragment, false);
                } else if (preference.equals(getString(R.string.package_manager_title))) {
                    fragmentLoader(packageManagerContainerFragment, false);
                } else if (preference.equals(getString(R.string.driver_settings_title))) {
                    fragmentLoader(driversSettingsFragment, false);
                } else if (preference.equals(getString(R.string.driver_info_title))) {
                    fragmentLoader(driverInfoFragment, false);
                } else if (preference.equals(getString(R.string.env_settings_title))) {
                    fragmentLoader(envVarsSettingsFragment, false);
                } else if (preference.equals(getString(R.string.sound_settings_title))) {
                    fragmentLoader(soundSettingsFragment, false);
                } else if (preference.equals(getString(R.string.wine_settings_title))) {
                    fragmentLoader(wineSettingsFragment, false);
                } else if (preference.equals(getString(R.string.graphic_engine_settings_title))) {
                    fragmentLoader(graphicEngineSettingsFragment, false);
                } else if (preference.equals(getString(R.string.winetricks_title))) {
                    fragmentLoader(winetricksFragment, false);
                }
            }
        }
    };

    public static void scanGames(File folder, Context context) {
        if (folder == null)
            return;

        File[] files = folder.listFiles();
        if (files == null) {
            android.util.Log.e("GameScanner", "Failed to list files in: " + folder.getPath());
            return;
        }

        // Show start Toast on UI thread
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> android.widget.Toast
                .makeText(context, "Scanning Games...", android.widget.Toast.LENGTH_SHORT).show());

        scanGamesRecursive(folder);

        // Show completion Toast on UI thread
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            com.windroid.emu.fragments.ShortcutsFragment.updateShortcuts();
            android.widget.Toast.makeText(context, "Scan Completed", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private static final long MIN_GAME_SIZE = 1024 * 1024; // 1MB

    private static final String[] BLACKLISTED_FOLDERS = {
            "__Installer", "CommonRedist", "Redist", "DirectX", "Support", "Prerequisites", "installers",
            "redist", "directx", "support", "prerequisites", "Engine", "MonoBleedingEdge", "_CommonRedist",
            "DotNet", "Tools", "Steamworks", "Overlay", "Dependencies", "PhysX", "OpenAL", "PunkBuster"
    };

    private static final String[] BLACKLISTED_EXECUTABLES = {
            "unins", "dxwebsetup", "vcredist", "crash", "redist", "unitycrashhandler",
            "cleanup", "touchup", "activation", "regist", "eapatch", "easyanticheat",
            "gmlive-server", "pbsvc", "eaproxyinstaller", "eacoreserver", "patchprogress", "pnkbstra",
            "createdump", "updater", "crashhandler", "socialclub", "webhelper", "diagnostics",
            "benchmark", "overlay", "errorreporter", "feedback", "register", "repair", "install",
            "vulkaninfo", "vulkandriverquery", "physxcudacheck", "vc_redist"
    };

    private static final String[] EXACT_BLACKLISTED_EXECUTABLES = {
            "setup", "config", "testapp", "testeapp", "settings", "launcher", "unins000", "unins001", "uninstall",
            "dxsetup", "physx", "vcredist_x64", "vcredist_x86"
    };

    private static void scanGamesRecursive(File folder) {
        File[] files = folder.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            String name = file.getName();

            if (file.isDirectory()) {
                if (name.startsWith(".")) {
                    continue;
                }
                boolean isBlacklistedFolder = false;
                for (String badFolder : BLACKLISTED_FOLDERS) {
                    if (name.equalsIgnoreCase(badFolder)) {
                        isBlacklistedFolder = true;
                        break;
                    }
                }
                if (!isBlacklistedFolder) {
                    scanGamesRecursive(file);
                }
            } else if (name.toLowerCase().endsWith(".exe")) {
                if (file.length() < MIN_GAME_SIZE) {
                    android.util.Log.d("GameScanner", "Skipping " + name + " (too small: " + file.length() + " bytes)");
                    continue;
                }

                String lowerName = name.toLowerCase();
                String nameWithoutExtension = name.substring(0, name.length() - 4).toLowerCase();

                boolean isBlacklisted = false;
                for (String badWord : BLACKLISTED_EXECUTABLES) {
                    if (lowerName.contains(badWord)) {
                        isBlacklisted = true;
                        break;
                    }
                }

                if (!isBlacklisted) {
                    for (String badWord : EXACT_BLACKLISTED_EXECUTABLES) {
                        if (nameWithoutExtension.equals(badWord)) {
                            isBlacklisted = true;
                            break;
                        }
                    }
                }

                if (isBlacklisted) {
                    android.util.Log.d("GameScanner", "Skipping blacklisted executable: " + name);
                    continue;
                }

                String prettyName = name.substring(0, name.length() - 4);
                String iconPath = "";

                try {
                    File iconFile = new File(com.windroid.emu.activities.MainActivity.usrDir,
                            "icons/" + prettyName + "-thumbnail");
                    com.windroid.emu.core.WineWrapper.extractIcon(file.getPath(), iconFile.getPath());
                    if (iconFile.exists() && iconFile.length() > 0) {
                        iconPath = iconFile.getPath();
                    }
                } catch (Exception e) {
                    android.util.Log.e("GameScanner", "Icon extraction failed for: " + name, e);
                }

                if (!iconPath.isEmpty()) {
                    com.windroid.emu.fragments.ShortcutsFragment.addGameToList(file.getPath(), prettyName, iconPath);
                } else {
                    android.util.Log.d("GameScanner", "Skipping " + name + " (no icon found)");
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGeneralSettingsBinding binding = ActivityGeneralSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fragmentLoader(new GeneralSettingsFragment(), true);

        generalSettingsToolbar = findViewById(R.id.generalSettingsToolbar);
        generalSettingsToolbar.setTitle(R.string.general_settings);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener((v) -> onKeyDown(KeyEvent.KEYCODE_BACK, null));

        androidx.core.content.ContextCompat.registerReceiver(this, receiver, new IntentFilter(ACTION_PREFERENCE_SELECT),
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);

        // Handle direct navigation to driver settings container
        String preference = getIntent().getStringExtra("preference");
        if (preference != null && preference.equals(getString(R.string.driver_settings_container_title))) {
            openedDirectly = true;
            initialPreference = getString(R.string.driver_settings_container_title);
            isAtContainerLevel = true;
            generalSettingsToolbar.setTitle(preference);
            fragmentLoader(driverSettingsContainerFragment, true); // Add to backstack
        } else if (preference != null && preference.equals(getString(R.string.package_manager_title))) {
            openedDirectly = true;
            initialPreference = getString(R.string.package_manager_title);
            isAtContainerLevel = true;
            generalSettingsToolbar.setTitle(preference);
            fragmentLoader(packageManagerContainerFragment, true); // Add to backstack
        } else if (preference != null && preference.equals(getString(R.string.debug_settings_title))) {
            openedDirectly = true;
            initialPreference = getString(R.string.debug_settings_title);
            isAtContainerLevel = true;
            generalSettingsToolbar.setTitle(preference);
            fragmentLoader(debugSettingsFragment, true); // Add to backstack
        } else if (preference != null && preference.equals(getString(R.string.sound_settings_title))) {
            openedDirectly = true;
            initialPreference = getString(R.string.sound_settings_title);
            isAtContainerLevel = true;
            generalSettingsToolbar.setTitle(preference);
            fragmentLoader(soundSettingsFragment, true); // Add to backstack
        } else if (preference != null && preference.equals(getString(R.string.env_settings_title))) {
            openedDirectly = true;
            initialPreference = getString(R.string.env_settings_title);
            isAtContainerLevel = true;
            generalSettingsToolbar.setTitle(preference);
            fragmentLoader(envVarsSettingsFragment, true); // Add to backstack
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setSharedVars(this);
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackCount > 0) {
                getSupportFragmentManager().popBackStack();
                // After popping, check the new back stack count
                int newBackStackCount = getSupportFragmentManager().getBackStackEntryCount();
                if (newBackStackCount == 0) {
                    // No more back stack entries
                    if (openedDirectly) {
                        // We were opened directly, finish the activity
                        finish();
                    } else {
                        // Normal navigation, show general settings
                        generalSettingsToolbar.setTitle(R.string.general_settings);
                    }
                } else if (openedDirectly && isAtContainerLevel && newBackStackCount == 1) {
                    // We're back at the container level (one entry left in backstack)
                    if (initialPreference != null) {
                        generalSettingsToolbar.setTitle(initialPreference);
                    }
                } else if (!openedDirectly) {
                    generalSettingsToolbar.setTitle(R.string.general_settings);
                }
            } else {
                // No more back stack entries, finish the activity
                finish();
            }
        }

        return true;
    }

    private void fragmentLoader(Fragment fragment, boolean appInit) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out);

        transaction.replace(R.id.settings_content, fragment);
        if (!appInit)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    public final static String ACTION_PREFERENCE_SELECT = "com.windroid.emu.ACTION_PREFERENCE_SELECT";
    public final static int SWITCH = 1;
    public final static int SPINNER = 2;
    public final static int CHECKBOX = 3;
    public final static int SEEKBAR = 4;

    public final static String BOX64_LOG = "BOX64_LOG";
    public final static int BOX64_LOG_DEFAULT_VALUE = 1;
    public final static String BOX64_MMAP32 = "BOX64_MMAP32";
    public final static int BOX64_MMAP32_DEFAULT_VALUE = 1;
    public final static String BOX64_AVX = "BOX64_AVX";
    public final static int BOX64_AVX_DEFAULT_VALUE = 2;
    public final static String BOX64_SSE42 = "BOX64_SSE42";
    public final static int BOX64_SSE42_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_BIGBLOCK = "BOX64_DYNAREC_BIGBLOCK";
    public final static int BOX64_DYNAREC_BIGBLOCK_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_STRONGMEM = "BOX64_DYNAREC_STRONGMEM";
    public final static int BOX64_DYNAREC_STRONGMEM_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_WEAKBARRIER = "BOX64_DYNAREC_WEAKBARRIER";
    public final static int BOX64_DYNAREC_WEAKBARRIER_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_PAUSE = "BOX64_DYNAREC_PAUSE";
    public final static int BOX64_DYNAREC_PAUSE_DEFAULT_VALUE = 0;
    public final static String BOX64_DYNAREC_X87DOUBLE = "BOX64_DYNAREC_X87DOUBLE";
    public final static int BOX64_DYNAREC_X87DOUBLE_DEFAULT_VALUE = 0;
    public final static String BOX64_DYNAREC_FASTNAN = "BOX64_DYNAREC_FASTNAN";
    public final static int BOX64_DYNAREC_FASTNAN_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_FASTROUND = "BOX64_DYNAREC_FASTROUND";
    public final static int BOX64_DYNAREC_FASTROUND_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_SAFEFLAGS = "BOX64_DYNAREC_SAFEFLAGS";
    public final static int BOX64_DYNAREC_SAFEFLAGS_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_CALLRET = "BOX64_DYNAREC_CALLRET";
    public final static int BOX64_DYNAREC_CALLRET_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_DF = "BOX64_DYNAREC_DF";
    public final static int BOX64_DYNAREC_DF_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_ALIGNED_ATOMICS = "BOX64_DYNAREC_ALIGNED_ATOMICS";
    public final static int BOX64_DYNAREC_ALIGNED_ATOMICS_DEFAULT_VALUE = 0;
    public final static String BOX64_DYNAREC_NATIVEFLAGS = "BOX64_DYNAREC_NATIVEFLAGS";
    public final static int BOX64_DYNAREC_NATIVEFLAGS_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_WAIT = "BOX64_DYNAREC_WAIT";
    public final static int BOX64_DYNAREC_WAIT_DEFAULT_VALUE = 1;
    public final static String BOX64_DYNAREC_DIRTY = "BOX64_DYNAREC_DIRTY";
    public final static int BOX64_DYNAREC_DIRTY_DEFAULT_VALUE = 0;
    public final static String BOX64_DYNAREC_FORWARD = "BOX64_DYNAREC_FORWARD";
    public final static int BOX64_DYNAREC_FORWARD_DEFAULT_VALUE = 128;
    public final static String BOX64_SHOWSEGV = "BOX64_SHOWSEGV";
    public final static boolean BOX64_SHOWSEGV_DEFAULT_VALUE = false;
    public final static String BOX64_SHOWBT = "BOX64_SHOWBT";
    public final static boolean BOX64_SHOWBT_DEFAULT_VALUE = false;
    public final static String BOX64_NOSIGSEGV = "BOX64_NOSIGSEGV";
    public final static boolean BOX64_NOSIGSEGV_DEFAULT_VALUE = false;
    public final static String BOX64_NOSIGILL = "BOX64_NOSIGILL";
    public final static boolean BOX64_NOSIGILL_DEFAULT_VALUE = false;

    public final static String SELECTED_BOX64 = "selectedBox64";
    public final static String SELECTED_EMULATOR = "selectedEmulator";
    public final static String EMULATOR_BOX64 = "Box64";
    public final static String EMULATOR_FEXCORE = "FEXCore";
    public final static String SELECTED_FEXCORE = "selectedFEXCore";
    public final static String SELECTED_FEXCORE_PRESET = "selectedFEXCorePreset";
    public final static String FEXCORE_PRESET_DEFAULT = "Balanced";
    public final static String FEX_TSO_ENABLED = "FEX_TSOENABLED";
    public final static boolean FEX_TSO_ENABLED_DEFAULT_VALUE = true;
    public final static String FEX_MULTI_BLOCK = "FEX_MULTIBLOCK";
    public final static boolean FEX_MULTI_BLOCK_DEFAULT_VALUE = true;
    public final static String FEX_X87_REDUCED_PRECISION = "FEX_X87REDUCEDPRECISION";
    public final static boolean FEX_X87_REDUCED_PRECISION_DEFAULT_VALUE = false;
    public final static String FEX_SILENT_LOG = "FEX_SILENTLOG";
    public final static boolean FEX_SILENT_LOG_DEFAULT_VALUE = true;
    public final static String FEX_SMC_CHECKS = "FEX_SMCCHECKS";
    public final static String FEX_SMC_CHECKS_DEFAULT_VALUE = "app";
    public final static String FEX_VECTOR_WIDTH = "FEX_VECTOR_WIDTH";
    public final static String FEX_VECTOR_WIDTH_DEFAULT_VALUE = "128";
    public final static String SELECTED_VULKAN_DRIVER = "selectedVulkanDriver";
    public final static String SELECTED_WINE_PREFIX = "selectedWinePrefix";
    public final static String SELECTED_TU_DEBUG_PRESET = "selectedTuDebugPreset";
    public final static String SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE = "noconform,sysmem";
    public final static String SELECTED_TU_TEXTURE_LOD_BIAS = "selectedTuTextureLodBias";
    public final static String SELECTED_TU_TEXTURE_LOD_BIAS_DEFAULT_VALUE = "Off";
    public final static String SELECTED_TU_FORCE_MIP_LEVEL = "selectedTuForceMipLevel";
    public final static String SELECTED_TU_FORCE_MIP_LEVEL_DEFAULT_VALUE = "Off";
    public final static String ENABLE_TU_FORCE_SHADING_RATE = "enableTuForceShadingRate";
    public final static boolean ENABLE_TU_FORCE_SHADING_RATE_DEFAULT_VALUE = false;
    public final static String ENABLE_TU_FORCE_SYSMEM = "enableTuForceSysmem";
    public final static boolean ENABLE_TU_FORCE_SYSMEM_DEFAULT_VALUE = false;
    public final static String ENABLE_TU_DISABLE_SHADOWS = "enableTuDisableShadows";
    public final static boolean ENABLE_TU_DISABLE_SHADOWS_DEFAULT_VALUE = false;
    public final static String ENABLE_TU_DISABLE_HEAVY_EFFECTS = "enableTuDisableHeavyEffects";
    public final static boolean ENABLE_TU_DISABLE_HEAVY_EFFECTS_DEFAULT_VALUE = false;
    public final static String SELECTED_VRAM_LIMIT = "selectedVramLimit";
    public final static String SELECTED_VRAM_LIMIT_DEFAULT_VALUE = "Auto";
    public final static String ENABLE_DRI3 = "enableDRI3";
    public final static boolean ENABLE_DRI3_DEFAULT_VALUE = true;
    public final static String ENABLE_MANGOHUD = "enableMangoHUD";
    public final static boolean ENABLE_MANGOHUD_DEFAULT_VALUE = false;
    public final static String WINE_LOG_LEVEL = "wineLogLevel";
    public final static String WINE_LOG_LEVEL_DEFAULT_VALUE = "default";
    public final static String SELECTED_GL_PROFILE = "selectedGLProfile";
    public final static String SELECTED_GL_PROFILE_DEFAULT_VALUE = "GL 3.2";

    public final static String SELECTED_DXVK_HUD_PRESET = "selectedDXVKHudPreset";
    public final static String SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE = "fps,gpuload";
    public final static String SELECTED_MESA_VK_WSI_PRESENT_MODE = "MESA_VK_WSI_PRESENT_MODE";
    public final static String SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE = "mailbox";

    public final static String DEAD_ZONE = "deadZone";
    public final static String MOUSE_SENSIBILITY = "mouseSensibility";
    public static final String FPS_LIMIT = "fpsLimit";
    public static final String VIRTUAL_CONTROL_OPACITY = "virtualControlOpacity";
    public static final int VIRTUAL_CONTROL_OPACITY_DEFAULT_VALUE = 255;
    public final static String PA_SINK = "pulseAudioSink";
    public final static String PA_SINK_DEFAULT_VALUE = "SLES";
    public final static String WINE_DPI = "wineDpi";
    public final static int WINE_DPI_DEFAULT_VALUE = 96;
    public final static String WINE_DPI_APPLIED = "wineDpiApplied";
    public final static boolean WINE_DPI_APPLIED_DEFAULT_VALUE = false;
    public final static String DISPLAY_STRETCH = "displayStretch";
    public final static boolean DISPLAY_STRETCH_DEFAULT_VALUE = true;

    public final static String ENABLE_AFME = "enableAFME";
    public final static boolean ENABLE_AFME_DEFAULT_VALUE = false;

    public final static String PERF_MODE_ROOT = "perfModeRoot";
    public final static boolean PERF_MODE_ROOT_DEFAULT_VALUE = false;

    public final static String SELINUX_PERMISSIVE_ROOT = "selinuxPermissiveRoot";
    public final static boolean SELINUX_PERMISSIVE_ROOT_DEFAULT_VALUE = false;

    public final static String SELECTED_SCALING_FILTER = "selectedScalingFilter";
    public final static String SCALING_FILTER_LINEAR = "Linear";
    public final static String SCALING_FILTER_FSR = "FSR (AI Upscaling)";
    public final static String SCALING_FILTER_CAS = "CAS (Sharpness)";

    public final static String SELECTED_FRAME_GENERATION = "selectedFrameGeneration";
    public final static String FRAME_GENERATION_OFF = "Off";
    public final static String FRAME_GENERATION_SMOOTHING = "Smoothing (Interpolation)";

    public final static String SELECTED_FRAMESKIP = "selectedFrameSkip";
    public final static String FRAMESKIP_0 = "0 (Off)";
    public final static String FRAMESKIP_1 = "1";
    public final static String FRAMESKIP_2 = "2";
    public final static String FRAMESKIP_3 = "3";
    public final static String FRAMESKIP_4 = "4";
    public final static String FRAMESKIP_5 = "5";

    public final static String SELECTED_WINE_FRAMESKIP = "selectedWineFrameSkip";
    public final static String WINE_FRAMESKIP_0 = "0 (Off)";
    public final static String WINE_FRAMESKIP_1 = "1";
    public final static String WINE_FRAMESKIP_2 = "2";
    public final static String WINE_FRAMESKIP_3 = "3";

    public final static String SUPER_RESOLUTION = "superResolution";
    public final static boolean SUPER_RESOLUTION_DEFAULT_VALUE = false;

    public final static String COLOR_PROFILE = "colorProfile";
    public final static String COLOR_PROFILE_DEFAULT_VALUE = "Neutral";
    public final static String WINE_FSR_MODE = "wineFsrMode";
    public final static String WINE_FSR_MODE_DEFAULT_VALUE = "Ultra Quality";
    public final static String WINE_FSR_SHARPNESS = "wineFsrSharpness";
    public final static int WINE_FSR_SHARPNESS_DEFAULT_VALUE = 2;

    public final static String WINE_HIDE = "wineHide";
    public final static boolean WINE_HIDE_DEFAULT_VALUE = false;

    public final static String ENABLE_VIBRATION = "enableVibration";
    public final static boolean ENABLE_VIBRATION_DEFAULT_VALUE = true;
}
