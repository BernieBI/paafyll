package no.hiof.matsl.pfyll;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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

import no.hiof.matsl.pfyll.adapter.ReviewRecycleViewAdapter;
import no.hiof.matsl.pfyll.model.Review;

public class MyReviewsActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        recyclerView = findViewById(R.id.review_recycler_view);

        user = FirebaseAuth.getInstance().getCurrentUser();

        userReviewRef = database.getReference("users/" + user.getUid() + "/reviews");
        reviewRef = database.getReference("userReviews");


        reviewAdapter = new ReviewRecycleViewAdapter(MyReviewsActivity.this, userReviews);
        gridLayoutManager = new GridLayoutManager(MyReviewsActivity.this, 1);

        createReviewListener();
        //Henter alle produktid'er fra brukeren
        getReviewedProducts();

        if (userReviews.size() > 0) {
            passReviews();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        reviewAdapter.notifyDataSetChanged();
    }
    private void createReviewListener() {
        reviewListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                current_review = dataSnapshot.getValue(Review.class);
                current_review.setId(dataSnapshot.getKey());
                userReviews.add(current_review);
                current_review.setProductId(reviewedProducts.get(userReviews.size()-1));
                reviewAdapter.notifyItemInserted(userReviews.size()-1);
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
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                if (reviewedProducts.size() == 0){
                    findViewById(R.id.emptyMessage).setVisibility(View.VISIBLE);
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
    public void passReviews(){
        recyclerView.setAdapter(reviewAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        reviewAdapter.notifyDataSetChanged();
    }
}
