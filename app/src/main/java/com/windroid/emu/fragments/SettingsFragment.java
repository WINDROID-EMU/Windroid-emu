package com.windroid.emu.fragments;

import static com.windroid.emu.activities.MainActivity.deviceArch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.windroid.emu.R;
import com.windroid.emu.adapters.AdapterSettings;
import com.windroid.emu.adapters.AdapterSettings.SettingsList;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {
    private final ArrayList<SettingsList> settingsList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewSettings);

        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterSettings(settingsList, requireContext()));

        settingsList.clear();

        addToAdapter(R.string.controller_settings_title, R.string.controller_settings_desc, R.drawable.ic_joystick);
        addToAdapter(R.string.debug_settings_title, R.string.debug_settings_desc, R.drawable.ic_settings_outline);

        if (!deviceArch.equals("x86_64")) {
            addToAdapter(R.string.box64_preset_manager_title, R.string.box64_preset_manager_desc, R.drawable.ic_box64);
            addToAdapter(R.string.fexcore_preset_manager_title, R.string.fexcore_preset_manager_desc, R.drawable.ic_fexcore);
        }

        addToAdapter(R.string.wine_unified_settings_title, R.string.wine_unified_settings_desc, R.drawable.ic_wine);
        addToAdapter(R.string.driver_settings_container_title, R.string.driver_settings_container_desc, R.drawable.ic_gpu);
        addToAdapter(R.string.package_manager_title, R.string.package_manager_desc, R.drawable.ic_rat_package_grayscale);
        addToAdapter(R.string.sound_settings_title, R.string.sound_settings_desc, R.drawable.ic_sound);
        addToAdapter(R.string.env_settings_title, R.string.env_settings_desc, R.drawable.ic_globe);
        addToAdapter(R.string.scan_games_title, R.string.scan_games_desc, R.drawable.ic_folder);
    }

    private void addToAdapter(int titleId, int descriptionId, int iconId) {
        settingsList.add(
                new SettingsList(getString(titleId), getString(descriptionId), iconId));
    }
}
