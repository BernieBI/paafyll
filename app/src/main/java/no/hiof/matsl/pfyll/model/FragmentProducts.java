package no.hiof.matsl.pfyll.model;

import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.ScanActivity;
import no.hiof.matsl.pfyll.adapter.ProductDataSourceFactory;
import no.hiof.matsl.pfyll.adapter.ProductRecycleViewAdapter;

public class FragmentProducts extends Fragment{
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

    private ConstraintLayout filterOptions;
    private ArrayList<Filter> filters = new ArrayList<>();
    private StringFilter selectedCategory, selectedCountry;
    private NumberFilter filterPriceFrom, filterPriceTo;

    private Button submitFilter;
    private String FILTER = "Filtrer";
    private String RESETFILTER = "Fjern filter";
    private int margin = 900;
    //firebase
    final private FirebaseFirestore database = FirebaseFirestore.getInstance();


    View view;
    String TAG = "ProductsFragment";

    public FragmentProducts() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products, container, false);


       config = new PagedList.Config.Builder().setPageSize(6).build();

       factory = new ProductDataSourceFactory(database, filters);
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

        buttons();

        return view;

    }

    private void buttons() {
        ImageButton button = view.findViewById(R.id.startScan);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ScanActivity.class);
                startActivity(intent);
            }
        });

        filterOptions =  view.findViewById(R.id.filterOptions);
        filterOptions.setTranslationY(-margin);

        final ImageButton filterButton = view.findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterOptions.getTranslationY() == 0){
                    filterButton.setBackgroundColor(getResources().getColor(R.color.white));
                    filterOptions.animate().translationY(-margin);
                }else{
                    filterButton.setBackgroundColor(getResources().getColor(R.color.browser_actions_bg_grey));
                    filterOptions.animate().translationY(0);
                }
            }
        });

        final Button categoryButton = view.findViewById(R.id.filterCategory);
        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog(categoryButton, getResources().getStringArray(R.array.productCategories), "Varetype", Filter.ComparisonType.EQUALS);
            }
        });
        final Button countryButton = view.findViewById(R.id.filterCountry);
        countryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog(countryButton, getResources().getStringArray(R.array.productCountries), "Land", Filter.ComparisonType.EQUALS );
            }
        });
        Button priceButton= view.findViewById(R.id.filterPrice);
        priceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder filterBuilder = new AlertDialog.Builder(getContext());
                filterBuilder.setTitle("Velg pris")
                        .setView(getLayoutInflater().inflate(R.layout.price_filter, null))
                        // Add action buttons
                        .setPositiveButton("Bruk", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                EditText priceFrom = ((AlertDialog)dialog).findViewById(R.id.priceFrom);
                                EditText priceTo = ((AlertDialog)dialog).findViewById(R.id.priceTo);
                                float from = Integer.parseInt(priceFrom.getText().toString());
                                float to = Integer.parseInt(priceTo.getText().toString());
                                Log.d(TAG, "from: " + from +" To:" + to);
                                if (from < to){
                                    filterPriceFrom = new NumberFilter(from,"Pris", Filter.ComparisonType.GREATER_THAN );
                                    filterPriceTo = new NumberFilter(to,"Pris", Filter.ComparisonType.LESS_THAN );
                                }else{
                                    Toast.makeText(getContext(), "Det der går ikke.. Det skjønner du også", Toast.LENGTH_SHORT);
                                }
                            }
                        })
                        .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                filterBuilder.show();
            }
        });

        submitFilter = view.findViewById(R.id.submitFilter);
        submitFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                filters.clear();

                if (submitFilter.getText().equals(FILTER)){

                    submitFilter.setText(RESETFILTER);
                    if (selectedCategory != null)
                        filters.add(selectedCategory);
                    if (selectedCountry != null)
                        filters.add(selectedCountry);
                    if (filterPriceFrom != null)
                        filters.add(filterPriceFrom);
                    if (filterPriceTo != null)
                        filters.add(filterPriceTo);

                }else {
                    submitFilter.setText(FILTER);
                    countryButton.setText("Land");
                    categoryButton.setText("Varetype");
                }
                factory = new ProductDataSourceFactory(database, filters);
                products = new LivePagedListBuilder<>(factory, config).build();
                initRecyclerView();

            }
        });
    }

    private void filterDialog(final Button button, final String[] elements, final String field, final Filter.ComparisonType comparator) {

        AlertDialog.Builder filterBuilder = new AlertDialog.Builder(getContext());
        filterBuilder.setTitle(field)
                .setItems(elements, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        submitFilter.setText(FILTER);

                        button.setText(elements[which]);
                        StringFilter filter = new StringFilter(field, comparator, elements[which]);

                        if (field.equals("Varetype"))
                            selectedCategory = filter;
                        else if(field.equals("Land"))
                            selectedCountry = filter;
                    }
                });

        filterBuilder.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void initRecyclerView(){
        recyclerView = view.findViewById(R.id.product_recycler_view);
        passProductsToView();

        products.observe(this, new Observer<PagedList<Product>>() {
            @Override
            public void onChanged(@Nullable PagedList<Product> products) {
                productAdapter.submitList(products);
                view.findViewById(R.id.progressBar).setVisibility(View.GONE);

                if (products.size() == 0 )
                    view.findViewById(R.id.noResults).setVisibility(View.VISIBLE);
                else
                    view.findViewById(R.id.noResults).setVisibility(View.GONE);
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
    public void passProductsToView (){



        productAdapter = new ProductRecycleViewAdapter(getActivity(), getArguments());
        if (layoutColumns == 2)
            productAdapter.setLayout(false);
        else
            productAdapter.setLayout(true);

        recyclerView.setAdapter(productAdapter);
        gridLayoutManager = new GridLayoutManager(getActivity(), layoutColumns);
        recyclerView.setLayoutManager(gridLayoutManager);
    }




}
