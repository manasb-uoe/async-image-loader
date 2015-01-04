package com.manas.asyncimageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Manas on 9/5/2014.
 */
public class AsyncImageLoader {
    private static final String TAG = "AsyncImageLoader";
    private Resources res;
    private LruCache<String, Bitmap> memoryCache;
    private Map<ImageView, String> imageViews;
    private FileCache fileCache;
    private ExecutorService executorService;
    private static AsyncImageLoader instance;
    private Handler handler;
    private Drawable placeHolder;
    private boolean shouldFadeIn;
    private int fadeInDuration;


    private AsyncImageLoader(AsyncImageLoaderConfig config) {
        res = config.context.getResources();
        int maxMemory = (config.maxMemory > 0) ? config.maxMemory  : (int) (Runtime.getRuntime().maxMemory() / 8);
        File cacheDir = (config.cacheDir != null) ? config.cacheDir : new File(config.context.getExternalCacheDir().getAbsolutePath() + "/images");
        int threadPoolSize = (config.threadPoolSize > 0) ? config.threadPoolSize : Runtime.getRuntime().availableProcessors()+1;
        placeHolder = (config.placeHolderResId != 0) ? res.getDrawable(config.placeHolderResId) : new ColorDrawable(res.getColor(android.R.color.transparent));
        shouldFadeIn = config.shouldFadeIn;
        fadeInDuration = (config.fadeInDuration > 0) ? config.fadeInDuration : 150;
        executorService = Executors.newFixedThreadPool(threadPoolSize);
        memoryCache = new LruCache<String, Bitmap>(maxMemory);
        fileCache = new FileCache(cacheDir);
        imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
        handler = new Handler();
    }

    /**
     * Initializes AsyncImageLoader using the provided AsyncImageLoaderConfig.
     * @param config
     */
    public static void initalize(AsyncImageLoaderConfig config) {
        if (config != null) {
            if (instance != null) {
                throw new IllegalStateException("AsyncImageLoader can only be initialized once");
            }
            else {
                instance = new AsyncImageLoader(config);
            }
        }
        else {
            throw new IllegalArgumentException("The supplied AsyncImageLoaderConfig instance cannot be null");
        }
    }

    /**
     * Returns an instance of AsyncImageLoader. Throws an IllegalStateException if AsyncImageLoader has not been initialized yet (by calling {@link #initalize(AsyncImageLoaderConfig)}).
     * @return AsyncImageLoader instance
     */
    public static AsyncImageLoader getInstance() {
        if (instance != null) {
            return instance;
        }
        else {
            throw new IllegalStateException("AsyncImageLoader has not been initialized yet");
        }
    }

