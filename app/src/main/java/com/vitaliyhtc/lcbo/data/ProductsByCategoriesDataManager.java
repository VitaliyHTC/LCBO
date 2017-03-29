package com.vitaliyhtc.lcbo.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.ProductsTab;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.model.ProductsResult;
import com.vitaliyhtc.lcbo.rest.ApiInterface;
import com.vitaliyhtc.lcbo.rest.RetrofitApiClient;
import com.vitaliyhtc.lcbo.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsByCategoriesDataManager {

    private static final String LOG_TAG = ProductsByCategoriesDataManager.class.getSimpleName();

    private DataManagerCallbacks mContext;

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    private HashSet<Integer> mProductIds = new HashSet<>();
    private int mIncrementalCounter = 0;

    private int pageOffsetLoadedFromServer = 0;
    private int beerProductsDbOffset = 0;
    private int wineProductsDbOffset = 0;
    private int spiritsProductsDbOffset = 0;

    private int productByCategoryUniqueItemsLoaded = 0;
    private int productByCategoryUniqueItemsLoaded1 = 0;

    private int beerUniqueItems = 0;
    private int wineUniqueItems = 0;
    private int spiritsUniqueItems = 0;

    private int currentPageRecordCount = 0;

    private int initialCounter = 0;

    private List<Product> uniqueProductsLoaded = new ArrayList<>();
    private List<Product> uniqueProductsByCategoryLoaded = new ArrayList<>();

    // used in loadFromServerAndSaveToDb() and loadFromDB()
    private List<Product> productsLoadedFromServer = new ArrayList<>();
    private List<Product> uniqueProductsList = new ArrayList<>();
    private List<Product> listToAddToDb = new ArrayList<>();
    private List<Product> productsLoadedFromDb = new ArrayList<>();



    public ProductsByCategoriesDataManager(DataManagerCallbacks mContext) {
        this.mContext = mContext;
    }

    /**
     * You'll need this in your class to release the helper when done.
     */
    public void onDestroy(){
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }
    }

    /**
     * You'll need this in your class to get the helper from the manager once per class.
     */
    private DatabaseHelper getDatabaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper((Context)mContext, DatabaseHelper.class);
            try {
                mIncrementalCounter = (int) mDatabaseHelper.getProductDao().countOf();
            } catch (SQLException e) {
                Log.e(LOG_TAG, "Database exception in getDatabaseHelper()", e);
                e.printStackTrace();
            }
        }
        return mDatabaseHelper;
    }

    private long getCountOfProductsInDatabase(){
        long storedInDatabaseCounter = 0;
        try {
            storedInDatabaseCounter = getDatabaseHelper().getProductDao().countOf();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in getCountOfProductsInDatabase()", e);
            e.printStackTrace();
        }
        return storedInDatabaseCounter;
    }









    public void performInitialLoading(){
        AsyncTask<Void, Void, Integer> performInitialLoadingAsyncTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return (int) getCountOfProductsInDatabase();
            }
            @Override
            protected void onPostExecute(Integer productsInDbCounter) {
                if(productsInDbCounter >= 3*Config.PRODUCTS_PER_PAGE){
                    if(Config.IS_LOG_DEBUG){
                        Log.d(LOG_TAG, "try initialLoadFromDb()");
                    }
                    initialLoadFromDb();
                } else if(getNetworkAvailability()){
                    if(Config.IS_LOG_DEBUG){
                        Log.d(LOG_TAG, "try initialLoadFromServerAndSaveToDb()");
                    }
                    initialLoadFromServerAndSaveToDb();
                } else {
                    Toast.makeText((Context)mContext, ":( We no have more data in database, and no internet connection!", Toast.LENGTH_LONG).show();
                    uniqueProductsLoaded.clear();
                    sendInitialResultToActivity(uniqueProductsLoaded);
                }
            }
        };
        performInitialLoadingAsyncTask.execute();
    }



    private void initialLoadFromServerAndSaveToDb(){
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        int offset = pageOffsetLoadedFromServer+1;
        currentPageRecordCount = 0;

        Call<ProductsResult> call = apiService.getProductsResult(offset, Config.PRODUCTS_PER_PAGE,
                Config.PRODUCTS_WHERE_NOT, Config.LCBO_API_ACCESS_KEY);
        call.enqueue(new Callback<ProductsResult>() {
            @Override
            public void onResponse(Call<ProductsResult>call, Response<ProductsResult> response) {

                /**
                 * Clear lists. Get productsResult from response. Get current page number for offset.
                 * Get DAO. Foreach productsResult and count unique items and add them to uniqueProductsList.
                 * Add unique in DB items to listToAddToDb. Save listToAddToDb to DB.
                 */

                uniqueProductsList.clear();
                listToAddToDb.clear();
                productsLoadedFromServer.clear();

                if(response.isSuccessful()){
                    ProductsResult productsResult = response.body();

                    currentPageRecordCount = productsResult.getPager().getCurrentPageRecordCount();

                    int productsPage = productsResult.getPager().getCurrentPage();

                    if(currentPageRecordCount > 0){
                        productsLoadedFromServer = productsResult.getResult();

                        pageOffsetLoadedFromServer = productsPage;

                        int productId;
                        for (Product product : productsLoadedFromServer) {
                            productId = product.getId();
                            if(!mProductIds.contains(productId)){
                                mProductIds.add(productId);
                                if(Config.PRODUCT_CATEGORY_BEER.equals(product.getPrimaryCategory())){
                                    beerUniqueItems++;
                                }
                                if(Config.PRODUCT_CATEGORY_WINE.equals(product.getPrimaryCategory())){
                                    wineUniqueItems++;
                                }
                                if(Config.PRODUCT_CATEGORY_SPIRITS.equals(product.getPrimaryCategory())){
                                    spiritsUniqueItems++;
                                }
                                uniqueProductsList.add(product);
                            }
                        }
                        final List<Product> productsToDb = new ArrayList<>();
                        productsToDb.addAll(productsLoadedFromServer);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();
                                    int productId1;
                                    for (Product product : productsToDb) {
                                        productId1 = product.getId();
                                        if(productDao.queryBuilder().where().eq("id", productId1).countOf() == 0){
                                            mIncrementalCounter++;
                                            product.setIncrementalCounter(mIncrementalCounter);
                                            listToAddToDb.add(product);
                                        }
                                    }
                                    productDao.create(listToAddToDb);
                                } catch (SQLException e) {
                                    Log.e(LOG_TAG, "Database exception", e);
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    Toast.makeText((Context)mContext, "loadFromServerAndSaveToDb() :: Page: "+productsPage, Toast.LENGTH_SHORT).show();
                }else{
                    Log.e(LOG_TAG, "loadFromServerAndSaveToDb() - response problem.");
                }

                if(currentPageRecordCount > 0){
                    if(!uniqueProductsList.isEmpty()){
                        uniqueProductsLoaded.addAll(uniqueProductsList);
                        int min = getMinOf(beerUniqueItems, wineUniqueItems, spiritsUniqueItems);
                        if(min < Config.PRODUCTS_PER_PAGE){
                            initialLoadFromServerAndSaveToDb();
                        }else{
                            sendInitialResultToActivity(uniqueProductsLoaded);
                        }
                    }else{
                        initialLoadFromServerAndSaveToDb();
                    }
                }else{
                    sendInitialResultToActivity(uniqueProductsLoaded);
                }

            }

            @Override
            public void onFailure(Call<ProductsResult>call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());

                // Continue work.
                sendInitialResultToActivity(uniqueProductsLoaded);
            }
        });

    }



    private void initialLoadFromDb(final int numberOfItems, final int category){
        productsLoadedFromDb.clear();
        uniqueProductsList.clear();

        AsyncTask<Void, Void, List<Product>> initialLoadFromDbAsyncTask = new AsyncTask<Void, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(Void... params) {
                List<Product> products = new ArrayList<>();
                try{
                    Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();
                    String categoryString = intCategoryToStringCategory(category);
                    long startOffset = getDbOffsetByCategory(category);

                    QueryBuilder<Product, Integer> queryBuilder = productDao.queryBuilder();
                    queryBuilder.orderBy("incrementalCounter", true);
                    queryBuilder.where().eq("primaryCategory", categoryString);
                    queryBuilder.offset(startOffset);
                    queryBuilder.limit((long)Config.PRODUCTS_PER_PAGE);
                    productsLoadedFromDb.addAll(productDao.query(queryBuilder.prepare()));

                    int productId;
                    int uniqueCounter=0;
                    for (Product product : productsLoadedFromDb) {
                        productId = product.getId();
                        if(!mProductIds.contains(productId)){
                            mProductIds.add(productId);
                            uniqueCounter++;
                            productByCategoryUniqueItemsLoaded++;
                            products.add(product);
                        }
                    }
                    appendDbOffsetByCategory(uniqueCounter, category);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Database exception in initialLoadFromDb()", e);
                    e.printStackTrace();
                }
                return products;
            }
            @Override
            protected void onPostExecute(List<Product> products) {
                //Toast.makeText((Context)mContext, "products: "+products.size(), Toast.LENGTH_SHORT).show();
                //uniqueProductsList.addAll(products);
                if(!products.isEmpty()){
                    uniqueProductsByCategoryLoaded.addAll(products);
                    if(productByCategoryUniqueItemsLoaded < numberOfItems){
                        initialLoadFromDb(numberOfItems, category);
                    }
                }
                postInitialResultToActivity();
            }
        };
        initialLoadFromDbAsyncTask.execute();
    }

    private void postInitialResultToActivity(){
        initialCounter++;
        if(initialCounter==3){
            //Toast.makeText((Context)mContext, "products: "+uniqueProductsByCategoryLoaded.size(), Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    sendInitialResultToActivity(uniqueProductsByCategoryLoaded);
                }
            }, 100);
        }
    }

    private void initialLoadFromDb(){
        AsyncTask<Void, Void, Integer> initialLoadFromDbAsyncTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return (int) getCountOfProductsInDatabase();
            }
            @Override
            protected void onPostExecute(Integer initialProductsInDbCount) {
                if(initialProductsInDbCount == 0){
                    Toast.makeText((Context)mContext, "No products in DB. No internet connection", Toast.LENGTH_LONG).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            sendInitialResultToActivity(uniqueProductsByCategoryLoaded);
                        }
                    }, 100);
                }else{
                    //Toast.makeText((Context)mContext, "Products in DB: "+initialProductsInDbCount, Toast.LENGTH_LONG).show();
                    initialLoadFromDb(Config.PRODUCTS_PER_PAGE, ProductsTab.TAB_CATEGORY_BEER);
                    productByCategoryUniqueItemsLoaded = 0;
                    initialLoadFromDb(Config.PRODUCTS_PER_PAGE, ProductsTab.TAB_CATEGORY_WINE);
                    productByCategoryUniqueItemsLoaded = 0;
                    initialLoadFromDb(Config.PRODUCTS_PER_PAGE, ProductsTab.TAB_CATEGORY_SPIRITS);
                    productByCategoryUniqueItemsLoaded = 0;
                    // See postInitialResultToActivity()
                }
            }
        };
        initialLoadFromDbAsyncTask.execute();
    }



    private void sendInitialResultToActivity(List<Product> products){
        mContext.onDataManagerInitialResultLoaded(products);
        uniqueProductsLoaded.clear();
        uniqueProductsByCategoryLoaded.clear();
    }











    public void getNItemsOf(final int numberOfItems, final int category){
        AsyncTask<Void, Void, Integer> getNItemsOfAsyncTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return (int) getCountOfProductsInDatabase();
            }
            @Override
            protected void onPostExecute(Integer productsInDbCounter) {
                if(mContext.getProductsLoaded() < productsInDbCounter){
                    loadFromDb(numberOfItems, category);
                } else if(getNetworkAvailability()){
                    pageOffsetLoadedFromServer = mContext.getProductsLoaded()/Config.PRODUCTS_PER_PAGE - 1;
                    loadFromServerAndSaveToDb(numberOfItems, category);
                } else {
                    Toast.makeText((Context)mContext, ":( We no have more data in database, and no internet connection!", Toast.LENGTH_LONG).show();
                    uniqueProductsLoaded.clear();
                    sendResultToActivity(uniqueProductsLoaded, category);
                }
            }
        };
        getNItemsOfAsyncTask.execute();
    }



    private void loadFromDb(final int numberOfItems, final int category){
        productsLoadedFromDb.clear();
        uniqueProductsList.clear();

        AsyncTask<Void, Void, List<Product>> loadFromDbAsyncTask = new AsyncTask<Void, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(Void... params) {
                List<Product> products = new ArrayList<>();
                try{
                    Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();
                    String categoryString = intCategoryToStringCategory(category);
                    long startOffset = getDbOffsetByCategory(category);

                    QueryBuilder<Product, Integer> queryBuilder = productDao.queryBuilder();
                    queryBuilder.orderBy("incrementalCounter", true);
                    queryBuilder.where().eq("primaryCategory", categoryString);
                    queryBuilder.offset(startOffset);
                    queryBuilder.limit((long)Config.PRODUCTS_PER_PAGE);
                    productsLoadedFromDb.addAll(productDao.query(queryBuilder.prepare()));

                    int productId;
                    int uniqueCounter=0;
                    for (Product product : productsLoadedFromDb) {
                        productId = product.getId();
                        if(!mProductIds.contains(productId)){
                            uniqueCounter++;
                            mProductIds.add(productId);
                            productByCategoryUniqueItemsLoaded++;
                            products.add(product);
                        }
                    }
                    appendDbOffsetByCategory(uniqueCounter, category);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Database exception in loadFromDB()", e);
                    e.printStackTrace();
                }
                return products;
            }
            @Override
            protected void onPostExecute(List<Product> products) {
                uniqueProductsList.addAll(products);
                if(!uniqueProductsList.isEmpty()){
                    uniqueProductsByCategoryLoaded.addAll(uniqueProductsList);
                    if(productByCategoryUniqueItemsLoaded < numberOfItems){
                        loadFromDb(numberOfItems, category);
                    }else{
                        sendResultToActivity(uniqueProductsByCategoryLoaded, category);
                    }
                }else{
                    if(getNetworkAvailability()) {
                        pageOffsetLoadedFromServer = mContext.getProductsLoaded() / Config.PRODUCTS_PER_PAGE - 1;
                        uniqueProductsLoaded.addAll(uniqueProductsByCategoryLoaded);
                        loadFromServerAndSaveToDb(numberOfItems, category);
                    }else{
                        sendResultToActivity(uniqueProductsByCategoryLoaded, category);
                    }
                }
            }
        };
        loadFromDbAsyncTask.execute();
    }



    private void loadFromServerAndSaveToDb(final int numberOfItems, final int category){
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        int offset = pageOffsetLoadedFromServer+1;
        currentPageRecordCount = 0;

        Call<ProductsResult> call = apiService.getProductsResult(offset, Config.PRODUCTS_PER_PAGE,
                Config.PRODUCTS_WHERE_NOT, Config.LCBO_API_ACCESS_KEY);
        call.enqueue(new Callback<ProductsResult>() {
            @Override
            public void onResponse(Call<ProductsResult>call, Response<ProductsResult> response) {

                /**
                 * Clear lists. Get productsResult from response. Get current page number for offset.
                 * Get DAO. Foreach productsResult and count unique items and add them to uniqueProductsList.
                 * Add unique in DB items to listToAddToDb. Save listToAddToDb to DB.
                 */

                uniqueProductsList.clear();
                listToAddToDb.clear();
                productsLoadedFromServer.clear();

                int productsPage;

                if(response.isSuccessful()){
                    ProductsResult productsResult = response.body();

                    currentPageRecordCount = productsResult.getPager().getCurrentPageRecordCount();

                    productsPage = productsResult.getPager().getCurrentPage();

                    if(currentPageRecordCount > 0){
                        productsLoadedFromServer = productsResult.getResult();

                        pageOffsetLoadedFromServer = productsPage;
                        final String stringCategory = intCategoryToStringCategory(category);

                        int productId;
                        for (Product product : productsLoadedFromServer) {
                            productId = product.getId();
                            if(!mProductIds.contains(productId)){
                                mProductIds.add(productId);
                                if(stringCategory.equals(product.getPrimaryCategory())){
                                    productByCategoryUniqueItemsLoaded1++;
                                }
                                uniqueProductsList.add(product);
                            }
                        }
                        final List<Product> productsToDb = new ArrayList<>();
                        productsToDb.addAll(productsLoadedFromServer);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();
                                    int productId1;
                                    for (Product product : productsToDb) {
                                        productId1 = product.getId();
                                        if(productDao.queryBuilder().where().eq("id", productId1).countOf() == 0){
                                            mIncrementalCounter++;
                                            product.setIncrementalCounter(mIncrementalCounter);
                                            listToAddToDb.add(product);
                                        }
                                    }
                                    productDao.create(listToAddToDb);
                                } catch (SQLException e) {
                                    Log.e(LOG_TAG, "Database exception", e);
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    Toast.makeText((Context)mContext, "loadFromServerAndSaveToDb() :: Page: "+productsPage, Toast.LENGTH_SHORT).show();
                }else{
                    Log.e(LOG_TAG, "loadFromServerAndSaveToDb() - response problem.");
                }

                /**
                 * count unique products of chosen category to productByCategoryUniqueItemsLoaded.
                 * if uniqueProductsList contains products and productByCategoryUniqueItemsLoaded < 40,
                 * add them to uniqueProductsLoaded and perform new request to server.
                 * if uniqueProductsList contains products and productByCategoryUniqueItemsLoaded >= 40
                 * add them to uniqueProductsLoaded and call sendResultToActivity(uniqueProductsLoaded, category);
                 * if currentPageRecordCount==0 - call sendResultToActivity(uniqueProductsLoaded, category);
                 */
                if(currentPageRecordCount > 0){
                    if(!uniqueProductsList.isEmpty()){
                        uniqueProductsLoaded.addAll(uniqueProductsList);
                        if(productByCategoryUniqueItemsLoaded1 < numberOfItems){
                            loadFromServerAndSaveToDb(numberOfItems, category);
                        }else{
                            sendResultToActivity(uniqueProductsLoaded, category);
                            productByCategoryUniqueItemsLoaded1 = 0;
                        }
                    }else{
                        loadFromServerAndSaveToDb(numberOfItems, category);
                    }
                }else{
                    sendResultToActivity(uniqueProductsLoaded, category);
                    productByCategoryUniqueItemsLoaded1 = 0;
                }

            }

            @Override
            public void onFailure(Call<ProductsResult>call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());

                // Continue work.
                sendResultToActivity(uniqueProductsLoaded, category);
            }
        });

    }



    private void sendResultToActivity(List<Product> products, int category){
        mContext.onDataManagerResultLoaded(products, category);
        productByCategoryUniqueItemsLoaded = 0;
        uniqueProductsLoaded.clear();
        uniqueProductsByCategoryLoaded.clear();
    }



    private String intCategoryToStringCategory(int intCategory){
        if(ProductsTab.TAB_CATEGORY_BEER == intCategory){
            return Config.PRODUCT_CATEGORY_BEER;
        }
        if(ProductsTab.TAB_CATEGORY_WINE == intCategory){
            return Config.PRODUCT_CATEGORY_WINE;
        }
        if(ProductsTab.TAB_CATEGORY_SPIRITS == intCategory){
            return Config.PRODUCT_CATEGORY_SPIRITS;
        }
        return "";
    }

    private int getDbOffsetByCategory(int category){
        int startOffset = 0;
        if(ProductsTab.TAB_CATEGORY_BEER == category){
            startOffset = beerProductsDbOffset;
        }
        if(ProductsTab.TAB_CATEGORY_WINE == category){
            startOffset = wineProductsDbOffset;
        }
        if(ProductsTab.TAB_CATEGORY_SPIRITS == category){
            startOffset = spiritsProductsDbOffset;
        }
        return startOffset;
    }

    private void appendDbOffsetByCategory(int uniqueCounter, int category){
        if(ProductsTab.TAB_CATEGORY_BEER == category){
            beerProductsDbOffset += uniqueCounter;
        }
        if(ProductsTab.TAB_CATEGORY_WINE == category){
            wineProductsDbOffset += uniqueCounter;
        }
        if(ProductsTab.TAB_CATEGORY_SPIRITS == category){
            spiritsProductsDbOffset += uniqueCounter;
        }
    }

    private boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable((Context)mContext);
    }

    private int getMinOf(int a, int b, int c){
        int smallest;
        if(a<b && a<c){
            smallest = a;
        }else if(b<c && b<a){
            smallest = b;
        }else{
            smallest = c;
        }
        return smallest;
    }

    public interface DataManagerCallbacks {
        void onDataManagerInitialResultLoaded(List<Product> products);
        void onDataManagerResultLoaded(List<Product> products, int category);
        int getProductsLoaded();
    }
}
