package no.hiof.matsl.pfyll.model;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;


import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.ScanActivity;
import no.hiof.matsl.pfyll.adapter.ProductDataSourceFactory;
import no.hiof.matsl.pfyll.adapter.ProductRecycleViewAdapter;

public class FragmentProducts extends Fragment{
    private LiveData<PagedList<FirestoreProduct>> products;
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
    private StringFilter selectedCategory, selectedCountry, productName;
    private NumberFilter filterPrice, filterAlcohol;

    private Button priceButton, categoryButton, countryButton, alcoholButton;
    private ImageButton filterButton, scanButton, searchButton;
    private FlexboxLayout selectedFilters;
    private SearchView searchBar;
    private Drawable removeIcon;

    private String FILTER = "Filtrer";
    private String RESETFILTER = "Fjern filter";
    float dpi;

    private int margin;
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

        dpi = getResources().getDisplayMetrics().density;
        margin = (int)(400*dpi);

        config = new PagedList.Config.Builder().setPageSize(6).build();

        factory = new ProductDataSourceFactory(database, filters);
        layoutButton = view.findViewById(R.id.layoutButton);
        layoutButton.setOnClickListener(layoutSwitchListener);
        removeIcon = getResources().getDrawable(R.drawable.remove);

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

        selectedFilters = view.findViewById(R.id.selectedFilters);
        selectedFilters.setFlexDirection(FlexDirection.ROW);
        selectedFilters.setFlexWrap(FlexWrap.WRAP);

