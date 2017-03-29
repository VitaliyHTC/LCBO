package com.vitaliyhtc.lcbo.data.StoresAsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.data.StoresDataManagerImpl;

import java.sql.SQLException;

public class PerformStoresSearchPageOffsetAsyncTask extends AsyncTask<Void, Void, Integer> {

    private DatabaseHelper mDatabaseHelper;
    private StoresDataManagerAsyncTaskCallbacks mStoresDataManager;
    private String mLogTag;

    public PerformStoresSearchPageOffsetAsyncTask(DatabaseHelper databaseHelper,
                                                  StoresDataManagerImpl storesDataManager,
                                                  String logTag) {
        this.mDatabaseHelper = databaseHelper;
        this.mStoresDataManager = storesDataManager;
        this.mLogTag = logTag;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        long storedInDatabaseCounter = 0;
        try {
            storedInDatabaseCounter = mDatabaseHelper.getStoreDao().countOf();
        } catch (SQLException e) {
            Log.e(mLogTag, "Database exception in PerformStoresSearchPageOffsetAsyncTask()", e);
            e.printStackTrace();
        }
        return (int) storedInDatabaseCounter/ Config.STORES_PER_PAGE;
    }
    @Override
    protected void onPostExecute(Integer pagesLoaded) {
        mStoresDataManager.loadAndSaveStores(pagesLoaded+1);
    }
}
