package com.vitaliyhtc.lcbo.data.StoresAsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.data.StoresDataManagerCallbacks;
import com.vitaliyhtc.lcbo.model.Store;

import java.sql.SQLException;
import java.util.List;

public class GetStoresPageAsyncTask extends AsyncTask<Void, Void, Integer> {

    private DatabaseHelper mDatabaseHelper;
    private int mOffset;
    private boolean mIsInitialLoading;
    private List<Store> mStoresResult;
    private boolean mIsNetworkAvailable;
    private StoresDataManagerCallbacks mDataManagerCallbacks;
    private StoresDataManagerAsyncTaskCallbacks mStoresDataManager;
    private String mLogTag;

    public GetStoresPageAsyncTask(DatabaseHelper databaseHelper,
                                  int offset,
                                  boolean isInitialLoading,
                                  List<Store> storesResult,
                                  boolean isNetworkAvailable,
                                  StoresDataManagerCallbacks dataManagerCallbacks,
                                  StoresDataManagerAsyncTaskCallbacks storesDataManager,
                                  String logTag) {
        this.mDatabaseHelper = databaseHelper;
        this.mOffset = offset;
        this.mIsInitialLoading = isInitialLoading;
        this.mStoresResult = storesResult;
        this.mIsNetworkAvailable = isNetworkAvailable;
        this.mDataManagerCallbacks = dataManagerCallbacks;
        this.mStoresDataManager = storesDataManager;
        this.mLogTag = logTag;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int storedInDatabaseCounter = 0;
        try {
            Dao<Store, Integer> storeDao = mDatabaseHelper.getStoreDao();
            storedInDatabaseCounter = (int)storeDao.countOf();
        } catch (SQLException e) {
            Log.e(mLogTag, "Database exception in getStoresPageAsyncTask()", e);
            e.printStackTrace();
        }
        return storedInDatabaseCounter;
    }
    @Override
    protected void onPostExecute(Integer storedInDatabaseCounter) {
        if(mDataManagerCallbacks.getCountOfStoresInAdapter() < storedInDatabaseCounter){
            mStoresDataManager.getStoresPageFromDb(mOffset, mIsInitialLoading);
        } else if (mIsNetworkAvailable) {
            mStoresDataManager.getStoresPageFromNetwork(mOffset, mIsInitialLoading);
        } else {
            mStoresDataManager.onStoresPageLoaded(mOffset, mIsInitialLoading, mStoresResult);
        }
    }
}
