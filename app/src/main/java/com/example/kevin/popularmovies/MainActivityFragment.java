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

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends android.support.v4.app.Fragment {

    private OnMovieSelectedListener mCallback;
    private int mGridPosition = 0;
    private String mCurrentSort;
    private String mJsonMovieData;
    private Context mContext;
    private MovieAdapter mMovieAdapter;
    private GridView mGrid;

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

        mCurrentSort = PreferenceManager.getDefaultSharedPreferences(getActivity())
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
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mGrid.setSelection(mGridPosition);
            }
        });
        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mGridPosition = position;
                Movie m = (Movie) mMovieAdapter.getItem(position);
                mCallback.onMovieSelected(m);
            }
        });
        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("sort", mCurrentSort);
        if(mGrid!=null) {
            outState.putInt("gridPosition", mGrid.getFirstVisiblePosition());
        }
        outState.putString("movieJson", mJsonMovieData);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState!=null) {
            mGridPosition = savedInstanceState.getInt("gridPosition");
            mCurrentSort = savedInstanceState.getString("sort");
            mJsonMovieData = savedInstanceState.getString("movieJson");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String newSort = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("sortby", "popularity.desc");
        if (mCurrentSort != newSort) {
            mCurrentSort = newSort;
            mGridPosition = 0;
            mJsonMovieData = null;
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
                convertView.setTag(holder);
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

    public class MovieDownloader extends AsyncTask<String,Void, ArrayList<Movie>> {
        final String LOG_TAG = "MovieJsonDownload";
        final String API_KEY = "";

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
            if(API_KEY==null || API_KEY==""){
                Log.e(LOG_TAG, "API_KEY=" + API_KEY + ". Did you forget to add your own?");
            }

            String BASEURL = "http://api.themoviedb.org/3/discover/movie?sort_by=" + mCurrentSort + "&api_key=" + API_KEY;
            Uri builtUri = Uri.parse(BASEURL);

            // hit api if necessary...
            if(mJsonMovieData==null || mJsonMovieData=="") {
                // Will contain the raw JSON response as a string.
                Log.v(LOG_TAG, "Hitting movie api: " + builtUri.toString() );
                mJsonMovieData = JsonDataFetch.fetchJson(builtUri);
            }

            try {
                movies = getMoviesFromJson(mJsonMovieData);
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

            JSONArray results= new JSONObject(json).getJSONArray(RESULTS);
            for(int i=0;i< results.length();i++){
                JSONObject result = results.getJSONObject(i);
                Movie m = Movie.newInstance(result);
                if(m!=null) {
                    movies.add(m);
                }
            }
            return movies;
        }
    }
}
