package no.hiof.matsl.pfyll;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.model.Product;

public class SingleProductActivity extends AppCompatActivity {
    String TAG = "SingleProduct Activity";

    private String productID;
    //firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference productsRef;

    //views
    private TextView productName, productTaste, productPrice, productLiterPrice, productVolume;
    private ImageView productImage, eatWith1, eatWith2, eatWith3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        productName = findViewById(R.id.productName);
        productTaste = findViewById(R.id.productTaste);
        productPrice = findViewById(R.id.productPrice);
        productLiterPrice = findViewById(R.id.productLiterPrice);
        productVolume = findViewById(R.id.productVolume);

        productImage = findViewById(R.id.productImage);
        eatWith1 = findViewById(R.id.eatWith1);
        eatWith2 = findViewById(R.id.eatWith2);
        eatWith3 = findViewById(R.id.eatWith3);

        Intent intent = getIntent();
        productID = intent.getStringExtra("ProductID");
        productsRef = database.getReference("Products/" + productID);
        GetData();
    }

    private void GetData() {

        ValueEventListener productListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Product product = dataSnapshot.getValue(Product.class);
                product.setBildeUrl(product.getVarenummer());

                Glide.with(SingleProductActivity.this)
                        .asBitmap()
                        .load(product.getBildeUrl())
                        .into(productImage);

                productName.setText(product.getVarenavn());
                productTaste.setText(product.getSmak());
                productPrice.setText("Kr " + product.getPris());
                productLiterPrice.setText("Kr " + product.getLiterpris() + " pr. liter");
                productVolume.setText(product.getVolum() + " Cl");


                eatWith1.setImageResource( getEatWiths( product.getPassertil01() ) );
                eatWith2.setImageResource( getEatWiths( product.getPassertil02() ) );
                eatWith3.setImageResource( getEatWiths( product.getPassertil03() ) );

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        productsRef.addValueEventListener(productListener);

    }

    public int getEatWiths(String value) {

        if (value.contains("Dessert"))
            return R.drawable.dessert;

        if (value.contains("Fisk"))
            return R.drawable.fisk;

        if (value.contains("Lyst kjøtt"))
            return R.drawable.kylling;

        if (value.contains("Lam og sau"))
            return R.drawable.lam;

        if (value.contains( "Ost" ))
            return R.drawable.ost;

        if (value.contains("Skalldyr"))
            return R.drawable.skalldyr;

        if (value.contains("Smavilt"))
            return R.drawable.smavilt;

        if (value.contains("Storfe"))
            return R.drawable.storfe;

        if (value.contains("Storvilt"))
            return R.drawable.storvilt;

        if (value.contains("Svinekjøtt"))
            return R.drawable.svin;

        return 0;
    }
}