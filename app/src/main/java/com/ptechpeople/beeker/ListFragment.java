package com.ptechpeople.beeker;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ptechpeople.beeker.model.DataModel;
import com.ptechpeople.beeker.until.APIManager;
import com.ptechpeople.beeker.until.GPSTracker;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListFragment extends Fragment {
	
	ListView listView;

	SearchAdapter adapter;
	MainActivity mParent;
	
	GPSTracker gps;
	Location current_location;
	String thumbnail_url = "";
	double current_latitude, current_lotitude;
	
	int refreshPage;
	
	Boolean isInternetPresent = false;
	int preLast = 0;
    boolean loading = false;

	@ Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
//		mParent = (MainActivity) getActivity();
		
		View v = inflater.inflate(
	    		  R.layout.fragment_list, container, false);
		
		gps = new GPSTracker(mParent);
		
		refreshPage = 1;
		
		listView = (ListView) v.findViewById(R.id.listView);
		adapter = new SearchAdapter(MainActivity._data, mParent);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				Intent intent = new Intent(mParent, ContentActivity.class);
				intent.putExtra("index", position);
				startActivity(intent);
			}
		});
        
        listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				final int lastItem = firstVisibleItem + visibleItemCount;
				if(lastItem == totalItemCount) {
					if(preLast != lastItem) {
						preLast = lastItem;
					}
					
					if(loading) {
						loading = false;
		        		refreshPage++;
						getRefreshData();
					}
				}
			}
		});
        
        
		if(gps.canGetLocation()){
            
        	current_latitude = gps.getLatitude();
        	current_lotitude = gps.getLongitude();
        	
        	getData();
        	
        } else{
            gps.showSettingsAlert();
        }
		
		return v;
	}
	
	public void getData() {

		isInternetPresent = isConnectingToInternet();
		if (isInternetPresent) {
			new LoadProductTask().execute();
		} else {
			Message("Please check internet connection.");
		}
	}
	
	public void getRefreshData() {

		isInternetPresent = isConnectingToInternet();
		if (isInternetPresent) {
			new LoadRefreshTask().execute();
		} else {
			Message("Please check internet connection.");
		}
	}
	
	class LoadProductTask extends AsyncTask<String, Integer, ArrayList<DataModel>> {
        private ProgressDialog progressDialog;
        
        protected void onPreExecute() {
        	progressDialog = ProgressDialog.show(mParent, "", "Loading...", true);
        }
        
        @Override
        protected void onPostExecute(ArrayList<DataModel> result) {
        	
        	loading = true;
        	
        	if(mParent._data.size() > 1) {
        		adapter.notifyDataSetChanged();
        	} else {
        		Intent intent = new Intent(mParent, ContentActivity.class);
				intent.putExtra("index", 0);
				startActivity(intent);
        	}        	
    		
            progressDialog.dismiss();
        }
 
        @Override
        protected ArrayList<DataModel> doInBackground(String... param) {
        	
        	mParent._data.clear();
        	
        	String api_url = "http://hoashopping.com/wp-json/wp/v2/posts?filter[post_status]=publish&filter[posts_per_page]=10&page=1&filter[category_name]=cat-one";
       		String result =  APIManager.getInstance().callGet(mParent, api_url, null, true);
        	
        	try {
        		JSONArray arr_data = new JSONArray(result);
        		
        		for(int i = 0 ; i < arr_data.length() ; i++) {
        			JSONObject item = arr_data.getJSONObject(i);
        			
        			setDataModel(item);
        		}
        		
        	} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        	sortMiles();

            return null;
        }
    }	
	
	class LoadRefreshTask extends AsyncTask<String, Integer, ArrayList<DataModel>> {
        private ProgressDialog progressDialog;
        
        protected void onPreExecute() {
        	progressDialog = ProgressDialog.show(mParent, "", "Loading...", true);
        }
        
        @Override
        protected void onPostExecute(ArrayList<DataModel> result) {
        	
        	adapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
 
        @Override
        protected ArrayList<DataModel> doInBackground(String... param) {
        	
        	String api_url = String.format("http://hoashopping.com/wp-json/wp/v2/posts?filter[post_status]=publish&filter[posts_per_page]=10&page=%d&filter[category_name]=cat-one", refreshPage);
       		String result =  APIManager.getInstance().callGet(mParent, api_url, null, true);
        	
        	try {
        		JSONArray arr_data = new JSONArray(result);
        		
        		for(int i = 0 ; i < arr_data.length() ; i++) {
        			JSONObject item = arr_data.getJSONObject(i);
        			
        			setDataModel(item);
        		}
        		
        		if(arr_data.length() > 0) {
        			loading = true;
        		}
        		
        	} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        	sortMiles();

            return null;
        }
    }	
	
	private void setDataModel(JSONObject item) {
		DataModel model = new DataModel();
		
		try {
			model.setId(item.getInt("id"));
			model.setDate_gmt(item.getString("date_gmt"));
			model.setDate_gmt(item.getJSONObject("guid").getString("rendered"));
			model.setModificated_gmt(item.getString("modified_gmt"));
			model.setSlug(item.getString("slug"));
			model.setType(item.getString("type"));
			model.setLink(item.getString("link"));
			model.setTitle(item.getJSONObject("title").getString("rendered"));
			
			model.setExcerpt(item.getJSONObject("excerpt").getString("rendered"));
			model.setAuthor(item.getInt("author"));
			model.setFeatured_image(item.getInt("featured_image"));
			model.setComment_status(item.getString("comment_status"));
			model.setPing_status(item.getString("ping_status"));
			model.setSticky(item.getBoolean("sticky"));
			model.setFormat(item.getString("format"));
			
			JSONObject links_obj = item.getJSONObject("_links");
			
			ArrayList<String> self_links = new ArrayList<String>();
			JSONArray self_arr = links_obj.getJSONArray("self");
			for(int j = 0 ; j < self_arr.length(); j++) {
				JSONObject href_obj = self_arr.getJSONObject(j);
				
				self_links.add(href_obj.getString("href"));
			}
			model.setSelf_links(self_links);
			
			ArrayList<String> collection_links = new ArrayList<String>();
			JSONArray collection_arr = links_obj.getJSONArray("collection");
			for(int j = 0 ; j < collection_arr.length(); j++) {
				JSONObject href_obj = collection_arr.getJSONObject(j);
				
				collection_links.add(href_obj.getString("href"));
			}
			model.setCollection_links(collection_links);
			
			ArrayList<String> author_links = new ArrayList<String>();
			JSONArray author_arr = links_obj.getJSONArray("author");
			for(int j = 0 ; j < author_arr.length(); j++) {
				JSONObject href_obj = author_arr.getJSONObject(j);
				
				author_links.add(href_obj.getString("href"));
			}
			model.setAuthor_links(author_links);
			
			ArrayList<String> replies_links = new ArrayList<String>();
			JSONArray replies_arr = links_obj.getJSONArray("replies");
			for(int j = 0 ; j < replies_arr.length(); j++) {
				JSONObject href_obj = replies_arr.getJSONObject(j);
				
				replies_links.add(href_obj.getString("href"));
			}
			model.setReplies_links(replies_links);
			
			ArrayList<String> version_history_links = new ArrayList<String>();
			JSONArray version_arr = links_obj.getJSONArray("version-history");
			for(int j = 0 ; j < version_arr.length(); j++) {
				JSONObject href_obj = version_arr.getJSONObject(j);
				
				version_history_links.add(href_obj.getString("href"));
			}
			model.setVersion_history_links(version_history_links);
			
			if(links_obj.has("http://api.w.org/featuredmedia")) {
				JSONArray media_arr = links_obj.getJSONArray("http://api.w.org/featuredmedia");
					JSONObject href_obj = media_arr.getJSONObject(0);
					String thumbnail_api = href_obj.getString("href");
				String media =  APIManager.getInstance().callGet(mParent, thumbnail_api, null, true);
				
				JSONObject _obj = new JSONObject(media);
				thumbnail_url = _obj.getString("source_url");
				
				model.setThumbnail_links(thumbnail_url);
			}
			
			String content_image = "";
			if(item.getInt("featured_image") != 0) {
				content_image = String.format("<img src='%s' width='400' height='300'>", thumbnail_url);
			}
			
			model.setContent(content_image + "\n" + item.getJSONObject("content").getString("rendered"));
			
			String location = item.getString("author_coordinates");
			if(location.contains(",")) {
				String[] arr_str = location.split(",");
				
				Location new_location = new Location(item.getString("author_address"));
				new_location.setLatitude(Double.parseDouble(arr_str[0]));
				new_location.setLongitude(Double.parseDouble(arr_str[1]));
				
				double distance = calculateDistance(current_latitude, current_lotitude, Double.parseDouble(arr_str[0]), Double.parseDouble(arr_str[1]));
				
				float miles = (float) (distance / 1000 / 1.609344);
				model.setMiles(miles);
			} else {
				model.setMiles(0);
			}        			
			
			mParent._data.add(model);		
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	class SearchAdapter extends ArrayAdapter<DataModel> {
		
		private Context context;
		
		public SearchAdapter(ArrayList<DataModel> itemList, Context ctx) {
			super(ctx, android.R.layout.simple_list_item_1, itemList);
			this.context = ctx;		
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			View v = convertView;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				v = inflater.inflate(R.layout.list_item, null);
			}
			
			DataModel model = mParent._data.get(position);
			
			ImageView picture = (ImageView) v.findViewById(R.id.picture);
			if(model.getFeatured_image() == 0) {
				UrlImageViewHelper.setUrlDrawable(picture, null, R.drawable.ic_launcher);
			} else {
				UrlImageViewHelper.setUrlDrawable(picture, model.getThumbnail_links());
			}
			
			TextView txt_title = (TextView) v.findViewById(R.id.txt_title);
			txt_title.setText(model.getTitle());
			
			final TextView txt_description = (TextView) v.findViewById(R.id.txt_description);
			
			txt_description.setText(model.getExcerpt());
		    ViewTreeObserver vto = txt_description.getViewTreeObserver();
		    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	
		        @Override
		        public void onGlobalLayout() {
		            ViewTreeObserver obs = txt_description.getViewTreeObserver();
		            obs.removeGlobalOnLayoutListener(this);
		            if(txt_description.getLineCount() > 3){
		                int lineEndIndex = txt_description.getLayout().getLineEnd(2);
		                String text = txt_description.getText().subSequence(0, lineEndIndex - 3)+"...";
		                
		                txt_description.setText(Html.fromHtml(text).toString());
		            }
	
		        }
		    });
			
			return v;
		}
	}
	
	private void sortMiles() {
		for(int i = 0 ; i < MainActivity._data.size() - 1 ; i++) {
			for(int j = i + 1 ; j < MainActivity._data.size() ; j++) {
				if(MainActivity._data.get(j).getMiles() < MainActivity._data.get(i).getMiles()) {
					DataModel model1 = MainActivity._data.get(i);
					DataModel model2 = MainActivity._data.get(j);
					
					MainActivity._data.set(i, model2);
					MainActivity._data.set(j, model1);
				}
			}
		}
	}
	
	private double calculateDistance(double fromLong, double fromLat,
            double toLong, double toLat) {
        double d2r = Math.PI / 180;
        double dLong = (toLong - fromLong) * d2r;
        double dLat = (toLat - fromLat) * d2r;
        double a = Math.pow(Math.sin(dLat / 2.0), 2) + Math.cos(fromLat * d2r)
                * Math.cos(toLat * d2r) * Math.pow(Math.sin(dLong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6367000 * c;
        return Math.round(d);
    }

	public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) mParent
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}
	
	public void Message(String msg) {
		AlertDialog alertDialog = new AlertDialog.Builder(mParent).create();
		alertDialog.setTitle("Event app");
		alertDialog.setMessage(msg);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		alertDialog.show();

	}

}
