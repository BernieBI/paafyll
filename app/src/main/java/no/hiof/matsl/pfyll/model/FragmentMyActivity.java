package no.hiof.matsl.pfyll.model;

import android.content.Intent;
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
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.RecentProductsActivity;
import no.hiof.matsl.pfyll.adapter.ReviewRecycleViewAdapter;

public class FragmentMyActivity extends Fragment {
    private RecyclerView recyclerView;
    private ReviewRecycleViewAdapter reviewAdapter;
    private GridLayoutManager gridLayoutManager;
    private Button logoutButton;
    private ArrayList<Review> userReviews = new ArrayList<>();
    private ArrayList<String> reviewedProducts = new ArrayList<>();
    private Review current_review;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    //firebase
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser user;
    private DatabaseReference userReviewRef;
    private DatabaseReference reviewRef;
    ValueEventListener reviewListener;
    View view;
    String TAG = "MyActivityFragment";

    public FragmentMyActivity(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_myactivity,container,false);
        Log.d(TAG, "onCreate: Started ");
        recyclerView = view.findViewById(R.id.review_recycler_view);

        user = FirebaseAuth.getInstance().getCurrentUser();

        Button recentProductBtn = view.findViewById(R.id.recentProductsButton);
        recentProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RecentProductsActivity.class);
                startActivity(intent);
            }
        });
        Button logOutButton = view.findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              auth.signOut();
              getActivity().finish();
              getActivity().startActivity(getActivity().getIntent());
            }
        });

        userReviewRef = database.getReference("users/" + user.getUid() + "/reviews");
        reviewRef = database.getReference("userReviews");


        reviewAdapter = new ReviewRecycleViewAdapter(getActivity(), userReviews);
        gridLayoutManager = new GridLayoutManager(getActivity(), 1);

        createReviewListener();
        //Henter alle produktid'er fra brukeren
        getReviewedProducts();
        if (userReviews.size() > 0)
            passReviews();


        return view;
    }

    private void createReviewListener() {
        reviewListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                current_review = dataSnapshot.getValue(Review.class);
                userReviews.add(current_review);
                reviewAdapter.notifyItemInserted(0);
                Log.d(TAG, "Review: " + current_review.getReviewText());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
    }


    private void getReviewedProducts() {
        ChildEventListener userReviewListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                if ( !reviewedProducts.contains(dataSnapshot.getValue()+"")) {

                    //Hvis den ikke finnes fra før så henter vi en ny
                    reviewRef.child(dataSnapshot.getValue() + "").child(user.getUid()).addListenerForSingleValueEvent(reviewListener);
                    reviewedProducts.add(dataSnapshot.getValue() + "");
                }
                passReviews();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };
        userReviewRef.addChildEventListener(userReviewListener);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        reviewAdapter.notifyDataSetChanged();
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


    public void passReviews(){
        recyclerView.setAdapter(reviewAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        reviewAdapter.notifyDataSetChanged();
    }

}
