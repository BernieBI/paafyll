package no.hiof.matsl.pfyll.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.firebase.database.FirebaseDatabase;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.adapter.ProductDataSourceFactory;
import no.hiof.matsl.pfyll.adapter.ProductRecycleViewAdapter;

public class FragmentProducts extends Fragment {
    private LiveData<PagedList<Product>> products;
    private RecyclerView recyclerView;
    private ImageButton layoutButton;
    private ProductRecycleViewAdapter productAdapter;
    private int layoutColumns = 2;
    private GridLayoutManager gridLayoutManager;
    //firebase
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();

    View view;
    String TAG = "MainActivity";

    public FragmentProducts(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products, container, false);
        /*Button productsButton = view.findViewById(R.id.viewProductsBtn);

        //Temporary button for starting ProductsActivity
        productsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ProductsActivity started");
                Intent productActivityIntent = new Intent(FragmentProducts.this.getActivity(), ProductsActivity.class);
                getActivity().startActivity(productActivityIntent);
            }
        });*/
        Log.d(TAG, "onCreate: Started");


        PagedList.Config config = new PagedList.Config.Builder().setPageSize(6).build();

        ProductDataSourceFactory factory = new ProductDataSourceFactory(database.getReference());

        products = new LivePagedListBuilder<>(factory, config).build();

        layoutButton = view.findViewById(R.id.layoutButton);
        layoutButton.setOnClickListener(layoutSwitchListener);

        initRecyclerView();
        return view;


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
                productAdapter.changeLayout(true);
            }else{
                layoutColumns = 2;
                layoutButton.setImageDrawable(getActivity().getDrawable(R.drawable.list));
                productAdapter.changeLayout(false);
            }
            gridLayoutManager.setSpanCount(layoutColumns);
        }
    };
    public void passProductsToView (int layoutColumns){

        productAdapter = new ProductRecycleViewAdapter(getActivity());
        recyclerView.setAdapter(productAdapter);
        gridLayoutManager = new GridLayoutManager(getActivity(), layoutColumns); // (Context context, int spanCount)
        recyclerView.setLayoutManager(gridLayoutManager);
    }
}
