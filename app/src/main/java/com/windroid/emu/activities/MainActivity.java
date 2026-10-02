package com.windroid.emu.activities;

import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_LOG;
import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_LOG_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_NOSIGILL;
import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_NOSIGILL_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_NOSIGSEGV;
import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_NOSIGSEGV_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_SHOWBT;
import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_SHOWBT_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_SHOWSEGV;
import static com.windroid.emu.activities.GeneralSettingsActivity.BOX64_SHOWSEGV_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_DRI3;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_DRI3_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_AFME;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_AFME_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.FPS_LIMIT;
import static com.windroid.emu.activities.GeneralSettingsActivity.PA_SINK;
import static com.windroid.emu.activities.GeneralSettingsActivity.PA_SINK_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.PERF_MODE_ROOT;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELINUX_PERMISSIVE_ROOT;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_DXVK_HUD_PRESET;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_GL_PROFILE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_GL_PROFILE_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_MESA_VK_WSI_PRESENT_MODE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_TU_DEBUG_PRESET;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_TU_TEXTURE_LOD_BIAS;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_TU_TEXTURE_LOD_BIAS_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_TU_FORCE_MIP_LEVEL;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_TU_FORCE_MIP_LEVEL_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_TU_FORCE_SHADING_RATE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_TU_FORCE_SHADING_RATE_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_TU_FORCE_SYSMEM;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_TU_FORCE_SYSMEM_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_TU_DISABLE_SHADOWS;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_TU_DISABLE_SHADOWS_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_TU_DISABLE_HEAVY_EFFECTS;
import static com.windroid.emu.activities.GeneralSettingsActivity.ENABLE_TU_DISABLE_HEAVY_EFFECTS_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_SCALING_FILTER;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_FRAME_GENERATION;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_FRAMESKIP;
import static com.windroid.emu.activities.GeneralSettingsActivity.SCALING_FILTER_LINEAR;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAME_GENERATION_OFF;
import static com.windroid.emu.activities.GeneralSettingsActivity.FRAMESKIP_0;
import static com.windroid.emu.activities.GeneralSettingsActivity.SUPER_RESOLUTION;
import static com.windroid.emu.activities.GeneralSettingsActivity.SUPER_RESOLUTION_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_FSR_MODE;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_FSR_MODE_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_FSR_SHARPNESS;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_FSR_SHARPNESS_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.COLOR_PROFILE;
import static com.windroid.emu.activities.GeneralSettingsActivity.COLOR_PROFILE_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_VRAM_LIMIT;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_VRAM_LIMIT_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_DPI;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_DPI_APPLIED;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_DPI_APPLIED_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_DPI_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_LOG_LEVEL;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_LOG_LEVEL_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_HIDE;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_HIDE_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_WINE_FRAMESKIP;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_FRAMESKIP_0;

