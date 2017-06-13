package com.fcih.gp.furniturego;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ProgressBar mProgressView;
    private ViewGroup mContainer;
    private List<FireBaseHelper.Categories> Categories = new ArrayList<>();

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public void showProgress(final boolean show) {
        mContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().findViewById(R.id.tabs).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        mProgressView = (ProgressBar) getActivity().findViewById(R.id.progress);
        mContainer = container;
        showProgress(true);
        new FireBaseHelper.Categories().Tolist(Data -> {
            Categories = Data;
            ViewPager mViewPager = (ViewPager) view.findViewById(R.id.container);
            mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());
            // Set up the ViewPager with the sections adapter.

            mViewPager.setAdapter(mSectionsPagerAdapter);

            TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(mViewPager);
            showProgress(false);
        });
        return view;
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return ItemFragment.newInstance(Categories.get(position).Key);
        }

        @Override
        public int getCount() {
            return Categories.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            for (int i = 0; i < getCount(); i++) {
                if (position == i) {
                    return Categories.get(i).name;
                }
            }
            return null;
        }
    }
}
