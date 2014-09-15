#** Asynchronous Image Loader (Android) **#

This library can be used for asynchronous image loading, displaying and caching (memory and disk). It also offers customization options and is well documented.   
[Download the sample app here.](https://bitbucket.org/enthusiast94/asyncimageloader/downloads/asyncimageloader-sample_app.apk)

![Screenshot_2014-09-12-11-15-42.png](https://bitbucket.org/repo/LnzAa6/images/1539677683-Screenshot_2014-09-12-11-15-42.png) ![Screenshot_2014-09-12-11-16-42.png](https://bitbucket.org/repo/LnzAa6/images/1607332766-Screenshot_2014-09-12-11-16-42.png) ![Screenshot_2014-09-12-11-17-34.png](https://bitbucket.org/repo/LnzAa6/images/1681662346-Screenshot_2014-09-12-11-17-34.png)

#** Features **#

* Asynchronous multithreaded image loading 
* Various customization options using `AsyncImageLoaderConfig` (thread pool size, placeholder image, image downsampling, memory and file cache locations, etc.)
* Retrieve progress as a percentage while an image is being downlaoded 

#** How do I get set up? **#

1. [Download asyncimageloader-library.aar](https://bitbucket.org/enthusiast94/asyncimageloader/downloads/asyncimageloader-library.aar)
2. Put this .aar file in your Android project's `libs` subfolder.
3. In your `build.gradle` specify the following and click sync project with Gradle files:

		repositories {
		    flatDir {
		        dirs 'libs'
		    }
		}

		dependencies {
		    compile(name:'asyncimageloader-library', ext:'aar')
		}

4. Add the following permissions to your `Android Manifest`: 

	    <uses-permission android:name="android.permission.INTERNET"/>
	    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

5. Add the following in your Application or Activity's `onCreate` (**before the first use of AsyncImageLoader**).   
Note that these are the default values and you can change them in any way you like: 
		
		public class SampleApplication extends Application {
		    @Override
		    public void onCreate() {
		        super.onCreate();

		        //init AsyncImageLoader with default values
		        AsyncImageLoaderConfig config = new AsyncImageLoaderConfig.Builder(this)
		                .setMemoryCacheSize((int) (Runtime.getRuntime().maxMemory() / 8))
		                .setDiskCacheLocation(new File(getExternalCacheDir().getAbsolutePath() + "/images"))
		                .setThreadPoolSize(Runtime.getRuntime().availableProcessors() + 1)
		                .setPlaceHolderImage(0)
		                .setShouldFadeIn(true)
		                .setFadeInDuration(150)
		                .build();
		        AsyncImageLoader.initalize(config);
	   		}
		} 

#** How do I use it? **#

** 1. Asynchronously loading images in a `ListView` or `GridView`: **

* Use the simplest version of `displayImage` method as follows:

		AsyncImageLoader.getInstance().displayImage(imageView, url);

* If you want to provide target dimensions of the image so that a low resolution version of the image is loaded into memory, then you can specify dimensions using `ImageDimensions`: 
		
		AsyncImageLoader.getInstance().displayImage(imageView, url, new ImageDimensions(400f, 400f), null, null);

* If you want to get the progress of the image as it's being downloaded OR want to retrieve the loaded bitmap, then you can provide your own `ProgressCallback` and `LoadCallback` implementations instead of `null`:

		AsyncImageLoader.ProgressCallback progressCallback = new AsyncImageLoader.ProgressCallback() {
            @Override
            public void onProgressUpdate(int percentDone) {
                //do something with the download percentage 
            }
        };

        AsyncImageLoader.LoadCallback loadCallback = new AsyncImageLoader.LoadCallback() {
            @Override
            public void onCompleted(Bitmap bitmap, Exception e) {
                if (e == null) {
                    //do something with the returned bitmap
                }
            }
        };

		AsyncImageLoader.getInstance().displayImage(imageView, url, new ImageDimensions(400f, 400f), loadCallback, progressCallback);

** 2. Asynchronously downloading an image and retrieving the bitmap: **

* Use the simplest version of `loadImage` as follows: 
		
        AsyncImageLoader.LoadCallback loadCallback = new AsyncImageLoader.LoadCallback() {
            @Override
            public void onCompleted(Bitmap bitmap, Exception e) {
                if (e == null) {
                    //do something with the loaded bitmap
                }
            }
        };

        AsyncImageLoader.getInstance().loadImage(url, loadCallback);

* If you want to provide target dimensions of the image so that a low resolution version of the image is loaded into memory, then you can specify dimensions using `ImageDimensions`: 
		
		AsyncImageLoader.getInstance().displayImage(url, new ImageDimensions(400f, 400f), loadCallback, null);

* If you want to get the progress of the image as it's being downloaded, then you can provide your own implementation of `ProgressCallback` as follows: 

		AsyncImageLoader.ProgressCallback progressCallback = new AsyncImageLoader.ProgressCallback() {
            @Override
            public void onProgressUpdate(int percentDone) {
                //do something with the download percentage 
            }
        };

        AsyncImageLoader.getInstance().loadImage(url, new ImageDimensions(400f, 400f), loadCallback, progressCallback);        

** 3. Clear cache: **

* Clear memory cache:

            AsyncImageLoader.getInstance().clearMemoryCache();

* Clear file cache: 

            AsyncImageLoader.getInstance().clearFileCache();


#** Developed by **#

* Manas Bajaj - <manas.bajaj94@gmail.com>