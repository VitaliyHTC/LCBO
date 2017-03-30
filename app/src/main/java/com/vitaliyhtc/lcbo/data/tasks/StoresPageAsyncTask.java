package com.vitaliyhtc.lcbo.data.tasks;

import android.os.AsyncTask;

import com.j256.ormlite.dao.Dao;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.model.Store;

import java.sql.SQLException;

public class StoresPageAsyncTask extends AsyncTask<Void, Void, Integer> {

    private DatabaseHelper mDatabaseHelper;
    private ProcessResult mProcessResult;

    public StoresPageAsyncTask(DatabaseHelper databaseHelper,
                               ProcessResult processResult) {
        this.mDatabaseHelper = databaseHelper;
        this.mProcessResult = processResult;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int storedInDatabaseCounter = 0;
        try {
            Dao<Store, Integer> storeDao = mDatabaseHelper.getStoreDao();
            storedInDatabaseCounter = (int)storeDao.countOf();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storedInDatabaseCounter;
    }
    @Override
    protected void onPostExecute(Integer storedInDatabaseCounter) {
        mProcessResult.onProcessResult(storedInDatabaseCounter);
    }

    public interface ProcessResult {
        void onProcessResult(Integer storedInDatabaseCounter);
    }
}
