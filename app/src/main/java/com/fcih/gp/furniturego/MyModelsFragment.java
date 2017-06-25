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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class MyModelsFragment extends Fragment {

    private FirebaseRecyclerAdapter<FireBaseHelper.Favorites, viewholder> mAdapter = null;
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
            TextView textView = (TextView) view.findViewById(R.id.emptycategory);
            textView.setText("Your Don't Have any Model");
            textView.setVisibility(View.VISIBLE);
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
                    new FireBaseHelper.Objects().Findbykey(Keys.get(position), Data -> {
                        viewHolder.mTitleView.setText(Data.name);
                        viewHolder.mCompanyView.setText(Data.companies.name);
                        viewHolder.mRateView.setText(getRate(Data.feedbacks));
                        Picasso.with(getContext()).load(Data.image_path).into(viewHolder.mImageView);
                        viewHolder.mView.setOnClickListener(v -> {
                            ModelFragment mod = ModelFragment.newInstance(Data.Key);
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent, mod).addToBackStack(null).commit();
                        });
                        viewHolder.mImageButton.setOnClickListener(v -> {
                            PopupMenu popup = new PopupMenu(context, viewHolder.mImageButton);
                            MenuInflater inflater1 = popup.getMenuInflater();
                            inflater1.inflate(R.menu.pop_menu, popup.getMenu());
                            new FireBaseHelper.Favorites().Findbykey(Data.Key + mAuth.getCurrentUser().getUid(), Data1 -> {
                                if (Data1 == null) {
                                    popup.getMenu().findItem(R.id.item_favorite).setOnMenuItemClickListener(menuItem -> {
                                        FireBaseHelper.Favorites favorites = new FireBaseHelper.Favorites();
                                        favorites.user_id = mAuth.getCurrentUser().getUid();
                                        favorites.object_id = Data.Key;
                                        favorites.Add(Data.Key + mAuth.getCurrentUser().getUid());
                                        return true;
                                    });

                                } else {
                                    popup.getMenu().findItem(R.id.item_favorite).setTitle("Remove From Favorite");
                                    popup.getMenu().findItem(R.id.item_favorite).setOnMenuItemClickListener(menuItem -> {
                                        Data1.Remove(Data.Key + mAuth.getCurrentUser().getUid());
                                        return true;
                                    });
                                }
                            });
                            popup.getMenu().findItem(R.id.item_download).setVisible(false);
                            popup.getMenu().findItem(R.id.item_delete).setVisible(true);
                            popup.setOnMenuItemClickListener(item -> {
                                int id = item.getItemId();
                                if (id == R.id.item_delete) {
                                    ModelFragment.Delete(Data.Key, getContext());
                                    mAdapter.getRef(position).removeValue();
                                    mAdapter.notifyDataSetChanged();
                                }
                                return true;
                            });
                            popup.show();
                        });
                        if (mAdapter.getItemCount() - 1 == position) {
                            progressDialog.dismiss();
                        }
                    });
                }

                @Override
                public int getItemCount() {
                    return Keys.size();
                }
            };
            recyclerView.setAdapter(Adapter);
        }
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
