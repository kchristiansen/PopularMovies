package com.example.kevin.popularmovies;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kchristiansen on 8/13/15.
 */
public class Trailer {
    final static String KEY = "key";
    final static String NAME = "name";
    final static String SITE = "site";

    String site;
    String name;
    String key;
    String Uri;

    public Trailer(){}

    public static Trailer newInstance(JSONObject jsonTrailerObject){
        Trailer t = new Trailer();
        try{
            t.key = jsonTrailerObject.getString(KEY);
            t.name = jsonTrailerObject.getString(NAME);
            t.site = jsonTrailerObject.getString(SITE);

            switch (t.site){
                case "YouTube":
                    t.Uri = "http://www.youtube.com/watch?v=" + t.key;
                    break;
                default:
                    Log.e("Trailer", "Unknown Site: " + t.site);
            }

        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }

        return t;
    }

    public String toJSON(){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put(KEY, key);
            jsonObject.put(SITE,site);
            jsonObject.put(NAME,name);
            return jsonObject.toString();
        } catch (JSONException e){
            e.printStackTrace();
            return "";
        }
    }
}
