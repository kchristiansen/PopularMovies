package com.example.kevin.popularmovies;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kchristiansen on 7/22/15.
 */
public class Movie {
    String mID;
    String mTitle;
    String mSynopsis;
    String mRating;
    String releaseDate;
    String mPosterUri;

    final static String ID = "id";
    final static String RATING = "vote_average";
    final static String TITLE = "title";
    final static String SYNOPSIS = "overview";
    final static String RELEASEDATE = "release_date";
    final static String POSTER = "poster_path";

    public static Movie newInstance(JSONObject jsonMovieObject) {
        Movie m = new Movie();
        try {

            m.mID = jsonMovieObject.getString(ID);
            m.mTitle = jsonMovieObject.getString(TITLE);
            m.mSynopsis = jsonMovieObject.getString(SYNOPSIS);
            m.mPosterUri = jsonMovieObject.getString(POSTER);
            m.releaseDate = jsonMovieObject.getString(RELEASEDATE);
            m.mRating = jsonMovieObject.getString(RATING);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return m;
    }

    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ID, mID);
            jsonObject.put(TITLE, mTitle);
            jsonObject.put(RATING, mRating);
            jsonObject.put(RELEASEDATE, releaseDate);
            jsonObject.put(POSTER, mPosterUri);
            jsonObject.put(SYNOPSIS, mSynopsis);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
