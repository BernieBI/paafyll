package no.hiof.matsl.pfyll;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CacheHandler {

    private Context context;
    private String key;
    private String filename;

    public CacheHandler(Context context, String key, String filename) {
        this.context = context;
        this.key = key;
        this.filename = filename;
    }
    public void setTheme(String themeName ){

        SharedPreferences sharedPref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = sharedPref.edit();
        editor.remove(key).commit();
        editor.putString(key, themeName);
        editor.commit();

    }
    public String getTheme( ){
        SharedPreferences sharedPref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        String response = sharedPref.getString(key , "Standard");
        return response;

    }
    public void setRecentProducts(ArrayList<String> recents){
        SharedPreferences sharedPref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;

        Gson gson = new Gson();
        recents.removeAll(Collections.singleton(null));

        String json = gson.toJson(recents);
        editor = sharedPref.edit();
        editor.remove(key).commit();
        editor.putString(key, json);
        editor.commit();

    }
    public ArrayList<String> getRecentProducts(){
        SharedPreferences sharedPref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String response= sharedPref.getString(key , "");
        ArrayList<String> recents = gson.fromJson(response,
                new TypeToken<List<String>>(){}.getType());
        return recents;
    }
}
