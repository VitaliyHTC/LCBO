package com.vitaliyhtc.lcbo.presenter;

import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;

public interface StorePresenter extends BasePresenter {

    void clearDbTables();
    void performStoresSearch(StoresSearchParameters storesSearchParameters);
    void getStoresPage(int offset, boolean isInitialLoading);
    void getSearchStoresPage(int offset);
}
