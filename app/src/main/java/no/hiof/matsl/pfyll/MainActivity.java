package no.hiof.matsl.pfyll;

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
        adapter.AddFragment(new FragmentProducts(),"Products");
        adapter.AddFragment(new FragmentUserList(),"Favorites");
        adapter.AddFragment(new FragmentMyActivity(),"My activity");
        adapter.AddFragment(new FragmentDrinklog(),"Drink log");
        //Adapter setup
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

       // createLists();
    }

    private void createLists(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userListsRef = database.getReference();

        ArrayList<UserList> lists = new ArrayList<>();
        lists.add(new UserList("Favorittliste", "1"));
        lists.add(new UserList("Fylleliste", "2"));
        lists.add(new UserList("Vinliste", "3"));
        lists.add(new UserList("Drittliste", "4"));

        for(UserList list : lists){
            userListsRef.child("userLists").child(list.getId()).setValue(list);
        }
    }
}
