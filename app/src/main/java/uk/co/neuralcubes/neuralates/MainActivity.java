package uk.co.neuralcubes.neuralates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.orbotix.common.Robot;

import java.util.ArrayList;
import java.util.List;

import uk.co.neuralcubes.neuralates.sphero.SpheroManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Neuralates";
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView)findViewById(R.id.control1).findViewById(R.id.player_label);
        tv.setText(getResources().getString(R.string.player, 1));
        tv = (TextView)findViewById(R.id.control2).findViewById(R.id.player_label);
        tv.setText(getResources().getString(R.string.player, 2));

        requestLocationPermission();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onStart() {
        super.onStart();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            SpheroManager.getInstance().startDiscovery(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpheroManager.getInstance().stopDiscovery();
        for (Robot m: SpheroManager.getInstance().getRobots()) {
            m.sleep();
        }
    }

    private void requestLocationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if(hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG + ".Permissions", "Location permission has not already been granted");
                List<String> permissions = new ArrayList<>();
                permissions.add( Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                Log.d(TAG + ".Permissions", "Location permission already granted");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                for(int i = 0; i < permissions.length; i++ ) {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        SpheroManager.getInstance().startDiscovery(this);
                        Log.d(TAG + ".Permissions", "Permission Granted: " + permissions[i]);
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG + ".Permissions", "Permission Denied: " + permissions[i]);
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
