package no.hiof.matsl.pfyll.model;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.CacheHandler;
import no.hiof.matsl.pfyll.MyReviewsActivity;
import no.hiof.matsl.pfyll.R;

import no.hiof.matsl.pfyll.RecentProductsActivity;
import no.hiof.matsl.pfyll.ScanActivity;
import no.hiof.matsl.pfyll.UserListActivity;

public class FragmentMyAccount extends Fragment {
    FirebaseAuth auth = FirebaseAuth.getInstance();

    //firebase
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser user;
    ArrayList<String> products = new ArrayList<>();
    Button recentButton;


    View view;
    String TAG = "MyActivityFragment";

    public FragmentMyAccount(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_myaccount,container,false);

        Log.d(TAG, "onCreate: Started ");
        user = FirebaseAuth.getInstance().getCurrentUser();
        recentButton = view.findViewById(R.id.recentButton);
        TextView welcome = view.findViewById(R.id.welcomeField);
        welcome.setText(String.format("%s, %s", getString(R.string.hello),user.getDisplayName()));

        buttons();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        CacheHandler cacheHandler = new CacheHandler(getContext(), "Recent Products", "LocalCache");
        products = cacheHandler.getRecentProducts();
        if (products != null)
            recentButton.setAlpha((float)1);
        else
            recentButton.setAlpha((float)0.5);

    }

    private void buttons() {
        Button logOutButton = view.findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                getActivity().finish();
                getActivity().startActivity(getActivity().getIntent());
            }
        });
        Button listsButton = view.findViewById(R.id.listsButton);
        listsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userListIntent = new Intent(getContext(), UserListActivity.class);
                startActivity(userListIntent);
            }
        });


        recentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (products != null){
                    Intent recentProductIntent = new Intent(getContext(), RecentProductsActivity.class);
                    startActivity(recentProductIntent);
                }

            }
        });
        Button reviewsButton = view.findViewById(R.id.reviewsButton);
        reviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reviewsIntent = new Intent(getContext(), MyReviewsActivity.class);
                startActivity(reviewsIntent);
            }
        });
    }


}
