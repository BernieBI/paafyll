package no.hiof.matsl.pfyll;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import no.hiof.matsl.pfyll.adapter.ReviewRecycleViewAdapter;
import no.hiof.matsl.pfyll.model.Review;
import no.hiof.matsl.pfyll.model.SharedPref;

public class MyReviewsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReviewRecycleViewAdapter reviewAdapter;
    private GridLayoutManager gridLayoutManager;
    private ArrayList<Review> userReviews = new ArrayList<>();
    private ArrayList<String> reviewedProducts = new ArrayList<>();
    private ArrayList<String> userReviewIds = new ArrayList<>();

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
        SharedPref sharedPref = new SharedPref(this);
        if (sharedPref.loadThemeState()==0){
            setTheme(R.style.AppTheme);
        }
        if (sharedPref.loadThemeState()==1) {
            setTheme(R.style.Night);
        }
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
        passReviews();

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
                if (!dataSnapshot.exists())
                    return;
                Review current_review = dataSnapshot.getValue(Review.class);
                Log.d(TAG, "Review: " + current_review.getReviewText());
                current_review.setId(dataSnapshot.getKey());
                userReviews.add(current_review);
                current_review.setProductId(reviewedProducts.get(userReviews.size()-1));
                current_review.setUserReviewId(userReviewIds.get(userReviews.size()-1));
                reviewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
    }


    private void getReviewedProducts() {
        userReviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userReviews.clear();
                reviewedProducts.clear();
                userReviewIds.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    reviewedProducts.add(child.getValue() + "");
                    userReviewIds.add(child.getKey()+"");
                }
                Collections.reverse(reviewedProducts);
                Collections.reverse(userReviewIds);
                for (String review : reviewedProducts){
                    reviewRef.child(review).child(user.getUid()).addListenerForSingleValueEvent(reviewListener);
                    Log.d(TAG, "id: " + review);
                }

                if (reviewedProducts.size() == 0){
                    recyclerView.removeAllViewsInLayout();
                    findViewById(R.id.emptyMessage).setVisibility(View.VISIBLE);
                    findViewById(R.id.emptyReviewMessage).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.emptyMessage).setVisibility(View.GONE);
                    findViewById(R.id.emptyReviewMessage).setVisibility(View.GONE);
                }
                passReviews();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void passReviews(){
        findViewById(R.id.progressBar).setVisibility(View.GONE);

        recyclerView.setAdapter(reviewAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        reviewAdapter.notifyDataSetChanged();
    }
}
