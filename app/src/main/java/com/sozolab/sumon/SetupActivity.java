package com.sozolab.sumon;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.sozolab.sumon.MainApplication.isGPSEnabled;

/**
 * @author rishabh-goel on 24-09-2020
 * @project Esense
 */
public class SetupActivity extends AppCompatActivity {

    private static final String TAG = "SetupActivity";

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int GPS_ENABLE_REQUEST = 300;

    private boolean isBluetoothEnabled = false;
    private boolean isGPSProviderEnabled = false;
    private boolean isLocationPermissionGranted = false;
    private boolean isStoragePermissionGranted = false;

    private ProgressDialog progressDialog;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPermission()) {
            isLocationPermissionGranted = false;
            isStoragePermissionGranted = true;
            Log.d(TAG, "Requesting Permissions..");
            requestPermission();
        } else {
            isLocationPermissionGranted = true;
            isStoragePermissionGranted = true;
            isBluetoothEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();
            askGpsPermission();
            Log.d(TAG, "Permission already granted..");
        }
        if (isStoragePermissionGranted && isLocationPermissionGranted && isBluetoothEnabled && isGPSProviderEnabled)
            navigateToMain();
    }

    private boolean checkPermission() {
        int recordResult = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int locationResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int writeResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return locationResult == PackageManager.PERMISSION_GRANTED &&
                writeResult == PackageManager.PERMISSION_GRANTED && recordResult == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        Toast.makeText(this, "Please Allow All the Permissions First!", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION,
                WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, PERMISSION_REQUEST_CODE);

    }

    public void askLocationPermission() {
        if (!isLocationPermissionGranted) {
            requestPermission();
        } else {
            Toast.makeText(this, "Already Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    public void askStoragePermission() {
        if (!isStoragePermissionGranted) {
            requestPermission();
        } else {
            Toast.makeText(this, "Already Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    public void askGpsPermission() {
        isGPSProviderEnabled = isGPSEnabled(isLocationPermissionGranted);
        if (!isGPSProviderEnabled) {
            showGPSDiabledDialog();
        } else {
            Toast.makeText(this, "GPS is Enabled!", Toast.LENGTH_SHORT).show();
        }
    }

    public void askBluetoothPerm() {
        isBluetoothEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();
        if (!isBluetoothEnabled) {
            BluetoothAdapter.getDefaultAdapter().enable();
            showProgressDialog(true, "Enabling Bluetooth");
            handler.postDelayed(() -> {
                isBluetoothEnabled = true;
                showProgressDialog(false, null);
                findViewById(R.id.continue_Btn).performClick();
            }, 1000);
        } else {
            Toast.makeText(this, "Bluetooth is Enabled!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog(boolean isShown, String msg) {
        if (null == progressDialog) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(msg == null || "".equals(msg) ? "Progressing..." : msg);
            progressDialog.setCancelable(false);
        }
        if (!isShown) progressDialog.dismiss();
        else progressDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && storageAccepted && recordAccepted) {
                        Log.d(TAG, "Permission granted");
                    } else {
                        Log.d(TAG, "Permission denied");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to all permissions",
                                        (dialog, which) -> requestPermissions(new String[]{ACCESS_FINE_LOCATION,
                                                WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, PERMISSION_REQUEST_CODE));
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GPS_ENABLE_REQUEST) {
            askGpsPermission();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void handleClick(View view) {
        switch (view.getId()) {
            case R.id.loc_perm:
                askLocationPermission();
                break;
            case R.id.store_perm:
                askStoragePermission();
                break;
            case R.id.gps_perm:
                askGpsPermission();
                break;
            case R.id.ble_perm:
                askBluetoothPerm();
                break;
            case R.id.continue_Btn:
                if (!checkPermission()) {
                    requestPermission();
                } else if (!isGPSProviderEnabled) {
                    askGpsPermission();
                } else if (!isBluetoothEnabled) {
                    askBluetoothPerm();
                } else {
                    navigateToMain();
                }
                break;

        }
    }

    public void showGPSDiabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS Disabled");
        builder.setMessage("Gps is disabled, in order to use the application properly you need to enable GPS of your device");
        builder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_ENABLE_REQUEST);
            }
        }).setNegativeButton("No, Just Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        Dialog mGPSDialog = builder.create();
        mGPSDialog.show();
    }


    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
