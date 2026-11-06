package com.windroid.emu.utils;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static void copyRecursively(File source, File target) {
        if (!source.exists()) {
            Log.w(TAG, "Source directory does not exist: " + source.getPath());
            return;
        }

        Path sourcePath = source.toPath();
        Path targetPath = target.toPath();

        try (Stream<Path> paths = Files.walk(sourcePath)) {
            paths.forEach(path -> {
                try {
                    Path relativePath = sourcePath.relativize(path);
                    Path targetResolved = targetPath.resolve(relativePath);

                    if (Files.isDirectory(path)) {
                        if (!Files.exists(targetResolved)) {
                            Files.createDirectories(targetResolved);
                        }
                    } else {
                        Files.copy(path, targetResolved, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to copy file: " + path, e);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Failed to walk source directory: " + sourcePath, e);
        }
    }

    public static void deleteDirectoryContents(File directory) {
        if (directory == null || !directory.exists()) return;
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectoryRecursively(file.toPath());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to delete contents of directory: " + directory, e);
        }
    }

    public static void deleteDirectoryRecursively(Path directory) {
        if (!Files.exists(directory)) return;

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to delete file: " + path, e);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Failed to walk directory for deletion: " + directory, e);
        }
    }

    public static String getFileExtension(File file) {
        if (file == null) return "";

        String name = file.getName();
        int lastDot = name.lastIndexOf('.');

        if (lastDot == -1 || lastDot == 0 || lastDot == name.length() - 1) {
            return "";
        }

        return name.substring(lastDot + 1);
    }
}
