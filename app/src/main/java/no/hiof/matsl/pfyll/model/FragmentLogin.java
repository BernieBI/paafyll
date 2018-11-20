package no.hiof.matsl.pfyll.model;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import no.hiof.matsl.pfyll.CacheHandler;
import no.hiof.matsl.pfyll.R;

public class FragmentLogin extends Fragment {

    View view;
    private static int RC_SIGN_IN = 100;
    String TAG = "FragmentLogin";

    public FragmentLogin () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login,container,false);
        final CacheHandler cacheHandler = new CacheHandler(getContext(), "theme", "theme-cache");
        Button button = view.findViewById(R.id.loginButton);

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
                                .setTheme(getResources().getIdentifier(cacheHandler.getTheme(), "style", Objects.requireNonNull(getActivity()).getPackageName()))
                                .build(),
                        RC_SIGN_IN);
            }
        });


        return view;
    }


}
