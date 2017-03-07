package com.vitaliyhtc.lcbo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.adapter.ShoppingCartAdapter;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.data.ShoppingCartDataManager;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.model.ProductResult;
import com.vitaliyhtc.lcbo.model.ShoppingCart;
import com.vitaliyhtc.lcbo.rest.ApiInterface;
import com.vitaliyhtc.lcbo.rest.RetrofitApiClient;
import com.vitaliyhtc.lcbo.util.ShoppingCartDialog;
import com.vitaliyhtc.lcbo.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingCartActivity extends CoreActivity
        implements ShoppingCartAdapter.ProductItemClickCallbacks{
    public static final String LOG_TAG = ShoppingCartActivity.class.getSimpleName();

    private ShoppingCartAdapter mShoppingCartAdapter = new ShoppingCartAdapter(this);

    private ShoppingCartDataManager shoppingCartDataManager = new ShoppingCartDataManager(this);

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    private List<Product> mProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_cart_activity);
        initiateUserInterface();
        initProductsList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_shopping_cart);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        shoppingCartDataManager.onDestroy();
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }
    }

    public void initProductsList(){
        List<ShoppingCart> shoppingCarts = shoppingCartDataManager.getAllShopingCartsFromDb();
        int productId;
        for (ShoppingCart shoppingCart : shoppingCarts) {
            productId = shoppingCart.getProductId();
            getProductById(productId);
        }
        loadProducts(mProducts);
    }

    private void loadProducts(List<Product> initialProductsList){
        RecyclerView mRecycleView = (RecyclerView) findViewById(R.id.recycler_view);
        mShoppingCartAdapter.appendToProducts(initialProductsList);
        mRecycleView.setAdapter(mShoppingCartAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(linearLayoutManager);
    }











    /**
     * You'll need this in your class to get the helper from the manager once per class.
     */
    private DatabaseHelper getDatabaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }



    private void getProductById(int productId) {
        Product product = null;
        try {
            Dao<Product, Integer> productDao = getDatabaseHelper().getProductDao();

            // @return The object that has the ID field which equals id or null if no matches.
            product = productDao.queryForId(productId);
            if (product != null) {
                onGetProductByIdResult(product);
            }

            // try to load from server
            if (product == null) {
                if (getNetworkAvailability()) {
                    getProductByIdFromServer(productId);
                }
            }

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in getProductById()", e);
            e.printStackTrace();
        }
    }

    private void getProductByIdFromServer(int productId) {
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        Call<ProductResult> call = apiService.getOneProduct(productId, Config.LCBO_API_ACCESS_KEY);
        call.enqueue(new Callback<ProductResult>() {
            @Override
            public void onResponse(Call<ProductResult> call, Response<ProductResult> response) {
                Product product = null;

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
    }











    public void onProductItemDetailsClicked(int position){
        FragmentManager manager = getSupportFragmentManager();
        ShoppingCartDialog shoppingCartDialog = new ShoppingCartDialog();
        shoppingCartDialog.setContextAndProduct(this, mShoppingCartAdapter.getProductAtPosition(position));
        shoppingCartDialog.show(manager, "ShoppingCartDialog");
    }
    public void onProductItemDeleteClicked(int position){
        int productId = mShoppingCartAdapter.getProductAtPosition(position).getId();
        shoppingCartDataManager.removeShoppingCartById(productId);
        mShoppingCartAdapter.removeAt(position);
    }

    private boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable(this);
    }

}
