package com.app.rohit.popularmovies.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.app.rohit.popularmovies.R;
import com.app.rohit.popularmovies.fragment.MainActivityFragment;
import com.app.rohit.popularmovies.fragment.MovieDetailsFragment;

public class MainActivity extends AppCompatActivity implements
        MainActivityFragment.MainActivityFragmentToMainActivityCommunicator,
        MovieDetailsFragment.MovieDetailsFragmentToMainActivityCommunicator{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static boolean isPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.main_fragment_container) != null){
            if(savedInstanceState != null){
                return; //to avoid overlapping fragments
            }
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragment_container, new MainActivityFragment()).commit();

            if(findViewById(R.id.details_fragment_container) != null){
                isPhone = false;
                clearDetails();
            }else{
                isPhone = true;
            }
        }

    }

    public static boolean isPhone(){
        return isPhone;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_share);
        if(menuItem.isVisible()){
            menuItem.setVisible(false);
        }
        return true;
    }





    /*
    callbacks for MainActivityFragment.MainActivityFragmentToMainActivityCommunicator
     */

    @Override
    public void showDetails(Bundle bundle) {
        MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
        movieDetailsFragment.setArguments(bundle);
        if(isPhone){
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, movieDetailsFragment).addToBackStack(null).commit();   //in this case addToBackStack is needed to get back to home screen
        }else{
            getSupportFragmentManager().beginTransaction().replace(R.id.details_fragment_container, movieDetailsFragment).commit();
        }
    }

    @Override
    public void clearDetails() {
        if(!isPhone){   //as in case of only tablet, both fragments are lying around. So we can get a call to clear details screen
            getSupportFragmentManager().beginTransaction().replace(R.id.details_fragment_container, new MovieDetailsFragment()).commit();
            invalidateOptionsMenu();
        }
    }




    /*
    callbacks for MovieDetailsFragment.MovieDetailsFragmentToMainActivityCommunicator
     */

    @Override
    public void movieRemovedFromFavorites() {
        if(!isPhone){   //this is needed so that both main and details freshen up in case of user is seeing favorites list and removes a movie from favorites
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new MainActivityFragment()).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.details_fragment_container, new MovieDetailsFragment()).commit();
        }
    }
}
