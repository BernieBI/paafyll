package no.hiof.matsl.pfyll.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    SharedPreferences mySharedPref;

    public SharedPref(Context context){
        mySharedPref = context.getSharedPreferences("filename",Context.MODE_PRIVATE);
    }

    public void setTheme(int state){
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putInt("Theme",state);
        editor.commit();
    }
    public Integer loadThemeState(){
        int state = mySharedPref.getInt("Theme", 0);
        return state;
    }
}
