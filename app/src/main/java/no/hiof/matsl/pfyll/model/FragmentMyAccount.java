package no.hiof.matsl.pfyll.model;

import android.app.AlertDialog;

import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import no.hiof.matsl.pfyll.CacheHandler;
import no.hiof.matsl.pfyll.MainActivity;
import no.hiof.matsl.pfyll.MyReviewsActivity;
import no.hiof.matsl.pfyll.R;

import no.hiof.matsl.pfyll.RecentProductsActivity;

import no.hiof.matsl.pfyll.UserListActivity;


public class FragmentMyAccount extends Fragment implements AdapterView.OnItemSelectedListener {
    SharedPref sharedPref;
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
        sharedPref = new SharedPref(getContext());

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

        ImageButton settingsButton = view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View mview = getLayoutInflater().inflate(R.layout.layout_themedialog, null);
                builder.setTitle("Velg tema");
                Spinner spinner = mview.findViewById(R.id.themeSpinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.themes));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(FragmentMyAccount.this);
                builder.setView(mview);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void restartApp(){
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        if(text.equals("Default")){
            sharedPref.setTheme(0);
            restartApp();
        }
        if(text.equals("Natt")){
            sharedPref.setTheme(1);
            restartApp();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
