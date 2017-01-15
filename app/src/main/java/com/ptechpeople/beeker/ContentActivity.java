package com.ptechpeople.beeker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptechpeople.beeker.model.DataModel;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ContentActivity extends FragmentActivity implements OnClickListener {
	
	WebView webView;
	ImageView img_back, picture;
	int index;
	TextView tvTitle, tvDistance;

//	private NewsPagerAdapter mAdapter;
//	private ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_content);
		
		Intent intent = getIntent();
		index = intent.getIntExtra("index", 0);
		
//		mAdapter = new NewsPagerAdapter(getSupportFragmentManager(), MainActivity._data);
//
//        mPager = (ViewPager) findViewById(R.id.pager);
//        mPager.setAdapter(mAdapter);
//        mPager.setCurrentItem(index);

		tvTitle = (TextView)findViewById(R.id.txt_title);
		tvDistance = (TextView)findViewById(R.id.txtDistance);
		webView = (WebView)findViewById(R.id.webView);

		img_back = (ImageView) findViewById(R.id.imgvBack);
		img_back.setOnClickListener(this);

		DataModel item = MainActivity._data.get(index);

		tvTitle.setText(Html.fromHtml(item.getTitle()));


		double dblDistance = item.getDistance();

		if(dblDistance < 0.0001){
			tvDistance.setVisibility(View.INVISIBLE);
		}else{
			tvDistance.setVisibility(View.VISIBLE);
			tvDistance.setText("Distance: " + dblDistance + " miles");
		}


		final ImageView picture = (ImageView)findViewById(R.id.picture);

		if (item.getFeatured_image() == 0) {
			UrlImageViewHelper.setUrlDrawable(picture, null, R.drawable.logo);
		} else {
			ImageLoader.getInstance().loadImage(item.getThumbnail_links(), new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String s, View view) {

				}

				@Override
				public void onLoadingFailed(String s, View view, FailReason failReason) {

				}

				@Override
				public void onLoadingComplete(String s, View view, Bitmap bitmap) {
					picture.setImageBitmap(bitmap);
				}

				@Override
				public void onLoadingCancelled(String s, View view) {

				}
			});
		}

//		String description = item.getExcerpt();
//		String temp_str = description.replace("<p>", "");
//		String new_str = temp_str.replace("</p>", "");

		WebSettings settings = webView.getSettings();
		settings.setDefaultTextEncodingName("utf-8");

		webView.loadData(item.getContent(), "text/html; charset=utf-8", "utf-8");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.imgvBack) {
			finish();
		}
	}
}
