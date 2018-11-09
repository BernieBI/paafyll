package no.hiof.matsl.pfyll.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.ScanActivity;
import no.hiof.matsl.pfyll.SingleProductActivity;
import no.hiof.matsl.pfyll.adapter.ItemClickListener;
import no.hiof.matsl.pfyll.adapter.ProductDataSourceFactory;
import no.hiof.matsl.pfyll.adapter.ProductRecycleViewAdapter;

public class FragmentProducts extends Fragment {
    private LiveData<PagedList<Product>> products;
    private RecyclerView recyclerView;
    private FloatingActionButton layoutButton;
    private ProductRecycleViewAdapter productAdapter;
    private int layoutColumns = 2;
    private ArrayList<String> productsInList;
    private GridLayoutManager gridLayoutManager;

    //firebase
    //final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    final private FirebaseFirestore database = FirebaseFirestore.getInstance();


    static View view;
    String TAG = "ProductsFragment";

    public FragmentProducts(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products, container, false);

        Log.d(TAG, "onCreate: Started " + layoutColumns + " columns");

        PagedList.Config config = new PagedList.Config.Builder().setPageSize(6).build();
        ProductDataSourceFactory  factory = new ProductDataSourceFactory(database);

        Bundle bundle = getArguments();
        if (bundle != null){
            //Retrieving list of product IDs.
            productsInList = bundle.getStringArrayList("preSetProducts");
            Log.d(TAG, "Parameters: " + productsInList);
            view.findViewById(R.id.filterField).setVisibility(View.GONE);

            config = new PagedList.Config.Builder().setPageSize(productsInList.size()).build();
            factory = new ProductDataSourceFactory(database, new IdFilter(productsInList));

        }



        products = new LivePagedListBuilder<>(factory, config).build();

        layoutButton = view.findViewById(R.id.layoutButton);
        layoutButton.setOnClickListener(layoutSwitchListener);

        initRecyclerView();

        ImageButton button = view.findViewById(R.id.startScan);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ScanActivity.class);
                startActivity(intent);
            }
        });
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        initRecyclerView();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onstop");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initRecyclerView(){

        recyclerView = view.findViewById(R.id.product_recycler_view);
        passProductsToView(layoutColumns);

        products.observe(this, new Observer<PagedList<Product>>() {
            @Override
            public void onChanged(@Nullable PagedList<Product> products) {
                productAdapter.submitList(products);
            }
        });

    }

    private View.OnClickListener layoutSwitchListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {

            if (layoutColumns == 2){
                layoutColumns = 1;
                layoutButton.setImageDrawable(getActivity().getDrawable(R.drawable.grid));
                productAdapter.setLayout(true);
            }else{
                layoutColumns = 2;
                layoutButton.setImageDrawable(getActivity().getDrawable(R.drawable.list));
                productAdapter.setLayout(false);
            }
            gridLayoutManager.setSpanCount(layoutColumns);
        }
    };
    public void passProductsToView (int layoutColumns){

        productAdapter = new ProductRecycleViewAdapter(getActivity(), getArguments());
        if (layoutColumns == 2)
            productAdapter.setLayout(false);
        else
            productAdapter.setLayout(true);

        recyclerView.setAdapter(productAdapter);
        gridLayoutManager = new GridLayoutManager(getActivity(), layoutColumns); // (Context context, int spanCount)
        recyclerView.setLayoutManager(gridLayoutManager);
    }
    public static void BarcodeReturn(int id){
        Intent singleProductIntent = new Intent(view.getContext(), SingleProductActivity.class);
        singleProductIntent.putExtra("ProductID", id);
        view.getContext().startActivity(singleProductIntent);

    }
}
