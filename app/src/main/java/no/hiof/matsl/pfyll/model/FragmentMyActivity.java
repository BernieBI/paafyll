package no.hiof.matsl.pfyll.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.adapter.ProductDataSourceFactory;
import no.hiof.matsl.pfyll.adapter.ProductRecycleViewAdapter;
import no.hiof.matsl.pfyll.RecentProductsActivity;

public class FragmentMyActivity extends Fragment {
    private LiveData<PagedList<Product>> products;
    private ProductRecycleViewAdapter productAdapter;
    private RecyclerView recyclerView;
    private int layoutColumns = 2;
    private ArrayList<String> productsInList;
    private FloatingActionButton layoutButton;
    private Button logoutButton;
    private GridLayoutManager gridLayoutManager;
    private ArrayList<UserReview> userReviews = new ArrayList<>();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    //firebase
    final private FirebaseFirestore database = FirebaseFirestore.getInstance();


    View view;
    String TAG = "MyActivityFragment";

    public FragmentMyActivity(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_myactivity,container,false);

        PagedList.Config config = new PagedList.Config.Builder().setPageSize(6).build();

        ProductDataSourceFactory factory = new ProductDataSourceFactory(database);

        initRecyclerView();

        layoutButton = view.findViewById(R.id.layoutButton);
        layoutButton.setOnClickListener(layoutSwitchListener);

        final Button recentProductsButton = view.findViewById(R.id.recentProductsButton);
        recentProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RecentProductsActivity.class);
                startActivity(intent);
            }
        });

        logoutButton = view.findViewById(R.id.logOutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                getActivity().finish();
                getActivity().startActivity(getActivity().getIntent());
            }
        });
        return view;
    }

    private void initRecyclerView(){

      

    }




    private View.OnClickListener layoutSwitchListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {

            if (layoutColumns == 2){
                layoutColumns = 1;
                layoutButton.setImageDrawable(getActivity().getDrawable(R.drawable.grid));
                productAdapter.setLayout(true);
            }else{
                layoutColumns = 2;
                layoutButton.setImageDrawable(getActivity().getDrawable(R.drawable.list));
                productAdapter.setLayout(false);
            }
            gridLayoutManager.setSpanCount(layoutColumns);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        initRecyclerView();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onstop");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
