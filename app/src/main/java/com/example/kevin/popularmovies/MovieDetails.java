package com.example.kevin.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MovieDetails extends android.support.v4.app.Fragment {

    private final String LOG_TAG = "MovieDetails";
    private View mRootView;
    private Movie mMovie;
    private String mTrailerData;
    private String mReviewData;
    private LinearLayout mTrailerList;
    private LinearLayout mReviewList;
    private Context mContext;
    private CheckBox mFavButton;
    String API_KEY;

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
        API_KEY = getResources().getString(R.string.API_KEY);
        mContext = getActivity();
        mRootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        mTrailerList = (LinearLayout)mRootView.findViewById(R.id.tableTrailers);
        mReviewList = (LinearLayout)mRootView.findViewById(R.id.tableReviews);
        mFavButton = (CheckBox) mRootView.findViewById(R.id.buttonFavorite);


        Bundle args = getArguments();

        if(args==null) return mRootView;

        Movie movie = deserializeMovie(args.getString("movie"));

        if(movie!=null)
        {
            final String movieID = movie.mID;
            updateMovie(movie);
        }

        return mRootView;
    }

    private void setupFavoriteButton(final String movieID){
        // set initial button state
        new GetFavoriteSetting().execute(movieID);
    }

    private void getReviews(String movieID) {
        mReviewList.removeAllViews();
        new ReviewInfoDownloader().execute(movieID);
    }
    private void getTrailers(String movieID) {
        mTrailerList.removeAllViews();
        new TrailerInfoDownloader().execute(movieID);
    }

    public class ReviewInfoDownloader extends AsyncTask<String, Void, ArrayList<Review>> {
        @Override
        protected void onPostExecute(ArrayList<Review> reviews) {
            // Update layout...
            int size = reviews.size();
            if(size>3){
                size=3;
                Button viewAllButton = (Button) mRootView.findViewById(R.id.buttonAllReviews);
                viewAllButton.setVisibility(View.VISIBLE);
            }

            View reviewView = null;
            if(size>0){
                mRootView.findViewById(R.id.textNoReviews).setVisibility(View.GONE);
            }

            for(int i=0;i<size;i++){
                reviewView = View.inflate(getActivity(), R.layout.review_list_item, null);
                Review r = reviews.get(i);
                TextView author = (TextView)reviewView.findViewById(R.id.reviewerName);
                author.setText(r.author);
                TextView content = (TextView)reviewView.findViewById(R.id.reviewContent);
                content.setText(r.content);

                mReviewList.addView(reviewView);
            }
        }

        @Override
        protected ArrayList<Review> doInBackground(String... params) {
            String reviewsUrl = "http://api.themoviedb.org/3/movie/"+params[0]+"/reviews?api_key=" + API_KEY;
            Uri uri = Uri.parse(reviewsUrl);
            if(mReviewData==null || mReviewData=="") {
                Log.v(LOG_TAG, "Hitting movie api: " + uri.toString());
                mReviewData = JsonDataFetch.fetchJson(uri);
            }

            ArrayList<Review> reviews = new ArrayList<>();

            try {
                reviews = getReviewsFromJson(mReviewData);
            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return reviews;
        }

        private ArrayList<Review> getReviewsFromJson(String json) throws JSONException {
            ArrayList<Review> reviews = new ArrayList<>();
            final String RESULTS = "results";

            JSONArray results= new JSONObject(json).getJSONArray(RESULTS);
            for(int i=0;i< results.length();i++){
                JSONObject result = results.getJSONObject(i);
                Review r = Review.newInstance(result);
                if(r!=null) {
                    reviews.add(r);
                }
            }
            return reviews;
        }
    }

    public class GetFavoriteSetting extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPostExecute(final Boolean isFavorite) {

            mFavButton.setChecked(isFavorite);

        }

        @Override
        protected Boolean doInBackground(final String... movieID) {
            FavoritesDbHelper dbHelper = new FavoritesDbHelper(getActivity());
            final SQLiteDatabase db = dbHelper.getWritableDatabase();

            final String selection = FavoritesContract.FavoritesEntry.COLUMN_ID + " =?";

            final String[] selectionArgs = {
                    movieID[0]
            };

            Cursor c = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            return c.getCount()>0;
        }
    }

    public class TrailerInfoDownloader extends AsyncTask<String,Void,ArrayList<Trailer>>{

        @Override
        protected void onPostExecute(ArrayList<Trailer> trailers) {
            // Update layout...
            int size = trailers.size();
            if(size>3){
                size=3;
                Button viewAllButton = (Button) mRootView.findViewById(R.id.buttonAllTrailers);
                viewAllButton.setVisibility(View.VISIBLE);
            }

            View trailerView = null;

            for(int i=0;i<size;i++){
                Trailer t = trailers.get(i);
                final Uri uri = Uri.parse(t.Uri);
                trailerView = View.inflate(getActivity(), R.layout.trailer_list_item, null);
                trailerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
                TextView title = (TextView)trailerView.findViewById(R.id.trailerTitle);
                title.setText(t.name);
                mTrailerList.addView(trailerView);
            }
        }

        @Override
        protected ArrayList<Trailer> doInBackground(String... params) {

            String trailersUrl = "http://api.themoviedb.org/3/movie/"+params[0]+"/videos?api_key=" + API_KEY;
            Uri uri = Uri.parse(trailersUrl);
            if(mTrailerData==null || mTrailerData=="") {
                Log.v(LOG_TAG, "Hitting movie api: " + uri.toString());
                mTrailerData = JsonDataFetch.fetchJson(uri);
            }

            ArrayList<Trailer> trailers = new ArrayList<>();

            try {
                trailers = getTrailersFromJson(mTrailerData);
            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return trailers;
        }

        private ArrayList<Trailer> getTrailersFromJson(String json) throws JSONException {
            ArrayList<Trailer> trailers = new ArrayList<>();
            final String RESULTS = "results";

            JSONArray results= new JSONObject(json).getJSONArray(RESULTS);
            for(int i=0;i< results.length();i++){
                JSONObject result = results.getJSONObject(i);
                Trailer t = Trailer.newInstance(result);
                if(t!=null) {
                    trailers.add(t);
                }
            }
            return trailers;
        }
    }

    public void updateMovie(Movie m) {
        mRootView.findViewById(R.id.instruction_panel).setVisibility(View.GONE);

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

        FavoritesDbHelper dbHelper = new FavoritesDbHelper(getActivity());
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        mFavButton.setOnCheckedChangeListener(null);
        mFavButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final String selection = FavoritesContract.FavoritesEntry.COLUMN_ID + " =?";
                final String[] selectionArgs = {
                        mMovie.mID
                };
                db.delete(FavoritesContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);

                if (isChecked) {
                    ContentValues values = new ContentValues();
                    values.put(FavoritesContract.FavoritesEntry.COLUMN_ID, mMovie.mID);
                    db.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, "", values);
                }
            }
        });

        setupFavoriteButton(m.mID);
        mTrailerData=null;
        mReviewData=null;
        getReviews(m.mID);
        getTrailers(m.mID);
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
