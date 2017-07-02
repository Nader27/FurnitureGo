package com.fcih.gp.furniturego;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;


/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class FavFragment extends Fragment {

    public static final String TAG = "FavFragment";
    private FirebaseRecyclerAdapter<FireBaseHelper.Favorites, ViewHolder> mAdapter = null;
    private RecyclerView recyclerView;
    private BaseActivity activity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavFragment() {
    }

    public static FavFragment newInstance() {
        return new FavFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // Set the adapter
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final Context context = view.getContext();
        activity = (BaseActivity) getActivity();
        activity.findViewById(R.id.tabs).setVisibility(View.GONE);
        activity.getSupportActionBar().show();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Query query = FireBaseHelper.Favorites.Ref.orderByChild(FireBaseHelper.Favorites.Table.User_id.text).equalTo(mAuth.getCurrentUser().getUid());
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading WhishList");
        progressDialog.show();
        new FireBaseHelper.Favorites().Where(FireBaseHelper.Favorites.Table.User_id, mAuth.getCurrentUser().getUid(), Data -> {
            if (Data.size() == 0) {
                progressDialog.dismiss();
                TextView textView = (TextView) view.findViewById(R.id.empty);
                textView.setText(getResources().getText(R.string.empty_whishlist));
                textView.setVisibility(View.VISIBLE);
                //showProgress(false);
            } else {
                mAdapter = new FirebaseRecyclerAdapter<FireBaseHelper.Favorites, ViewHolder>(
                        FireBaseHelper.Favorites.class, R.layout.fragment_item, ViewHolder.class, query) {
                    @Override
                    protected void populateViewHolder(ViewHolder viewHolder, FireBaseHelper.Favorites model, int position) {
                        new FireBaseHelper.Objects().Findbykey(model.object_id, Data -> {
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
                };
                recyclerView.setAdapter(mAdapter);
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.cleanup();
    }
}
