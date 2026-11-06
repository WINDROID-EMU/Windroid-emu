package com.windroid.emu.core;

import static com.windroid.emu.activities.MainActivity.deviceArch;
import static com.windroid.emu.activities.MainActivity.selectedEmulator;
import static com.windroid.emu.activities.MainActivity.usrDir;
import static com.windroid.emu.activities.MainActivity.wineDisksFolder;
import static com.windroid.emu.activities.MainActivity.winePrefix;
import static com.windroid.emu.activities.MainActivity.winePrefixesDir;
import static com.windroid.emu.activities.MainActivity.ratPackagesDir;
import static com.windroid.emu.activities.MainActivity.selectedWine;
import static com.windroid.emu.core.EnvVars.getEnv;
import static com.windroid.emu.core.ShellLoader.runCommand;
import static com.windroid.emu.core.ShellLoader.runCommandWithOutput;
import static com.windroid.emu.fragments.DebugSettingsFragment.availableCPUs;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class WineWrapper {
    private static final String TAG = "WineWrapper";
    public static final String IS_BOX64 = deviceArch.equals("x86_64") ? "" : "box64";

    public static String getEmulatorRunner() {
        if (deviceArch.equals("x86_64")) {
            return "";
        }
        if ("FEXCore".equals(selectedEmulator)) {
            return "FEXLoader --";
        }
        return "box64";
    }

    /**
     * Verifica se o comando é uma solicitação para abrir o WinetricksFragment.
     * Isso é usado para interceptar cliques no atalho do winetricks no menu Iniciar.
     */
    public static boolean isWinetricksCommand(String args) {
        if (args == null) return false;
        String lowerArgs = args.toLowerCase();
        return lowerArgs.contains("winetricks.bat") || lowerArgs.contains("winetricks.exe");
    }

    public static String getCpuHexMask(String cpuAffinityMask) {
        int availCpus = Runtime.getRuntime().availableProcessors();
        List<Character> cpuMask = new ArrayList<>();

        for (int i = 0; i < availCpus; i++) {
            cpuMask.add('0');
        }

        String cpuAffinity = cpuAffinityMask.replace(",", "");

        for (char element : cpuAffinity.toCharArray()) {
            int index = Math.abs(Character.getNumericValue(element) - availCpus) - 1;
            cpuMask.set(index, '1');
        }

        StringBuilder binary = new StringBuilder();
        for (char c : cpuMask) {
            binary.append(c);
        }

        return Integer.toHexString(Integer.parseInt(binary.toString(), 2));
    }

    public static boolean[] maskToCpuAffinity(long mask) {
        int availCpus = Runtime.getRuntime().availableProcessors();
        boolean[] affinity = new boolean[availCpus];

        for (int i = 0; i < availCpus; i++) {
            if (((mask >> i) & 1L) == 1L) {
                affinity[i] = true;
            }
        }

        return affinity;
    }

    public static void waitForProcess(String name) {
        waitForProcess(name, 60000); // Default timeout of 60 seconds
    }

    public static void waitForProcess(String name, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (isProcessRunning(name)) {
                return;
            }
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                Log.w(TAG, "Timeout waiting for process: " + name);
                return;
            }
            
            try {
                Thread.sleep(125);
            } catch (InterruptedException e) {
                Log.w(TAG, "Process wait interrupted", e);
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static boolean isProcessRunning(String name) {
        File[] processes = new File("/proc").listFiles();
        if (processes == null)
            return false;

        for (File process : processes) {
            if (process.isDirectory()) {
                try {
                    // Fast check: just read cmdline, don't follow symlinks or read stats yet
                    File cmdlineFile = new File(process, "cmdline");
                    if (cmdlineFile.exists()) {
                        byte[] cmdlineBytes = Files.readAllBytes(cmdlineFile.toPath());
                        String cmdline = new String(cmdlineBytes).replace('\0', ' ');

                        if (cmdline.toLowerCase().contains(name.toLowerCase())) {
                            return true;
                        }
                    }
                } catch (IOException | NumberFormatException ignored) {
                    // Process might have died or access denied
                }
            }
        }
        return false;
    }

    public static void wine(String args) {
        wine(args, null);
    }

    public static void wine(String args, String cwd) {
        String wineBin = ratPackagesDir + "/" + selectedWine + "/files/wine/bin/wine";

        runCommand(
                ((cwd != null) ? "cd " + cwd + ";" : "") + getEnv() + "WINEPREFIX='" + winePrefixesDir + "/"
                        + winePrefix + "' " + getEmulatorRunner() + " " + wineBin + " " + args,
                true);
    }

    public static void runVirtualAndroidTest() {
        String testBin = ratPackagesDir + "/" + selectedWine + "/files/wine/lib/wine/x86_64-windows/ntdll_test.exe";
        wine(testBin + " virtual_android");
    }

    public static void killAll() {
        String wineserverBin = ratPackagesDir + "/" + selectedWine + "/files/wine/bin/wineserver";

        runCommand(getEnv() + "WINEPREFIX='" + winePrefixesDir + "/" + winePrefix + "' " + getEmulatorRunner() + " " + wineserverBin + " -k",
                false);
        runCommand("pkill -9 -f .exe", false);
        runCommand("pkill -9 -f wineserver", false);
        runCommand("pkill -9 -f wineboot", false);
        runCommand("pkill -9 -f wine", false);
        runCommand("pkill -9 -f FEXLoader", false);
        runCommand("pkill -9 -f FEXInterpreter", false);
        runCommand("pkill -9 -f FEXServer", false);
        runCommand("pkill -9 -f box64", false);
    }

    public static void clearDrives() {
        char letter = 'e';

        while (letter <= 'y') {
            File diskFile = new File(wineDisksFolder + "/" + letter + ":");
            if (diskFile.exists()) {
                diskFile.delete();
            }
            letter++;
        }
    }

    public static void addDrive(String path) {
        runCommand("ln -sf " + path + " " + wineDisksFolder + "/" + getAvailableDisks().get(0) + ":", false);
    }

    private static List<String> getAvailableDisks() {
        char letter = 'e';

        List<String> availableDisks = new ArrayList<>();

        while (letter <= 'z') {
            File diskFile = new File(wineDisksFolder + "/" + letter + ":");
            if (!diskFile.exists()) {
                availableDisks.add(String.valueOf(letter));
            }
            letter++;
        }

        return availableDisks;
    }

    public static void extractIcon(String exePath, String output) {
        if (exePath.toLowerCase().endsWith(".exe")) {
            runCommand(
                    getEnv() + "wrestool -x -t 14 '" + getSanitizedPath(exePath) + "' > '" + output + "'", false);
        }
    }

    public static String getSanitizedPath(String filePath) {
        return filePath.replace("'", "'\\''");
    }

    private static String getProcessPath(String processName, String processCwd) {
        File file = new File(processCwd, processName);
        if (file.exists()) {
            return file.toString();
        }

        String[] searchPaths = {
                wineDisksFolder + "/c:/windows/system32",
                wineDisksFolder + "/c:/windows/",
        };

        for (String path : searchPaths) {
            File exeFile = new File(path, processName);
            if (exeFile.exists()) {
                return path + "/" + processName;
            }
        }

        return "";
    }

    private static int getProcessRamUsageKB(int pid) {
        File statusFile = new File("/proc/" + pid + "/status");
        if (!statusFile.exists()) {
            return 0;
        }

        try {
            List<String> lines = Files.readAllLines(statusFile.toPath());

            for (String line : lines) {
                if (line.startsWith("VmRSS:")) {
                    return Integer.parseInt(line.substring(line.indexOf(":") + 1).replace("kB", "").trim());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read process RAM usage for PID: " + pid, e);
        }

        return 0;
    }

    public static String getProcessCPUAffinity(int pid) {
        File statusFile = new File("/proc/" + pid + "/status");
        if (!statusFile.exists()) {
            return "0";
        }

        try {
            List<String> lines = Files.readAllLines(statusFile.toPath());

            for (String line : lines) {
                if (line.startsWith("Cpus_allowed:")) {
                    return line.substring(line.indexOf(":") + 1).replace(",", "").trim();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read process CPU affinity for PID: " + pid, e);
        }

        return "0";
    }

    public static int getWinPidByName(String processName) {
        String extra = "FEXCore".equals(selectedEmulator) ? "FEX_SILENTLOG=1 " : "BOX64_LOG=0 ";
        String[] taskList = runCommandWithOutput(
                getEnv() + extra + "WINEPREFIX='" + winePrefixesDir + "/" + winePrefix + "' " + getEmulatorRunner()
                        + " wine tasklist",
                false).split("\n");

        for (String s : taskList) {
            if (s.contains(processName)) {
                return Integer.parseInt(s.trim().replaceAll("  +", " ").split(" ")[1]);
            }
        }

        return -1;
    }

    public static List<ExeProcess> getExeProcesses() {
        List<ExeProcess> exeProcesses = new ArrayList<>();

        File[] processes = new File("/proc").listFiles();

        if (processes != null) {
            for (File process : processes) {
                if (process.isDirectory()) {
                    Integer unixPid;

                    try {
                        unixPid = Integer.parseInt(process.getName());
                    } catch (NumberFormatException ignored) {
                        unixPid = null;
                    }

                    File cmdlineFile = new File(process, "cmdline");
                    if (cmdlineFile.exists() && unixPid != null) {
                        try {
                            byte[] cmdlineBytes = Files.readAllBytes(cmdlineFile.toPath());
                            String cmdline = new String(cmdlineBytes).trim();

                            // Handle null chars if present
                            if (cmdline.contains("\u0000")) {
                                cmdline = cmdline.split("\u0000")[0];
                            }

                            String processName = cmdline;

                            if (processName != null && processName.toLowerCase().endsWith(".exe")) {
                                processName = new File(processName).getName(); // Ensure we get just the name

                                String cwd = "";
                                try {
                                    cwd = Files.readSymbolicLink(new File(process, "cwd").toPath()).toString();
                                } catch (IOException e) {
                                    // Fallback or ignore
                                }

                                String path = getProcessPath(processName, cwd);
                                String iconPath = usrDir + "/icons/"
                                        + processName.substring(0, processName.indexOf(".exe")) + "-thumbnail";
                                int ramUsageKB = getProcessRamUsageKB(unixPid);
                                float cpuUsage = getProcessCpuUsage(unixPid);

                                if (!new File(iconPath).exists()) {
                                    extractIcon(path, iconPath);
                                }

                                exeProcesses.add(
                                        new ExeProcess(processName, unixPid, cwd, path, iconPath, ramUsageKB,
                                                cpuUsage / availableCPUs.length));
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to read process info for PID: " + unixPid, e);
                        }
                    }
                }
            }
        }

        return exeProcesses;
    }

    private static float getProcessCpuUsage(int pid) {
        // Simplified CPU usage calculation based on lifetime stats
        // To do real instantaneous usage we'd need to store previous state, which is
        // overkill for this optimization pass.
        // We will return (utime + stime) / (uptime - starttime) which is the lifetime
        // average usage.
        try {
            File statFile = new File("/proc/" + pid + "/stat");
            File uptimeFile = new File("/proc/uptime");

            if (!statFile.exists() || !uptimeFile.exists())
                return 0F;

            String statContent = new String(Files.readAllBytes(statFile.toPath())).trim();
            String uptimeContent = new String(Files.readAllBytes(uptimeFile.toPath())).trim();

            // uptime is "total_seconds idle_seconds"
            float uptime = Float.parseFloat(uptimeContent.split(" ")[0]);

            // /proc/[pid]/stat format is complex, but fields are space separated.
            // Field 14: utime, Field 15: stime, Field 22: starttime
            // Wait, filename can have spaces and is in parenthesis ( ). We need to handle
            // that.
            int lastParenIndex = statContent.lastIndexOf(')');
            if (lastParenIndex == -1)
                return 0F;

            String statsString = statContent.substring(lastParenIndex + 2); // Skip ") "
            String[] stats = statsString.split(" ");

            // Indexes in stats array (0-based) relative to after filename:
            // Original 14 (utime) -> becomes index 11
            // Original 15 (stime) -> becomes index 12
            // Original 22 (starttime) -> becomes index 19

            long utime = Long.parseLong(stats[11]);
            long stime = Long.parseLong(stats[12]);
            long starttime = Long.parseLong(stats[19]);

            long clkTck = 100; // Android/Linux usually 100Hz. sysconf(_SC_CLK_TCK).
            // NOTE: In Java we can't easily get CLK_TCK without JNI, but 100 is standard
            // for ARM Android.
            // If it's 1000, off by 10x.

            float totalTimeSeconds = (utime + stime) / (float) clkTck;
            float startTimeSeconds = starttime / (float) clkTck;

            float secondsActive = uptime - startTimeSeconds;

            if (secondsActive > 0) {
                return (totalTimeSeconds / secondsActive) * 100F;
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate CPU usage for PID: " + pid, e);
        }
        return 0F;
    }

    public static class ExeProcess {
        String name;
        int unixPid;
        String cwd;
        String path;
        String iconPath;
        int ramUsageKB;
        float cpuUsage;

        public ExeProcess(String name, int unixPid, String cwd, String path, String iconPath, int ramUsageKB,
                float cpuUsage) {
            this.name = name;
            this.unixPid = unixPid;
            this.cwd = cwd;
            this.path = path;
            this.iconPath = iconPath;
            this.ramUsageKB = ramUsageKB;
            this.cpuUsage = cpuUsage;
        }

        public String getName() {
            return name;
        }

        public int getRamUsageKB() {
            return ramUsageKB;
        }

        public float getCpuUsage() {
            return cpuUsage;
        }

        public String getIconPath() {
            return iconPath;
        }

        public int getUnixPid() {
            return unixPid;
        }
    }
}