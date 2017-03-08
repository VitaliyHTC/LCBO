package com.vitaliyhtc.lcbo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.adapter.ShoppingCartAdapter;
import com.vitaliyhtc.lcbo.data.ShoppingCartDataManager;
import com.vitaliyhtc.lcbo.helpers.ShoppingCartDialogCloseListener;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.model.ShoppingCart;
import com.vitaliyhtc.lcbo.util.ShoppingCartDialog;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartActivity extends CoreActivity
        implements ShoppingCartAdapter.ProductItemClickCallbacks, ShoppingCartDialogCloseListener {
    public static final String LOG_TAG = ShoppingCartActivity.class.getSimpleName();

    private ShoppingCartAdapter mShoppingCartAdapter = new ShoppingCartAdapter(this);

    private ShoppingCartDataManager mShoppingCartDataManager = new ShoppingCartDataManager(this);

    private List<ShoppingCart> mShoppingCarts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_cart_activity);
        initiateUserInterface();
        initProductsList();
        calculateTotalPrice();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_shopping_cart);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mShoppingCartDataManager.onDestroy();
    }

    public void initProductsList(){
        List<ShoppingCart> shoppingCarts = mShoppingCartDataManager.getAllShoppingCartsFromDb();
        mShoppingCarts = shoppingCarts;
        List<Integer> idsList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            idsList.add(shoppingCart.getProductId());
        }
        mShoppingCartDataManager.LoadProductsByIds(idsList);
    }

    public void onProductsListLoaded(List<Product> products){
        loadProducts(products);
    }

    private void loadProducts(List<Product> initialProductsList){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mShoppingCartAdapter.appendToProducts(initialProductsList);
        recyclerView.setAdapter(mShoppingCartAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }



    @Override
    public void onProductItemDetailsClicked(int position){
        FragmentManager manager = getSupportFragmentManager();
        ShoppingCartDialog shoppingCartDialog = new ShoppingCartDialog();
        shoppingCartDialog.setContextAndProduct(this, mShoppingCartAdapter.getProductAtPosition(position));
        shoppingCartDialog.show(manager, "ShoppingCartDialog");
    }
    @Override
    public void onProductItemDeleteClicked(int position){
        int productId = mShoppingCartAdapter.getProductAtPosition(position).getId();
        mShoppingCartDataManager.removeShoppingCartById(productId);
        mShoppingCartAdapter.removeAt(position);
        calculateTotalPrice();
    }

    public void handleShoppingCartDialogClose(){
        calculateTotalPrice();
        mShoppingCartAdapter.notifyDataSetChanged();
    }

    public int getProductItemCountForId(int id){
        int count = 0;
        for (ShoppingCart shoppingCart:mShoppingCarts){
            if(shoppingCart.getProductId()==id){
                count = shoppingCart.getCount();
            }
        }
        return count;
    }

    private void calculateTotalPrice(){
        List<ShoppingCart> shoppingCarts = mShoppingCartDataManager.getAllShoppingCartsFromDb();
        mShoppingCarts = shoppingCarts;
        int totalPriceInCents = 0;
        for (ShoppingCart shoppingCart : shoppingCarts) {
            totalPriceInCents += shoppingCart.getCount()*shoppingCart.getPriceInCents();

        }
        Float totalPrice = totalPriceInCents/100f;
        String totalPriceString = ""+totalPrice;
        ((TextView)findViewById(R.id.value_total_price_of_products)).setText(totalPriceString);
    }

}
