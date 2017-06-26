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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.AnimateGifMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ModelFragment extends Fragment {

    private static final String TAG = "ModelActivity";
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
    private ProgressBar mdownloadprogressBar;
    private LinearLayout mDownloading;
    private TextView mDownloadLog;
    private TextView mDownloadpresentage;
    private Button mdownloadbutton , mAddFeedback;
    private TextView mNameView;
    private TextView mCompanyView;
    private TextView mSizeView;
    private ImageView mimageView;
    private CollapsingToolbarLayout mTitleBarView;
    private Context context;
    private ArrayList<String> fdb , userFdbImg , Feeders, feedbackDate ;
    private FeedbackItems adapter ;
    private EditText getFeedback ;
    private FirebaseAuth mFirebaseAuth ;


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
            try {
                File Dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FurnitureGo" + File.separator + Data.Key);
                if (!Dir.exists())
                    if (!Dir.mkdir()) {
                        Toast.makeText(context, "Create Directory Error", Toast.LENGTH_LONG).show();
                    }
                File modelFile = File.createTempFile(Data.Key, ".wt3", Dir);
                File imageFile = File.createTempFile(Data.Key, ".png", Dir);
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
                        int presentage = (int) ((taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) * 100);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        try {
            FragmentTransaction ft = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            ft.remove(this);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        mProgressView = (ProgressBar) view.findViewById(R.id.progress);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.container);
        mAppBarView = (AppBarLayout) view.findViewById(R.id.app_bar);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        ObjectKey = getArguments().getString(OBJECT_KEY);
        storage = FirebaseStorage.getInstance();
        mgifImageView = (GifImageView) view.findViewById(R.id.gif);
        mdownloadprogressBar = (ProgressBar) view.findViewById(R.id.downloadprogressBar);
        mDownloading = (LinearLayout) view.findViewById(R.id.Downloading);
        mDownloadLog = (TextView) view.findViewById(R.id.TextDownloadlog);
        mDownloadpresentage = (TextView) view.findViewById(R.id.Textpresentage);
        mdownloadbutton = (Button) view.findViewById(R.id.Download);
        mNameView = (TextView) view.findViewById(R.id.Name_TextView);
        mCompanyView = (TextView) view.findViewById(R.id.Company_TextView);
        mSizeView = (TextView) view.findViewById(R.id.Size_TextView);
        mimageView = (ImageView) view.findViewById(R.id.model_imageView);
        mTitleBarView = (CollapsingToolbarLayout) view.findViewById(R.id.toolbar_layout);
        context = getContext();
        activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().hide();
        activity.findViewById(R.id.tabs).setVisibility(View.GONE);
        showProgress(true);
        new FireBaseHelper.Objects().Findbykey(ObjectKey, Data -> {

            Ion.with(context)
                    .load(Data.image_path)
                    .withBitmap()
                    .placeholder(R.drawable.loading)
                    .animateGif(AnimateGifMode.ANIMATE)
                    .fitXY()
                    .intoImageView(mimageView);
            Ion.with(context)
                    .load(Data.gif_path)
                    .withBitmap()
                    .placeholder(R.drawable.loading)
                    .animateGif(AnimateGifMode.ANIMATE)
                    .fitXY()
                    .intoImageView(mgifImageView);


            fdb = new ArrayList<>() ;
            userFdbImg = new ArrayList<>() ;
            Feeders = new ArrayList<>() ;
            feedbackDate = new ArrayList<>();


            //fdb.add("test");
            ListView feedbacks = (ListView) activity.findViewById(R.id.FeedbackList) ;
            new FireBaseHelper.Feedbacks().Where(FireBaseHelper.Feedbacks.Table.Object_id,ObjectKey, data->{
                int i = 0 ;
                for (FireBaseHelper.Feedbacks item:data) {
                        fdb.add(item.feedback.toString());
                        Feeders.add(item.users.getName());
                        userFdbImg.add(item.users.getImage_uri());
                        feedbackDate.add(item.getDate()) ;
                    //Toast.makeText(activity,item.users.getImage_uri() ,Toast.LENGTH_SHORT).show();
                }
                adapter =  new FeedbackItems(activity,Feeders,fdb,userFdbImg,feedbackDate) ;
                feedbacks.setAdapter(adapter);
                //Toast.makeText(activity,Integer.toString(Feeders.size()),Toast.LENGTH_SHORT).show();
            });


            mCompanyView.setText(Data.companies.name);
            mNameView.setText(Data.name);
            mTitleBarView.setTitle(Data.name);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference modelRef = storage.getReferenceFromUrl(Data.model_path);
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
                showProgress(false);
            });
            File Dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FurnitureGo" + File.separator + Data.Key);
            if (Dir.exists()) {
                if (modelRef.getActiveDownloadTasks().size() > 0) {
                    mDownloading.setVisibility(View.VISIBLE);
                    mdownloadbutton.setText("Cancel");
                    fab.setImageResource(R.drawable.ic_cancel);
                    FileDownloadTask task = modelRef.getActiveDownloadTasks().get(0);
                    task.addOnCompleteListener(task1 -> {
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                    fab.setOnClickListener(v -> {
                        task.cancel();
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                    mdownloadbutton.setOnClickListener(v -> {
                        task.cancel();
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                } else {
                    fab.setImageResource(R.drawable.ic_delete);
                    mdownloadbutton.setText("Delete");
                    fab.setOnClickListener(v -> {
                        Delete(Data.Key, context);
                        mdownloadbutton.setText("Download");
                        fab.setImageResource(R.drawable.ic_download);
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                    mdownloadbutton.setOnClickListener(v -> {
                        Delete(Data.Key, context);
                        mdownloadbutton.setText("Download");
                        fab.setImageResource(R.drawable.ic_download);
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                }
            } else {
                fab.setOnClickListener(v -> {
                    mDownloading.setVisibility(View.VISIBLE);
                    mdownloadbutton.setText("Cancel");
                    fab.setImageResource(R.drawable.ic_cancel);
                    FileDownloadTask task = Download(Data, context);
                    task.addOnCompleteListener(task1 -> {
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                    fab.setOnClickListener(vv -> {
                        task.cancel();
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                    mdownloadbutton.setOnClickListener(vv -> {
                        task.cancel();
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                });
                mdownloadbutton.setOnClickListener(v -> {
                    mDownloading.setVisibility(View.VISIBLE);
                    mdownloadbutton.setText("Cancel");
                    fab.setImageResource(R.drawable.ic_cancel);
                    FileDownloadTask task = Download(Data, context);
                    task.addOnCompleteListener(task1 -> {
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                    fab.setOnClickListener(vv -> {
                        task.cancel();
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                    mdownloadbutton.setOnClickListener(vv -> {
                        task.cancel();
                        /*FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                        ft.detach(this);
                        ft.attach(this);
                        ft.commit();*/
                    });
                });
            }

            getFeedback = (EditText)activity.findViewById(R.id.FeedbackTxt);
            mAddFeedback = (Button) activity.findViewById(R.id.addFeedback);
            mAddFeedback.setOnClickListener(v -> {
                FireBaseHelper.Feedbacks feedbacks1 = new FireBaseHelper.Feedbacks() ;
                String feedbkTxt = getFeedback.getText().toString() ;
                if (feedbkTxt.isEmpty()){
                    Toast.makeText(activity,"You must enter your feedback first!",Toast.LENGTH_SHORT).show();
                }
                else{
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(c.getTime());

                    mFirebaseAuth = FirebaseAuth.getInstance() ;
                    String UserKey = mFirebaseAuth.getCurrentUser().getUid() ;

                    int rate = 5 ;

                    feedbacks1.setFeedback(feedbkTxt);
                    feedbacks1.setDate(formattedDate);
                    feedbacks1.setObject_id(ObjectKey);
                    feedbacks1.setUid(UserKey);
                    feedbacks1.setRate(Integer.toString(rate));

                    feedbacks1.Add();


                }
            });

        });
        return view;
    }

}
