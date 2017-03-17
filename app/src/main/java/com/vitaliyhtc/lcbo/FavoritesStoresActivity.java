package com.vitaliyhtc.lcbo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.adapter.FavoriteStoresAdapter;
import com.vitaliyhtc.lcbo.data.FavoriteStoreDataManager;
import com.vitaliyhtc.lcbo.model.FavoriteStore;
import com.vitaliyhtc.lcbo.model.Store;

import java.util.ArrayList;
import java.util.List;

public class FavoritesStoresActivity extends CoreActivity
        implements FavoriteStoresAdapter.StoreItemClickCallbacks,
        FavoriteStoreDataManager.DataManagerCallbacks {
    public static final String LOG_TAG = ShoppingCartActivity.class.getSimpleName();

    private FavoriteStoresAdapter mFavoritesStoresAdapter = new FavoriteStoresAdapter(this);

    private FavoriteStoreDataManager mFavoriteStoreDataManager = new FavoriteStoreDataManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_stores_activity);
        initiateUserInterface();
        initStoresList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_favorites_stores);
    }

    @Override
    protected void onResume(){
        super.onResume();
        revalidateFavorites();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mFavoriteStoreDataManager.onDestroy();
    }

    private void initStoresList(){
        List<FavoriteStore> favoriteStores = mFavoriteStoreDataManager.getAllFavoriteStoresFromDb();
        List<Integer> idsList = new ArrayList<>();
        for(FavoriteStore favoriteStore : favoriteStores){
            idsList.add(favoriteStore.getId());
        }
        mFavoriteStoreDataManager.LoadStoresByIds(idsList);
    }

    @Override
    public void onStoresListLoaded(List<Store> stores) {
        loadStores(stores);
    }

    private void loadStores(List<Store> initialStoresList){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mFavoritesStoresAdapter.appendToStores(initialStoresList);
        recyclerView.setAdapter(mFavoritesStoresAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }



    @Override
    public void onStoreItemDetailsClicked(int position){
        Store store = mFavoritesStoresAdapter.getStoreAtPosition(position);
        int storeId = store.getId();

        Intent intent = new Intent(this, StoreDetailActivity.class);
        intent.putExtra("targetStoreId", storeId);
        intent.putExtra("activityFirst", StoreDetailActivity.ACTIVITY_FAVORITES);
        startActivity(intent);
    }

    @Override
    public void onStoreItemRemoveClicked(int position){
        final int storeId = mFavoritesStoresAdapter.getStoreAtPosition(position).getId();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mFavoriteStoreDataManager.removeFavoriteStoreById(storeId);
            }
        });
        mFavoritesStoresAdapter.removeAt(position);
    }

    private void revalidateFavorites(){
        AsyncTask<Void, Void, Integer> revalidateFavoritesAsyncTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int storeToRemoveFromList = -1;
                Store store;
                for (int i = 0; i < mFavoritesStoresAdapter.getItemCount(); i++) {
                    store = mFavoritesStoresAdapter.getStoreAtPosition(i);
                    if(!mFavoriteStoreDataManager.isStoreFavoriteById(store.getId())){
                        storeToRemoveFromList = i;
                    }
                }
                return storeToRemoveFromList;
            }
            @Override
            protected void onPostExecute(Integer storeToRemoveFromList) {
                if(storeToRemoveFromList != -1){
                    mFavoritesStoresAdapter.removeAt(storeToRemoveFromList);
                }
            }
        };
        revalidateFavoritesAsyncTask.execute();
    }
}
