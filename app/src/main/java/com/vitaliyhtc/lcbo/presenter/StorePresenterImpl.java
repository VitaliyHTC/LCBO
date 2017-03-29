package com.vitaliyhtc.lcbo.presenter;

import com.vitaliyhtc.lcbo.data.DatabaseHelperTableCleaner;
import com.vitaliyhtc.lcbo.data.StoresDataManager;
import com.vitaliyhtc.lcbo.data.StoresDataManagerCallbacks;
import com.vitaliyhtc.lcbo.data.StoresDataManagerImpl;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;
import com.vitaliyhtc.lcbo.interfaces.BaseView;
import com.vitaliyhtc.lcbo.interfaces.StoresView;
import com.vitaliyhtc.lcbo.model.Store;

import java.util.List;

public class StorePresenterImpl
        implements StoresDataManagerCallbacks,
        StorePresenter {

    private StoresView mView;

    private StoresDataManager mStoresDataManager;


    public StorePresenterImpl() {}

    @Override
    public void onAttachView(BaseView baseView) {
        this.mView = (StoresView) baseView;

        mStoresDataManager = getStoresDataManager();
    }

    @Override
    public void onDetachView() {
        mView = null;
        // TODO: Review this place later. Cancel DataManager action and call onDestroy().
        mStoresDataManager.onDestroy();
    }



    public void clearDbTables(){
        new DatabaseHelperTableCleaner().clearDbTables(mView);
    }



    private StoresDataManager getStoresDataManager(){
        return new StoresDataManagerImpl(mView.getContext(), this);
    }



    @Override
    public void performStoresSearch(StoresSearchParameters storesSearchParameters) {
        mView.showLoadingProgress();
        mStoresDataManager.performStoresSearch(storesSearchParameters);
    }

    @Override
    public void getStoresPage(int offset, boolean isInitialLoading) {
        mView.showLoadingProgress();
        mStoresDataManager.getStoresPage(offset, isInitialLoading);
    }

    @Override
    public void getSearchStoresPage(int offset) {
        mView.showLoadingProgress();
        mStoresDataManager.getSearchStoresPage(offset);
    }



    @Override
    public void onInitStoresListLoaded(List<Store> stores, int offset) {
        if(mView!=null) {
            mView.hideLoadingProgress();
            mView.onInitStoresListLoaded(stores, offset);
        }
    }

    @Override
    public void onStoresListLoaded(List<Store> stores, int offset) {
        if(mView!=null) {
            mView.hideLoadingProgress();
            mView.onStoresListLoaded(stores, offset);
        }
    }

    @Override
    public void onStoresSearchListLoaded(List<Store> stores, int offset) {
        if(mView!=null) {
            mView.hideLoadingProgress();
            mView.onStoresSearchListLoaded(stores, offset);
        }
    }

    @Override
    public int getCountOfStoresInAdapter() {
        if(mView!=null){
            return mView.getCountOfStoresInAdapter();
        }
        return 0;
    }
}
