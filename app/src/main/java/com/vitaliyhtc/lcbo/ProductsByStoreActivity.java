package com.vitaliyhtc.lcbo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.vitaliyhtc.lcbo.adapter.ProductsByStoreAdapter;
import com.vitaliyhtc.lcbo.data.ProductsByStoreDataManager;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.util.EndlessRecyclerViewScrollListener;
import com.vitaliyhtc.lcbo.util.ProductDetailsDialog;

import java.util.List;

public class ProductsByStoreActivity extends AppCompatActivity
        implements ProductsByStoreDataManager.DataManagerCallbacks,
        ProductsByStoreAdapter.ProductItemClickCallbacks {
    private static final String LOG_TAG = ProductsByStoreActivity.class.getSimpleName();

    //params for EndlessRecyclerViewScrollListener
    // The current offset index of data you have loaded
    private static final int INITIAL_CURRENT_PAGE = 1;//0
    // The total number of items in the dataset after the last load
    private static final int INITIAL_PREVIOUS_TOTAL_ITEM_COUNT = 0;//0
    // Sets the starting page index
    private static final int INITIAL_STARTING_PAGE_INDEX = 1;//0

    private ProductsByStoreAdapter mProductsAdapter = new ProductsByStoreAdapter(this);

    private ProductsByStoreDataManager productsDataManager;

    private EndlessRecyclerViewScrollListener mScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.products_by_store_activity);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int targetStoreId = getIntent().getExtras().getInt("targetStoreId");
        String storeName = getIntent().getExtras().getString("targetStoreName");
        this.setTitle(storeName+" products");

        productsDataManager = getProductsDataManager(targetStoreId);
        initProductsList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release DatabaseHelper by calling DataManager onDestroy.
        productsDataManager.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private ProductsByStoreDataManager getProductsDataManager(int targetStoreId){
        ProductsByStoreDataManager dataManager = new ProductsByStoreDataManager(this, targetStoreId);
        dataManager.init();
        return dataManager;
    }



    private void initProductsList(){
        productsDataManager.getProductsPage(1, true);
    }

    @Override
    public void onInitProductsListLoaded(List<Product> products, int offset){
        loadProducts(products);
    }

    @Override
    public void onProductsListLoaded(final List<Product> products, final int offset){
        mProductsAdapter.appendToProducts(products);

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                mProductsAdapter.notifyItemRangeInserted((offset-1)*Config.PRODUCTS_PER_PAGE, products.size());
            }
        };
        handler.post(r);
    }

    private void loadProducts(List<Product> initialProductsList){
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //mProductsAdapter = new ProductsByStoreAdapter(this); initiated at variables block
        mProductsAdapter.appendToProducts(initialProductsList);
        mRecyclerView.setAdapter(mProductsAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

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
        mRecyclerView.addOnScrollListener(mScrollListener);

    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        productsDataManager.getProductsPage(offset, false);
    }

    @Override
    public int getCountOfProductsInAdapter(){
        return mProductsAdapter.getItemCount();
    }








    @Override
    public void onProductItemDetailsClicked(int position) {
        FragmentManager manager = this.getSupportFragmentManager();
        ProductDetailsDialog productDetailsDialog = new ProductDetailsDialog();
        productDetailsDialog.setContextAndProduct(this, mProductsAdapter.getProductAtPosition(position));
        productDetailsDialog.show(manager, "ProductDetailsDialog");
    }

    @Override
    public void onProductItemCartClicked(int position) {

    }
}
