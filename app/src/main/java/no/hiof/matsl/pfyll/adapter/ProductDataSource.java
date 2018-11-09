package no.hiof.matsl.pfyll.adapter;

import android.arch.paging.ItemKeyedDataSource;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import no.hiof.matsl.pfyll.model.BooleanFilter;
import no.hiof.matsl.pfyll.model.Filter;
import no.hiof.matsl.pfyll.model.NumberFilter;
import no.hiof.matsl.pfyll.model.Product;
import no.hiof.matsl.pfyll.model.StringFilter;

public class ProductDataSource extends ItemKeyedDataSource<Integer, Product> {

    private FirebaseFirestore database;
    private List<Filter> filters;

    public ProductDataSource(FirebaseFirestore database, List<Filter> filters) {
        this.database = database;
        this.filters = filters;
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
        CollectionReference collection = database.collection("Produkter");

        Query query = collection.orderBy("Index", Query.Direction.ASCENDING).startAfter(key).limit(loadSize);

        boolean rangeSelector = false; // There can only be one
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
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Product> products = new ArrayList<>();

                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot doc : task.getResult()){
                        //Product product = doc.toObject(Product.class);
                        Product product = new Product(
                                Integer.parseInt(doc.getId()),
                                stringify(doc.get("Alkohol")),
                                stringify(doc.get("Argang")),
                                stringify(doc.get("Biodynamisk")),
                                stringify(doc.get("Bitterhet")),
                                stringify(doc.get("Butikkategori")),
                                stringify(doc.get("Datotid")),
                                stringify(doc.get("Distributor")),
                                stringify(doc.get("Distrikt")),
                                stringify(doc.get("Emballasjetype")),
                                stringify(doc.get("Fairtrade")),
                                stringify(doc.get("Farge")),
                                stringify(doc.get("Friskhet")),
                                stringify(doc.get("Fylde")),
                                stringify(doc.get("Garvestoffer")),
                                stringify(doc.get("Gluten_lav_pa")),
                                stringify(doc.get("Grossist")),
                                stringify(doc.get("Korktype")),
                                stringify(doc.get("Kosher")),
                                stringify(doc.get("Lagringsgrad")),
                                stringify(doc.get("Land")),
                                stringify(doc.get("Literpris")),
                                stringify(doc.get("Lukt")),
                                stringify(doc.get("Metode")),
                                stringify(doc.get("Miljosmart_emballasje")),
                                stringify(doc.get("Okologisk")),
                                stringify(doc.get("Passertil01")),
                                stringify(doc.get("Passertil02")),
                                stringify(doc.get("Passertil03")),
                                stringify(doc.get("Pris")),
                                stringify(doc.get("Produktutvalg")),
                                stringify(doc.get("Produsent")),
                                stringify(doc.get("Rastoff")),
                                stringify(doc.get("Smak")),
                                stringify(doc.get("Sodme")),
                                stringify(doc.get("Sukker")),
                                stringify(doc.get("Syre")),
                                stringify(doc.get("Underdistrikt")),
                                stringify(doc.get("Varenavn")),
                                stringify(doc.get("Varenummer")),
                                stringify(doc.get("Varetype")),
                                stringify(doc.get("Vareurl")),
                                stringify(doc.get("Volum")),
                                stringify(doc.get("HovedGTIN"))
                        );
                        product.setBildeUrl(product.getVarenummer());
                        products.add(product);
                    }
                }

                if (!async) {
                    taskCompletionSource.setResult(products);
                } else {
                    callback.onResult(products);
                }
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

    private String stringify(Object object) {
        return (object == null)
                ? ""
                : object.toString();
    }
}
