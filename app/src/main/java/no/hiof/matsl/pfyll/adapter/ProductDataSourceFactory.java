package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.DataSource;

import com.google.firebase.database.DatabaseReference;

import no.hiof.matsl.pfyll.model.Product;

public class ProductDataSourceFactory extends DataSource.Factory<String, Product> {

    private DatabaseReference databaseReference;

    public ProductDataSourceFactory(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    @Override
    public DataSource<String, Product> create() {
        return new ProductDataSource(databaseReference);
    }
}
