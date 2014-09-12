package com.manas.asyncimageloader;

import android.content.Context;

import java.io.File;

/**
 * Created by Manas on 9/6/2014.
 */
public class AsyncImageLoaderConfig {

    Context context;
    int maxMemory;
    File cacheDir;
    int threadPoolSize;
    int placeHolderResId;
    boolean shouldFadeIn;
    int fadeInDuration;

    private AsyncImageLoaderConfig(Context context, int maxMemory, File cacheDir, int threadPoolSize, int placeHolderResId, boolean shouldFadeIn, int fadeInDuration) {
        this.context = context;
        this.maxMemory = maxMemory;
        this.cacheDir = cacheDir;
        this.threadPoolSize = threadPoolSize;
        this.placeHolderResId = placeHolderResId;
        this.shouldFadeIn = shouldFadeIn;
        this.fadeInDuration = fadeInDuration;
    }

    public static class Builder {

        private Context context;
        private int maxMemory;
        private File cacheDir;
        private int threadPoolSize;
        private int placeHolderResId;
        private boolean shouldFadeIn;
        private int fadeInDuration;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMemoryCacheSize(int maxMemory) {
            this.maxMemory = maxMemory;
            return this;
        }

        public Builder setDiskCacheLocation(File cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        public Builder setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
            return this;
        }

        public Builder setPlaceHolderImage(int placeHolderResId) {
            this.placeHolderResId = placeHolderResId;
            return this;
        }

        public Builder setShouldFadeIn(boolean shouldFadeIn) {
            this.shouldFadeIn = shouldFadeIn;
            return this;
        }

        public Builder setFadeInDuration(int fadeInDuration) {
            this.fadeInDuration = fadeInDuration;
            return this;
        }

        public AsyncImageLoaderConfig build() {
            return new AsyncImageLoaderConfig(context, maxMemory, cacheDir, threadPoolSize, placeHolderResId, shouldFadeIn, fadeInDuration);
        }
    }
}
