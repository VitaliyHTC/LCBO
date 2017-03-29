package com.vitaliyhtc.lcbo.data;

import android.os.AsyncTask;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.vitaliyhtc.lcbo.interfaces.BaseView;

public class DatabaseHelperTableCleaner {

    public void clearDbTables(final BaseView view){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseHelper databaseHelper = OpenHelperManager.getHelper(view.getContext(), DatabaseHelper.class);

                databaseHelper.clearStoresTable();
                databaseHelper.clearProductsTable();

                OpenHelperManager.releaseHelper();
            }
        });
    }
}
