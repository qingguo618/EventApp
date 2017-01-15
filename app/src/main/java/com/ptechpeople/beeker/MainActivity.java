package com.ptechpeople.beeker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ptechpeople.beeker.model.DataModel;
import com.ptechpeople.beeker.until.APIManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener {

    public static final String TAG = "MainActivity";
//    String strHeaderURL = EventApp.HEADER_URL_DEV;
    String strHeaderURL = EventApp.HEADER_URL_PROD;

    public static ArrayList<DataModel> _data = new ArrayList<DataModel>();

    ListView listView;
    SearchAdapter adapter;
    String thumbnail_url = "";
    int refreshPage;

    Boolean isInternetPresent = false;
    boolean loading = false;

    Context context;
    int px, dp;

    private Handler mHandler;
    private Runnable mRunnable;
    private ProgressDialog progressDialog;
    private String beaconString = null;
    LoadRefreshTask mLoadRefreshTask = null;
    LoadProductTask mLoadProductTask = null;
    ImageLoader imageLoader = null;
    /*
    Code written by Birjesh...........
     */
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         Code written By Birjesh
         This code is written to check either device API level is >= 23
         and adding permission runtime
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }
        context = this;

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            beaconString = EventApp.beaconsString;
        } else {
            beaconString = extras.getString(EventApp.BEACON_STRING);
        }

        if(beaconString == null ||  beaconString.isEmpty() || beaconString.equals("null")) {
            beaconString = "";
        }

//        beaconString = "1001554415";

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        px = metrics.widthPixels;
        dp = (int) (px / (metrics.densityDpi / 160f));

        refreshPage = 1;
        imageLoader = ImageLoader.getInstance();

        listView = (ListView) findViewById(R.id.listView);
        adapter = new SearchAdapter(MainActivity._data, this);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(MainActivity.this, ContentActivity.class);
                intent.putExtra("index", position);
                startActivity(intent);
            }
        });

        findViewById(R.id.imgvBack).setOnClickListener(this);

        int nMiliSeconds = 1000;

        if(beaconString == null || beaconString.equalsIgnoreCase("null")) nMiliSeconds = 10000;

        progressDialog = ProgressDialog.show(context, "", "Finding BEEKER...", true);
        _data.clear();

//        try {
//            EventApp.beaconManager.startRangingBeaconsInRegion(EventApp.region);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();

                beaconString = EventApp.beaconsString;

                if(beaconString == null || beaconString.equalsIgnoreCase("null")){
                    beaconString = "";
                }

                getData();
            }
        };
        mHandler.postDelayed(mRunnable, nMiliSeconds);
    }
    /*
     Code written By Birjesh
     This provide the callBack for setting permission
     if Device API level is >= 23
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
    /*
      This method is used to verify either device bluetooth is on or not
     */
    private boolean verifyBluetooth() {
        Log.e(TAG,"@@@@@@@@@I am in Bijesh I am in verifyBluetooth");
        try {
            if (!EventApp.beaconManager.isBluetoothEnabled()) {
                Log.e(TAG,"@@@@@@@@@I am in Bijesh I am in verifyBluetooth in if");
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please turn ON bluetooth to get events.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.e(TAG, "@@@@@@@@@I am in Bijesh I am in verifyBluetooth setOnDismissListener");
                        /*finish();
                        System.exit(0);*/
                    }
                });
                builder.show();
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (RuntimeException e) {
            Log.e(TAG,"@@@@@@@@@I am in Bijesh I am in verifyBluetooth RuntimeException");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {
            case R.id.imgvBack:
                if(mLoadProductTask != null) mLoadProductTask.cancel(true);
                if(mLoadRefreshTask != null) mLoadRefreshTask.cancel(true);
                finish();
                break;
        }
    }



    public void getData() {

        isInternetPresent = isConnectingToInternet();
        if (isInternetPresent) {
           mLoadProductTask = new LoadProductTask();
            mLoadProductTask.execute();
        } else {
            Message("Please check internet connection.");
        }
    }

    public void getRefreshData() {

        isInternetPresent = isConnectingToInternet();
        if (isInternetPresent) {
            mLoadRefreshTask = new LoadRefreshTask();
            mLoadRefreshTask.execute();
        } else {
            Message("Please check internet connection.");
        }
    }

    class LoadProductTask extends AsyncTask<String, Integer, ArrayList<DataModel>> {
        String api_url;

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, "", "Loading...", true);
            //Toast.makeText(MainActivity.this,EventApp.beaconsString,Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(ArrayList<DataModel> result) {

            loading = true;
            /*
              Code written by Birjesh
             */
            EventApp.isApiCalling = false;
            if (_data.size() > 0) {
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MainActivity.this,"No Beeks found. Please try again.", Toast.LENGTH_SHORT).show();
                finish();

