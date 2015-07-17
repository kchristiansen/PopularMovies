package com.example.kevin.popularmovies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetails extends android.support.v4.app.Fragment {

    View rootView;

    public static MovieDetails newInstance(MainActivityFragment.Movie movie) {
        MovieDetails fragment = new MovieDetails();
        Bundle args = new Bundle();
        args.putSerializable("movie", movie);
        fragment.setArguments(args);

        return fragment;
    }

    public MovieDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        Bundle args = getArguments();

        MainActivityFragment.Movie movie = (MainActivityFragment.Movie)args.getSerializable("movie");
        if(movie!=null)
        {
            updateMovie(movie);
        }

        return rootView;
    }

    public void updateMovie(MainActivityFragment.Movie m) {
        String baseMovieDbImageUrl = getResources().getString(R.string.moviedb_base_image_url) + getResources().getString(R.string.moviedb_image_size);

        TextView title = (TextView) rootView.findViewById(R.id.detail_title);
        TextView rating = (TextView) rootView.findViewById(R.id.detail_rating);
        TextView releaseDate = (TextView) rootView.findViewById(R.id.detail_release_date);
        TextView synopsis = (TextView) rootView.findViewById(R.id.detail_synopsis);
        ImageView poster = (ImageView) rootView.findViewById(R.id.detail_movie_poster);
        RatingBar starRating = (RatingBar) rootView.findViewById(R.id.detail_rating_stars);

        title.setText(m.mTitle);
        releaseDate.setText(m.releaseDate);
        rating.setText("(" + String.valueOf(m.mRating) + "/10)");
        synopsis.setText(m.mSynopsis);
        starRating.setRating((float)m.mRating / 2);
        Picasso.with(getActivity())
                .load(baseMovieDbImageUrl + m.mPosterUri)
                .resize(158,237)
                .centerInside()
                .into(poster);
    }
}
