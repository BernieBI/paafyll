package no.hiof.matsl.pfyll;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.data.SimplePieInfo;

import java.util.ArrayList;
import no.hiof.matsl.pfyll.model.Product;
import no.hiof.matsl.pfyll.model.Review;
import no.hiof.matsl.pfyll.model.UserList;

import static android.view.View.GONE;

public class SingleProductActivity extends AppCompatActivity {
    String TAG = "SingleProductActivity";
    private String reviewText ="";
    private float reviewValue = 0;

    private int productID;
    private Product product;

    //firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference productRef = db.collection("Produkter");
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference productsRef;

    TypedValue typedValue = new TypedValue();
    Resources.Theme theme;
    private DatabaseReference userListRef;
    private DatabaseReference userReviewRef;
    private DatabaseReference reviewRef;
    private ArrayList<String> reviewedProducts = new ArrayList<>();
    private ArrayList<Review> productReviews = new ArrayList<>();
    float dpi;

    //views
    private LinearLayout productDetails1, productDetails2, productDetails3;
    private TextView productName, productTaste, productPrice, productLiterPrice, productVolume, reviewCount;
    private ImageView productImage, drinkWith1, drinkWith2, drinkWith3;
    private Button reviewButton;
    private RatingBar ratingBar;
    private LinearLayout commentswrapper;
    FloatingActionButton addToListBtn;

    //PieCharts
    AnimatedPieView pieChartSweetness;
    AnimatedPieView pieChartFreshness;
    AnimatedPieView pieChartFullness;
    AnimatedPieView pieChartTannin;
    AnimatedPieView pieChartBitterness;

    private ArrayList<UserList> userLists = new ArrayList<>();
    private ArrayList<String> options = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefHandler themeGetter = new SharedPrefHandler(this, "theme", "theme-cache");// Retrieving user-selected theme from sharedpreferences
        setTheme(getResources().getIdentifier(themeGetter.getTheme(), "style", this.getPackageName()));
        super.onCreate(savedInstanceState);

        theme = SingleProductActivity.this.getTheme();

        setContentView(R.layout.activity_single_product);
        dpi = getResources().getDisplayMetrics().density;

        //Initiation Views
        reviewButton = findViewById(R.id.reviewButton);
        productName = findViewById(R.id.productName);
        productTaste = findViewById(R.id.productTaste);
        productPrice = findViewById(R.id.productPrice);
        productLiterPrice = findViewById(R.id.productLiterPrice);
        productVolume = findViewById(R.id.productVolume);
        productDetails1 = findViewById(R.id.productDetails1);
        productDetails2 = findViewById(R.id.productDetails2);
        productDetails3 = findViewById(R.id.productDetails3);
        addToListBtn = findViewById(R.id.addToListButton);
        ratingBar = findViewById(R.id.productRatingBar);
        ratingBar.setRating(0);
        commentswrapper = findViewById(R.id.reviewComments);
        reviewCount = findViewById(R.id.reviewCount);
        productImage = findViewById(R.id.productImage);
        drinkWith1 = findViewById(R.id.drinkWith1);
        drinkWith2 = findViewById(R.id.drinkWith2);
        drinkWith3 = findViewById(R.id.drinkWith3);
        pieChartSweetness = findViewById(R.id.pieChartSweetness);
        pieChartFreshness = findViewById(R.id.pieChartFreshness);
        pieChartFullness = findViewById(R.id.pieChartFullness);
        pieChartTannin = findViewById(R.id.pieChartTannin);
        pieChartBitterness = findViewById(R.id.pieChartBitterness);

        Intent intent = getIntent();
        productID = intent.getIntExtra("ProductID", -1);
        productsRef = database.getReference("Products/" + productID);

        if (productID == -1) { //If no product ID is set, -1 is default. Display message and return to previous activity
            Toast toast = Toast.makeText(SingleProductActivity.this, getResources().getString(R.string.no_match), Toast.LENGTH_LONG);
            toast.show();
            onBackPressed();
        }


        //getting product and list data from firebase. Not checking for internet connection as some data might be cached
        getProductData();

        reviewRef = database.getReference("userReviews/" + productID);

