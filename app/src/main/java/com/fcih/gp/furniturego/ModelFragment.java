package com.fcih.gp.furniturego;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.AnimateGifMode;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ModelFragment extends Fragment {

    public static final String TAG = "ModelFragment";
    private static final String OBJECT_KEY = "KEY";
    private static final int DOWNLOAD_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1001;
    private String ObjectKey;
    private FirebaseStorage storage;
    private LinearLayout mLinearLayout;
    private ProgressBar mProgressView;
    private AppBarLayout mAppBarView;
    private FloatingActionButton fab;
    private BaseActivity activity;
    private GifImageView mgifImageView;
    private LinearLayout mDownloading;
    private Button mdownloadbutton;
    private Button mAddFeedback;
    private TextView mNameView;
    private TextView mDescriptionView;
    private TextView mCompanyView;
    private TextView mSizeView;
    private ImageView mimageView;
    private TextView mModelRate;
    private TextView mModelFeedNum;
    private RatingBar mModelStars;

    private CollapsingToolbarLayout mTitleBarView;
    private Context context;
    private EditText FeedbackText;
    private RatingBar FeedbackRate;
    private FirebaseAuth mAuth;
    private StorageReference modelRef;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<FireBaseHelper.Feedbacks, viewholder> mAdapter;
    private RoundedImageView FeedUser;


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

    public static FileDownloadTask Download(FireBaseHelper.Objects Data, Context context) {
        FileDownloadTask task = null;
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference modelRef = storage.getReferenceFromUrl(Data.model_path);
            final StorageReference imageRef = storage.getReferenceFromUrl(Data.image_path);
            File Dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FurnitureGo" + File.separator + Data.Key + File.separator);
            if (!Dir.exists())
                if (!Dir.mkdirs()) {
                    Toast.makeText(context, "Create Directory Error", Toast.LENGTH_LONG).show();
                }
            File modelFile = new File(Dir, Data.Key + ".wt3");
            File imageFile = new File(Dir, Data.Key + ".png");
            imageRef.getFile(imageFile);
            task = modelRef.getFile(modelFile);
            task.addOnProgressListener(taskSnapshot -> {
                double data = taskSnapshot.getBytesTransferred();
                boolean mb = false;
                data /= 1024;
                if (data > 1000) {
                    data /= 1024;
                    mb = true;
                }
                String Transferred = String.format(Locale.ENGLISH, "%.2f" + (mb ? "MB" : "KB"), data);
                data = taskSnapshot.getTotalByteCount();
                mb = false;
                data /= 1024;
                if (data > 1000) {
                    data /= 1024;
                    mb = true;
                }
                String Total = String.format(Locale.ENGLISH, "%.2f" + (mb ? "MB" : "KB"), data);
                Activity activity = (Activity) context;
                if (activity.findViewById(R.id.TextDownloadlog) != null) {
                    TextView textView = (TextView) activity.findViewById(R.id.TextDownloadlog);
                    textView.setText(Transferred + "/" + Total);
                }
                if (activity.findViewById(R.id.Textpresentage) != null) {
                    TextView textView = (TextView) activity.findViewById(R.id.Textpresentage);
                    int presentage = (int) (taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount());
                    textView.setText(presentage + "%");
                }
                if (activity.findViewById(R.id.downloadprogressBar) != null) {
                    ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.downloadprogressBar);
                    int presentage = (int) ((taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) * 100);
                    progressBar.setProgress(presentage);
                }
            });
            task.addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(context, "Download Complete", Toast.LENGTH_LONG).show();
                Activity activity = (Activity) context;
                if (activity.findViewById(R.id.TextDownloadlog) != null) {
                    TextView textView = (TextView) activity.findViewById(R.id.TextDownloadlog);
                    textView.setText("Download Success");
                }
            });
            task.addOnFailureListener(task1 -> {
                Toast.makeText(context, "Download Failed " + task1.getMessage(), Toast.LENGTH_LONG).show();
                Activity activity = (Activity) context;
                if (activity.findViewById(R.id.TextDownloadlog) != null) {
                    TextView textView = (TextView) activity.findViewById(R.id.TextDownloadlog);
                    textView.setText("Download Failed");
                }
            });
        }
        return task;
    }

    public static void Delete(String Key, Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
        } else {
            File Dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FurnitureGo" + File.separator + Key);
            deleteRecursive(Dir);
            if (Dir.exists()) {
                Toast.makeText(context, "Delete Directory Error", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.cleanup();
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
        View view = inflater.inflate(R.layout.fragment_model, container, false);

        ObjectKey = getArguments().getString(OBJECT_KEY);
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        context = getContext();
        activity = (BaseActivity) getActivity();

        mProgressView = (ProgressBar) view.findViewById(R.id.progress);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.container);
        mAppBarView = (AppBarLayout) view.findViewById(R.id.app_bar);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        mgifImageView = (GifImageView) view.findViewById(R.id.gif);
        mDownloading = (LinearLayout) view.findViewById(R.id.Downloading);
        mdownloadbutton = (Button) view.findViewById(R.id.Download);
        mNameView = (TextView) view.findViewById(R.id.Name_TextView);
        mDescriptionView = (TextView) view.findViewById(R.id.model_description);
        mCompanyView = (TextView) view.findViewById(R.id.Company_TextView);
        mSizeView = (TextView) view.findViewById(R.id.Size_TextView);
        mimageView = (ImageView) view.findViewById(R.id.model_imageView);
        mTitleBarView = (CollapsingToolbarLayout) view.findViewById(R.id.toolbar_layout);
        recyclerView = (RecyclerView) view.findViewById(R.id.FeedbackList);
        mModelRate = (TextView) view.findViewById(R.id.model_rate);
        mModelFeedNum = (TextView) view.findViewById(R.id.model_feed_num);
        mModelStars = (RatingBar) view.findViewById(R.id.model_stars);
        mAddFeedback = (Button) view.findViewById(R.id.Submit_Button);
        FeedbackText = (EditText) view.findViewById(R.id.User_Feedback);
        FeedbackRate = (RatingBar) view.findViewById(R.id.User_Rate);
        FeedUser = (RoundedImageView) view.findViewById(R.id.userimage);

        activity.getSupportActionBar().hide();
        activity.findViewById(R.id.tabs).setVisibility(View.GONE);

        showProgress(true);

        InitializeFeedback();
        InitializeUserFeedback();
        new FireBaseHelper.Objects().Findbykey(ObjectKey, Data -> {
            modelRef = storage.getReferenceFromUrl(Data.model_path);
            InitializeModel(Data);
            InitializeDownload(Data);
            showProgress(false);
        });

        return view;
    }

    private void InitializeUserFeedback() {
        if (mAuth.getCurrentUser().getPhotoUrl() != null) {
            Ion.with(context)
                    .load(mAuth.getCurrentUser().getPhotoUrl().toString())
                    .withBitmap()
                    .fitXY()
                    .intoImageView(FeedUser);
            mAddFeedback.setEnabled(false);
        }
        FeedbackText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    mAddFeedback.setEnabled(false);
                } else if (FeedbackRate.getRating() == 0) {
                    mAddFeedback.setEnabled(false);
                } else mAddFeedback.setEnabled(true);
            }
        });

        FeedbackRate.setOnRatingBarChangeListener((ratingBar, v, b) -> {
            if (v == 0) {
                mAddFeedback.setEnabled(false);
            } else if (TextUtils.isEmpty(FeedbackText.getText().toString())) {
                mAddFeedback.setEnabled(false);
            } else mAddFeedback.setEnabled(true);

        });

        mAddFeedback.setOnClickListener(view1 -> {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat Formater = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            FireBaseHelper.Feedbacks feedbacks = new FireBaseHelper.Feedbacks();
            feedbacks.date = Formater.format(calendar.getTime());
            feedbacks.feedback = FeedbackText.getText().toString();
            feedbacks.rate = String.valueOf((int) FeedbackRate.getRating());
            feedbacks.uid = mAuth.getCurrentUser().getUid();
            feedbacks.object_id = ObjectKey;
            feedbacks.Add(ObjectKey + mAuth.getCurrentUser().getUid());
            FeedbackText.setText("");
            FeedbackRate.setRating(0);
        });
    }

    private void InitializeModel(FireBaseHelper.Objects Data) {
        Ion.with(context)
                .load(Data.image_path)
                .withBitmap()
                .fitXY()
                .intoImageView(mimageView);
        Ion.with(context)
                .load(Data.gif_path)
                .withBitmap()
                .animateGif(AnimateGifMode.ANIMATE)
                .fitXY()
                .intoImageView(mgifImageView);

        mCompanyView.setText(Data.companies.name);
        mNameView.setText(Data.name);
        mDescriptionView.setText(Data.description+"\n Post Date: "+Data.date+"\n About Company: "+Data.companies.about+"\n Price : "+Data.price);
        mTitleBarView.setTitle(Data.name);
        mTitleBarView.setExpandedTitleColor(getResources().getColor(R.color.colorPrimary));
        modelRef.getMetadata().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                double kb = (double) task.getResult().getSizeBytes() / 1024;
                if (kb > 1000) {
                    double mb = kb / 1024;
                    mSizeView.setText(String.format(Locale.ENGLISH, "%.2fMB", mb));
                } else {
                    mSizeView.setText(String.format(Locale.ENGLISH, "%.2fKB", kb));
                }
            } else {
                Toast.makeText(context, task.getException().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void InitializeDownload(FireBaseHelper.Objects Data) {
        File Dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FurnitureGo" + File.separator + Data.Key);
        if (Dir.exists()) {
            if (modelRef.getActiveDownloadTasks().size() > 0) {
                mDownloading.setVisibility(View.VISIBLE);
                mdownloadbutton.setText("Cancel");
                fab.setImageResource(R.drawable.ic_cancel);
                FileDownloadTask task = modelRef.getActiveDownloadTasks().get(0);
                task.addOnCompleteListener(task1 -> {
                    Refresh();
                });
                fab.setOnClickListener(v -> {
                    task.cancel();
                    Delete(Data.Key, context);
                    Refresh();
                });
                mdownloadbutton.setOnClickListener(v -> {
                    task.cancel();
                    Delete(Data.Key, context);
                    Refresh();
                });
            } else {
                fab.setImageResource(R.drawable.ic_delete);
                mdownloadbutton.setText("Delete");
                fab.setOnClickListener(v -> {
                    Delete(Data.Key, context);
                    mdownloadbutton.setText("Download");
                    fab.setImageResource(R.drawable.ic_download);
                    Refresh();
                });
                mdownloadbutton.setOnClickListener(v -> {
                    Delete(Data.Key, context);
                    mdownloadbutton.setText("Download");
                    fab.setImageResource(R.drawable.ic_download);
                    Refresh();
                });
            }
        } else {
            fab.setOnClickListener(v -> {
                mDownloading.setVisibility(View.VISIBLE);
                mdownloadbutton.setText("Cancel");
                fab.setImageResource(R.drawable.ic_cancel);
                FileDownloadTask task = Download(Data, context);
                task.addOnCompleteListener(task1 -> {
                    Refresh();
                });
                fab.setOnClickListener(vv -> {
                    task.cancel();
                    Delete(Data.Key, context);
                    Refresh();
                });
                mdownloadbutton.setOnClickListener(vv -> {
                    task.cancel();
                    Delete(Data.Key, context);
                    Refresh();
                });
            });
            mdownloadbutton.setOnClickListener(v -> {
                mDownloading.setVisibility(View.VISIBLE);
                mdownloadbutton.setText("Cancel");
                fab.setImageResource(R.drawable.ic_cancel);
                FileDownloadTask task = Download(Data, context);
                task.addOnCompleteListener(task1 -> {
                    Refresh();
                });
                fab.setOnClickListener(vv -> {
                    task.cancel();
                    Refresh();
                });
                mdownloadbutton.setOnClickListener(vv -> {
                    task.cancel();
                    Refresh();
                });
            });
        }
    }

    private void InitializeFeedback() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Query query = FireBaseHelper.Feedbacks.Ref.orderByChild(FireBaseHelper.Feedbacks.Table.Object_id.text).equalTo(ObjectKey);
        mAdapter = new FirebaseRecyclerAdapter<FireBaseHelper.Feedbacks, viewholder>(
                FireBaseHelper.Feedbacks.class, R.layout.feedback_item, viewholder.class, query) {
            private int Counter = 0;
            private int Sum = 0;

            @Override
            protected void populateViewHolder(viewholder viewholder, FireBaseHelper.Feedbacks feedbacks, int position) {
                new FireBaseHelper.Feedbacks().Findbykey(mAdapter.getRef(position).getKey(), Data1 -> {
                    viewholder.mDateView.setText(Data1.date);
                    viewholder.mFeedbackView.setText(Data1.feedback);
                    viewholder.mRateView.setRating(Float.parseFloat(Data1.rate));
                    viewholder.mUserNameView.setText(Data1.users.name);
                    viewholder.mDeleteView.setOnClickListener(view -> {
                        Data1.Remove(Data1.Key);
                    });
                    if (Data1.uid.equals(mAuth.getCurrentUser().getUid())) {
                        viewholder.mDeleteView.setVisibility(View.VISIBLE);
                    } else {
                        viewholder.mDeleteView.setVisibility(View.GONE);
                    }
                    Ion.with(context)
                            .load(Data1.users.image_uri)
                            .withBitmap()
                            .fitXY()
                            .intoImageView(viewholder.mImageView);
                    Sum += Integer.parseInt(Data1.rate);
                    if (mAdapter.getItemCount() == ++Counter) {
                        mModelFeedNum.setText(String.valueOf(mAdapter.getItemCount()));
                        String rate = String.format(Locale.ENGLISH, "%.1f", (double) Sum / mAdapter.getItemCount());
                        mModelRate.setText(rate);
                        mModelStars.setRating((float) Sum / mAdapter.getItemCount());
                    }
                });

            }
        };
        recyclerView.setAdapter(mAdapter);
    }

    private void Refresh() {
        Fragment frg = activity.getSupportFragmentManager().findFragmentByTag(TAG);
        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                .beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUserNameView;
        public final TextView mDateView;
        public final RatingBar mRateView;
        public final RoundedImageView mImageView;
        public final TextView mFeedbackView;
        public final ImageView mDeleteView;

        public viewholder(View view) {
            super(view);
            mView = view;
            mUserNameView = (TextView) view.findViewById(R.id.feed_user_name);
            mDateView = (TextView) view.findViewById(R.id.feed_date);
            mRateView = (RatingBar) view.findViewById(R.id.feed_rate);
            mImageView = (RoundedImageView) view.findViewById(R.id.feed_user_image);
            mFeedbackView = (TextView) view.findViewById(R.id.feed_text);
            mDeleteView = (ImageView) view.findViewById(R.id.feedback_delete);
        }
    }

}
