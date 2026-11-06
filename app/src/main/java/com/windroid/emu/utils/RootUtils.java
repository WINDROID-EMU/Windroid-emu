package com.windroid.emu.utils;

import android.util.Log;

import static com.windroid.emu.core.ShellLoader.runCommand;
import static com.windroid.emu.core.ShellLoader.runCommandWithOutput;

public class RootUtils {
    private static final String TAG = "RootUtils";
    private static String[] originalCpuGovernors;
    private static String originalGpuGovernor;

    /**
     * Escapes a shell argument to prevent command injection.
     * Wraps the argument in single quotes and escapes any existing single quotes.
     */
    private static String escapeShellArg(String arg) {
        if (arg == null) return "";
        return "'" + arg.replace("'", "'\\''") + "'";
    }
    private static boolean originalSelinuxPermissive;

    /**
     * Checks if root access is available.
     * @return true if root is available.
     */
    public static boolean isRootAvailable() {
        String testRoot = runCommandWithOutput("su -c 'echo test' 2>/dev/null", false);
        return testRoot != null && testRoot.contains("test");
    }

    /**
     * Applies performance mode by setting CPU and GPU governors to 'performance'.
     */
    public static void applyPerformanceMode() {
        if (!isRootAvailable()) {
            Log.w(TAG, "Root not available, cannot apply performance mode");
            return;
        }

        Log.i(TAG, "Applying performance mode...");

        // CPU
        int cpuCount = Runtime.getRuntime().availableProcessors();
        originalCpuGovernors = new String[cpuCount];

        for (int i = 0; i < cpuCount; i++) {
            String path = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_governor";
            originalCpuGovernors[i] = runCommandWithOutput("su -c cat " + escapeShellArg(path), false);
            if (originalCpuGovernors[i] != null) {
                originalCpuGovernors[i] = originalCpuGovernors[i].trim();
                runCommand("su -c sh -c 'echo performance > " + escapeShellArg(path) + "'", false);
            }
        }

        // GPU (Adreno)
        String adrenoPath = "/sys/class/kgsl/kgsl-3d0/devfreq/governor";
        originalGpuGovernor = runCommandWithOutput("su -c cat " + escapeShellArg(adrenoPath), false);
        if (originalGpuGovernor != null) {
            originalGpuGovernor = originalGpuGovernor.trim();
            runCommand("su -c sh -c 'echo performance > " + escapeShellArg(adrenoPath) + "'", false);
        } else {
            // Try another common path
            adrenoPath = "/sys/class/kgsl/kgsl-3d0/governor";
            originalGpuGovernor = runCommandWithOutput("su -c cat " + escapeShellArg(adrenoPath), false);
            if (originalGpuGovernor != null) {
                originalGpuGovernor = originalGpuGovernor.trim();
                runCommand("su -c sh -c 'echo performance > " + escapeShellArg(adrenoPath) + "'", false);
            }
        }

        optimizeIOScheduler();
    }

    /**
     * Restores default governor settings.
     */
    public static void restoreDefaultMode() {
        if (!isRootAvailable()) return;

        Log.i(TAG, "Restoring default mode...");

        // CPU
        if (originalCpuGovernors != null) {
            for (int i = 0; i < originalCpuGovernors.length; i++) {
                if (originalCpuGovernors[i] != null) {
                    String path = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_governor";
                    runCommand("su -c sh -c 'echo " + escapeShellArg(originalCpuGovernors[i]) + " > " + escapeShellArg(path) + "'", false);
                }
            }
        } else {
            // Fallback to schedutil or interactive
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                String path = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_governor";
                runCommand("su -c sh -c 'echo schedutil > " + escapeShellArg(path) + "' 2>/dev/null", false);
                runCommand("su -c sh -c 'echo interactive > " + escapeShellArg(path) + "' 2>/dev/null", false);
            }
        }

        // GPU
        if (originalGpuGovernor != null) {
            String adrenoPath = "/sys/class/kgsl/kgsl-3d0/devfreq/governor";
            runCommand("su -c sh -c 'echo " + escapeShellArg(originalGpuGovernor) + " > " + escapeShellArg(adrenoPath) + "' 2>/dev/null", false);
            adrenoPath = "/sys/class/kgsl/kgsl-3d0/governor";
            runCommand("su -c sh -c 'echo " + escapeShellArg(originalGpuGovernor) + " > " + escapeShellArg(adrenoPath) + "' 2>/dev/null", false);
        } else {
            // Fallback
            String path1 = "/sys/class/kgsl/kgsl-3d0/devfreq/governor";
            String path2 = "/sys/class/kgsl/kgsl-3d0/governor";
            runCommand("su -c sh -c 'echo msm-adreno-tz > " + escapeShellArg(path1) + "' 2>/dev/null", false);
            runCommand("su -c sh -c 'echo msm-adreno-tz > " + escapeShellArg(path2) + "' 2>/dev/null", false);
        }

