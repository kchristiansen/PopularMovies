package com.example.kevin.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends android.support.v4.app.Fragment {

    private OnMovieSelectedListener mCallback;
    private int gridPosition=0;
    private String currentSort;

    public MainActivityFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        currentSort = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("sortby", "popularity.desc");

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
    public interface OnMovieSelectedListener {
        void onMovieSelected(Movie m);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mCallback = (OnMovieSelectedListener) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement OnMovieSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback=null;
    }

    private Context mContext;
    private MovieAdapter mMovieAdapter;
    private GridView mGrid;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGrid = (GridView) rootView.findViewById(R.id.gridViewMovies);
        mMovieAdapter = new MovieAdapter();
        mGrid.setAdapter(mMovieAdapter);
        mGrid.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mGrid.setSelection(gridPosition);
            }
        });
        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gridPosition = position;
                Movie m = (Movie) mMovieAdapter.getItem(position);
                mCallback.onMovieSelected(m);
            }
        });
        //getPopularMovies();
        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("sort", currentSort);
        outState.putInt("gridPosition", mGrid.getFirstVisiblePosition());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState!=null) {
            gridPosition = savedInstanceState.getInt("gridPosition");
            currentSort = savedInstanceState.getString("sort");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String newSort = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("sortby", "popularity.desc");
        if (currentSort != newSort) {
            currentSort = newSort;
            gridPosition = 0;
        }

        getPopularMovies();
    }

    private void getPopularMovies(){
        new MovieDownloader().execute();

    }

    public class MovieAdapter extends BaseAdapter{
        ArrayList<Movie> mMovies;
        final String imageUrl = getResources().getString(R.string.moviedb_base_image_url) + getResources().getString(R.string.moviedb_image_size);
        @Override
        public int getCount() {
            if(mMovies==null) return 0;
            return  mMovies.size();
        }

        @Override
        public Object getItem(int position) {
            if(mMovies==null) return null;
            return mMovies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if(convertView==null){
                convertView = inflater.inflate(R.layout.main_poster_thumbnail, null);
            }

            Movie movieInfo = mMovies.get(position);

            MovieViewHolder holder = (MovieViewHolder) convertView.getTag();

            if(holder==null) {
                holder = new MovieViewHolder();
                holder.movieInfo = movieInfo;
                holder.moviePoster = (ImageView) convertView.findViewById(R.id.mainPosterThumb);

                holder.moviePoster.setImageResource(R.mipmap.ic_launcher);
                Picasso.with(mContext)
                        .load(imageUrl + movieInfo.mPosterUri)
                        .resize(158,237)
                        .centerCrop()
                        .into(holder.moviePoster);
            }

            if(holder.movieInfo.mID !=movieInfo.mID){
                holder.movieInfo=movieInfo;
                Picasso.with(mContext)
                        .load(imageUrl + movieInfo.mPosterUri)
                        .resize(158,237)
                        .centerInside()
                        .into(holder.moviePoster);
            }

            return convertView;
        }
    }

    public class MovieViewHolder{
        Movie movieInfo;
        ImageView moviePoster;
    }

    public class Movie implements Serializable {
        String mID;
        String mTitle;
        String mSynopsis;
        double mRating;
        String releaseDate;
        String mPosterUri;
    }

    public class MovieDownloader extends AsyncTask<String,Void, ArrayList<Movie>> {
        final String LOG_TAG = "MovieJsonDownload";
        ArrayList<Movie> movies = new ArrayList<>();
        @Override
        protected void onPostExecute(ArrayList<Movie> movieList) {
            super.onPostExecute(movieList);
            // Update adapter...
            mMovieAdapter.mMovies = movieList;
            mMovieAdapter.notifyDataSetChanged();
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            String BASEURL = "http://api.themoviedb.org/3/discover/movie?sort_by=" + currentSort + "&api_key=b002976a12de8c9c6ae1d89a3d0faea2";
            Uri builtUri = Uri.parse(BASEURL);

            // Will contain the raw JSON response as a string.
            String movieDbJsonStr = JsonDataFetch.fetchJson(builtUri);

            try {
                movies = getMoviesFromJson(movieDbJsonStr);
            }
            catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return movies;
        }

        ArrayList<Movie> getMoviesFromJson(String json) throws JSONException {
            ArrayList<Movie> movies = new ArrayList<>();

            final String RESULTS = "results";
            final String ID = "id";
            final String RATING = "vote_average";
            final String TITLE = "title";
            final String SYNOPSIS = "overview";
            final String RELEASEDATE = "release_date";
            final String POSTER="poster_path";

            JSONArray results= new JSONObject(json).getJSONArray(RESULTS);
            for(int i=0;i< results.length();i++){
                JSONObject result = results.getJSONObject(i);
                Movie m = new Movie();
                m.mID = result.getString(ID);
                m.mTitle = result.getString(TITLE);
                m.mSynopsis = result.getString(SYNOPSIS);
                m.mPosterUri = result.getString(POSTER);
                m.releaseDate = result.getString(RELEASEDATE);
                m.mRating = result.getDouble(RATING);
                movies.add(m);
            }
            return movies;
        }
    }
}
