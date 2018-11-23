package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.ItemKeyedDataSource;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import no.hiof.matsl.pfyll.model.Filter;
import no.hiof.matsl.pfyll.model.FirestoreProduct;
import no.hiof.matsl.pfyll.model.IdFilter;

/*
 * Provides Product data from the Firestore database to a PagedList. Allows for data to be acquired
 * in small chunks rather than all at once.
 * Also supports various types of data filtering, limited by what is possible using Firestore.
 */
public class ProductDataSource extends ItemKeyedDataSource<DocumentSnapshot, FirestoreProduct> {

    // Name of the collection where the Products are listed.
    private static final String COLLECTION_PATH = "Produkter";

    private FirebaseFirestore database;
    private List<Filter> filters;
    private IdFilter idFilter;

    /* Constructs a Datasource where the data is filtered by value. */
    public ProductDataSource(FirebaseFirestore database, List<Filter> filters) {
        this.database = database;
        this.filters = filters;
    }

    /* Constructs a Datasource where only specified product ids are acquired. */
    public ProductDataSource(FirebaseFirestore database, IdFilter idFilter) {
        this.database = database;
        this.idFilter = idFilter;
    }

    /*
     * Loads the first chunk of data to initialize the PagedList.
     * This method acquires data synchronously.
     */
    @Override
    public void loadInitial(@NonNull LoadInitialParams<DocumentSnapshot> params, @NonNull LoadInitialCallback<FirestoreProduct> callback) {
        loadData(null, params.requestedLoadSize, callback, false, false);
    }

    /*
     * Loads a new chunk of data when the PagedList has reached its end.
     * This method acquires data asynchronously.
     */
    @Override
    public void loadAfter(@NonNull LoadParams<DocumentSnapshot> params, @NonNull LoadCallback<FirestoreProduct> callback) {
        if (idFilter == null)
            loadData(params.key, params.requestedLoadSize, callback, true, false);
    }

    /*
     * Loads a new chunk of data when the PagedList has reached its beginning.
     * This is not useful for our application. Rather, it produced a bug where the top Product
     * of the list would be infinitely duplicated if the user scrolled up from the top of the list.
     * This method should therefore remain unimplemented.
     */
    @Override
    public void loadBefore(@NonNull LoadParams<DocumentSnapshot> params, @NonNull LoadCallback<FirestoreProduct> callback) {
        // Empty
    }

    /*
     * Gets the associated DocumentSnapshot from a Product. The snapshot is used by Firestore as a
     * "cursor" to continue where it left off from the previous request. This is essential to
     * allow for pagination of the data.
     */
    @NonNull
    @Override
    public DocumentSnapshot getKey(@NonNull FirestoreProduct item) {
        return item.getDocumentSnapshot();
    }

    /*
     * The method responsible for getting data from Firestore.
     * Depending on the current filters it will either loadDataByIdFilter or loadDataByValueFilter.
     * Is capable of both synchronous and asynchronous loading.
     */
    private void loadData(DocumentSnapshot key, int loadSize, @NonNull final LoadCallback<FirestoreProduct> callback, final boolean async, final boolean reverse) {

        // The TaskCompletionSource is used to wait for data-loading to complete during synchronous
        // loading.
        final TaskCompletionSource<List<FirestoreProduct>> taskCompletionSource = new TaskCompletionSource<>();

        // IdFilters and ValueFilters cannot be used together. IdFilters takes precedence. Therefore,
        // if an idFilter is present, the valueFilter is ignored.
        if (idFilter != null) {
            // If the call is asynchronous, pass the callback directly to the data-loading method.
            // Otherwise, pass the taskCompletionSource which calls the callback once all the data
            // is loaded.
            if (async)
                loadDataByIdFilter(callback, new ArrayList<FirestoreProduct>(), 0);
            else
                loadDataByIdFilter(taskCompletionSource, new ArrayList<FirestoreProduct>(), 0);
        } else {
            // Same as above.
            if (async)
                loadDataByValueFilter(callback, key, loadSize, reverse);
            else
                loadDataByValueFilter(taskCompletionSource, key, loadSize, reverse);
        }

        // If we are loading data asynchronously, there is no need to lock the thread and we can
        // return at once.
        if (async) {
            return;
        }

        // If data is loaded synchronously, we lock the thread with 'await'
        Task<List<FirestoreProduct>> task = taskCompletionSource.getTask();
        try {
            Tasks.await(task);
        } catch (InterruptedException | ExecutionException e) {
            task = Tasks.forException(e);
        }

        if (task.isSuccessful() && task.getResult() != null) {
            callback.onResult(task.getResult());
        } else {
            callback.onResult(new ArrayList<FirestoreProduct>(loadSize));
        }
    }

