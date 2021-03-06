package com.vitaliyhtc.lcbo.data.tasks;

import android.os.AsyncTask;

import com.j256.ormlite.dao.Dao;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.model.Store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StoresSaveToDbAsyncTask extends AsyncTask<Void, Void, Void> {

    private DatabaseHelper mDatabaseHelper;
    private List<Store> mStoresToDb = new ArrayList<>();
    private List<Store> mListToAdd = new ArrayList<>();
    private int mStoresPage;

    public StoresSaveToDbAsyncTask(DatabaseHelper databaseHelper, List<Store> storesToDb, int storesPage) {
        this.mDatabaseHelper = databaseHelper;
        this.mStoresToDb.addAll(storesToDb);
        this.mStoresPage = storesPage;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try{
            Dao<Store, Integer> storeDao = mDatabaseHelper.getStoreDao();

            int i = 0;
            int incrementalCounter;
            int storeId;
            for (Store store : mStoresToDb) {
                storeId = store.getId();
                if(storeDao.queryBuilder().where().eq("id", storeId).countOf() == 0){
                    // Why storesPage-1 ? On first page elements numbers starts from 0!
                    incrementalCounter = (mStoresPage-1)* Config.STORES_PER_PAGE + i;
                    store.setIncrementalCounter(incrementalCounter);
                    i++;
                    mListToAdd.add(store);
                }
            }
            storeDao.create(mListToAdd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
