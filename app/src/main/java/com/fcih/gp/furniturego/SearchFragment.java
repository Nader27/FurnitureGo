package com.fcih.gp.furniturego;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Locale;


/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class SearchFragment extends Fragment {

    private static final String OBJECT_QUERY = "QUERY";
    private String query;
    private ProgressBar mProgressView;
    private FirebaseRecyclerAdapter<FireBaseHelper.Favorites, viewholder> mAdapter = null;
    private RecyclerView recyclerView;
    private View mView;
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
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final Context context = view.getContext();
        activity.findViewById(R.id.tabs).setVisibility(View.GONE);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Query query = FireBaseHelper.Favorites.Ref.orderByChild(FireBaseHelper.Favorites.Table.User_id.text).equalTo(mAuth.getCurrentUser().getUid());
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading WhishList");
        progressDialog.show();
        Query que = FireBaseHelper.Objects.Ref.orderByChild(FireBaseHelper.Objects.Table.Name.text).startAt(this.query).endAt(this.query + "\uf8ff");
        new FireBaseHelper.Objects().Where(que, Data -> {
            if (Data.size() == 0) {
                progressDialog.dismiss();
                TextView textView = (TextView) view.findViewById(R.id.emptycategory);
                textView.setText("Your Search '" + this.query + "' didn't match any model");
                textView.setVisibility(View.VISIBLE);
                //showProgress(false);
            } else {
                RecyclerView.Adapter<viewholder> Adapter = new RecyclerView.Adapter<viewholder>() {
                    @Override
                    public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View itemView = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_item, parent, false);

                        return new viewholder(itemView);
                    }

                    @Override
                    public void onBindViewHolder(viewholder viewHolder, int position) {
                        FireBaseHelper.Objects data = Data.get(position);
                        viewHolder.mTitleView.setText(data.name);
                        viewHolder.mCompanyView.setText(data.companies.name);
                        viewHolder.mRateView.setText(getRate(data.feedbacks));
                        Picasso.with(getContext()).load(data.image_path).into(viewHolder.mImageView);
                        viewHolder.mView.setOnClickListener(v -> {
                            ModelFragment mod = ModelFragment.newInstance(data.Key);
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent, mod).addToBackStack(null).commit();
                        });
                        viewHolder.mImageButton.setOnClickListener(v -> {
                            PopupMenu popup = new PopupMenu(context, viewHolder.mImageButton);
                            MenuInflater inflater1 = popup.getMenuInflater();
                            inflater1.inflate(R.menu.pop_menu, popup.getMenu());
                            popup.getMenu().findItem(R.id.item_favorite).setTitle("Remove From Favorite");
                            File Dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FurnitureGo" + File.separator + data.Key);
                            if (Dir.exists()) {
                                popup.getMenu().findItem(R.id.item_download).setVisible(false);
                                popup.getMenu().findItem(R.id.item_delete).setVisible(true);
                            } else {
                                popup.getMenu().findItem(R.id.item_download).setVisible(true);
                                popup.getMenu().findItem(R.id.item_delete).setVisible(false);
                            }
                            popup.setOnMenuItemClickListener(item -> {
                                int id = item.getItemId();
                                if (id == R.id.item_download) {
                                    ModelFragment.Download(data, getContext());
                                } else if (id == R.id.item_favorite) {
                                    mAdapter.getRef(position).removeValue();
                                    mAdapter.notifyDataSetChanged();
                                } else if (id == R.id.item_delete) {
                                    ModelFragment.Delete(data.Key, getContext());
                                }
                                return true;
                            });
                            popup.show();
                        });
                        if (mAdapter.getItemCount() - 1 == position) {
                            progressDialog.dismiss();
                        }

                    }

                    @Override
                    public int getItemCount() {
                        return Data.size();
                    }
                };
                recyclerView.setAdapter(Adapter);
            }
        });
        return view;
    }

    private String getRate(List<FireBaseHelper.Feedbacks> lst) {

        if (lst.size() == 0) {
            return "0.0";
        } else {
            int sum = 0;

            for (FireBaseHelper.Feedbacks item : lst) {
                sum += Integer.parseInt(item.rate);
            }
            return String.format(Locale.ENGLISH, "%.1f", (double) sum / lst.size());
        }
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mCompanyView;
        public final TextView mRateView;
        public final ImageView mImageView;
        public final ImageButton mImageButton;

        public viewholder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.item_title);
            mCompanyView = (TextView) view.findViewById(R.id.item_company);
            mRateView = (TextView) view.findViewById(R.id.item_rate);
            mImageView = (ImageView) view.findViewById(R.id.item_image);
            mImageButton = (ImageButton) view.findViewById(R.id.item_menu);
        }
    }


}
