package no.hiof.matsl.pfyll;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class Querier {
    public static final int DEFAULT_LIMIT = 100;
    public static final int DEFAULT_OFFSET = 1;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private int limit;
    private int offset;
    private Ordering ordering;
    private List<Filter> filters;


    public Querier() {
        this.database =  FirebaseDatabase.getInstance();
        this.databaseReference = database.getReference("Products");
        this.offset = DEFAULT_OFFSET;
        this.limit = DEFAULT_LIMIT;
        this.ordering = null;
        this.filters = new ArrayList<>();
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

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setOrdering(Ordering ordering) {
        this.ordering = ordering;
    }

    public void addListenerForSingleValueEvent(ValueEventListener valueEventListener) {
        Query query = databaseReference.limitToFirst(limit);
        if (ordering != null) {
            query = query.orderByChild(ordering.getKey());
        }
        for (Filter filter : filters) {
            switch (filter.getType()) {
                case DOUBLE:
                    if (filter.isLowerBounded()) {
                        query = query.startAt(filter.getLowerAsDouble(), filter.getKey());
                    }
                    if (filter.isUpperBounded()) {
                        query = query.endAt(filter.getUpperAsDouble(), filter.getKey());
                    }
                    break;
                case BOOLEAN:
                    if (filter.isLowerBounded()) {
                        query = query.startAt(filter.getLowerAsBoolean(), filter.getKey());
                    }
                    if (filter.isLowerBounded()) {
                        query = query.endAt(filter.getUpperAsBoolean(), filter.getKey());
                    }
                    break;
                case STRING:
                    if (filter.isLowerBounded()) {
                        query = query.startAt(filter.getLowerAsString(), filter.getKey());
                    }
                    if (filter.isLowerBounded()) {
                        query = query.endAt(filter.getUpperAsString(), filter.getKey());
                    }
                    break;
                default:
                    break;
            }
        }
        query.addListenerForSingleValueEvent(valueEventListener);
    }
}
