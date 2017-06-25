package com.fcih.gp.furniturego;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
import com.wikitude.architect.services.camera.CameraLifecycleListener;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.permission.PermissionManager;
import com.wikitude.tools.device.features.MissingDeviceFeatures;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Abstract activity which handles live-cycle events.
 * Feel free to extend from this activity when setting up your own AR-Activity
 *
 */
public abstract class AbstractArchitectCamActivity extends Activity implements ArchitectViewHolderInterface{

	/**
	 * holds the Wikitude SDK AR-View, this is where camera, markers, compass, 3D models etc. are rendered
	 */
	protected ArchitectView architectView;

	/**
	 * sensor accuracy listener in case you want to display calibration hints
	 */
	protected SensorAccuracyChangeListener	sensorAccuracyListener;

	/**
	 * JS interface listener handling e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' calls in JavaScript
	 */
	protected ArchitectJavaScriptInterfaceListener mArchitectJavaScriptInterfaceListener;

	/**
	 * worldLoadedListener receives calls when the AR world is finished loading or when it failed to laod.
	 */
	protected ArchitectView.ArchitectWorldLoadedListener worldLoadedListener;
	private PermissionManager mPermissionManager;

	private static String[] getDownloaded3D(String pathname) {
		File file;
		final JSONArray models = new JSONArray();
		final String ATTR_3D = "model";
		final String ATTR_Image = "image";
		final String Model_ext = ".wt3";
		final String Image_ext = ".png";
		file = new File(pathname);
		if (file.exists()) {
			String list[] = file.list();
			for (String name : list) {
				if (new File(pathname + File.separator + name).isDirectory()) {
					File model = new File(pathname + File.separator + name + File.separator+ name+ Model_ext);
					File image = new File(pathname + File.separator + name + File.separator+ name+ Image_ext);
					if (model.exists() && image.exists()) {
                        final HashMap<String, String> modelInformation = new HashMap<>();
                        modelInformation.put(ATTR_3D, model.getPath());
						modelInformation.put(ATTR_Image, image.getPath());
						models.put(new JSONObject(modelInformation));
					}
				}

			}
		}
		return new String[]{models.toString()};
	}

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		MissingDeviceFeatures missingDeviceFeatures = ArchitectView.isDeviceSupported(this,
				ArchitectStartupConfiguration.Features.ImageTracking | ArchitectStartupConfiguration.Features.Geo | ArchitectStartupConfiguration.Features.InstantTracking);

