package no.hiof.matsl.pfyll;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import no.hiof.matsl.pfyll.model.FragmentProducts;

public class RecentProductsActivity extends AppCompatActivity {
    String TAG = "recentProducts";
    ArrayList<String> products;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_products);
        Log.d(TAG, "onCreate: ");

        CacheHandler cacheHandler = new CacheHandler(this, "Recent Products", "LocalCache");
        Log.d(TAG, "recent products: " + cacheHandler.getRecentProducts());
        products = cacheHandler.getRecentProducts();

        StartFragment();
    }

    private void StartFragment(){
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // set Fragmentclass Arguments
        FragmentProducts fragment = newInstance(products);
        fragmentTransaction.add(R.id.parent, fragment);
        fragmentTransaction.commit();


    }
    public static FragmentProducts newInstance(ArrayList<String> productList) {
        FragmentProducts fragment = new FragmentProducts();

        Bundle args = new Bundle();
        args.putStringArrayList("RecentProducts", productList );
        fragment.setArguments(args);
        return fragment;
    }
}
