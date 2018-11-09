package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.ItemKeyedDataSource;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import no.hiof.matsl.pfyll.model.Product;

public class ProductDataSource extends ItemKeyedDataSource<Integer, Product> {

    //private DatabaseReference databaseReference;
    private DocumentReference documentReference;

    public ProductDataSource(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Product> callback) {
        loadData(0, params.requestedLoadSize, callback, false, false);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Product> callback) {
        loadData(params.key + 1, params.requestedLoadSize, callback, true, false);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Product> callback) {
        //loadData(params.key, params.requestedLoadSize, callback, true, true);
    }

    @NonNull
    @Override
    public Integer getKey(@NonNull Product item) {
        return item.getId();
    }

    private void loadData(Integer key, int loadSize, @NonNull final LoadCallback<Product> callback, final boolean async, final boolean reverse) {

        final TaskCompletionSource<List<Product>> taskCompletionSource = new TaskCompletionSource<>();
        CollectionReference query = documentReference.collection("Products");

        query.limit(loadSize).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Product> products = new ArrayList<>();

                if (task.isSuccessful()) {

                }
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product == null) {
                        continue;
                    }
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
