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

    // TODO: 28/03/17 view don't know when need to show/hide loading progress bar,
    //    maybe presenter has some data cached  and even don't need to show loading progress

//    inside of presenter, when you start data loading from model, view need to showLoadingProgress(),
//    when data loaded, hideLoadingProgress and display loaded data,

//    void showLoadingProgress();
//    void hideLoadingProgress();
}
