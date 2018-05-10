package com.oo_h_oo.musiccollection.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import com.oo_h_oo.musiccollection.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by HeHang on 2018/3/31.
 */

public class ImageLoader {
    /**
     * Network time out
     */
    private static final int TIME_OUT = 30000;
    /**
     * Default picture resource
     */
    private static final int DEFAULT_BG = R.mipmap.default_background_music;

    /**
     * Thread pool number
     */
    private static final int THREAD_NUM = 5;

    /**
     * Memory image cache
     */
    MemoryCache memoryCache = new MemoryCache();

    /**
     * File image cache
     */
    FileCache fileCache;

    /**
     * Judge image view if it is reuse
     */
    private Map<ImageView, String> imageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());

    /**
     * Thread pool
     */
    ExecutorService executorService;

    /**
     * Handler to display images in UI thread
     */
    Handler handler = new Handler();

    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(THREAD_NUM);
    }

    public void disPlayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            // Display image from Memory cache
            imageView.setImageBitmap(bitmap);
        } else {
            // Display image from File cache or Network
            queuePhoto(url, imageView);
        }
    }
//    public Bitmap getBitmap(String path, BitmapFactory.Options options) throws IOException {
//        Bitmap bitmap = memoryCache.get(path);
//        if (bitmap != null) {
//            // Display image from Memory cache
//            return bitmap;
//        } else {
//            URL url = new URL(path);
//            // Display image from File cache or Network
//            return BitmapFactory.decodeStream(url.openStream(), null,options);
//        }
//    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(photoToLoad));
    }

    private Bitmap getBitmap(String url) {
        File f = fileCache.getFile(url);

        // From File cache
        Bitmap bmp = decodeFile(f);
        if (bmp != null) {
            return bmp;
        }

        // From Network
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.setConnectTimeout(TIME_OUT);
            conn.setReadTimeout(TIME_OUT);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            copyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex) {
            if (ex instanceof OutOfMemoryError) {
                clearCache();
            }
            return null;
        }
    }

    public String getBitmapFilePath(String url) {
        File f = fileCache.getFile(url);

        if (f.exists()) {
            return f.getPath();
        }

        // From Network
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            copyStream(is, os);
            os.close();
            conn.disconnect();
            return f.getPath();
        } catch (Throwable ex) {
            if (ex instanceof OutOfMemoryError) {
                clearCache();
            }
            return "";
        }
    }

    public static String getImageFilePathNet(Context context,String url) {
        File f = new FileCache(context).getFile(url);
        // From Network
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            copyStream(is, os);
            os.close();
            conn.disconnect();
            return f.getPath();
        } catch (Throwable ex) {
            return "";
        }
    }

    private static void copyStream(InputStream is, OutputStream os) {
        int buffer_size = 1024;

        try {
            byte[] bytes = new byte[buffer_size];
            while (true) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }

        } catch (Exception e) {

        }
    }

    private Bitmap decodeFile(File f) {
        try {
            // TODO:Compress image size
            FileInputStream fileInputStream = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
            return bitmap;

        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

    /**
     * Task for the queue
     *
     * @author zhengyi.wzy
     *
     */
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }
    }

    /**
     * Asynchronous to load picture
     *
     * @author zhengyi.wzy
     *
     */
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        public PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        private boolean imageViewReused(PhotoToLoad photoToLoad) {
            String tag = imageViews.get(photoToLoad.imageView);
            return tag == null || !tag.equals(photoToLoad.url);

        }

        @Override
        public void run() {
            // Abort current thread if Image View reused
            if (imageViewReused(photoToLoad)) {
                return;
            }

            Bitmap bitmap = getBitmap(photoToLoad.url);

            // Update Memory
            memoryCache.put(photoToLoad.url, bitmap);

            if (imageViewReused(photoToLoad)) {
                return;
            }

            // Don't change UI in children thread
            BitmapDisplayer bd = new BitmapDisplayer(bitmap, photoToLoad);
            handler.post(bd);
        }

        class BitmapDisplayer implements Runnable {
            Bitmap bitmap;
            PhotoToLoad photoToLoad;

            public BitmapDisplayer(Bitmap bitmap, PhotoToLoad photoToLoad) {
                this.bitmap = bitmap;
                this.photoToLoad = photoToLoad;
            }

            @Override
            public void run() {
                if (imageViewReused(photoToLoad)) {
                    return;
                }

                if (bitmap != null) {
                    photoToLoad.imageView.setImageBitmap(bitmap);
                } else {
                    photoToLoad.imageView.setImageResource(DEFAULT_BG);
                }
            }

        }
    }
}
