package android.datatheorem.com.ecnryptiondemo.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.datatheorem.com.ecnryptiondemo.R;
import android.datatheorem.com.ecnryptiondemo.listeners.ImageLoadedListener;
import android.datatheorem.com.ecnryptiondemo.model.Model;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private SwipeRefreshLayout container;
    private Model model;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        container = ((SwipeRefreshLayout) findViewById(R.id.container));
        container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                container.setRefreshing(false);
                refresh();
            }
        });
        model = new Model(this);
        refresh();

    }

    private void refresh() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();

        //Get the most recent picture from DB
        model.getRecentPicture(new ImageLoadedListener() {

            @Override
            public void onImageReady(Bitmap image) {
                final Bitmap recentPic = image;

                //Load a new picture
                model.loadNewPicture(new ImageLoadedListener() {
                    @Override
                    public void onImageReady(final Bitmap newImage) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                //Update the views
                                if (recentPic != null) {
                                    ((ImageView) findViewById(R.id.iv2)).setImageBitmap(recentPic);
                                }
                                if (newImage != null) {
                                    ((ImageView) findViewById(R.id.iv1)).setImageBitmap(newImage);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            if (model != null) {
                refresh();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