        // Reset I/O Scheduler
        runCommand("su -c 'echo cfq > /sys/block/mmcblk0/queue/scheduler' 2>/dev/null", false);
        runCommand("su -c 'echo mq-deadline > /sys/block/mmcblk0/queue/scheduler' 2>/dev/null", false);
    }

    /**
     * Otimiza processos do Wine/Box64 para máxima prioridade.
     */
    public static void optimizeWineProcesses() {
        if (!isRootAvailable()) return;

        Log.i(TAG, "Optimizing process priorities...");

        // Aumenta a prioridade do wineserver e processos box64
        String pids = runCommandWithOutput("su -c 'pgrep -f \"wineserver|box64\"'", false);
        if (pids != null && !pids.isEmpty()) {
            for (String pid : pids.split("\n")) {
                pid = pid.trim();
                if (!pid.isEmpty()) {
                    // CPU Priority (Niceness)
                    runCommand("su -c 'renice -n -20 -p " + pid + "'", false);
                    // I/O Priority
                    runCommand("su -c 'ionice -c 1 -n 0 -p " + pid + "'", false);
                    // OOM Protection
                    runCommand("su -c 'echo -1000 > /proc/" + pid + "/oom_score_adj'", false);
                }
            }
        }
    }

    /**
     * Melhora o scheduler de I/O para reduzir latência.
     */
    public static void optimizeIOScheduler() {
        if (!isRootAvailable()) return;

        Log.i(TAG, "Optimizing I/O scheduler...");
        // Tenta definir para deadline ou noop que costumam ser melhores para emulação
        runCommand("su -c 'echo deadline > /sys/block/mmcblk0/queue/scheduler' 2>/dev/null", false);
        runCommand("su -c 'echo noop > /sys/block/mmcblk0/queue/scheduler' 2>/dev/null", false);
        runCommand("su -c 'echo 0 > /sys/block/mmcblk0/queue/add_random' 2>/dev/null", false);
        runCommand("su -c 'echo 1024 > /sys/block/mmcblk0/queue/read_ahead_kb' 2>/dev/null", false);
    }

    /**
     * Verifica se o tmpfs já está montado.
     */
    public static boolean isTmpfsMounted() {
        if (!isRootAvailable()) return false;
        String mounts = runCommandWithOutput("su -c 'cat /proc/mounts'", false);
        return mounts != null && mounts.contains("/data/data/com.windroid.emu/files/usr/tmp");
    }

    /**
     * Monta um tmpfs na pasta temporária da esync para evitar I/O na memória Flash.
     */
    public static void mountTmpfs() {
        if (!isRootAvailable() || isTmpfsMounted()) return;

        Log.i(TAG, "Mounting tmpfs for esync...");
        String tmpPath = "/data/data/com.windroid.emu/files/usr/tmp";
        int myUid = android.os.Process.myUid();

        runCommand("su -c 'mkdir -p " + tmpPath + "'", false);

        // Mount tmpfs with app UID, GID and mode
        runCommand("su -c 'mount -t tmpfs -o size=256M,mode=0771,uid=" + myUid + ",gid=" + myUid + " tmpfs " + tmpPath + "'", false);

        if (isTmpfsMounted()) {
            // Apply app SELinux context so untrusted_app can access without AVC denial
            runCommand("su -c 'chcon u:object_r:app_data_file:s0 " + tmpPath + " 2>/dev/null || restorecon -R " + tmpPath + "'", false);
            runCommand("su -c 'chown " + myUid + ":" + myUid + " " + tmpPath + " && chmod 0771 " + tmpPath + "'", false);

            // Verify if app actually has write access
            java.io.File testFile = new java.io.File(tmpPath, ".tmpfs_test");
            boolean writeOk = false;
            try {
                if (testFile.createNewFile()) {
                    testFile.delete();
                    writeOk = true;
                }
            } catch (Exception e) {
                Log.w(TAG, "tmpfs write test failed: " + e.getMessage());
            }

            if (writeOk) {
                Log.i(TAG, "tmpfs successfully mounted and verified in RAM.");
            } else {
                Log.w(TAG, "tmpfs mounted but not writable by app due to SELinux. Unmounting to fallback to physical storage...");
                umountTmpfs();
            }
        } else {
            Log.e(TAG, "Failed to mount tmpfs. Esync will use physical storage.");
        }
    }

    /**
     * Desmonta o tmpfs quando o Wine finaliza.
     */
    public static void umountTmpfs() {
        if (!isRootAvailable()) return;

        Log.i(TAG, "Unmounting tmpfs...");
        runCommand("su -c 'umount -f /data/data/com.windroid.emu/files/usr/tmp' 2>/dev/null", false);
    }

    /**
     * Sets SELinux to permissive mode or enforcing mode.
     * @param permissive true to set to permissive (0), false to set to enforcing (1).
     */
    public static void setSelinuxPermissive(boolean permissive) {
        if (!isRootAvailable()) return;

        Log.i(TAG, "Setting SELinux to " + (permissive ? "permissive" : "enforcing") + "...");
        runCommand("su -c 'setenforce " + (permissive ? "0" : "1") + "'", false);
    }

    /**
     * Applies SELinux permissive mode and saves the original state.
     */
    public static void applySelinuxPermissive() {
        if (!isRootAvailable()) return;
        originalSelinuxPermissive = isSelinuxPermissive();
        if (!originalSelinuxPermissive) {
            setSelinuxPermissive(true);
        }
    }

    /**
     * Restores the original SELinux state.
     */
    public static void restoreSelinuxMode() {
        if (!isRootAvailable()) return;
        if (!originalSelinuxPermissive) {
            setSelinuxPermissive(false);
        }
    }

    /**
     * Checks if SELinux is currently permissive.
     * @return true if permissive, false if enforcing or unknown.
     */
    public static boolean isSelinuxPermissive() {
        if (!isRootAvailable()) return false;

        String output = runCommandWithOutput("su -c 'getenforce'", false);
        return output != null && output.trim().equalsIgnoreCase("Permissive");
    }
}
