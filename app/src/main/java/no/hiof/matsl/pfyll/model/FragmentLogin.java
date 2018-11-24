package no.hiof.matsl.pfyll.model;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.firebase.ui.auth.AuthUI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import no.hiof.matsl.pfyll.SharedPrefHandler;
import no.hiof.matsl.pfyll.R;

public class FragmentLogin extends Fragment {

    View view;
    private static int RC_SIGN_IN = 100;
    String TAG = "FragmentLogin";

    public FragmentLogin () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login,container,false);
        final SharedPrefHandler sharedPrefHandler = new SharedPrefHandler(getContext(), "theme", "theme-cache");
        Button button = view.findViewById(R.id.loginButton);

        if (isNetworkAvailable()) {

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build()
                    );
                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setLogo(R.drawable.logo)
                                    .setTheme(getResources().getIdentifier(sharedPrefHandler.getTheme(), "style", Objects.requireNonNull(getActivity()).getPackageName()))
                                    .build(),
                            RC_SIGN_IN);
                }
            });
        }else{
            reTryConnection();
        }
        return view;
    }

    private boolean isNetworkAvailable() { // Gotten at: https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void reTryConnection(){ // Restarting activity to check internet again
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
}
