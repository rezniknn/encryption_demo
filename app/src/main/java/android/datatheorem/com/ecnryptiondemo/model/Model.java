package android.datatheorem.com.ecnryptiondemo.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.datatheorem.com.ecnryptiondemo.listeners.ImageLoadedListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by alexeyreznik on 23/07/15.
 */
public class Model {
    private static final String BING_URL = "http://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=en-US";
    private static Model instance = new Model();
    private Context ctx;
    private ProgressDialog dialog;

    private Model() {
    }

    public static Model getInstance() {
        return instance;
    }

    public void getPhotoOfTheDay(ImageLoadedListener listener) {
        new Thread(new LoadImageRunnable(listener)).start();
    }

    private class LoadImageRunnable implements Runnable {
        private ImageLoadedListener listener;

        public LoadImageRunnable(ImageLoadedListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            String response = makeQuery(BING_URL);
            String imageUrl = getImageUrl(response);
            Bitmap image = getBitmapFromURL(imageUrl);
            listener.imageLoaded(image);
        }
    }

    private static String makeQuery(String url) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder;
        String line = "";
        String response = null;
        try {
            URL u = new URL(url);
            httpURLConnection = (HttpURLConnection) u.openConnection();
            httpURLConnection.setRequestMethod("GET");
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            response = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return response;
    }


    private String getImageUrl(String jsonString) {
        String imageUrl = null;
        Log.d("Crypto", "JSON: " + jsonString);
        try {
            JSONObject images = new JSONObject(jsonString);
            JSONArray image_array = images.getJSONArray("images");
            JSONObject image = image_array.getJSONObject(0);
            imageUrl = image.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imageUrl;
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL("http://bing.com" + src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
