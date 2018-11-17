package no.hiof.matsl.pfyll.adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.SingleProductActivity;
import no.hiof.matsl.pfyll.model.Product;
import no.hiof.matsl.pfyll.model.Review;


public class ReviewRecycleViewAdapter extends RecyclerView.Adapter<ReviewRecycleViewAdapter.ViewHolder> {
    private static final String TAG = "ReviewRVAdapter";

    private ArrayList<Review> reviews;
    private LayoutInflater inflater;
    private Context context;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private FirebaseDatabase firebase = FirebaseDatabase.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public ReviewRecycleViewAdapter(Context context, ArrayList<Review> reviews) {
        this.reviews = reviews;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_list_userreview, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Review current_review = reviews.get(position);

        DocumentReference docRef = database.collection("Produkter").document(current_review.getProductId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        final Product product = new Product().documentToProduct(document);

                        Glide.with(context)
                                .asBitmap()
                                .load(product.getBildeUrl())
                                .into(holder.productImage);
                        holder.progressBar.setVisibility(View.GONE);
                        holder.productName.setText(product.getVarenavn());
                        holder.reviewText.setText(current_review.getReviewText());
                        holder.reviewValue.setRating(current_review.getReviewValue());

                        holder.removeReviewBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage( R.string.confirm_delete_review)
                                        .setPositiveButton("Slett", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                DatabaseReference allReviewsRef = firebase.getReference("userReviews/" + product.getId() +"/"+ user.getUid() );
                                                DatabaseReference userReviewRef = firebase.getReference("users/" + user.getUid() + "/reviews/" + current_review.getUserReviewId());
                                                Log.d(TAG, "user level"+userReviewRef + " all: " + allReviewsRef );
                                                allReviewsRef.removeValue();
                                                userReviewRef.removeValue();
                                                notifyItemRemoved(position);
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
                        holder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View view, int position, boolean isLoading) {

                            }
                        });

                        holder.setItemClickListener(new ItemClickListener(){
                            @Override
                            public void onClick(View view, int position, boolean isLoading) {

                                //Starting single product activity
                                Intent singleProductIntent = new Intent(context, SingleProductActivity.class);
                                singleProductIntent.putExtra("ProductID", product.getId());
                                context.startActivity(singleProductIntent);

                            }
                        });
                    } else {
                        return;
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView productName;
        TextView reviewText;
        ImageView productImage;
        ProgressBar progressBar;
        RatingBar reviewValue;
        ImageButton removeReviewBtn;

        private ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            reviewValue = itemView.findViewById(R.id.productRatingBar);
            progressBar = itemView.findViewById(R.id.progressBar);
            productImage = itemView.findViewById(R.id.productImage);
            reviewText = itemView.findViewById(R.id.reviewText);
            removeReviewBtn = itemView.findViewById(R.id.removeReviewBtn);
            itemView.setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickUserReviewener){
            this.itemClickListener = itemClickUserReviewener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }

    }

}