        if (isNetworkAvailable()){

            getAllReviews(); // only getting reviews if has internet connection.

            if (user != null){ // only retrieving user data if the current user is logged in.
                userListRef = database.getReference("users/" + user.getUid() + "/userLists"); // Current users lists
                userReviewRef = database.getReference("users/" + user.getUid() + "/reviews"); // Current users reviewed products
                getUserLists();
                getUserReviews();
                submitReview();

            }else{ //Hiding reviewbutton and displayin toast to not-logged in user
                reviewButton.setVisibility(GONE);
                addToListBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Toast toast = Toast.makeText(SingleProductActivity.this, getString(R.string.list_require_login), Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
            }
        }else{ //hidinng reviewbutton and list button if no internet connection
            reviewButton.setVisibility(GONE);
            addToListBtn.hide();
        }

        //Adding product to recently viewed products
        SharedPrefHandler sharedPrefHandler = new SharedPrefHandler(this, "Recent Products", "LocalCache");
        ArrayList<String> recentProducts;
        if (sharedPrefHandler.getRecentProducts() == null)
            recentProducts = new ArrayList<>();
        else
            recentProducts = sharedPrefHandler.getRecentProducts();

        if (recentProducts.size() >= 20) // removing oldest product if count is more or equal to 20
            recentProducts.remove(0);

        if (recentProducts.contains(productID+"")) //if product is already in recents list, its moved to the top.
            recentProducts.remove(productID+"");

        recentProducts.add(productID+"");
        sharedPrefHandler.setRecentProducts(recentProducts); // storing new list of recent products

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        productID = savedInstanceState.getInt("productID");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("productID", productID);
    }

