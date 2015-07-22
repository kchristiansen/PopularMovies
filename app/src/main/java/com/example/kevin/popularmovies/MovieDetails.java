package com.example.kevin.popularmovies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieDetails extends android.support.v4.app.Fragment {

    private View mRootView;
    private Movie mMovie;

    public static MovieDetails newInstance(Movie movie) {
        MovieDetails fragment = new MovieDetails();
        Bundle args = new Bundle();
        args.putString("movie", movie.toJSON());
        fragment.setArguments(args);

        return fragment;
    }

    private static Movie deserializeMovie(String movieJson){
        Movie movie = null;
        try{
            JSONObject jsonObject = new JSONObject(movieJson);
            movie = Movie.newInstance(jsonObject);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return movie;
    }

    public MovieDetails() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        Bundle args = getArguments();

        Movie movie = deserializeMovie(args.getString("movie"));

        if(movie!=null)
        {
            updateMovie(movie);
        }

        return mRootView;
    }

    public void updateMovie(Movie m) {
        mMovie = m;
        String baseMovieDbImageUrl = getResources().getString(R.string.moviedb_base_image_url) + getResources().getString(R.string.moviedb_image_size);

        TextView title = (TextView) mRootView.findViewById(R.id.detail_title);
        TextView rating = (TextView) mRootView.findViewById(R.id.detail_rating);
        TextView releaseDate = (TextView) mRootView.findViewById(R.id.detail_release_date);
        TextView synopsis = (TextView) mRootView.findViewById(R.id.detail_synopsis);
        ImageView poster = (ImageView) mRootView.findViewById(R.id.detail_movie_poster);
        RatingBar starRating = (RatingBar) mRootView.findViewById(R.id.detail_rating_stars);

        title.setText(m.mTitle);
        releaseDate.setText(m.releaseDate);
        rating.setText("(" + String.valueOf(m.mRating) + "/10)");
        synopsis.setText(m.mSynopsis);
        starRating.setRating(Float.parseFloat(m.mRating) / 2);
        Picasso.with(getActivity())
                .load(baseMovieDbImageUrl + m.mPosterUri)
                .resize(158,237)
                .centerInside()
                .into(poster);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("movie", mMovie.toJSON());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState!=null) {
            mMovie = deserializeMovie(savedInstanceState.getString("movie"));
            if(mMovie!=null) {
                updateMovie(mMovie);
            }
        }
    }
}
