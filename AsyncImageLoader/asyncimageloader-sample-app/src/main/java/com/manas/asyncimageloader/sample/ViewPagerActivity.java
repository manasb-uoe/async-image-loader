package com.manas.asyncimageloader.sample;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

/**
 * Created by Manas on 9/7/2014.
 */
public class ViewPagerActivity extends Activity {

    String[] imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
        imageUrls = getIntent().getStringArrayExtra("imageUrls");
        int pos = getIntent().getIntExtra("pos", 0);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new CustomPagerAdapter(getFragmentManager()));
        pager.setCurrentItem(pos);
    }

    private class CustomPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> fragments;

        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<Fragment>();
            for (String url : imageUrls) {
                fragments.add(ViewPagerImageFragment.newInstance(url));
            }
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
