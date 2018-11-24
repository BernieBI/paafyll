package no.hiof.matsl.pfyll;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import no.hiof.matsl.pfyll.model.FragmentProducts;

public class RecentProductsActivity extends AppCompatActivity {
    String TAG = "recentProducts";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefHandler themeGetter = new SharedPrefHandler(this, "theme", "theme-cache");// Retrieving user-selected theme from sharedpreferences
        setTheme(getResources().getIdentifier(themeGetter.getTheme(), "style", this.getPackageName()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_products);

        SharedPrefHandler sharedPrefHandler = new SharedPrefHandler(this, "Recent Products", "LocalCache"); // Retrieving a list of recently watched products from sharedPreferences
        ArrayList<String> products = sharedPrefHandler.getRecentProducts();

        StartFragment(products);
    }

    private void StartFragment(ArrayList<String> products){

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        FragmentProducts fragment = newInstance(products); //Building new fragment of FragmentProducts
        fragmentTransaction.add(R.id.parent, fragment);
        fragmentTransaction.commit();

    }
    public static FragmentProducts newInstance(ArrayList<String> productList) {
        FragmentProducts fragment = new FragmentProducts();

        Bundle args = new Bundle(); //Passing the list of recent products to the new fragment
        args.putStringArrayList("preSetProducts", productList );
        fragment.setArguments(args);
        return fragment;
    }
}
