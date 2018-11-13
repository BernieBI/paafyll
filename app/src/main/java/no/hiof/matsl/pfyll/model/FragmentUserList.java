package no.hiof.matsl.pfyll.model;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.adapter.UserListRecycleViewAdapter;

public class FragmentUserList extends Fragment {
    View view;
    String TAG = "UserListFragment";
    private RecyclerView recyclerView;
    private UserListRecycleViewAdapter userListAdapter;
    private FloatingActionButton addListBtn;
    private ArrayList<UserList> userLists = new ArrayList<>();
    private Button logInButton;
    private TextView logInInfo;

    private FirebaseUser user;
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userListRef;

    public FragmentUserList(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_userlists,container,false);
        Log.d(TAG, "onCreate: Started ");

        user = FirebaseAuth.getInstance().getCurrentUser();

        userListRef = database.getReference("users/" + user.getUid() + "/userLists");

        recyclerView = view.findViewById(R.id.userList_recycler_view);

        addListBtn = view.findViewById(R.id.addListButton);
        addListBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Lag ny liste");
                builder.setView(inflater.inflate(R.layout.addlistfields,null))
                        .setPositiveButton("Lag ny liste", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText editText = ((AlertDialog)dialog).findViewById(R.id.newListName);
                                Log.d(TAG, "List name " + editText.getText());

                                if (editText.getText().length() == 0){
                                    Toast toast = Toast.makeText(getActivity(), getString(R.string.require_name), Toast.LENGTH_LONG);
                                    toast.show();
                                    return;
                                }
                                UserList newList = new UserList();
                                newList.setNavn(editText.getText().toString());

                                userListRef.push().setValue(newList);

                                Log.d(TAG, "List created ");
                                Toast toast = Toast.makeText(getActivity(), String.format("Listen %s ble opprettet!", newList.getNavn()), Toast.LENGTH_LONG);
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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        initRecyclerView();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onstop");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init Recyclerview");
        userListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userLists.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                        UserList list = child.getValue(UserList.class);
                        list.setId(child.getKey());
                        userLists.add(list);
                        Log.d(TAG, "List added " + list.getNavn());
                }
                if (userLists.size() > 0)
                    passUserListsToView();
                else
                    recyclerView.removeAllViewsInLayout();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void passUserListsToView (){

        userListAdapter = new UserListRecycleViewAdapter(getActivity(), userLists);
        recyclerView.setAdapter(userListAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

    }
}
