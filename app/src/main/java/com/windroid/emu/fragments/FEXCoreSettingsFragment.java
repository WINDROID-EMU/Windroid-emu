package com.windroid.emu.fragments;

import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_MULTI_BLOCK;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_SILENT_LOG;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_SMC_CHECKS;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_TSO_ENABLED;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_VECTOR_WIDTH;
import static com.windroid.emu.activities.GeneralSettingsActivity.FEX_X87_REDUCED_PRECISION;
import static com.windroid.emu.activities.GeneralSettingsActivity.SPINNER;
import static com.windroid.emu.activities.GeneralSettingsActivity.SWITCH;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.windroid.emu.R;
import com.windroid.emu.adapters.AdapterSettingsFEXCorePreset;

import java.util.ArrayList;

public class FEXCoreSettingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private final ArrayList<AdapterSettingsFEXCorePreset.FEXCoreListSpinner> settingsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_model, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewSettingsModel);

        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.setSpanCount(1);
        }

        setAdapter();

        return rootView;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new AdapterSettingsFEXCorePreset(settingsList, requireActivity()));

        settingsList.clear();

        addToAdapter(R.string.fex_tso_enabled, R.string.fex_tso_enabled_desc, null, SWITCH, FEX_TSO_ENABLED);
        addToAdapter(R.string.fex_multi_block, R.string.fex_multi_block_desc, null, SWITCH, FEX_MULTI_BLOCK);
        addToAdapter(R.string.fex_x87_reduced_precision, R.string.fex_x87_reduced_precision_desc, null, SWITCH, FEX_X87_REDUCED_PRECISION);
        addToAdapter(R.string.fex_silent_log, R.string.fex_silent_log_desc, null, SWITCH, FEX_SILENT_LOG);
        addToAdapter(R.string.fex_smc_checks, R.string.fex_smc_checks_desc, new String[]{ "none", "app", "mtrack" }, SPINNER, FEX_SMC_CHECKS);
        addToAdapter(R.string.fex_vector_width, R.string.fex_vector_width_desc, new String[]{ "128", "256" }, SPINNER, FEX_VECTOR_WIDTH);
    }

    private void addToAdapter(int titleId, int descriptionId, String[] values, int type, String keyId) {
        settingsList.add(
                new AdapterSettingsFEXCorePreset.FEXCoreListSpinner(titleId, descriptionId, values, type, keyId)
        );
    }
}

