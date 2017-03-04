package com.vitaliyhtc.lcbo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.data.StoresDataManager;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;
import com.vitaliyhtc.lcbo.model.Store;
import com.vitaliyhtc.lcbo.util.EndlessRecyclerViewScrollListener;
import com.vitaliyhtc.lcbo.adapter.StoresAdapter;
import com.vitaliyhtc.lcbo.util.SetStoresSearchParametersDialog;

import java.util.List;

public class MainActivity extends CoreActivity
        implements StoresDataManager.StoresListLoadedListener, SearchView.OnQueryTextListener {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

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

    private StoresDataManager storesDataManager;

    private EndlessRecyclerViewScrollListener mScrollListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initiateUserInterface();

        storesDataManager = getStoresDataManager();
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
        storesDataManager.onDestroy();
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
        ImageView searchGoBtnImageView = (ImageView) searchView.findViewById(R.id.search_go_btn);
        searchGoBtnImageView.setPadding(0, 0, 0, 0);

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
        mStoresSearchParameters.setSearchStringQuery(query);
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
        //See javadoc.
        return false;
    }

    private void performStoresSearch(StoresSearchParameters storesSearchParameters){
        storesDataManager.performStoresSearch(storesSearchParameters);
    }

    public void onStoresSearchListLoaded(final List<Store> stores, final int offset){
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
            storesDataManager.getStoresPage(1, false);
        }
    }


















    /**
     * When creating {@code new StoresDataManager()} - we need to pass context in it.
     *
     * @return instance of StoresDataManager
     */
    private StoresDataManager getStoresDataManager(){
        StoresDataManager storesDataManager = new StoresDataManager(this);
        storesDataManager.init();
        return storesDataManager;
    }

    /**
     * Execution flow:
     * onCreate()
     * initStoresList() - retrieve first page of Stores.
     * storesDataManager.getStoresPage(1, true) - load page and pass it to callback
     * onInitStoresListLoaded() - callback, call
     * loadStores(initialStoresList) - which initialize adapter and RecyclerView.
     *          Here is EndlessRecyclerViewScrollListener, which fire onLoadMore(),
     *          where we need to load more data for adapter.
     * loadNextDataFromApi() ==> storesDataManager.getStoresPage(offset, false) ==>
     * onStoresListLoaded() - add stores to adapter and notify them.
     */

    @Override
    public void onInitStoresListLoaded(List<Store> stores, int offset){
        loadStores(stores);
    }

    @Override
    public void onStoresListLoaded(final List<Store> stores, final int offset){

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

    private void initStoresList(){
        storesDataManager.getStoresPage(1, true);// >>> onInitStoresListLoaded Callback
    }

    private void loadStores(List<Store> initialStoresList){
        RecyclerView mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);
        //mStoresAdapter = new StoresAdapter(this); initiated at variables block
        mStoresAdapter.appendToStores(initialStoresList);
        mRecycleView.setAdapter(mStoresAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
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

        if(!mIsInSearchState){
            storesDataManager.getStoresPage(offset, false);// >>> onStoresListLoaded Callback
        }else{
            storesDataManager.getSearchStoresPage(offset);// >>> onStoresSearchListLoaded Callback
        }

        //Toast.makeText(getApplicationContext(), "loadNextDataFromApi", Toast.LENGTH_SHORT).show();
    }

    public int getCountOfStoresInAdapter(){
        return mStoresAdapter.getItemCount();
    }

}
