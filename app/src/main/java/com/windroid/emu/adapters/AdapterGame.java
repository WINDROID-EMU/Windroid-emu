package com.windroid.emu.adapters;

import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.windroid.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.windroid.emu.activities.MainActivity.ACTION_RUN_WINE;
import static com.windroid.emu.activities.MainActivity.preferences;
import static com.windroid.emu.core.RatPackageManager.listRatPackagesId;
import static com.windroid.emu.fragments.DebugSettingsFragment.availableCPUs;
import static com.windroid.emu.fragments.ShortcutsFragment.MESA_DRIVER;
import static com.windroid.emu.fragments.ShortcutsFragment.getBox64Preset;
import static com.windroid.emu.fragments.ShortcutsFragment.getBox64Version;
import static com.windroid.emu.fragments.ShortcutsFragment.getCpuAffinity;
import static com.windroid.emu.fragments.ShortcutsFragment.getD3DXRenderer;
import static com.windroid.emu.fragments.ShortcutsFragment.getDXVKVersion;
import static com.windroid.emu.fragments.ShortcutsFragment.getDisplaySettings;
import static com.windroid.emu.fragments.ShortcutsFragment.getEnableAFME;
import static com.windroid.emu.fragments.ShortcutsFragment.getEnableDInput;
import static com.windroid.emu.fragments.ShortcutsFragment.getEnableXInput;
import static com.windroid.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.windroid.emu.fragments.ShortcutsFragment.getVKD3DVersion;
import static com.windroid.emu.fragments.ShortcutsFragment.getVulkanDriver;
import static com.windroid.emu.fragments.ShortcutsFragment.getVulkanDriverType;
import static com.windroid.emu.fragments.ShortcutsFragment.getVramLimit;
import static com.windroid.emu.fragments.ShortcutsFragment.getWineD3DVersion;
import static com.windroid.emu.fragments.ShortcutsFragment.getWineESync;
import static com.windroid.emu.fragments.ShortcutsFragment.getWineServices;
import static com.windroid.emu.fragments.ShortcutsFragment.getWineVirtualDesktop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.windroid.emu.R;
import com.windroid.emu.activities.EmulationActivity;
import com.windroid.emu.fragments.EditGamePreferencesFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdapterGame extends RecyclerView.Adapter<AdapterGame.ViewHolder> {
    private final FragmentActivity activity;
    private final float size;
    private final ArrayList<GameItem> gameList;
    private final ArrayList<GameItem> filteredList = new ArrayList<>();

    public AdapterGame(ArrayList<GameItem> gameList, float size, FragmentActivity activity) {
        this.activity = activity;
        this.size = size;
        this.gameList = gameList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_game_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameItem item = filteredList.get(position);

        holder.titleGame.setText(item.name);

        File imgFile = new File(item.iconPath);
        if (imgFile.exists() && imgFile.length() > 0) {
            Bitmap imgBitmap = BitmapFactory.decodeFile(item.iconPath);
            if (imgBitmap != null) {
                if (new File(item.exePath).exists()) {
                    holder.gameImage.setImageBitmap(imgBitmap);
                } else {
                    holder.gameImage.setImageBitmap(toGrayScale(imgBitmap));
                }
            }
        } else if (item.iconPath.isEmpty()) {
            holder.gameImage.setImageBitmap(
                    BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_icon));
        } else {
            holder.gameImage.setImageResource(R.drawable.unknown_exe);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    private Bitmap toGrayScale(Bitmap bitmap) {
        Bitmap grayScaleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0F);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);

        Canvas canvas = new Canvas(grayScaleBitmap);
        canvas.drawBitmap(bitmap, 0F, 0F, paint);

        return grayScaleBitmap;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(String name) {
        filteredList.clear();

        for (GameItem item : gameList) {
            if (item.name != null && item.name.toLowerCase().contains(name.toLowerCase())) {
                filteredList.add(item);
            }
        }

        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView titleGame = itemView.findViewById(R.id.title_game_model);
        public ImageView gameImage = itemView.findViewById(R.id.img_game);

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            GameItem gameItem = gameList.get(getAdapterPosition());

            selectedGameName = gameItem.name;

            File exeFile = new File(gameItem.exePath);

            String exePath = gameItem.exePath;
            String exeArguments = gameItem.exeArguments;

            if (!exeFile.exists()) {
                if (gameItem.exePath.equals(activity.getString(R.string.desktop_mode_init))) {
                    exePath = "";
                    exeArguments = "";
                } else {
                    activity.runOnUiThread(() -> Toast
                            .makeText(activity, R.string.executable_file_not_found, Toast.LENGTH_SHORT).show());
                    new EditGamePreferencesFragment(EditGamePreferencesFragment.EDIT_GAME_PREFERENCES, null)
                            .show(activity.getSupportFragmentManager(), "");
                    return;
                }
            }

            String driverName = getVulkanDriver(selectedGameName);
            int driverType = getVulkanDriverType(selectedGameName);
            String box64Version = getBox64Version(selectedGameName);

            Intent runActivityIntent = new Intent(activity, EmulationActivity.class);
            Intent runWineIntent = new Intent(ACTION_RUN_WINE);

            runWineIntent.putExtra("exePath", exePath);
            runWineIntent.putExtra("exeArguments", exeArguments);
            runWineIntent.putExtra("driverName", driverName);
            runWineIntent.putExtra("driverType", driverType);
            runWineIntent.putExtra("box64Version", box64Version);
            runWineIntent.putExtra("box64Preset", getBox64Preset(selectedGameName));
            runWineIntent.putExtra("displayResolution", getDisplaySettings(selectedGameName).get(1));
            runWineIntent.putExtra("virtualControllerPreset", getSelectedVirtualControllerPreset(selectedGameName));
            runWineIntent.putExtra("d3dxRenderer", getD3DXRenderer(selectedGameName));
            runWineIntent.putExtra("wineD3D", getWineD3DVersion(selectedGameName));
            runWineIntent.putExtra("dxvk", getDXVKVersion(selectedGameName));
            runWineIntent.putExtra("vkd3d", getVKD3DVersion(selectedGameName));
            runWineIntent.putExtra("esync", getWineESync(selectedGameName));
            runWineIntent.putExtra("services", getWineServices(selectedGameName));
            runWineIntent.putExtra("virtualDesktop", getWineVirtualDesktop(selectedGameName));
            runWineIntent.putExtra("enableXInput", getEnableXInput(selectedGameName));
            runWineIntent.putExtra("enableDInput", getEnableDInput(selectedGameName));
            runWineIntent.putExtra("cpuAffinity", getCpuAffinity(selectedGameName));
            runWineIntent.putExtra("vramLimit", getVramLimit(selectedGameName));
            runWineIntent.putExtra("enableAFME", getEnableAFME(selectedGameName));

            runWineIntent.setPackage("com.windroid.emu");
            activity.sendBroadcast(runWineIntent);
            activity.startActivity(runActivityIntent);
        }

        @Override
        public boolean onLongClick(View view) {
            selectedGameName = gameList.get(getAdapterPosition()).name;
            return false;
        }
    }

    public static String selectedGameName = "";

    public static class GameItem {
        public String name;
        public String exePath;
        public String exeArguments;
        public String iconPath;
        public String emulator;
        public String box64Version;
        public String box64Preset;
        public String fexCoreVersion;
        public String fexCorePreset;
        public ArrayList<String> controllersPreset;
        public ArrayList<Boolean> controllersEnableXInput;
        public ArrayList<Boolean> controllersXInputSwapAnalogs;
        public String virtualControllerPreset;
        public boolean virtualControllerEnableXInput;
        public String displayMode;
        public String displayResolution;
        public String vulkanDriver;
        public int vulkanDriverType;
        public String d3dxRenderer;
        public String dxvkVersion;
        public String wineD3DVersion;
        public String vkd3dVersion;
        public boolean wineESync;
        public boolean wineServices;
        public String cpuAffinityCores;
        public boolean wineVirtualDesktop;
        public boolean enableXInput;
        public boolean enableDInput;
        public String vramLimit;
        public boolean enableAFME;
        public ArrayList<AdapterEnvVar.EnvVar> envVars;

        public GameItem(String name, String exePath, String exeArguments, String iconPath) {
            this.name = name;
            this.exePath = exePath;
            this.exeArguments = exeArguments;
            this.iconPath = iconPath;
            this.emulator = "Global";
            this.box64Version = "Global";
            this.box64Preset = "default";
            this.fexCoreVersion = "Global";
            this.fexCorePreset = "Balanced";
            this.controllersPreset = new ArrayList<>(Arrays.asList("default", "default", "default", "default"));
            this.controllersEnableXInput = new ArrayList<>(Arrays.asList(true, true, true, true));
            this.controllersXInputSwapAnalogs = new ArrayList<>(Arrays.asList(false, false, false, false));
            this.virtualControllerPreset = "default";
            this.virtualControllerEnableXInput = true;
            this.displayMode = "16:9";
            this.displayResolution = "1280x720";
            this.vulkanDriver = "Global";
            this.vulkanDriverType = MESA_DRIVER;
            this.d3dxRenderer = "DXVK";
            List<String> dxvkList = listRatPackagesId("DXVK");
            this.dxvkVersion = dxvkList.isEmpty() ? "" : dxvkList.get(0);

            List<String> wineD3DList = listRatPackagesId("WineD3D");
            this.wineD3DVersion = wineD3DList.isEmpty() ? "" : wineD3DList.get(0);

            List<String> vkd3dList = listRatPackagesId("VKD3D");
            this.vkd3dVersion = vkd3dList.isEmpty() ? "" : vkd3dList.get(0);
            this.wineESync = true;
            this.wineServices = false;
            this.cpuAffinityCores = String.join(",", availableCPUs);
            this.wineVirtualDesktop = false;
            this.enableXInput = true;
            this.enableDInput = true;
            this.vramLimit = "Auto";
            this.enableAFME = false;
            this.envVars = new ArrayList<>();
        }
    }
}