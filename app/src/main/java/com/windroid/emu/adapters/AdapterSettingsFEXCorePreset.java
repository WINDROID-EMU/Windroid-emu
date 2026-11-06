package com.windroid.emu.adapters;

import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_MULTI_BLOCK;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_SILENT_LOG;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_SMC_CHECKS;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_TSO_ENABLED;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_VECTOR_WIDTH;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_X87_REDUCED_PRECISION;
import static com.windroid.emu.activities.GeneralSettingsActivity.SPINNER;
import static com.windroid.emu.activities.GeneralSettingsActivity.SWITCH;
import static com.windroid.emu.adapters.AdapterPreset.clickedPresetName;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.getFEXMultiBlock;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.getFEXSilentLog;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.getFEXSmcChecks;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.getFEXTsoEnabled;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.getFEXVectorWidth;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.getFEXX87ReducedPrecision;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.putFEXMultiBlock;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.putFEXSilentLog;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.putFEXSmcChecks;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.putFEXTsoEnabled;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.putFEXVectorWidth;
import static com.windroid.emu.fragments.FEXCorePresetManagerFragment.putFEXX87ReducedPrecision;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.windroid.emu.R;

import java.util.ArrayList;
import java.util.Arrays;

public class AdapterSettingsFEXCorePreset extends RecyclerView.Adapter<AdapterSettingsFEXCorePreset.ViewHolder> {
    private final ArrayList<FEXCoreListSpinner> presetsList;
    private final FragmentActivity activity;

    public AdapterSettingsFEXCorePreset(ArrayList<FEXCoreListSpinner> presetsList, FragmentActivity activity) {
        this.presetsList = presetsList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_settings_preferences_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FEXCoreListSpinner item = presetsList.get(position);

        holder.settingsName.setText(item.titleSettings);
        holder.settingsDescription.setText(item.descriptionSettings);

        if (activity.getString(item.descriptionSettings).trim().isEmpty()) {
            holder.settingsDescription.setVisibility(View.GONE);
        } else {
            holder.settingsDescription.setVisibility(View.VISIBLE);
        }

        switch (item.type) {
            case SWITCH -> {
                holder.spinnerOptions.setVisibility(View.GONE);
                holder.settingsSwitch.setVisibility(View.VISIBLE);
                holder.seekBar.setVisibility(View.GONE);
                holder.seekBarValue.setVisibility(View.GONE);

                boolean value = switch (item.key) {
                    case FEX_TSO_ENABLED -> getFEXTsoEnabled(clickedPresetName);
                    case FEX_MULTI_BLOCK -> getFEXMultiBlock(clickedPresetName);
                    case FEX_X87_REDUCED_PRECISION -> getFEXX87ReducedPrecision(clickedPresetName);
                    case FEX_SILENT_LOG -> getFEXSilentLog(clickedPresetName);
                    default -> false;
                };

                holder.settingsSwitch.setChecked(value);
                holder.settingsSwitch.setOnClickListener((v) -> {
                    switch (item.key) {
                        case FEX_TSO_ENABLED -> putFEXTsoEnabled(clickedPresetName, holder.settingsSwitch.isChecked());
                        case FEX_MULTI_BLOCK -> putFEXMultiBlock(clickedPresetName, holder.settingsSwitch.isChecked());
                        case FEX_X87_REDUCED_PRECISION -> putFEXX87ReducedPrecision(clickedPresetName, holder.settingsSwitch.isChecked());
                        case FEX_SILENT_LOG -> putFEXSilentLog(clickedPresetName, holder.settingsSwitch.isChecked());
                    }
                });
            }
            case SPINNER -> {
                holder.spinnerOptions.setVisibility(View.VISIBLE);
                holder.settingsSwitch.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.GONE);
                holder.seekBarValue.setVisibility(View.GONE);

                holder.spinnerOptions.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, item.spinnerOptions));

                String selectedItem = switch (item.key) {
                    case FEX_SMC_CHECKS -> getFEXSmcChecks(clickedPresetName);
                    case FEX_VECTOR_WIDTH -> getFEXVectorWidth(clickedPresetName);
                    default -> "";
                };

                int selection = Arrays.asList(item.spinnerOptions).indexOf(selectedItem);
                if (selection < 0) selection = 0;
                holder.spinnerOptions.setSelection(selection);

                holder.spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String chosen = item.spinnerOptions[i];
                        switch (item.key) {
                            case FEX_SMC_CHECKS -> putFEXSmcChecks(clickedPresetName, chosen);
                            case FEX_VECTOR_WIDTH -> putFEXVectorWidth(clickedPresetName, chosen);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {}
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return presetsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView settingsName;
        public TextView settingsDescription;
        public SwitchCompat settingsSwitch;
        public Spinner spinnerOptions;
        public SeekBar seekBar;
        public TextView seekBarValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            settingsName = itemView.findViewById(R.id.title_preferences_model);
            settingsDescription = itemView.findViewById(R.id.description_preferences_model);
            settingsSwitch = itemView.findViewById(R.id.optionSwitch);
            spinnerOptions = itemView.findViewById(R.id.keyBindSpinner);
            seekBar = itemView.findViewById(R.id.seekBar);
            seekBarValue = itemView.findViewById(R.id.seekBarValue);
        }
    }

    public static class FEXCoreListSpinner {
        public int titleSettings;
        public int descriptionSettings;
        public String[] spinnerOptions;
        public int type;
        public String key;

        public FEXCoreListSpinner(int titleSettings, int descriptionSettings, String[] spinnerOptions, int type, String key) {
            this.titleSettings = titleSettings;
            this.descriptionSettings = descriptionSettings;
            this.spinnerOptions = spinnerOptions;
            this.type = type;
            this.key = key;
        }
    }
}

