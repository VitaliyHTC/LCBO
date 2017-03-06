package com.vitaliyhtc.lcbo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProductsTabFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static int int_items = 3 ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
        View view =  inflater.inflate(R.layout.products_by_categories_content, null);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(int_items-1);

        /**
         *Set an Apater for the View Pager
         */
        ProductsTabAdapter productsTabAdapter = new ProductsTabAdapter(getChildFragmentManager());
        viewPager.setAdapter(productsTabAdapter);
        /**
         * Now , this is a workaround ,
         * The setupWithViewPager dose't works without the runnable .
         * Maybe a Support Library Bug .
         */
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        return view;
    }


    class ProductsTabAdapter extends FragmentPagerAdapter {
        private ProductsTab beerTab;
        private ProductsTab wineTab;
        private ProductsTab spiritsTab;

        public ProductsTabAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0 : return new ProductsTabBeer();
                case 1 : return new ProductsTabWine();
                case 2 : return new ProductsTabSpirits();
            }
            return null;
        }

        @Override
        public int getCount() {
            return int_items;
        }

        /**
         * This method returns the title of the tab according to the position.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0 :
                    return getString(R.string.products_tab_beer);
                case 1 :
                    return getString(R.string.products_tab_wine);
                case 2 :
                    return getString(R.string.products_tab_spirits);
            }
            return null;
        }
    }

}