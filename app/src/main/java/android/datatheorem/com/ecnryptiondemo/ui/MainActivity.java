package android.datatheorem.com.ecnryptiondemo.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.datatheorem.com.ecnryptiondemo.R;
import android.datatheorem.com.ecnryptiondemo.listeners.ImageLoadedListener;
import android.datatheorem.com.ecnryptiondemo.model.Model;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends Activity implements ImageLoadedListener {
    private Model model;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = Model.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();
        model.getPhotoOfTheDay(this);
    }

    @Override
    public void imageLoaded(final Bitmap image) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                ((ImageView)findViewById(R.id.iv_picture_of_the_day)).setImageBitmap(image);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
