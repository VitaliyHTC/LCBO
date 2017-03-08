package com.vitaliyhtc.lcbo.data;

import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.ProductsByStoreActivity;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.model.ProductsByStoreResult;
import com.vitaliyhtc.lcbo.rest.ApiInterface;
import com.vitaliyhtc.lcbo.rest.RetrofitApiClient;
import com.vitaliyhtc.lcbo.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsByStoreDataManager {

    private static final String LOG_TAG = ProductsByStoreDataManager.class.getSimpleName();

    private ProductsByStoreActivity mContext;
    private int mTargetStoreId;

    private List<Product> mProductsResult = new ArrayList<>();
    private List<Product> mListToAdd = new ArrayList<>();

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    private int mIncrementalCounter = 0;



    /**
     * @param context MainActivity context
     */
    public ProductsByStoreDataManager(ProductsByStoreActivity context, int targetStoreId) {
        this.mContext = context;
        this.mTargetStoreId = targetStoreId;
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
            try {
                mIncrementalCounter = (int) mDatabaseHelper.getProductDao().countOf();
            } catch (SQLException e) {
                Log.e(LOG_TAG, "Database exception in getDatabaseHelper()", e);
                e.printStackTrace();
            }
        }
        return mDatabaseHelper;
    }



    public void getProductsPage(int offset, boolean isInitialLoading){
        if(getNetworkAvailability()){
            getProductsFromNetwork(offset, isInitialLoading);
        }else{
            Toast.makeText(mContext, ":( We no have internet connection!", Toast.LENGTH_LONG).show();
            mProductsResult.clear();
            onProductsPageLoaded(offset, isInitialLoading, mProductsResult);
        }
    }



    private void getProductsFromNetwork(final int offset, final boolean isInitialLoading){
        mProductsResult.clear();
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        Call<ProductsByStoreResult> call= apiService.getProductsByStore(mTargetStoreId, offset,
                Config.PRODUCTS_PER_PAGE, Config.PRODUCTS_WHERE_NOT, Config.LCBO_API_ACCESS_KEY);
        call.enqueue(new Callback<ProductsByStoreResult>() {
            @Override
            public void onResponse(Call<ProductsByStoreResult>call, Response<ProductsByStoreResult> response) {

                if(response.isSuccessful()){
                    ProductsByStoreResult productsResult = response.body();
                    mListToAdd.clear();
                    mProductsResult = productsResult.getResult();

                    try{
                        Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();

                        int productId;
                        for (Product product : mProductsResult){
                            productId = product.getId();
                            if(productDao.queryBuilder().where().eq("id", productId).countOf()==0){
                                mIncrementalCounter++;
                                product.setIncrementalCounter(mIncrementalCounter);
                                mListToAdd.add(product);
                            }
                        }

                        productDao.create(mListToAdd);
                    } catch (SQLException e) {
                        Log.e(LOG_TAG, "Database exception in getProductsFromNetwork()", e);
                        e.printStackTrace();
                    }
                }else{
                    Log.e(LOG_TAG, "getProductsFromNetwork() - response problem.");
                }

                onProductsPageLoaded(offset, isInitialLoading, mProductsResult);

            }

            @Override
            public void onFailure(Call<ProductsByStoreResult>call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());

                // Continue work.
                mProductsResult.clear();
                onProductsPageLoaded(offset, isInitialLoading, mProductsResult);
            }
        });

    }



    private void onProductsPageLoaded(final int offset, boolean isInitialLoading, final List<Product> productsPage){
        if(isInitialLoading){
            mContext.onInitProductsListLoaded(productsPage, offset);
        }else{
            mContext.onProductsListLoaded(productsPage, offset);
        }
    }



    private boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable(mContext);
    }

    public interface DataManagerCallbacks {
        void onInitProductsListLoaded(List<Product> products, int offset);
        void onProductsListLoaded(List<Product> products, int offset);
        int getCountOfProductsInAdapter();
    }
}
