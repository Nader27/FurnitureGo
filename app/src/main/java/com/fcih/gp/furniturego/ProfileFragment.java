package com.fcih.gp.furniturego;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ProfileFragment";
    private static int RESULT_LOAD_IMAGE = 1;
    private static boolean WAITINGFORIMAGE = false;
    private TextView mNameview;
    private Uri imageUri;
    private Uri ImageURI;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private AlertDialog dialog;
    private AlertDialog Edialog;
    private FirebaseAuth mAuth;
    private RoundedImageView imageView;
    private BaseActivity activity;
    private Context context;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            FragmentTransaction ft = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            ft.remove(this);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mview = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        context = getContext();
        activity = (BaseActivity) getActivity();
        ImageURI = null;
        mNameview = (TextView) mview.findViewById(R.id.nameedit);
        mNameview.setText(mAuth.getCurrentUser().getDisplayName());
        imageView = (RoundedImageView) mview.findViewById(R.id.userimage);
        activity.findViewById(R.id.tabs).setVisibility(View.GONE);
        activity.getSupportActionBar().hide();

        TextView mEmailview = (TextView) mview.findViewById(R.id.emailedit);
        mEmailview.setText(mAuth.getCurrentUser().getEmail());
        mEmailview.setEnabled(false);
        TextView mPasswordview = (TextView) mview.findViewById(R.id.passwordedit);
        mPasswordview.setEnabled(false);
        Picasso.with(context)
                .load(mAuth.getCurrentUser().getPhotoUrl())
                .fit()
                .into(imageView);

        mview.findViewById(R.id.backimg).setOnClickListener(this);
        mview.findViewById(R.id.savetext).setOnClickListener(this);
        mview.findViewById(R.id.editpassword).setOnClickListener(this);
        mview.findViewById(R.id.editemail).setOnClickListener(this);
        mview.findViewById(R.id.Browsebutton).setOnClickListener(this);
        return mview;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK && WAITINGFORIMAGE) {
            imageUri = CropImage.getPickImageResultUri(context, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(context, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setFixAspectRatio(true)
                        .start(activity);
                //baby_image.setImageURI(imageUri);
                ImageURI = imageUri;
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && WAITINGFORIMAGE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imageView.setImageURI(resultUri);
                ImageURI = resultUri;
                WAITINGFORIMAGE = false;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(context, result.getError().getMessage(), Toast.LENGTH_LONG).show();
                WAITINGFORIMAGE = false;
            }
        } else if (requestCode == 0) {
            WAITINGFORIMAGE = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setFixAspectRatio(true)
                        .start(activity);
            } else {
                Toast.makeText(context, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setFixAspectRatio(true)
                        .start(activity);
            } else {
                Toast.makeText(context, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backimg:
                //region Back
                activity.onBackPressed();
                //endregion
                break;
            case R.id.savetext:
                //region Save
                if (ImageURI != null) {
                    Uri file = Uri.fromFile(new File(ImageURI.toString()));
                    StorageReference riversRef = storageRef.child("ProfileImage/" + file.getLastPathSegment());
                    UploadTask uploadTask = riversRef.putFile(file);
                    uploadTask.addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show());
                    uploadTask.addOnCompleteListener(task -> {
                        try {

                            if (task.isSuccessful()) {
                                @SuppressWarnings("VisibleForTests") final Uri downloadUrl = task.getResult().getDownloadUrl();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(mNameview.getText().toString())
                                        .setPhotoUri(downloadUrl)
                                        .build();
                                FirebaseUser user = mAuth.getCurrentUser();
                                user.updateProfile(profileUpdates);
                                FireBaseHelper.Users USER = new FireBaseHelper.Users();
                                USER.Findbykey(user.getUid(), Data -> {
                                    Data.name = mNameview.getText().toString();
                                    Data.image_uri = downloadUrl.toString();
                                    Data.Update(Data.Key);
                                    activity.onNavigationItemSelected(activity.navigationView.getMenu().getItem(0));
                                });
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                /*} else {
                        Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }*/
                    });
                } else {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(mNameview.getText().toString())
                            .build();
                    FirebaseUser user = mAuth.getCurrentUser();
                    user.updateProfile(profileUpdates);
                    FireBaseHelper.Users USER = new FireBaseHelper.Users();
                    USER.Findbykey(user.getUid(), Data -> {
                        Data.name = mNameview.getText().toString();
                        Data.Update(Data.Key);
                        activity.onNavigationItemSelected(activity.navigationView.getMenu().getItem(0));
                    });
                }

                //endregion
                break;
            case R.id.editpassword:
                //region password form
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
                View mView = activity.getLayoutInflater().inflate(R.layout.editpassword, null);
                Button mButton = (Button) mView.findViewById(R.id.doeditpassword);
                mButton.setOnClickListener(this);
                mBuilder.setView(mView);
                dialog = mBuilder.create();
                dialog.show();
                //endregion
                break;
            case R.id.editemail:
                //region email form
                AlertDialog.Builder mEBuilder = new AlertDialog.Builder(activity);
                View mEView = activity.getLayoutInflater().inflate(R.layout.editemail, null);
                Button mEButton = (Button) mEView.findViewById(R.id.doeditemail);
                mEButton.setOnClickListener(this);
                mEBuilder.setView(mEView);
                Edialog = mEBuilder.create();
                Edialog.show();
                //endregion
                break;
            case R.id.doeditpassword:
                //region change password
                EditText mpassword = (EditText) dialog.findViewById(R.id.password1);
                EditText mrepassword = (EditText) dialog.findViewById(R.id.repassword1);
                if (!mpassword.getText().toString().isEmpty() && !mrepassword.getText().toString().isEmpty()) {
                    if (mpassword.getText().toString().equals(mrepassword.getText().toString())) {
                        mAuth.getCurrentUser().updatePassword(mpassword.getText().toString()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context,
                                        "Password Changed Successfully",
                                        Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            } else
                                Toast.makeText(context,
                                        task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                        });
                    } else
                        Toast.makeText(context,
                                "Password Not Matched",
                                Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(context,
                            "Password Field is Empty",
                            Toast.LENGTH_LONG).show();
                //endregion
                break;
            case R.id.doeditemail:
                //region Change Email
                final EditText memail = (EditText) Edialog.findViewById(R.id.email1);
                EditText mreemail = (EditText) Edialog.findViewById(R.id.reemail1);
                if (!memail.getText().toString().isEmpty() && !mreemail.getText().toString().isEmpty()) {
                    if (memail.getText().toString().equals(mreemail.getText().toString())) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        user.updateEmail(memail.getText().toString()).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                FireBaseHelper.Users USER = new FireBaseHelper.Users();
                                USER.Findbykey(user.getUid(), Data -> {
                                    Data.email = memail.getText().toString();
                                    Data.Update(Data.Key);
                                    Toast.makeText(context,
                                            "Email Changed Successfully",
                                            Toast.LENGTH_LONG).show();
                                    Edialog.dismiss();
                                });
                            }else
                                Toast.makeText(context,
                                        task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                        });


                    } else
                        Toast.makeText(context,
                                "Email Not Matched",
                                Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(context,
                            "Email Field is Empty",
                            Toast.LENGTH_LONG).show();
                //endregion
                break;
            case R.id.Browsebutton:
                //region Browse Button
                if (CropImage.isExplicitCameraPermissionRequired(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    WAITINGFORIMAGE = true;
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                } else {
                    WAITINGFORIMAGE = true;
                    CropImage.startPickImageActivity(activity);
                }
                //endregion
                break;

        }
    }
}