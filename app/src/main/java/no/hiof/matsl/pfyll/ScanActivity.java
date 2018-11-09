package no.hiof.matsl.pfyll;

import android.Manifest;
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
        Log.d(TAG, "handleresult");
        zXingScannerView.stopCamera();
        ProgressBar progressBar = new ProgressBar(this);
        zXingScannerView.addView(progressBar);
        Query query = db.collection("Produkter").limit(1).whereEqualTo("HovedGTIN", Long.parseLong(result.getText()));
        query.get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            FragmentProducts.BarcodeReturn(Integer.parseInt(document.getId()));
                            onBackPressed();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });


    }
}
