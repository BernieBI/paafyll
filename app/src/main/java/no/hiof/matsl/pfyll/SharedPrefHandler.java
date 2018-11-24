package no.hiof.matsl.pfyll;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SharedPrefHandler { //Class for handling storing data to sharedPreferences

    private Context context;
    private String key;
    private String filename;

    public SharedPrefHandler(Context context, String key, String filename) {
        this.context = context;
        this.key = key;
        this.filename = filename;
    }
    public void setTheme(String themeName ){ //Storing theme name as String to sharedpreferences

        SharedPreferences sharedPref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = sharedPref.edit();
        editor.remove(key).commit();
        editor.putString(key, themeName);
        editor.commit();

    }
    public String getTheme( ){ //Retrieving theme name as string from sharedPreferences.
        SharedPreferences sharedPref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        String response = sharedPref.getString(key , "Standard");
        String[] themes = context.getResources().getStringArray(R.array.themes);
        int i = 0;
        while(i < themes.length){ //Verifying that the stored value still exists in strings.xml. if not, returnes the first available theme.
            if (themes[i].equals(response))
                return themes[i];
            i++;
        }
        return themes[0];

    }
    public void setRecentProducts(ArrayList<String> recents){ //Encoding arraylist as Json and storing in sharedPrefences
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
    public ArrayList<String> getRecentProducts(){//Retrieving Json and decoding back to arraylist. returning list of productIds
        SharedPreferences sharedPref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String response= sharedPref.getString(key , "");
        ArrayList<String> recents = gson.fromJson(response,
                new TypeToken<List<String>>(){}.getType());
        return recents;
    }
}
