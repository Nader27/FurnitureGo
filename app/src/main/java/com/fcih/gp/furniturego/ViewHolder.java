package com.fcih.gp.furniturego;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mTitleView;
    public final TextView mCompanyView;
    public final TextView mRateView;
    public final ImageView mImageView;
    public final ImageButton mImageButton;
    public final PopupMenu mPopupMenu;
    private final Context mContext;

    public ViewHolder(View view) {
        super(view);
        mView = view;
        mTitleView = (TextView) view.findViewById(R.id.item_title);
        mCompanyView = (TextView) view.findViewById(R.id.item_company);
        mRateView = (TextView) view.findViewById(R.id.item_rate);
        mImageView = (ImageView) view.findViewById(R.id.item_image);
        mImageButton = (ImageButton) view.findViewById(R.id.item_menu);
        mContext = mView.getContext();
        mPopupMenu = new PopupMenu(mContext, mImageButton);
        MenuInflater inflater = mPopupMenu.getMenuInflater();
        inflater.inflate(R.menu.pop_menu, mPopupMenu.getMenu());
        mImageButton.setOnClickListener(v -> {
            mPopupMenu.show();
        });
    }

    private String getRate(List<FireBaseHelper.Feedbacks> lst) {

        if (lst.size() == 0) {
            return "0.0★";
        } else {
            int sum = 0;

            for (FireBaseHelper.Feedbacks item : lst) {
                sum += Integer.parseInt(item.rate);
            }
            return String.format(Locale.ENGLISH, "%.1f★", (double) sum / lst.size());
        }
    }

    public void Initialize(FireBaseHelper.Objects Data) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mTitleView.setText(Data.name);
        mCompanyView.setText(Data.companies.name);
        mRateView.setText(getRate(Data.feedbacks));
        Picasso.with(mContext).load(Data.image_path).into(mImageView);
        new FireBaseHelper.Favorites().Findbykey(Data.Key + mAuth.getCurrentUser().getUid(), Data1 -> {
            if (Data1 != null) {
                mPopupMenu.getMenu().findItem(R.id.item_Whishlist).setVisible(false);
                mPopupMenu.getMenu().findItem(R.id.item_remove_Whishlist).setVisible(true);
            } else {
                mPopupMenu.getMenu().findItem(R.id.item_Whishlist).setVisible(true);
                mPopupMenu.getMenu().findItem(R.id.item_remove_Whishlist).setVisible(false);
            }
        });
        File Dir = new File(Environment.getExternalStorageDirectory(), File.separator + "FurnitureGo" + File.separator + Data.Key);
        if (Dir.exists()) {
            mPopupMenu.getMenu().findItem(R.id.item_download).setVisible(false);
            mPopupMenu.getMenu().findItem(R.id.item_delete).setVisible(true);
        } else {
            mPopupMenu.getMenu().findItem(R.id.item_download).setVisible(true);
            mPopupMenu.getMenu().findItem(R.id.item_delete).setVisible(false);
        }
        mPopupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.item_download) {
                ModelFragment.Download(Data, mContext);
            } else if (id == R.id.item_delete) {
                ModelFragment.Delete(Data.Key, mContext);
            } else if (id == R.id.item_Whishlist) {
                FireBaseHelper.Favorites favorites = new FireBaseHelper.Favorites();
                favorites.user_id = mAuth.getCurrentUser().getUid();
                favorites.object_id = Data.Key;
                favorites.Add(Data.Key + mAuth.getCurrentUser().getUid());
                mPopupMenu.getMenu().findItem(R.id.item_Whishlist).setVisible(false);
                mPopupMenu.getMenu().findItem(R.id.item_remove_Whishlist).setVisible(true);
            } else if (id == R.id.item_remove_Whishlist) {
                new FireBaseHelper.Favorites().Remove(Data.Key + mAuth.getCurrentUser().getUid());
                mPopupMenu.getMenu().findItem(R.id.item_Whishlist).setVisible(true);
                mPopupMenu.getMenu().findItem(R.id.item_remove_Whishlist).setVisible(false);
            }
            return true;
        });
    }

}
