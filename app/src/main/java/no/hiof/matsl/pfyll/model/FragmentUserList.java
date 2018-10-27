package no.hiof.matsl.pfyll.model;
import android.arch.lifecycle.Observer;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.adapter.ProductDataSourceFactory;
import no.hiof.matsl.pfyll.adapter.ProductRecycleViewAdapter;
import no.hiof.matsl.pfyll.adapter.UserListRecycleViewAdapter;

public class FragmentUserList extends Fragment {
    View view;
    String TAG = "UserListFragment";
    private RecyclerView recyclerView;
    private UserListRecycleViewAdapter userListAdapter;
    private ArrayList<UserList> userLists = new ArrayList<>();

    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userListsRef = database.getReference("userLists");

    public FragmentUserList(){


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_userlists,container,false);
        Log.d(TAG, "onCreate: Started ");

        recyclerView = view.findViewById(R.id.userList_recycler_view);
        initRecyclerView();
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
        userListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userLists.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                        UserList list = child.getValue(UserList.class);
                        userLists.add(list);
                        Log.d(TAG, "List added " + list.getNavn());
                }
                passUserListsToView(userLists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void passUserListsToView (ArrayList<UserList> userLists){

        userListAdapter = new UserListRecycleViewAdapter(getActivity(), userLists);
        recyclerView.setAdapter(userListAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

    }
}
