package com.windroid.emu.core;

import static com.windroid.emu.activities.MainActivity.adrenoToolsDriverFile;
import static com.windroid.emu.activities.MainActivity.appLang;
import static com.windroid.emu.activities.MainActivity.appRootDir;
import static com.windroid.emu.activities.MainActivity.box64Avx;
import static com.windroid.emu.activities.MainActivity.box64DynarecAlignedAtomics;
import static com.windroid.emu.activities.MainActivity.box64DynarecBigBlock;
import static com.windroid.emu.activities.MainActivity.box64DynarecBleedingEdge;
import static com.windroid.emu.activities.MainActivity.box64DynarecCallRet;
import static com.windroid.emu.activities.MainActivity.box64DynarecDF;
import static com.windroid.emu.activities.MainActivity.box64DynarecDirty;
import static com.windroid.emu.activities.MainActivity.box64DynarecFastNan;
import static com.windroid.emu.activities.MainActivity.box64DynarecFastRound;
import static com.windroid.emu.activities.MainActivity.box64DynarecForward;
import static com.windroid.emu.activities.MainActivity.box64DynarecNativeFlags;
import static com.windroid.emu.activities.MainActivity.box64DynarecPause;
import static com.windroid.emu.activities.MainActivity.box64DynarecSafeFlags;
import static com.windroid.emu.activities.MainActivity.box64DynarecStrongMem;
import static com.windroid.emu.activities.MainActivity.box64DynarecWait;
import static com.windroid.emu.activities.MainActivity.box64DynarecWeakBarrier;
import static com.windroid.emu.activities.MainActivity.box64DynarecX87Double;
import static com.windroid.emu.activities.MainActivity.box64LogLevel;
import static com.windroid.emu.activities.MainActivity.box64MMap32;
import static com.windroid.emu.activities.MainActivity.box64NoSigSegv;
import static com.windroid.emu.activities.MainActivity.box64NoSigill;
import static com.windroid.emu.activities.MainActivity.box64ShowBt;
import static com.windroid.emu.activities.MainActivity.box64ShowSegv;
import static com.windroid.emu.activities.MainActivity.box64Sse42;
import static com.windroid.emu.activities.MainActivity.deviceArch;
import static com.windroid.emu.activities.MainActivity.fpsLimit;
import static com.windroid.emu.activities.MainActivity.enableAFME;
import static com.windroid.emu.activities.MainActivity.enableDRI3;
import static com.windroid.emu.activities.MainActivity.homeDir;
import static com.windroid.emu.activities.MainActivity.preferences;
import static com.windroid.emu.activities.MainActivity.ratPackagesDir;
import static com.windroid.emu.activities.MainActivity.selectedBox64;
import static com.windroid.emu.activities.MainActivity.selectedDXVKHud;
import static com.windroid.emu.activities.MainActivity.selectedGLProfile;
import static com.windroid.emu.activities.MainActivity.selectedMesaVkWsiPresentMode;
import static com.windroid.emu.activities.MainActivity.selectedTuDebugPreset;
import static com.windroid.emu.activities.MainActivity.selectedTuTextureLodBias;
import static com.windroid.emu.activities.MainActivity.selectedTuForceMipLevel;
import static com.windroid.emu.activities.MainActivity.enableTuForceShadingRate;
import static com.windroid.emu.activities.MainActivity.enableTuForceSysmem;
import static com.windroid.emu.activities.MainActivity.enableTuDisableShadows;
import static com.windroid.emu.activities.MainActivity.enableTuDisableHeavyEffects;
import static com.windroid.emu.activities.MainActivity.selectedVramLimit;
import static com.windroid.emu.activities.MainActivity.selectedWine;
import static com.windroid.emu.activities.MainActivity.strBoolToNum;
import static com.windroid.emu.activities.MainActivity.tmpDir;
import static com.windroid.emu.activities.MainActivity.useAdrenoTools;
import static com.windroid.emu.activities.MainActivity.usrDir;
import static com.windroid.emu.activities.MainActivity.wineESync;
import static com.windroid.emu.activities.MainActivity.wineFsrMode;
import static com.windroid.emu.activities.MainActivity.wineFsrSharpness;
import static com.windroid.emu.activities.MainActivity.wineLogLevel;
import static com.windroid.emu.activities.MainActivity.selectedSuperResolution;
import static com.windroid.emu.activities.MainActivity.selectedWineFrameSkip;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_FRAMESKIP_0;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_FRAMESKIP_1;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_FRAMESKIP_2;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_FRAMESKIP_3;
import static com.windroid.emu.adapters.AdapterGame.selectedGameName;
import static com.windroid.emu.fragments.EnvVarsSettingsFragment.getCustomEnvVars;
import static com.windroid.emu.fragments.ShortcutsFragment.getEnvVars;

