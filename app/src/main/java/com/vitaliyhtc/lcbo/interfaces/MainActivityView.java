package com.vitaliyhtc.lcbo.interfaces;

import android.content.Context;

import com.vitaliyhtc.lcbo.model.Store;

import java.util.List;

public interface MainActivityView {
    void onStoresSearchListLoaded(List<Store> stores, int offset);
    void onInitStoresListLoaded(List<Store> stores, int offset);
    void onStoresListLoaded(List<Store> stores, int offset);
    int getCountOfStoresInAdapter();
    Context getContext();
}
