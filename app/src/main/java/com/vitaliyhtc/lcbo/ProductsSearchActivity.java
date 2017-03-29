package com.vitaliyhtc.lcbo;

import android.app.Activity;
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
        ProductsAdapter.ProductItemClickCallbacks {

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
        setContentView(R.layout.activity_products_search);
        initiateUserInterface();

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
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



    // TODO: problem that SearchView don't submit empty query!
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        final ImageButton searchOptionsButton = (ImageButton) getLayoutInflater().inflate(R.layout.search_view_options_button, null);
        final Activity activity = this;

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // problem that SearchView don't submit empty query!
                hideSoftKeyboard(activity);
                if(".".equals(query)){
                    mProductsSearchParameters.setSearchStringQuery("");
                }else{
                    mProductsSearchParameters.setSearchStringQuery(query);
                }
                performProductsSearch(mProductsSearchParameters);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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

    public void loadNextDataFromApi(int offset) {
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
        // TODO: 29/03/17 avoid hardcoded keys/args usage
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
