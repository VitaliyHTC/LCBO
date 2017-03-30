package com.vitaliyhtc.lcbo.data.tasks;

import android.os.AsyncTask;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.model.Store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StoresPageFromDbAsyncTask extends AsyncTask<Void, Void, List<Store>> {

    private DatabaseHelper mDatabaseHelper;
    private int mOffset;
    private ProcessResult mProcessResult;


    public StoresPageFromDbAsyncTask(DatabaseHelper databaseHelper,
                                     int offset,
                                     ProcessResult processResult) {
        this.mDatabaseHelper = databaseHelper;
        this.mOffset = offset;
        this.mProcessResult = processResult;
    }

    @Override
    protected List<Store> doInBackground(Void... params) {
        List<Store> stores = new ArrayList<>();
        try {
            Dao<Store, Integer> storeDao = mDatabaseHelper.getStoreDao();
            // See: http://ormlite.com/javadoc/ormlite-core/com/j256/ormlite/stmt/QueryBuilder.html
            long startRow = (mOffset - 1) * Config.STORES_PER_PAGE;
            QueryBuilder<Store, Integer> queryBuilder = storeDao.queryBuilder();
            queryBuilder.orderBy("incrementalCounter", true);
            queryBuilder.offset(startRow);
            queryBuilder.limit((long)Config.STORES_PER_PAGE);
            stores.addAll(storeDao.query(queryBuilder.prepare()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stores;
    }
    @Override
    protected void onPostExecute(List<Store> stores) {
        mProcessResult.onProcessResult(stores);
    }

    public interface ProcessResult{
        void onProcessResult(List<Store> stores);
    }
}
