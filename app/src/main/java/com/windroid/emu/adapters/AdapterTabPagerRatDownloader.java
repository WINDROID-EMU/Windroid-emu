package com.windroid.emu.adapters;

import static com.windroid.emu.adapters.AdapterRatPackage.DXVK;
import static com.windroid.emu.adapters.AdapterRatPackage.FEXCORE;
import static com.windroid.emu.adapters.AdapterRatPackage.VKD3D;
import static com.windroid.emu.adapters.AdapterRatPackage.VK_DRIVER;
import static com.windroid.emu.adapters.AdapterRatPackage.BOX64;
import static com.windroid.emu.adapters.AdapterRatPackage.WINE;
import static com.windroid.emu.adapters.AdapterRatPackage.WINED3D;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.windroid.emu.fragments.RatDownloaderFragment;

public class AdapterTabPagerRatDownloader extends FragmentStateAdapter {
    public AdapterTabPagerRatDownloader(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new RatDownloaderFragment("VulkanDriver", VK_DRIVER);
            case 1 -> new RatDownloaderFragment("Box64", BOX64);
            case 2 -> new RatDownloaderFragment("FEXCore", FEXCORE);
            case 3 -> new RatDownloaderFragment("Wine", WINE);
            case 4 -> new RatDownloaderFragment("DXVK", DXVK);
            case 5 -> new RatDownloaderFragment("WineD3D", WINED3D);
            case 6 -> new RatDownloaderFragment("VKD3D", VKD3D);
            default -> throw new IllegalArgumentException("Invalid Fragment for Position " + position);
        };
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    public String getItemName(int position) {
        return switch (position) {
            case 0 -> "Drivers";
            case 1 -> "Box64";
            case 2 -> "FEXCore";
            case 3 -> "Wine";
            case 4 -> "DXVK";
            case 5 -> "WineD3D";
            case 6 -> "VKD3D";
            default -> throw new IllegalArgumentException("Unexpected value: " + position);
        };
    }
}