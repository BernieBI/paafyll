package no.hiof.matsl.pfyll;


import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import no.hiof.matsl.pfyll.model.FragmentLogin;
import no.hiof.matsl.pfyll.model.FragmentMyAccount;
import no.hiof.matsl.pfyll.model.FragmentProducts;
import no.hiof.matsl.pfyll.model.SharedPref;
import no.hiof.matsl.pfyll.model.ViewPagerAdapter;



public class MainActivity extends AppCompatActivity  {
    String TAG = "MainActivity";
    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private ViewPager viewPager;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseReference users = database.getReference("users");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPref sharedPref = new SharedPref(this);
        if (sharedPref.loadThemeState()==0){
            setTheme(R.style.AppTheme);
        }
        if (sharedPref.loadThemeState()==1) {
            setTheme(R.style.Night);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CreateLayout();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    public void CreateLayout(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        tabLayout = findViewById(R.id.tabLayout);
        appBarLayout = findViewById(R.id.appBarid);
        viewPager = findViewById(R.id.viewPagerid);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //Adding fragments

        adapter.AddFragment(new FragmentProducts(),"");
        if (user != null)
            adapter.AddFragment(new FragmentMyAccount(), "");
        else
            adapter.AddFragment(new FragmentLogin(), "");


        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primaryLightColor));
        tabLayout.setTabTextColors(Color.parseColor("#727272"), Color.parseColor("#ffffff"));
        //Adapter setup
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.wines);
        tabLayout.getTabAt(1).setIcon(R.drawable.person);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                users.child(user.getUid()).child("Name").setValue(user.getDisplayName());
                finish();
                startActivity(getIntent());

            } else {

            }
    }


}
