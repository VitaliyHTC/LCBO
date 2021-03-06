package com.vitaliyhtc.lcbo.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.model.ProductResult;
import com.vitaliyhtc.lcbo.model.ShoppingCart;
import com.vitaliyhtc.lcbo.rest.ApiInterface;
import com.vitaliyhtc.lcbo.rest.RetrofitApiClient;
import com.vitaliyhtc.lcbo.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingCartDataManager {

    private static final String LOG_TAG = ShoppingCartDataManager.class.getSimpleName();

    private Context mContext;

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    private List<Product> mProducts = new ArrayList<>();
    private int mProductsNumberToLoad;
    private int mProductsNumberLoaded;
    private int mTryCounter;



    public ShoppingCartDataManager(Context context) {
        this.mContext = context;
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



    /**
     * Next 4 methods need to be wrapped in AsyncTask.
     */
    /**
     * Need to be wrapped in AsyncTask.
     * Looking for shopping cart in DB, and if cart not found in DB - return new empty cart.
     *
     * @param productId Product ID to find shopping cart
     * @return          ShoppingCart
     */
    public ShoppingCart getShoppingCartByProductId(int productId){
        ShoppingCart shoppingCart = null;
        try {
            Dao<ShoppingCart, Integer> shoppingCartDao = getDatabaseHelper().getShoppingCartDao();

            if(shoppingCartDao.queryBuilder().where().eq("productId", productId).countOf()==1){
                QueryBuilder<ShoppingCart, Integer> queryBuilder = shoppingCartDao.queryBuilder();
                queryBuilder.where().eq("productId", productId);
                shoppingCart = queryBuilder.query().get(0);
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
        if(shoppingCart == null){
            shoppingCart = new ShoppingCart(productId, 0, 0);
        }
        return shoppingCart;
    }

    /**
     * Need to be wrapped in AsyncTask.
     * Save shopping cart to DB.
     *
     * @param shoppingCart  cart to save in DB.
     */
    public void saveShoppingCartToDb(ShoppingCart shoppingCart){
        try {
            Dao<ShoppingCart, Integer> shoppingCartDao = getDatabaseHelper().getShoppingCartDao();

            shoppingCartDao.createOrUpdate(shoppingCart);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
    }

    /**
     * Need to be wrapped in AsyncTask.
     * Remove shopping cart by given ID.
     *
     * @param id    ID of cart, which you like to remove.
     */
    public void removeShoppingCartById(int id){
        try {
            Dao<ShoppingCart, Integer> shoppingCartDao = getDatabaseHelper().getShoppingCartDao();

            shoppingCartDao.deleteById(id);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
    }

    /**
     * Need to be wrapped in AsyncTask.
     * Retrieve all shopping carts that presented in DB.
     *
     * @return  all shopping carts stored in DB.
     */
    public List<ShoppingCart> getAllShoppingCartsFromDb(){
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        try {
            Dao<ShoppingCart, Integer> shoppingCartDao = getDatabaseHelper().getShoppingCartDao();

            shoppingCarts.addAll(shoppingCartDao.queryForAll());
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
        return shoppingCarts;
    }



    public void LoadProductsByIds(List<Integer> idsList){
        mProductsNumberToLoad = idsList.size();
        for (Integer productId : idsList) {
            getProductById(productId);
        }
    }

    private void getProductById(final int productId) {
        AsyncTask<Void, Void, Product> getProductByIdAsyncTask = new AsyncTask<Void, Void, Product>() {
            @Override
            protected Product doInBackground(Void... params) {
                Product product = null;
                try {
                    Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();
                    // @return The object that has the ID field which equals id or null if no matches.
                    product = productDao.queryForId(productId);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Database exception in getProductById()", e);
                    e.printStackTrace();
                }
                return product;
            }
            @Override
            protected void onPostExecute(Product product) {
                if (product != null) {
                    onGetProductByIdResult(product);
                }
                // try to load from server
                if (product == null) {
                    if (getNetworkAvailability()) {
                        getProductByIdFromServer(productId);
                    }
                }
                mTryCounter++;
                if(mTryCounter == mProductsNumberToLoad && mProductsNumberLoaded < mProductsNumberToLoad){
                    ((DataManagerCallbacks) mContext).onProductsListLoaded(mProducts);
                }
            }
        };
        getProductByIdAsyncTask.execute();
    }

    private void getProductByIdFromServer(int productId) {
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        Call<ProductResult> call = apiService.getOneProduct(productId, Config.LCBO_API_ACCESS_KEY);
        call.enqueue(new Callback<ProductResult>() {
            @Override
            public void onResponse(Call<ProductResult> call, Response<ProductResult> response) {
                Product product=null;

                if (response.isSuccessful()) {
                    ProductResult productResult = response.body();
                    product = productResult.getResult();
                } else {
                    Log.e(LOG_TAG, "getProductByIdFromServer() - response problem.");
                }

                onGetProductByIdResult(product);
            }

            @Override
            public void onFailure(Call<ProductResult> call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());
            }
        });
    }

    private void onGetProductByIdResult(Product product) {
        mProducts.add(product);
        mProductsNumberLoaded++;
        if(mProducts.size() == mProductsNumberToLoad){
            ((DataManagerCallbacks) mContext).onProductsListLoaded(mProducts);
        }
    }



    private boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable(mContext);
    }

    public interface DataManagerCallbacks{
        void onProductsListLoaded(List<Product> products);
    }
}
