package com.example.kevin.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class MainActivity extends ActionBarActivity
        implements MainActivityFragment.OnMovieSelectedListener {

    private Boolean mIsDualPane;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        View detailView = findViewById(R.id.details_frag);

        if (detailView != null) {
            mIsDualPane = true;
            if (savedInstanceState != null) {
                return;
            }
        }
    }

    @Override
    public void onMovieSelected(final Movie m) {
        MovieDetails detailFragment = (MovieDetails) getSupportFragmentManager().findFragmentById(R.id.details_frag);
        if (mIsDualPane) {
            detailFragment.updateMovie(m);
        } else {
            detailFragment = MovieDetails.newInstance(m);
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, detailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
