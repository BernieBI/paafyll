package no.hiof.matsl.pfyll;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import no.hiof.matsl.pfyll.model.FragmentProductScan;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

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
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
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
        Toast.makeText(getApplicationContext(),result.getText(),Toast.LENGTH_SHORT).show();
        zXingScannerView.stopCamera();
        FragmentProductScan.BarcodeReturn(result.getText());
        onBackPressed();
    }
}
