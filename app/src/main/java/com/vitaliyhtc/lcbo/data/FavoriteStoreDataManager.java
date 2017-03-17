package com.vitaliyhtc.lcbo.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.model.FavoriteStore;
import com.vitaliyhtc.lcbo.model.Store;
import com.vitaliyhtc.lcbo.model.StoreResult;
import com.vitaliyhtc.lcbo.rest.ApiInterface;
import com.vitaliyhtc.lcbo.rest.RetrofitApiClient;
import com.vitaliyhtc.lcbo.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteStoreDataManager {

    private static final String LOG_TAG = FavoriteStoreDataManager.class.getSimpleName();

    private Context mContext;

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    private List<Store> mStores = new ArrayList<>();
    private int mStoresNumberToLoad;
    private int mStoresNumberLoaded;
    private int mTryCounter;



    public FavoriteStoreDataManager(Context context) {
        this.mContext = context;
    }

    /**
     * You'll need this in your class to release the helper when done.
     */
    public void onDestroy(){
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }
    }

    /**
     * You'll need this in your class to get the helper from the manager once per class.
     */
    private DatabaseHelper getDatabaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }



    /**
     * Next 4 methods need to be wrapped in AsyncTask.
     */
    /**
     * Need to be wrapped in AsyncTask.
     *
     * @param storeId   Store ID that we looking for in favorites
     * @return          true - if store is favorite, false - otherwise
     */
    public boolean isStoreFavoriteById(int storeId){
        try {
            Dao<FavoriteStore, Integer> favoriteStoreDao = getDatabaseHelper().getFavoriteStoresDao();
            return (favoriteStoreDao.queryBuilder().where().eq("id", storeId).countOf() == 1);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Need to be wrapped in AsyncTask.
     *
     * @param favoriteStore Store to add to favorites.
     */
    public void saveFavoriteStoreToDb(FavoriteStore favoriteStore){
        try {
            Dao<FavoriteStore, Integer> favoriteStoreDao = getDatabaseHelper().getFavoriteStoresDao();
            favoriteStoreDao.createOrUpdate(favoriteStore);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
    }

    /**
     * Need to be wrapped in AsyncTask.
     *
     * @param id    Remove store from favorites.
     */
    public void removeFavoriteStoreById(int id){
        try {
            Dao<FavoriteStore, Integer> favoriteStoreDao = getDatabaseHelper().getFavoriteStoresDao();
            favoriteStoreDao.deleteById(id);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
    }

    /**
     * Need to be wrapped in AsyncTask.
     *
     * @return  List of FavoriteStores.
     */
    public List<FavoriteStore> getAllFavoriteStoresFromDb(){
        List<FavoriteStore> favoriteStores = new ArrayList<>();
        try{
            Dao<FavoriteStore, Integer> favoriteStoreDao = getDatabaseHelper().getFavoriteStoresDao();
            favoriteStores.addAll(favoriteStoreDao.queryForAll());
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
        return favoriteStores;
    }



    public void LoadStoresByIds(List<Integer> idsList){
        mStoresNumberToLoad = idsList.size();
        for (Integer storeId : idsList) {
            getStoreById(storeId);
        }
    }

    private void getStoreById(final int storeId){
        AsyncTask<Integer, Void, Store> getStoreByIdAsyncTask = new AsyncTask<Integer, Void, Store>() {
            @Override
            protected Store doInBackground(Integer... params) {
                Store store = null;
                try {
                    Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();
                    // @return The object that has the ID field which equals id or null if no matches.
                    store = storeDao.queryForId(params[0]);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Database exception in getStoreByIdAsyncTask()", e);
                    e.printStackTrace();
                }
                return store;
            }
            @Override
            protected void onPostExecute(Store store) {
                if(store != null){
                    onGetStoreByIdResult(store);
                }
                //try to load from server
                if(store == null){
                    if(getNetworkAvailability()){
                        getStoreByIdFromServer(storeId);
                    }
                }

                mTryCounter++;
                if(mTryCounter == mStoresNumberToLoad && mStoresNumberLoaded < mStoresNumberToLoad){
                    ((DataManagerCallbacks)mContext).onStoresListLoaded(mStores);
                }
            }
        };
        getStoreByIdAsyncTask.execute(storeId);
    }

    private void getStoreByIdFromServer(int storeId) {
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        Call<StoreResult> call = apiService.getOneStore(storeId, Config.LCBO_API_ACCESS_KEY);
        call.enqueue(new Callback<StoreResult>() {
            @Override
            public void onResponse(Call<StoreResult> call, Response<StoreResult> response) {
                Store store=null;

                if (response.isSuccessful()) {
                    StoreResult storeResult = response.body();
                    store = storeResult.getResult();
                } else {
                    Log.e(LOG_TAG, "getStoreByIdFromServer() - response problem.");
                }

                onGetStoreByIdResult(store);
            }

            @Override
            public void onFailure(Call<StoreResult> call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());
            }
        });
    }

    private void onGetStoreByIdResult(Store store){
        mStores.add(store);
        mStoresNumberLoaded++;
        if(mStores.size() == mStoresNumberToLoad){
            ((DataManagerCallbacks)mContext).onStoresListLoaded(mStores);
        }
    }



    private boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable(mContext);
    }

    public interface DataManagerCallbacks{
        void onStoresListLoaded(List<Store> stores);
    }
}
