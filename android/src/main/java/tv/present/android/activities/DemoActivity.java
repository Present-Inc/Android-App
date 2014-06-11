package tv.present.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import tv.present.android.R;
import tv.present.android.controllers.EntryController;

public final class DemoActivity extends Activity implements View.OnClickListener {

    /**
     * Creates this view.
     * @param savedInstanceState is a Bundle of data that represents how this view once existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        // Set button click listeners
        final Button demoButton1 = (Button) this.findViewById(R.id.demo1Button);
        final Button demoButton2 = (Button) this.findViewById(R.id.demo2Button);
        final Button demoButton3 = (Button) this.findViewById(R.id.demo3Button);
        demoButton1.setOnClickListener(this);
        demoButton2.setOnClickListener(this);
        demoButton3.setOnClickListener(this);

    }

    /**
     * Inflate the menu.
     * @param menu is a Menu.
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.demo, menu);
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

    /**
     * Handles clicks on each of the demo buttons.
     * @param view is the View (Button) that was clicked.
     */
    public void onClick(View view) {

        Intent intent;

        if (view instanceof Button) {

            switch(view.getId()) {
                case R.id.demo1Button:
                    intent = new Intent(this, EntryController.class);
                    startActivity(intent);
                    break;
                case R.id.demo2Button:
                    intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    break;
                case R.id.demo3Button:
                    intent = new Intent(this, HLSActivity.class);
                    startActivity(intent);
                    break;
            }

        }

    }

}
