package com.fcih.gp.furniturego;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.permission.PermissionManager;
import com.wikitude.tools.device.features.MissingDeviceFeatures;

import java.util.Arrays;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "BaseActivity";
    protected NavigationView navigationView;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MissingDeviceFeatures missingDeviceFeatures = ArchitectView.isDeviceSupported(this,
                ArchitectStartupConfiguration.Features.ImageTracking | ArchitectStartupConfiguration.Features.InstantTracking);

        if (false) {
            //if (missingDeviceFeatures.areFeaturesMissing()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Sorry");
            alertDialog.setMessage("Sorry Your Device Is Not Supported." + missingDeviceFeatures.getMissingFeatureMessage());
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    (dialog, which) -> {
                        finish();
                        dialog.dismiss();
                    });
            alertDialog.show();
            finish();
        } else {
            setContentView(R.layout.activity_base);
            if (mAuth.getCurrentUser() == null) {
                Log.e(TAG, "User is signed out");
                Intent loginIntent = new Intent(BaseActivity.this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
                finish();
            } else {
                mAuthListener = firebaseAuth -> {
                    if (firebaseAuth.getCurrentUser() == null) {
                        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                        finish();

                    }

                };
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                toggle = new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                drawer.setDrawerListener(toggle);
                toggle.syncState();

                navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setNavigationItemSelectedListener(this);
                onNavigationItemSelected(navigationView.getMenu().getItem(0));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.item_menu, menu);

        SearchSetup(menu);

        UserSetup();

        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.

        if (mAuth.getCurrentUser() != null) {
            toggle.syncState();
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        // Pass any configuration change to the drawer toggles

        toggle.onConfigurationChanged(newConfig);

    }

    public void UserSetup() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ImageView userimage = (ImageView) findViewById(R.id.nav_user_image);
        TextView username = (TextView) findViewById(R.id.nav_user_name);
        TextView useremail = (TextView) findViewById(R.id.nav_user_email);

        if (user != null) {

            if (user.getPhotoUrl() != null) {
                Picasso.with(getApplicationContext())
                        .load(user.getPhotoUrl())
                        .resize(userimage.getWidth(), userimage.getWidth())
                        .into(userimage);
            }
            username.setText(user.getDisplayName());
            useremail.setText(user.getEmail());
        }
    }

    public void SearchSetup(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        final ImageView mCloseButton = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, BaseActivity.class)
        ));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchFragment fragment = SearchFragment.newInstance(query);
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, SearchFragment.TAG).commit();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            HomeFragment fragment = HomeFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, HomeFragment.TAG).addToBackStack(null).commit();
        } else if (id == R.id.nav_camera) {
            PermissionManager mPermissionManager = ArchitectView.getPermissionManager();
            String[] permissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE};

            mPermissionManager.checkPermissions(this, permissions, PermissionManager.WIKITUDE_PERMISSION_REQUEST, new PermissionManager.PermissionManagerCallback() {
                @Override
                public void permissionsGranted(int requestCode) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        startActivity(new Intent(BaseActivity.this, SampleCam2Activity.class));
                    } else {
                        startActivity(new Intent(BaseActivity.this, SampleCamActivity.class));
                    }
                }

                @Override
                public void permissionsDenied(String[] deniedPermissions) {

                    Toast.makeText(getApplicationContext(), "The Wikitude SDK needs the following permissions to enable an AR experience: " + Arrays.toString(deniedPermissions), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void showPermissionRationale(final int requestCode, final String[] permissions) {
                    android.app.AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(getApplicationContext());
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Wikitude Permissions");
                    alertBuilder.setMessage("The Wikitude SDK needs the following permissions to enable an AR experience: " + Arrays.toString(permissions));
                    alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> mPermissionManager.positiveRationaleResult(requestCode, permissions));

                    android.app.AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
            });
        } else if (id == R.id.nav_account) {
            profileFragment = ProfileFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, profileFragment, ProfileFragment.TAG).addToBackStack(null).commit();
        } else if (id == R.id.nav_models) {
            MyModelsFragment fragment = MyModelsFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, MyModelsFragment.TAG).addToBackStack(null).commit();
        } else if (id == R.id.nav_whishlist) {
            FavFragment fragment = FavFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, FavFragment.TAG).addToBackStack(null).commit();
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            LoginManager.getInstance().logOut();
        }
        item.setChecked(true);
        setTitle(item.getTitle());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (profileFragment != null) {
            profileFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (profileFragment != null) {
            profileFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
