package com.example.kevin.popularmovies;

import android.app.Application;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView grid = (GridView) rootView.findViewById(R.id.gridViewMovies);
        MovieAdapter mMovieAdapter = new MovieAdapter();
        grid.setAdapter(mMovieAdapter);
        return rootView;
    }

    public class MovieAdapter extends BaseAdapter{
        ArrayList<Movie> mMovies;
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

            return null;

        }
    }

    public class MovieViewHolder{
        int position;
        Movie movieInfo;
        ImageView moviePoster;
    }
    public class DownloadParams{

    }

    public class Movie{
        String mID;
        Image mThumbnail;
        String mTitle;
        String mSynopsis;
        double mRating;
        String releaseDate;
        String mPosterUri;
    }

    public class MovieDownloader extends AsyncTask<DownloadParams,Void, Void> {
        final String LOG_TAG = "MovieJsonDownload";
        ArrayList<Movie> movies = new ArrayList<>();
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Update adapter...
        }

        @Override
        protected Void doInBackground(DownloadParams... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String BASEURL = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]";
            Uri builtUri = Uri.parse(BASEURL);
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Attempt to get it from raw
                    // I don't have a connection right now.
                    // Could also get cache the last successful response and read it here...
                    int id = getResources().getIdentifier("movies", "raw", this.getClass().getPackage().getName());
                    inputStream = getResources().openRawResource(id);
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                movies = getMoviesFromJson(forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.


            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
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
