package com.vitaliyhtc.lcbo.interfaces;

import android.content.Context;

import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;

public interface MainActivityPresenterInterface extends Presenter {
    void clearDbTables(Context context);
    void performStoresSearch(StoresSearchParameters storesSearchParameters);
    void getStoresPage(int offset, boolean isInitialLoading);
    void getSearchStoresPage(int offset);
}
