package com.fcih.gp.furniturego;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
public class ItemFragment extends Fragment {

    private static final String OBJECT_KEY = "KEY";
    private static final String OBJECT_NAME = "NAME";
    private String CategoryKEY;
    private FirebaseRecyclerAdapter<FireBaseHelper.Objects, viewholder> mAdapter = null;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CategoryKEY = getArguments().getString(OBJECT_KEY);
        CategoryNAME = getArguments().getString(OBJECT_NAME);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // Set the adapter
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Query query = FireBaseHelper.Objects.Ref.orderByChild(FireBaseHelper.Objects.Table.Category.text).equalTo(CategoryKEY);
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading Category : " + CategoryNAME);
        progressDialog.show();
        new FireBaseHelper.Objects().Where(FireBaseHelper.Objects.Table.Category, CategoryKEY, Data -> {
            if (Data.size() < 1) {
                progressDialog.dismiss();
                view.findViewById(R.id.emptycategory).setVisibility(View.VISIBLE);
            } else {
                //region noadapter
                   /* RecyclerView.Adapter<viewholder> Adapter = new RecyclerView.Adapter<viewholder>() {
                        @Override
                        public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View itemView = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.fragment_item, parent, false);

                            return new viewholder(itemView);
                        }

                        @Override
                        public void onBindViewHolder(viewholder holder, int position) {
                            FireBaseHelper.Objects date = Data.get(position);
                            holder.mTitleView.setText(date.name);
                            holder.mCompanyView.setText(date.companies.name);
                            holder.mRateView.setText(getRate(date.feedbacks));
                            Picasso.with(getContext()).load(date.image_path).into(holder.mImageView);
                            holder.mView.setOnClickListener(v -> {
                                Intent intent = new Intent(getActivity(), ModelActivity.class);
                                intent.putExtra(OBJECT_KEY, date.Key);
                                startActivity(intent);
                            });
                            holder.mImageButton.setOnClickListener(v -> {
                                PopupMenu popup = new PopupMenu(context, holder.mImageButton);
                                MenuInflater inflater1 = popup.getMenuInflater();
                                inflater1.inflate(R.menu.pop_menu, popup.getMenu());
                                popup.setOnMenuItemClickListener(item -> {
                                    int id = item.getItemId();
                                    if (id == R.id.item_download) {

                                    } else if (id == R.id.item_favorite) {
                                        FireBaseHelper.Favorites favorites = new FireBaseHelper.Favorites();
                                        favorites.user_id = mAuth.getCurrentUser().getUid();
                                        favorites.object_id = date.Key;
                                        favorites.Add(date.Key+mAuth.getCurrentUser().getUid());
                                    } else if (id == R.id.item_delete) {

                                    }
                                    return true;
                                });
                                popup.show();
                            });
                            if (Data.size() - 1 == position) {
                                //showProgress(false);
                            }

                        }

                        @Override
                        public int getItemCount() {
                            return Data.size();
                        }
                    };*/
                //endregion
                mAdapter = new FirebaseRecyclerAdapter<FireBaseHelper.Objects, viewholder>(
                        FireBaseHelper.Objects.class, R.layout.fragment_item, viewholder.class, query) {
                    @Override
                    protected void populateViewHolder(viewholder viewHolder, FireBaseHelper.Objects model, int position) {
                        model.Findbykey(mAdapter.getRef(position).getKey(), Data -> {
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
                                File Dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FurnitureGo" + File.separator + Data.Key);
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
                                        ModelFragment.Download(Data, getContext());
                                    } else if (id == R.id.item_delete) {
                                        ModelFragment.Delete(Data.Key, getContext());
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
                };
                recyclerView.setAdapter(mAdapter);
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
