package com.fcih.gp.furniturego;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.CaptureScreenCallback;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
import com.wikitude.common.camera.CameraSettings;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;

public class SampleCamActivity extends AbstractArchitectCamActivity {

	private static final String TAG = "SampleCamActivity";
    private static final int WIKITUDE_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 3;
    protected Bitmap screenCapture = null;
    /**
     * last time the calibration toast was shown, this avoids too many toast shown when compass needs calibration
     */
    private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();

	@Override
	public String getARchitectWorldPath() {
		return "index.html";
	}

	@Override
	public String getActivityTitle() {
        return "AR";
    }

	@Override
	public int getContentViewId() {
		return R.layout.sample_cam;
	}

	@Override
	public int getArchitectViewId() {
		return R.id.architectView;
	}
	
	@Override
	public String getWikitudeSDKLicenseKey() {
		return WikitudeSDKConstants.WIKITUDE_SDK_KEY;
	}
	
	@Override
	public SensorAccuracyChangeListener getSensorAccuracyListener() {
        return accuracy -> {
            /* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3 */
            if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && SampleCamActivity.this != null && !SampleCamActivity.this.isFinishing() && System.currentTimeMillis() - SampleCamActivity.this.lastCalibrationToastShownTimeMillis > 5 * 1000) {
                Toast.makeText(SampleCamActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG).show();
                SampleCamActivity.this.lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
            }
        };
    }

    @Override
    public ArchitectJavaScriptInterfaceListener getArchitectJavaScriptInterfaceListener() {
        return jsonObject -> {
            try {
                switch (jsonObject.getString("action")) {
                    case "capture_screen":
                        SampleCamActivity.this.architectView.captureScreen(CaptureScreenCallback.CAPTURE_MODE_CAM_AND_WEBVIEW, screenCapture -> {
                            if (ContextCompat.checkSelfPermission(SampleCamActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                SampleCamActivity.this.screenCapture = screenCapture;
                                ActivityCompat.requestPermissions(SampleCamActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WIKITUDE_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                            } else {
                                SampleCamActivity.this.saveScreenCaptureToExternalStorage(screenCapture);
                            }
                        });
                        break;
                }
            } catch (JSONException e) {
                Log.e(TAG, "onJSONObjectReceived: ", e);
            }
        };
    }

	@Override
	public ArchitectView.ArchitectWorldLoadedListener getWorldLoadedListener() {
		return new ArchitectView.ArchitectWorldLoadedListener() {
			@Override
			public void worldWasLoaded(String url) {
				Log.i(TAG, "worldWasLoaded: url: " + url);
			}

			@Override
			public void worldLoadFailed(int errorCode, String description, String failingUrl) {
				Log.e(TAG, "worldLoadFailed: url: " + failingUrl + " " + description);
			}
		};
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WIKITUDE_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    this.saveScreenCaptureToExternalStorage(SampleCamActivity.this.screenCapture);
                } else {
                    Toast.makeText(this, "Please allow access to external storage, otherwise the screen capture can not be saved.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
	
	@Override
	public float getInitialCullingDistanceMeters() {
		// you need to adjust this in case your POIs are more than 50km away from user here while loading or in JS code (compare 'AR.context.scene.cullingDistance')
		return ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS;
	}

	@Override
	protected boolean hasInstant() {
        //return true;
        return (ArchitectView.getSupportedFeaturesForDevice(getApplicationContext()) & ArchitectStartupConfiguration.Features.InstantTracking) != 0;
    }

    @Override
    public CameraSettings.CameraResolution getCameraResolution() {
        return CameraSettings.CameraResolution.AUTO;
    }

	@Override
	protected CameraSettings.CameraPosition getCameraPosition() {
		return CameraSettings.CameraPosition.DEFAULT;
	}

    protected void saveScreenCaptureToExternalStorage(Bitmap screenCapture) {
        if ( screenCapture != null ) {
            // store screenCapture into external cache directory
            File Dir = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "FurnitureGo ScreenShots" + File.separator);
            Dir.mkdirs();
            final File screenCaptureFile = new File(Dir, "Shoot_" + System.currentTimeMillis() + ".jpg");

            // 1. Save bitmap to file & compress to jpeg. You may use PNG too
            try {
                final FileOutputStream out = new FileOutputStream(screenCaptureFile);
                screenCapture.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                Toast.makeText(SampleCamActivity.this, "ScreenShot Saved at " + screenCaptureFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

                // 2. create send intent
                final Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpg");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(screenCaptureFile));

                // 3. launch intent-chooser
                final String chooserTitle = "Share Snaphot";
                SampleCamActivity.this.startActivity(Intent.createChooser(share, chooserTitle));

            } catch (final Exception e) {
                // should not occur when all permissions are set
                SampleCamActivity.this.runOnUiThread(() -> {
                    // show toast message in case something went wrong
                    Toast.makeText(SampleCamActivity.this, "Unexpected error, " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }
    }
}
