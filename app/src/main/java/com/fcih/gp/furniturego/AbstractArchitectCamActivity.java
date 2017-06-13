package com.fcih.gp.furniturego;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;
import com.wikitude.architect.services.camera.CameraLifecycleListener;
import com.wikitude.common.camera.CameraSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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
	protected ArchitectView					architectView;

	/**
	 * sensor accuracy listener in case you want to display calibration hints
	 */
	protected SensorAccuracyChangeListener	sensorAccuracyListener;

	/**
	 * last known location of the user, used internally for content-loading after user location was fetched
	 */
	protected Location 						lastKnownLocaton;

	/**
	 * sample location strategy, you may implement a more sophisticated approach too
	 */
	protected ILocationProvider				locationProvider;

	/**
	 * location listener receives location updates and must forward them to the architectView
	 */
	protected LocationListener 				locationListener;

	/**
	 * JS interface listener handling e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' calls in JavaScript
	 */
	protected ArchitectJavaScriptInterfaceListener mArchitectJavaScriptInterfaceListener;

	/**
	 * worldLoadedListener receives calls when the AR world is finished loading or when it failed to laod.
	 */
	protected ArchitectView.ArchitectWorldLoadedListener worldLoadedListener;

	protected JSONArray poiData;

	protected boolean isLoading = false;

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		/* pressing volume up/down should cause music volume changes */
		this.setVolumeControlStream( AudioManager.STREAM_MUSIC );

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

		if (hasGeo()) {
			// listener passed over to locationProvider, any location update is handled here
			this.locationListener = new LocationListener() {

				@Override
				public void onStatusChanged( String provider, int status, Bundle extras ) {
				}

				@Override
				public void onProviderEnabled( String provider ) {
				}

				@Override
				public void onProviderDisabled( String provider ) {
				}

				@Override
				public void onLocationChanged( final Location location ) {
					// forward location updates fired by LocationProvider to architectView, you can set lat/lon from any location-strategy
					if (location!=null) {
					// sore last location as member, in case it is needed somewhere (in e.g. your adjusted project)
						AbstractArchitectCamActivity.this.lastKnownLocaton = location;
						if ( AbstractArchitectCamActivity.this.architectView != null ) {
							// check if location has altitude at certain accuracy level & call right architect method (the one with altitude information)
							if ( location.hasAltitude() && location.hasAccuracy() && location.getAccuracy()<7) {
								AbstractArchitectCamActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy() );
							} else {
								AbstractArchitectCamActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
							}
						}
					}
				}
			};

			// locationProvider used to fetch user position
			this.locationProvider = getLocationProvider( this.locationListener );
		} else {
			this.locationProvider = null;
			this.locationListener = null;
		}
	}

	protected abstract CameraSettings.CameraPosition getCameraPosition();

	private int getFeatures() {
		int features = (hasGeo() ? ArchitectStartupConfiguration.Features.Geo : 0) |
				(hasIR() ? ArchitectStartupConfiguration.Features.ImageTracking : 0) |
				(hasInstant() ? ArchitectStartupConfiguration.Features.InstantTracking : 0) ;
		return features;
	}

	protected abstract boolean hasGeo();
	protected abstract boolean hasIR();
	protected abstract boolean hasInstant();

	protected CameraLifecycleListener getCameraLifecycleListener() {
		return null;
	}

	//TODO: Check CameraResolution
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

		// tell locationProvider to resume, usually location is then (again) fetched, so the GPS indicator appears in status bar
		if ( this.locationProvider != null ) {
			this.locationProvider.onResume();
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

		// tell locationProvider to pause, usually location is then no longer fetched, so the GPS indicator disappears in status bar
		if ( this.locationProvider != null ) {
			this.locationProvider.onPause();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
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
	 * JS interface listener fired once e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' is called in JS
	 */
	@Override
	public abstract ArchitectJavaScriptInterfaceListener getArchitectJavaScriptInterfaceListener();

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
	 *
	 * @return Implementation of a Location
	 */
	@Override
	public abstract ILocationProvider getLocationProvider(final LocationListener locationListener);

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
	 * helper to check if video-drawables are supported by this device. recommended to check before launching ARchitect Worlds with videodrawables
	 * @return true if AR.VideoDrawables are supported, false if fallback rendering would apply (= show video fullscreen)
	 */
	public static final boolean isVideoDrawablesSupported() {
		String extensions = GLES20.glGetString( GLES20.GL_EXTENSIONS );
		return extensions != null && extensions.contains( "GL_OES_EGL_image_external" );
	}

	protected void injectData() {
		if (!isLoading) {
			final Thread t = new Thread(new Runnable() {

				@Override
				public void run() {

					isLoading = true;

					final int WAIT_FOR_LOCATION_STEP_MS = 2000;

					while (lastKnownLocaton==null && !isFinishing()) {

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(AbstractArchitectCamActivity.this, R.string.location_fetching, Toast.LENGTH_SHORT).show();
							}
						});

						try {
							Thread.sleep(WAIT_FOR_LOCATION_STEP_MS);
						} catch (InterruptedException e) {
							break;
						}
					}

					if (lastKnownLocaton!=null && !isFinishing()) {
						// TODO: you may replace this dummy implementation and instead load POI information e.g. from your database
						poiData = getPoiInformation(lastKnownLocaton, 20);
						callJavaScript("World.loadPoisFromJsonData", new String[] { poiData.toString() });
					}

					isLoading = false;
				}
			});
			t.start();
		}
	}

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

	/**
	 * loads poiInformation and returns them as JSONArray. Ensure attributeNames of JSON POIs are well known in JavaScript, so you can parse them easily
	 * @param userLocation the location of the user
	 * @param numberOfPlaces number of places to load (at max)
	 * @return POI information in JSONArray
	 */
	public static JSONArray getPoiInformation(final Location userLocation, final int numberOfPlaces) {

		if (userLocation==null) {
			return null;
		}

		final JSONArray pois = new JSONArray();

		// ensure these attributes are also used in JavaScript when extracting POI data
		final String ATTR_ID = "id";
		final String ATTR_NAME = "name";
		final String ATTR_DESCRIPTION = "description";
		final String ATTR_LATITUDE = "latitude";
		final String ATTR_LONGITUDE = "longitude";
		final String ATTR_ALTITUDE = "altitude";

		for (int i=1;i <= numberOfPlaces; i++) {
			final HashMap<String, String> poiInformation = new HashMap<String, String>();
			poiInformation.put(ATTR_ID, String.valueOf(i));
			poiInformation.put(ATTR_NAME, "POI#" + i);
			poiInformation.put(ATTR_DESCRIPTION, "This is the description of POI#" + i);
			double[] poiLocationLatLon = getRandomLatLonNearby(userLocation.getLatitude(), userLocation.getLongitude());
			poiInformation.put(ATTR_LATITUDE, String.valueOf(poiLocationLatLon[0]));
			poiInformation.put(ATTR_LONGITUDE, String.valueOf(poiLocationLatLon[1]));
			final float UNKNOWN_ALTITUDE = -32768f;  // equals "AR.CONST.UNKNOWN_ALTITUDE" in JavaScript (compare AR.GeoLocation specification)
			// Use "AR.CONST.UNKNOWN_ALTITUDE" to tell ARchitect that altitude of places should be on user level. Be aware to handle altitude properly in locationManager in case you use valid POI altitude value (e.g. pass altitude only if GPS accuracy is <7m).
			poiInformation.put(ATTR_ALTITUDE, String.valueOf(UNKNOWN_ALTITUDE));
			pois.put(new JSONObject(poiInformation));
		}

		return pois;
	}

	/**
	 * helper for creation of dummy places.
	 * @param lat center latitude
	 * @param lon center longitude
	 * @return lat/lon values in given position's vicinity
	 */
	private static double[] getRandomLatLonNearby(final double lat, final double lon) {
		return new double[] { lat + Math.random()/5-0.1 , lon + Math.random()/5-0.1};
	}
}
