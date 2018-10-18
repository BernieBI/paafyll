package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.ItemKeyedDataSource;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import no.hiof.matsl.pfyll.model.Product;

public class ProductDataSource extends ItemKeyedDataSource<String, Product> {

    private DatabaseReference databaseReference;

    public ProductDataSource(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull LoadInitialCallback<Product> callback) {
        loadData(params.requestedInitialKey, params.requestedLoadSize, callback, false);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<Product> callback) {
        loadData(params.key, params.requestedLoadSize, callback, true);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<Product> callback) {

    }

    @NonNull
    @Override
    public String getKey(@NonNull Product item) {
        return item.getVarenummer();
    }

    private void loadData(String key, int loadSize, @NonNull final LoadCallback<Product> callback, final boolean async) {

        final TaskCompletionSource<List<Product>> taskCompletionSource = new TaskCompletionSource<>();
        Query query = databaseReference.child("Produkter");
        query.orderByChild("varenummer")
                .startAt(key)
                .limitToFirst(loadSize)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Product> products = new ArrayList<>();

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Product product = child.getValue(Product.class);
                            if (product == null) {
                                continue;
                            }
                            product.setFirebaseID(child.getKey());
                            product.setBildeUrl(product.getVarenummer());
                            products.add(product);
                        }
                        if (!async) {
                            taskCompletionSource.setResult(products);
                        } else {
                            callback.onResult(products);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        taskCompletionSource.setException(databaseError.toException());
                    }
                });

        if (async) {
            return;
        }

        Task<List<Product>> task = taskCompletionSource.getTask();

        try {
            Tasks.await(task);
        } catch (InterruptedException | ExecutionException e) {
            task = Tasks.forException(e);
        }

        if (task.isSuccessful() && task.getResult() != null) {
            callback.onResult(task.getResult());
        } else {
            callback.onResult(new ArrayList<Product>(loadSize));
        }
    }
}
