package com.vitaliyhtc.lcbo.data;

import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.ProductsByCategoriesActivity;
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

    private static final String LOG_TAG = StoresDataManager.class.getSimpleName();

    private ProductsByCategoriesActivity mContext;

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    private HashSet<Integer> productIds = new HashSet<>();

    private int pageOffsetLoadedFromServer;
    private int beerProductsDbOffset;
    private int wineProductsDbOffset;
    private int spiritsProductsDbOffset;

    private int productByCategoryUniqueItemsLoaded;

    private int currentPageRecordCount;

    private List<Product> uniqueProductsLoaded = new ArrayList<>();
    private List<Product> uniqueProductsByCategoryLoaded = new ArrayList<>();

    // used in loadFromServerAndSaveToDb() and loadFromDB()
    private List<Product> productsLoadedFromServer = new ArrayList<>();
    private List<Product> uniqueProductsList = new ArrayList<>();
    private List<Product> listToAddToDb = new ArrayList<>();
    private List<Product> productsLoadedFromDb = new ArrayList<>();





    public ProductsByCategoriesDataManager(ProductsByCategoriesActivity mContext) {
        this.mContext = mContext;
    }

    public void init(){
        if (Config.LCBO_API_ACCESS_KEY.isEmpty()) {
            Toast.makeText(mContext, "Please obtain your API ACCESS_KEY first from lcboapi.com", Toast.LENGTH_LONG).show();
        }
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
            mDatabaseHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }

    private long getCountOfProductsInDatabase(){
        long mStoredInDatabaseCounter = 0;
        try {
            mStoredInDatabaseCounter = getDatabaseHelper().getProductDao().countOf();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in performStoresSearch()", e);
            e.printStackTrace();
        }
        return mStoredInDatabaseCounter;
    }



    public void getNItemsOf(int numberOfItems, int category){
        if(getNetworkAvailability()){
            //load from server
            loadFromServerAndSaveToDb(numberOfItems, category);
        } else {
            //load from DB
            loadFromDB(numberOfItems, category);
        }
    }




    private void loadFromServerAndSaveToDb(final int numberOfItems, final int category){
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        int offset = pageOffsetLoadedFromServer+1;
        currentPageRecordCount = 0;

        Call<ProductsResult> call = apiService.getProductsResult(offset, Config.STORES_PER_PAGE,
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
                        String stringCategory = intCategoryToStringCategory(category);

                        try{
                            Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();

                            int productId;
                            for (Product product : productsLoadedFromServer) {
                                productId = product.getId();
                                if(!productIds.contains(productId)){
                                    productIds.add(productId);
                                    if(stringCategory.equals(product.getPrimaryCategory())){
                                        productByCategoryUniqueItemsLoaded++;
                                    }
                                    uniqueProductsList.add(product);
                                }
                                if(productDao.queryBuilder().where().eq("id", productId).countOf() == 0){
                                    listToAddToDb.add(product);
                                }
                            }

                            productDao.create(listToAddToDb);
                        } catch (SQLException e) {
                            Log.e(LOG_TAG, "Database exception", e);
                            e.printStackTrace();
                        }
                    }

                    Toast.makeText(mContext, "loadFromServerAndSaveToDb() :: Page: "+productsPage, Toast.LENGTH_SHORT).show();
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
                        if(productByCategoryUniqueItemsLoaded < numberOfItems){
                            loadFromServerAndSaveToDb(numberOfItems, category);
                        }else{
                            sendResultToActivity(uniqueProductsLoaded, category);
                        }
                    }else{
                        loadFromServerAndSaveToDb(numberOfItems, category);
                    }
                }else{
                    sendResultToActivity(uniqueProductsLoaded, category);
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



    private void loadFromDB(int numberOfItems, int category){
        productsLoadedFromDb.clear();
        uniqueProductsList.clear();
        try{
            Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();
            String categoryString = intCategoryToStringCategory(category);
            long startOffset = (int) getDbOffsetByCategory(category);

            QueryBuilder<Product, Integer> queryBuilder = productDao.queryBuilder();
            queryBuilder.where().eq("primaryCategory", categoryString);
            queryBuilder.offset(startOffset);
            queryBuilder.limit((long)Config.PRODUCTS_PER_PAGE);
            productsLoadedFromDb.addAll(productDao.query(queryBuilder.prepare()));

            int productId;
            int uniqueCounter=0;
            for (Product product : productsLoadedFromDb) {
                productId = product.getId();
                if(!productIds.contains(productId)){
                    productIds.add(productId);
                    productByCategoryUniqueItemsLoaded++;
                    uniqueProductsList.add(product);
                }
            }

            appendDbOffsetByCategory(uniqueCounter, category);

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in loadFromDB()", e);
            e.printStackTrace();
        }
        if(!uniqueProductsList.isEmpty()){
            uniqueProductsByCategoryLoaded.addAll(uniqueProductsList);
            if(productByCategoryUniqueItemsLoaded < numberOfItems){
                loadFromDB(numberOfItems, category);
            }else{
                sendResultToActivity(uniqueProductsByCategoryLoaded, category);
            }
        }else{
            sendResultToActivity(uniqueProductsByCategoryLoaded, category);
        }
    }



    private void sendResultToActivity(List<Product> products, int category){
        productByCategoryUniqueItemsLoaded = 0;
        mContext.onDataManagerResultLoaded(products, category);
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
        return Utils.isNetworkAvailable(mContext);
    }

    public interface ProductsByCategoriesDataManagerCallbacks{
        void onDataManagerResultLoaded(List<Product> products, int category);
    }
}
