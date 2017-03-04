package com.vitaliyhtc.lcbo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.model.Store;

import java.sql.SQLException;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String LOG_TAG = DatabaseHelper.class.getName();

    private static final String DATABASE_NAME = "lcbo.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Store, Integer> storeDao = null;
    private Dao<Product, Integer> productDao = null;
    private RuntimeExceptionDao<Store, Integer> storeRuntimeDao = null;
    private RuntimeExceptionDao<Product, Integer> productRuntimeDao = null;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(LOG_TAG, "onCreate");
            TableUtils.createTable(connectionSource, Store.class);
            TableUtils.createTable(connectionSource, Product.class);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Can't create database", e);
            e.printStackTrace();
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(LOG_TAG, "onUpgrade");
            TableUtils.dropTable(connectionSource, Store.class, true);
            TableUtils.dropTable(connectionSource, Product.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Can't drop databases", e);
            e.printStackTrace();
        }
    }

    /**
     * Clear all data out of the stores table.
     * WARNING: This is [obviously] very destructive and is unrecoverable.
     */
    public void clearStoresTable(){
        try {
            TableUtils.clearTable(getConnectionSource(), Store.class);
            Log.i(LOG_TAG, "clearStoresTable complete!");
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Can't clear stores table", e);
            e.printStackTrace();
        }
    }

    /**
     * Clear all data out of the products table.
     * WARNING: This is [obviously] very destructive and is unrecoverable.
     */
    public void clearProductsTable(){
        try {
            TableUtils.clearTable(getConnectionSource(), Product.class);
            Log.i(LOG_TAG, "clearProductsTable complete!");
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Can't clear products table", e);
            e.printStackTrace();
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our Store class. It will create it or just give the cached
     * value.
     */
    public Dao<Store, Integer> getStoreDao() throws SQLException {
        if(storeDao == null){
            storeDao = getDao(Store.class);
        }
        return storeDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our Product class. It will create it or just give the cached
     * value.
     */
    public Dao<Product, Integer> getProductDao() throws SQLException {
        if(productDao == null){
            productDao = getDao(Product.class);
        }
        return productDao;
    }

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Store class. It will
     * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<Store, Integer> getStoreRuntimeDao() {
        if (storeRuntimeDao == null) {
            storeRuntimeDao = getRuntimeExceptionDao(Store.class);
        }
        return storeRuntimeDao;
    }

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our Product class. It will
     * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<Product, Integer> getProductRuntimeDao() {
        if (productRuntimeDao == null) {
            productRuntimeDao = getRuntimeExceptionDao(Product.class);
        }
        return productRuntimeDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        storeDao = null;
        productDao = null;
        storeRuntimeDao = null;
        productRuntimeDao = null;
    }

}
