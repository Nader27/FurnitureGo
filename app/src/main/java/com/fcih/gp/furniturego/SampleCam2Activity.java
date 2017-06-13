package com.fcih.gp.furniturego;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.wikitude.architect.services.camera.CameraLifecycleListener;

public class SampleCam2Activity extends AutoHdSampleCamActivity {

    @Override
    protected boolean getCamera2Enabled() {
        return getIntent().getBooleanExtra("enableCamera2", true);
    }

    private CameraLifecycleListener cameraLifecycleListener = new CameraLifecycleListener() {
        @Override
        public void onCameraOpen() {

        }

        @Override
        public void onCameraReleased() {

        }

        @Override
        public void onCameraOpenAbort() {
            /*
                This is a workaround for some devices whose camera2 implementation is not working as expected.
            */
            if (getCamera2Enabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                AlertDialog alertDialog = new AlertDialog.Builder(SampleCam2Activity.this).create();
                alertDialog.setTitle("Camera2 issue.");
                alertDialog.setMessage("There was an unexpected issue with this devices camera2. Should this activity be recreated with the old camera api?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(SampleCam2Activity.this, SampleCam2Activity.class);
                        intent.putExtra("enableCamera2", false);
                        intent.putExtras(getIntent());
                        finish();
                        startActivity(intent);
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recreate();
                            }
                        });
                    }
                });
                alertDialog.show();
            } else {
                Toast.makeText(SampleCam2Activity.this, "Camera could not be started.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected CameraLifecycleListener getCameraLifecycleListener() {
        return cameraLifecycleListener;
    }
}
