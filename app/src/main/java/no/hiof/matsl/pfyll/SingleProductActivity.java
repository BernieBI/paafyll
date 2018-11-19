package no.hiof.matsl.pfyll;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.Rating;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.razerdp.widget.animatedpieview.data.SimplePieInfo;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.hiof.matsl.pfyll.model.Product;
import no.hiof.matsl.pfyll.model.Review;
import no.hiof.matsl.pfyll.model.SharedPref;
import no.hiof.matsl.pfyll.model.UserList;
import no.hiof.matsl.pfyll.model.UserReview;

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

    /* */
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference productsRef;
    /* */

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
    private TextView productName, productTaste, productPrice, productLiterPrice, productVolume, drinkWithhead, reviewCount, productRating;
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

    private int white;

    private ArrayList<UserList> userLists = new ArrayList<>();
    private ArrayList<String> options = new ArrayList<>();

    private boolean hasDrinkWiths = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CacheHandler themeGetter = new CacheHandler(this, "theme", "theme-cache");
        setTheme(getResources().getIdentifier(themeGetter.getTheme(), "style", this.getPackageName()));
        super.onCreate(savedInstanceState);

        theme = SingleProductActivity.this.getTheme();

        setContentView(R.layout.activity_single_product);
        dpi = getResources().getDisplayMetrics().density;

        //Populating text fields and other
        //white = getResources().getColor(R.color.white);
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
        productRating = findViewById(R.id.productRating);
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

        if (productID == -1) {
            Toast toast = Toast.makeText(SingleProductActivity.this, "Fant ikke produktet", Toast.LENGTH_LONG);
            toast.show();
            onBackPressed();
        }


        //getting product and list data from firebase
        getProductData();
        getProductData();
        reviewRef = database.getReference("userReviews/" + productID);
        getAllReviews();
        if (user != null){
            userListRef = database.getReference("users/" + user.getUid() + "/userLists");


            userReviewRef = database.getReference("users/" + user.getUid() + "/reviews");
            getUserLists();
            getUserReviews();
            submitReview();

        }else{
            reviewButton.setVisibility(View.GONE);
            addToListBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast toast = Toast.makeText(SingleProductActivity.this, getString(R.string.list_require_login), Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        //Adding product to recently viewed producs
        CacheHandler cacheHandler = new CacheHandler(this, "Recent Products", "LocalCache");
        ArrayList<String> recentProducts;
        if (cacheHandler.getRecentProducts() == null)
            recentProducts = new ArrayList<>();
        else
            recentProducts = cacheHandler.getRecentProducts();

        if (recentProducts.size() >= 20)
            recentProducts.remove(0);

        if (recentProducts.contains(productID+""))
            recentProducts.remove(productID+"");

        recentProducts.add(productID+"");
        cacheHandler.setRecentProducts(recentProducts);

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

    private void getUserLists() {

        ChildEventListener userListListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                UserList list = dataSnapshot.getValue(UserList.class);
                list.setId(dataSnapshot.getKey());
                userLists.add(list);
                options.add(list.getNavn());
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
        userListRef.addChildEventListener(userListListener);


        addToListBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (options.size() == 0){
                    Toast toast = Toast.makeText(SingleProductActivity.this, getString(R.string.have_no_lists), Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(SingleProductActivity.this);
                    builder.setTitle(R.string.selectList);
                    builder.setItems(options.toArray(new CharSequence[options.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ArrayList<String> products = new ArrayList<>();

                            if (userLists.get(which).getProducts() != null) {

                                if (!userLists.get(which).addProduct(productID + "")) {
                                    Toast toast = Toast.makeText(SingleProductActivity.this, String.format("%s %s!", getString(R.string.already_exists), userLists.get(which).getNavn()), Toast.LENGTH_SHORT);
                                    toast.show();
                                    return;
                                }
                            } else {
                                products.add(productID + "");
                                userLists.get(which).setProducts(products);
                            }

                            userListRef.child(userLists.get(which).getId()).child("products").setValue(userLists.get(which).getProducts());
                            Toast toast = Toast.makeText(SingleProductActivity.this, String.format("%s %s!", getString(R.string.add_success), userLists.get(which).getNavn()), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                    builder.show();
                }
            }
        });


    }
    private void getAllReviews() {

        reviewRef.orderByChild("index").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentswrapper.removeAllViews();
                productReviews.clear();
                for (DataSnapshot reviewSnapShot: dataSnapshot.getChildren()) {
                    if (reviewSnapShot.exists())
                        productReviews.add( reviewSnapShot.getValue(Review.class));
                }
                if (productReviews.size() > 0){
                    float total = 0;
                    for (Review review : productReviews){
                        total += review.getReviewValue();
                        createComment( review.getReviewText(), review.getUser(),review.getReviewValue());
                    }
                    DecimalFormat df = new DecimalFormat("#.#");
                    String rating = df.format(total / productReviews.size());
                    ratingBar.setRating((float)total / productReviews.size());

                    reviewCount.setText(String.format("%s anmeldelse" +( productReviews.size()>1 ? "r" : "") , productReviews.size()));
                    productRating.setText("(" + rating + ")");
                    commentswrapper.setVisibility(View.VISIBLE);
                    findViewById(R.id.reviewsHeader).setVisibility(View.VISIBLE);

                }else{
                    commentswrapper.setVisibility(View.GONE);
                    findViewById(R.id.reviewsHeader).setVisibility(View.INVISIBLE);
                    reviewCount.setText(getString(R.string.no_reviews));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    public void getUserReviews(){
        userReviewRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                reviewedProducts.add(dataSnapshot.getValue() + "");
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void submitReview(){

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (reviewedProducts.contains(productID+"")) {
                    Toast toast = Toast.makeText(SingleProductActivity.this, getString(R.string.already_reviewed), Toast.LENGTH_LONG);
                    toast.show();

                }else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(SingleProductActivity.this);
                    builder.setTitle("Fortell hva du synes!");
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
                    input.setHint("Skriv en kommentar");
                    layout.setPadding((int)(30*dpi), (int)(8*dpi), (int)(30*dpi), (int)(5*dpi));
                    layout.addView(newRatingBar);
                    layout.addView(input);
                    builder.setView(layout);
                    builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            reviewText = input.getText().toString();
                            reviewValue = newRatingBar.getRating();
                            Log.d(TAG, "onClick rating: " + reviewValue);
                            if (reviewText == "" ){
                                Toast toast = Toast.makeText(SingleProductActivity.this, getString(R.string.require_comment), Toast.LENGTH_LONG);
                                toast.show();
                            }

                            String username = "ukjent";
                            if (user.getDisplayName() != null)
                                username = user.getDisplayName();

                            int index = productReviews.size() > 0 ? productReviews.get(productReviews.size()-1).getIndex()+1 : 0;

                            Review review = new Review(reviewText, reviewValue, username, index);
                            reviewedProducts.add(productID + "");
                            reviewRef.child(user.getUid()).setValue(review);
                            userReviewRef.setValue(reviewedProducts);
                            Toast toast = Toast.makeText(SingleProductActivity.this, getString(R.string.review_success), Toast.LENGTH_LONG);
                            toast.show();


                        }
                    });
                    builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    private void getProductData(){
        final DocumentReference productDoc = productRef.document(productID+"");
        productDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        findViewById(R.id.loadOverlay).setVisibility(View.GONE);

                        product = new Product().documentToProduct(document);

                        if (product == null){
                            Toast toast = Toast.makeText(SingleProductActivity.this, "Fant ikke produktet", Toast.LENGTH_LONG);
                            toast.show();
                            onBackPressed();
                            return;
                        }
                        product.setBildeUrl(product.getVarenummer());

                        Glide.with(SingleProductActivity.this)
                                .asBitmap()
                                .load(product.getBildeUrl())
                                .into(productImage);
                        productImage.setContentDescription(product.getVarenavn());

                        productName.setText(product.getVarenavn());
                        //productName.setBackgroundColor(white);

                        productPrice.setText(String.format( "%s %s", getString(R.string.currency), product.getPris() ));
                        //productPrice.setBackgroundColor(white);

                        productTaste.setText(product.getSmak());
                        //productTaste.setBackgroundColor(white);

                        productVolume.setText(String.format("%s l", product.getVolum()));

                        productLiterPrice.setText(String.format("%s kr/l", product.getLiterpris() ));
                        //productLiterPrice.setBackgroundColor(white);

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
                                Log.d(TAG, "onClick: ProductsActivity started");
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(product.getVareurl()));
                                startActivity(browserIntent);

                            }
                        });


                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void createComment(String text, String userName, Float rating){

        Timestamp time = new Timestamp(System.currentTimeMillis());

        ConstraintLayout comment = new ConstraintLayout(this);
        comment.setId(1 + time.getNanos());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int)(8*dpi), (int)(4*dpi), (int)(8*dpi), (int)(4*dpi));
        comment.setLayoutParams(params);

        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.brightOverlay));

        comment.setBackground(shape);
        comment.setElevation((int)(3*dpi));
        commentswrapper.addView(comment);

        TextView headerTextView = new TextView(this);
        headerTextView.setText(userName);
        headerTextView.setTextSize(20);
        headerTextView.setId(2 + time.getNanos());
        headerTextView.setTypeface(null, Typeface.BOLD);
        comment.addView(headerTextView);

        RatingBar newRatingBar = new RatingBar(new ContextThemeWrapper(SingleProductActivity.this, R.style.ratingBarThemeSmall), null, 0);
        newRatingBar.setRating(rating);
        newRatingBar.setNumStars(5);
        newRatingBar.setId( 3 + time.getNanos());
        newRatingBar.setIsIndicator(true);
        newRatingBar.setStepSize(1);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams ((int)(80*dpi), (int)(30*dpi)); //Width, Height
        newRatingBar.setLayoutParams(params2);
        comment.addView(newRatingBar);

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setId(4 + time.getNanos());
        if (!text.equals(""))
            comment.addView(textView);

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
    public void createTextView(LinearLayout parent, String text, String headerText){
        if (text.equals("") || text.equals(null) || text.contains("Øvrige"))
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

        if (image != 0)
            hasDrinkWiths = true;
        else
            view.setVisibility(View.GONE);


        view.setImageResource(image);
        view.setContentDescription(imageAlt);

    }
    public void createPieCharts(String headerText, int value,AnimatedPieView pieView){

        if(((LinearLayout)pieView.getParent()).findViewWithTag(headerText) != null)
            return;
        if (value == 0) {
            ((LinearLayout)pieView.getParent()).setVisibility(View.GONE);
            return;
        }

        if (((LinearLayout)pieView.getParent().getParent()).getVisibility() == View.GONE)
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
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
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