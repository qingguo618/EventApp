package com.ptechpeople.beeker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ptechpeople.beeker.model.DataModel;

import java.util.ArrayList;

class NewsPagerAdapter extends FragmentPagerAdapter {
	
    private int mCount = 0;    
	ArrayList news_item = new ArrayList<DataModel>();
    
    public NewsPagerAdapter(FragmentManager fm, ArrayList item) {
        super(fm);
        
        this.news_item = item;
        this.mCount = item.size();
    }

    @Override
    public Fragment getItem(int position) {
        return NewsPagerFragment.newInstance(this.news_item, position);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
    
    
}