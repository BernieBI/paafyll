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
import android.support.v7.widget.LinearLayoutManager;
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
import java.util.Collection;
import java.util.Collections;

import no.hiof.matsl.pfyll.CacheHandler;
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
    private int layoutColumns = 1;
    private ArrayList<String> preSetProducts;
    private GridLayoutManager gridLayoutManager;
    private ProductDataSourceFactory  factory;
    private PagedList.Config config;
    private Boolean isRecent = false;
    private Bundle bundle;
    //firebase
    final private FirebaseFirestore database = FirebaseFirestore.getInstance();


    View view;
    String TAG = "ProductsFragment";

    public FragmentProducts(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products, container, false);


       config = new PagedList.Config.Builder().setPageSize(6).build();
        factory = new ProductDataSourceFactory(database);

        layoutButton = view.findViewById(R.id.layoutButton);
        layoutButton.setOnClickListener(layoutSwitchListener);

        bundle = getArguments();
        if (bundle != null){
            //Retrieving list of product IDs.
            view.findViewById(R.id.filterField).setVisibility(View.GONE);

            if (bundle.getStringArrayList("preSetProducts") != null){
                preSetProducts = bundle.getStringArrayList("preSetProducts");
                Collections.reverse(preSetProducts);
                factory = new ProductDataSourceFactory(database, new IdFilter(preSetProducts));
            }


        }

        products = new LivePagedListBuilder<>(factory, config).build();

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
                view.findViewById(R.id.progressBar).setVisibility(View.GONE);
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

}
