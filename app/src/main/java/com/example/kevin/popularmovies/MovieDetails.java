package com.example.kevin.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
    private LinearLayout mTrailerTable;
    //private TrailerAdapter mTrailerAdapter;
    private Context mContext;

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
        mContext = getActivity();
        mRootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        //mTrailerAdapter = new TrailerAdapter();

        //ListView trailerListView = (ListView)mRootView.findViewById(R.id.trailerListView);
        mTrailerTable = (LinearLayout)mRootView.findViewById(R.id.tableTrailers);


        //trailerListView.setAdapter(mTrailerAdapter);
//        trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Trailer t = (Trailer) mTrailerAdapter.getItem(position);
//                Uri uri = Uri.parse(t.Uri);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
//            }
//        });

        Bundle args = getArguments();

        Movie movie = deserializeMovie(args.getString("movie"));



        if(movie!=null)
        {
            updateMovie(movie);
            getTrailers(movie.mID);
        }

        return mRootView;
    }

    public class TrailerAdapter extends BaseAdapter{
        private ArrayList<Trailer> mTrailers;

        public void setTrailers(ArrayList<Trailer> trailers){
            mTrailers = trailers;
        }

        @Override
        public int getCount() {
            if(mTrailers==null) return 0;
            return mTrailers.size();
        }

        @Override
        public Object getItem(int position) {
            if(mTrailers==null) return null;
            return mTrailers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if(convertView==null){
                convertView = inflator.inflate(R.layout.trailer_list_item, null);
            }

            Trailer trailerInfo = mTrailers.get(position);
            TextView title = (TextView)convertView.findViewById(R.id.trailerTitle);
            title.setText(trailerInfo.name);

            return convertView;
        }
    }


    private void getTrailers(String movieID) {
        new TrailerInfoDownloader().execute(movieID);
    }

    public class TrailerInfoDownloader extends AsyncTask<String,Void,ArrayList<Trailer>>{
        String API_KEY = getResources().getString(R.string.API_KEY);

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
                trailerView = (LinearLayout) View.inflate(getActivity(), R.layout.trailer_list_item, null);
                trailerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
                TextView title = (TextView)trailerView.findViewById(R.id.trailerTitle);
                title.setText(t.name);
                mTrailerTable.addView(trailerView);
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
