package com.ptechpeople.beeker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;

import com.ptechpeople.beeker.model.DataModel;

import java.util.ArrayList;

@SuppressLint("SetJavaScriptEnabled")
public final class NewsPagerFragment extends Fragment {
    public static NewsPagerFragment newInstance(ArrayList<DataModel> content, int id) {
        NewsPagerFragment fragment = new NewsPagerFragment();

        fragment.drawID = id;
        fragment.content = content;
        
        return fragment;
    }

    private int drawID;
    private ArrayList content = new ArrayList<DataModel>();
    private int font_size = 0;
    ScrollView scrollview;
	WebView wv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	View view = inflater.inflate(R.layout.main_pagerview, null);
    	
    	wv = (WebView) view.findViewById(R.id.webView1);
    	wv.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
    	
    	DataModel item = (DataModel) content.get(drawID);
    	
    	final String mimeType = "text/html";
        final String encoding = "UTF-8";
        
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadDataWithBaseURL("", item.getContent(), mimeType, encoding, "");
        
        return view;
    }
    
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		wv.destroy();
	    wv = null;  
		super.onDestroy();
	}	
}
