package com.example.kevin.popularmovies;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kchristiansen on 8/20/15.
 */
public class Review {
    final static String ID = "id";
    final static String AUTHOR = "author";
    final static String CONTENT = "content";

    String id;
    String author;
    String content;

    public Review(){}

    public static Review newInstance(JSONObject jsonReviewObject){
        Review r = new Review();
        try{
            r.id = jsonReviewObject.getString(ID);
            r.author = jsonReviewObject.getString(AUTHOR);
            r.content = jsonReviewObject.getString(CONTENT);
            return r;
        }
        catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put(ID,id);
            jsonObject.put(AUTHOR,author);
            jsonObject.put(CONTENT,content);
            return jsonObject.toString();
        }
        catch (JSONException e){
            e.printStackTrace();
            return "";
        }
    }
}