        buttons();

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        scanButton.setBackgroundColor(getResources().getColor(R.color.primaryLightColor));
        scanButton.setColorFilter(getResources().getColor(R.color.white));
    }

    private void buttons() {
        scanButton= view.findViewById(R.id.startScan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanButton.setBackgroundColor(getResources().getColor(R.color.white));
                scanButton.setColorFilter(getResources().getColor(R.color.primaryLightColor));
                Intent intent = new Intent(getContext(), ScanActivity.class);
                startActivity(intent);
            }
        });

        filterOptions =  view.findViewById(R.id.filterOptions);
        //filterOptions.setTranslationY(-margin);
        filterOptions.setVisibility(View.GONE);

        filterButton = view.findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterOptions.getVisibility() == View.VISIBLE/*filterOptions.getTranslationY() == 0*/){
                    filterButton.setBackgroundColor(getResources().getColor(R.color.primaryLightColor));
                    filterButton.setColorFilter(getResources().getColor(R.color.white));
                    //filterOptions.animate().translationY(-margin);
                    filterOptions.setVisibility(View.GONE);
                }else{
                    filterButton.setBackgroundColor(getResources().getColor(R.color.white));
                    filterButton.setColorFilter(getResources().getColor(R.color.primaryLightColor));
                    //filterOptions.animate().translationY(0);
                    filterOptions.setVisibility(View.VISIBLE);
                }
            }
        });

        categoryButton = view.findViewById(R.id.filterCategory);
        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stringFilterDialog( getResources().getStringArray(R.array.productCategories), "Varetype", Filter.ComparisonType.EQUALS);
            }
        });
        countryButton = view.findViewById(R.id.filterCountry);
        countryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stringFilterDialog( getResources().getStringArray(R.array.productCountries), "Land", Filter.ComparisonType.EQUALS );
            }
        });
        priceButton = view.findViewById(R.id.filterPrice);
        priceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              numberFilterDialog("Pris");
            }
        });
        alcoholButton = view.findViewById(R.id.filterAlcohol);
        alcoholButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberFilterDialog("Alkohol");
            }
        });

        searchBar = view.findViewById(R.id.searchBar);
        //searchBar.setTranslationY(-margin);
        searchBar.setVisibility(View.GONE);
        searchBar.setBackgroundColor(getResources().getColor(R.color.white));
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.trim();
                if (query.equals(""))
                    productName = null;
                else {
                    productName = new StringFilter(
                            "Varenavn",
                            Filter.ComparisonType.BETWEEN,
                            query
                    );
                }
                submitFilter();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return onQueryTextSubmit(newText);
            }
        });

        searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchBar.getVisibility() == View.VISIBLE /*searchBar.getTranslationY() == 0*/){
                    searchButton.setBackgroundColor(getResources().getColor(R.color.primaryLightColor));
                    searchButton.setColorFilter(getResources().getColor(R.color.white));
                   // searchBar.animate().translationY(-margin);
                    searchBar.setVisibility(View.GONE);
                } else {
                    searchButton.setBackgroundColor(getResources().getColor(R.color.white));
                    searchButton.setColorFilter(getResources().getColor(R.color.primaryLightColor));
                    //searchBar.animate().translationY(0);
                    searchBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    public void submitFilter(){
        view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        filters.clear();

        if (selectedCategory != null)
            filters.add(selectedCategory);

        if (selectedCountry != null)
            filters.add(selectedCountry);

        if (filterPrice != null)
            filters.add(filterPrice);

        if (filterAlcohol != null)
            filters.add(filterAlcohol);

        if (productName != null)
            filters.add(productName);

        factory = new ProductDataSourceFactory(database, filters);
        products = new LivePagedListBuilder<>(factory, config).build();
        initRecyclerView();
    }
        private void stringFilterDialog( final String[] elements, final String field, final Filter.ComparisonType comparisonType) {

        AlertDialog.Builder filterBuilder = new AlertDialog.Builder(getContext());
        filterBuilder.setTitle(field)
                .setItems(elements, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        selectedFilters.removeView(view.findViewWithTag(field));
                        final Button button = new Button(getContext());
                        button.setTag(field);
                        button.setText(elements[which]);
                        button.setTextSize(11);
                        button.setCompoundDrawablesWithIntrinsicBounds(removeIcon, null, null, null);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (field.equals("Varetype")) {
                                    selectedCategory = null;
                                } else if(field.equals("Land")){
                                    selectedCountry = null;
                                }
                                selectedFilters.removeView(view.findViewWithTag(field));
                                submitFilter();
                            }
                        });

                        selectedFilters.addView(button);

                        StringFilter filter = new StringFilter(field, comparisonType, elements[which]);
                        if (field.equals("Varetype"))
                            selectedCategory = filter;
                        else if(field.equals("Land"))
                            selectedCountry = filter;

                        submitFilter();
                    }
                });

        Dialog alertFilter = filterBuilder.create();
        alertFilter.show();
        alertFilter.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, (int)(500*dpi));
    }

    private void numberFilterDialog(final String fieldString) {

        final AlertDialog.Builder filterBuilder = new AlertDialog.Builder(getContext());
        filterBuilder.setTitle(fieldString)
                .setView(getLayoutInflater().inflate(R.layout.number_filter, null))
                .setPositiveButton("Bruk", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        selectedFilters.removeView(view.findViewWithTag(fieldString));
                        EditText priceFrom = ((AlertDialog)dialog).findViewById(R.id.priceFrom);
                        float from = Integer.parseInt(priceFrom.getText().toString());

                        NumberFilter field = null;

                            field = new NumberFilter(from,fieldString, Filter.ComparisonType.EQUALS );

                        String unit = "";
                        if (fieldString == "Pris"){
                            filterPrice = field;
                            unit = "kr";
                        }else if( fieldString == "Alkohol") {
                            filterAlcohol = field;
                            unit = "%";
                        }

                        Button button = new Button(getContext());
                        button.setTag(fieldString);
                        button.setText(String.format("%s %s%s", fieldString, from, unit));
                        button.setTextSize(11);
                        button.setCompoundDrawablesWithIntrinsicBounds(removeIcon, null, null, null);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (fieldString.equals("Pris")) {
                                    filterPrice = null;
                                } else if(fieldString.equals("Alkohol")){
                                    filterAlcohol = null;
                                }
                                selectedFilters.removeView(view.findViewWithTag(fieldString));
                                submitFilter();
                            }
                        });
                        selectedFilters.addView(button);
                        submitFilter();
                    }
                })
                .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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

        products.observe(this, new Observer<PagedList<FirestoreProduct>>() {
            @Override
            public void onChanged(@Nullable PagedList<FirestoreProduct> products) {
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
                layoutButton.setImageDrawable(getActivity().getDrawable(R.drawable.rows));
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
