package com.fcih.gp.furniturego;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class ModelFragment extends Fragment {

    private static final String TAG = "ModelActivity";
    private static final String OBJECT_KEY = "KEY";
    private String ObjectKey;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private LinearLayout mLinearLayout;
    private ProgressBar mProgressView;
    private AppBarLayout mAppBarView;
    private FloatingActionButton fab;

    public ModelFragment() {
        // Required empty public constructor
    }

    public static ModelFragment newInstance(String Model) {
        ModelFragment fragment = new ModelFragment();
        Bundle args = new Bundle();
        args.putString(OBJECT_KEY, Model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ObjectKey = getArguments().getString(OBJECT_KEY);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mProgressView = (ProgressBar) view.findViewById(R.id.progress);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.container);
        mAppBarView = (AppBarLayout) view.findViewById(R.id.app_bar);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        showProgress(true);
        new FireBaseHelper.Objects().Findbykey(ObjectKey, Data -> {
            TextView mNameView = (TextView) getActivity().findViewById(R.id.Name_TextView);
            TextView mCompanyView = (TextView) getActivity().findViewById(R.id.Company_TextView);
            final TextView mSizeView = (TextView) getActivity().findViewById(R.id.Size_TextView);
            ImageView mimageView = (ImageView) getActivity().findViewById(R.id.model_imageView);
            CollapsingToolbarLayout mTitleBarView = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
            Picasso.with(getActivity().getApplicationContext()).load(Data.image_path).resize(75, 75).into(mimageView);
            mCompanyView.setText(Data.companies.name);
            mNameView.setText(Data.name);
            mTitleBarView.setTitle(Data.name);
            //StorageReference storageRef = storage.getReference();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference forestRef = storage.getReferenceFromUrl(Data.model_path);
            forestRef.getMetadata().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    double kb = (double) task.getResult().getSizeBytes() / 1024;
                    double mb = kb / 1024;
                    mSizeView.setText(String.format(Locale.ENGLISH, "%.2f", mb));
                } else {
                    Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                    fab.setEnabled(false);
                }
                showProgress(false);
            });
            fab.setOnClickListener(v -> {
                try {
                    File Dir = new File(Environment.getExternalStorageDirectory(), "/FurnitureGo/");
                    if (!Dir.exists())
                        Dir.mkdir();
                    File localFile = File.createTempFile(Data.Key, ".obj", Dir);
                    forestRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                        // Local temp file has been created
                        Toast.makeText(getContext(), "Download Complete", Toast.LENGTH_LONG).show();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });


        });
        return view;
    }

}
