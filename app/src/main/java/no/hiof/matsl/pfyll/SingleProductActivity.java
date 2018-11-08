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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import no.hiof.matsl.pfyll.model.UserList;

public class SingleProductActivity extends AppCompatActivity {
    String TAG = "SingleProductActivity";

    private String productID;
    private Product product;
    //firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference productsRef;

    private DatabaseReference userListRef = database.getReference("userLists");
    private ValueEventListener userListListener;

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

        Intent intent = getIntent();
        productID = intent.getStringExtra("ProductID");
        productsRef = database.getReference("Products/" + productID);
        userListRef = database.getReference("userLists");

        //getting product and list data from firebase
        GetData();
        GetUserLists();

        //Adding product to recently viewed producs
        CacheHandler cacheHandler = new CacheHandler(this, "Recent Products", "LocalCache");
        ArrayList<String> products;
        if (cacheHandler.getRecentProducts() == null)
            products = new ArrayList<>();
        else
            products = cacheHandler.getRecentProducts();

        if (products.size() >= 24)
            products.remove(0);

        if (products.contains(productID))
            products.remove(productID);

        products.add(productID);
        cacheHandler.setRecentProducts(products);

        Log.d(TAG, "recents: " + cacheHandler.getRecentProducts());


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");
        productID = savedInstanceState.getString("productID");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        outState.putString("productID", productID);
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

                            if (!userLists.get(which).addProduct(productID)) {
                                Toast toast = Toast.makeText(SingleProductActivity.this,  String.format("%s %s!",getString(R.string.already_exists), userLists.get(which).getNavn()), Toast.LENGTH_LONG);
                                toast.show();
                                return;
                            }
                        }else{
                            products.add(productID);
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

    private void GetData() {

        ValueEventListener productListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                product = dataSnapshot.getValue(Product.class);

                if (product == null){
                    Toast toast = Toast.makeText(SingleProductActivity.this, "Fant ikke produktet", Toast.LENGTH_LONG);
                    toast.show();
                    onBackPressed();
                    return;
                }

                Log.d(TAG, "Product: " + product.getHovedGTIN());
                productID = product.getHovedGTIN();
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast toast = Toast.makeText(SingleProductActivity.this, "Fant ikke produktet", Toast.LENGTH_LONG);
                toast.show();
                onBackPressed();
            }
        };
        productsRef.addValueEventListener(productListener);

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

}