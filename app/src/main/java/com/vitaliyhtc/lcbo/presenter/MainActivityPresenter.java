package com.vitaliyhtc.lcbo.presenter;

// TODO: 28/03/17 try to avoid android SDK usage in presenter, this let you to write test's for business logic
import android.content.Context;
import android.os.AsyncTask;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.data.StoresDataManager;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;
import com.vitaliyhtc.lcbo.interfaces.MainActivityPresenterInterface;
import com.vitaliyhtc.lcbo.interfaces.MainActivityView;
import com.vitaliyhtc.lcbo.model.Store;

import java.util.List;

// TODO: 28/03/17 use meaningful names (StorePresenter, StorePresenterImpl, etc)
public class MainActivityPresenter
        implements StoresDataManager.DataManagerCallbacks,
        MainActivityPresenterInterface {

    private MainActivityView view;

    private StoresDataManager mStoresDataManager;


    public MainActivityPresenter(MainActivityView view) {
        this.view = view;
    }

    @Override
    public void onCreate() {
        mStoresDataManager = getStoresDataManager();
    }

    // TODO: 28/03/17 you don't need all lifecycle methods in presenter, use only needed
    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        mStoresDataManager.onDestroy();
    }



    public void clearDbTables(final Context context){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

                databaseHelper.clearStoresTable();
                databaseHelper.clearProductsTable();

                OpenHelperManager.releaseHelper();
            }
        });
    }



    /**
     * When creating {@code new StoresDataManager()} - we need to pass context in it.
     *
     * @return instance of StoresDataManager
     */
    private StoresDataManager getStoresDataManager(){
        StoresDataManager storesDataManager = new StoresDataManager(this, view.getContext());
        storesDataManager.init();
        return storesDataManager;
    }



    @Override
    public void performStoresSearch(StoresSearchParameters storesSearchParameters) {
        mStoresDataManager.performStoresSearch(storesSearchParameters);
    }

    @Override
    public void getStoresPage(int offset, boolean isInitialLoading) {
        mStoresDataManager.getStoresPage(offset, isInitialLoading);
    }

    @Override
    public void getSearchStoresPage(int offset) {
        mStoresDataManager.getSearchStoresPage(offset);
    }



    @Override
    public void onInitStoresListLoaded(List<Store> stores, int offset) {
        view.onInitStoresListLoaded(stores, offset);
    }

    @Override
    public void onStoresListLoaded(List<Store> stores, int offset) {
        view.onStoresListLoaded(stores, offset);
    }

    @Override
    public void onStoresSearchListLoaded(List<Store> stores, int offset) {
        view.onStoresSearchListLoaded(stores, offset);
    }

    @Override
    public int getCountOfStoresInAdapter() {
        return view.getCountOfStoresInAdapter();
    }
}
