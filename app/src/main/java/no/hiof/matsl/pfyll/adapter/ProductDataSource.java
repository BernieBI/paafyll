package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.ItemKeyedDataSource;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import no.hiof.matsl.pfyll.model.Filter;
import no.hiof.matsl.pfyll.model.IdFilter;
import no.hiof.matsl.pfyll.model.Product;

public class ProductDataSource extends ItemKeyedDataSource<Integer, Product> {
    private static final String COLLECTION_PATH = "Produkter";

    private FirebaseFirestore database;
    private List<Filter> filters;
    private IdFilter idFilter;

    public ProductDataSource(FirebaseFirestore database, List<Filter> filters) {
        this.database = database;
        this.filters = filters;
    }

    public ProductDataSource(FirebaseFirestore database, IdFilter idFilter) {
        this.database = database;
        this.idFilter = idFilter;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Product> callback) {
        loadData(0, params.requestedLoadSize, callback, false, false);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Product> callback) {
        if (idFilter == null)
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

        if (idFilter != null) {
            if (async)
                loadDataByIdFilter(callback, new ArrayList<Product>(), 0);
            else
                loadDataByIdFilter(taskCompletionSource, new ArrayList<Product>(), 0);
        } else {
            if (async)
                loadDataByValueFilter(callback, key, loadSize);
            else
                loadDataByValueFilter(taskCompletionSource, key, loadSize);
        }

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

    // TODO: Refactor to remove duplicate code
    private void loadDataByValueFilter(@NonNull final LoadCallback<Product> callback, int key, int loadSize) {
        CollectionReference collection = database.collection(COLLECTION_PATH);
        Query query = collection.orderBy("Index", Query.Direction.ASCENDING).startAfter(key).limit(loadSize);

        boolean rangeSelector = false; // There can only be one
        if (filters != null) {
            for (Filter filter : filters) {
                switch (filter.getComparisonType()) {
                    case EQUALS:
                        query = query.whereEqualTo(filter.getFieldName(), filter.getValue());
                        break;
                    case GREATER_THAN:
                        if (rangeSelector)
                            break;
                        query = query.whereGreaterThan(filter.getFieldName(), filter.getValue());
                        rangeSelector = true;
                        break;
                    case LESS_THAN:
                        if (rangeSelector)
                            break;
                        query = query.whereLessThan(filter.getFieldName(), filter.getValue());
                        rangeSelector = true;
                        break;
                }
            }
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Product> products = new ArrayList<>();

                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot doc : task.getResult()){
                        products.add(new Product().documentToProduct(doc));
                    }
                }
                callback.onResult(products);
            }
        });
    }
    private void loadDataByValueFilter(@NonNull final TaskCompletionSource<List<Product>> taskCompletionSource, int key, int loadSize) {
        CollectionReference collection = database.collection(COLLECTION_PATH);
        Query query = collection.orderBy("Index", Query.Direction.ASCENDING).startAfter(key).limit(loadSize);

        boolean rangeSelector = false; // There can only be one
        if (filters != null) {
            for (Filter filter : filters) {
                switch (filter.getComparisonType()) {
                    case EQUALS:
                        query = query.whereEqualTo(filter.getFieldName(), filter.getValue());
                        break;
                    case GREATER_THAN:
                        if (rangeSelector)
                            break;
                        query = query.whereGreaterThan(filter.getFieldName(), filter.getValue());
                        rangeSelector = true;
                        break;
                    case LESS_THAN:
                        if (rangeSelector)
                            break;
                        query = query.whereLessThan(filter.getFieldName(), filter.getValue());
                        rangeSelector = true;
                        break;
                }
            }
        }

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Product> products = new ArrayList<>();

                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot doc : task.getResult()){
                        Product product =  new Product().documentToProduct(doc);
                        product.setBildeUrl(product.getVarenummer());
                        products.add(product);
                    }
                }
                taskCompletionSource.setResult(products);
            }
        });
    }
    private void loadDataByIdFilter(@NonNull final LoadCallback<Product> callback, @NonNull final List<Product> result, final int index) {
        final List<String> ids = idFilter.getIds();
        final int size = ids.size();

        database.collection(COLLECTION_PATH).document(ids.get(index)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null)
                        result.add(new Product().documentToProduct(task.getResult()));
                    if ((index + 1) >= size)
                        callback.onResult(result);
                    else
                        loadDataByIdFilter(callback, result, index + 1);
                }
            }
        );
    }
    private void loadDataByIdFilter(@NonNull final TaskCompletionSource<List<Product>> taskCompletionSource, @NonNull final List<Product> result, final int index) {
        final List<String> ids = idFilter.getIds();
        final int size = ids.size();

        database.collection("Produkter").document(ids.get(index)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                  @Override
                                                                                                  public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if (task.isSuccessful() && task.getResult() != null)
                  result.add(new Product().documentToProduct(task.getResult()));
              if ((index + 1) >= size)
                  taskCompletionSource.setResult(result);
              else
                  loadDataByIdFilter(taskCompletionSource, result, index + 1);
                  }
              }
        );
    }

}
