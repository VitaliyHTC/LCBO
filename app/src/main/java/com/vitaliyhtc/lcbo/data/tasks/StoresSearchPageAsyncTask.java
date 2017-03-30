package com.vitaliyhtc.lcbo.data.tasks;

import android.os.AsyncTask;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;
import com.vitaliyhtc.lcbo.model.Store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StoresSearchPageAsyncTask extends AsyncTask<Void, Void, List<Store>> {

    private DatabaseHelper mDatabaseHelper;
    private Long mStartPosition;
    private Long mCount;
    private StoresSearchParameters mStoresSearchParameters;
    private ProcessResult mProcessResult;

    public StoresSearchPageAsyncTask(DatabaseHelper databaseHelper,
                                     Long startPosition,
                                     Long count,
                                     StoresSearchParameters storesSearchParameters,
                                     ProcessResult processResult) {
        this.mDatabaseHelper = databaseHelper;
        this.mStartPosition = startPosition;
        this.mCount = count;
        this.mStoresSearchParameters = storesSearchParameters;
        this.mProcessResult = processResult;
    }

    @Override
    protected List<Store> doInBackground(Void... params) {
        List<Store> storesResult = new ArrayList<>();
        try{
            Dao<Store, Integer> storeDao = mDatabaseHelper.getStoreDao();

            // See: http://ormlite.com/javadoc/ormlite-core/com/j256/ormlite/stmt/QueryBuilder.html
            QueryBuilder<Store, Integer> queryBuilder = storeDao.queryBuilder();

            Where where = null;
            if(mStoresSearchParameters.isWhereNotEmpty()){
                where = queryBuilder.where();
            }
            mStoresSearchParameters.configureWhere(where);

            queryBuilder.orderBy("incrementalCounter", true);
            queryBuilder.offset(mStartPosition);
            queryBuilder.limit(mCount);
            storesResult.addAll(storeDao.query(queryBuilder.prepare()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storesResult;
    }
    @Override
    protected void onPostExecute(List<Store> stores) {
        mProcessResult.onProcessResult(stores);
    }

    public interface ProcessResult {
        void onProcessResult(List<Store> stores);
    }
}