import static com.windroid.emu.activities.PresetManagerActivity.SELECTED_BOX64_PRESET;
import static com.windroid.emu.activities.RatManagerActivity.generateICDFile;
import static com.windroid.emu.activities.RatManagerActivity.generateMangoHUDConfFile;
import static com.windroid.emu.activities.WelcomeActivity.finishedWelcomeScreen;
import static com.windroid.emu.adapters.AdapterGame.selectedGameName;
import static com.windroid.emu.controller.ControllerUtils.connectedPhysicalControllers;
import static com.windroid.emu.controller.ControllerUtils.disconnectController;
import static com.windroid.emu.controller.ControllerUtils.prepareControllersMappings;
import static com.windroid.emu.core.EnvVars.getEnv;
import static com.windroid.emu.core.RatPackageManager.checkPackageInstalled;
import static com.windroid.emu.core.RatPackageManager.getPackageById;
import static com.windroid.emu.core.RatPackageManager.installADToolsDriver;
import static com.windroid.emu.core.RatPackageManager.installRat;
import static com.windroid.emu.core.RatPackageManager.installablePackagesCategories;
import static com.windroid.emu.core.RatPackageManager.listRatPackages;
import static com.windroid.emu.core.ShellLoader.runCommand;
import static com.windroid.emu.core.ShellLoader.runCommandWithOutput;
import static com.windroid.emu.core.WineWrapper.getCpuHexMask;
import static com.windroid.emu.core.WineWrapper.getSanitizedPath;
import static com.windroid.emu.fragments.AskInstallPackageFragment.ADTOOLS_DRIVER_PACKAGE;
import static com.windroid.emu.fragments.AskInstallPackageFragment.MWP_PRESET_PACKAGE;
import static com.windroid.emu.fragments.AskInstallPackageFragment.RAT_PACKAGE;
import static com.windroid.emu.fragments.AskInstallPackageFragment.adToolsDriverCandidate;
import static com.windroid.emu.fragments.AskInstallPackageFragment.mwpPresetCandidate;
import static com.windroid.emu.fragments.AskInstallPackageFragment.ratCandidate;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64AlignedAtomics;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64Avx;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64BigBlock;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64CallRet;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64DF;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64Dirty;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64FastNan;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64FastRound;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64Forward;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64MMap32;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64NativeFlags;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64Pause;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64SafeFlags;
import com.windroid.emu.utils.RootUtils;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64Sse42;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64StrongMem;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64Wait;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64WeakBarrier;
import static com.windroid.emu.fragments.Box64PresetManagerFragment.getBox64X87Double;
import static com.windroid.emu.fragments.ControllerSettingsFragment.ACTION_UPDATE_CONTROLLERS_STATUS;
import static com.windroid.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.windroid.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.windroid.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.windroid.emu.fragments.DebugSettingsFragment.availableCPUs;
import static com.windroid.emu.fragments.EditGamePreferencesFragment.FILE_MANAGER_START_PREFERENCES;
import static com.windroid.emu.fragments.FileManagerFragment.refreshFiles;
import static com.windroid.emu.fragments.FloatingFileManagerFragment.OPERATION_SELECT_EXE;
import static com.windroid.emu.fragments.FloatingFileManagerFragment.OPERATION_SELECT_ICON;
import static com.windroid.emu.fragments.FloatingFileManagerFragment.OPERATION_SELECT_RAT;
import static com.windroid.emu.fragments.RootFSDownloaderFragment.rootFSIsDownloaded;
import static com.windroid.emu.fragments.SetupFragment.abortSetup;
import static com.windroid.emu.fragments.ShortcutsFragment.ADRENO_TOOLS_DRIVER;
import static com.windroid.emu.fragments.ShortcutsFragment.MESA_DRIVER;
import static com.windroid.emu.fragments.ShortcutsFragment.addGameToList;
import static com.windroid.emu.fragments.ShortcutsFragment.getBox64Preset;
import static com.windroid.emu.fragments.ShortcutsFragment.getBox64Version;
import static com.windroid.emu.fragments.ShortcutsFragment.getCpuAffinity;
import static com.windroid.emu.fragments.ShortcutsFragment.getD3DXRenderer;
import static com.windroid.emu.fragments.ShortcutsFragment.getDXVKVersion;
import static com.windroid.emu.fragments.ShortcutsFragment.getDisplaySettings;
import static com.windroid.emu.fragments.ShortcutsFragment.getEnableAFME;
import static com.windroid.emu.fragments.ShortcutsFragment.getEnableDInput;
import static com.windroid.emu.fragments.ShortcutsFragment.getEnableXInput;
import static com.windroid.emu.fragments.ShortcutsFragment.getExeArguments;
import static com.windroid.emu.fragments.ShortcutsFragment.getExePath;
import static com.windroid.emu.activities.GeneralSettingsActivity.VIRTUAL_CONTROL_OPACITY;
import static com.windroid.emu.activities.GeneralSettingsActivity.VIRTUAL_CONTROL_OPACITY_DEFAULT_VALUE;
import static com.windroid.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.windroid.emu.fragments.ShortcutsFragment.getVKD3DVersion;
import static com.windroid.emu.fragments.ShortcutsFragment.getVulkanDriver;
import static com.windroid.emu.fragments.ShortcutsFragment.getVulkanDriverType;
import static com.windroid.emu.fragments.ShortcutsFragment.getWineD3DVersion;
import static com.windroid.emu.fragments.ShortcutsFragment.getWineESync;
import static com.windroid.emu.fragments.ShortcutsFragment.getWineServices;
import static com.windroid.emu.fragments.ShortcutsFragment.getWineVirtualDesktop;
import static com.windroid.emu.fragments.ShortcutsFragment.updateShortcuts;
import static com.windroid.emu.fragments.SoundSettingsFragment.generatePAFile;
import static com.windroid.emu.fragments.WinePrefixManagerFragment.createWinePrefix;
import static com.windroid.emu.fragments.WinePrefixManagerFragment.getSelectedWinePrefix;
import static com.windroid.emu.fragments.SetupFragment.progressBarIsIndeterminate;
import static com.windroid.emu.fragments.SetupFragment.dialogTitleText;
import static com.windroid.emu.fragments.WinePrefixManagerFragment.getWinePrefixFile;
import static com.windroid.emu.utils.AppKiller.killBackgroundApps;
import static com.windroid.emu.utils.DriveUtils.parseUnixPath;
import static com.windroid.emu.utils.DriveUtils.parseWindowsPath;
import static com.windroid.emu.utils.FileUtils.copyRecursively;
import static com.windroid.emu.utils.FileUtils.deleteDirectoryRecursively;
import static com.windroid.emu.utils.FileUtils.getFileExtension;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.WindowManager;
import android.os.Handler;
import android.media.MediaPlayer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.windroid.emu.BuildConfig;
import com.windroid.emu.R;
import com.windroid.emu.adapters.AdapterBottomNavigation;
import com.windroid.emu.controller.ControllerUtils;
import com.windroid.emu.activities.GeneralSettingsActivity;
import com.windroid.emu.core.RatPackageManager;
import com.windroid.emu.core.WineWrapper;
import com.windroid.emu.databinding.ActivityMainBinding;
import com.windroid.emu.fragments.AskInstallPackageFragment;
import com.windroid.emu.fragments.Box64PresetManagerFragment;
import com.windroid.emu.fragments.ControllerPresetManagerFragment;
import com.windroid.emu.fragments.EditGamePreferencesFragment;
import com.windroid.emu.fragments.FloatingFileManagerFragment;
import com.windroid.emu.fragments.SetupFragment;
import com.windroid.emu.fragments.ShortcutsFragment;
import com.windroid.emu.fragments.VirtualControllerPresetManagerFragment;
import com.windroid.emu.utils.FilePathResolver;
import com.windroid.emu.utils.RootUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mslinks.ShellLink;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;

    public static Context getAppContext() {
        return instance != null ? instance.getApplicationContext() : null;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null)
                return;
            switch (intent.getAction()) {
                case ACTION_RUN_WINE -> {
                    String exePath = intent.getStringExtra("exePath");
                    String exeArguments = intent.getStringExtra("exeArguments");
                    String driverName = intent.getStringExtra("driverName");
                    int driverType = intent.getIntExtra("driverType", MESA_DRIVER);
                    String box64Version = intent.getStringExtra("box64Version");
                    String box64Preset = intent.getStringExtra("box64Preset");
                    String displayResolution = intent.getStringExtra("displayResolution");
                    String d3dxRenderer = intent.getStringExtra("d3dxRenderer");
                    String wineD3D = intent.getStringExtra("wineD3D");
                    String dxvk = intent.getStringExtra("dxvk");
                    String vkd3d = intent.getStringExtra("vkd3d");
                    boolean esync = intent.getBooleanExtra("esync", true);
                    boolean services = intent.getBooleanExtra("services", false);
                    boolean virtualDesktop = intent.getBooleanExtra("virtualDesktop", false);
                    boolean enableXInput = intent.getBooleanExtra("enableXInput", true);
                    boolean enableDInput = intent.getBooleanExtra("enableDInput", true);
                    String cpuAffinity = intent.getStringExtra("cpuAffinity");
                    String vramLimit = intent.getStringExtra("vramLimit");
                    boolean enableAFME = intent.getBooleanExtra("enableAFME", false);

                    if (exeArguments == null)
                        exeArguments = "";
                    if (driverName == null)
                        driverName = "Global";
                    if (box64Version == null)
                        box64Version = "Global";
                    if (box64Preset == null)
                        box64Preset = "default";
                    if (displayResolution == null)
                        displayResolution = "1280x720";
                    if (d3dxRenderer == null)
                        d3dxRenderer = "DXVK";
                    if (wineD3D == null) {
                        List<RatPackageManager.RatPackage> wineD3DPackages = listRatPackages("WineD3D");
                        if (!wineD3DPackages.isEmpty()) {
                            wineD3D = wineD3DPackages.get(0).getFolderName();
                        }
                    }
                    if (dxvk == null) {
                        List<RatPackageManager.RatPackage> dxvkPackages = listRatPackages("DXVK");
                        if (!dxvkPackages.isEmpty()) {
                            dxvk = dxvkPackages.get(0).getFolderName();
                        }
                    }
                    if (vkd3d == null) {
                        List<RatPackageManager.RatPackage> vkd3dPackages = listRatPackages("VKD3D");
                        if (!vkd3dPackages.isEmpty()) {
                            vkd3d = vkd3dPackages.get(0).getFolderName();
                        }
                    }
                    if (cpuAffinity == null)
                        cpuAffinity = String.join(",", availableCPUs);
                    if (vramLimit == null)
                        vramLimit = "Auto";

                    deleteDirectoryRecursively(tmpDir.toPath());
                    tmpDir.mkdirs();

                    if (driverName.equals("Global")) {
                        driverName = preferences.getString(SELECTED_VULKAN_DRIVER, "");
                        driverType = getVulkanDriverType(driverName);
                    }

                    String driverLibPath;
                    String adrenoToolsDriverPath = null;

                    if (driverType == MESA_DRIVER) {
                        driverLibPath = getPackageById(driverName).getDriverLib();
                        if (driverLibPath != null && driverLibPath.endsWith("libvulkan_freedreno.so")) {
                            List<RatPackageManager.RatPackage> wrappers = listRatPackages("VulkanDriver");
                            RatPackageManager.RatPackage wrapperPkg = null;
                            for (RatPackageManager.RatPackage p : wrappers) {
                                if (p.getDriverLib() != null && p.getDriverLib().endsWith("libvulkan_wrapper.so")) {
                                    wrapperPkg = p;
                                    break;
                                }
                            }
                            if (wrapperPkg != null) {
                                driverLibPath = wrapperPkg.getDriverLib();
                            } else {
                                List<RatPackageManager.RatPackage> adrenoTools = listRatPackages("AdrenoTools");
                                if (!adrenoTools.isEmpty()) {
                                    adrenoToolsDriverPath = driverLibPath;
                                    driverLibPath = adrenoTools.get(0).getDriverLib();
                                    driverType = ADRENO_TOOLS_DRIVER;
                                }
                            }
                        }
                    } else if (driverType == ADRENO_TOOLS_DRIVER) {
                        try {
                            driverLibPath = listRatPackages("AdrenoTools").get(0).getDriverLib();
                            adrenoToolsDriverPath = getPackageById(driverName).getDriverLib();
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(MainActivity.this, "AdrenoTools Provider Not Found", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                    } else {
                        driverLibPath = "";
                    }

                    // Validate driver library path before generating ICD file
                    if (driverLibPath == null || driverLibPath.isEmpty()) {
                        Log.e("MainActivity", "Driver library path is null or empty for driver: " + driverName);
                        final String errorDriverName = driverName;
                        runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                "Error: Driver library path not found for " + errorDriverName, Toast.LENGTH_LONG)
                                .show());
                        return;
                    }

                    File driverFile = new File(driverLibPath);
                    if (!driverFile.exists()) {
                        Log.e("MainActivity", "Driver library file does not exist: " + driverLibPath);
                        final String errorDriverPath = driverLibPath;
                        runOnUiThread(
                                () -> Toast
                                        .makeText(MainActivity.this,
                                                "Error: Driver file not found at " + errorDriverPath, Toast.LENGTH_LONG)
                                        .show());
                        return;
                    }

                    generateICDFile(driverLibPath);
                    generateMangoHUDConfFile();
                    generatePAFile();

                    setSharedVars(
                            MainActivity.this,
                            box64Version,
                            box64Preset,
                            d3dxRenderer,
                            wineD3D,
                            dxvk,
                            vkd3d,
                            displayResolution,
                            esync,
                            services,
                            virtualDesktop,
                            enableXInput,
                            enableDInput,
                            cpuAffinity,
                            vramLimit,
                            enableAFME,
                            adrenoToolsDriverPath);

                    runXServer();

                    String finalExeArguments = exeArguments;
                    new Thread(() -> runWine(exePath, finalExeArguments)).start();
                }
                case ACTION_SELECT_FILE_MANAGER -> {
                    String fileName = intent.getStringExtra("selectedFile");
                    if (fileName == null)
                        return;
                    if (fileName.equals("..")) {
                        fileManagerCwd = new File(fileManagerCwd).getParent();
                        refreshFiles(MainActivity.this);
                        return;
                    }

                    File file = new File(fileName);
                    if (file.isFile()) {
                        String fileExtension = getFileExtension(file).toLowerCase();
                        switch (fileExtension) {
                            case "exe", "bat", "msi", "lnk" -> {
                                if (fileExtension.equals("lnk")) {
                                    try {
                                        ShellLink shellLink = new ShellLink(file);
                                        String parsedUnixPath = parseUnixPath(shellLink.resolveTarget());
                                        File targetFile = new File(parsedUnixPath);
                                        new EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, targetFile)
                                                .show(getSupportFragmentManager(), "");
                                        return;
                                    } catch (Exception ignored) {
                                        Toast.makeText(MainActivity.this, R.string.lnk_read_fail, Toast.LENGTH_SHORT)
                                                .show();
                                        return;
                                    }
                                }

                                new EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, file)
                                        .show(getSupportFragmentManager(), "");
                            }
                            case "rat" -> {
                                ratCandidate = new RatPackageManager.RatPackage(file.getPath());

                                if (ratCandidate.getName() != null) {
                                    new AskInstallPackageFragment(RAT_PACKAGE).show(getSupportFragmentManager(), "");
                                }
                            }
                            case "zip" -> {
                                adToolsDriverCandidate = new RatPackageManager.AdrenoToolsPackage(file.getPath());

                                if (adToolsDriverCandidate.getName() != null) {
                                    new AskInstallPackageFragment(ADTOOLS_DRIVER_PACKAGE)
                                            .show(getSupportFragmentManager(), "");
                                }
                            }
                            case "mwp" -> {
                                List<String> mwpLines;

                                try {
                                    mwpLines = Files.readAllLines(file.toPath());
                                } catch (IOException ignored) {
                                    mwpLines = null;
                                }

                                if (mwpLines != null && !mwpLines.isEmpty()) {
                                    switch (mwpLines.get(0)) {
                                        case "controllerPreset" ->
                                            mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(
                                                    CONTROLLER_PRESET, file);
                                        case "virtualControllerPreset" ->
                                            mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(
                                                    VIRTUAL_CONTROLLER_PRESET, file);
                                        case "box64Preset", "box64PresetV2" ->
                                            mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(BOX64_PRESET,
                                                    file);
                                    }

                                    new AskInstallPackageFragment(MWP_PRESET_PACKAGE).show(getSupportFragmentManager(),
                                            "");
                                }
                            }
                        }
                    } else if (file.isDirectory()) {
                        fileManagerCwd = file.getPath();
                        refreshFiles(MainActivity.this);
                    }
                }
                case ACTION_SETUP -> new Thread(() -> {
                    new SetupFragment().show(getSupportFragmentManager(), "");
                    setupWindroid();
                }).start();
                case ACTION_INSTALL_RAT -> {
                    if (!(ratCandidate.getArchitecture().equals(deviceArch)
                            || ratCandidate.getArchitecture().equals("any"))
                            && !ratCandidate.getCategory().equals("Wine")) {
                        Toast.makeText(context, R.string.invalid_architecture_rat_file, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (ratCandidate.getCategory().equals("rootfs")) {
                        Toast.makeText(context, R.string.error_install_rootfs_file_manager, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!installablePackagesCategories.contains(ratCandidate.getCategory())) {
                        Toast.makeText(context, R.string.unknown_package_category, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (checkPackageInstalled(ratCandidate.getName(), ratCandidate.getCategory(),
                            ratCandidate.getVersion())) {
                        Toast.makeText(context, R.string.package_already_installed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        setupDone = false;

                        new SetupFragment().show(getSupportFragmentManager(), "");

                        dialogTitleText = "Installing " + ratCandidate.getName() + " (" + ratCandidate.getVersion()
                                + ")...";
                        progressBarIsIndeterminate = true;

                        installRat(ratCandidate, context);

                        setupDone = true;
                    }).start();
                }
                case ACTION_INSTALL_ADTOOLS_DRIVER -> {
                    boolean isPackageInstalled = checkPackageInstalled(
                            adToolsDriverCandidate.getName() + " (AdrenoTools)", "AdrenoToolsDriver",
                            adToolsDriverCandidate.getVersion());

                    if (isPackageInstalled) {
                        Toast.makeText(context, R.string.package_already_installed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        setupDone = false;

                        new SetupFragment().show(getSupportFragmentManager(), "");

                        dialogTitleText = "Installing " + adToolsDriverCandidate.getName() + " ("
                                + adToolsDriverCandidate.getVersion() + ")...";
                        progressBarIsIndeterminate = true;

                        installADToolsDriver(adToolsDriverCandidate);

                        setupDone = true;
                    }).start();
                }
                case ACTION_SELECT_ICON ->
                    new FloatingFileManagerFragment(OPERATION_SELECT_ICON, wineDisksFolder.getPath())
                            .show(getSupportFragmentManager(), "");
                case ACTION_SELECT_EXE_PATH -> new FloatingFileManagerFragment(OPERATION_SELECT_EXE,
                        new File(getExePath(selectedGameName)).getParent()).show(getSupportFragmentManager(), "");
                case ACTION_CREATE_WINE_PREFIX -> {
                    String winePrefix = intent.getStringExtra("winePrefix");
                    String wine = intent.getStringExtra("wine");

                    runXServer();

                    setupDone = false;

                    new SetupFragment().show(getSupportFragmentManager(), "");

                    dialogTitleText = getString(R.string.creating_wine_prefix);
                    progressBarIsIndeterminate = true;

                    new Thread(() -> {
                        createWinePrefix(winePrefix, wine);
                        setSharedVars(MainActivity.this);

                        fileManagerCwd = fileManagerDefaultDir;
                        floatingFileManagerCwd = fileManagerDefaultDir;
                        setupDone = true;
                    }).start();
                }
            }
        }
    };

    private MediaPlayer bgMusic;
    private InputManager inputManager;
    private final InputManager.InputDeviceListener inputDeviceListener = new InputManager.InputDeviceListener() {
        @Override
        public void onInputDeviceAdded(int deviceId) {
            InputDevice.getDevice(deviceId);
        }

        @Override
        public void onInputDeviceChanged(int deviceId) {
            InputDevice device = InputDevice.getDevice(deviceId);
            if (device == null)
                return;

            if (ControllerUtils.getConnectedControllersCopy().stream().anyMatch(c -> c.id == deviceId))
                return;
            if (((device.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((device.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                if (!device.getName().contains("uinput")) {
                    ControllerUtils.addPhysicalController(new ControllerUtils.PhysicalController(device.getName(), deviceId));
                    prepareControllersMappings();
                    sendBroadcast(new Intent(ACTION_UPDATE_CONTROLLERS_STATUS).setPackage("com.windroid.emu"));
                }
            }
        }

        @Override
        public void onInputDeviceRemoved(int deviceId) {
            List<ControllerUtils.PhysicalController> controllersCopy = ControllerUtils.getConnectedControllersCopy();
            int index = -1;
            for (int i = 0; i < controllersCopy.size(); i++) {
                if (controllersCopy.get(i).id == deviceId) {
                    index = i;
                    break;
                }
            }

            if (index == -1)
                return;

            disconnectController(controllersCopy.get(index).virtualControllerID);
            ControllerUtils.removePhysicalController(index);
            sendBroadcast(new Intent(ACTION_UPDATE_CONTROLLERS_STATUS).setPackage("com.windroid.emu"));
        }
    };

    private BottomNavigationView bottomNavigation;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ControllerPresetManagerFragment.initialize(false);
        VirtualControllerPresetManagerFragment.initialize(this, false);
        Box64PresetManagerFragment.initialize();
        ShortcutsFragment.initialize();
        ControllerUtils.initialize(this);

        inputManager = (InputManager) getSystemService(INPUT_SERVICE);
        inputManager.registerInputDeviceListener(inputDeviceListener, null);

        bgMusic = MediaPlayer.create(this, R.raw.background);

        if (bgMusic != null) {
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.05f, 0.05f);
        }

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSharedVars(this);

        // Fecha apps em segundo plano para liberar recursos
        new Thread(() -> killBackgroundApps(this)).start();

        // On future here will have a code for check if app is updated and do specific
        // data conversion if needed

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(APP_VERSION, BuildConfig.VERSION_NAME);
        editor.apply();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_shortcuts) {
                selectedFragmentId = 0;
                updateShortcuts();
            } else if (item.getItemId() == R.id.nav_settings) {
                selectedFragmentId = 1;
            } else if (item.getItemId() == R.id.nav_file_manager) {
                selectedFragmentId = 2;
            }
            viewPager.setCurrentItem(selectedFragmentId);
            return true;
        });

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new AdapterBottomNavigation(this));
        viewPager.setUserInputEnabled(false);

        bottomNavigation.post(() -> bottomNavigation.setSelectedItemId(R.id.nav_shortcuts));

        androidx.core.content.ContextCompat.registerReceiver(this, receiver, new IntentFilter() {
            {
                addAction(ACTION_RUN_WINE);
                addAction(ACTION_SETUP);
                addAction(ACTION_INSTALL_RAT);
                addAction(ACTION_INSTALL_ADTOOLS_DRIVER);
                addAction(ACTION_SELECT_FILE_MANAGER);
                addAction(ACTION_SELECT_ICON);
                addAction(ACTION_SELECT_EXE_PATH);
                addAction(ACTION_CREATE_WINE_PREFIX);
            }
        }, androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWinePrefixFile(winePrefix).exists()) {
                WineWrapper.clearDrives();

                List<StorageVolume> storageVolumes = ((StorageManager) getSystemService(Context.STORAGE_SERVICE))
                        .getStorageVolumes();

                for (StorageVolume volume : storageVolumes) {
                    if (volume.isRemovable()) {
                        WineWrapper.addDrive(volume.getDirectory().getPath());
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (!usrDir.exists()) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            setupDone = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        runXServer();

        updateShortcuts();

        if (bgMusic != null && !bgMusic.isPlaying()) {
            bgMusic.setVolume(0.05f, 0.05f);
            bgMusic.start();
        }

        if (!setupDone && finishedWelcomeScreen) {
            File rootfsFile = new File(appRootDir.getPath() + "/rootfs.rat");
            if (rootFSIsDownloaded && rootfsFile.exists()) {
                sendBroadcast(new Intent(ACTION_SETUP).setPackage("com.windroid.emu"));
            } else {
                // Reset flag if file doesn't exist
                if (rootFSIsDownloaded && !rootfsFile.exists()) {
                    rootFSIsDownloaded = false;
                }
                new FloatingFileManagerFragment(OPERATION_SELECT_RAT, "/storage/emulated/0")
                        .show(getSupportFragmentManager(), "");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Unregister broadcast receiver
        unregisterReceiver(receiver);
        
        // Unregister input device listener
        if (inputManager != null && inputDeviceListener != null) {
            inputManager.unregisterInputDeviceListener(inputDeviceListener);
        }
        
        // Release MediaPlayer to prevent memory leak
        if (bgMusic != null) {
            if (bgMusic.isPlaying()) {
                bgMusic.stop();
            }
            bgMusic.release();
            bgMusic = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (selectedFragmentId == 2) {
                if (!fileManagerCwd.equals(fileManagerDefaultDir)) {
                    fileManagerCwd = new File(fileManagerCwd).getParent();
                    refreshFiles(this);
                    return true;
                }
            }
            if (selectedFragmentId > 0) {
                bottomNavigation.setSelectedItemId(R.id.nav_shortcuts);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void installDXWrapper(String winePrefixName) {
        File winePrefix = getWinePrefixFile(winePrefixName);

        File driveC = new File(winePrefix, "drive_c");
        File system32 = new File(driveC, "windows/system32");
        File syswow64 = new File(driveC, "windows/syswow64");
        File selectedDXVKDir = new File(ratPackagesDir, selectedDXVK);
        File selectedVKD3DDir = new File(ratPackagesDir, selectedVKD3D);
        File selectedWineD3DDir = new File(ratPackagesDir, selectedWineD3D);

        switch (selectedD3DXRenderer) {
            case "DXVK" -> {
                if (selectedDXVKDir.exists()) {
                    File x64Folder = new File(selectedDXVKDir, "files/x64");
                    File x32Folder = new File(selectedDXVKDir, "files/x32");

                    if (x64Folder.exists() && x32Folder.exists()) {
                        copyRecursively(x64Folder, system32);
                        copyRecursively(x32Folder, syswow64);
                    }
                }
            }
            case "WineD3D" -> {
                if (selectedWineD3DDir.exists()) {
                    File x64Folder = new File(selectedWineD3DDir, "files/x64");
                    File x32Folder = new File(selectedWineD3DDir, "files/x32");

                    if (x64Folder.exists() && x32Folder.exists()) {
                        copyRecursively(x64Folder, system32);
                        copyRecursively(x32Folder, syswow64);
                    }
                }
            }
        }

        if (selectedVKD3DDir.exists()) {
            File x64Folder = new File(selectedVKD3DDir, "files/x64");
            File x32Folder = new File(selectedVKD3DDir, "files/x32");

            if (x64Folder.exists() && x32Folder.exists()) {
                copyRecursively(x64Folder, system32);
                copyRecursively(x32Folder, syswow64);
            }
        }
    }

    private void runWine(String exePath, String exeArguments) {
        // Verificar se é um comando do winetricks (atalho do menu Iniciar)
        if (WineWrapper.isWinetricksCommand(exePath)) {
            runOnUiThread(() -> {
                Intent intent = new Intent(MainActivity.this, GeneralSettingsActivity.class);
                intent.setAction("com.windroid.emu.ACTION_PREFERENCE_SELECT");
                intent.putExtra("preference", getString(R.string.winetricks_title));
                startActivity(intent);
            });
            return;
        }

        if (preferences.getBoolean(PERF_MODE_ROOT, false)) {
            RootUtils.applyPerformanceMode();

            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Espera o Wine iniciar
                    RootUtils.optimizeWineProcesses();
                } catch (InterruptedException ignored) {
                }
            }).start();
        }

        installDXWrapper(winePrefix);
        setupUserLinks(winePrefix);

        boolean changedDpi = !(preferences.getBoolean(WINE_DPI_APPLIED, WINE_DPI_APPLIED_DEFAULT_VALUE));
        if (changedDpi) {
            int newDpi = preferences.getInt(WINE_DPI, WINE_DPI_DEFAULT_VALUE);
            WineWrapper.wine(
                    "reg add HKCU\\\\Control\\\\ Panel\\\\Desktop /t REG_DWORD /v LogPixels /d " + newDpi + " /f");

            WineWrapper.wine(
                    "reg add HKCU\\\\Software\\\\Wine\\\\DllOverrides /v dcomp /d \"\" /f");

            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean(WINE_DPI_APPLIED, true);
            editor.apply();
        }

        WineWrapper.killAll();

        File skCodec = new File("/system/lib64/libskcodec.so");
        if (skCodec.exists()) {
            runCommand(getEnv() + "LD_PRELOAD=$skCodec $usrDir/bin/pulseaudio --start --exit-idle=-1", true);
        }

        if (!wineServices) {
            new Thread(() -> {
                WineWrapper.waitForProcess("window_handler.exe");
                runCommand("pkill -SIGTERM -f services.exe", false);
            }).start();
        }

        if (exePath.isEmpty()) {
            WineWrapper.wine("explorer /desktop=shell," + selectedResolution + " window_handler.exe "
                    + getCpuHexMask(selectedCpuAffinity) + " TFM");
        } else {
            if (enableWineVirtualDesktop) {
                String targetPath = (exePath.startsWith(wineDisksFolder.getPath())
                        ? parseWindowsPath(exePath) : exePath);
                WineWrapper.wine("explorer /desktop=shell," + selectedResolution + " window_handler.exe "
                        + getCpuHexMask(selectedCpuAffinity) + " \"" + targetPath + "\" " + exeArguments,
                        "'" + getSanitizedPath(Objects.requireNonNull(new File(exePath).getParent())) + "'");
            } else {
                new Thread(() -> WineWrapper
                        .wine("start /unix C:\\\\windows\\\\window_handler.exe " + getCpuHexMask(selectedCpuAffinity)))
                        .start();

                WineWrapper.wine("'" + getSanitizedPath(exePath) + "' " + exeArguments,
                        "'" + getSanitizedPath(new File(exePath).getParent()) + "'");
            }
        }

        WineWrapper.killAll();

        if (preferences.getBoolean(PERF_MODE_ROOT, false)) {
            RootUtils.restoreDefaultMode();
        }

        if (preferences.getBoolean(SELINUX_PERMISSIVE_ROOT, false)) {
            RootUtils.restoreSelinuxMode();
        }

        RootUtils.umountTmpfs();

        runOnUiThread(() -> Toast.makeText(this, getString(R.string.wine_is_closed), Toast.LENGTH_SHORT).show());
    }

    private boolean runningXServer = false;

    private void runXServer() {
        if (runningXServer || !setupDone)
            return;

        runningXServer = true;

        if (preferences.getBoolean(SELINUX_PERMISSIVE_ROOT, false)) {
            RootUtils.applySelinuxPermissive();
        }

        RootUtils.mountTmpfs();

        // O servidor X11 legado foi desativado em favor da renderização nativa do Android
    }

    private String getClassPath() {
        return new File(getLibsPath()).getParentFile().getParentFile().getAbsolutePath() + "/base.apk";
    }

    private String getLibsPath() {
        return getApplicationInfo().nativeLibraryDir;
    }

    private void setupWindroid() {
        appRootDir.mkdirs();
        ratPackagesDir.mkdirs();

        progressBarIsIndeterminate = true;

        RatPackageManager.RatPackage ratFile;

        if (rootFSIsDownloaded) {
            dialogTitleText = getString(R.string.checking_rat_type);
            ratFile = new RatPackageManager.RatPackage(appRootDir.getPath() + "/rootfs.rat");
        } else {
            dialogTitleText = getString(R.string.checking_rat_type);
            ratFile = new RatPackageManager.RatPackage(customRootFSPath);
        }

        if (!ratFile.getCategory().equals("rootfs")) {
            abortSetup = true;

            runOnUiThread(() -> Toast.makeText(this, R.string.invalid_rootfs_rat_file, Toast.LENGTH_SHORT).show());

            new FloatingFileManagerFragment(OPERATION_SELECT_RAT, "/storage/emulated/0")
                    .show(getSupportFragmentManager(), "");

            return;
        }

        if (!ratFile.getArchitecture().equals(deviceArch)) {
            abortSetup = true;

            runOnUiThread(
                    () -> Toast.makeText(this, R.string.invalid_architecture_rat_file, Toast.LENGTH_SHORT).show());

            new FloatingFileManagerFragment(OPERATION_SELECT_RAT, "/storage/emulated/0")
                    .show(getSupportFragmentManager(), "");

            return;
        }

        dialogTitleText = getString(R.string.extracting_resources_text);

        installRat(ratFile, this);

        if (rootFSIsDownloaded) {
            new File(appRootDir.getPath() + "/rootfs.rat").delete();
            rootFSIsDownloaded = false; // Reset flag after successful installation
        }

        tmpDir.mkdirs();
        homeDir.mkdirs();
        winePrefixesDir.mkdirs();

        runCommand("chmod 700 -R " + appRootDir.getPath(), false);

        new File(usrDir.getPath() + "/icons").mkdirs();

        addGameToList(getString(R.string.desktop_mode_init), getString(R.string.desktop_mode_init), "");

        dialogTitleText = getString(R.string.creating_wine_prefix);
        progressBarIsIndeterminate = true;

        runXServer();

        setSharedVars(this);

        String wine = listRatPackages("Wine").get(0).getFolderName();
        Intent createWinePrefixIntent = new Intent(ACTION_CREATE_WINE_PREFIX);

        createWinePrefixIntent.putExtra("wine", wine);
        createWinePrefixIntent.putExtra("winePrefix", winePrefix);

        createWinePrefixIntent.setPackage("com.windroid.emu");
        sendBroadcast(createWinePrefixIntent);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        String shortcutName = intent.getStringExtra("shortcutName");
        if (shortcutName != null) {
            selectedGameName = shortcutName;

            String driverName = getVulkanDriver(selectedGameName);
            int driverType = getVulkanDriverType(selectedGameName);
            String box64Version = getBox64Version(selectedGameName);

            Intent runActivityIntent = new Intent(this, EmulationActivity.class);
            Intent runWineIntent = new Intent(ACTION_RUN_WINE);

            runWineIntent.putExtra("exePath", getExePath(shortcutName));
            runWineIntent.putExtra("exeArguments", getExeArguments(shortcutName));
            runWineIntent.putExtra("driverName", driverName);
            runWineIntent.putExtra("driverType", driverType);
            runWineIntent.putExtra("box64Version", box64Version);
            runWineIntent.putExtra("box64Preset", getBox64Preset(selectedGameName));
            runWineIntent.putExtra("displayResolution", getDisplaySettings(selectedGameName).get(1));
            runWineIntent.putExtra("virtualControllerPreset", getSelectedVirtualControllerPreset(selectedGameName));
            runWineIntent.putExtra("d3dxRenderer", getD3DXRenderer(selectedGameName));
            runWineIntent.putExtra("wineD3D", getWineD3DVersion(selectedGameName));
            runWineIntent.putExtra("dxvk", getDXVKVersion(selectedGameName));
            runWineIntent.putExtra("vkd3d", getVKD3DVersion(selectedGameName));
            runWineIntent.putExtra("esync", getWineESync(selectedGameName));
            runWineIntent.putExtra("services", getWineServices(selectedGameName));
            runWineIntent.putExtra("virtualDesktop", getWineVirtualDesktop(selectedGameName));
            runWineIntent.putExtra("enableXInput", getEnableXInput(selectedGameName));
            runWineIntent.putExtra("enableDInput", getEnableDInput(selectedGameName));
            runWineIntent.putExtra("cpuAffinity", getCpuAffinity(selectedGameName));

            runWineIntent.setPackage("com.windroid.emu");
            sendBroadcast(runWineIntent);
            startActivity(runActivityIntent);

            return;
        }

        Uri uri = intent.getData();

        if (uri != null) {
            String filePath = FilePathResolver.resolvePath(this, uri);

            if (filePath != null) {
                new EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, new File(filePath))
                        .show(getSupportFragmentManager(), "");
            }
        }
    }

    @SuppressLint("SdCardPath")
    public static final File appRootDir = new File("/data/data/com.windroid.emu/files");
    public static final String WINDROID_IPC_SOCKET_PATH = appRootDir.getPath() + "/ipc.sock";
    public static File ratPackagesDir = new File(appRootDir + "/packages");
    public static String deviceArch = Build.SUPPORTED_ABIS[0].replace("arm64-v8a", "aarch64");
    private static String unixUsername = null;

    public static String getUnixUsername() {
        if (unixUsername == null) {
            unixUsername = runCommandWithOutput("whoami", false).replace("\n", "");
        }
        return unixUsername;
    }

    public static String customRootFSPath = null;
    public static File usrDir = new File(appRootDir + "/usr");
    public static File tmpDir = new File(usrDir + "/tmp");
    public static File homeDir = new File(appRootDir + "/home");
    public static boolean setupDone = false;
    public static boolean enableRamCounter = false;
    public static boolean enableCpuCounter = false;
    public static boolean enableDebugInfo = false;
    public static boolean enableDRI3 = false;
    public static boolean enableMangoHUD = false;
    public static String appLang = null;
    public static String box64LogLevel = null;
    public static Integer box64MMap32 = null;
    public static Integer box64Avx = null;
    public static Integer box64Sse42 = null;
    public static Integer box64DynarecBigBlock = null;
    public static Integer box64DynarecStrongMem = null;
    public static Integer box64DynarecWeakBarrier = null;
    public static Integer box64DynarecPause = null;
    public static Integer box64DynarecX87Double = null;
    public static Integer box64DynarecFastNan = null;
    public static Integer box64DynarecFastRound = null;
    public static Integer box64DynarecSafeFlags = null;
    public static Integer box64DynarecCallRet = null;
    public static Integer box64DynarecAlignedAtomics = null;
    public static Integer box64DynarecNativeFlags = null;
    public static Integer box64DynarecBleedingEdge = null;
    public static Integer box64DynarecWait = null;
    public static Integer box64DynarecDirty = null;
    public static Integer box64DynarecForward = null;
    public static Integer box64DynarecDF = null;
    public static Integer box64ShowSegv = null;
    public static Integer box64ShowBt = null;
    public static Integer box64NoSigSegv = null;
    public static Integer box64NoSigill = null;
    public static String wineLogLevel = null;
    public static String selectedBox64 = null;
    public static String selectedD3DXRenderer = null;
    public static String selectedWineD3D = null;
    public static String selectedDXVK = null;
    public static String selectedVKD3D = null;
    public static String selectedGLProfile = null;
    public static String selectedDXVKHud = null;
    public static String selectedMesaVkWsiPresentMode = null;
    public static String selectedTuDebugPreset = null;
    public static String selectedTuTextureLodBias = null;
    public static String selectedTuForceMipLevel = null;
    public static boolean enableTuForceShadingRate = false;
    public static boolean enableTuForceSysmem = false;
    public static boolean enableTuDisableShadows = false;
    public static boolean enableTuDisableHeavyEffects = false;
    public static String selectedVramLimit = null;
    public static String selectedScalingFilter = null;
    public static String selectedFrameGeneration = null;
    public static String selectedFrameSkip = null;
    public static boolean selectedSuperResolution = false;
    public static String wineFsrMode = null;
    public static int wineFsrSharpness = 0;
    public static String selectedWineFrameSkip = null;
    public static String selectedColorProfile = null;
    public static int selectedFragmentId = 0;
    public static String memoryStats = "??/??";
    public static String totalCpuUsage = "???%";
    public static File winePrefixesDir = new File(appRootDir + "/winePrefixes");
    public static File wineDisksFolder = null;
    public static String winePrefix = null;
    public static boolean wineESync = false;
    public static boolean wineServices = false;
    public static String selectedCpuAffinity = null;
    public static boolean enableWineVirtualDesktop = false;
    public static String selectedWine = null;
    public static String fileManagerDefaultDir = "";
    public static String fileManagerCwd = null;
    public static String floatingFileManagerCwd = null;
    public static String selectedFilePath = "";
    public static String windroidVersion = "Windroid " + BuildConfig.VERSION_NAME
            + (BuildConfig.DEBUG ? " (git-" + BuildConfig.GIT_SHORT_SHA + ")" : "");
    public static String vulkanDriverDeviceName = null;
    public static String vulkanDriverDriverVersion = null;
    public static int screenFpsLimit = 60;
    public static int fpsLimit = 0;
    public static String paSink = null;
    public static String selectedResolution = null;
    public static boolean useAdrenoTools = false;
    public static boolean enableXInput = true;
    public static boolean enableDInput = true;
    public static File adrenoToolsDriverFile = null;
    public static SharedPreferences preferences = null;
    public static final Gson gson = new Gson();

    public static final String ACTION_RUN_WINE = "com.windroid.emu.ACTION_RUN_WINE";
    public static final String ACTION_SETUP = "com.windroid.emu.ACTION_SETUP";
    public static final String ACTION_INSTALL_RAT = "com.windroid.emu.ACTION_INSTALL_RAT";
    public static final String ACTION_INSTALL_ADTOOLS_DRIVER = "com.windroid.emu.ACTION_INSTALL_ADTOOLS_DRIVER";
    public static final String ACTION_SELECT_FILE_MANAGER = "com.windroid.emu.ACTION_SELECT_FILE_MANAGER";
    public static final String ACTION_SELECT_ICON = "com.windroid.emu.ACTION_SELECT_ICON";
    public static final String ACTION_SELECT_EXE_PATH = "com.windroid.emu.ACTION_SELECT_EXE_PATH";
    public static final String ACTION_CREATE_WINE_PREFIX = "com.windroid.emu.ACTION_CREATE_WINE_PREFIX";

    public static boolean enableAFME;
    public static boolean wineHide;
    public static int virtualControlOpacity;
    public static final String RAM_COUNTER = "ramCounter";
    public static final boolean RAM_COUNTER_DEFAULT_VALUE = true;

    public static final String CPU_COUNTER = "cpuCounter";
    public static final boolean CPU_COUNTER_DEFAULT_VALUE = false;

    public static final String ENABLE_DEBUG_INFO = "debugInfo";
    public static final boolean ENABLE_DEBUG_INFO_DEFAULT_VALUE = false;

    public static final String APP_VERSION = "appVersion";

    public static int strBoolToNum(boolean strBool) {
        return (strBool ? 1 : 0);
    }

    public static void setSharedVars(Activity activity, String adrenoToolsDriverPath) {
        setSharedVars(activity, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null,
                adrenoToolsDriverPath);
    }

    public static void setSharedVars(Activity activity) {
        setSharedVars(activity, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null,
                null);
    }

    public static void setSharedVars(
            Activity activity,
            String box64Version,
            String box64Preset,
            String d3dxRenderer,
            String wineD3D,
            String dxvk,
            String vkd3d,
            String displayResolution,
            Boolean esync,
            Boolean services,
            Boolean virtualDesktop,
            Boolean enableXInputController,
            Boolean enableDInputController,
            String cpuAffinity,
            String vramLimit,
            Boolean afme,
            String adrenoToolsDriverPath) {
        useAdrenoTools = (adrenoToolsDriverPath != null);
        adrenoToolsDriverFile = (adrenoToolsDriverPath != null ? new File(adrenoToolsDriverPath) : null);

        appLang = activity.getResources().getString(R.string.app_lang);

        selectedBox64 = (box64Version != null ? box64Version : getBox64Version(selectedGameName));
        if ("Global".equals(selectedBox64)) {
            selectedBox64 = preferences.getString(SELECTED_BOX64, "");
        }

        box64LogLevel = preferences.getString(BOX64_LOG, String.valueOf(BOX64_LOG_DEFAULT_VALUE));

        box64ShowSegv = strBoolToNum(preferences.getBoolean(BOX64_SHOWSEGV, BOX64_SHOWSEGV_DEFAULT_VALUE));
        box64ShowBt = strBoolToNum(preferences.getBoolean(BOX64_SHOWBT, BOX64_SHOWBT_DEFAULT_VALUE));
        box64NoSigill = strBoolToNum(preferences.getBoolean(BOX64_NOSIGILL, BOX64_NOSIGILL_DEFAULT_VALUE));
        box64NoSigSegv = strBoolToNum(preferences.getBoolean(BOX64_NOSIGSEGV, BOX64_NOSIGSEGV_DEFAULT_VALUE));

        setBox64Preset(box64Preset);

        enableDRI3 = preferences.getBoolean(ENABLE_DRI3, ENABLE_DRI3_DEFAULT_VALUE);
        enableMangoHUD = preferences.getBoolean(ENABLE_MANGOHUD, ENABLE_MANGOHUD_DEFAULT_VALUE);
        wineLogLevel = preferences.getString(WINE_LOG_LEVEL, WINE_LOG_LEVEL_DEFAULT_VALUE);

        selectedD3DXRenderer = (d3dxRenderer != null ? d3dxRenderer : getD3DXRenderer(selectedGameName));
        selectedWineD3D = (wineD3D != null ? wineD3D : getWineD3DVersion(selectedGameName));
        selectedDXVK = (dxvk != null ? dxvk : getDXVKVersion(selectedGameName));
        selectedVKD3D = (vkd3d != null ? vkd3d : getVKD3DVersion(selectedGameName));

        selectedResolution = (displayResolution != null ? displayResolution
                : getDisplaySettings(selectedGameName).get(1));
        wineESync = (esync != null ? esync : getWineESync(selectedGameName));
        wineServices = (services != null ? services : getWineServices(selectedGameName));
        enableWineVirtualDesktop = (virtualDesktop != null ? virtualDesktop : getWineVirtualDesktop(selectedGameName));
        enableXInput = (enableXInputController != null ? enableXInputController : getEnableXInput(selectedGameName));
        enableDInput = (enableDInputController != null ? enableDInputController : getEnableDInput(selectedGameName));
        selectedCpuAffinity = (cpuAffinity != null ? cpuAffinity : getCpuAffinity(selectedGameName));

        enableAFME = (afme != null ? afme : getEnableAFME(selectedGameName));
        if (selectedGameName.equals("")) {
            enableAFME = preferences.getBoolean(ENABLE_AFME, ENABLE_AFME_DEFAULT_VALUE);
        }

        selectedGLProfile = preferences.getString(SELECTED_GL_PROFILE, SELECTED_GL_PROFILE_DEFAULT_VALUE);
        selectedDXVKHud = preferences.getString(SELECTED_DXVK_HUD_PRESET, SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE);
        selectedMesaVkWsiPresentMode = preferences.getString(SELECTED_MESA_VK_WSI_PRESENT_MODE,
                SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE);
        selectedTuDebugPreset = preferences.getString(SELECTED_TU_DEBUG_PRESET, SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE);
        selectedTuTextureLodBias = preferences.getString(SELECTED_TU_TEXTURE_LOD_BIAS, SELECTED_TU_TEXTURE_LOD_BIAS_DEFAULT_VALUE);
        selectedTuForceMipLevel = preferences.getString(SELECTED_TU_FORCE_MIP_LEVEL, SELECTED_TU_FORCE_MIP_LEVEL_DEFAULT_VALUE);
        enableTuForceShadingRate = preferences.getBoolean(ENABLE_TU_FORCE_SHADING_RATE, ENABLE_TU_FORCE_SHADING_RATE_DEFAULT_VALUE);
        enableTuForceSysmem = preferences.getBoolean(ENABLE_TU_FORCE_SYSMEM, ENABLE_TU_FORCE_SYSMEM_DEFAULT_VALUE);
        enableTuDisableShadows = preferences.getBoolean(ENABLE_TU_DISABLE_SHADOWS, ENABLE_TU_DISABLE_SHADOWS_DEFAULT_VALUE);
        enableTuDisableHeavyEffects = preferences.getBoolean(ENABLE_TU_DISABLE_HEAVY_EFFECTS, ENABLE_TU_DISABLE_HEAVY_EFFECTS_DEFAULT_VALUE);
        selectedVramLimit = (vramLimit != null ? vramLimit
                : preferences.getString(SELECTED_VRAM_LIMIT, SELECTED_VRAM_LIMIT_DEFAULT_VALUE));
        selectedScalingFilter = preferences.getString(SELECTED_SCALING_FILTER, SCALING_FILTER_LINEAR);
        selectedFrameGeneration = preferences.getString(SELECTED_FRAME_GENERATION, FRAME_GENERATION_OFF);
        selectedFrameSkip = preferences.getString(SELECTED_FRAMESKIP, FRAMESKIP_0);
        selectedSuperResolution = preferences.getBoolean(SUPER_RESOLUTION, SUPER_RESOLUTION_DEFAULT_VALUE);
        wineFsrMode = preferences.getString(WINE_FSR_MODE, WINE_FSR_MODE_DEFAULT_VALUE);
        wineFsrSharpness = preferences.getInt(WINE_FSR_SHARPNESS, WINE_FSR_SHARPNESS_DEFAULT_VALUE);
        selectedWineFrameSkip = preferences.getString(SELECTED_WINE_FRAMESKIP, WINE_FRAMESKIP_0);
        wineHide = preferences.getBoolean(WINE_HIDE, WINE_HIDE_DEFAULT_VALUE);
        selectedColorProfile = preferences.getString(COLOR_PROFILE, COLOR_PROFILE_DEFAULT_VALUE);

        enableRamCounter = preferences.getBoolean(RAM_COUNTER, RAM_COUNTER_DEFAULT_VALUE);
        enableCpuCounter = preferences.getBoolean(CPU_COUNTER, CPU_COUNTER_DEFAULT_VALUE);
        enableDebugInfo = preferences.getBoolean(ENABLE_DEBUG_INFO, ENABLE_DEBUG_INFO_DEFAULT_VALUE);

        screenFpsLimit = (int) ((WindowManager) activity.getSystemService(WINDOW_SERVICE)).getDefaultDisplay()
                .getRefreshRate();
        fpsLimit = preferences.getInt(FPS_LIMIT, 0);
        virtualControlOpacity = preferences.getInt(VIRTUAL_CONTROL_OPACITY, VIRTUAL_CONTROL_OPACITY_DEFAULT_VALUE);
        vulkanDriverDeviceName = getVulkanDriverInfo("deviceName", false) + (useAdrenoTools ? " (AdrenoTools)" : "");
        vulkanDriverDriverVersion = getVulkanDriverInfo("driverVersion", false).split(" ")[0];

        winePrefix = getSelectedWinePrefix();
        wineDisksFolder = new File(winePrefixesDir + "/" + winePrefix + "/dosdevices/");

        File winePrefixConfigFile = new File(winePrefixesDir + "/" + winePrefix + "/config");
        if (winePrefixConfigFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(winePrefixConfigFile.toPath());
                selectedWine = lines.get(0);
            } catch (IOException ignored) {
            }

            fileManagerDefaultDir = wineDisksFolder.getPath();

            paSink = preferences.getString(PA_SINK, PA_SINK_DEFAULT_VALUE).toLowerCase();
        }
    }

    public static void setBox64Preset(String box64Preset) {
        String selectedBox64Preset = ((box64Preset != null && !box64Preset.equals("--")) ? box64Preset
                : preferences.getString(SELECTED_BOX64_PRESET, "default"));

        box64MMap32 = strBoolToNum(getBox64MMap32(selectedBox64Preset));
        box64Avx = getBox64Avx(selectedBox64Preset);
        box64Sse42 = strBoolToNum(getBox64Sse42(selectedBox64Preset));
        box64DynarecBigBlock = getBox64BigBlock(selectedBox64Preset);
        box64DynarecStrongMem = getBox64StrongMem(selectedBox64Preset);
        box64DynarecWeakBarrier = getBox64WeakBarrier(selectedBox64Preset);
        box64DynarecPause = getBox64Pause(selectedBox64Preset);
        box64DynarecX87Double = getBox64X87Double(selectedBox64Preset);
        box64DynarecFastNan = strBoolToNum(getBox64FastNan(selectedBox64Preset));
        box64DynarecFastRound = strBoolToNum(getBox64FastRound(selectedBox64Preset));
        box64DynarecSafeFlags = getBox64SafeFlags(selectedBox64Preset);
        box64DynarecCallRet = getBox64CallRet(selectedBox64Preset);
        box64DynarecAlignedAtomics = strBoolToNum(getBox64AlignedAtomics(selectedBox64Preset));
        box64DynarecNativeFlags = strBoolToNum(getBox64NativeFlags(selectedBox64Preset));
        box64DynarecWait = strBoolToNum(getBox64Wait(selectedBox64Preset));
        box64DynarecDirty = getBox64Dirty(selectedBox64Preset);
        box64DynarecForward = getBox64Forward(selectedBox64Preset);
        box64DynarecDF = strBoolToNum(getBox64DF(selectedBox64Preset));
    }

    public static void copyFile(InputStream input, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = input.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private static String getVulkanDriverInfo(String info, boolean stdErr) {
        try {
            return runCommandWithOutput(
                    "echo $(" + getEnv() + " DISPLAY= timeout 10 vulkaninfo | grep " + info + " | cut -d '=' -f 2)", stdErr);
        } catch (Exception e) {
            Log.e("MainActivity", "Error getting Vulkan driver info for " + info, e);
            return "";
        }
    }

    public static String[] resolutions16_9 = new String[] {
            "640x360", "854x480",
            "960x540", "1280x720",
            "1366x768", "1600x900",
            "1920x1080", "2560x1440",
            "3840x2160", "7680x4320"
    };

    public static String[] resolutions18_9 = new String[] {
            "720x360", "960x480",
            "1080x540", "1440x720",
            "1536x768", "1800x900",
            "2160x1080", "2880x1440",
            "4320x2160", "8640x4320"
    };

    public static String[] resolutions4_3 = new String[] {
            "640x480", "800x600",
            "1024x768", "1280x960",
            "1400x1050", "1600x1200"
    };

    public static String getNativeResolution(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);

        if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
            return displayMetrics.widthPixels + "x" + displayMetrics.heightPixels;
        } else {
            return displayMetrics.heightPixels + "x" + displayMetrics.widthPixels;
        }
    }

    public static String getPercentOfResolution(String originalResolution, int percent) {
        String[] resolution = originalResolution.split("x");
        int width = Integer.parseInt(resolution[0]) * percent / 100;
        int height = Integer.parseInt(resolution[1]) * percent / 100;

        return width + "x" + height;
    }

    public static List<String> getNativeResolutions(Activity activity) {
        ArrayList<String> parsedResolutions = new ArrayList<>();
        String nativeResolution = getNativeResolution(activity);

        parsedResolutions.add(nativeResolution);
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 90));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 80));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 70));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 60));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 50));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 40));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 30));

        return parsedResolutions;
    }

    private void setupUserLinks(String prefixName) {
        File winePrefixDir = getWinePrefixFile(prefixName);
        File driveC = new File(winePrefixDir, "drive_c");
        File usersDir = new File(driveC, "users");
        String unixUser = getUnixUsername();
        File unixUserDir = new File(usersDir, unixUser);
        File steamUserDir = new File(usersDir, "steamuser");
        File userSharedFolder = new File(Environment.getExternalStorageDirectory(), "Windroid");

        if (!userSharedFolder.exists()) {
            userSharedFolder.mkdirs();
        }

        if (steamUserDir.exists() && !Files.isSymbolicLink(steamUserDir.toPath())) {
            runCommand("rm -rf '" + steamUserDir + "'", false);
        }

        if (!steamUserDir.exists()) {
            runCommand("ln -sf '" + userSharedFolder + "' '" + steamUserDir + "'", false);
        }

        // Ensure both folders exist and have correct symlinks
        setupProfile(unixUserDir, userSharedFolder);
    }

    private void setupProfile(File userDir, File sharedFolder) {
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        File localAppData = new File(userDir, "AppData");
        File localSavedGames = new File(userDir, "Saved Games");
        File localDocuments = new File(userDir, "Documents");

        if (!localAppData.exists())
            runCommand("ln -sf '" + sharedFolder + "/AppData' '" + localAppData + "'", false);
        if (!localSavedGames.exists())
            runCommand("ln -sf '" + sharedFolder + "/Saved Games' '" + localSavedGames + "'", false);
        if (!localDocuments.exists())
            runCommand("ln -sf '" + sharedFolder + "/Documents' '" + localDocuments + "'", false);
    }
}
