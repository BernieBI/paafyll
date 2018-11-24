package no.hiof.matsl.pfyll;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler { // Library source: https://github.com/dm77/barcodescanner
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    String TAG = "ScanActivity";

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private ZXingScannerView zXingScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPrefHandler sharedPrefHandler = new SharedPrefHandler(this, "theme", "theme-cache");// Retrieving user-selected theme from sharedpreferences
        setTheme(getResources().getIdentifier(sharedPrefHandler.getTheme(), "style", this.getPackageName()));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);
        ActivityCompat.requestPermissions(ScanActivity.this, // Requesting camera permissions.
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {  // Starting scannerView on granted permissions
                zXingScannerView = new ZXingScannerView(getApplicationContext());
                setContentView(zXingScannerView);
                zXingScannerView.setResultHandler(this);
                zXingScannerView.startCamera();

            } else { //Returning to MainActivity on denied permissions
                onBackPressed();
            }
        }
    }

    @Override
    public void handleResult(Result result) { //Handling scan result

        if (result.getBarcodeFormat().toString() != "EAN_13"){ // Continuing scanning if wrong type of barcode
            Toast toast = Toast.makeText(ScanActivity.this, getResources().getString(R.string.wrong_barcode), Toast.LENGTH_LONG);
            toast.show();
            zXingScannerView.resumeCameraPreview(this);
            return;
        }
        zXingScannerView.stopCamera(); // Stopping camera on correct barcode scan

        ProgressBar progressBar = new ProgressBar(this);
        zXingScannerView.addView(progressBar); //Displaying progressbar while product search is ongoiong

        Toast.makeText(ScanActivity.this, getResources().getString(R.string.searching_products) , Toast.LENGTH_LONG).show();

        Query query = db.collection("Produkter").limit(1).whereEqualTo("HovedGTIN", Long.parseLong(result.getText())); //Querying Firestore for wanted product, limiting to one.
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    if (task.getResult().size() == 0){ //Notifying when no hit
                        Toast.makeText(ScanActivity.this, getResources().getString(R.string.no_match), Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }
                    for (QueryDocumentSnapshot document : task.getResult()) { // Starting new activity and passing product ID
                        Intent singleProductIntent = new Intent(ScanActivity.this, SingleProductActivity.class);
                        singleProductIntent.putExtra("ProductID", Integer.parseInt(document.getId()));
                        startActivity(singleProductIntent);
                        onBackPressed(); // adding onBackpressed so that when the user returnes from singleProductActivity, arrives at MainActivity.
                    }
                } else { //returning to MainActivity on error.
                    onBackPressed();
                }
            }
        });
    }


}
