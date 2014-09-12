package com.manas.asyncimageloader.sample;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.manas.asyncimageloader.AsyncImageLoader;


/**
 * Created by Manas on 9/9/2014.
 */
public class ViewPagerImageFragment extends Fragment {

    private final String TAG = "ViewPagerImageFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewpager_image, container, false);
        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        String url = getArguments().getString("url");
        AsyncImageLoader.getInstance().loadImage(url, new AsyncImageLoader.LoadCallback() {
            @Override
            public void onCompleted(Bitmap bitmap, Exception e) {
                if (bitmap != null && e == null) {
                    imageView.setImageBitmap(bitmap);
                    progressBar.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        });
        return view;
    }

    public static ViewPagerImageFragment newInstance(String url) {
        ViewPagerImageFragment fragment = new ViewPagerImageFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        return fragment;
    }
}