import com.windroid.emu.adapters.AdapterEnvVar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnvVars {
    public static String getEnv() {
        final ArrayList<String> vars = new ArrayList<>();

        setEnv(vars);

        if (preferences != null) {
            List<AdapterEnvVar.EnvVar> customEnvVars = getCustomEnvVars();

            customEnvVars.forEach((v) -> vars.add(v.key + "=" + v.value));
            getEnvVars(selectedGameName).forEach((v) -> vars.add(v.key + "=" + v.value));
        }

        return "env " + String.join(" ", vars) + " ";
    }

    private static void setEnv(ArrayList<String> vars) {
        vars.add("LANG=" + appLang + ".UTF-8");
        vars.add("TMPDIR=" + tmpDir);
        vars.add("HOME=" + homeDir);
        vars.add("XDG_CONFIG_HOME=" + homeDir + "/.config");
        vars.add("WINDROID_IPC_SOCKET=" + com.windroid.emu.activities.MainActivity.WINDROID_IPC_SOCKET_PATH);
        vars.add("WINDROID_SHM_PATH=" + com.windroid.emu.activities.MainActivity.WINDROID_SHM_PATH);
        vars.add("PULSE_LATENCY_MSEC=60");
        vars.add("LD_LIBRARY_PATH=" + usrDir + "/lib:/system/lib64");
        vars.add("PATH=$PATH:" + usrDir + "/bin:" + ratPackagesDir + "/" + selectedWine + "/files/wine/bin:"
                + ratPackagesDir + "/" + selectedWine + "/files/wine/lib/wine/x86_64-unix:" + ratPackagesDir + "/"
                + selectedBox64 + "/files/usr/bin");
        vars.add("PREFIX=" + usrDir);
        vars.add("MESA_SHADER_CACHE_DIR=" + homeDir + "/.cache");
        vars.add("MESA_VK_WSI_PRESENT_MODE=" + selectedMesaVkWsiPresentMode);

        if (selectedGLProfile != null) {
            String glVersionStr = selectedGLProfile.split(" ")[1];
            int glVersionInt = Integer.parseInt(glVersionStr.replace(".", ""));
            String glslVersion = switch (glVersionInt) {
                case 32 -> "150";
                case 31 -> "140";
                case 30 -> "130";
                case 21 -> "120";
                default -> String.valueOf(glVersionInt * 10);
            };

            vars.add("MESA_GL_VERSION_OVERRIDE=" + glVersionStr);
            vars.add("MESA_GLSL_VERSION_OVERRIDE=" + glslVersion);
        }

        vars.add("VK_ICD_FILENAMES=" + appRootDir + "/vulkan_icd.json");

        vars.add("GALLIUM_DRIVER=zink");

        // Build TU_DEBUG with VRAM limit if specified
        String tuDebug = selectedTuDebugPreset;
        if (selectedVramLimit != null && !selectedVramLimit.equals("Auto")) {
            // Extract the numeric value from the limit (e.g., "512 MB" -> "512")
            String vramValue = selectedVramLimit.replace(" MB", "").trim();
            tuDebug = tuDebug + ",max_vram:" + vramValue;
        }
        vars.add("TU_DEBUG=" + tuDebug);

        if (selectedTuTextureLodBias != null && !selectedTuTextureLodBias.equals("Off")) {
            vars.add("TU_TEXTURE_LOD_BIAS=" + selectedTuTextureLodBias);
        }

        if (selectedTuForceMipLevel != null && !selectedTuForceMipLevel.equals("Off")) {
            String mipVal = selectedTuForceMipLevel.replaceAll("[^0-9]", "");
            if (!mipVal.isEmpty()) {
                vars.add("TU_FORCE_MIP_LEVEL=" + mipVal);
            }
        }

        if (enableTuForceShadingRate) {
            vars.add("TU_FORCE_SHADING_RATE=1");
        }

        if (enableTuForceSysmem) {
            vars.add("TU_FORCE_SYSMEM=1");
        }

        if (enableTuDisableShadows) {
            vars.add("TU_DISABLE_SHADOWS=1");
        }

        if (enableTuDisableHeavyEffects) {
            vars.add("TU_DISABLE_HEAVY_EFFECTS=1");
        }

        vars.add("ZINK_DEBUG=compact");
        vars.add("ZINK_DESCRIPTORS=lazy");

        vars.add("DXVK_ASYNC=1");
        vars.add("DXVK_STATE_CACHE_PATH=" + homeDir + "/.cache/dxvk-shader-cache");
        vars.add("DXVK_HUD=" + selectedDXVKHud);

        if (fpsLimit > 0) {
            vars.add("DXVK_FRAME_RATE=" + fpsLimit);
        }

        if (selectedSuperResolution) {
            vars.add("WINE_FULLSCREEN_FSR=1");

            int fsrMode = switch (wineFsrMode) {
                case "Ultra Quality" -> 1;
                case "Quality" -> 2;
                case "Balanced" -> 3;
                case "Performance" -> 4;
                default -> 1;
            };

            vars.add("WINE_FULLSCREEN_FSR_MODE=" + fsrMode);
            vars.add("WINE_FULLSCREEN_FSR_SHARPNESS=" + wineFsrSharpness);
        }

        if (!selectedWineFrameSkip.equals(WINE_FRAMESKIP_0)) {
            int skip = switch (selectedWineFrameSkip) {
                case WINE_FRAMESKIP_1 -> 1;
                case WINE_FRAMESKIP_2 -> 2;
                case WINE_FRAMESKIP_3 -> 3;
                default -> 0;
            };
            vars.add("WINE_FRAME_SKIP=" + skip);
        }

        vars.add("MANGOHUD=1");
        vars.add("MANGOHUD_CONFIGFILE=" + usrDir + "/etc/MangoHud.conf");

        if (!deviceArch.equals("x86_64")) {
            vars.add("BOX64_LOG=" + box64LogLevel);

            if (com.windroid.emu.activities.MainActivity.wineHide) {
                vars.add("BOX64_CPUNAME=\"Intel(R) Core(TM) i9-10900K CPU @ 3.70GHz\"");
                vars.add("BOX64_SSE42=1");
                vars.add("BOX64_AVX=2");
                vars.add("BOX64_DYNAREC_STRONGMEM=1");
                vars.add("BOX64_DYNAREC_WAIT=1");
            } else {
                vars.add("BOX64_CPUNAME=\"ARM64 CPU\"");
                vars.add("BOX64_AVX=" + box64Avx);
                vars.add("BOX64_SSE42=" + box64Sse42);
                vars.add("BOX64_DYNAREC_STRONGMEM=" + box64DynarecStrongMem);
                vars.add("BOX64_DYNAREC_WAIT=" + box64DynarecWait);
            }

            vars.add("BOX64_MMAP32=" + box64MMap32);
            vars.add("BOX64_RCFILE=" + usrDir + "/etc/box64.box64rc");
            vars.add("BOX64_DYNAREC_BIGBLOCK=" + box64DynarecBigBlock);
            vars.add("BOX64_DYNAREC_WEAKBARRIER=" + box64DynarecWeakBarrier);
            vars.add("BOX64_DYNAREC_PAUSE=" + box64DynarecPause);
            vars.add("BOX64_DYNAREC_X87DOUBLE=" + box64DynarecX87Double);
            vars.add("BOX64_DYNAREC_FASTNAN=" + box64DynarecFastNan);
            vars.add("BOX64_DYNAREC_FASTROUND=" + box64DynarecFastRound);
            vars.add("BOX64_DYNAREC_SAFEFLAGS=" + box64DynarecSafeFlags);
            vars.add("BOX64_DYNAREC_CALLRET=" + box64DynarecCallRet);
            vars.add("BOX64_DYNAREC_ALIGNED_ATOMICS=" + box64DynarecAlignedAtomics);
            vars.add("BOX64_DYNAREC_NATIVEFLAGS=" + box64DynarecNativeFlags);
            vars.add("BOX64_DYNAREC_BLEEDING_EDGE=" + box64DynarecBleedingEdge);
            vars.add("BOX64_DYNAREC_DIRTY=" + box64DynarecDirty);
            vars.add("BOX64_DYNAREC_FORWARD=" + box64DynarecForward);
            vars.add("BOX64_DYNAREC_DF=" + box64DynarecDF);
            vars.add("BOX64_SHOWSEGV=" + box64ShowSegv);
            vars.add("BOX64_SHOWBT=" + box64ShowBt);
            vars.add("BOX64_NOSIGSEGV=" + box64NoSigSegv);
            vars.add("BOX64_NOSIGILL=" + box64NoSigill);
        }

        vars.add("VKD3D_FEATURE_LEVEL=12_0");

        if (Objects.equals(wineLogLevel, "disabled")) {
            vars.add("WINEDEBUG=-all");
        }

        vars.add("WINE_Z_DISK=" + appRootDir);
        vars.add("WINEESYNC=" + strBoolToNum(wineESync));

        vars.add("WINE_NO_WINEMENUBUILDER=1");

        if (useAdrenoTools && adrenoToolsDriverFile != null) {
            vars.add("USE_ADRENOTOOLS=1");
            vars.add("ADRENOTOOLS_CUSTOM_DRIVER_DIR=" + adrenoToolsDriverFile.getParent() + "/");
            vars.add("ADRENOTOOLS_CUSTOM_DRIVER_NAME=" + adrenoToolsDriverFile.getName());
            // Fix Segfault (at least on my device)
            vars.add("LD_PRELOAD=/system/lib64/libEGL.so");
        }

        if (enableAFME) {
            vars.add("ADRENO_AFME_ENABLE=1");
        }

        // Force SDL Games to use DInput/XInput (RawInput and WGI don't works)
        vars.add("SDL_JOYSTICK_WGI=0");
        vars.add("SDL_JOYSTICK_RAWINPUT=0");

        // Virtual controller mode (HID or WINEXINPUT)
        boolean useWinexinputMode = preferences.getBoolean("virtual_controller_mode", false);
        vars.add("WINDROID_CONTROLLER_MODE=" + (useWinexinputMode ? "WINEXINPUT" : "HID"));
    }
}