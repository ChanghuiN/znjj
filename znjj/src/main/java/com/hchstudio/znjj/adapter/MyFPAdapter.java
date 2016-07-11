package com.hchstudio.znjj.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.hchstudio.znjj.activity.MyFragment;

import java.util.ArrayList;

/**
 * Created by hch on 16-5-13.
 */
public class MyFPAdapter extends FragmentPagerAdapter {
    private static final String TAG = "MyFPAdapter";

    private ArrayList<Fragment> fragments;

    public MyFPAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        Log.i(TAG,"position----" + position + "---");
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
