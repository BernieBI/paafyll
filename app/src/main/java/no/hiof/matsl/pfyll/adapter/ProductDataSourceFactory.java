package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.DataSource;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import no.hiof.matsl.pfyll.model.Product;

public class ProductDataSourceFactory extends DataSource.Factory<Integer, Product> {

    private FirebaseFirestore database;

    public ProductDataSourceFactory(FirebaseFirestore database) {
        this.database = database;
    }

    @Override
    public DataSource<Integer, Product> create() {
        return new ProductDataSource(database);
    }
}
