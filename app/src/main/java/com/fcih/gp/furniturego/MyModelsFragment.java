package com.fcih.gp.furniturego;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class MyModelsFragment extends Fragment {

    public static final String TAG = "MyModelsFragment";
    private RecyclerView.Adapter<ViewHolder> mAdapter = null;
    private RecyclerView recyclerView;
    private BaseActivity activity;
    private List<String> Keys;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MyModelsFragment() {
    }

    public static MyModelsFragment newInstance() {
        return new MyModelsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // Set the adapter
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final Context context = view.getContext();
        activity.findViewById(R.id.tabs).setVisibility(View.GONE);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading Your Models");
        progressDialog.show();
        Keys = new ArrayList<>();
        File Dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FurnitureGo" + File.separator);
        if (Dir.isDirectory()) {
            for (File child : Dir.listFiles()) {
                if (child.isDirectory()) {
                    Keys.add(child.getName());
                }
            }
        }
        if (Keys.size() == 0) {
            progressDialog.dismiss();
            TextView textView = (TextView) view.findViewById(R.id.empty);
            textView.setText(getResources().getText(R.string.empty_model));
            textView.setVisibility(View.VISIBLE);
        } else {
            mAdapter = new RecyclerView.Adapter<ViewHolder>() {
                @Override
                public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.fragment_item, parent, false);

                    return new ViewHolder(itemView);
                }

                @Override
                public void onBindViewHolder(ViewHolder viewHolder, int position) {
                    new FireBaseHelper.Objects().Findbykey(Keys.get(position), Data -> {
                        viewHolder.Initialize(Data);
                        viewHolder.mView.setOnClickListener(v -> {
                            ModelFragment fragment = ModelFragment.newInstance(Data.Key);
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment, ModelFragment.TAG).addToBackStack(null).commit();
                        });
                    });
                    if (mAdapter.getItemCount() - 1 == position) {
                        progressDialog.dismiss();
                    }
                }

                @Override
                public int getItemCount() {
                    return Keys.size();
                }
            };
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

}
