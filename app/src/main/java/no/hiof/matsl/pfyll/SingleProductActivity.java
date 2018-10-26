package no.hiof.matsl.pfyll;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.model.Product;
import no.hiof.matsl.pfyll.model.UserList;

public class SingleProductActivity extends AppCompatActivity {
    String TAG = "SingleProductActivity";

    private String productID;
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
    private int secondaryColor;

    private ArrayList<UserList> userLists = new ArrayList<>();
    private ArrayList<String> options = new ArrayList<>();

    private boolean hasDrinkWiths = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        secondaryColor = getResources().getColor(R.color.primaryLightColor);

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

        Intent intent = getIntent();
        productID = intent.getStringExtra("ProductID");
        productsRef = database.getReference("Products/" + productID);
        userListRef = database.getReference("userLists");

        GetData();
        GetUserLists();


    }
    private void GetUserLists() {
        userListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userLists.clear();
                options.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    UserList list = child.getValue(UserList.class);
                    userLists.add(list);
                    options.add(list.getNavn());
                    Log.d(TAG, "List added to dialog " + options);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        userListRef.addValueEventListener(userListListener);

        //Getting list data'
        addToListBtn = findViewById(R.id.addToListButton);
        addToListBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SingleProductActivity.this);
                builder.setTitle(R.string.selectList);
                builder.setItems(options.toArray(new CharSequence[options.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Products: " + userLists.get(which).getProducts());

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
                final Product product = dataSnapshot.getValue(Product.class);
                Log.d(TAG, "Product: " + product.getVarenummer());

                product.setBildeUrl(product.getVarenummer());

                Glide.with(SingleProductActivity.this)
                        .asBitmap()
                        .load(product.getBildeUrl())
                        .into(productImage);
                productImage.setContentDescription(product.getVarenavn());

                productName.setText(product.getVarenavn());
                productName.setBackgroundColor(secondaryColor);

                productPrice.setText(String.format( "%s %s", getString(R.string.currency), product.getPris() ));
                productPrice.setBackgroundColor(secondaryColor);


                    productTaste.setText(product.getSmak());
                    productTaste.setBackgroundColor(secondaryColor);

                    productLiterPrice.setText(String.format("%s %s %s", getString(R.string.currency), product.getLiterpris(), getString(R.string.product_perLiter)) );
                    productLiterPrice.setBackgroundColor(secondaryColor);

                    productVolume.setText(String.format( "%s %s", product.getVolum(), getString(R.string.centiLiter) ));


                    //Adding info related to product contents
                    createTextView(productDetails1, String.format("%s%%", product.getAlkohol()), getString(R.string.product_alkohol));
                    createTextView(productDetails1, product.getArgang() , getString(R.string.product_year));
                    createTextView(productDetails1, product.getLagringsgrad(), getString(R.string.product_storage));
                    createTextView(productDetails1, product.getFriskhet(), getString(R.string.product_freshness));
                    createTextView(productDetails1, product.getFylde(), getString(R.string.product_fullness));
                    createTextView(productDetails1, product.getGarvestoffer(), getString(R.string.product_tannin));
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



                    drinkWithhead.setBackgroundColor(secondaryColor);
                    setDrinkWiths(drinkWith1, product.getPassertil01());
                    setDrinkWiths(drinkWith2, product.getPassertil02());
                    setDrinkWiths(drinkWith3, product.getPassertil03());
                    if (hasDrinkWiths)
                        drinkWithhead.setText(getString(R.string.product_drinkWith));


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

        if (!headerText.equals("") || !headerText.equals(null)){

            TextView headerTextView = new TextView(this);
            headerTextView.setText(String.format("%s: ",headerText));
            headerTextView.setTypeface(null, Typeface.BOLD);

            listElement.addView(headerTextView);
        }

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
}