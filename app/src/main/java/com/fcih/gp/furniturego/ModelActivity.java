package com.fcih.gp.furniturego;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class ModelActivity extends AppCompatActivity {

    private static final String TAG = "ModelActivity";
    private static final String OBJECT_KEY = "KEY";
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private LinearLayout mLinearLayout;
    private ProgressBar mProgressView;
    private AppBarLayout mAppBarView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressView = (ProgressBar) findViewById(R.id.progress);
        mLinearLayout = (LinearLayout) findViewById(R.id.container);
        mAppBarView = (AppBarLayout) findViewById(R.id.app_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showProgress(true);
        String Key = getIntent().getStringExtra(OBJECT_KEY);

        new FireBaseHelper.Objects().Findbykey(Key, Data -> {
            TextView mNameView = (TextView) findViewById(R.id.Name_TextView);
            TextView mCompanyView = (TextView) findViewById(R.id.Company_TextView);
            final TextView mSizeView = (TextView) findViewById(R.id.Size_TextView);
            ImageView mimageView = (ImageView) findViewById(R.id.model_imageView);
            CollapsingToolbarLayout mTitleBarView = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
            Picasso.with(getApplicationContext()).load(Data.image_path).resize(75, 75).into(mimageView);
            mCompanyView.setText(Data.companies.users.name);
            mNameView.setText(Data.name);
            mTitleBarView.setTitle(Data.name);
            StorageReference storageRef = storage.getReference();
            final StorageReference forestRef = storageRef.child(Data.model_path);
            forestRef.getMetadata().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    double kb = (double) task.getResult().getSizeBytes() / 1024;
                    double mb = kb / 1024;
                    mSizeView.setText(String.format(Locale.ENGLISH, "%.2f", mb));
                }else {
                    Toast.makeText(getApplicationContext(),"Error Retrieving the item",Toast.LENGTH_LONG).show();
                    fab.setEnabled(false);
                }
                showProgress(false);
            });
            fab.setOnClickListener(view -> {
                try {
                    File Dir = new File(Environment.getExternalStorageDirectory(), "/FurnitureGo/");
                    if (!Dir.exists())
                        Dir.mkdir();
                    File localFile = File.createTempFile(Data.Key, ".obj", Dir);
                    forestRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                        // Local temp file has been created
                        Toast.makeText(getApplicationContext(), "Download Complete", Toast.LENGTH_LONG).show();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });


        });
    }

    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLinearLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        mAppBarView.setVisibility(show ? View.GONE : View.VISIBLE);
        fab.setVisibility(show ? View.GONE : View.VISIBLE);
        mLinearLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLinearLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                fab.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
        mAppBarView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAppBarView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
