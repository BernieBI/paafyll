package no.hiof.matsl.pfyll;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.ArrayList;

import no.hiof.matsl.pfyll.adapter.ItemClickListener;
import no.hiof.matsl.pfyll.adapter.ProductRecycleViewAdapter;
import no.hiof.matsl.pfyll.model.Product;

public class ProductsActivity extends AppCompatActivity {
    String TAG = "ProductsActivity";


    private ArrayList<Product> products = new ArrayList<>();
    private RecyclerView recyclerView;
    private Button layoutButton;
    private ProductRecycleViewAdapter productAdapter;
    private int layoutColumns = 2;

    //firebase
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference productsRef = database.getReference("Products");
    private Querier querier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Started");
        setContentView(R.layout.activity_products);

        layoutButton = findViewById(R.id.layoutButton);
        layoutButton.setOnClickListener(layoutSwitchListener);

        initRecyclerView();
    }

    private void initRecyclerView(){

        recyclerView = findViewById(R.id.product_recycler_view);

        passProductsToView(products, layoutColumns);

        productsRef.limitToFirst(50).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                Product product = dataSnapshot.getValue(Product.class);

                product.setFirebaseID(dataSnapshot.getKey());
                product.setBildeUrl(product.getVarenummer());

                Log.d(TAG, "onChildAdded: product: " + product.getVarenavn());

                products.add(product);
                recyclerView.getAdapter().notifyItemInserted(products.size() - 1);
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
                Log.w(TAG, "onCancelled:", databaseError.toException());

            }
        });

    }

    private View.OnClickListener layoutSwitchListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            if (layoutColumns == 2){
                layoutColumns = 1;
            }else{
                layoutColumns = 2;
            }
            passProductsToView(products, layoutColumns);

        }
    };
    public void passProductsToView (ArrayList<Product> products, int layoutColumns){

        productAdapter = new ProductRecycleViewAdapter(ProductsActivity.this, products);
        recyclerView.setAdapter(productAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ProductsActivity.this, layoutColumns); // (Context context, int spanCount)
        recyclerView.setLayoutManager(gridLayoutManager);
    }
}
