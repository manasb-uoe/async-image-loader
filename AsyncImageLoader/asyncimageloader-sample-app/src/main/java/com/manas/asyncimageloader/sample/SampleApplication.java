package com.manas.asyncimageloader.sample;

import android.app.Application;

import com.manas.asyncimageloader.AsyncImageLoader;
import com.manas.asyncimageloader.AsyncImageLoaderConfig;

import java.io.File;

/**
 * Created by Manas on 9/5/2014.
 */
public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //init AsyncImageLoader
        AsyncImageLoaderConfig config = new AsyncImageLoaderConfig.Builder(this)
                .setMemoryCacheSize((int) (Runtime.getRuntime().maxMemory() / 8))
                .setDiskCacheLocation(new File(getExternalCacheDir().getAbsolutePath() + "/images"))
                .setThreadPoolSize(Runtime.getRuntime().availableProcessors() + 1)
                .setPlaceHolderImage(R.drawable.placeholder)
                .setShouldFadeIn(true)
                .setFadeInDuration(150)
                .build();
        AsyncImageLoader.initalize(config);
    }

}
