package no.hiof.matsl.pfyll;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefHandler themeGetter = new SharedPrefHandler(this, "theme", "theme-cache");
        setTheme(getResources().getIdentifier(themeGetter.getTheme(), "style", this.getPackageName()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_list);

        Intent intent = getIntent();
        listID = intent.getStringExtra("UserListId");
        listsRef = database.getReference("users/" + user.getUid() + "/userLists/" + listID);
        GetData();

        ImageButton editList = findViewById(R.id.editList);
        editList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SingleUserListActivity.this);

                builder.setTitle(getResources().getString(R.string.changeList));
                builder.setView(getLayoutInflater().inflate(R.layout.addlistfields,null))
                        .setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText editText = ((AlertDialog)dialog).findViewById(R.id.newListName);

                                if (editText.getText().length() == 0){
                                    Toast toast = Toast.makeText(SingleUserListActivity.this, getString(R.string.require_name), Toast.LENGTH_LONG);
                                    toast.show();
                                    return;
                                }
                                userList.setNavn(editText.getText().toString());
                                listsRef.setValue(userList);

                                Toast.makeText(SingleUserListActivity.this, getResources().getString(R.string.list_change_success), Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.abort), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Intent intent = getIntent();
        listID = intent.getStringExtra("UserListId");
    }

    private void GetData() { // Getting current list object from Firebase

        ValueEventListener listListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList = dataSnapshot.getValue(UserList.class);

                if (userList == null)
                    return;

                listName = findViewById(R.id.listName);
                listName.setText(userList.getNavn());

                if (userList.getProducts() == null) {
                    findViewById(R.id.emptyText).setVisibility(View.VISIBLE);
                    return;
                }

                startFragment();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        listsRef.addValueEventListener(listListener);

    }

    private void startFragment(){ //Starting instance of FragmentProducts with list of product ID's. Also sending list ID to allow removing products

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // set Fragmentclass Arguments
        if (userList.getProducts() == null)
            return;

        FragmentProducts fragment = newInstance(userList.getProducts());
        fragmentTransaction.add(R.id.parent, fragment);
        fragmentTransaction.commitAllowingStateLoss();


    }
    public static FragmentProducts newInstance(ArrayList<String> productList) {
        FragmentProducts fragment = new FragmentProducts();
        Bundle args = new Bundle();
        args.putStringArrayList("preSetProducts", productList );
        args.putString("userListId", listID);
        fragment.setArguments(args);

        return fragment;
    }


}