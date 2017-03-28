package com.vitaliyhtc.lcbo;

import android.content.Context;
import android.content.Intent;
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

import com.squareup.leakcanary.LeakCanary;
import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;
import com.vitaliyhtc.lcbo.interfaces.MainActivityPresenterInterface;
import com.vitaliyhtc.lcbo.interfaces.MainActivityView;
import com.vitaliyhtc.lcbo.model.Store;
import com.vitaliyhtc.lcbo.presenter.MainActivityPresenter;
import com.vitaliyhtc.lcbo.util.EndlessRecyclerViewScrollListener;
import com.vitaliyhtc.lcbo.adapter.StoresAdapter;
import com.vitaliyhtc.lcbo.util.SetStoresSearchParametersDialog;

import java.util.List;

/**
 * TODO: Rewrite to MVP.
 */
public class MainActivity extends CoreActivity
        implements SearchView.OnQueryTextListener,
        StoresAdapter.StoreItemClickCallbacks,
        MainActivityView {

    //params for EndlessRecyclerViewScrollListener
    // The current offset index of data you have loaded
    private static final int INITIAL_CURRENT_PAGE = 1;//0
    // The total number of items in the dataset after the last load
    private static final int INITIAL_PREVIOUS_TOTAL_ITEM_COUNT = 0;//0
    // Sets the starting page index
    private static final int INITIAL_STARTING_PAGE_INDEX = 1;//0

    private boolean mIsInSearchState = false;

    private StoresSearchParameters mStoresSearchParameters;

    private StoresAdapter mStoresAdapter = new StoresAdapter(this);

    private MainActivityPresenterInterface mMainActivityPresenter = new MainActivityPresenter(this);

    private EndlessRecyclerViewScrollListener mScrollListener;

    private ProgressBar mProgressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: 28/03/17 move leakCanary initialization to your application class
        if (LeakCanary.isInAnalyzerProcess(this.getApplication())) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this.getApplication());
        // Normal app init code...

        setContentView(R.layout.main_activity);
        initiateUserInterface();
        this.setTitle(R.string.main_activity_stores_title);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,
                R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        clearDbTables();

        // moved to presenter
        mMainActivityPresenter.onCreate();

        initStoresSearchParameters();

        initStoresList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_stores);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        // Call to release resources
        mMainActivityPresenter.onDestroy();
    }

    private void clearDbTables(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("LCBO_DB_Clear_Time_Setting", 0);
        long lastClearTimeInMillis = sharedPreferences.getLong("dbLastClearTimeInMillis", 0);
        long currentTimeInMillis = System.currentTimeMillis();
        long delta = 24*60*60*1000;
        if(currentTimeInMillis - lastClearTimeInMillis >= delta){

            mMainActivityPresenter.clearDbTables(this);

            lastClearTimeInMillis = System.currentTimeMillis();
        }

        SharedPreferences.Editor editor = this.getSharedPreferences("LCBO_DB_Clear_Time_Setting", 0).edit();
        editor.putLong("dbLastClearTimeInMillis", lastClearTimeInMillis);
        editor.commit();
    }

    private void initStoresSearchParameters(){
        mStoresSearchParameters = new StoresSearchParameters();

        SharedPreferences sharedPreferences = this.getSharedPreferences("StoresSearchParameters_Setting", 0);
        mStoresSearchParameters.setHasWheelchairAccessability(sharedPreferences.getBoolean("hasWheelchairAccessability", false));
        mStoresSearchParameters.setHasBilingualServices(sharedPreferences.getBoolean("hasBilingualServices", false));
        mStoresSearchParameters.setHasProductConsultant(sharedPreferences.getBoolean("hasProductConsultant", false));
        mStoresSearchParameters.setHasTastingBar(sharedPreferences.getBoolean("hasTastingBar", false));
        mStoresSearchParameters.setHasBeerColdRoom(sharedPreferences.getBoolean("hasBeerColdRoom", false));
        mStoresSearchParameters.setHasSpecialOccasionPermits(sharedPreferences.getBoolean("hasSpecialOccasionPermits", false));
        mStoresSearchParameters.setHasVintagesCorner(sharedPreferences.getBoolean("hasVintagesCorner", false));
        mStoresSearchParameters.setHasParking(sharedPreferences.getBoolean("hasParking", false));
        mStoresSearchParameters.setHasTransitAccess(sharedPreferences.getBoolean("hasTransitAccess", false));
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
        SetStoresSearchParametersDialog setStoresSearchParametersDialog = new SetStoresSearchParametersDialog();
        setStoresSearchParametersDialog.setStoresSearchParameters(this, mStoresSearchParameters);
        setStoresSearchParametersDialog.show(fragmentManager, "SetStoresSearchParametersDialog");
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
        hideSoftKeyboard(this);
        if(".".equals(query)){
            mStoresSearchParameters.setSearchStringQuery("");
        }else{
            mStoresSearchParameters.setSearchStringQuery(query);
        }
        performStoresSearch(mStoresSearchParameters);
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

    private void performStoresSearch(StoresSearchParameters storesSearchParameters){
        mProgressBar.setVisibility(View.VISIBLE);
        mMainActivityPresenter.performStoresSearch(storesSearchParameters);
    }

    @Override
    public void onStoresSearchListLoaded(final List<Store> stores, final int offset){
        mProgressBar.setVisibility(View.GONE);
        if(offset==1){
            mStoresAdapter.clearStoresList();
            mIsInSearchState=true;
            mScrollListener.resetState();
        }
        mStoresAdapter.appendToStores(stores);
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                mStoresAdapter.notifyItemRangeInserted((offset-1)*Config.STORES_PER_PAGE, stores.size());
            }
        };
        handler.post(r);
    }

    private void onSearchViewCollapsed(){
        if(mIsInSearchState){
            mProgressBar.setVisibility(View.VISIBLE);
            mMainActivityPresenter.getStoresPage(1, false);
        }
    }



    /**
     * Execution flow:
     * onCreate()
     * initStoresList() - retrieve first page of Stores.
     * mStoresDataManager.getStoresPage(1, true) - load page and pass it to callback
     * onInitStoresListLoaded() - callback, call
     * loadStores(initialStoresList) - which initialize adapter and RecyclerView.
     *          Here is EndlessRecyclerViewScrollListener, which fire onLoadMore(),
     *          where we need to load more data for adapter.
     * loadNextDataFromApi() ==> mStoresDataManager.getStoresPage(offset, false) ==>
     * onStoresListLoaded() - add stores to adapter and notify them.
     */
    private void initStoresList(){
        mProgressBar.setVisibility(View.VISIBLE);
        mMainActivityPresenter.getStoresPage(1, true);// >>> onInitStoresListLoaded Callback
    }

    @Override
    public void onInitStoresListLoaded(List<Store> stores, int offset){
        mProgressBar.setVisibility(View.GONE);
        loadStores(stores);
    }

    @Override
    public void onStoresListLoaded(final List<Store> stores, final int offset){
        mProgressBar.setVisibility(View.GONE);

        if(offset==1 || mIsInSearchState){
            mStoresAdapter.clearStoresList();
            mIsInSearchState=false;
            mScrollListener.resetState();
        }

        mStoresAdapter.appendToStores(stores);

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                mStoresAdapter.notifyItemRangeInserted((offset-1)*Config.STORES_PER_PAGE, stores.size());
            }
        };
        handler.post(r);
    }

    private void loadStores(List<Store> initialStoresList){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //mStoresAdapter = new StoresAdapter(this); initiated at variables block
        mStoresAdapter.appendToStores(initialStoresList);
        recyclerView.setAdapter(mStoresAdapter);

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
            mMainActivityPresenter.getStoresPage(offset, false);// >>> onStoresListLoaded Callback
        }else{
            mMainActivityPresenter.getSearchStoresPage(offset);// >>> onStoresSearchListLoaded Callback
        }
    }

    @Override
    public int getCountOfStoresInAdapter(){
        return mStoresAdapter.getItemCount();
    }

    @Override
    public Context getContext() {
        return this.getApplicationContext();
    }

    @Override
    public void onStoreItemClick(int positionInAdapter){
        Store store = mStoresAdapter.getStoreAtPosition(positionInAdapter);
        int storeId = store.getId();

        startStoreDetailActivity(storeId);
    }

    private void startStoreDetailActivity(int storeId) {
        Intent intent = new Intent(this, StoreDetailActivity.class);
        // TODO: 28/03/17 avoid hardcoded keys/args usage
        intent.putExtra("targetStoreId", storeId);
        intent.putExtra("activityFirst", StoreDetailActivity.ACTIVITY_MAIN);
        startActivity(intent);
    }

}
