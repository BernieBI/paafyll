package no.hiof.matsl.pfyll;


import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.model.FragmentLogin;
import no.hiof.matsl.pfyll.model.FragmentMyAccount;
import no.hiof.matsl.pfyll.model.FragmentProducts;
import no.hiof.matsl.pfyll.model.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
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
    private void removeNonDrinkables(){

        //Gaveesker
        //Gaveposer
        //Tilbehør

        db.collection("Produkter").whereEqualTo("Varetype", "Gaveesker")
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        deleteItem(document);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void deleteItem(QueryDocumentSnapshot document) {

        db.collection("Produkter").document(document.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }


}
