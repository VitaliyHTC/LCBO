package com.vitaliyhtc.lcbo;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.adapter.ProductsAdapter;
import com.vitaliyhtc.lcbo.data.ProductsDataManager;
import com.vitaliyhtc.lcbo.helpers.ProductsSearchParameters;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.util.EndlessRecyclerViewScrollListener;
import com.vitaliyhtc.lcbo.util.ProductDetailsDialog;
import com.vitaliyhtc.lcbo.util.SetProductsSearchParametersDialog;
import com.vitaliyhtc.lcbo.util.ShoppingCartDialog;

import java.util.List;

public class ProductsSearchActivity extends CoreActivity
        implements ProductsDataManager.DataManagerCallbacks,
        ProductsAdapter.ProductItemClickCallbacks,
        SearchView.OnQueryTextListener {

    //params for EndlessRecyclerViewScrollListener
    // The current offset index of data you have loaded
    private static final int INITIAL_CURRENT_PAGE = 1;//0
    // The total number of items in the dataset after the last load
    private static final int INITIAL_PREVIOUS_TOTAL_ITEM_COUNT = 0;//0
    // Sets the starting page index
    private static final int INITIAL_STARTING_PAGE_INDEX = 1;//0

    private boolean mIsInSearchState = false;

    private ProductsSearchParameters mProductsSearchParameters;

    private ProductsAdapter mProductsAdapter = new ProductsAdapter(this);

    private ProductsDataManager mProductsDataManager;

    private EndlessRecyclerViewScrollListener mScrollListener;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products_search_activity);
        initiateUserInterface();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,
                R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        mProductsDataManager = getProductsDataManager();
        initProductsSearchParameters();

        initProductsList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_products_search);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        // Call to release resources
        mProductsDataManager.onDestroy();
    }

    private void initProductsSearchParameters(){
        mProductsSearchParameters = new ProductsSearchParameters();

        SharedPreferences sharedPreferences = this.getSharedPreferences("ProductsSearchParameters_Setting", 0);
        mProductsSearchParameters.setHasValueAddedPromotion(sharedPreferences.getBoolean("hasValueAddedPromotion", false));
        mProductsSearchParameters.setHasLimitedTimeOffer(sharedPreferences.getBoolean("hasLimitedTimeOffer", false));
        mProductsSearchParameters.setHasBonusRewardMiles(sharedPreferences.getBoolean("hasBonusRewardMiles", false));
        mProductsSearchParameters.setSeasonal(sharedPreferences.getBoolean("isSeasonal", false));
        mProductsSearchParameters.setVqa(sharedPreferences.getBoolean("isVqa", false));
        mProductsSearchParameters.setOcb(sharedPreferences.getBoolean("isOcb", false));
        mProductsSearchParameters.setKosher(sharedPreferences.getBoolean("isKosher", false));
    }



    // TODO: android searchview submit empty query !
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        final ImageButton searchOptionsButton = (ImageButton) getLayoutInflater().inflate(R.layout.search_view_options_button, null);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        LinearLayout searchViewSearchPlate = (LinearLayout) searchView.findViewById(R.id.search_plate);
        searchViewSearchPlate.addView(searchOptionsButton);
        searchOptionsButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        searchOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchOptionsButtonClicked(v);
            }
        });

        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onSearchViewCollapsed();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onSearchOptionsButtonClicked(View view){
        FragmentManager fragmentManager = getSupportFragmentManager();
        SetProductsSearchParametersDialog setProductsSearchParametersDialog = new SetProductsSearchParametersDialog();
        setProductsSearchParametersDialog.setProductsSearchParameters(this, mProductsSearchParameters);
        setProductsSearchParametersDialog.show(fragmentManager, "SetProductsSearchParametersDialog");
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO: problem that SearchView don't submit empty query!
        if(".".equals(query)){
            mProductsSearchParameters.setSearchStringQuery("");
        }else{
            mProductsSearchParameters.setSearchStringQuery(query);
        }
        performProductsSearch(mProductsSearchParameters);
        return true;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void performProductsSearch(ProductsSearchParameters productsSearchParameters){
        mProgressBar.setVisibility(View.VISIBLE);
        mProductsDataManager.performProductsSearch(productsSearchParameters);
    }

    @Override
    public void onProductsSearchListLoaded(final List<Product> products, final int offset){
        mProgressBar.setVisibility(View.GONE);
        if(offset==1){
            mProductsAdapter.clearProductsList();
            mIsInSearchState=true;
            mScrollListener.resetState();
        }
        mProductsAdapter.appendToProducts(products);
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                mProductsAdapter.notifyItemRangeInserted((offset-1)*Config.STORES_PER_PAGE, products.size());
            }
        };
        handler.post(r);
    }

    private void onSearchViewCollapsed(){
        if(mIsInSearchState){
            mProgressBar.setVisibility(View.VISIBLE);
            mProductsDataManager.getProductsPage(1, false);
        }
    }



    private ProductsDataManager getProductsDataManager(){
        ProductsDataManager productsDataManager = new ProductsDataManager(this);
        productsDataManager.init();
        return productsDataManager;
    }



    private void initProductsList(){
        mProgressBar.setVisibility(View.VISIBLE);
        mProductsDataManager.getProductsPage(1, true);
    }

    @Override
    public void onInitProductsListLoaded(List<Product> products, int offset){
        mProgressBar.setVisibility(View.GONE);
        loadProducts(products);
    }

    @Override
    public void onProductsListLoaded(final List<Product> products, final int offset){
        mProgressBar.setVisibility(View.GONE);

        if(offset==1 || mIsInSearchState){
            mProductsAdapter.clearProductsList();
            mIsInSearchState=false;
            mScrollListener.resetState();
        }

        mProductsAdapter.appendToProducts(products);

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                mProductsAdapter.notifyItemRangeInserted((offset-1)*Config.STORES_PER_PAGE, products.size());
            }
        };
        handler.post(r);
    }

    private void loadProducts(List<Product> initialProductsList){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //mProductsAdapter = new ProductsAdapter(this); initiated at variables block
        mProductsAdapter.appendToProducts(initialProductsList);
        recyclerView.setAdapter(mProductsAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

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
        recyclerView.addOnScrollListener(mScrollListener);
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
        mProgressBar.setVisibility(View.VISIBLE);
        if(!mIsInSearchState){
            mProductsDataManager.getProductsPage(offset, false);// >>> onProductsListLoaded Callback
        }else{
            mProductsDataManager.getSearchProductsPage(offset);// >>> onProductsSearchListLoaded Callback
        }
    }

    @Override
    public int getCountOfProductsInAdapter(){
        return mProductsAdapter.getItemCount();
    }



    @Override
    public void onProductItemDetailsClicked(int position) {
        FragmentManager manager = getSupportFragmentManager();
        ProductDetailsDialog productDetailsDialog = new ProductDetailsDialog();
        productDetailsDialog.setContextAndProduct(this, mProductsAdapter.getProductAtPosition(position));
        productDetailsDialog.show(manager, "ProductDetailsDialog");
    }

    @Override
    public void onProductItemCartClicked(int position) {
        FragmentManager manager = getSupportFragmentManager();
        ShoppingCartDialog shoppingCartDialog = new ShoppingCartDialog();
        shoppingCartDialog.setContextAndProduct(this, mProductsAdapter.getProductAtPosition(position));
        shoppingCartDialog.show(manager, "ShoppingCartDialog");
    }
}
