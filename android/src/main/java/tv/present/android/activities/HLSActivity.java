package tv.present.android.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import tv.present.android.R;

public class HLSActivity extends Activity {

    private static ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //displays "loading" circle during buffering
        progressDialog = ProgressDialog.show(this, "", "Buffering...", true);
        setContentView(R.layout.fragment_hls);
        final VideoView player = (VideoView) findViewById(R.id.player);
        //specify the HLS stream URL here
        String streamUrl = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
        player.setVideoURI(Uri.parse(streamUrl));
        //keep the screen on during viewing
        player.setKeepScreenOn(true);
        //loads the media controller/scrubber and sets skip/rewind to disabled
        player.setMediaController(new MediaController(this, false));
        player.requestFocus();
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            //clears loading circle when the stream is ready and starts the stream
            public void onPrepared(MediaPlayer mp) {
                progressDialog.dismiss();
                player.start();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_hls, container, false);
            return rootView;
        }
    }
}