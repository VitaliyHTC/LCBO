package com.vitaliyhtc.lcbo;

import android.app.Activity;
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

import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;
import com.vitaliyhtc.lcbo.presenter.StorePresenter;
import com.vitaliyhtc.lcbo.interfaces.StoresView;
import com.vitaliyhtc.lcbo.model.Store;
import com.vitaliyhtc.lcbo.presenter.StorePresenterImpl;
import com.vitaliyhtc.lcbo.util.EndlessRecyclerViewScrollListener;
import com.vitaliyhtc.lcbo.adapter.StoresAdapter;
import com.vitaliyhtc.lcbo.util.SetStoresSearchParametersDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends CoreActivity
        implements StoresAdapter.StoreItemClickCallbacks,
        StoresView {

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

    private StorePresenter mStorePresenter;

    private EndlessRecyclerViewScrollListener mScrollListener;

    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initiateUserInterface();
        this.setTitle(R.string.activity_title_main_stores);

        //mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        ButterKnife.bind(this);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,
                R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        mStorePresenter = new StorePresenterImpl();
        mStorePresenter.onAttachView(this);
        clearDbTables();

        initStoresList();

        initStoresSearchParameters();
    }

    @Override
    protected void onStart() {
        super.onStart();

        setNavigationViewCheckedItem(R.id.nav_stores);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mStorePresenter.onDetachView();
    }

    private void clearDbTables(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("LCBO_DB_Clear_Time_Setting", 0);
        long lastClearTimeInMillis = sharedPreferences.getLong("dbLastClearTimeInMillis", 0);
        long currentTimeInMillis = System.currentTimeMillis();
        long delta = 24*60*60*1000;
        if(currentTimeInMillis - lastClearTimeInMillis >= delta){

            mStorePresenter.clearDbTables();

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



    // !!! problem that SearchView don't submit empty query!
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
                    mStoresSearchParameters.setSearchStringQuery("");
                }else{
                    mStoresSearchParameters.setSearchStringQuery(query);
                }
                performStoresSearch(mStoresSearchParameters);
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
        SetStoresSearchParametersDialog setStoresSearchParametersDialog = new SetStoresSearchParametersDialog();
        setStoresSearchParametersDialog.setStoresSearchParameters(this, mStoresSearchParameters);
        setStoresSearchParametersDialog.show(fragmentManager, "SetStoresSearchParametersDialog");
    }

    private void performStoresSearch(StoresSearchParameters storesSearchParameters){
        mStorePresenter.performStoresSearch(storesSearchParameters);
    }

    @Override
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
            mStorePresenter.getStoresPage(1, false);
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
        mStorePresenter.getStoresPage(1, true);// >>> onInitStoresListLoaded Callback
    }

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

    private void loadStores(List<Store> initialStoresList){
        //RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view); initiated by ButterKnife
        //mStoresAdapter = new StoresAdapter(this); initiated at variables block
        mStoresAdapter.appendToStores(initialStoresList);
        mRecyclerView.setAdapter(mStoresAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        // Retain an instance so that you can call `resetState()` for fresh searches
        mScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };
        mScrollListener.setCounters(INITIAL_CURRENT_PAGE, INITIAL_PREVIOUS_TOTAL_ITEM_COUNT, INITIAL_STARTING_PAGE_INDEX);

        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    public void loadNextDataFromApi(int offset) {
        if(!mIsInSearchState){
            mStorePresenter.getStoresPage(offset, false);// >>> onStoresListLoaded Callback
        }else{
            mStorePresenter.getSearchStoresPage(offset);// >>> onStoresSearchListLoaded Callback
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
    public void showLoadingProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStoreItemClick(int positionInAdapter){
        Store store = mStoresAdapter.getStoreAtPosition(positionInAdapter);
        int storeId = store.getId();

        startStoreDetailActivity(storeId);
    }

    private void startStoreDetailActivity(int storeId) {
        Intent intent = new Intent(this, StoreDetailActivity.class);
        intent.putExtra(StoreDetailActivity.KEY_TARGET_STORE_ID, storeId);
        intent.putExtra(StoreDetailActivity.KEY_ACTIVITY_FIRST, StoreDetailActivity.ACTIVITY_MAIN);
        startActivity(intent);
    }

}
