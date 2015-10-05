package com.example.kevin.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private Boolean mIsConnected;
    private View mNoConnectionView;

    private final int MAX_CACHE_SIZE = 1024 * 1024;

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        mCurrentSort = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("sortby", "popularity.desc");

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
        try {
            mCallback = (OnMovieSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMovieSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        mContext = getActivity();
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mNoConnectionView = rootView.findViewById(R.id.no_connection_view);

        RetryConnection();

        Button retryConnectionButton = (Button) rootView.findViewById(R.id.button_retry);
        retryConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RetryConnection();
            }
        });

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
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                RetryConnection();
                if (mIsConnected) {
                    mGridPosition = position;
                    Movie m = (Movie) mMovieAdapter.getItem(position);
                    mCallback.onMovieSelected(m);
                }
            }
        });

        return rootView;
    }

    private void RetryConnection() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mIsConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        mNoConnectionView.setVisibility(mIsConnected ? View.GONE : View.VISIBLE);
        onResume();
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("sort", mCurrentSort);
        if (mGrid != null) {
            outState.putInt("gridPosition", mGrid.getFirstVisiblePosition());
        }
        outState.putString("movieJson", mJsonMovieData);
    }

    @Override
    public void onViewStateRestored(final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
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

        if (mIsConnected) {
            getPopularMovies();
        }
    }

    private void getPopularMovies() {
        new MovieDownloader().execute();
    }

    public class MovieAdapter extends BaseAdapter {
        private ArrayList<Movie> mMovies;
        final String imageUrl = getResources().getString(R.string.moviedb_base_image_url) + getResources().getString(R.string.moviedb_image_size);
        @Override
        public int getCount() {
            if (mMovies == null) {
                return 0;
            }
            return  mMovies.size();
        }

        @Override
        public Object getItem(final int position) {
            if (mMovies == null) {
                return null;
            }
            return mMovies.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.main_poster_thumbnail, null);
            }

            Movie movieInfo = mMovies.get(position);

            MovieViewHolder holder = (MovieViewHolder) convertView.getTag();

            if (holder == null) {
                holder = new MovieViewHolder();
                holder.movieInfo = movieInfo;
                holder.moviePoster = (ImageView) convertView.findViewById(R.id.mainPosterThumb);

                holder.moviePoster.setImageResource(R.mipmap.ic_launcher);
                Picasso.with(mContext)
                        .load(imageUrl + movieInfo.mPosterUri)
                        .into(holder.moviePoster);
                convertView.setTag(holder);
            }

            if (holder.movieInfo.mID != movieInfo.mID) {
                holder.movieInfo = movieInfo;
                Picasso.with(mContext)
                        .load(imageUrl + movieInfo.mPosterUri)
                        .into(holder.moviePoster);
            }

            return convertView;
        }
    }

    public class MovieViewHolder {
        Movie movieInfo;
        ImageView moviePoster;
    }

    public class MovieDownloader extends AsyncTask<String, Void, ArrayList<Movie>> {
        private final String LOG_TAG = "MovieJsonDownload";
        private final String API_KEY = getResources().getString(R.string.API_KEY);

        private ArrayList<Movie> movies = new ArrayList<>();
        @Override
        protected void onPostExecute(final ArrayList<Movie> movieList) {
            super.onPostExecute(movieList);
            // Update adapter...
            mMovieAdapter.mMovies = movieList;
            mMovieAdapter.notifyDataSetChanged();

        }

        @Override
        protected ArrayList<Movie> doInBackground(final String... params) {
            if (API_KEY == null || API_KEY == "") {
                Log.e(LOG_TAG, "API_KEY=" + API_KEY + ". Did you forget to add your own?");
            }

            if (mCurrentSort.equals("favorite")) {
                // get ids from sqlite db
                FavoritesDbHelper dbHelper = new FavoritesDbHelper(getActivity());
                final SQLiteDatabase db = dbHelper.getWritableDatabase();
                final String[] columns = {FavoritesContract.FavoritesEntry.COLUMN_ID};

                Cursor c = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME, columns, null, null, null, null, null);
                while (c.moveToNext()) {
                    String movieId = c.getString(0);
                    // get the cached movies from disklru cache
                    Movie m = MovieCache.GetMovieFromCache(mContext, movieId);
                    if (m == null) {
                        // make api calls if necessary
                        Uri builtUri = Uri.parse("http://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + API_KEY);
                        String jsonData = JsonDataFetch.fetchJson(builtUri);

                        if (jsonData != null) {
                            JSONObject jObjMovie = null;
                            try {
                                jObjMovie = new JSONObject(jsonData);
                                m = Movie.newInstance(jObjMovie);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (m != null) {
                        // add to movie list
                        movies.add(m);
                    }
                }
            } else {
                String baseUrl = "http://api.themoviedb.org/3/discover/movie?sort_by=" + mCurrentSort + "&api_key=" + API_KEY;

                Uri builtUri = Uri.parse(baseUrl);

                // hit api if necessary...
                if (mJsonMovieData == null || mJsonMovieData == "") {
                    // Will contain the raw JSON response as a string.
                    Log.v(LOG_TAG, "Hitting movie api: " + builtUri.toString());
                    mJsonMovieData = JsonDataFetch.fetchJson(builtUri);
                }

                if (mJsonMovieData != null) {
                    try {
                        movies = getMoviesFromJson(mJsonMovieData);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }
                }
            }
            return movies;
        }

        ArrayList<Movie> getMoviesFromJson(final String json) throws JSONException {
            movies = new ArrayList<>();
            final String RESULTS = "results";

            JSONArray results = new JSONObject(json).getJSONArray(RESULTS);
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);

                Movie m = Movie.newInstance(result);
                try {
                    MovieCache.CacheMovie(mContext, m, m.mID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (m != null) {
                    movies.add(m);
                }
            }
            return movies;
        }
    }
}
