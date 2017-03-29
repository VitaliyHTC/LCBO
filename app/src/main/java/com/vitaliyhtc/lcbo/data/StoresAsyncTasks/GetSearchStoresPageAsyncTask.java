package com.vitaliyhtc.lcbo.data.StoresAsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.data.StoresDataManagerCallbacks;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;
import com.vitaliyhtc.lcbo.model.Store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetSearchStoresPageAsyncTask extends AsyncTask<Void, Void, List<Store>> {

    private DatabaseHelper mDatabaseHelper;
    private int mOffset;
    private StoresSearchParameters mStoresSearchParameters;
    private StoresDataManagerCallbacks mDataManagerCallbacks;
    private String mLogTag;

    public GetSearchStoresPageAsyncTask(DatabaseHelper databaseHelper,
                                        int offset,
                                        StoresSearchParameters storesSearchParameters,
                                        StoresDataManagerCallbacks dataManagerCallbacks,
                                        String logTag) {
        this.mDatabaseHelper = databaseHelper;
        this.mOffset = offset;
        this.mStoresSearchParameters = storesSearchParameters;
        this.mDataManagerCallbacks = dataManagerCallbacks;
        this.mLogTag = logTag;
    }

    @Override
    protected List<Store> doInBackground(Void... params) {
        List<Store> storesResult = new ArrayList<>();
        try{
            Dao<Store, Integer> storeDao = mDatabaseHelper.getStoreDao();
            long storesPerPage = Config.STORES_PER_PAGE;

            // See: http://ormlite.com/javadoc/ormlite-core/com/j256/ormlite/stmt/QueryBuilder.html
            long startRow = (mOffset - 1) * storesPerPage;
            QueryBuilder<Store, Integer> queryBuilder = storeDao.queryBuilder();

            Where where = null;
            if(mStoresSearchParameters.isWhereNotEmpty()){
                where = queryBuilder.where();
            }
            mStoresSearchParameters.configureWhere(where);

            queryBuilder.orderBy("incrementalCounter", true);
            queryBuilder.offset(startRow);
            queryBuilder.limit(storesPerPage);
            storesResult.addAll(storeDao.query(queryBuilder.prepare()));
        } catch (SQLException e) {
            Log.e(mLogTag, "Database exception in performStoresSearch()", e);
            e.printStackTrace();
        }
        return storesResult;
    }
    @Override
    protected void onPostExecute(List<Store> stores) {
        mDataManagerCallbacks.onStoresSearchListLoaded(stores, mOffset);
    }
}