    /**
     * Downloads the requested image asynchronously(if not already present in cache) and sets it on the provided ImageView.
     * @param imageView  ImageView on which bitmap needs to be set
     * @param url URL of the image that needs to be downloaded
     * @param isResource boolean indicating whether the requested url is a resource id or an actual url
     */
    public void displayImage(ImageView imageView, String url, boolean isResource) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        else {
            imageView.setImageDrawable(placeHolder);
            queueImage(url, imageView, isResource, null, null, null);
        }
    }

    /**
     * Downloads the requested image asynchronously(if not already present in cache) and sets it on the provided ImageView.
     * @param imageView  ImageView on which bitmap needs to be set
     * @param url URL of the image that needs to be downloaded
     * @param isResource boolean indicating whether the requested url is a resource id or an actual url
     * @param dimensions Target dimensions of the image (used for down-sampling the bitmap before loading into memory)
     * @param progressCallback A ProgressCallback is used to get progress while an image is being downloaded
     * @param loadCallback A LoadCallback is used to retrieve the requested bitmap.
     */
    public void displayImage(ImageView imageView, String url, boolean isResource, ImageDimensions dimensions, LoadCallback loadCallback, ProgressCallback progressCallback) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        else {
            imageView.setImageDrawable(placeHolder);
            queueImage(url, imageView, isResource, dimensions, loadCallback, progressCallback);
        }
    }

    /**
     * Downloads the requested image asynchronously(if not already present in cache) and returns it using the provided LoadCallback instance.
     * @param url URL of the image that needs to be downloaded
     * @param isResource boolean indicating whether the requested url is a resource id or an actual url
     * @param loadCallback A LoadCallback is used to retrieve the requested bitmap.
     */
    public void loadImage(String url, boolean isResource, LoadCallback loadCallback) {
        //first, check if image exists in memory cache
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            if (loadCallback != null) {
                loadCallback.onCompleted(bitmap, null);
            }
            return;
        }

        //now, get it from fileCache OR download it from internet
        queueImage(url, null, isResource, null, loadCallback, null);
    }

    /**
     * Downloads the requested image asynchronously(if not already present in cache) and returns it using the provided LoadCallback instance.
     * @param url URL of the image that needs to be downloaded
     * @param isResource boolean indicating whether the requested url is a resource id or an actual url
     * @param dimensions Target dimensions of the image (used for down-sampling the bitmap before loading into memory)
     * @param loadCallback A LoadCallback is used to retrieve the requested bitmap.
     * @param progressCallback A ProgressCallback is used to get progress while an image is being downloaded
     */
    public void loadImage(String url, boolean isResource, ImageDimensions dimensions, LoadCallback loadCallback, ProgressCallback progressCallback) {
        //first, check if image exists in memory cache
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            if (loadCallback != null) {
                loadCallback.onCompleted(bitmap, null);
            }
            return;
        }

        //now, get it from fileCache OR download it from internet
        queueImage(url, null, isResource, dimensions, loadCallback, progressCallback);
    }

    /**
     * A ProgressCallback is used to get progress while an image is being downloaded
     */
    public static interface ProgressCallback {
        /**Gets invoked whenever progress gets updated.
         * @param percentDone Progress as a percentage (i.e. b/w 0 and 100)
         */
        public void onProgressUpdate(int percentDone);
    }

    /**
     * A LoadCallback is used to retrieve the requested bitmap.
     *
     */
    public static interface LoadCallback {
        /**
         * Gets invoked when the image load operation has completed.
         * @param bitmap Bitmap that was requested. Null if an exception occurred.
         * @param e Any exception that occurred while performing the image load operation. Null of no exception occurred.
         */
        public void onCompleted(Bitmap bitmap, Exception e);
    }

    /**
     * Clears memory cache.
     */
    public void clearMemoryCache() {
        if (memoryCache != null) {
            memoryCache.evictAll();
        }
    }

    /**
     * Clears file cache.
     */
    public void clearFileCache() {
        if (fileCache != null) {
            fileCache.clearCache();
        }
    }

    private void queueImage(String url, ImageView imageView, boolean isResource, ImageDimensions dimensions, LoadCallback loadCallback, ProgressCallback progressCallback) {
        ImageToLoad imageToLoad = new ImageToLoad(url, imageView, isResource, dimensions, loadCallback, progressCallback);
        executorService.submit(new ImageLoader(imageToLoad));
    }

    private static class ImageToLoad {

        private String url;
        private ImageView imageView;
        private boolean isResource;
        private float reqWidth;
        private float reqHeight;
        private LoadCallback loadCallback;
        private ProgressCallback progressCallback;

        public ImageToLoad(String url, ImageView imageView, boolean isResource, ImageDimensions dimensions, LoadCallback loadCallback, ProgressCallback progressCallback) {
            this.url = url;
            this.imageView = imageView;
            this.isResource = isResource;
            if (dimensions != null) {
                this.reqWidth = dimensions.getReqWidth();
                this.reqHeight = dimensions.getReqHeight();
            }
            this.progressCallback = progressCallback;
            this.loadCallback = loadCallback;
        }
    }

    private class ImageLoader implements Runnable {

        ImageToLoad imageToLoad;

        public ImageLoader(ImageToLoad imageToLoad) {
            this.imageToLoad = imageToLoad;
        }

        @Override
        public void run() {
            Bitmap bitmap = null;
            if (imageToLoad.imageView != null) {
                if (isImageViewReused(imageToLoad)) {
                    return;
                }

                bitmap = getBitmap(imageToLoad);

                if (isImageViewReused(imageToLoad)) {
                    return;
                }
                //now that the imageView isn't reused, we are ready to set the bitmap to the imageView in the main thread
                handler.post(new ImageDisplayer(imageToLoad, bitmap));
            }
            else {
                bitmap = getBitmap(imageToLoad);
            }

            //add to memory cache
            memoryCache.put(imageToLoad.url, bitmap);
        }
    }

    private class ImageDisplayer implements Runnable{

        ImageToLoad imageToLoad;
        Bitmap bitmap;

        public ImageDisplayer(ImageToLoad imageToLoad, Bitmap bitmap) {
            this.imageToLoad = imageToLoad;
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            //one more final check
            if (isImageViewReused(imageToLoad)) {
                return;
            }

            //finally, its safe to set bitmap to imageView
            if (bitmap != null) {
                if (shouldFadeIn) {
                    TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{placeHolder, new BitmapDrawable(res, bitmap)});
                    imageToLoad.imageView.setImageDrawable(transitionDrawable);
                    transitionDrawable.startTransition(fadeInDuration);
                }
                else {
                    imageToLoad.imageView.setImageBitmap(bitmap);
                }
            }
            else {
                imageToLoad.imageView.setImageDrawable(placeHolder);
            }
        }
    }

    private boolean isImageViewReused(ImageToLoad imageToLoad) {
        String check = imageViews.get(imageToLoad.imageView);
        if (check == null || !check.equals(imageToLoad.url)) {
            return true;
        }
        else {
            return false;
        }
    }

    private Bitmap getBitmap(ImageToLoad imageToLoad) {
        Bitmap bitmap = null;

        if (imageToLoad.isResource) {
            bitmap = decodeImageResource(Integer.parseInt(imageToLoad.url), imageToLoad.reqWidth, imageToLoad.reqHeight);
            return bitmap;
        }
        else {
            //first, check if bitmap exists in fileCache
            File imageFile = fileCache.getFile(imageToLoad.url);
            if (imageFile.exists()) {
                bitmap = decodeImageFile(imageFile.getAbsolutePath(), imageToLoad.reqWidth, imageToLoad.reqHeight);
                if (bitmap != null) {
                    if (imageToLoad.loadCallback != null) {
                        handler.post(new RegisterCallback(imageToLoad.loadCallback, bitmap, null));
                    }
                    return bitmap;
                }
            }

            //now, attempt to download it from the internet
            HttpURLConnection hConn = null;
            InputStream is = null;
            OutputStream os = null;
            try {
                hConn = (HttpURLConnection) (new URL(imageToLoad.url).openConnection());
                hConn.setReadTimeout(30000);
                hConn.setConnectTimeout(30000);
                is = hConn.getInputStream();
                os = new FileOutputStream(imageFile);

                //add to file cache
                copyStream(is, os, hConn.getContentLength(), imageToLoad.progressCallback);

                bitmap = decodeImageFile(imageFile.getAbsolutePath(), imageToLoad.reqWidth, imageToLoad.reqHeight);

                if (imageToLoad.loadCallback != null) {
                    handler.post(new RegisterCallback(imageToLoad.loadCallback, bitmap, null));
                }
                return bitmap;
            }
            catch (IOException e) {
                if (imageToLoad.loadCallback != null) {
                    handler.post(new RegisterCallback(imageToLoad.loadCallback, null, e));
                }
                e.printStackTrace();
                return null;
            }
            finally {
                if (hConn != null) {
                    hConn.disconnect();
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //used to register callbacks on main thread
    private static class RegisterCallback implements Runnable {

        private LoadCallback loadCallback;
        private Bitmap bitmap;
        private Exception e;
        private ProgressCallback progressCallback;
        private int percentDone;

        public RegisterCallback(LoadCallback loadCallback, Bitmap bitmap, Exception e) {
            this.loadCallback = loadCallback;
            this.bitmap = bitmap;
            this.e = e;
        }

        public RegisterCallback(ProgressCallback progressCallback, int percentDone) {
            this.progressCallback = progressCallback;
            this.percentDone = percentDone;
        }

        @Override
        public void run() {
            if (loadCallback != null) {
                loadCallback.onCompleted(bitmap, e);
            }
            if (progressCallback != null) {
                progressCallback.onProgressUpdate(percentDone);
            }
        }
    }

    private static class FileCache {

        private File cacheDir;

        public FileCache(File cacheDir) {
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            this.cacheDir = cacheDir;
        }

        public File getFile(String url) {
            String fileName = generateFileName(url);
            return new File(cacheDir.getAbsolutePath() + "/" + fileName);
        }

        private static String generateFileName(String url) {
            return url.replaceAll("[^a-zA-Z0-9]", "");
        }

        public void clearCache() {
            for (File f : cacheDir.listFiles()) {
                f.delete();
            }
        }
    }

    //------------------------------------------------------Utility methods------------------------------------------------------

    private void copyStream(InputStream is, OutputStream os, int contentLength, AsyncImageLoader.ProgressCallback progressCallback) {
        final int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int read;
        float readSoFar = 0f;
        try {
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
                if (progressCallback != null) {
                    readSoFar += read;
                    handler.post(new RegisterCallback(progressCallback, (int) ((readSoFar / contentLength) * 100)));
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap decodeImageFile(String imageFilePath, float reqWidth, float reqHeight) {
        Bitmap bitmap = null;

        if (reqWidth != 0f && reqHeight != 0f) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFilePath, options);
            int scaleFactor = Math.max(Math.round(options.outWidth / reqWidth), Math.round(options.outHeight / reqHeight));
            options.inSampleSize = scaleFactor;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(imageFilePath, options);
        }
        else {
            bitmap = BitmapFactory.decodeFile(imageFilePath);
        }

        return bitmap;
    }

    private Bitmap decodeImageResource(int resId, float reqWidth, float reqHeight) {
        Bitmap bitmap = null;

        if (reqWidth != 0f && reqHeight != 0f) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);
            int bitmapWidth = options.outWidth;
            int bitmapHeight = options.outHeight;
            int scaleFactor = Math.max((int) Math.round(bitmapWidth/reqWidth), (int) Math.round(bitmapHeight/reqHeight));
            options.inSampleSize = scaleFactor;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeResource(res, resId, options);
        }
        else {
            bitmap = BitmapFactory.decodeResource(res, resId);
        }

        return bitmap;
    }
}
