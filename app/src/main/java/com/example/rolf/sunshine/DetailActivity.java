package com.example.rolf.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.support.v7.widget.ShareActionProvider;
// instead of android.widget.ShareActionProvider;
import android.widget.TextView;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail2);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }

        Animation animateDetail = AnimationUtils
                .loadAnimation(this, R.anim.anim_detailview);
        animateDetail.setDuration(2000);
        animateDetail.setFillAfter(true);
        findViewById(R.id.container).startAnimation(animateDetail);
    }

    @Override
    public void finish() {
        super.finish();
        //overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem shareItem = menu.findItem(R.id.item_share);
        //ShareActionProvider myShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        ShareActionProvider myShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
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
            startActivity( new Intent(this, SettingsActivity.class) );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private String myForecastString;

        public DetailFragment() {
            setHasOptionsMenu(true);
            // this leads to call onCreateOptionsMenu()
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present
            inflater.inflate(R.menu.menu_detail, menu);
            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.item_share);
            // Get the provider and hold ontu it to set or change the share intent
            ShareActionProvider myShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
                    // this gives the error cannot cast to android.suport.v4 ...
                    // udacity forum gives the following as remedy:
                    //(ShareActionProvider) menuItem.getActionProvider()
                    // but it doesn't work, instead improve imports - look there;

            // Attach an intent to this ShareActionProvider. You can update this at any time
            // like when the user selects a new piece of data to share
            if (myShareActionProvider != null) {
                myShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // create alpha animation for entry
            Animation animateDetail = AnimationUtils
                    .loadAnimation(getActivity().getApplicationContext(), R.anim.anim_detailview);
            animateDetail.setDuration(2000);
            animateDetail.setFillAfter(true);
            rootView.findViewById(R.id.detail_root).startAnimation(animateDetail);

            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                myForecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
                ( (TextView) rootView.findViewById(R.id.detail_text) ).setText(myForecastString);
            }


            return rootView;
        }

        private Intent createShareForecastIntent() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = prefs.getString(
                    getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default));

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            // prevents the App the system launches to beeing put an the activity stack
            shareIntent.setType("text/plain"); // tell Android that we gona going to share plain text
            shareIntent.putExtra(Intent.EXTRA_TEXT, location + myForecastString + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }
    }
}
