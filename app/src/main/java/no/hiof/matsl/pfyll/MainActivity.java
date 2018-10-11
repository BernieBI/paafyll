package no.hiof.matsl.pfyll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Temporary button for starting ProductsActivity
        Button productsButton = findViewById(R.id.viewProductsBtn);

        productsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ProductsActivity started");
                Intent productActivityIntent = new Intent(MainActivity.this, ProductsActivity.class);
                startActivity(productActivityIntent);
            }
        });

    }
}
