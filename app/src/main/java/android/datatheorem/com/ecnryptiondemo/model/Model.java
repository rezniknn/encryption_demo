package android.datatheorem.com.ecnryptiondemo.model;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.datatheorem.com.ecnryptiondemo.Utils.DbBitmapUtility;
import android.datatheorem.com.ecnryptiondemo.Utils.SecurityUtils;
import android.datatheorem.com.ecnryptiondemo.listeners.ImageLoadedListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import example.EventDataSQLHelper;

/**
 * Created by alexeyreznik on 23/07/15.
 */
public class Model {
    private static final String IMAGE_URL = "http://lorempixel.com/800/600/";
    private static final String KEY_DATE = "date";
    private static final String KEY_IMAGE = "image";
    private static final String DB_TABLE = "pictures";
    private static final String DB_NAME = "picture.db";
    private static final String KEY_ALIAS = "encryption_demo";
    private static final String SP_FIRST_RUN = "first_run";
    private static final String SP_DB_PASSWORD = "db_password";
    private Context ctx;
    private SharedPreferences sp;

    public Model(Context ctx) {
        this.ctx = ctx;
        sp =  ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
        boolean firstRun = sp.getBoolean(SP_FIRST_RUN, true);
        //Is it a first launch of the app
        if (firstRun) {

            sp.edit().putBoolean(SP_FIRST_RUN, false).apply();

            //Generate random password String and init SQLCipher
            String password = SecurityUtils.generateRandomString();
            InitializeSQLCipher(password);

            //Save password to shared preferences
            sp.edit().putString(SP_DB_PASSWORD, password).apply();
        }
    }

    private void InitializeSQLCipher(String password) {
        SQLiteDatabase.loadLibs(ctx);
        File databaseFile = ctx.getDatabasePath(DB_NAME);
        databaseFile.mkdirs();
        databaseFile.delete();
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile, password, null);
        db.execSQL("create table if not exists " + DB_TABLE + " (" + KEY_DATE + " TEXT, " + KEY_IMAGE + " BLOB);");
        db.close();
    }

    public void loadNewPicture(ImageLoadedListener listener) {
        new Thread(new LoadImageRunnable(listener)).start();
    }

    public void getRecentPicture(final ImageLoadedListener listener) {
        String password = sp.getString(SP_DB_PASSWORD, "");
        if (!password.isEmpty()) {
            File databaseFile = ctx.getDatabasePath(DB_NAME);
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile, password, null);
            Bitmap image = null;
            Cursor cursor = db.rawQuery("select * from " + DB_TABLE + " order by " + KEY_DATE + " DESC limit 1;", null);
            if (cursor.moveToFirst()) {
                Log.d(this.getClass().getSimpleName(), "Most recent image has been found. Date: "
                        + cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                byte[] raw = cursor.getBlob(cursor.getColumnIndex(KEY_IMAGE));
                image = DbBitmapUtility.getImage(raw);
            } else {
                Log.d(this.getClass().getSimpleName(), "Recent picture not found");
            }
            cursor.close();
            db.close();
            //Notify the View
            if (listener != null) {
                listener.onImageReady(image);
            }
        } else {
            Log.e(this.getClass().getSimpleName(), "Failed to retrieve DB password from Shared Pref");
        }
    }

    private class LoadImageRunnable implements Runnable {
        private ImageLoadedListener listener;

        public LoadImageRunnable(ImageLoadedListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            final Bitmap image = getBitmapFromURL(IMAGE_URL);
            //Save image to DB
            saveImageToDB(image);
            //Notify the View
            if (listener != null) {
                listener.onImageReady(image);
            }
        }

        public Bitmap getBitmapFromURL(String src) {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void saveImageToDB(Bitmap image) {
            if (image != null) {
                String password = sp.getString(SP_DB_PASSWORD, "");
                if (!password.isEmpty()) {
                    File databaseFile = ctx.getDatabasePath(DB_NAME);
                    SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile, password, null);
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String formattedDate = df.format(c.getTime());

                    ContentValues cv = new ContentValues();
                    cv.put(KEY_DATE, formattedDate);
                    cv.put(KEY_IMAGE, DbBitmapUtility.getBytes(image));
                    db.insert(DB_TABLE, null, cv);
                    db.close();
                    Log.d(this.getClass().getSimpleName(), "New image is stored to DB. Date: " + formattedDate);
                } else {
                    Log.e(this.getClass().getSimpleName(), "Failed to retrieve DB password from SharedPref");
                }
            }
        }
    }
}
