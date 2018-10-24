package no.hiof.matsl.pfyll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import no.hiof.matsl.pfyll.model.UserList;

public class SingleListActivity extends AppCompatActivity {
    String TAG = "SingleListActivity";

    private String listID;
    //firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference listsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_list);


        Intent intent = getIntent();
        listID = intent.getStringExtra("ListID");
        listsRef = database.getReference("Lists/" + listID);
        GetData();
    }

    private void GetData() {

        ValueEventListener listListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final UserList list = dataSnapshot.getValue(UserList.class);
                Log.d(TAG, "List: ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        listsRef.addValueEventListener(listListener);

    }


}