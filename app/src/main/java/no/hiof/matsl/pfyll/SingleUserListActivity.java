package no.hiof.matsl.pfyll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import no.hiof.matsl.pfyll.model.UserList;

public class SingleUserListActivity extends AppCompatActivity {
    String TAG = "SingleUserListActivity";

    private String listID;
    private TextView listName;

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
                final UserList list = dataSnapshot.getValue(UserList.class);
                Log.d(TAG, "List: " + list.getNavn());

                listName = findViewById(R.id.listName);

                listName.setText(list.getNavn());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        listsRef.addValueEventListener(listListener);

    }


}