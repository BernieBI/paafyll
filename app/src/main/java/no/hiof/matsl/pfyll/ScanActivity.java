package no.hiof.matsl.pfyll;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import no.hiof.matsl.pfyll.model.FragmentProducts;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    String TAG = "ScanActivity";

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private ZXingScannerView zXingScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Created");
        setContentView(R.layout.activity_scan);
        ActivityCompat.requestPermissions(ScanActivity.this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_CAMERA);
    }

    public void scan(View view){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                zXingScannerView = new ZXingScannerView(getApplicationContext());
                setContentView(zXingScannerView);
                zXingScannerView.setResultHandler(this);
                zXingScannerView.startCamera();

            } else {
                onBackPressed();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "paused");
    }

    @Override
    public void handleResult(Result result) {

        Log.d(TAG, "handleResult: " + result.getBarcodeFormat());
        if (result.getBarcodeFormat().toString() != "EAN_13"){
            Toast toast = Toast.makeText(ScanActivity.this, "Feil type strekkode", Toast.LENGTH_LONG);
            toast.show();
            zXingScannerView.resumeCameraPreview(this);
            return;
        }
        zXingScannerView.stopCamera();
        ProgressBar progressBar = new ProgressBar(this);
        zXingScannerView.addView(progressBar);
        Toast toast = Toast.makeText(ScanActivity.this, "Søker etter produkt", Toast.LENGTH_LONG);
        toast.show();
        Query query = db.collection("Produkter").limit(1).whereEqualTo("HovedGTIN", Long.parseLong(result.getText()));
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: " + task.getResult().size());
                    if (task.getResult().size() == 0){
                        Toast toast = Toast.makeText(ScanActivity.this, "Fant ikke produktet", Toast.LENGTH_LONG);
                        toast.show();
                        onBackPressed();
                    }
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        Toast toast = Toast.makeText(ScanActivity.this, "Henter produktet", Toast.LENGTH_SHORT);
                        toast.show();

                        Intent singleProductIntent = new Intent(ScanActivity.this, SingleProductActivity.class);
                        singleProductIntent.putExtra("ProductID", Integer.parseInt(document.getId()));
                        startActivity(singleProductIntent);
                        onBackPressed();
                    }
                } else {

                    onBackPressed();
                }
            }
        });
    }


}
