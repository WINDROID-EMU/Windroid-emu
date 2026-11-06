package com.windroid.emu.fragments;

import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_MULTI_BLOCK_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_SILENT_LOG_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_SMC_CHECKS_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_TSO_ENABLED_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_VECTOR_WIDTH_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_X87_REDUCED_PRECISION_DEFAULT_VALUE;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_FEXCORE_PRESET;
import static com.windroid.emu.activities.MainActivity.gson;
import static com.windroid.emu.activities.MainActivity.preferences;
import static com.windroid.emu.adapters.AdapterPreset.selectedPresetId;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.windroid.emu.R;
import com.windroid.emu.adapters.AdapterPreset;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class FEXCorePresetManagerFragment extends Fragment {
    public static RecyclerView recyclerView;
    public static final int FEXCORE_PRESET = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_general_settings, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewGeneralSettings);

        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterPreset(presetListAdapters, requireContext(), requireActivity().getSupportFragmentManager()));

        presetListAdapters.clear();
        presetList.forEach((i) -> presetListAdapters.add(
                new AdapterPreset.Item(i.name, FEXCORE_PRESET, true)
        ));
    }

    private static final ArrayList<AdapterPreset.Item> presetListAdapters = new ArrayList<>();
    private static ArrayList<FEXCorePreset> presetList = new ArrayList<>();
    private static final Type listType = new TypeToken<FEXCorePreset>() {}.getType();

    public static void initialize() {
        presetList = getFEXCorePresets();
    }

    public static void addFEXCorePreset(Context context, String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index != -1) {
            Toast.makeText(context, R.string.executable_already_added, Toast.LENGTH_SHORT).show();
            return;
        }

        presetList.add(new FEXCorePreset(name));
        presetListAdapters.add(new AdapterPreset.Item(name, FEXCORE_PRESET, true));

        saveFEXCorePresets();

        if (recyclerView == null) return;
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyItemInserted(presetList.size() - 1);
        }
    }

    public static boolean deleteFEXCorePreset(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1 || presetList.size() <= 1) return false;

        presetList.remove(index);
        presetListAdapters.remove(index);

        if (index == selectedPresetId && !presetList.isEmpty()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SELECTED_FEXCORE_PRESET, presetList.get(0).name);
            editor.apply();
        }

        saveFEXCorePresets();

        if (recyclerView == null) return true;
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyItemRemoved(index);
        }

        return true;
    }

    public static void renameFEXCorePreset(String name, String newName) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        presetList.get(index).name = newName;
        presetListAdapters.get(index).titleSettings = newName;

        saveFEXCorePresets();

        if (recyclerView == null) return;
        recyclerView.post(() -> {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.notifyItemChanged(index);
            }
        });
    }

    public static boolean importFEXCorePreset(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) return false;

            String type = lines.get(0);
            if (!type.equals("fexcorePreset")) return false;

            StringBuilder json = new StringBuilder();
            for (int i = 1; i < lines.size(); i++) {
                json.append(lines.get(i));
            }

            FEXCorePreset imported = gson.fromJson(json.toString(), listType);
            if (imported == null || imported.name == null) return false;

            int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(imported.name)).findFirst().orElse(-1);
            if (index != -1) {
                presetList.set(index, imported);
            } else {
                presetList.add(imported);
                presetListAdapters.add(new AdapterPreset.Item(imported.name, FEXCORE_PRESET, true));
            }

            saveFEXCorePresets();

            if (recyclerView != null && recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void exportFEXCorePreset(String name, File file) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("fexcorePreset\n" + gson.toJson(presetList.get(index)));
        } catch (Exception ignored) {
        }
    }

    private static void saveFEXCorePresets() {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("fexcorePresetList", gson.toJson(presetList));
        editor.apply();
    }

    public static ArrayList<FEXCorePreset> getFEXCorePresets() {
        if (preferences == null) return getDefaultPresets();
        String json = preferences.getString("fexcorePresetList", "");
        Type type = new TypeToken<ArrayList<FEXCorePreset>>() {}.getType();
        ArrayList<FEXCorePreset> list = gson.fromJson(json, type);

        return (list != null && !list.isEmpty()) ? list : getDefaultPresets();
    }

    private static ArrayList<FEXCorePreset> getDefaultPresets() {
        ArrayList<FEXCorePreset> defaults = new ArrayList<>();
        defaults.add(new FEXCorePreset("Balanced", true, true, true, true, "app", "128"));
        defaults.add(new FEXCorePreset("Compatibility", true, true, false, false, "mtrack", "128"));
        defaults.add(new FEXCorePreset("Performance", false, true, true, true, "none", "128"));
        return defaults;
    }

    public static boolean getFEXTsoEnabled(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        return index != -1 ? presetList.get(index).fexTsoEnabled : FEX_TSO_ENABLED_DEFAULT_VALUE;
    }

    public static void putFEXTsoEnabled(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;
        presetList.get(index).fexTsoEnabled = value;
        saveFEXCorePresets();
    }

    public static boolean getFEXMultiBlock(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        return index != -1 ? presetList.get(index).fexMultiBlock : FEX_MULTI_BLOCK_DEFAULT_VALUE;
    }

    public static void putFEXMultiBlock(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;
        presetList.get(index).fexMultiBlock = value;
        saveFEXCorePresets();
    }

    public static boolean getFEXX87ReducedPrecision(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        return index != -1 ? presetList.get(index).fexX87ReducedPrecision : FEX_X87_REDUCED_PRECISION_DEFAULT_VALUE;
    }

    public static void putFEXX87ReducedPrecision(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;
        presetList.get(index).fexX87ReducedPrecision = value;
        saveFEXCorePresets();
    }

    public static boolean getFEXSilentLog(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        return index != -1 ? presetList.get(index).fexSilentLog : FEX_SILENT_LOG_DEFAULT_VALUE;
    }

    public static void putFEXSilentLog(String name, boolean value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;
        presetList.get(index).fexSilentLog = value;
        saveFEXCorePresets();
    }

    public static String getFEXSmcChecks(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        return index != -1 ? presetList.get(index).fexSmcChecks : FEX_SMC_CHECKS_DEFAULT_VALUE;
    }

    public static void putFEXSmcChecks(String name, String value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;
        presetList.get(index).fexSmcChecks = value;
        saveFEXCorePresets();
    }

    public static String getFEXVectorWidth(String name) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        return index != -1 ? presetList.get(index).fexVectorWidth : FEX_VECTOR_WIDTH_DEFAULT_VALUE;
    }

    public static void putFEXVectorWidth(String name, String value) {
        int index = IntStream.range(0, presetList.size()).filter(i -> presetList.get(i).name.equals(name)).findFirst().orElse(-1);
        if (index == -1) return;
        presetList.get(index).fexVectorWidth = value;
        saveFEXCorePresets();
    }

    public static class FEXCorePreset {
        public String name;
        public boolean fexTsoEnabled = true;
        public boolean fexMultiBlock = true;
        public boolean fexX87ReducedPrecision = false;
        public boolean fexSilentLog = true;
        public String fexSmcChecks = "app";
        public String fexVectorWidth = "128";

        public FEXCorePreset(String name) {
            this.name = name;
        }

        public FEXCorePreset(String name, boolean tso, boolean multiBlock, boolean x87Reduced, boolean silentLog, String smc, String vectorWidth) {
            this.name = name;
            this.fexTsoEnabled = tso;
            this.fexMultiBlock = multiBlock;
            this.fexX87ReducedPrecision = x87Reduced;
            this.fexSilentLog = silentLog;
            this.fexSmcChecks = smc;
            this.fexVectorWidth = vectorWidth;
        }
    }
}
