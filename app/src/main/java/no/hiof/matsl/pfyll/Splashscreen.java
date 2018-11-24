package no.hiof.matsl.pfyll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Followed guide found at: https://medium.com/viithiisys/android-perfect-way-to-create-splash-screen-ca3c5bee137f
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen); //Displaying app logo

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent); //Starting MainActivity
        finish();

    }
}
