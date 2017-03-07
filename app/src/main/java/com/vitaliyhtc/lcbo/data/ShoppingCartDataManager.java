package com.vitaliyhtc.lcbo.data;


import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vitaliyhtc.lcbo.model.ShoppingCart;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCartDataManager {

    private static final String LOG_TAG = ShoppingCartDataManager.class.getSimpleName();

    private Context mContext;

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;


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

    public void saveShoppingCartToDb(ShoppingCart shoppingCart){
        try {
            Dao<ShoppingCart, Integer> shoppingCartDao = getDatabaseHelper().getShoppingCartDao();

            shoppingCartDao.createOrUpdate(shoppingCart);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
    }

    public void removeShoppingCartById(int id){
        try {
            Dao<ShoppingCart, Integer> shoppingCartDao = getDatabaseHelper().getShoppingCartDao();

            shoppingCartDao.deleteById(id);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception", e);
            e.printStackTrace();
        }
    }

    public List<ShoppingCart> getAllShopingCartsFromDb(){
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





}
