package tv.present.android.controllers;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import tv.present.android.R;
import tv.present.android.models.PView;
import tv.present.android.util.PLog;
import tv.present.android.views.PCreationalView;

/**
 * Present Creational Controller Object
 *
 * This controller manages the creation of content on Present.
 *
 * June 20, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public class PCreationalController extends PController {

    private static final String TAG ="tv.present.android.controllers.PCreationalController";
    private PCreationalView creationalView;

    /**
     * Creates this view.
     * @param savedInstanceState is a Bundle of data that represents how this view once existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState == null) {
            PView creationalView = this.getCreationalView();
            getFragmentManager().beginTransaction().add(R.id.container, creationalView).commit();
        }
    }

    /**
     * Inflate the menu.
     * @param menu is a Menu.
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Handle clicks on the action bar.  Home/Up clicks are handled automatically so long as a
     * parent is specified in the manifest.
     * @param item is the MenuItem that was clicked.
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_about) {

            Toast.makeText(this, "You clicked the menu!", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets the login view that this controller controls.
     * @return a LoginView.
     */
    public PCreationalView getCreationalView() {
        if (this.creationalView == null) {
            PLog.logDebug(TAG, "getCreationalView() -> A creational view did not exist.  Creating a new one.");
            this.creationalView = PCreationalView.newInstance(this);
        }

        return this.creationalView;
    }

}
