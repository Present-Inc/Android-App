package tv.present.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import tv.present.android.R;

public final class DetailActivity extends Activity {

    /**
     * Creates this view.
     * @param savedInstanceState is a Bundle of data that represents how this view once existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }

    /**
     * Inflate the menu.
     * @param menu is a Menu.
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    /**
     * Handles clicks on the action bar.  Home/Up clicks are handled automatically so long as a
     * parent is specified in the manifest.
     * @param item is the MenuItem that was clicked.
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
