package no.hiof.matsl.pfyll.model;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private Bundle bundle;

    private ConstraintLayout filterOptions;
    private ArrayList<Filter> filters = new ArrayList<>();
    private StringFilter selectedCategory, selectedCountry, productName;
    private NumberFilter filterPriceFrom, filterPriceTo, filterAlcoholFrom, filterAlcoholTo;
    
    private String categoryString;
    private String countryString;
    private String priceString;
    private String alcoholString;

    private Button priceButton, categoryButton, countryButton, alcoholButton;
    private ImageButton filterButton, scanButton;
    private FlexboxLayout selectedFilters;
    private SearchView searchBar;
    private Drawable removeIcon;
    private @ColorInt int primaryColor, primaryDarkColor, primaryLightColor, primaryTextColor, secondaryColor;

    TypedValue typedValue = new TypedValue();
    Resources.Theme theme;


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

        //Initiation views
        scanButton= view.findViewById(R.id.startScan);
        filterOptions =  view.findViewById(R.id.filterOptions);
        categoryButton = view.findViewById(R.id.filterCategory);
        filterButton = view.findViewById(R.id.filterButton);
        countryButton = view.findViewById(R.id.filterCountry);
        priceButton = view.findViewById(R.id.filterPrice);
        alcoholButton = view.findViewById(R.id.filterAlcohol);
        searchBar = view.findViewById(R.id.searchBar);
        filterOptions.setVisibility(View.GONE);


        dpi = getResources().getDisplayMetrics().density;
        margin = (int)(400*dpi);
        categoryString =  getResources().getString(R.string.product_type);
        countryString =  getResources().getString(R.string.product_country);
        priceString =  getResources().getString(R.string.product_price);
        alcoholString =  getResources().getString(R.string.product_alkohol);


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

        //Productretriev setup
        config = new PagedList.Config.Builder().setPageSize(6).build();
        factory = new ProductDataSourceFactory(database, filters);
        layoutButton = view.findViewById(R.id.layoutButton);
        layoutButton.setOnClickListener(layoutSwitchListener);
        removeIcon = getResources().getDrawable(R.drawable.remove);

        bundle = getArguments();
        if (bundle != null){ // hiding/showing views if preSetProducts is set.
            view.findViewById(R.id.filterField).setVisibility(View.GONE);

            if (bundle.getStringArrayList("preSetProducts") != null){
                layoutButton.hide();
                //Retrieving list of product IDs.
                preSetProducts = bundle.getStringArrayList("preSetProducts");
                Collections.reverse(preSetProducts);
                factory = new ProductDataSourceFactory(database, new IdFilter(preSetProducts));
            }
        }
        products = new LivePagedListBuilder<>(factory, config).build();
        initRecyclerView();

        //Initiation selectedFilters flexbox.
        selectedFilters = view.findViewById(R.id.selectedFilters);
        selectedFilters.setFlexDirection(FlexDirection.ROW);
        selectedFilters.setFlexWrap(FlexWrap.WRAP);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Making sure scanButton is not highlighted
        scanButton.setBackgroundColor(primaryColor);
        scanButton.setColorFilter(primaryTextColor);

        buttons();
    }

    private boolean isNetworkAvailable() { // found at: https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void reTryConnection(){ // Showing views notifying users of no internet connection
        view.findViewById(R.id.noConnection).setVisibility(View.VISIBLE);
        Button button = view.findViewById(R.id.retryConnection);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);
            }
        });
    }

    private void buttons() {

        if (!isNetworkAvailable()) {
            reTryConnection();
            return;
        }

        //Starting ScanActivity on click
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

        //Showing filter view on click
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
        //Creating alertDialog on click. Only allowing one filter at a time
        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryButton.getAlpha() == 1)
                    stringFilterDialog( getResources().getStringArray(R.array.productCategories), categoryString, Filter.ComparisonType.EQUALS);
                else
                    Toast.makeText(getContext(), view.getResources().getString(R.string.one_filter_limit), Toast.LENGTH_SHORT).show();
            }
        });
        //Creating alertDialog on click. Only allowing one filter at a time
        countryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countryButton.getAlpha() == 1)
                    stringFilterDialog( getResources().getStringArray(R.array.productCountries), countryString, Filter.ComparisonType.EQUALS );
                else
                    Toast.makeText(getContext(), view.getResources().getString(R.string.one_filter_limit), Toast.LENGTH_SHORT).show();
            }
        });
        //Creating alertDialog on click. Only allowing one filter at a time. can not be combined with search or alcoholfilter
        priceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (priceButton.getAlpha() == 1)
                    numberFilterDialog(alcoholString);

                else if(view.findViewWithTag("search") != null)
                    Toast.makeText(getContext(), getResources().getString(R.string.not_combine_search), Toast.LENGTH_SHORT).show();

                else if(view.findViewWithTag(alcoholString) != null)
                    Toast.makeText(getContext(),getResources().getString(R.string.fields_can_not_combine), Toast.LENGTH_SHORT).show();

            }
        });
        //Creating alertDialog on click. Only allowing one filter at a time. can not be combined with search or priceFilter
        alcoholButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alcoholButton.getAlpha() == 1)
                    numberFilterDialog(priceString);

                else if (view.findViewWithTag("search") != null)
                    Toast.makeText(getContext(), getResources().getString(R.string.not_combine_search), Toast.LENGTH_SHORT).show();

                else if(view.findViewWithTag(priceString) != null)
                    Toast.makeText(getContext(), getResources().getString(R.string.fields_can_not_combine), Toast.LENGTH_SHORT).show();

            }
        });

        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setIconified(false);
            }
        });
        searchBar.setVisibility(View.GONE);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() { // Searchlistener
            @Override
            public boolean onQueryTextSubmit(String query) {
                alcoholButton.setAlpha((float) 0.3); //Disabling alcoholfilter and pricefilter
                priceButton.setAlpha((float) 0.3);

                selectedFilters.removeView(view.findViewWithTag("search")); //Removing existing search queries

                query = query.trim();

                if (query.equals("")) {
                    productName = null;
                }else { //not accepting empty queries

                    productName = new StringFilter("Sokeord", Filter.ComparisonType.LIKE, query.toLowerCase()); //Creating new StringFilter

                    TextView activeFilter = createActiveFilterView("search", "\"" +query +"\""); // Creating view to indicate that the search is accepted.
                    activeFilter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //Making it easy for the user to remove a search. By clicking the view.
                            productName = null;
                            selectedFilters.removeView(view.findViewWithTag("search"));
                            priceButton.setAlpha(1);
                            alcoholButton.setAlpha(1);
                            submitFilter();
                        }
                    });
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
        searchBar.setVisibility(View.VISIBLE);
    }
    public void submitFilter(){
        view.findViewById(R.id.noResults).setVisibility(View.GONE);
        view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE); // Showing progressbar while loading
        filters.clear(); //Resetting filters

        //only adding filters that arent null

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

        //Passing filters to factory.
        factory = new ProductDataSourceFactory(database, filters);
        products = new LivePagedListBuilder<>(factory, config).build();
        initRecyclerView(); //Notifying recyclerview
    }
        private void stringFilterDialog( final String[] elements, final String field, final Filter.ComparisonType comparisonType) { //Creating alertDialog and selectedFilter view

        AlertDialog.Builder filterBuilder = new AlertDialog.Builder(getContext());
        filterBuilder.setTitle(field)
                .setItems(elements, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        selectedFilters.removeView(view.findViewWithTag(field)); // removing selecedFilters with same tag

                        TextView activeFilter = createActiveFilterView(field, elements[which]); //Creating activeFilterview with selected value
                        activeFilter.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) { //Setting onclicklistener for the new selectedFilter, making it easy to remove for the user.

                                //Ensures filter is set to correct field
                                if (field.equals(categoryString)) {
                                    selectedCategory = null;
                                    categoryButton.setAlpha(1);
                                } else if(field.equals(countryString)){
                                    selectedCountry = null;
                                    countryButton.setAlpha(1);
                                }
                                selectedFilters.removeView(view.findViewWithTag(field));
                                submitFilter();
                            }
                        });

                        StringFilter filter = new StringFilter(field, comparisonType, elements[which]); //Creating new stringfilter

                        if (field.equals(categoryString)) { //Disabling the current field button. Only one filter per button is possible. This is due to the limitations of Firestore.
                            selectedCategory = filter;
                            categoryButton.setAlpha((float) 0.3);
                        }
                        else if(field.equals(countryString)) {
                            selectedCountry = filter;
                            countryButton.setAlpha((float) 0.3);
                        }

                        submitFilter();
                    }
                });

        Dialog alertFilter = filterBuilder.create();
        alertFilter.show();
        alertFilter.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, (int)(500*dpi));
    }

    private void numberFilterDialog(final String field) { //Almost the same as StringFilterDialog, but for number filters

        final AlertDialog.Builder filterBuilder = new AlertDialog.Builder(getContext());
        filterBuilder.setTitle(field)
                .setView(getLayoutInflater().inflate(R.layout.number_filter, null)) //Using existing layout for dialog
                .setPositiveButton(view.getResources().getString(R.string.use), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        selectedFilters.removeView(view.findViewWithTag(field)); //Removing selectedFilters with same tag

                        //Getting for dialog edittext values
                        EditText numFrom = ((AlertDialog)dialog).findViewById(R.id.from);
                        EditText numTo = ((AlertDialog)dialog).findViewById(R.id.to);

                        float from = Integer.parseInt(numFrom.getText().toString().equals("") ? "0" : numFrom.getText().toString() ); //defaults to 0 if empty
                        float to = Integer.parseInt(numTo.getText().toString().equals("") ? "999999999" : numTo.getText().toString() ); //defaults to 999999999 if empty

                        NumberFilter fieldFrom = null;
                        NumberFilter fieldTo = null;

                            if (from < to) { //Verifying that from is lower than to
                                fieldFrom = new NumberFilter(from, field, Filter.ComparisonType.GREATER_THAN_OR_EQUALS);
                                if (to < 999999999){
                                    fieldTo = new NumberFilter(to, field, Filter.ComparisonType.LESS_THAN_OR_EQUALS);
                                }
                            }else { //Notifying user if from is larger than to
                                Toast.makeText(getContext(),view.getResources().getString(R.string.wont_work), Toast.LENGTH_SHORT).show();
                                numberFilterDialog(field);
                                return;
                            }

                        String unit = "";

                        if (field == priceString){ //Setting correct filter, and disabling numberfilter of other type
                            filterPriceFrom = fieldFrom;
                            filterPriceTo = fieldTo;
                            selectedFilters.removeView(view.findViewWithTag(alcoholString));
                            alcoholButton.setAlpha((float) 0.3);
                            filterAlcoholFrom = null;
                            filterAlcoholTo = null;
                            unit = getResources().getString(R.string.currency);

                        }else if( field == alcoholString) {
                            filterAlcoholFrom = fieldFrom;
                            filterAlcoholTo = fieldTo;
                            selectedFilters.removeView(view.findViewWithTag(priceString));
                            priceButton.setAlpha((float) 0.3);
                            filterPriceFrom = null;
                            filterPriceTo = null;
                            unit = "%";
                        }
                        String text = String.format("%1$s %3$s - %2$s", (int)from, to >= 999999999 ? ">" : (int)to + " " +unit, unit); //Creating text for selectedFilter view
                        TextView activeFilter = createActiveFilterView(field,text);
                        activeFilter.setOnClickListener(new View.OnClickListener() { //Adding onclicklistener for easy removal of filter
                            @Override
                            public void onClick(View v) {
                                if (field.equals(priceString)) {
                                    filterPriceFrom = null;
                                    filterPriceTo = null;

                                } else if(field.equals(alcoholString)){
                                    filterAlcoholFrom = null;
                                    filterAlcoholTo = null;
                                }
                                priceButton.setAlpha(1);
                                alcoholButton.setAlpha(1);
                                searchBar.setVisibility(View.VISIBLE);
                                selectedFilters.removeView(view.findViewWithTag(field));
                                submitFilter();
                            }
                        });
                        searchBar.setVisibility(View.GONE); //Disabling searchbar if numberfilter is set.
                        submitFilter();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.abort), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        filterBuilder.show();

    }

    private void initRecyclerView(){
        recyclerView = view.findViewById(R.id.product_recycler_view);
        passProductsToView();

        products.observe(this, new Observer<PagedList<FirestoreProduct>>() {
            @Override
            public void onChanged(@Nullable PagedList<FirestoreProduct> products) {
                productAdapter.submitList(products);
                view.findViewById(R.id.progressBar).setVisibility(View.GONE);

                if (products.size() == 0 ) // Showing/hiding no products message
                    view.findViewById(R.id.noResults).setVisibility(View.VISIBLE);
                else
                    view.findViewById(R.id.noResults).setVisibility(View.GONE);
            }
        });
    }

    private TextView createActiveFilterView (String tag, String text){ //Creating text view added to selectedFilters

        TextView activeFilter = new TextView(getContext());
        activeFilter.setTag(tag);
        activeFilter.setText(text);
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
        selectedFilters.addView(activeFilter);

        return activeFilter;
    }
    private View.OnClickListener layoutSwitchListener = new View.OnClickListener() { //Listener for switching recyclerciew layout

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
