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
public class ItemFragment extends Fragment {

    public static final String TAG = "ItemFragment";
    private static final String OBJECT_KEY = "KEY";
    private static final String OBJECT_NAME = "NAME";
    private String CategoryKEY;
    private FirebaseRecyclerAdapter<FireBaseHelper.Objects, ViewHolder> mAdapter = null;
    private RecyclerView recyclerView;
    private String CategoryNAME;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    public static ItemFragment newInstance(String categorykey, String categoryname) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(OBJECT_KEY, categorykey);
        args.putString(OBJECT_NAME, categoryname);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CategoryKEY = getArguments().getString(OBJECT_KEY);
        CategoryNAME = getArguments().getString(OBJECT_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // Set the adapter
        final Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        TextView empty = (TextView) view.findViewById(R.id.empty);
        Query query = FireBaseHelper.Objects.Ref.orderByChild(FireBaseHelper.Objects.Table.Category.text).equalTo(CategoryKEY);
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading Category : " + CategoryNAME);
        progressDialog.show();
        new FireBaseHelper.Objects().Where(FireBaseHelper.Objects.Table.Category, CategoryKEY, Data -> {
            if (Data.size() < 1) {
                progressDialog.dismiss();
                empty.setText(getResources().getText(R.string.empty_category));
                empty.setVisibility(View.VISIBLE);
            } else {
                mAdapter = new FirebaseRecyclerAdapter<FireBaseHelper.Objects, ViewHolder>(
                        FireBaseHelper.Objects.class, R.layout.fragment_item, ViewHolder.class, query) {
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
                };
                recyclerView.setAdapter(mAdapter);
            }
        });
        return view;
    }

}
