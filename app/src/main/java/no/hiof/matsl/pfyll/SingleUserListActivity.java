package no.hiof.matsl.pfyll;

import android.app.FragmentManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import no.hiof.matsl.pfyll.model.FragmentProducts;
import no.hiof.matsl.pfyll.model.UserList;

public class SingleUserListActivity extends AppCompatActivity {
    String TAG = "SingleUserListActivity";

    static private String listID;
    private TextView listName;
    private UserList userList;
    //firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference listsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_list);

        Intent intent = getIntent();
        listID = intent.getStringExtra("UserListId");
        listsRef = database.getReference("userLists/" + listID);
        GetData();

    }

    private void GetData() {

        ValueEventListener listListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList = dataSnapshot.getValue(UserList.class);
                Log.d(TAG, "List: " + userList.getNavn());

                listName = findViewById(R.id.listName);
                listName.setText(userList.getNavn());
                StartFragment();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        listsRef.addValueEventListener(listListener);

    }

    private void StartFragment(){
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // set Fragmentclass Arguments
        FragmentProducts fragment = newInstance(userList.getProducts());
        fragmentTransaction.add(R.id.parent, fragment);
        fragmentTransaction.commit();


    }
    public static FragmentProducts newInstance(ArrayList<String> productList) {
        FragmentProducts fragment = new FragmentProducts();

        Bundle args = new Bundle();
        args.putStringArrayList("ProductsInList", productList );
        args.putString("userListId", listID);
        fragment.setArguments(args);

        return fragment;
    }


}