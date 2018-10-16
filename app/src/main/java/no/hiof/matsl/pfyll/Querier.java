package no.hiof.matsl.pfyll;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.hiof.matsl.pfyll.model.Product;

public class Querier {
    public static final int DEFAULT_LIMIT = 100;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private int limit;
    private Ordering ordering;
    private List<Filter> filters;


    public Querier() {
        this.database =  FirebaseDatabase.getInstance();
        this.databaseReference = database.getReference();
        this.limit = DEFAULT_LIMIT;
        this.ordering = null;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void addFilters(List<Filter> filters) {
        this.filters.addAll(filters);
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    public void setDatalimit(int limit) {
        this.limit = limit;
    }

    public void setOrdering(Ordering ordering) {
        this.ordering = ordering;
    }

    public List<Product> getData(int offset) {
        final List<Product> data = new ArrayList<>();

        Query query = databaseReference.child("Products");
        if (ordering != null) {
            query.orderByChild(ordering.getKey());
        }
        for (Filter filter : filters) {
            switch (filter.getType()) {
                case DOUBLE:
                    if (filter.isLowerBounded()) {
                        query.startAt(filter.getLowerAsDouble(), filter.getKey());
                    }
                    if (filter.isUpperBounded()) {
                        query.endAt(filter.getUpperAsDouble(), filter.getKey());
                    }
                    break;
                case BOOLEAN:
                    if (filter.isLowerBounded()) {
                        query.startAt(filter.getLowerAsBoolean(), filter.getKey());
                    }
                    if (filter.isLowerBounded()) {
                        query.endAt(filter.getUpperAsBoolean(), filter.getKey());
                    }
                    break;
                case STRING:
                    if (filter.isLowerBounded()) {
                        query.startAt(filter.getLowerAsString(), filter.getKey());
                    }
                    if (filter.isLowerBounded()) {
                        query.endAt(filter.getUpperAsString(), filter.getKey());
                    }
                    break;
                default:
                    break;
            }
        }
        query.limitToFirst(offset);
        query.limitToLast(offset + limit);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    data.add(d.getValue(Product.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (ordering != null && ordering.getMode() == Ordering.Mode.DESCENDING) {
            Collections.reverse(data);
        }
        return data;
    }


}
