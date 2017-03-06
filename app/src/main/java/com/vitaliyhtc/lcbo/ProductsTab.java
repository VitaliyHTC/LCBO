package com.vitaliyhtc.lcbo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vitaliyhtc.lcbo.adapter.ProductsByCategoriesAdapter;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.util.EndlessRecyclerViewScrollListener;

import java.util.List;

public class ProductsTab extends Fragment {
    public static final String LOG_TAG = ProductsTab.class.getSimpleName();

    public static final int TAB_CATEGORY_BEER = 730;
    public static final int TAB_CATEGORY_WINE = 731;
    public static final int TAB_CATEGORY_SPIRITS = 732;

    //params for EndlessRecyclerViewScrollListener
    // The current offset index of data you have loaded
    private static final int INITIAL_CURRENT_PAGE = 1;//0
    // The total number of items in the dataset after the last load
    private static final int INITIAL_PREVIOUS_TOTAL_ITEM_COUNT = 0;//0
    // Sets the starting page index
    private static final int INITIAL_STARTING_PAGE_INDEX = 1;//0

    private int mCategory;

    private ProductsByCategoriesActivity parentActivity;

    private ProductsByCategoriesAdapter mProductsAdapter = new ProductsByCategoriesAdapter(this);

    private EndlessRecyclerViewScrollListener mScrollListener;

    private boolean initialState = true;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.products_tab_recycler_view, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(parentActivity==null){
            parentActivity = (ProductsByCategoriesActivity) getActivity();
            parentActivity.setProductsTab(this);
            initProductsList();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    protected void setCategory(int category){
        mCategory = category;
    }



    private void initProductsList(){
        initialState = true;
        // parentActivity.getNItemsOf(Config.PRODUCTS_PER_PAGE, mCategory);
        // ProductsByCategoriesActivity - automatically perform initial callbacks.
    }

    public void onProductsListLoaded(final List<Product> products){
        if(initialState){
            initialState = false;
            loadProducts(products);
        }else{
            final int oldSize = mProductsAdapter.getItemCount();

            mProductsAdapter.appendToProducts(products);

            Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    mProductsAdapter.notifyItemRangeInserted(oldSize, products.size());
                }
            };
            handler.post(r);
        }
    }

    private void loadProducts(List<Product> initialProductsList){
        RecyclerView mRecycleView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        //mStoresAdapter = new StoresAdapter(this); initiated at variables block
        mProductsAdapter.appendToProducts(initialProductsList);
        mRecycleView.setAdapter(mProductsAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecycleView.setLayoutManager(linearLayoutManager);

        // Retain an instance so that you can call `resetState()` for fresh searches
        mScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        mScrollListener.setCounters(INITIAL_CURRENT_PAGE, INITIAL_PREVIOUS_TOTAL_ITEM_COUNT, INITIAL_STARTING_PAGE_INDEX);
        // Adds the scroll listener to RecyclerView
        mRecycleView.addOnScrollListener(mScrollListener);
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
        parentActivity.getNItemsOf(Config.PRODUCTS_PER_PAGE, mCategory);// >>> onProductsListLoaded Callback
    }











    public int getCategory() {
        return mCategory;
    }

    public String getCategoryString(){
        String category="";
        if(mCategory== TAB_CATEGORY_BEER){
            category="Beer";
        }
        if(mCategory== TAB_CATEGORY_WINE){
            category="Wine";
        }
        if(mCategory== TAB_CATEGORY_SPIRITS){
            category="Spirits";
        }
        return category;
    }


    public interface RequestForProductsByCategory{
        void getNItemsOf(int numberOfItems, int category);
    }
}
