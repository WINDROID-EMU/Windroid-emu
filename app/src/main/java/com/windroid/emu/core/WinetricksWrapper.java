package com.windroid.emu.core;

import static com.windroid.emu.activities.MainActivity.appLang;
import static com.windroid.emu.activities.MainActivity.deviceArch;
import static com.windroid.emu.activities.MainActivity.homeDir;
import static com.windroid.emu.activities.MainActivity.ratPackagesDir;
import static com.windroid.emu.activities.MainActivity.selectedBox64;
import static com.windroid.emu.activities.MainActivity.selectedWine;
import static com.windroid.emu.activities.MainActivity.usrDir;
import static com.windroid.emu.activities.MainActivity.winePrefix;
import static com.windroid.emu.activities.MainActivity.winePrefixesDir;
import static com.windroid.emu.core.ShellLoader.runCommand;

public class WinetricksWrapper {
    private static final String IS_BOX64 = deviceArch.equals("x86_64") ? "" : "box64";

    public static int winetricks(String args) {
        return winetricks(args, null);
    }

    private static String getEnv() {
        String box64BinDir = ratPackagesDir + "/" + selectedBox64 + "/files/usr/bin";

        return "export LANG=" + appLang + ".UTF-8; " +
                "export TMPDIR=" + usrDir + "/tmp; " +
                "export HOME=" + homeDir + "; " +
                "export XDG_CONFIG_HOME=" + homeDir + "/.config; " +
                "export WINDROID_IPC_SOCKET=" + com.windroid.emu.activities.MainActivity.WINDROID_IPC_SOCKET_PATH + "; " +
                "export WINDROID_SHM_PATH=" + com.windroid.emu.activities.MainActivity.WINDROID_SHM_PATH + "; " +
                "export PULSE_LATENCY_MSEC=60; " +
                "export LD_LIBRARY_PATH=" + usrDir + "/lib:/system/lib64; " +
                "export PATH=" + usrDir + "/bin:" + box64BinDir + ":$PATH; " +
                "export PREFIX=" + usrDir + "; " +
                "export MESA_SHADER_CACHE_DIR=" + homeDir + "/.cache; " +
                "export MESA_VK_WSI_PRESENT_MODE=mailbox; " +
                "export MESA_GL_VERSION_OVERRIDE=3.2; " +
                "export MESA_GLSL_VERSION_OVERRIDE=150; " +
                "export VK_ICD_FILENAMES=" + usrDir + "/vulkan_icd.json; " +
                "export GALLIUM_DRIVER=zink; " +
                "export TU_DEBUG=noconform,sysmem; " +
                "export ZINK_DEBUG=compact; " +
                "export ZINK_DESCRIPTORS=lazy; " +
                "export WINEDEBUG=-all; " +
                "export BOX64_LOG=0; " +
                "export BOX64_NOBANNER=1; ";
    }

    public static int winetricks(String args, String cwd) {
        String wineBinDir = ratPackagesDir + "/" + selectedWine + "/files/wine/bin";

        String wineWrapper = usrDir + "/bin/wine";
        String wine64Wrapper = usrDir + "/bin/wine64";
        String wineserverWrapper = usrDir + "/bin/wineserver";
        String winebootWrapper = usrDir + "/bin/wineboot";
        String lscpuWrapper = usrDir + "/bin/lscpu";

        String realWine = wineBinDir + "/wine";
        String realWineserver = wineBinDir + "/wineserver";
        String realWineboot = wineBinDir + "/wineboot";

        String prefix = IS_BOX64.isEmpty() ? "" : IS_BOX64 + " ";

        String setupWrappers = "echo '#!/system/bin/sh' > " + wineWrapper + "; " +
                "echo 'exec " + prefix + realWine + " \"$@\"' >> " + wineWrapper + "; " +
                "cp " + wineWrapper + " " + wine64Wrapper + "; " +
                "echo '#!/system/bin/sh' > " + wineserverWrapper + "; " +
                "echo 'exec " + prefix + realWineserver + " \"$@\"' >> " + wineserverWrapper + "; " +
                "echo '#!/system/bin/sh' > " + winebootWrapper + "; " +
                "echo 'exec " + prefix + realWineboot + " \"$@\"' >> " + winebootWrapper + "; " +
                "echo '#!/system/bin/sh\n[ \"$1\" = \"--all\" ] || [ \"$1\" = \"-a\" ] && echo \"Architecture: x86_64\nCPU op-mode(s): 32-bit, 64-bit\" || echo \"x86_64\"' > " + lscpuWrapper + "; " +
                "echo 'check_certificate = off' > " + homeDir + "/.wgetrc; " +
                "[ -f " + usrDir + "/bin/winetricks ] || wget --no-check-certificate -O " + usrDir + "/bin/winetricks https://raw.githubusercontent.com/Winetricks/winetricks/master/src/winetricks || curl -k -o " + usrDir + "/bin/winetricks https://raw.githubusercontent.com/Winetricks/winetricks/master/src/winetricks; " +
                "chmod +x " + wineWrapper + " " + wine64Wrapper + " " + wineserverWrapper + " " + winebootWrapper + " " + lscpuWrapper + " " + usrDir + "/bin/winetricks; ";

        boolean isListCommand = args.contains("list");
        String winetricksCmd = "export WINEPREFIX='" + winePrefixesDir + "/" + winePrefix + "'; " +
                "export WINE='" + wineWrapper + "'; " +
                "export WINESERVER='" + wineserverWrapper + "'; " +
                "export WINETRICKS_CHECK_FOR_UPDATES=0; " +
                "export WINETRICKS_LATEST_VERSION_CHECK=0; " +
                "export WINETRICKS_VERSION=20240105; " +
                "cd " + homeDir + "; " +
                (isListCommand ? "" : "wineboot -p; sleep 1; ") +
                "sh " + usrDir + "/bin/winetricks --unattended " + args;

        return runCommand(getEnv() + setupWrappers + winetricksCmd, true);
    }
}
