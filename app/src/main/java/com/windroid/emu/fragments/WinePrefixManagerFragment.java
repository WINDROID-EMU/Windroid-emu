package com.windroid.emu.fragments;

import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_WINE_PREFIX;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_DPI;
import static com.windroid.emu.activities.GeneralSettingsActivity.WINE_DPI_DEFAULT_VALUE;
import static com.windroid.emu.activities.MainActivity.appRootDir;
import static com.windroid.emu.activities.MainActivity.preferences;
import static com.windroid.emu.activities.MainActivity.selectedWine;
import static com.windroid.emu.activities.MainActivity.getUnixUsername;
import static com.windroid.emu.activities.MainActivity.winePrefix;
import static com.windroid.emu.activities.MainActivity.winePrefixesDir;
import static com.windroid.emu.adapters.AdapterPreset.selectedPresetId;
import static com.windroid.emu.utils.FileUtils.copyRecursively;
import static com.windroid.emu.utils.FileUtils.deleteDirectoryRecursively;
import static com.windroid.emu.core.ShellLoader.runCommand;
import static com.windroid.emu.core.WineWrapper.killAll;
import static com.windroid.emu.core.WineWrapper.wine;
import static com.windroid.emu.fragments.CreatePresetFragment.WINE_PREFIX_PRESET;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.windroid.emu.R;
import com.windroid.emu.adapters.AdapterPreset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class WinePrefixManagerFragment extends Fragment {
    private static RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewGeneralSettings);

        initialize();
        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(
                new AdapterPreset(prefixListNames, requireContext(), requireActivity().getSupportFragmentManager()));

        prefixListNames.clear();
        prefixList.forEach((name) -> prefixListNames.add(
                new AdapterPreset.Item(name, WINE_PREFIX_PRESET, true, true)));
    }

    private static final ArrayList<AdapterPreset.Item> prefixListNames = new ArrayList<>();
    private static ArrayList<String> prefixList = new ArrayList<>();

    private static void initialize() {
        prefixList = getWinePrefixes();
    }

    private static ArrayList<String> getWinePrefixes() {
        File[] winePrefixesFiles = winePrefixesDir.listFiles();
        ArrayList<String> winePrefixesNames = new ArrayList<>();

        if (winePrefixesFiles != null) {
            for (File prefix : winePrefixesFiles) {
                winePrefixesNames.add(prefix.getName());
            }
        }

        return winePrefixesNames;
    }

    public static File getWinePrefixFile(String name) {
        return new File(winePrefixesDir, name);
    }

    public static String getSelectedWinePrefix() {
        if (preferences != null) {
            return preferences.getString(SELECTED_WINE_PREFIX, "default");
        }
        return "default";
    }

    public static void putSelectedWinePrefix(String name) {
        if (preferences == null)
            return;

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SELECTED_WINE_PREFIX, name);
        editor.apply();

        winePrefix = name;
    }

    public static void createWinePrefix(String name, String wineId) {
        File winePrefix = getWinePrefixFile(name);
        if (!winePrefix.exists()) {
            File driveC = new File(winePrefix, "drive_c");
            File wineUtils = new File(appRootDir, "wine-utils");
            File startMenu = new File(driveC, "ProgramData/Microsoft/Windows/Start Menu");
            File userRootFolder = Environment.getExternalStorageDirectory();
            File userSharedFolder = new File(userRootFolder, "Windroid");
            boolean isProton = new File(driveC, "users/steamuser").exists();

            File wineUserDir;
            if (isProton) {
                wineUserDir = new File(driveC, "users/steamuser");
            } else {
                wineUserDir = new File(driveC, "users/" + getUnixUsername());
            }

            File localPublicDocuments = new File(driveC, "users/Public/Documents");
            File sharedPublicDocuments = new File(userSharedFolder, "Public Documents");
            File localAppData = new File(wineUserDir, "AppData");
            File sharedAppData = new File(userSharedFolder, "AppData");
            File localSavedGames = new File(wineUserDir, "Saved Games");
            File localDocuments = new File(wineUserDir, "Documents");
            File sharedDocuments = new File(userSharedFolder, "Documents");
            File localSteam = new File(driveC, "Program Files (x86)/Steam");
            File sharedSteam = new File(userSharedFolder, "Steam");
            File localProgramData = new File(driveC, "ProgramData");
            File system32 = new File(driveC, "windows/system32");
            File syswow64 = new File(driveC, "windows/syswow64");
            File winePrefixConfigFile = new File(winePrefix, "config");
            File wineFontsDir = new File(driveC, "windows/Fonts");
            File coreFonts = new File(wineUtils, "CoreFonts");

            winePrefix.mkdirs();

            try (FileWriter writer = new FileWriter(winePrefixConfigFile)) {
                writer.write(wineId + "\n" + isProton + "\n");
            } catch (IOException ignored) {
            }

            selectedWine = wineId;

            killAll();
            wine("wineboot");

            copyRecursively(coreFonts, wineFontsDir);
            copyRecursively(localAppData, sharedAppData);
            copyRecursively(localDocuments, sharedDocuments);
            copyRecursively(localPublicDocuments, sharedPublicDocuments);
            
            if (localSteam.exists()) {
                copyRecursively(localSteam, sharedSteam);
            } else {
                sharedSteam.mkdirs();
                localSteam.getParentFile().mkdirs();
            }

            if (localProgramData.exists()) {
                copyRecursively(localProgramData, userSharedFolder);
            } else {
                localProgramData.getParentFile().mkdirs();
            }

            deleteDirectoryRecursively(localAppData.toPath());
            deleteDirectoryRecursively(localSavedGames.toPath());
            deleteDirectoryRecursively(localDocuments.toPath());
            deleteDirectoryRecursively(localPublicDocuments.toPath());
            deleteDirectoryRecursively(localSteam.toPath());
            deleteDirectoryRecursively(localProgramData.toPath());

            runCommand("ln -sf '" + userSharedFolder + "/AppData' '" + localAppData + "'", false);
            runCommand("ln -sf '" + userSharedFolder + "/Saved Games' '" + localSavedGames + "'", false);
            runCommand("ln -sf '" + userSharedFolder + "/Documents' '" + localDocuments + "'", false);
            runCommand("ln -sf '" + userSharedFolder + "/Public Documents' '" + localPublicDocuments + "'", false);
            runCommand("ln -sf '" + userSharedFolder + "/Steam' '" + localSteam + "'", false);
            runCommand("ln -sf '" + userSharedFolder + "' '" + localProgramData + "'", false);

            deleteDirectoryRecursively(startMenu.toPath());

            copyRecursively(new File(wineUtils, "Start Menu"), startMenu);
            copyRecursively(new File(wineUtils, "Addons"), new File(driveC, "Addons"));
            copyRecursively(new File(wineUtils, "Addons/Windows"), new File(driveC, "windows"));
            copyRecursively(new File(wineUtils, "Addons"), new File(driveC, "Addons"));
            copyRecursively(new File(wineUtils, "DirectX/x64"), system32);
            copyRecursively(new File(wineUtils, "DirectX/x32"), syswow64);
            copyRecursively(new File(wineUtils, "OpenAL/x64"), system32);
            copyRecursively(new File(wineUtils, "OpenAL/x32"), syswow64);

            wine("regedit '" + driveC + "/Addons/DefaultDLLsOverrides.reg'");
            wine("regedit '" + driveC + "/Addons/Themes/DarkBlue/DarkBlue.reg'");
            wine("reg add HKCU\\\\Software\\\\Wine\\\\Drivers /t REG_SZ /v Graphics /d android /f");
            wine("reg add HKCU\\\\Control\\ Panel\\\\Desktop /t REG_DWORD /v LogPixels /d "
                    + (preferences != null ? preferences.getInt(WINE_DPI, WINE_DPI_DEFAULT_VALUE)
                            : WINE_DPI_DEFAULT_VALUE)
                    + " /f");

            prefixList.add(name);
            prefixListNames.add(
                    new AdapterPreset.Item(name, WINE_PREFIX_PRESET, true, true));

            if (recyclerView != null) {
                recyclerView.post(() -> {
                    AdapterPreset adapter = (AdapterPreset) recyclerView.getAdapter();
                    if (adapter != null) {
                        adapter.notifyItemInserted(prefixListNames.size());
                    }
                });
            }
        }
    }

    public static boolean deleteWinePrefix(String name) {
        if (prefixList.size() == 1)
            return false;

        int index = -1;
        for (int i = 0; i < prefixList.size(); i++) {
            if (prefixList.get(i).equals(name)) {
                index = i;
                break;
            }
        }

        if (index == -1)
            return false;

        runCommand("rm -rf " + winePrefixesDir + "/" + name, false);

        prefixList.remove(index);
        prefixListNames.remove(index);

        AdapterPreset adapter = (AdapterPreset) recyclerView.getAdapter();

        if (adapter != null) {
            adapter.notifyItemRemoved(index);
        }

        if (index == selectedPresetId) {
            if (preferences != null) {
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString(SELECTED_WINE_PREFIX, prefixListNames.get(0).getTitleSettings());
                editor.apply();

                if (adapter != null) {
                    adapter.notifyItemChanged(0);
                }
            }
        }

        return true;
    }
}
