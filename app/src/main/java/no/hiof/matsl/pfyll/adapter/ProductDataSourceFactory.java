package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.DataSource;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import no.hiof.matsl.pfyll.model.Filter;
import no.hiof.matsl.pfyll.model.FirestoreProduct;
import no.hiof.matsl.pfyll.model.IdFilter;
import no.hiof.matsl.pfyll.model.Product;

public class ProductDataSourceFactory extends DataSource.Factory<DocumentSnapshot, FirestoreProduct> {

    private FirebaseFirestore database;
    private List<Filter> filters;
    private IdFilter idFilter;

    public ProductDataSourceFactory(@NonNull FirebaseFirestore database) {
        this.database = database;
    }

    public ProductDataSourceFactory(@NonNull FirebaseFirestore database, @NonNull IdFilter idFilter) {
        this(database);
        this.idFilter = idFilter;
    }
    public ProductDataSourceFactory (@NonNull FirebaseFirestore database,  @NonNull List<Filter> filters) {
        this(database);
        this.filters = filters;
    }

    @Override
    public DataSource<DocumentSnapshot, FirestoreProduct> create() {
        if (idFilter != null)
            return new ProductDataSource(database, idFilter);
        else
            return new ProductDataSource(database, filters);
    }
}
