package no.hiof.matsl.pfyll;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.model.FragmentDrinklog;
import no.hiof.matsl.pfyll.model.FragmentUserList;
import no.hiof.matsl.pfyll.model.FragmentMyActivity;
import no.hiof.matsl.pfyll.model.FragmentProducts;
import no.hiof.matsl.pfyll.model.Product;
import no.hiof.matsl.pfyll.model.UserList;
import no.hiof.matsl.pfyll.model.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tabLayout);
        appBarLayout = findViewById(R.id.appBarid);
        viewPager = findViewById(R.id.viewPagerid);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //Adding fragments
        adapter.AddFragment(new FragmentProducts(),"Sortiment");
        adapter.AddFragment(new FragmentUserList(),"Lister");
        adapter.AddFragment(new FragmentMyActivity(),"Min aktivitet");
        adapter.AddFragment(new FragmentDrinklog(),"Drikkelogg");
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.secondaryColor));
        tabLayout.setTabTextColors(Color.parseColor("#727272"), Color.parseColor("#ffffff"));
        //Adapter setup
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }


}
