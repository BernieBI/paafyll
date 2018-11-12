package no.hiof.matsl.pfyll;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.hiof.matsl.pfyll.model.Product;
import no.hiof.matsl.pfyll.model.Review;
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

    private DatabaseReference userListRef;
    private DatabaseReference userReviewRef;


    //views
    private LinearLayout productDetails1, productDetails2, productDetails3;
    private TextView productName, productTaste, productPrice, productLiterPrice, productVolume, drinkWithhead;
    private ImageView productImage, drinkWith1, drinkWith2, drinkWith3;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);
        Log.d(TAG, "ID " + productID);


        //Populating text fields and other
        white = getResources().getColor(R.color.white);

        productName = findViewById(R.id.productName);
        productTaste = findViewById(R.id.productTaste);
        productPrice = findViewById(R.id.productPrice);
        productLiterPrice = findViewById(R.id.productLiterPrice);
        productVolume = findViewById(R.id.productVolume);
        productDetails1 = findViewById(R.id.productDetails1);
        productDetails2 = findViewById(R.id.productDetails2);
        productDetails3 = findViewById(R.id.productDetails3);

        productImage = findViewById(R.id.productImage);
        drinkWithhead = findViewById(R.id.drinkWith);
        drinkWith1 = findViewById(R.id.drinkWith1);
        drinkWith2 = findViewById(R.id.drinkWith2);
        drinkWith3 = findViewById(R.id.drinkWith3);

        pieChartSweetness = findViewById(R.id.pieChartSweetness);
        pieChartFreshness = findViewById(R.id.pieChartFreshness);
        pieChartFullness = findViewById(R.id.pieChartFullness);
        pieChartTannin = findViewById(R.id.pieChartTannin);
        pieChartBitterness = findViewById(R.id.pieChartBitterness);

        if (productID == -1) {
            Toast toast = Toast.makeText(SingleProductActivity.this, "Fant ikke produktet", Toast.LENGTH_LONG);
            toast.show();
            onBackPressed();
        }

        Intent intent = getIntent();
        productID = intent.getIntExtra("ProductID", -1);
        productsRef = database.getReference("Products/" + productID);



        //getting product and list data from firebase
        GetData();

        if (user != null){
            userListRef = database.getReference("users/" + user.getUid() + "/userLists");
            userReviewRef = database.getReference("users/" + user.getUid() + "/userReviews");
            GetUserLists();
         }

        //Adding product to recently viewed producs
        CacheHandler cacheHandler = new CacheHandler(this, "Recent Products", "LocalCache");
        ArrayList<String> recentProducts;
        if (cacheHandler.getRecentProducts() == null)
            recentProducts = new ArrayList<>();
        else
            recentProducts = cacheHandler.getRecentProducts();

        if (recentProducts.size() >= 24)
            recentProducts.remove(0);

        if (recentProducts.contains(productID+""))
            recentProducts.remove(productID+"");

        recentProducts.add(productID+"");
        cacheHandler.setRecentProducts(recentProducts);

        Log.d(TAG, "recents: " + cacheHandler.getRecentProducts());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");
        productID = savedInstanceState.getInt("productID");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        outState.putInt("productID", productID);
    }

    private void GetUserLists() {

        ChildEventListener userListListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                UserList list = dataSnapshot.getValue(UserList.class);
                list.setId(dataSnapshot.getKey());
                userLists.add(list);
                options.add(list.getNavn());
                Log.d(TAG, "List added to dialog " + options);
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

        //Getting list data'
        addToListBtn = findViewById(R.id.addToListButton);
        addToListBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SingleProductActivity.this);
                builder.setTitle(R.string.selectList);
                builder.setItems(options.toArray(new CharSequence[options.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ArrayList<String> products = new ArrayList<>();

                        if (userLists.get(which).getProducts() != null){

                            if (!userLists.get(which).addProduct(productID+"")) {
                                Toast toast = Toast.makeText(SingleProductActivity.this,  String.format("%s %s!",getString(R.string.already_exists), userLists.get(which).getNavn()), Toast.LENGTH_LONG);
                                toast.show();
                                return;
                            }
                        }else{
                            products.add(productID+"");
                            userLists.get(which).setProducts(products);
                        }

                        userListRef.child(userLists.get(which).getId()).child("products").setValue( userLists.get(which).getProducts());
                        Toast toast = Toast.makeText(SingleProductActivity.this, String.format("%s %s!",getString(R.string.add_success), userLists.get(which).getNavn()), Toast.LENGTH_LONG);
                        toast.show();
                        Log.d(TAG, "Products in list: " + userLists.get(which).getProducts());
                    }
                });
                builder.show();
            }
        });
    }
    private void GetData(){
        DocumentReference productDoc = productRef.document(productID+"");
        productDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        product = createProductObject(document);

                        Log.d(TAG, "Product name: " + product.getVarenavn());

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
                        productName.setBackgroundColor(white);

                        productPrice.setText(String.format( "%s %s", getString(R.string.currency), product.getPris() ));
                        productPrice.setBackgroundColor(white);

                        productTaste.setText(product.getSmak());
                        productTaste.setBackgroundColor(white);

                        productLiterPrice.setText(String.format("%s %s %s", getString(R.string.currency), product.getLiterpris(), getString(R.string.product_perLiter)) );
                        productLiterPrice.setBackgroundColor(white);

                        productVolume.setText(String.format( "%s %s", product.getVolum(), getString(R.string.centiLiter) ));

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

                        Button reviewButton = findViewById(R.id.reviewButton);

                        reviewButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SingleProductActivity.this);
                                builder.setTitle("Gi din anbefaling");
                                LinearLayout layout = new LinearLayout(SingleProductActivity.this);
                                layout.setOrientation(LinearLayout.VERTICAL);

                                final RatingBar ratingBar = new RatingBar(SingleProductActivity.this);
                                ratingBar.setNumStars(5);
                                ratingBar.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                layout.addView(ratingBar);

                                final EditText input = new EditText(SingleProductActivity.this);
                                input.setInputType(InputType.TYPE_CLASS_TEXT );
                                layout.addView(input);
                                builder.setView(layout);


                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        reviewText = input.getText().toString();
                                        reviewValue = ratingBar.getRating();
                                        Review review = new Review(reviewText,reviewValue);
                                        UserReview userReview = new UserReview(productID+"", review);

                                        userReviewRef.push().setValue(userReview);
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


    //pass header text as "" if not to use
    public void createTextView(LinearLayout parent, String text, String headerText){
        if (text.equals("") || text.equals(null) || text.contains("Øvrige"))
             return;

        LinearLayout listElement = new LinearLayout(this);
        listElement.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        listElement.setOrientation(LinearLayout.HORIZONTAL);

        parent.addView(listElement);

        TextView headerTextView = new TextView(this);
        headerTextView.setText(String.format("%s: ",headerText));
        headerTextView.setTypeface(null, Typeface.BOLD);

        listElement.addView(headerTextView);

        TextView textView = new TextView(this);
        textView.setText(text);
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

        if (value == 0) {
            ((LinearLayout)pieView.getParent()).setVisibility(View.GONE);
            return;
        }

        if (((LinearLayout)pieView.getParent().getParent()).getVisibility() == View.GONE)
            ((LinearLayout)pieView.getParent().getParent()).setVisibility(View.VISIBLE);

        int remaining = 10 - value;


        //Adding chart and text to section
        TextView headerTextView = new TextView(this);
        headerTextView.setText(headerText);
        headerTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout)pieView.getParent()).addView(headerTextView);

        //Configuring chart
        AnimatedPieViewConfig config = new AnimatedPieViewConfig();
        config.addData(new SimplePieInfo(value, getResources().getColor(R.color.secondaryDarkColor)));
        config.addData(new SimplePieInfo(remaining, getResources().getColor(R.color.blank)));
        config.canTouch(false);
        config.strokeMode(false);
        config.pieRadius(40);
        config.duration(100);
        config.autoSize(true);
        pieView.applyConfig(config);
        pieView.start();
    }

    private Product createProductObject (DocumentSnapshot doc) {
        Product product = new Product(
                Integer.parseInt(doc.getId()),
                stringify(doc.get("Alkohol")),
                stringify(doc.get("Argang")),
                stringify(doc.get("Biodynamisk")),
                stringify(doc.get("Bitterhet")),
                stringify(doc.get("Butikkategori")),
                stringify(doc.get("Datotid")),
                stringify(doc.get("Distributor")),
                stringify(doc.get("Distrikt")),
                stringify(doc.get("Emballasjetype")),
                stringify(doc.get("Fairtrade")),
                stringify(doc.get("Farge")),
                stringify(doc.get("Friskhet")),
                stringify(doc.get("Fylde")),
                stringify(doc.get("Garvestoffer")),
                stringify(doc.get("Gluten_lav_pa")),
                stringify(doc.get("Grossist")),
                stringify(doc.get("Korktype")),
                stringify(doc.get("Kosher")),
                stringify(doc.get("Lagringsgrad")),
                stringify(doc.get("Land")),
                stringify(doc.get("Literpris")),
                stringify(doc.get("Lukt")),
                stringify(doc.get("Metode")),
                stringify(doc.get("Miljosmart_emballasje")),
                stringify(doc.get("Okologisk")),
                stringify(doc.get("Passertil01")),
                stringify(doc.get("Passertil02")),
                stringify(doc.get("Passertil03")),
                stringify(doc.get("Pris")),
                stringify(doc.get("Produktutvalg")),
                stringify(doc.get("Produsent")),
                stringify(doc.get("Rastoff")),
                stringify(doc.get("Smak")),
                stringify(doc.get("Sodme")),
                stringify(doc.get("Sukker")),
                stringify(doc.get("Syre")),
                stringify(doc.get("Underdistrikt")),
                stringify(doc.get("Varenavn")),
                stringify(doc.get("Varenummer")),
                stringify(doc.get("Varetype")),
                stringify(doc.get("Vareurl")),
                stringify(doc.get("Volum")),
                stringify(doc.get("HovedGTIN"))
        );
        product.setBildeUrl(product.getVarenummer());
        return product;
    }
    private String stringify(Object object) {
        return (object == null)
                ? ""
                : object.toString();
    }
}