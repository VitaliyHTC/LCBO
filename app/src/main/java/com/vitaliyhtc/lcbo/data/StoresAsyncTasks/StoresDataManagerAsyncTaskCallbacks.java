package com.vitaliyhtc.lcbo.data.StoresAsyncTasks;

import com.vitaliyhtc.lcbo.model.Store;

import java.util.List;

public interface StoresDataManagerAsyncTaskCallbacks {

    void loadAndSaveStores(int offset);
    void getStoresPageFromDb(int offset, boolean isInitialLoading);
    void getStoresPageFromNetwork(int offset, boolean isInitialLoading);
    void onStoresPageLoaded(int offset, boolean isInitialLoading, List<Store> stores);
    void onPostGetStoresPageAsyncTask(int storedInDatabaseCounter, int offset, boolean isInitialLoading);

}
