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

public class MyReviewsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReviewRecycleViewAdapter reviewAdapter;
    private GridLayoutManager gridLayoutManager;
    private ArrayList<Review> userReviews = new ArrayList<>(); // List of actual reviews. Beeing sent to adapter and recyclerview
    private ArrayList<String> reviewedProducts = new ArrayList<>(); // List of product IDs of the users reviewed products
    private ArrayList<String> userProductReviewIndex = new ArrayList<>(); // List of indexes of the users product IDs. Is paired with reviewedProducts for ordering.

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
        SharedPrefHandler themeGetter = new SharedPrefHandler(this, "theme", "theme-cache"); // Retrieving user-selected theme from sharedpreferences
        setTheme(getResources().getIdentifier(themeGetter.getTheme(), "style", this.getPackageName()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        recyclerView = findViewById(R.id.review_recycler_view);

        user = FirebaseAuth.getInstance().getCurrentUser();

        userReviewRef = database.getReference("users/" + user.getUid() + "/reviews"); //Path to current users review list
        reviewRef = database.getReference("userReviews"); //Path to all reviews from all users


        reviewAdapter = new ReviewRecycleViewAdapter(MyReviewsActivity.this, userReviews);
        gridLayoutManager = new GridLayoutManager(MyReviewsActivity.this, 1);

        getUsersReviewedProducts(); // Retrieving list of product IDs from users reviewed products
        createReviewListener(); //Creating ValueEventListener for each of the IDs retrieved

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        reviewAdapter.notifyDataSetChanged();
    }

    private void getUsersReviewedProducts() {
        userReviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //emptying lists of reviews to avoid duplicates
                userReviews.clear();
                reviewedProducts.clear();
                userProductReviewIndex.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    reviewedProducts.add(child.getValue() + ""); // Storing the product ID
                    userProductReviewIndex.add(child.getKey()+""); // Storing index of the product ID
                }
                Collections.reverse(reviewedProducts); //Reversing both arrays to display from newest to oldest.
                Collections.reverse(userProductReviewIndex);

                for (String review : reviewedProducts){ //Creating a ListenerForSingleValueEvent for each of the product IDs. Adding the current user-id and creating an absolute path to the desired review
                    reviewRef.child(review).child(user.getUid()).addListenerForSingleValueEvent(reviewListener);
                }

                if (reviewedProducts.size() == 0){ //If the user has no reviews, display message and clear the recyclerview in case the reviews was just deleted
                    recyclerView.removeAllViewsInLayout();
                    findViewById(R.id.emptyMessage).setVisibility(View.VISIBLE);
                    findViewById(R.id.emptyReviewMessage).setVisibility(View.VISIBLE);

                }else { //Hide messages if user has lists
                    findViewById(R.id.emptyMessage).setVisibility(View.GONE);
                    findViewById(R.id.emptyReviewMessage).setVisibility(View.GONE);
                }
                passReviews();  //Notifying adapter the data has changed

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void createReviewListener() { // Getting the actual review data
        reviewListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    return;

                Review current_review = dataSnapshot.getValue(Review.class);
                current_review.setId(dataSnapshot.getKey());
                userReviews.add(current_review); // Adding the review to the list
                current_review.setProductId(reviewedProducts.get(userReviews.size()-1)); // using the previous set lists to set the right product ID for the list.
                current_review.setUserIndex(userProductReviewIndex.get(userReviews.size()-1)); // using the previous set indexes to ensure the reviews is displayed in the right order. This fixed a bug causing wrong order when the user created new reviews after app startup.
                reviewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
    }

    public void passReviews(){
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        recyclerView.setAdapter(reviewAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        reviewAdapter.notifyDataSetChanged();
    }
}