    private boolean isNetworkAvailable() { // Hentet fra https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
        ConnectivityManager connectivityManager
                = (ConnectivityManager) SingleProductActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getUserLists() { // Retrieving all of current users  product lists.

        userListRef.addChildEventListener( new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                UserList list = dataSnapshot.getValue(UserList.class);
                list.setId(dataSnapshot.getKey());
                userLists.add(list);
                options.add(list.getName()); // added to options array user for Alertdialog.
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        addToListBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (options.size() == 0){ //Notifying if user has no lists
                    Toast toast = Toast.makeText(SingleProductActivity.this, getString(R.string.have_no_lists), Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(SingleProductActivity.this);
                    builder.setTitle(R.string.selectList);
                    builder.setItems(options.toArray(new CharSequence[options.size()]), new DialogInterface.OnClickListener() { // populating alertDialog with list name
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ArrayList<String> products = new ArrayList<>();

                            if (userLists.get(which).getProducts() != null) {

                                if (!userLists.get(which).addProduct(productID + "")) { //Notifying if selected list already contains current product.
                                    Toast.makeText(SingleProductActivity.this, String.format("%s %s!", getString(R.string.already_exists), userLists.get(which).getName()), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else { // If list is empty, add current product without check
                                products.add(productID + "");
                                userLists.get(which).setProducts(products);
                            }

                            userListRef.child(userLists.get(which).getId()).child("products").setValue(userLists.get(which).getProducts()); // Update list in firebase
                            Toast.makeText(SingleProductActivity.this, String.format("%s %s!", getString(R.string.add_success), userLists.get(which).getName()), Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();
                }
            }
        });

    }
    private void getAllReviews() { // Getting all reviews to current product

        reviewRef.orderByChild("index").addValueEventListener(new ValueEventListener() { //Ordering by custom field "Index" to ensure oldest to newest sorting
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentswrapper.removeAllViews(); //Removing all reviews on change to avoid duplicates
                productReviews.clear();
                for (DataSnapshot reviewSnapShot: dataSnapshot.getChildren()) { // adding all reviews to list
                    if (reviewSnapShot.exists()) {
                        Review review = reviewSnapShot.getValue(Review.class);
                        review.setId(reviewSnapShot.getKey());
                        productReviews.add(review);
                    }

                }
                if (productReviews.size() > 0){

                    float total = 0;
                    for (Review review : productReviews){ //counting total score of review
                        total += review.getReviewValue();
                        review.getId();
                        createComment( review );
                    }
                    ratingBar.setRating(total / productReviews.size());
                    reviewCount.setText(String.format("%s %s" +( productReviews.size()>1 ? "r" : "") , productReviews.size(), getResources().getString(R.string.review)));

                    commentswrapper.setVisibility(View.VISIBLE);
                    findViewById(R.id.reviewsHeader).setVisibility(View.VISIBLE);

                }else{ //Hiding reviewSection if no reviews exist
                    commentswrapper.setVisibility(GONE);
                    findViewById(R.id.reviewsHeader).setVisibility(View.INVISIBLE);
                    reviewCount.setText(getString(R.string.no_reviews));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    public void getUserReviews(){ // Retrieving product ids of current users reviews
        userReviewRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                reviewedProducts.add(dataSnapshot.getValue() + "");
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled( @NonNull DatabaseError databaseError) {
            }
        });
    }
    public void submitReview(){ // Submitting new review on current product from current user

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (reviewedProducts.contains(productID+"")) { // notifying and exiting function if product is already reviewed
                    Toast.makeText(SingleProductActivity.this, getString(R.string.already_reviewed), Toast.LENGTH_LONG).show();
                    return;
                }

                // Building AlertDialog with custom ratingbar and EditText
                AlertDialog.Builder builder = new AlertDialog.Builder(SingleProductActivity.this);
                builder.setTitle(getResources().getString(R.string.tell_others_what_you_think));
                LinearLayout layout = new LinearLayout(SingleProductActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final RatingBar newRatingBar = new RatingBar(new ContextThemeWrapper(SingleProductActivity.this, R.style.ratingBarTheme), null, 0);
                newRatingBar.setRating(1);
                newRatingBar.setNumStars(5);
                newRatingBar.setStepSize(1);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams ((int)(160*dpi), ViewGroup.LayoutParams.WRAP_CONTENT); //Width, Height
                newRatingBar.setLayoutParams(params);

                final EditText input = new EditText(SingleProductActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint(getResources().getString(R.string.write_comment));
                layout.setPadding((int)(30*dpi), (int)(8*dpi), (int)(30*dpi), (int)(5*dpi));
                layout.addView(newRatingBar);
                layout.addView(input);
                builder.setView(layout);

                builder.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        reviewText = input.getText().toString().trim();
                        reviewValue = newRatingBar.getRating();

                        if (reviewText.equals("")){ // Veryfying comment input.
                            Toast.makeText(SingleProductActivity.this, getString(R.string.require_comment), Toast.LENGTH_LONG).show();
                        }
                        // Creating index of new review. To display reviews in correct order even if some is deleted
                        int index = productReviews.size() > 0 ? productReviews.get(productReviews.size()-1).getIndex()+1 : 0;

                        Review review = new Review(reviewText, reviewValue, index);

                        reviewedProducts.add(productID + "");
                        reviewRef.child(user.getUid()).setValue(review); // adding review object to userReviews node in firebase
                        userReviewRef.setValue(reviewedProducts); // adding product id to users list

                        Toast.makeText(SingleProductActivity.this, getString(R.string.review_success), Toast.LENGTH_LONG).show();

                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.abort), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private void getProductData(){ //Retrieving all current product data from Firestore
        final DocumentReference productDoc = productRef.document(productID+"");
        productDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        findViewById(R.id.loadOverlay).setVisibility(GONE);

                        product = new Product().documentToProduct(document);

                        if (product == null){ // returning to Previous if product does not exist
                            Toast toast = Toast.makeText(SingleProductActivity.this, getResources().getString(R.string.no_match), Toast.LENGTH_LONG);
                            toast.show();
                            onBackPressed();
                            return;
                        }
                        product.setBildeUrl(product.getVarenummer()); // creating image url

                        //Getting image if has internet connection. If not setting placeholder
                        if (isNetworkAvailable()) {
                            RequestOptions requestOptions = new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .fallback(getResources().getDrawable(R.drawable.bottle))
                            .error(getResources().getDrawable(R.drawable.bottle));

                            Glide.with(SingleProductActivity.this)
                                    .asBitmap()
                                    .load(product.getBildeUrl())
                                    .apply(requestOptions)
                                    .into(productImage);
                        }else
                            productImage.setImageDrawable(getResources().getDrawable(R.drawable.bottle));

                        //Setting productHeader textViews
                        productImage.setContentDescription(product.getVarenavn());
                        productName.setText(product.getVarenavn());
                        productPrice.setText(String.format( "%s %s", getString(R.string.currency), product.getPris() ));
                        productTaste.setText(product.getSmak());
                        productVolume.setText(String.format("%s l", product.getVolum()));
                        productLiterPrice.setText(String.format("%s kr/l", product.getLiterpris() ));

                        //Adding info related to product contents
                        createTextView(productDetails1, String.format("%s%%", product.getAlkohol()), getString(R.string.product_alkohol));
                        createTextView(productDetails1, product.getArgang() , getString(R.string.product_year));
                        createTextView(productDetails1, product.getLagringsgrad(), getString(R.string.product_storage));
                        createTextView(productDetails1, product.getFarge(), getString(R.string.product_color));
                        createTextView(productDetails1, product.getLukt(), getString(R.string.product_smell));
                        createTextView(productDetails1, product.getRastoff(), getString(R.string.product_feedstock));

                        //Adding info related to product production
                        createTextView(productDetails2, product.getProdusent(), getString(R.string.product_producer));
                        createTextView(productDetails2, product.getMetode(), getString(R.string.product_method));
                        createTextView(productDetails2, product.getLand() , getString(R.string.product_country));
                        createTextView(productDetails2, String.format("%s, %s",product.getDistrikt(), product.getUnderdistrikt()) , getString(R.string.product_district));

                        //Other info
                        createTextView(productDetails3, product.getEmballasjetype() , getString(R.string.product_packaging));
                        createTextView(productDetails3, product.getButikkategori() , getString(R.string.product_category));
                        createTextView(productDetails3, product.getGrossist() , getString(R.string.product_wholesaler));

                        setDrinkWiths(drinkWith1, product.getPassertil01());
                        setDrinkWiths(drinkWith2, product.getPassertil02());
                        setDrinkWiths(drinkWith3, product.getPassertil03());

                        //Creating animated pie charts
                        createPieCharts(getString(R.string.product_Sweetness), Integer.parseInt(product.getSodme()), pieChartSweetness);
                        createPieCharts(getString(R.string.product_freshness), Integer.parseInt(product.getFriskhet()), pieChartFreshness);
                        createPieCharts(getString(R.string.product_fullness), Integer.parseInt(product.getFylde()), pieChartFullness);
                        createPieCharts(getString(R.string.product_tannin), Integer.parseInt(product.getGarvestoffer()), pieChartTannin);
                        createPieCharts(getString(R.string.product_bitterness), Integer.parseInt(product.getGarvestoffer()), pieChartBitterness);

                        //Button for opening product in browser
                        Button productsButton = findViewById(R.id.webButton);
                        productsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(product.getVareurl()));
                                startActivity(browserIntent);
                            }
                        });


                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void createComment(final Review review){ //Creating and populating comment from user reviews related to current product

        final String text = review.getReviewText();
        final float rating = review.getReviewValue();

        DatabaseReference commentUser = database.getReference("users/" + review.getId() + "/Name");
        commentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;

                //comment parent
                ConstraintLayout comment = new ConstraintLayout(SingleProductActivity.this);
                comment.setId(View.generateViewId());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins((int)(8*dpi), (int)(4*dpi), (int)(8*dpi), (int)(4*dpi));
                comment.setLayoutParams(params);
                comment.setPadding((int)(8*dpi), (int)(4*dpi), (int)(8*dpi), (int)(8*dpi));
                GradientDrawable shape =  new GradientDrawable();
                shape.setCornerRadius( 8 );
                shape.setColor(getResources().getColor(R.color.brightOverlay));

                comment.setBackground(shape);
                comment.setElevation((int)(3*dpi));
                commentswrapper.addView(comment);

                //Username of review
                TextView headerTextView = new TextView(SingleProductActivity.this);
                LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                headerParams.setMargins((int)(0*dpi), (int)(0*dpi), (int)(0*dpi), (int)(0*dpi));
                headerTextView.setText(dataSnapshot.getValue().toString());
                headerTextView.setTextSize(20);
                headerTextView.setId(View.generateViewId());
                headerTextView.setTypeface(null, Typeface.BOLD);
                comment.addView(headerTextView);

                //Ratingbar of review
                RatingBar newRatingBar = new RatingBar(new ContextThemeWrapper(SingleProductActivity.this, R.style.ratingBarThemeSmall), null, 0);
                newRatingBar.setRating(rating);
                newRatingBar.setNumStars(5);
                newRatingBar.setId(View.generateViewId());
                newRatingBar.setIsIndicator(true);
                newRatingBar.setStepSize(1);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams ((int)(80*dpi), (int)(30*dpi)); //Width, Height
                newRatingBar.setLayoutParams(params2);
                comment.addView(newRatingBar);

                //Comment text of review
                TextView textView = new TextView(SingleProductActivity.this);
                textView.setText(text);
                textView.setPadding((int)(8*dpi), (int)(4*dpi), (int)(8*dpi), (int)(8*dpi));
                textView.setId(View.generateViewId());
                if (!text.equals(""))
                    comment.addView(textView);

                //Setting constraints
                ConstraintSet set = new ConstraintSet();
                set.clone(comment);
                set.connect(headerTextView.getId(), ConstraintSet.TOP, comment.getId(), ConstraintSet.TOP,  20);
                set.connect(headerTextView.getId(), ConstraintSet.LEFT, comment.getId(), ConstraintSet.LEFT,  20);
                set.connect(newRatingBar.getId(), ConstraintSet.TOP, comment.getId(), ConstraintSet.TOP,  20);
                set.connect(newRatingBar.getId(), ConstraintSet.RIGHT, comment.getId(), ConstraintSet.RIGHT,  20);
                if (text != "")
                    set.connect(textView.getId(), ConstraintSet.TOP, headerTextView.getId(), ConstraintSet.BOTTOM,  20);
                set.applyTo(comment);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
    public void createTextView(LinearLayout parent, String text, String headerText){
        if (text.equals("") || text.contains("Øvrige"))
            return;

        LinearLayout listElement = new LinearLayout(this);
        listElement.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        listElement.setOrientation(LinearLayout.HORIZONTAL);

        parent.addView(listElement);
        theme.resolveAttribute(R.attr.colorSecondaryText, typedValue, true);
        @ColorInt int color = typedValue.data;
        TextView headerTextView = new TextView(this);
        headerTextView.setText(String.format("%s: ",headerText));
        headerTextView.setTextColor(color);
        headerTextView.setTypeface(null, Typeface.BOLD);


        listElement.addView(headerTextView);

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(color);
        listElement.addView(textView);
    }

    public void setDrinkWiths(ImageView view, String value) {
        int image = 0;
        String imageAlt = value;

        if (value.contains("Dessert"))
            image = R.drawable.dessert;

        if (value.contains("Fisk"))
            image = R.drawable.fisk;

        if (value.contains("Lyst kjøtt"))
            image = R.drawable.kylling;

        if (value.contains("Lam og sau"))
            image = R.drawable.lam;

        if (value.contains( "Ost" ))
            image = R.drawable.ost;

        if (value.contains("Skalldyr"))
            image = R.drawable.skalldyr;

        if (value.contains("Smavilt"))
            image = R.drawable.smavilt;

        if (value.contains("Storfe"))
            image = R.drawable.storfe;

        if (value.contains("Storvilt"))
            image = R.drawable.storvilt;

        if (value.contains("Svinekjøtt"))
            image = R.drawable.svin;

        if (value.contains("Aperitiff"))
            image = R.drawable.aperitiff;

        if (image == 0)
            view.setVisibility(GONE);

        theme.resolveAttribute(R.attr.colorPrimaryText, typedValue, true); //Setting image color to match the current theme
        @ColorInt int color = typedValue.data;
        view.setColorFilter(color);
        view.setImageResource(image);
        view.setContentDescription(imageAlt);

    }
    public void createPieCharts(String headerText, int value,AnimatedPieView pieView){ // Library source: https://github.com/razerdp/AnimatedPieView

        if (value == 0) {
            ((LinearLayout)pieView.getParent()).setVisibility(GONE); // hiding view if has no value
            return;
        }

        if (((LinearLayout)pieView.getParent().getParent()).getVisibility() == GONE) //displaying entire piechartsection if this is the first pieChart
            ((LinearLayout)pieView.getParent().getParent()).setVisibility(View.VISIBLE);

        int remaining = 10 - value;

        theme.resolveAttribute(R.attr.colorPrimaryText, typedValue, true);
        @ColorInt int color = typedValue.data;

        //Adding chart and text to section
        TextView headerTextView = new TextView(this);
        headerTextView.setText(headerText);
        headerTextView.setTag(headerText);
        headerTextView.setTextColor(color);
        headerTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout)pieView.getParent()).addView(headerTextView);

        //Configuring chart
        AnimatedPieViewConfig config = new AnimatedPieViewConfig();
        theme.resolveAttribute(R.attr.colorPrimaryText, typedValue, true);
        color = typedValue.data;
        config.addData(new SimplePieInfo(value, color));
        theme.resolveAttribute(R.attr.colorPrimaryLight, typedValue, true);
        color = typedValue.data;
        config.addData(new SimplePieInfo(remaining, color));
        config.canTouch(false);
        config.strokeMode(false);
        config.splitAngle(2);
        config.pieRadius(40);
        config.duration(1000);
        config.autoSize(true);
        pieView.applyConfig(config);
        pieView.start();
    }

}