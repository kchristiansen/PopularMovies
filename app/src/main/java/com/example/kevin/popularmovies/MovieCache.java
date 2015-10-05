package com.example.kevin.popularmovies;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.jakewharton.disklrucache.DiskLruCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by kchristiansen on 9/10/15.
 */
public class MovieCache {
    private static DiskLruCache getCache(final Context mContext) {
        final int MAX_CACHE_SIZE = 1024 * 1024;
        final File filesDir = mContext.getFilesDir();
        final File movieCache = new File(filesDir, "Movies");
        if (!movieCache.exists()) {
            movieCache.mkdir();
        }
        final int appVersion = getVersion(mContext);
        try {
            final DiskLruCache cache = DiskLruCache.open(movieCache, appVersion, 1, MAX_CACHE_SIZE);
            return cache;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getVersion (final Context context) {
        try {
            final PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static Movie GetMovieFromCache(final Context context, final String key) {
        final DiskLruCache cache = getCache(context);
        Movie movie = null;
        if (cache != null) {
            try {
                final DiskLruCache.Snapshot snapshot = cache.get(key);
                if (snapshot != null) {
                    movie = Movie.newInstance(new JSONObject(snapshot.getString(0)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return movie;
    }

    public static void CacheMovie (final Context context, final Movie movie, final String key) throws IOException {
        final DiskLruCache cache = getCache(context);

        if (cache != null) {
            final DiskLruCache.Editor editor = cache.edit(key);
            if (editor != null) {
                editor.set(0, movie.toJSON());
                editor.commit();
            }
        }
    }
}
