package com.vitaliyhtc.lcbo.interfaces;

import com.vitaliyhtc.lcbo.model.Store;

import java.util.List;

public interface StoresView extends BaseView{

    void onStoresSearchListLoaded(List<Store> stores, int offset);
    void onInitStoresListLoaded(List<Store> stores, int offset);
    void onStoresListLoaded(List<Store> stores, int offset);
    int getCountOfStoresInAdapter();

    void showLoadingProgress();
    void hideLoadingProgress();

}
