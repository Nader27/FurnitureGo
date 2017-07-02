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
import com.google.firebase.database.Query;


/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";
    private static final String OBJECT_QUERY = "QUERY";
    private String query;
    private FirebaseRecyclerAdapter<FireBaseHelper.Objects, ViewHolder> mAdapter = null;
    private RecyclerView recyclerView;
    private BaseActivity activity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SearchFragment() {
    }

    public static SearchFragment newInstance(String Query) {
        SearchFragment fragment = new SearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString(OBJECT_QUERY, Query);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (BaseActivity) getActivity();
        query = getArguments().getString(OBJECT_QUERY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // Set the adapter
        final Context context = view.getContext();
        activity.findViewById(R.id.tabs).setVisibility(View.GONE);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Searching");
        progressDialog.setMessage("Searching For " + query);
        progressDialog.show();
        Query que = FireBaseHelper.Objects.Ref.orderByChild(FireBaseHelper.Objects.Table.Name.text).startAt(this.query);
        new FireBaseHelper.Objects().Where(que, Data -> {
            if (Data.size() == 0) {
                progressDialog.dismiss();
                TextView textView = (TextView) view.findViewById(R.id.empty);
                textView.setText("Your Search '" + this.query + "' didn't match any model");
                textView.setVisibility(View.VISIBLE);
                //showProgress(false);
            } else {
                mAdapter = new FirebaseRecyclerAdapter<FireBaseHelper.Objects, ViewHolder>(
                        FireBaseHelper.Objects.class, R.layout.fragment_item, ViewHolder.class, que) {
                    @Override
                    protected void populateViewHolder(ViewHolder viewHolder, FireBaseHelper.Objects model, int position) {
                        model.Findbykey(mAdapter.getRef(position).getKey(), Data -> {
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
                }

                ;
                recyclerView.setAdapter(mAdapter);
            }
        });
        return view;
    }


}
