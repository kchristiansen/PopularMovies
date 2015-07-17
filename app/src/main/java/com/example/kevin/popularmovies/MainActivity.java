package com.example.kevin.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class MainActivity extends ActionBarActivity
        implements MainActivityFragment.OnMovieSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.fragment_container)!=null){
            if(savedInstanceState!=null){
                return;
            }
        }

        MainActivityFragment collectionFragment = new MainActivityFragment();
        collectionFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, collectionFragment)
                .commit();
    }

    @Override
    public void onMovieSelected(MainActivityFragment.Movie m) {
        MovieDetails detailFragment = (MovieDetails) getSupportFragmentManager().findFragmentById(R.id.fragment_movie_detail);
        if(detailFragment!=null) {
            detailFragment.updateMovie(m);
        }else{
            detailFragment = MovieDetails.newInstance(m);

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, detailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
