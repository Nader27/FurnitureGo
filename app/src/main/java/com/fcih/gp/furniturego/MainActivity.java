package com.fcih.gp.furniturego;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wikitude.architect.ArchitectView;
import com.wikitude.common.permission.PermissionManager;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends ListActivity {

	public static final String EXTRAS_KEY_ACTIVITY_TITLE_STRING = "activityTitle";
	public static final String EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL = "activityArchitectWorldUrl";

	public static final String EXTRAS_KEY_ACTIVITY_IR = "activityIr";
	public static final String EXTRAS_KEY_ACTIVITY_GEO = "activityGeo";
	public static final String EXTRAS_KEY_ACTIVITY_INSTANT = "activityInstant";

	public static final String EXTRAS_KEY_ACTIVITIES_ARCHITECT_WORLD_URLS_ARRAY = "activitiesArchitectWorldUrls";
	public static final String EXTRAS_KEY_ACTIVITIES_TILES_ARRAY = "activitiesTitles";
	public static final String EXTRAS_KEY_ACTIVITIES_CLASSNAMES_ARRAY = "activitiesClassnames";

	public static final String EXTRAS_KEY_ACTIVITIES_IR_ARRAY = "activitiesIr";
	public static final String EXTRAS_KEY_ACTIVITIES_GEO_ARRAY = "activitiesGeo";
	public static final String EXTRAS_KEY_ACTIVITIES_INSTANT_ARRAY = "activitiesInstant";

	private PermissionManager mPermissionManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(this.getContentViewId());

		mPermissionManager = ArchitectView.getPermissionManager();
		String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION} ;

		mPermissionManager.checkPermissions(this, permissions, PermissionManager.WIKITUDE_PERMISSION_REQUEST, new PermissionManager.PermissionManagerCallback() {
			@Override
			public void permissionsGranted(int requestCode) {
				loadExample();
			}

			@Override
			public void permissionsDenied(String[] deniedPermissions) {

				Toast.makeText(MainActivity.this, "The Wikitude SDK needs the following permissions to enable an AR experience: " + Arrays.toString(deniedPermissions), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void showPermissionRationale(final int requestCode, final String[] permissions) {
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
				alertBuilder.setCancelable(true);
				alertBuilder.setTitle("Wikitude Permissions");
				alertBuilder.setMessage("The Wikitude SDK needs the following permissions to enable an AR experience: " + Arrays.toString(permissions));
				alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mPermissionManager.positiveRationaleResult(requestCode, permissions);
					}
				});

				AlertDialog alert = alertBuilder.create();
				alert.show();
			}
		});
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		mPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

}
