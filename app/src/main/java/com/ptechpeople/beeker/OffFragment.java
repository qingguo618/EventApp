package com.ptechpeople.beeker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OffFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		
		View v = inflater.inflate(
	    		  R.layout.fragment_off, container, false);
		
//		txt_test.setText("ActivityManager: Starting: Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.example.eventapp/.MainActivity }");
//	    ViewTreeObserver vto = txt_test.getViewTreeObserver();
//	    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//
//	        @Override
//	        public void onGlobalLayout() {
//	            ViewTreeObserver obs = txt_test.getViewTreeObserver();
//	            obs.removeGlobalOnLayoutListener(this);
//	            if(txt_test.getLineCount() > 3){
//	                Log.d("","Line["+txt_test.getLineCount()+"]"+txt_test.getText());
//	                int lineEndIndex = txt_test.getLayout().getLineEnd(2);
//	                String text = txt_test.getText().subSequence(0, lineEndIndex-3)+"...";
//	                txt_test.setText(text);
//	                Log.d("","NewText:"+text);
//	            }
//
//	        }
//	    });
	    
		return v;
	}
}
