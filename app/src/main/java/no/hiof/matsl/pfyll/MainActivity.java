package no.hiof.matsl.pfyll;


import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

import java.util.Arrays;
import java.util.List;

import no.hiof.matsl.pfyll.model.FragmentLogin;
import no.hiof.matsl.pfyll.model.FragmentUserList;
import no.hiof.matsl.pfyll.model.FragmentMyActivity;
import no.hiof.matsl.pfyll.model.FragmentProducts;
import no.hiof.matsl.pfyll.model.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private ViewPager viewPager;
    private static int RC_SIGN_IN = 100;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference users = database.getReference("users");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        CreateLayout();

    }

    public void CreateLayout(){
        tabLayout = findViewById(R.id.tabLayout);
        appBarLayout = findViewById(R.id.appBarid);
        viewPager = findViewById(R.id.viewPagerid);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //Adding fragments
        adapter.AddFragment(new FragmentProducts(),"Sortiment");

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            adapter.AddFragment(new FragmentUserList(), "Lister");
            adapter.AddFragment(new FragmentMyActivity(), "Min aktivitet");
            Log.d(TAG,"Login, OK: " + user.getUid() );

        }else{
            adapter.AddFragment(new FragmentLogin(), "Logg inn");
        }

        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.secondaryColor));
        tabLayout.setTabTextColors(Color.parseColor("#727272"), Color.parseColor("#ffffff"));
        //Adapter setup
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG,"Login, OK" + user.getDisplayName());
                Log.d(TAG,"Response: " + response);
                users.child(user.getUid()).child("Name").setValue(user.getDisplayName());
                users.child(user.getUid()).child("Email").setValue(user.getEmail());

                finish();
                startActivity(getIntent());

            } else {

            }
    }

}
