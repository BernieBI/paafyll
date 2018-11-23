package no.hiof.matsl.pfyll.model;

import android.app.AlertDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.okhttp.Cache;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import no.hiof.matsl.pfyll.CacheHandler;
import no.hiof.matsl.pfyll.MainActivity;
import no.hiof.matsl.pfyll.MapActivity;
import no.hiof.matsl.pfyll.MyReviewsActivity;
import no.hiof.matsl.pfyll.R;

import no.hiof.matsl.pfyll.RecentProductsActivity;

import no.hiof.matsl.pfyll.SingleProductActivity;
import no.hiof.matsl.pfyll.UserListActivity;


public class FragmentMyAccount extends Fragment {
    SharedPref sharedPref;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    //firebase
    final private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser user;
    private static int RC_SIGN_IN = 100;
    ArrayList<String> products = new ArrayList<>();
    Button recentButton;
    TextView welcome;

    private static final int ERROR_DIALOG_REQUEST = 9001;
    View view;
    String TAG = "MyActivityFragment";

    public FragmentMyAccount(){
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_myaccount,container,false);
        sharedPref = new SharedPref(getContext());


        if (isNetworkAvailable()) {
            recentButton = view.findViewById(R.id.recentButton);
            welcome = view.findViewById(R.id.welcomeField);
            user = FirebaseAuth.getInstance().getCurrentUser();
            welcome.setText(String.format("%s, %s", getString(R.string.hello), user.getDisplayName().trim().equals("") ? "Anonym" : user.getDisplayName()));
            Button btnMap = (Button) view.findViewById(R.id.mapbtn);
            btnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (isServicesOK()) {
                        Intent intent = new Intent(getContext(), MapActivity.class);
                        startActivity(intent);
                    }
                }
            });

        }

        return view;
    }

    private boolean isNetworkAvailable() { // Hentet fra https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void reTryConnection(){
        view.findViewById(R.id.noConnection).setVisibility(View.VISIBLE);
        Button button = view.findViewById(R.id.retryConnection);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getActivity().getIntent();
                getActivity().finish();
                startActivity(intent);
            }
        });
    }

    private void editAccount() {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getResources().getString(R.string.update_profile));
                final String[] choices = {getResources().getString(R.string.change_name)};
                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fieldEdit(choices[which]);
                    }
                }).setNegativeButton(getResources().getString(R.string.abort), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();

    }


    private void fieldEdit(final String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(field);
        float dpi = getResources().getDisplayMetrics().density;
        final LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding((int)(30*dpi), (int)(8*dpi), (int)(30*dpi), (int)(5*dpi));
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        input.setText(user.getDisplayName());
        layout.addView(input);

        builder.setView(layout);
        builder.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                if (input.getText().toString().trim().equals("")){
                    Toast.makeText(getContext(), getResources().getString(R.string.require_username), Toast.LENGTH_SHORT).show();
                    return;
                }
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(input.getText().toString().trim())
                        .build();
                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.account_updated), Toast.LENGTH_SHORT).show();
                                    welcome.setText(String.format("%s, %s", getString(R.string.hello), user.getDisplayName().trim().equals("") ? "Anonym" : user.getDisplayName() ));
                                }
                            }
                        });
                    database.getReference("users/" + user.getUid()).child("Name").setValue(input.getText().toString().trim());
            }
        }).setNegativeButton(getResources().getString(R.string.abort), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    @Override
    public void onResume() {
        super.onResume();
        CacheHandler cacheHandler = new CacheHandler(getContext(), "Recent Products", "LocalCache");
        products = cacheHandler.getRecentProducts();
        if (isNetworkAvailable()) {
            if (products != null)
                recentButton.setAlpha((float) 1);
            else
                recentButton.setAlpha((float) 0.5);
        }else{
            reTryConnection();
            view.findViewById(R.id.reviewsButton).setAlpha((float)0.3);
            view.findViewById(R.id.listsButton).setAlpha((float)0.3);
            view.findViewById(R.id.accountButton).setAlpha((float)0.3);
            view.findViewById(R.id.recentButton).setAlpha((float)0.3);
            view.findViewById(R.id.mapbtn).setAlpha((float)0.3);
        }
        buttons();
    }


    public boolean isServicesOK(){

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
        }
        return false;
    }

    private void buttons() {

        if (isNetworkAvailable()) {
            ImageButton adminButton = view.findViewById(R.id.accountButton);
            adminButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editAccount();
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

                    if (products != null) {
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
        final Button logOutButton = view.findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                getActivity().finish();
                getActivity().startActivity(getActivity().getIntent());
            }
        });
        ImageButton settingsButton = view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CacheHandler cacheHandler = new CacheHandler(getContext(), "theme", "theme-cache");
                final String[] themes = getResources().getStringArray(R.array.themes);
                int i = 0;
                while ( i < themes.length){
                    if (themes[i].equals(cacheHandler.getTheme())) break;
                    i++;
                }

                AlertDialog.Builder themeChanger = new AlertDialog.Builder(getContext());
                themeChanger.setTitle("Velg tema")
                       .setSingleChoiceItems(themes, i, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               cacheHandler.setTheme(themes[which]);
                               restartApp();
                           }
                       }).setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                           }
                       });
                themeChanger.show();
            }
        });
    }

    public void restartApp(){
        getActivity().finish();
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }

}
