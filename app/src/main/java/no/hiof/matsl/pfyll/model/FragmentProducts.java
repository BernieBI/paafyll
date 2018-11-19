package no.hiof.matsl.pfyll.model;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;

import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


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
    private NumberFilter filterPriceFrom, filterPriceTo, filterAlcoholFrom, filterAlcoholTo;

    private Button priceButton, categoryButton, countryButton, alcoholButton;
    private ImageButton filterButton, scanButton;
    private FlexboxLayout selectedFilters;
    private SearchView searchBar;
    private TextView searchWord;
    private Drawable removeIcon;
    private @ColorInt int primaryColor, primaryDarkColor, primaryLightColor, primaryTextColor, secondaryColor;

    TypedValue typedValue = new TypedValue();
    Resources.Theme theme;

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

        //Colors from active theme
        theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorPrimaryLight, typedValue, true);
        primaryLightColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimaryText, typedValue, true);
        primaryTextColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        primaryDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primaryColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorSecondary, typedValue, true);
        secondaryColor = typedValue.data;

        config = new PagedList.Config.Builder().setPageSize(6).build();
        factory = new ProductDataSourceFactory(database, filters);
        layoutButton = view.findViewById(R.id.layoutButton);
        layoutButton.setOnClickListener(layoutSwitchListener);
        removeIcon = getResources().getDrawable(R.drawable.remove);
        searchWord = view.findViewById(R.id.searchWord);
        bundle = getArguments();
        if (bundle != null){
            //Retrieving list of product IDs.
            view.findViewById(R.id.filterField).setVisibility(View.GONE);

            if (bundle.getStringArrayList("preSetProducts") != null){
                layoutButton.hide();
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
        scanButton.setBackgroundColor(primaryColor);
        scanButton.setColorFilter(primaryTextColor);
    }

    private void buttons() {
        scanButton= view.findViewById(R.id.startScan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanButton.setBackgroundColor(primaryLightColor);
                scanButton.setColorFilter(primaryColor);
                hideFilter();

                Intent intent = new Intent(getContext(), ScanActivity.class);
                startActivity(intent);
            }
        });

        filterOptions =  view.findViewById(R.id.filterOptions);
        filterOptions.setVisibility(View.GONE);

        filterButton = view.findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterOptions.getVisibility() == View.VISIBLE) {
                    hideFilter();
                }else {
                    showFilter();
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
                if (priceButton.getAlpha() == 1)
                    numberFilterDialog("Pris");
            }
        });
        alcoholButton = view.findViewById(R.id.filterAlcohol);
        alcoholButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alcoholButton.getAlpha() == 1)
                    numberFilterDialog("Alkohol");
            }
        });
        searchWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchWord.setVisibility(View.GONE);
                productName = null;
                submitFilter();
            }
        });
        searchBar = view.findViewById(R.id.searchBar);
        //searchBar.setTranslationY(-margin);
        searchBar.setVisibility(View.GONE);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                selectedFilters.removeView(view.findViewWithTag("search"));
                query = query.trim();
                if (query.equals("")) {
                    productName = null;
                }else {
                    searchWord.setText(query);
                    productName = new StringFilter(
                            "Sokeord",
                            Filter.ComparisonType.LIKE,
                            query.toLowerCase()
                    );

                    TextView activeFilter = new Button(getContext());
                    activeFilter.setTag("search");
                    activeFilter.setText("\"" +query +"\"");
                    activeFilter.setTextSize(11);
                    activeFilter.setCompoundDrawablesWithIntrinsicBounds(removeIcon, null, null, null);

                    activeFilter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            productName = null;
                            selectedFilters.removeView(view.findViewWithTag("search"));
                            submitFilter();
                        }
                    });
                    selectedFilters.addView(activeFilter);
                }
                submitFilter();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void hideFilter(){
        filterButton.setBackgroundColor(primaryColor);
        filterButton.setColorFilter(primaryTextColor);
        filterOptions.setVisibility(View.GONE);
        searchBar.onActionViewCollapsed();
        searchBar.setVisibility(View.GONE);
    }
    private void showFilter(){
        filterButton.setBackgroundColor(primaryLightColor);
        filterButton.setColorFilter(primaryDarkColor);
        filterOptions.setVisibility(View.VISIBLE);
        searchBar.onActionViewExpanded();
        searchBar.setVisibility(View.VISIBLE);
    }
    public void submitFilter(){
        searchWord.setVisibility(View.GONE);
        view.findViewById(R.id.noResults).setVisibility(View.GONE);
        view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        filters.clear();

        if (selectedCategory != null)
            filters.add(selectedCategory);

        if (selectedCountry != null)
            filters.add(selectedCountry);

        if (filterPriceFrom != null)
            filters.add(filterPriceFrom);
        if (filterPriceTo != null)
            filters.add(filterPriceTo);

        if (filterAlcoholFrom != null)
            filters.add(filterAlcoholFrom);
        if (filterAlcoholTo != null)
            filters.add(filterAlcoholTo);

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
                        TextView activeFilter = new TextView(getContext());
                        activeFilter.setTag(field);
                        activeFilter.setText(elements[which]);
                        activeFilter.setTextSize(12);
                        activeFilter.setTextColor(primaryTextColor);
                        activeFilter.setGravity(Gravity.CENTER_VERTICAL);
                        GradientDrawable shape =  new GradientDrawable();
                        shape.setCornerRadius(8);
                        shape.setColor(secondaryColor);
                        shape.setAlpha(150);
                        activeFilter.setBackground(shape);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins((int)(8*dpi), (int)(4*dpi), 0, (int)(4*dpi));
                        activeFilter.setLayoutParams(params);
                        activeFilter.setPadding((int)(4*dpi), (int)(4*dpi), (int)(4*dpi), (int)(6*dpi));
                        activeFilter.setCompoundDrawablesWithIntrinsicBounds(removeIcon, null, null, null);

                        activeFilter.setOnClickListener(new View.OnClickListener() {
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

                        selectedFilters.addView(activeFilter);

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
                        EditText numFrom = ((AlertDialog)dialog).findViewById(R.id.from);
                        EditText numTo = ((AlertDialog)dialog).findViewById(R.id.to);
                        float from = Integer.parseInt(numFrom.getText().toString().equals("") ? "0" : numFrom.getText().toString() );
                        float to = Integer.parseInt(numTo.getText().toString().equals("") ? "999999999" : numTo.getText().toString() );

                        NumberFilter fieldFrom = null;
                        NumberFilter fieldTo = null;
                            if (from < to) {
                                fieldFrom = new NumberFilter(from, fieldString, Filter.ComparisonType.GREATER_THAN_OR_EQUALS);
                                if (to < 999999999){
                                    fieldTo = new NumberFilter(to, fieldString, Filter.ComparisonType.LESS_THAN_OR_EQUALS);
                                }
                            }else {
                                Toast.makeText(getContext(),"Det går ikke!", Toast.LENGTH_SHORT).show();
                                numberFilterDialog(fieldString);
                                return;
                            }

                        String unit = "";
                        if (fieldString == "Pris"){
                            filterPriceFrom = fieldFrom;
                            filterPriceTo = fieldTo;
                            selectedFilters.removeView(view.findViewWithTag("Alkohol"));
                            alcoholButton.setAlpha((float) 0.4);
                            filterAlcoholFrom = null;
                            filterAlcoholTo = null;
                            unit = "kr";
                        }else if( fieldString == "Alkohol") {
                            filterAlcoholFrom = fieldFrom;
                            filterAlcoholTo = fieldTo;
                            selectedFilters.removeView(view.findViewWithTag("Pris"));
                            priceButton.setAlpha((float) 0.4);
                            filterPriceFrom = null;
                            filterPriceTo = null;
                            unit = "%";
                        }

                        TextView activeFilter = new TextView(getContext());
                        activeFilter.setTag(fieldString);
                        activeFilter.setText(String.format("%1$s %3$s - %2$s", (int)from, to >= 999999999 ? ">" : (int)to + " " +unit, unit));
                        activeFilter.setTextSize(11);
                        activeFilter.setTextColor(primaryTextColor);
                        activeFilter.setGravity(Gravity.CENTER_VERTICAL);
                        GradientDrawable shape =  new GradientDrawable();
                        shape.setCornerRadius(8);
                        shape.setColor(secondaryColor);
                        shape.setAlpha(150);
                        activeFilter.setBackground(shape);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins((int)(8*dpi), (int)(4*dpi), 0, (int)(4*dpi));
                        activeFilter.setLayoutParams(params);
                        activeFilter.setPadding((int)(4*dpi), (int)(4*dpi), (int)(4*dpi), (int)(6*dpi));
                        activeFilter.setCompoundDrawablesWithIntrinsicBounds(removeIcon, null, null, null);
                        activeFilter.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (fieldString.equals("Pris")) {
                                    filterPriceFrom = null;
                                    filterPriceTo = null;

                                } else if(fieldString.equals("Alkohol")){
                                    filterAlcoholFrom = null;
                                    filterAlcoholTo = null;
                                }
                                priceButton.setAlpha(1);
                                alcoholButton.setAlpha(1);
                                selectedFilters.removeView(view.findViewWithTag(fieldString));
                                submitFilter();
                            }
                        });
                        selectedFilters.addView(activeFilter);
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
