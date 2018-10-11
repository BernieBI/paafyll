package no.hiof.matsl.pfyll;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.adapter.ProductRecycleViewAdapter;
import no.hiof.matsl.pfyll.model.Product;

public class ProductsActivity extends AppCompatActivity {
    String TAG = "ProductsActivity";


    private ArrayList<Product> products = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProductRecycleViewAdapter productAdapter;

    //firebase
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference productsRef = database.getReference("Products");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started");
        setContentView(R.layout.activity_products);

       initRecyclerView();
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init Recyclerview");


        recyclerView = findViewById(R.id.product_recycler_view);

        productsRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                Product product = dataSnapshot.getValue(Product.class);

                product.setBildeUrl(product.getVarenummer());
                Log.d(TAG, "onChildAdded: product: " + product.getVarenavn());

                products.add(product);
                passProductsToView(products);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Product new_product = dataSnapshot.getValue(Product.class);

                Log.d(TAG, "onChildChanged:" + new_product.getVarenavn());

                for (Product old_product : products){
                    if(old_product.getVarenummer() == new_product.getVarenummer() ){
                        products.set( products.indexOf(old_product) , new_product);
                        break;
                    }
                }
                passProductsToView(products);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {


            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled:", databaseError.toException());

            }
        });

    }

    public void passProductsToView (ArrayList<Product> products){

        productAdapter = new ProductRecycleViewAdapter(ProductsActivity.this, products);
        recyclerView.setAdapter(productAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ProductsActivity.this, 2); // (Context context, int spanCount)
        recyclerView.setLayoutManager(gridLayoutManager);
    }
}
