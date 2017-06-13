package com.fcih.gp.furniturego;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, GoogleApiClient.OnConnectionFailedListener, OnClickListener  {

    private static final String TAG = "Login Activity";

    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int RC_SIGN_IN = 9001;
    private static final String DESIGNER_TYPE = "Designer";
    // UI references.
    private AutoCompleteTextView mLoginEmailView;
    private EditText mLoginPasswordView;
    private AutoCompleteTextView mRegisterEmailView;
    private EditText mRegisterPasswordView;
    private EditText mRegisterNameView;
    private EditText mConfirmPasswordView;
    private View mProgressView;
    private View mMainFormView;
    //Facebook
    private CallbackManager callbackManager;
    //Google
    private GoogleApiClient mGoogleApiClient;
    //FireBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
            // ...
        };

        TabHost host = (TabHost) findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Sign In");
        spec.setContent(R.id.Signin);
        spec.setIndicator("Sign In");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Sign Up");
        spec.setContent(R.id.SignUp);
        spec.setIndicator("Sign Up");
        host.addTab(spec);

        // Views
        mLoginEmailView = (AutoCompleteTextView) findViewById(R.id.login_email);
        mLoginPasswordView = (EditText) findViewById(R.id.login_password);
        mRegisterEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        mRegisterPasswordView = (EditText) findViewById(R.id.register_password);
        mConfirmPasswordView = (EditText) findViewById(R.id.register_confirm_password);
        mRegisterNameView = (EditText) findViewById(R.id.register_Name);
        mProgressView = findViewById(R.id.progress);
        mMainFormView = findViewById(R.id.main_form);

        //Buttons
        findViewById(R.id.email_login_Button).setOnClickListener(this);
        findViewById(R.id.email_register_Button).setOnClickListener(this);
        //findViewById(R.id.Facebook_button).setOnClickListener(this);
        //findViewById(R.id.Google_button).setOnClickListener(this);
        findViewById(R.id.password_forget).setOnClickListener(this);

        populateAutoComplete();

        FacebookLogin();

        GoogleLogin();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.email_login_Button:
                Login();
                break;
            case R.id.email_register_Button:
                Register();
                break;
            case R.id.password_forget:
                ForgetPassword();
                break;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
        //Google
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.d(TAG, "Error Google Signin ");
                Toast.makeText(LoginActivity.this, result.getStatus().getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showProgress(false);
        Toast.makeText(LoginActivity.this, "Connection Failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void GoogleLogin() {
        findViewById(R.id.Google_button).setOnClickListener(v -> {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
            showProgress(true);
        });
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void FacebookLogin() {
        callbackManager = CallbackManager.Factory.create();
        LoginButton fbLoginButton = (LoginButton) findViewById(R.id.Facebook_button);
        fbLoginButton.setReadPermissions("email", "public_profile");
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                showProgress(true);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // App code
                Toast.makeText(LoginActivity.this, "Login Canceled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(final FacebookException exception) {
                Log.d(TAG, "facebook:onError", exception);
                // App code
                Toast.makeText(
                        LoginActivity.this,
                        exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!task.isSuccessful()) {
                showProgress(false);
                Log.w(TAG, "signInWithCredential", task.getException());
                Toast.makeText(LoginActivity.this, "Authentication failed.\n" + task.getException(),
                        Toast.LENGTH_SHORT).show();
            } else {
                final FirebaseUser user = task.getResult().getUser();
                FireBaseHelper.Users FUSER = new FireBaseHelper.Users();
                FUSER.name = user.getDisplayName();
                FUSER.email = user.getEmail();
                FUSER.image_uri = user.getPhotoUrl().toString();
                FUSER.type_id = DESIGNER_TYPE;
                FUSER.Add(user.getUid());
                showProgress(false);
                startActivity(new Intent(LoginActivity.this,BaseActivity.class));
                finish();
            }
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        showProgress(false);
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.\n" + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        final FirebaseUser user = task.getResult().getUser();
                        final FireBaseHelper.Users FUSER = new FireBaseHelper.Users();
                        FUSER.name = user.getDisplayName();
                        FUSER.email = user.getEmail();
                        FUSER.image_uri = user.getPhotoUrl().toString();
                        FUSER.type_id = DESIGNER_TYPE;
                        FUSER.Add(user.getUid());
                        showProgress(false);
                        startActivity(new Intent(LoginActivity.this,BaseActivity.class));
                        finish();
                    }
                });
    }

    /**
     * Email Auto Complete
     */
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Email Auto Complete
     * Ask Permission To Access Contacts
     */
    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mLoginEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, v -> requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS));
            Snackbar.make(mRegisterEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, v -> requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS));
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void Login() {

        // Reset errors.
        mLoginEmailView.setError(null);
        mLoginPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mLoginEmailView.getText().toString();
        String password = mLoginPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mLoginEmailView.setError(getString(R.string.error_field_required));
            focusView = mLoginEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mLoginEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mLoginEmailView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mLoginPasswordView.setError(getString(R.string.error_field_required));
            focusView = mLoginPasswordView;
            cancel = true;
        }
        if (!isPasswordValid(password)) {
            mLoginPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mLoginPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            showProgress(false);
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            showProgress(false);
                            startActivity(new Intent(LoginActivity.this,BaseActivity.class));
                            finish();
                        }
                    });
        }
    }

    /**
     * Attempts to sign up the account specified by the Register form.
     */
    private void Register() {

        // Reset errors.
        mRegisterEmailView.setError(null);
        mRegisterNameView.setError(null);
        mRegisterPasswordView.setError(null);
        mConfirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mRegisterEmailView.getText().toString();
        String password = mRegisterPasswordView.getText().toString();
        final String name = mRegisterNameView.getText().toString();
        String confirmpassword = mConfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid User Name.
        if (TextUtils.isEmpty(name)) {
            mRegisterNameView.setError(getString(R.string.error_field_required));
            focusView = mRegisterNameView;
            cancel = true;
        } else if (!isNameValid(name)) {
            mRegisterNameView.setError("Not Valid Name");
            focusView = mRegisterNameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mRegisterEmailView.setError(getString(R.string.error_field_required));
            focusView = mRegisterEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mRegisterEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mRegisterEmailView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mRegisterPasswordView.setError(getString(R.string.error_field_required));
            focusView = mRegisterPasswordView;
            cancel = true;
        }
        if (!isPasswordValid(password)) {
            mRegisterPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mRegisterPasswordView;
            cancel = true;
        }

        // Check for a valid confirm password, if the user entered one.
        if (TextUtils.isEmpty(confirmpassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = mConfirmPasswordView;
            cancel = true;
        }
        if (!isCheckPasswordCorrect(password, confirmpassword)) {
            mConfirmPasswordView.setError("Password Not Match");
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    final FirebaseUser user = task.getResult().getUser();
                    user.updateProfile(profileUpdates);
                    FireBaseHelper.Users FUSER = new FireBaseHelper.Users();
                    FUSER.name = name;
                    FUSER.email = user.getEmail();
                    FUSER.image_uri = "";
                    FUSER.type_id = DESIGNER_TYPE;
                    FUSER.Add(user.getUid());
                    user.sendEmailVerification()
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                    Toast.makeText(LoginActivity.this,
                                            "Verification email sent to " + user.getEmail(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                    showProgress(false);
                    startActivity(new Intent(LoginActivity.this,BaseActivity.class));
                    finish();
                }
            });
        }
    }

    /**
     * Attempts To Recover account password
     */
    private void ForgetPassword() {

        // Reset errors.
        mLoginEmailView.setError(null);
        //mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mLoginEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mLoginEmailView.setError(getString(R.string.error_field_required));
            focusView = mLoginEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mLoginEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mLoginEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showProgress(false);
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(LoginActivity.this, "Please,Check Your Email Address\nWe send You a password Reset Link",
                                    Toast.LENGTH_LONG).show();
                        }
                        else {
                            showProgress(false);
                            Log.d(TAG, "Email Noy sent."+task.getException().getMessage());
                            Toast.makeText(LoginActivity.this,task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private boolean isNameValid(String name) {
        return name.length() > 3;
    }

    private boolean isCheckPasswordCorrect(String password, String confirm_password) {
        return password.equals(confirm_password);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mMainFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mMainFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMainFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    /**
     * Email Auto Complete
     * Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
     */
    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mRegisterEmailView.setAdapter(adapter);
        mLoginEmailView.setAdapter(adapter);
    }

    /**
     * Email Auto Complete
     * Used To Get All Emails in User Contacts
     */
    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

}

