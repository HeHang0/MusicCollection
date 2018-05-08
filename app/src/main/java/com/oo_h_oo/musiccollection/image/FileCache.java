package com.oo_h_oo.musiccollection.image;

import android.content.Context;

import java.io.File;

public class FileCache {
    public static final String DIR_NAME = "MusicCollection/Cache/Image";
    private File cacheDir;

    public FileCache(Context context) {
        // Find the directory to save cached images
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(
                    android.os.Environment.getExternalStorageDirectory(),
                    DIR_NAME);
        } else {
            cacheDir = context.getCacheDir();
        }

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public File getFile(String url) {
        // Identify images by url's hash code
        String filename = String.valueOf(url.hashCode());
//filename = "11.jpg";        ///////////////////////////////////////////////////////////////强制修改
        return new File(cacheDir, filename);
    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }
}