    /*
     * Applies all filters to the query. Some filters might be ignored if they are incompatible with
     * other filters.
     */
    private Query addFilters(Query query, int loadSize) {
        // There can only be a single range based filter for any given Firestore query.
        // The field on which we apply the ranged filtering is stored in this variable. If there is
        // no such filtering, the field will be an empty string.
        String rangedField = "";

        if (filters != null) {
            for (Filter filter : filters) {
                switch (filter.getComparisonType()) {
                    case EQUALS:
                        query = query.whereEqualTo(filter.getFieldName(), filter.getValue());
                        break;
                    case GREATER_THAN:
                        if (!rangedField.equals("") && !rangedField.equals(filter.getFieldName()))
                            break;
                        if (rangedField.equals("")) {
                            rangedField = filter.getFieldName();
                            query = query.orderBy(rangedField);
                        }
                        query = query.startAfter(filter.getValue());
                        break;
                    case GREATER_THAN_OR_EQUALS:
                        if (!rangedField.equals("") && !rangedField.equals(filter.getFieldName()))
                            break;
                        if (rangedField.equals("")) {
                            rangedField = filter.getFieldName();
                            query = query.orderBy(rangedField);
                        }
                        query = query.startAt(filter.getValue());
                        break;
                    case LESS_THAN:
                        if (!rangedField.equals("") && !rangedField.equals(filter.getFieldName()))
                            break;
                        if (rangedField.equals("")) {
                            rangedField = filter.getFieldName();
                            query = query.orderBy(rangedField);
                        }
                        query = query.endBefore(filter.getValue());
                        break;
                    case LESS_THAN_OR_EQUALS:
                        if (!rangedField.equals("") && !rangedField.equals(filter.getFieldName()))
                            break;
                        if (rangedField.equals("")) {
                            rangedField = filter.getFieldName();
                            query = query.orderBy(rangedField);
                        }
                        query = query.endAt(filter.getValue());
                        break;
                    case LIKE:
                        if (!rangedField.equals("") && !rangedField.equals(filter.getFieldName()))
                            break;
                        query = query.whereArrayContains(filter.getFieldName(), filter.getValue());
                        rangedField = filter.getFieldName();
                        break;
                }
            }
        }
        return query.limit(loadSize);
    }

    // TODO: Refactor to remove duplicate code
    private void loadDataByValueFilter(@NonNull final LoadCallback<FirestoreProduct> callback, DocumentSnapshot key, int loadSize, boolean reverse) {
        Query query = database.collection(COLLECTION_PATH);
        query = addFilters(query, loadSize);
        if (key != null) {
            if (reverse)
                query = query.endBefore(key);
            else
                query = query.startAfter(key);
        }

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<FirestoreProduct> products = new ArrayList<>();

                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot doc : task.getResult()){
                        FirestoreProduct product =  FirestoreProduct.documentToProduct(doc);
                        product.setBildeUrl(product.getVarenummer());
                        products.add(product);
                    }
                }
                callback.onResult(products);
            }
        });
    }
    private void loadDataByValueFilter(@NonNull final TaskCompletionSource<List<FirestoreProduct>> taskCompletionSource, DocumentSnapshot key, int loadSize, boolean reverse) {
        Query query = database.collection(COLLECTION_PATH);
        query = addFilters(query, loadSize);
        if (key != null) {
            if (reverse)
                query = query.endBefore(key);
            else
                query = query.startAfter(key);
        }

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<FirestoreProduct> products = new ArrayList<>();

                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot doc : task.getResult()){
                        FirestoreProduct product =  FirestoreProduct.documentToProduct(doc);
                        product.setBildeUrl(product.getVarenummer());
                        products.add(product);
                    }
                }

                taskCompletionSource.setResult(products);
            }
        });
    }
    private void loadDataByIdFilter(@NonNull final LoadCallback<FirestoreProduct> callback, @NonNull final List<FirestoreProduct> result, final int index) {
        final List<String> ids = idFilter.getIds();
        final int size = ids.size();

        database.collection(COLLECTION_PATH).document(ids.get(index)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null)
                        result.add(FirestoreProduct.documentToProduct(task.getResult()));
                    if ((index + 1) >= size)
                        callback.onResult(result);
                    else
                        loadDataByIdFilter(callback, result, index + 1);
                }
            }
        );
    }
    private void loadDataByIdFilter(@NonNull final TaskCompletionSource<List<FirestoreProduct>> taskCompletionSource, @NonNull final List<FirestoreProduct> result, final int index) {
        final List<String> ids = idFilter.getIds();
        final int size = ids.size();

        database.collection("Produkter").document(ids.get(index)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if (task.isSuccessful() && task.getResult() != null)
                  result.add(FirestoreProduct.documentToProduct(task.getResult()));
              if ((index + 1) >= size)
                  taskCompletionSource.setResult(result);
              else
                  loadDataByIdFilter(taskCompletionSource, result, index + 1);
                  }
              }
        );
    }

}
