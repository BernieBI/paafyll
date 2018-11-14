package no.hiof.matsl.pfyll;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import no.hiof.matsl.pfyll.adapter.UserListRecycleViewAdapter;
import no.hiof.matsl.pfyll.model.UserList;

public class UserListActivity extends AppCompatActivity {

    String TAG = "UserList";
    private RecyclerView recyclerView;
    private UserListRecycleViewAdapter userListAdapter;
    private FloatingActionButton addListBtn;
    private ArrayList<UserList> userLists = new ArrayList<>();


    private FirebaseUser user;
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userListRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        user = FirebaseAuth.getInstance().getCurrentUser();

        userListRef = database.getReference("users/" + user.getUid() + "/userLists");

        recyclerView = findViewById(R.id.userList_recycler_view);

        addListBtn = findViewById(R.id.addListButton);
        addListBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserListActivity.this);

                builder.setTitle("Lag ny liste");
                builder.setView(getLayoutInflater().inflate(R.layout.addlistfields,null))
                        .setPositiveButton("Lag ny liste", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText editText = ((AlertDialog)dialog).findViewById(R.id.newListName);
                                Log.d(TAG, "List name " + editText.getText());

                                if (editText.getText().length() == 0){
                                    Toast toast = Toast.makeText(UserListActivity.this, getString(R.string.require_name), Toast.LENGTH_LONG);
                                    toast.show();
                                    return;
                                }
                                UserList newList = new UserList();
                                newList.setNavn(editText.getText().toString());
                                userListRef.push().setValue(newList);

                                Log.d(TAG, "List created ");
                                Toast toast = Toast.makeText(UserListActivity.this, String.format("Listen %s ble opprettet!", newList.getNavn()), Toast.LENGTH_LONG);
                                toast.show();
                            }
                        })
                        .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        initRecyclerView();
    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init Recyclerview");
        userListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userLists.clear();
                findViewById(R.id.progressBar).setVisibility(View.GONE);

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    UserList list = child.getValue(UserList.class);
                    list.setId(child.getKey());
                    userLists.add(list);
                }
                if (userLists.size() > 0) {
                    passUserListsToView();
                    findViewById(R.id.emptyText).setVisibility(View.GONE);
                    findViewById(R.id.emptyText2).setVisibility(View.GONE);
                }
                else {
                    recyclerView.removeAllViewsInLayout();
                    findViewById(R.id.emptyText).setVisibility(View.VISIBLE);
                    findViewById(R.id.emptyText2).setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void passUserListsToView (){

        userListAdapter = new UserListRecycleViewAdapter(UserListActivity.this, userLists);
        recyclerView.setAdapter(userListAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(UserListActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

    }
}
