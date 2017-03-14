package com.vitaliyhtc.lcbo.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.helpers.ProductsSearchParameters;
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

public class ProductsDataManager {

    private static final String LOG_TAG = ProductsDataManager.class.getSimpleName();

    private DataManagerCallbacks mContext;

    private HashSet<Integer> mProductIds = new HashSet<>();

    private List<Product> mProductsResult = new ArrayList<>();
    private List<Product> mUniqueProducts = new ArrayList<>();
    private List<Product> mListToAdd = new ArrayList<>();

    private ProductsSearchParameters mProductsSearchParameters;

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    /**
     * @param context ProductsSearchActivity context
     */
    public ProductsDataManager(DataManagerCallbacks context) {
        this.mContext = context;
    }

    public void init(){
        if (Config.LCBO_API_ACCESS_KEY.isEmpty()) {
            Toast.makeText((Context)mContext, "Please obtain your API ACCESS_KEY first from lcboapi.com", Toast.LENGTH_LONG).show();
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
            mDatabaseHelper = OpenHelperManager.getHelper((Context)mContext, DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }



    public void performProductsSearch(ProductsSearchParameters productsSearchParameters){
        mProductsSearchParameters = productsSearchParameters;
        mProductIds.clear();
        getSearchProductsPage(1);
    }

    public void getSearchProductsPage(int offset){
        if(getNetworkAvailability()){
            searchProductsOnServer(offset);
        } else {
            searchProductsInDb(offset);
        }
    }

    private void searchProductsOnServer(final int offset){
        mProductsResult.clear();
        mUniqueProducts.clear();
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        String query = mProductsSearchParameters.getSearchStringQuery();
        String where = mProductsSearchParameters.getWhereStringQuery();

        Call<ProductsResult> call;
        if((!query.isEmpty())&&(!where.isEmpty())){
            call = apiService.getProductsSearchWithQWResult(offset, Config.PRODUCTS_PER_PAGE,
                    Config.PRODUCTS_WHERE_NOT, query, where, Config.LCBO_API_ACCESS_KEY);
        } else if ((!query.isEmpty())&&where.isEmpty()){
            call = apiService.getProductsSearchWithQResult(offset, Config.PRODUCTS_PER_PAGE,
                    Config.PRODUCTS_WHERE_NOT, query, Config.LCBO_API_ACCESS_KEY);
        } else if (query.isEmpty()&&(!where.isEmpty())){
            call = apiService.getProductsSearchWithWResult(offset, Config.PRODUCTS_PER_PAGE,
                    Config.PRODUCTS_WHERE_NOT, where, Config.LCBO_API_ACCESS_KEY);
        } else {
            call = apiService.getProductsResult(offset, Config.PRODUCTS_PER_PAGE, Config.PRODUCTS_WHERE_NOT, Config.LCBO_API_ACCESS_KEY);
        }

        call.enqueue(new Callback<ProductsResult>() {
            @Override
            public void onResponse(Call<ProductsResult>call, Response<ProductsResult> response) {

                if(response.isSuccessful()){
                    ProductsResult productsResult = response.body();
                    mListToAdd.clear();
                    mProductsResult = productsResult.getResult();

                    try{
                        Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();

                        int incrementalCounter = (int) productDao.countOf();
                        int productId;
                        for (Product product : mProductsResult) {
                            productId = product.getId();
                            if(!mProductIds.contains(productId)){
                                mProductIds.add(productId);
                                mUniqueProducts.add(product);
                            }
                            if(productDao.queryBuilder().where().eq("id", productId).countOf() == 0){
                                incrementalCounter++;
                                product.setIncrementalCounter(incrementalCounter);
                                mListToAdd.add(product);
                            }
                        }
                        productDao.create(mListToAdd);
                    } catch (SQLException e) {
                        Log.e(LOG_TAG, "Database exception in searchProductsOnServer()", e);
                        e.printStackTrace();
                    }
                }else{
                    Log.e(LOG_TAG, "searchProductsOnServer() - response problem.");
                }

                onSearchResultLoaded(mUniqueProducts, offset);

            }

            @Override
            public void onFailure(Call<ProductsResult>call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());

                // Continue work.
                mUniqueProducts.clear();
                onSearchResultLoaded(mUniqueProducts, offset);
            }
        });

    }

    private void searchProductsInDb(int offset){
        mProductsResult.clear();
        mUniqueProducts.clear();
        try{
            Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();
            long productsPerPage = Config.PRODUCTS_PER_PAGE;

            long startRow = (offset - 1) * productsPerPage;
            QueryBuilder<Product, Integer> queryBuilder = productDao.queryBuilder();

            Where where = null;
            boolean containsTrueValues = mProductsSearchParameters.containTrueValues();
            String searchQuery = mProductsSearchParameters.getSearchStringQuery();

            if(containsTrueValues || (!searchQuery.isEmpty())){
                where = queryBuilder.where();
            }
            if(!searchQuery.isEmpty()){
                where.like("name", "%"+searchQuery+"%");
                where.like("tags", "%"+searchQuery+"%");
                where.like("stockType", "%"+searchQuery+"%");
                where.like("primaryCategory", "%"+searchQuery+"%");
                where.like("secondaryCategory", "%"+searchQuery+"%");
                where.like("origin", "%"+searchQuery+"%");
                where.like("producerName", "%"+searchQuery+"%");
                where.like("varietal", "%"+searchQuery+"%");
                where.like("style", "%"+searchQuery+"%");
                where.like("tertiaryCategory", "%"+searchQuery+"%");
                where.or(10);
                if(containsTrueValues){
                    where.and();
                }
            }

            if(containsTrueValues){
                if(mProductsSearchParameters.isHasValueAddedPromotion()){
                    where.eq("hasValueAddedPromotion", true);
                    where.and();
                }
                if(mProductsSearchParameters.isHasLimitedTimeOffer()){
                    where.eq("hasLimitedTimeOffer", true);
                    where.and();
                }
                if(mProductsSearchParameters.isHasBonusRewardMiles()){
                    where.eq("hasBonusRewardMiles", true);
                    where.and();
                }
                if(mProductsSearchParameters.isSeasonal()){
                    where.eq("isSeasonal", true);
                    where.and();
                }
                if(mProductsSearchParameters.isVqa()){
                    where.eq("isVqa", true);
                    where.and();
                }
                if(mProductsSearchParameters.isOcb()){
                    where.eq("isOcb", true);
                    where.and();
                }
                if(mProductsSearchParameters.isKosher()){
                    where.eq("isKosher", true);
                    where.and();
                }
                where.eq("isDead", false);
            }

            queryBuilder.orderBy("incrementalCounter", true);
            queryBuilder.offset(startRow);
            queryBuilder.limit(productsPerPage);
            mProductsResult.addAll(productDao.query(queryBuilder.prepare()));

            Toast.makeText((Context)mContext, "Search. Load from DataBase.", Toast.LENGTH_SHORT).show();

            int productId;
            for (Product product : mProductsResult) {
                productId = product.getId();
                if(!mProductIds.contains(productId)){
                    mProductIds.add(productId);
                    mUniqueProducts.add(product);
                }
            }

            onSearchResultLoaded(mUniqueProducts, offset);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in searchProductsInDb()", e);
            e.printStackTrace();
        }
    }

    private void onSearchResultLoaded(List<Product> products, int offset){
        mContext.onProductsSearchListLoaded(products, offset);
        mProductsResult.clear();
        mUniqueProducts.clear();
    }



    public void getProductsPage(int offset, boolean isInitialLoading){
        if(isInitialLoading){
            mProductIds.clear();
        }
        mProductsResult.clear();
        mUniqueProducts.clear();

        long storedInDatabaseCounter;
        long productsPerPage = Config.PRODUCTS_PER_PAGE;

        try{
            Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();
            storedInDatabaseCounter = productDao.countOf();

            if(mContext.getCountOfProductsInAdapter() < storedInDatabaseCounter){
                long startRow = (offset - 1) * productsPerPage;
                QueryBuilder<Product, Integer> queryBuilder = productDao.queryBuilder();
                queryBuilder.orderBy("incrementalCounter", true);
                queryBuilder.offset(startRow);
                queryBuilder.limit(productsPerPage);
                mProductsResult.addAll(productDao.query(queryBuilder.prepare()));

                int productId;
                for (Product product : mProductsResult) {
                    productId = product.getId();
                    if(!mProductIds.contains(productId)){
                        mProductIds.add(productId);
                        mUniqueProducts.add(product);
                    }
                }

                Toast.makeText((Context)mContext, "Load from DataBase", Toast.LENGTH_SHORT).show();
                onProductsPageLoaded(offset, isInitialLoading, mUniqueProducts);
            } else if (getNetworkAvailability()) {
                getProductsPageFromNetwork(offset, isInitialLoading);
            } else {
                Toast.makeText((Context)mContext, ":( We no have more data in database, and no internet connection!", Toast.LENGTH_LONG).show();
                mUniqueProducts.clear();
                onProductsPageLoaded(offset, isInitialLoading, mUniqueProducts);
            }

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in getProductsPage()", e);
            e.printStackTrace();
        }
    }

    private void onProductsPageLoaded(int offset, boolean isInitialLoading, List<Product> products){
        if(isInitialLoading){
            mContext.onInitProductsListLoaded(products, offset);
        }else{
            mContext.onProductsListLoaded(products, offset);
        }
        mProductsResult.clear();
        mUniqueProducts.clear();
    }

    private void getProductsPageFromNetwork(final int offset, final boolean isInitialLoading){
        mProductsResult.clear();
        mUniqueProducts.clear();
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        Call<ProductsResult> call = apiService.getProductsResult(offset, Config.PRODUCTS_PER_PAGE, Config.PRODUCTS_WHERE_NOT, Config.LCBO_API_ACCESS_KEY);

        call.enqueue(new Callback<ProductsResult>() {
            @Override
            public void onResponse(Call<ProductsResult>call, Response<ProductsResult> response) {

                if(response.isSuccessful()){
                    ProductsResult productsResult = response.body();
                    mListToAdd.clear();
                    mProductsResult = productsResult.getResult();

                    try{
                        Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();
                        int incrementalCounter = (int) productDao.countOf();
                        int productId;
                        for (Product product : mProductsResult) {
                            productId = product.getId();
                            if(!mProductIds.contains(productId)){
                                mProductIds.add(productId);
                                mUniqueProducts.add(product);
                            }
                            if(productDao.queryBuilder().where().eq("id", productId).countOf() == 0){
                                incrementalCounter++;
                                product.setIncrementalCounter(incrementalCounter);
                                mListToAdd.add(product);
                            }
                        }
                        productDao.create(mListToAdd);
                    } catch (SQLException e) {
                        Log.e(LOG_TAG, "Database exception in getProductsPageFromNetwork()", e);
                        e.printStackTrace();
                    }
                }else{
                    Log.e(LOG_TAG, "getProductsPageFromNetwork() - response problem.");
                }

                onProductsPageLoaded(offset, isInitialLoading, mUniqueProducts);

            }

            @Override
            public void onFailure(Call<ProductsResult>call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());

                // Continue work.
                mUniqueProducts.clear();
                onProductsPageLoaded(offset, isInitialLoading, mUniqueProducts);
            }
        });

    }



    private boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable((Context)mContext);
    }

    /**
     * Callback methods. One for first page loading due adapter and RecyclerView initialization.
     * Second for the second and subsequent pages.
     * Also, one method for Search results.
     */
    public interface DataManagerCallbacks {
        void onInitProductsListLoaded(List<Product> products, int offset);
        void onProductsListLoaded(List<Product> products, int offset);
        void onProductsSearchListLoaded(List<Product> products, int offset);
        int getCountOfProductsInAdapter();
    }
}
