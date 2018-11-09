package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.DataSource;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import no.hiof.matsl.pfyll.model.Filter;
import no.hiof.matsl.pfyll.model.Product;

public class ProductDataSourceFactory extends DataSource.Factory<Integer, Product> {

    private FirebaseFirestore database;
    private List<Filter> filters;

    public ProductDataSourceFactory(FirebaseFirestore database, List<Filter> filters) {
        this.database = database;
        this.filters = filters;
    }

    public ProductDataSourceFactory(FirebaseFirestore database) {
        this(database, new ArrayList<Filter>());
    }

    @Override
    public DataSource<Integer, Product> create() {
        return new ProductDataSource(database, filters);
    }
}
