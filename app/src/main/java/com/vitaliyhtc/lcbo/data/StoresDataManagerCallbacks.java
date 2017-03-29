package com.vitaliyhtc.lcbo.data;

import com.vitaliyhtc.lcbo.model.Store;

import java.util.List;

public interface StoresDataManagerCallbacks {

    void onInitStoresListLoaded(List<Store> stores, int offset);
    void onStoresListLoaded(List<Store> stores, int offset);
    void onStoresSearchListLoaded(List<Store> stores, int offset);
    int getCountOfStoresInAdapter();
}
