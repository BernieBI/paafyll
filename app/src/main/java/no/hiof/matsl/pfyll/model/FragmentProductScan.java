package no.hiof.matsl.pfyll.model;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import no.hiof.matsl.pfyll.R;

public class FragmentDrinklog extends Fragment {
    View view;
    public FragmentDrinklog(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_drinklog,container,false);
        return view;
    }
}
