package com.vitaliyhtc.lcbo.data;

import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;

public interface StoresDataManager {

    void onDestroy();
    void performStoresSearch(StoresSearchParameters storesSearchParameters);
    void getSearchStoresPage(int offset);
    void getStoresPage(int offset, boolean isInitialLoading);

}
