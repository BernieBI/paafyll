package no.hiof.matsl.pfyll.model;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.RecentProductsActivity;

public class FragmentMyActivity extends Fragment {
    View view;
    public FragmentMyActivity(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_myactivity,container,false);

        final Button recentProductsButton = view.findViewById(R.id.recentProductsButton);
        recentProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RecentProductsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
