package dev.docas.magictrapgo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If the app is started again while the
        // floating window service is running
        // then the floating window service will stop
        if (isMyServiceRunning()) {
            // onDestroy() method in FloatingWindowGFG
            // class will be called here
            stopService(new Intent(MainActivity.this, FloatingWindowIVCalculator.class));
            stopService(new Intent(MainActivity.this, FloatingWindowButton.class));
        }

        openFloatingScreen();
    }

    private boolean isMyServiceRunning() {
        // The ACTIVITY_SERVICE is needed to retrieve a
        // ActivityManager for interacting with the global system
        // It has a constant String value "activity".
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        // A loop is needed to get Service information that are currently running in the System.
        // So ActivityManager.RunningServiceInfo is used. It helps to retrieve a
        // particular service information, here its this service.
        // getRunningServices() method returns a list of the services that are currently running
        // and MAX_VALUE is 2147483647. So at most this many services can be returned by this method.
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            // If this service is found as a running, it will return true or else false.
            if (FloatingWindowIVCalculator.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void openFloatingScreen(){
        if (checkOverlayDisplayPermission()) {
            // FloatingWindowGFG service is started
            startService(new Intent(MainActivity.this, FloatingWindowButton.class));
            // The MainActivity closes here
            finish();
            return;
        }

        requestOverlayDisplayPermission();
    }

    private void requestOverlayDisplayPermission() {
        // An AlertDialog is created
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // This dialog can be closed, just by
        // taping outside the dialog-box
        builder.setCancelable(true);

        // The title of the Dialog-box is set
        builder.setTitle("Screen Overlay Permission Needed");

        // The message of the Dialog-box is set
        builder.setMessage("Enable 'Display over other apps' from System Settings.");

        // The event of the Positive-Button is set
        builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The app will redirect to the 'Display over other apps' in Settings.
                // This is an Implicit Intent. This is needed when any Action is needed
                // to perform, here it is
                // redirecting to an other app(Settings).
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));

                // This method will start the intent. It takes two parameter,
                // one is the Intent and the other is
                // an requestCode Integer. Here it is -1.
                startActivityForResult(intent, RESULT_OK);
            }
        });
        dialog = builder.create();
        // The Dialog will show in the screen
        dialog.show();
    }

    private boolean checkOverlayDisplayPermission() {
        // If 'Display over other apps' is not enabled it
        // will return false or else true
        return Settings.canDrawOverlays(this);
    }
}