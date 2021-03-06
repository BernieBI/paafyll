package no.hiof.matsl.pfyll;


import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import no.hiof.matsl.pfyll.model.FragmentLogin;
import no.hiof.matsl.pfyll.model.FragmentMyAccount;
import no.hiof.matsl.pfyll.model.FragmentProducts;
import no.hiof.matsl.pfyll.model.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity  {
    String TAG = "MainActivity";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference users = database.getReference("users");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPrefHandler sharedPrefHandler = new SharedPrefHandler(this, "theme", "theme-cache"); // Retrieving user-selected theme from sharedpreferences
        setTheme(getResources().getIdentifier(sharedPrefHandler.getTheme(), "style", this.getPackageName())); //finding theme by the stored name.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CreateLayout();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    public void CreateLayout(){ // Dynamically populating tablayout depending on whether user is logged in or not.
        user = FirebaseAuth.getInstance().getCurrentUser();
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPagerid);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //Adding fragments

        adapter.AddFragment(new FragmentProducts(),"");
        if (user != null)
            adapter.AddFragment(new FragmentMyAccount(), "");
        else
            adapter.AddFragment(new FragmentLogin(), "");


        //Adapter setup
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.wines);
        tabLayout.getTabAt(1).setIcon(R.drawable.person);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //Verifying successfull login, restarts activity.
        super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                users.child(user.getUid()).child("Name").setValue(user.getDisplayName());
                finish();
                startActivity(getIntent());

            }
    }


}
