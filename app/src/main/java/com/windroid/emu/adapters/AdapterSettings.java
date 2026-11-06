package com.windroid.emu.adapters;

import static com.windroid.emu.activities.GeneralSettingsActivity.ACTION_PREFERENCE_SELECT;
import static com.windroid.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.windroid.emu.fragments.CreatePresetFragment.FEXCORE_PRESET;
import static com.windroid.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.windroid.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.windroid.emu.fragments.CreatePresetFragment.WINE_PREFIX_PRESET;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.windroid.emu.R;
import com.windroid.emu.activities.ControllerCategoryActivity;
import com.windroid.emu.activities.ControllerTestActivity;
import com.windroid.emu.activities.GeneralSettingsActivity;
import com.windroid.emu.activities.MainActivity;
import com.windroid.emu.activities.PresetManagerActivity;
import com.windroid.emu.activities.RatDownloaderActivity;
import com.windroid.emu.activities.RatManagerActivity;
import com.windroid.emu.activities.WineSettingsActivity;
import com.windroid.emu.fragments.FloatingFileManagerFragment;

import java.util.ArrayList;
import java.util.List;

public class AdapterSettings extends RecyclerView.Adapter<AdapterSettings.ViewHolder> {
    private final ArrayList<SettingsList> settingsList;
    private final Context context;

    public AdapterSettings(ArrayList<SettingsList> settingsList, Context context) {
        this.settingsList = settingsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_settings_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingsList item = settingsList.get(position);

        holder.settingsName.setText(item.titleSettings);
        holder.settingsDescription.setText(item.descriptionSettings);
        holder.imageSettings.setImageResource(item.imageSettings);
    }

    @Override
    public int getItemCount() {
        return settingsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView settingsName = itemView.findViewById(R.id.title_preferences_model);
        private final TextView settingsDescription = itemView.findViewById(R.id.description_preferences_model);
        private final ImageView imageSettings = itemView.findViewById(R.id.set_img);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            SettingsList item = settingsList.get(getAdapterPosition());

            if (item.titleSettings.equals(context.getString(R.string.general_settings))) {
                Intent intent = new Intent(context, GeneralSettingsActivity.class);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.controller_settings_title))) {
                Intent intent = new Intent(context, ControllerCategoryActivity.class);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.controller_mapper_title))) {
                Intent intent = new Intent(context, PresetManagerActivity.class);
                intent.putExtra("presetType", CONTROLLER_PRESET);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.virtual_controller_mapper_title))) {
                Intent intent = new Intent(context, PresetManagerActivity.class);
                intent.putExtra("presetType", VIRTUAL_CONTROLLER_PRESET);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.box64_preset_manager_title))) {
                Intent intent = new Intent(context, PresetManagerActivity.class);
                intent.putExtra("presetType", BOX64_PRESET);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.fexcore_preset_manager_title))) {
                Intent intent = new Intent(context, PresetManagerActivity.class);
                intent.putExtra("presetType", FEXCORE_PRESET);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.wine_prefix_manager_title))) {
                Intent intent = new Intent(context, PresetManagerActivity.class);
                intent.putExtra("presetType", WINE_PREFIX_PRESET);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.wine_unified_settings_title))) {
                Intent intent = new Intent(context, WineSettingsActivity.class);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.rat_manager_title))) {
                Intent intent = new Intent(context, RatManagerActivity.class);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.rat_downloader_title))) {
                Intent intent = new Intent(context, RatDownloaderActivity.class);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.controller_view_title))) {
                Intent intent = new Intent(context, ControllerTestActivity.class);
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.scan_games_title))) {
                String startPath = MainActivity.wineDisksFolder != null
                        ? MainActivity.wineDisksFolder.getPath()
                        : "/storage/emulated/0";
                new FloatingFileManagerFragment(FloatingFileManagerFragment.OPERATION_SELECT_FOLDER, startPath)
                        .show(((FragmentActivity) context).getSupportFragmentManager(), "");
            } else if (item.titleSettings.equals(context.getString(R.string.driver_settings_container_title))) {
                Intent intent = new Intent(context, GeneralSettingsActivity.class);
                intent.putExtra("preference", context.getString(R.string.driver_settings_container_title));
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.package_manager_title))) {
                Intent intent = new Intent(context, GeneralSettingsActivity.class);
                intent.putExtra("preference", context.getString(R.string.package_manager_title));
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.debug_settings_title))) {
                Intent intent = new Intent(context, GeneralSettingsActivity.class);
                intent.putExtra("preference", context.getString(R.string.debug_settings_title));
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.sound_settings_title))) {
                Intent intent = new Intent(context, GeneralSettingsActivity.class);
                intent.putExtra("preference", context.getString(R.string.sound_settings_title));
                context.startActivity(intent);
            } else if (item.titleSettings.equals(context.getString(R.string.env_settings_title))) {
                Intent intent = new Intent(context, GeneralSettingsActivity.class);
                intent.putExtra("preference", context.getString(R.string.env_settings_title));
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(ACTION_PREFERENCE_SELECT);
                intent.putExtra("preference", item.titleSettings);
                intent.setPackage("com.windroid.emu");
                context.sendBroadcast(intent);
            }
        }
    }

    public static class SettingsList {
        public String titleSettings;
        public String descriptionSettings;
        public int imageSettings;

        public SettingsList(String titleSettings, String descriptionSettings, int imageSettings) {
            this.titleSettings = titleSettings;
            this.descriptionSettings = descriptionSettings;
            this.imageSettings = imageSettings;
        }
    }
}