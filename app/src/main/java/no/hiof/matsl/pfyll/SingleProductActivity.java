package no.hiof.matsl.pfyll;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import no.hiof.matsl.pfyll.model.Product;

public class SingleProductActivity extends AppCompatActivity {
    String TAG = "SingleProductActivity";

    private String productID;
    //firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference productsRef;

    //views
    private LinearLayout productDetails1, productDetails2;
    private TextView productName, productTaste, productPrice, productLiterPrice, productVolume, drinkWithhead, productCountry;
    private ImageView productImage, drinkWith1, drinkWith2, drinkWith3;


    private String[] nonDrinkables;
    private boolean hasDrinkWiths = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        nonDrinkables = getResources().getStringArray(R.array.nonDrinkables);

        productName = findViewById(R.id.productName);
        productTaste = findViewById(R.id.productTaste);
        productPrice = findViewById(R.id.productPrice);
        productLiterPrice = findViewById(R.id.productLiterPrice);
        productVolume = findViewById(R.id.productVolume);
        productDetails1 = findViewById(R.id.productDetails1);
        productDetails2 = findViewById(R.id.productDetails2);
        productCountry = findViewById(R.id.productCountry);

        productImage = findViewById(R.id.productImage);
        drinkWithhead = findViewById(R.id.drinkWith);
        drinkWith1 = findViewById(R.id.drinkWith1);
        drinkWith2 = findViewById(R.id.drinkWith2);
        drinkWith3 = findViewById(R.id.drinkWith3);

        Intent intent = getIntent();
        productID = intent.getStringExtra("ProductID");
        productsRef = database.getReference("Products/" + productID);
        GetData();
    }

    private void GetData() {

        ValueEventListener productListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);
                Log.d(TAG, "Product: " + product.getVarenummer());

                product.setBildeUrl(product.getVarenummer());

                Glide.with(SingleProductActivity.this)
                        .asBitmap()
                        .load(product.getBildeUrl())
                        .into(productImage);
                productImage.setContentDescription(product.getVarenavn());

                productName.setText(product.getVarenavn());
                productPrice.setText(String.format( "%s %s", getString(R.string.currency), product.getPris() ));

                if (confirmType(product.getVaretype())) {

                    if (!product.getLand().equals(""))
                        productCountry.setBackgroundColor(getResources().getColor(R.color.primaryLightColor));
                        productCountry.setText(product.getLand());

                    productTaste.setText(product.getSmak());
                    productLiterPrice.setText(String.format("%s %s %s", getString(R.string.currency), product.getLiterpris(), getString(R.string.product_perLiter)) );
                    productVolume.setText(String.format( "%s %s", product.getVolum(), getString(R.string.centiLiter) ));

                    //Adding info related to product contents
                    createTextView(productDetails1, String.format("%s%%", product.getAlkohol()), getString(R.string.product_alkohol));
                    createTextView(productDetails1, product.getArgang() , getString(R.string.product_year));
                    createTextView(productDetails1, product.getLagringsgrad(), getString(R.string.product_storage));
                    createTextView(productDetails1, product.getFriskhet(), getString(R.string.product_freshness));
                    createTextView(productDetails1, product.getFylde(), getString(R.string.product_fullness));
                    createTextView(productDetails1, product.getGarvestoffer(), getString(R.string.product_tannin));
                    createTextView(productDetails1, product.getFarge(), getString(R.string.product_color));
                    createTextView(productDetails1, product.getRastoff(), getString(R.string.product_feedstock));

                    //Adding infor related to product production
                    createTextView(productDetails2, product.getDistrikt() , getString(R.string.product_district));
                    createTextView(productDetails2, product.getProdusent(), getString(R.string.product_producer));


                    setDrinkWiths(drinkWith1, product.getPassertil01());
                    setDrinkWiths(drinkWith2, product.getPassertil02());
                    setDrinkWiths(drinkWith3, product.getPassertil03());
                    if (hasDrinkWiths)
                        drinkWithhead.setText(getString(R.string.product_drinkWith));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        productsRef.addValueEventListener(productListener);

    }

    public boolean confirmType(String productType){

        for (String type:nonDrinkables){
            if (productType.contains(type))
                return false;
        }

        return true;
    }

    //pass header text as "" if not to use
    public void createTextView(LinearLayout parent, String text, String headerText){
        if (text.equals("") || text.equals(null) || text.contains("Øvrige"))
             return;

        if (!headerText.equals("") || !headerText.equals(null)){

            TextView headerTextView = new TextView(this);
            headerTextView.setText(String.format("%s:",headerText));
            headerTextView.setTypeface(null, Typeface.BOLD);

            parent.addView(headerTextView);
        }

        TextView textView = new TextView(this);
        textView.setText(text);

        parent.addView(textView);
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

        view.setImageResource(image);
        view.setContentDescription(imageAlt);

    }
}