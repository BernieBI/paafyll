package no.hiof.matsl.pfyll.model;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import no.hiof.matsl.pfyll.ProductsActivity;
import no.hiof.matsl.pfyll.R;

public class FragmentProducts extends Fragment {
    View view;
    String TAG = "MainActivity";
    public FragmentProducts(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products, container, false);
        Button productsButton = view.findViewById(R.id.viewProductsBtn);

        //Temporary button for starting ProductsActivity
        productsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ProductsActivity started");
                Intent productActivityIntent = new Intent(FragmentProducts.this.getActivity(), ProductsActivity.class);
                getActivity().startActivity(productActivityIntent);
            }
        });

        return view;


    }
}