//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
//
//                // Setting Dialog Title
//                alertDialog.setTitle("URL information");
//
//                // Setting Dialog Message
//                alertDialog.setMessage(api_url);
//
//                // On pressing Settings button
//                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        finish();
//                    }
//                });
//
//                // on pressing cancel button
//                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        finish();
//                    }
//                });
//
//                // Showing Alert Message
//                alertDialog.show();
            }

            progressDialog.dismiss();
        }

        @Override
        protected ArrayList<DataModel> doInBackground(String... param) {
            _data.clear();
            /*
              Code written by Birjesh
             */
            EventApp.isApiCalling = true;
//            beaconString = "1001554415";
            Log.d(TAG,"####BeaconString="+ beaconString);
            api_url = strHeaderURL + "/api/post_beek.php?filter[post_status]=publish&filter[posts_per_page]=10&page=1&filter[category_name]=" + beaconString + "&lat=" + EventApp.lat + "&lng=" + EventApp.lng;
//            api_url = "http://ptechpeople.wpengine.com/api/post_beek.php?filter[post_status]=publish&filter[posts_per_page]=10&page=1&filter[category_name]=1014554555&lat=40.1199516&lng=124.3615693";
            Log.d(TAG,"####BeaconString URL="+ api_url);
            String result = APIManager.getInstance().callGet(context, api_url, null, true);
            try {
                JSONArray arr_data = new JSONArray(result);

                for (int i = 0; i < arr_data.length(); i++) {
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
            progressDialog = ProgressDialog.show(context, "", "Loading...", true);
        }

        @Override
        protected void onPostExecute(ArrayList<DataModel> result) {
 /*
              Code written by Birjesh
             */
            EventApp.isApiCalling = false;
            adapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }

        @Override
        protected ArrayList<DataModel> doInBackground(String... param) {
            /*
             Code written by Birjesh
             */
            EventApp.isApiCalling = true;
            Log.d(TAG,"####BeaconString="+ beaconString);
            String api_url = strHeaderURL + "/api/post_beek.php?filter[post_status]=publish&filter[posts_per_page]=10&page=" + refreshPage +"&filter[category_name]=" + beaconString + "&lat=" + EventApp.lat + "&lng=" + EventApp.lng;

            String result = APIManager.getInstance().callGet(context, api_url, null, true);

            try {
                JSONArray arr_data = new JSONArray(result);

                for (int i = 0; i < arr_data.length(); i++) {
                    JSONObject item = arr_data.getJSONObject(i);

                    setDataModel(item);
                }

                if (arr_data.length() > 0) {
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
//            model.setFeatured_image(item.getInt("featured_image"));
            model.setComment_status(item.getString("comment_status"));
            model.setPing_status(item.getString("ping_status"));
            model.setSticky(item.getBoolean("sticky"));
            model.setFormat(item.getString("format"));

            if(item.has("_links")){
                JSONObject links_obj = item.getJSONObject("_links");

                ArrayList<String> self_links = new ArrayList<String>();
                JSONArray self_arr = links_obj.getJSONArray("self");
                for (int j = 0; j < self_arr.length(); j++) {
                    JSONObject href_obj = self_arr.getJSONObject(j);

                    self_links.add(href_obj.getString("href"));
                }
                model.setSelf_links(self_links);

                ArrayList<String> collection_links = new ArrayList<String>();
                JSONArray collection_arr = links_obj.getJSONArray("collection");
                for (int j = 0; j < collection_arr.length(); j++) {
                    JSONObject href_obj = collection_arr.getJSONObject(j);

                    collection_links.add(href_obj.getString("href"));
                }
                model.setCollection_links(collection_links);

                ArrayList<String> author_links = new ArrayList<String>();
                JSONArray author_arr = links_obj.getJSONArray("author");
                for (int j = 0; j < author_arr.length(); j++) {
                    JSONObject href_obj = author_arr.getJSONObject(j);

                    author_links.add(href_obj.getString("href"));
                }
                model.setAuthor_links(author_links);

                ArrayList<String> replies_links = new ArrayList<String>();
                JSONArray replies_arr = links_obj.getJSONArray("replies");
                for (int j = 0; j < replies_arr.length(); j++) {
                    JSONObject href_obj = replies_arr.getJSONObject(j);

                    replies_links.add(href_obj.getString("href"));
                }
                model.setReplies_links(replies_links);

                ArrayList<String> version_history_links = new ArrayList<String>();
                JSONArray version_arr = links_obj.getJSONArray("version-history");
                for (int j = 0; j < version_arr.length(); j++) {
                    JSONObject href_obj = version_arr.getJSONObject(j);

                    version_history_links.add(href_obj.getString("href"));
                }
                model.setVersion_history_links(version_history_links);

            }

//            if (links_obj.has("http://api.w.org/featuredmedia")) {
//                JSONArray media_arr = links_obj.getJSONArray("http://api.w.org/featuredmedia");
//                JSONObject href_obj = media_arr.getJSONObject(0);
//                String thumbnail_api = href_obj.getString("href");
//                String media = APIManager.getInstance().callGet(this, thumbnail_api, null, true);
//
//                JSONObject _obj = new JSONObject(media);
//                thumbnail_url = _obj.getString("source_url");
//
//                model.setThumbnail_links(thumbnail_url);
//            }

            thumbnail_url = item.getString("featured_image_url");

            if(thumbnail_url.equals("false")){
                thumbnail_url = "";
                model.setFeatured_image(0);
            }else{
                model.setFeatured_image(1);
            }

            model.setThumbnail_links(thumbnail_url);

            String content_image = "";
            if (model.getFeatured_image() != 0) {
                content_image = String.format("<img src='%s' width='%d' height='300'>", thumbnail_url, dp - 15);
            }

            String html = item.getJSONObject("content").getString("rendered");
            String new_html = "";
            Document doc = Jsoup.parse(html);
            Elements elementsLinks = doc.select("iframe[width]");
            if (!elementsLinks.isEmpty()) {
                Element ee = elementsLinks.get(0);

                String video_width = ee.attr("width");
                new_html = html.replace(video_width, String.valueOf(dp - 15));
            } else {
                new_html = html;
            }
            model.setContent(content_image + "\n" + new_html);

            String location = item.getString("author_coordinates");
            if (location.contains(",")) {
                String[] arr_str = location.split(",");

                Location new_location = new Location(item.getString("author_address"));
                new_location.setLatitude(Double.parseDouble(arr_str[0]));
                new_location.setLongitude(Double.parseDouble(arr_str[1]));

                double distance = calculateDistance(EventApp.lat, EventApp.lng, Double.parseDouble(arr_str[0]), Double.parseDouble(arr_str[1]));

                float miles = (float) (distance / 1000 / 1.609344);
                model.setMiles(miles);
            } else {
                model.setMiles(0);
            }

            if(item.has("distance")){
                model.setDistance(item.getDouble("distance"));
            }else{
                model.setDistance(0);
            }

            _data.add(model);

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
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = inflater.inflate(R.layout.list_item, null);

            DataModel model = _data.get(position);

            final ImageView picture = (ImageView) v.findViewById(R.id.picture);

            if (model.getFeatured_image() == 0) {
                UrlImageViewHelper.setUrlDrawable(picture, null, R.drawable.logo);
            } else {
                if(model.bitmap != null){
                    picture.setImageBitmap(model.bitmap);
                }else{

                    Picasso.with(context)
                            .load(model.getThumbnail_links())
                            .into(picture);
//                    imageLoader.loadImage(model.getThumbnail_links(), new ImageLoadingListener() {
//                        @Override
//                        public void onLoadingStarted(String s, View view) {
//
//                        }
//
//                        @Override
//                        public void onLoadingFailed(String s, View view, FailReason failReason) {
//
//                        }
//
//                        @Override
//                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
//                            picture.setImageBitmap(bitmap);
//                            DataModel model = _data.get(position);
//                            model.bitmap = bitmap;
//                        }
//
//                        @Override
//                        public void onLoadingCancelled(String s, View view) {
//
//                        }
//                    });
                }
            }
            //Typeface font = Typeface.createFromAsset(getAssets(), "fonts/ufonts.com_century-gothic.ttf");

            TextView txt_title = (TextView) v.findViewById(R.id.txt_title);
            //txt_title.setTypeface(font);
            txt_title.setText(Html.fromHtml(model.getTitle()));

            int height = txt_title.getHeight();
            int remain_height = 100 - height / 2;

            int line_count = 0;
            if (remain_height > 20) line_count = 1;
            else if (remain_height > 40) line_count = 1;
            else if (remain_height > 60) line_count = 2;
            else if (remain_height > 80) line_count = 3;

            final int line = line_count;

            final TextView txt_description = (TextView) v.findViewById(R.id.txt_description);
           // txt_description.setTypeface(font);

            String description = model.getExcerpt();
            String temp_str = description.replace("<p>", "");
            String new_str = temp_str.replace("</p>", "");

            txt_description.setText(Html.fromHtml(new_str));
            ViewTreeObserver vto = txt_description.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver obs = txt_description.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);
                    if (txt_description.getLineCount() > line) {

                        int lineEndIndex = txt_description.getLayout().getLineEnd(line - 1);

                        Log.d("subsequence", String.valueOf(lineEndIndex - line));
                        String text = txt_description.getText().subSequence(0, lineEndIndex - line) + "...";
                        txt_description.setText(Html.fromHtml(text).toString());
                    }
                }
            });

            TextView txt_distance = (TextView) v.findViewById(R.id.txt_distance);
            double dblDistance = model.getDistance();

            if(dblDistance < 0.0001){
                txt_distance.setVisibility(View.INVISIBLE);
            }else{
                txt_distance.setVisibility(View.VISIBLE);
                txt_distance.setText("Distance: " + dblDistance + " miles");
            }
            /*
             Code witten by Birjesh to check wheter list has to be refresh or not
             */
            if(position == MainActivity._data.size()-1 && verifyBluetooth())
            {
                if (loading) {
                    loading = false;
                    refreshPage++;

                    if(MainActivity._data.size() > 1) getRefreshData();
                }
            }
            return v;
        }
    }

    private void sortMiles() {
        for (int i = 0; i < MainActivity._data.size() - 1; i++) {
            for (int j = i + 1; j < MainActivity._data.size(); j++) {
                if (MainActivity._data.get(j).getMiles() < MainActivity._data.get(i).getMiles()) {
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
        ConnectivityManager connectivity = (ConnectivityManager) this
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
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Event app");
        alertDialog.setMessage(msg);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();

    }
}