		if (missingDeviceFeatures.areFeaturesMissing()) {
			Toast toast =  Toast.makeText(this, missingDeviceFeatures.getMissingFeatureMessage() +
					"Because of this Cannot Run App on Your Device.", Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		mPermissionManager = ArchitectView.getPermissionManager();
		String[] permissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE} ;

		mPermissionManager.checkPermissions(this, permissions, PermissionManager.WIKITUDE_PERMISSION_REQUEST, new PermissionManager.PermissionManagerCallback() {
			@Override
			public void permissionsGranted(int requestCode) {
			}

			@Override
			public void permissionsDenied(String[] deniedPermissions) {

				Toast.makeText(getApplicationContext(), "The Wikitude SDK needs the following permissions to enable an AR experience: " + Arrays.toString(deniedPermissions), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void showPermissionRationale(final int requestCode, final String[] permissions) {
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getApplicationContext());
				alertBuilder.setCancelable(true);
				alertBuilder.setTitle("Wikitude Permissions");
				alertBuilder.setMessage("The Wikitude SDK needs the following permissions to enable an AR experience: " + Arrays.toString(permissions));
				alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> mPermissionManager.positiveRationaleResult(requestCode, permissions));

				AlertDialog alert = alertBuilder.create();
				alert.show();
			}
		});

		/* pressing volume up/down should cause music volume changes */
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		/* full Screen Window */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		/* set samples content view */
		this.setContentView( this.getContentViewId() );

		this.setTitle( this.getActivityTitle() );

		/*
		 *	this enables remote debugging of a WebView on Android 4.4+ when debugging = true in AndroidManifest.xml
		 *	If you get a compile time error here, ensure to have SDK 19+ used in your ADT/Eclipse.
		 *	You may even delete this block in case you don't need remote debugging or don't have an Android 4.4+ device in place.
		 *	Details: https://developers.google.com/chrome-developer-tools/docs/remote-debugging
		 */
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
		    if ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) ) {
		        WebView.setWebContentsDebuggingEnabled(true);
		    }
		}

		/* set AR-view for life-cycle notifications etc. */
		this.architectView = (ArchitectView)this.findViewById( this.getArchitectViewId()  );

		/* pass SDK key if you have one, this one is only valid for this package identifier and must not be used somewhere else */
		final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
		config.setLicenseKey(this.getWikitudeSDKLicenseKey());
		config.setFeatures(this.getFeatures());
		config.setCameraPosition(this.getCameraPosition());
		config.setCameraResolution(this.getCameraResolution());
		config.setCamera2Enabled(this.getCamera2Enabled());

		this.architectView.setCameraLifecycleListener(getCameraLifecycleListener());
		try {
			/* first mandatory life-cycle notification */
			this.architectView.onCreate( config );
		} catch (RuntimeException rex) {
			this.architectView = null;
			Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
			Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
		}

		// set world loaded listener if implemented
		this.worldLoadedListener = this.getWorldLoadedListener();

		// register valid world loaded listener in architectView, ensure this is set before content is loaded to not miss any event
		if (this.worldLoadedListener != null && this.architectView != null) {
			this.architectView.registerWorldLoadedListener(worldLoadedListener);
		}

		// set accuracy listener if implemented, you may e.g. show calibration prompt for compass using this listener
		this.sensorAccuracyListener = this.getSensorAccuracyListener();

		// set JS interface listener, any calls made in JS like 'AR.platform.sendJSONObject({foo:"bar", bar:123})' is forwarded to this listener, use this to interact between JS and native Android activity/fragment
		this.mArchitectJavaScriptInterfaceListener = this.getArchitectJavaScriptInterfaceListener();

		// set JS interface listener in architectView, ensure this is set before content is loaded to not miss any event
		if (this.mArchitectJavaScriptInterfaceListener != null && this.architectView != null) {
			this.architectView.addArchitectJavaScriptInterfaceListener(mArchitectJavaScriptInterfaceListener);
		}
	}

	protected abstract CameraSettings.CameraPosition getCameraPosition();

	private int getFeatures() {
		return (hasInstant() ? ArchitectStartupConfiguration.Features.InstantTracking : 0);
	}

	protected abstract boolean hasInstant();

	protected CameraLifecycleListener getCameraLifecycleListener() {
		return null;
	}

	protected CameraSettings.CameraResolution getCameraResolution(){
		return CameraSettings.CameraResolution.SD_640x480;
	}
	protected boolean getCamera2Enabled() {
		return false;
	}

	@Override
	protected void onPostCreate( final Bundle savedInstanceState ) {
		super.onPostCreate( savedInstanceState );

		if ( this.architectView != null ) {

			// call mandatory live-cycle method of architectView
			this.architectView.onPostCreate();

			try {
				// load content via url in architectView, ensure '<script src="architect://architect.js"></script>' is part of this HTML file, have a look at wikitude.com's developer section for API references
				this.architectView.load( this.getARchitectWorldPath() );

				if (this.getInitialCullingDistanceMeters() != ArchitectViewHolderInterface.CULLING_DISTANCE_DEFAULT_METERS) {
					// set the culling distance - meaning: the maximum distance to render geo-content
					this.architectView.setCullingDistance( this.getInitialCullingDistanceMeters() );
				}
				String path = Environment.getExternalStorageDirectory().toString() + File.separator + "FurnitureGo";
				callJavaScript("World.loadPathFromJsonData", getDownloaded3D(path));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// call mandatory live-cycle method of architectView
		if ( this.architectView != null ) {
			this.architectView.onResume();

			// register accuracy listener in architectView, if set
			if (this.sensorAccuracyListener!=null) {
				this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// call mandatory live-cycle method of architectView
		if ( this.architectView != null ) {
			this.architectView.onPause();

			// unregister accuracy listener in architectView, if set
			if ( this.sensorAccuracyListener != null ) {
				this.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
			}
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		// call mandatory live-cycle method of architectView
		if ( this.architectView != null ) {
			this.architectView.clearCache();
			this.architectView.onDestroy();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if ( this.architectView != null ) {
			this.architectView.onLowMemory();
		}
	}

	/**
	 * title shown in activity
	 * @return
	 */
	public abstract String getActivityTitle();

	/**
	 * path to the architect-file (AR-Experience HTML) to launch
	 * @return
	 */
	@Override
	public abstract String getARchitectWorldPath();

	/**
	 * @return layout id of your layout.xml that holds an ARchitect View, e.g. R.layout.camview
	 */
	@Override
	public abstract int getContentViewId();

	/**
	 * @return Wikitude SDK license key, checkout www.wikitude.com for details
	 */
	@Override
	public abstract String getWikitudeSDKLicenseKey();

	/**
	 * @return layout-id of architectView, e.g. R.id.architectView
	 */
	@Override
	public abstract int getArchitectViewId();

	/**
	 * @return Implementation of Sensor-Accuracy-Listener. That way you can e.g. show prompt to calibrate compass
	 */
	@Override
	public abstract ArchitectView.SensorAccuracyChangeListener getSensorAccuracyListener();

	/**
	 * @return Implementation of ArchitectWorldLoadedListener. That way you know when a AR world is finished loading or when it failed to load.
	 */
	@Override
	public abstract ArchitectView.ArchitectWorldLoadedListener getWorldLoadedListener();

	/**
	 * call JacaScript in architectView
	 * @param methodName
	 * @param arguments
	 */
	private void callJavaScript(final String methodName, final String[] arguments) {
		final StringBuilder argumentsString = new StringBuilder("");
		for (int i= 0; i<arguments.length; i++) {
			argumentsString.append(arguments[i]);
			if (i<arguments.length-1) {
				argumentsString.append(", ");
			}
		}

		if (this.architectView!=null) {
			final String js = ( methodName + "( " + argumentsString.toString() + " );" );
			this.architectView.callJavascript(js);
		}
	}
}
