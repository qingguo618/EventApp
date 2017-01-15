package com.ptechpeople.beeker.until;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class APIManager {
    private static APIManager INSTANCE = null;

    public static APIManager getInstance() {

        if(INSTANCE == null) {
            INSTANCE = new APIManager();
        }

        return INSTANCE;
    }

    public JSONObject callPost(Context context, String method, List<NameValuePair> params, boolean bacount) {

        String api_url = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(api_url);
        InputStream is = null;

        try {
            if (params != null)
                httppost.setEntity(new UrlEncodedFormEntity(params));
            
            if (bacount) {

            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                String access_token = prefs.getString("Access-Token", null);
                String device_id = prefs.getString("Device-Id", null);
                
                httppost.addHeader("Access-Token", access_token);
                httppost.addHeader("Device-Id", device_id);
            }
            
            try {
                HttpResponse response = httpclient.execute(httppost);
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                    //convert response to string
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();

                    //try parse the string to a JSON object
                    return new JSONObject(new JSONTokener(sb.toString()));
                } else {
                    // close connection
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }

            } catch (ClientProtocolException cpe) {
                System.out.println("First Exception caz of HttpResponese :" + cpe);
                cpe.printStackTrace();
            } catch (IOException ioe) {
                System.out.println("Second Exception caz of HttpResponse :" + ioe);
                ioe.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String callGet(Context context, String api_link, List<NameValuePair> params, boolean baccount) {

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = null;
        InputStream is = null;

        httpGet = new HttpGet(api_link);

        try {
            HttpResponse response = httpclient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                HttpEntity entity = response.getEntity();
                String str = EntityUtils.toString(entity, HTTP.UTF_8);
                return str;
//                is = entity.getContent();
//                //convert response to string
//                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
//                StringBuilder sb = new StringBuilder();
//                String line = null;
//                while ((line = reader.readLine()) != null) {
//                    sb.append(line + "\n");
//                }
//                is.close();
//                
//                new JSONObject(sb.toString());
//                //try parse the string to a JSON object
//                return new JSONObject(new JSONTokener(sb.toString()));
            } else {
                // close connection
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }

        } catch (ClientProtocolException cpe) {
            System.out.println("First Exception caz of HttpResponese :" + cpe);
            cpe.printStackTrace();
        } catch (IOException ioe) {
            System.out.println("Second Exception caz of HttpResponse :" + ioe);
            ioe.printStackTrace();
        } 

        return null;
    }
    
}

