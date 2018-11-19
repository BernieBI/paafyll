package no.hiof.matsl.pfyll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import no.hiof.matsl.pfyll.MainActivity;
import no.hiof.matsl.pfyll.R;
import no.hiof.matsl.pfyll.model.SharedPref;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        Intent intent = new Intent(getApplicationContext(),
                MainActivity.class);
        startActivity(intent);
        finish();

    }
}
