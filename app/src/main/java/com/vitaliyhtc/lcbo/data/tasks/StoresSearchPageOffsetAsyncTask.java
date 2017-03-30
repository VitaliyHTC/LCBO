package com.vitaliyhtc.lcbo.data.tasks;

import android.os.AsyncTask;

import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;

import java.sql.SQLException;

public class StoresSearchPageOffsetAsyncTask extends AsyncTask<Void, Void, Integer> {

    private DatabaseHelper mDatabaseHelper;
    private ProcessResult mProcessResult;

    public StoresSearchPageOffsetAsyncTask(DatabaseHelper databaseHelper,
                                           ProcessResult processResult) {
        this.mDatabaseHelper = databaseHelper;
        this.mProcessResult = processResult;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        long storedInDatabaseCounter = 0;
        try {
            storedInDatabaseCounter = mDatabaseHelper.getStoreDao().countOf();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (int) storedInDatabaseCounter/ Config.STORES_PER_PAGE;
    }
    @Override
    protected void onPostExecute(Integer pagesLoaded) {
        mProcessResult.onProcessResult(pagesLoaded);
    }

    public interface ProcessResult{
        void onProcessResult(Integer pagesLoaded);
    }
}
