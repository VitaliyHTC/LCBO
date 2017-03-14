package com.vitaliyhtc.lcbo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.data.ProductsByCategoriesDataManager;
import com.vitaliyhtc.lcbo.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsByCategoriesActivity extends CoreActivity
        implements ProductsByCategoriesDataManager.DataManagerCallbacks,
        ProductsTab.RequestForProductsByCategory {

    private ProductsTab mBeerFragment;
    private ProductsTab mWineFragment;
    private ProductsTab mSpiritsFragment;

    private ProductsByCategoriesDataManager mProductsByCategoriesDataManager;
    private ArrayList<Product> mBeerList;
    private ArrayList<Product> mWineList;
    private ArrayList<Product> mSpiritsList;

    private int mProductsLoaded = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products_by_categories_activity);
        initiateTabFragment();
        initiateUserInterface();

        mProductsByCategoriesDataManager = getProductsByCategoriesDataManager();
        initProductsByCategoriesQueues();
        mProductsByCategoriesDataManager.performInitialLoading();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_products_by_categories);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Call to release resources
        mProductsByCategoriesDataManager.onDestroy();
    }



    /**
     * Lets inflate the very first fragment
     * Here , we are inflating the ProductsTabFragment as the first Fragment
     */
    private void initiateTabFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction mFragmentTransaction = fragmentManager.beginTransaction();
        ProductsTabFragment productsTabFragment = new ProductsTabFragment();
        mFragmentTransaction.replace(R.id.container_view, productsTabFragment).commit();
    }

    public void setProductsTab(ProductsTab productsTab){
        if(productsTab.getCategory() == ProductsTab.TAB_CATEGORY_BEER){
            mBeerFragment = productsTab;
        }
        if(productsTab.getCategory() == ProductsTab.TAB_CATEGORY_WINE){
            mWineFragment = productsTab;
        }
        if(productsTab.getCategory() == ProductsTab.TAB_CATEGORY_SPIRITS){
            mSpiritsFragment = productsTab;
        }

    }

    private ProductsByCategoriesDataManager getProductsByCategoriesDataManager(){
        ProductsByCategoriesDataManager productsByCategoriesDataManager = new ProductsByCategoriesDataManager(this);
        productsByCategoriesDataManager.init();
        return productsByCategoriesDataManager;
    }

    private void initProductsByCategoriesQueues(){
        mBeerList = new ArrayList<>();
        mWineList = new ArrayList<>();
        mSpiritsList = new ArrayList<>();
    }



    @Override
    public void onDataManagerInitialResultLoaded(List<Product> products){
        appendResultToLists(products);

        if(mBeerFragment !=null){
            fireBeerCallback();
        }
        if(mWineFragment !=null){
            fireWineCallback();
        }
        if(mSpiritsFragment !=null){
            fireSpiritsCallback();
        }
    }

    @Override
    public void onDataManagerResultLoaded(List<Product> products, int category){
        appendResultToLists(products);

        if(ProductsTab.TAB_CATEGORY_BEER==category){
            fireBeerCallback();
        }
        if(ProductsTab.TAB_CATEGORY_WINE==category){
            fireWineCallback();
        }
        if(ProductsTab.TAB_CATEGORY_SPIRITS==category){
            fireSpiritsCallback();
        }

    }

    private void appendResultToLists(List<Product> products){
        mProductsLoaded += products.size();
        for (Product product : products) {
            String categoryString = product.getPrimaryCategory();
            if(Config.PRODUCT_CATEGORY_BEER.equals(categoryString)){
                mBeerList.add(product);
            }
            if(Config.PRODUCT_CATEGORY_WINE.equals(categoryString)){
                mWineList.add(product);
            }
            if(Config.PRODUCT_CATEGORY_SPIRITS.equals(categoryString)){
                mSpiritsList.add(product);
            }
        }
    }



    @Override
    public void getNItemsOf(int numberOfItems, int category){
        int itemsLack;

        if(ProductsTab.TAB_CATEGORY_BEER==category){
            if(mBeerList.size()>=Config.PRODUCTS_PER_PAGE){
                fireBeerCallback();
            }else{
                itemsLack = Config.PRODUCTS_PER_PAGE - mBeerList.size();
                mProductsByCategoriesDataManager.getNItemsOf(itemsLack, category);
            }
        }
        if(ProductsTab.TAB_CATEGORY_WINE==category){
            if(mWineList.size()>=Config.PRODUCTS_PER_PAGE){
                fireWineCallback();
            }else{
                itemsLack = Config.PRODUCTS_PER_PAGE - mWineList.size();
                mProductsByCategoriesDataManager.getNItemsOf(itemsLack, category);
            }
        }
        if(ProductsTab.TAB_CATEGORY_SPIRITS==category){
            if(mSpiritsList.size()>=Config.PRODUCTS_PER_PAGE){
                fireSpiritsCallback();
            }else{
                itemsLack = Config.PRODUCTS_PER_PAGE - mSpiritsList.size();
                mProductsByCategoriesDataManager.getNItemsOf(itemsLack, category);
            }
        }

    }



    @Override
    public int getProductsLoaded() {
        return mProductsLoaded;
    }



    private void fireBeerCallback(){
        int listSize = mBeerList.size();
        List<Product> products = new ArrayList<>();
        if(listSize > Config.PRODUCTS_PER_PAGE){
            for (int i = 0; i < Config.PRODUCTS_PER_PAGE; i++) {
                products.add(mBeerList.get(i));
            }
            mBeerList.subList(0, Config.PRODUCTS_PER_PAGE).clear();
        }
        if(listSize <= Config.PRODUCTS_PER_PAGE){
            products.addAll(mBeerList);
            mBeerList.clear();
        }
        mBeerFragment.onProductsListLoaded(products);
    }

    private void fireWineCallback(){
        int listSize = mWineList.size();
        List<Product> products = new ArrayList<>();
        if(listSize > Config.PRODUCTS_PER_PAGE){
            for (int i = 0; i < Config.PRODUCTS_PER_PAGE; i++) {
                products.add(mWineList.get(i));
            }
            mWineList.subList(0, Config.PRODUCTS_PER_PAGE).clear();
        }
        if(listSize <= Config.PRODUCTS_PER_PAGE){
            products.addAll(mWineList);
            mWineList.clear();
        }
        mWineFragment.onProductsListLoaded(products);
    }

    private void fireSpiritsCallback(){
        int listSize = mSpiritsList.size();
        List<Product> products = new ArrayList<>();
        if(listSize > Config.PRODUCTS_PER_PAGE){
            for (int i = 0; i < Config.PRODUCTS_PER_PAGE; i++) {
                products.add(mSpiritsList.get(i));
            }
            mSpiritsList.subList(0, Config.PRODUCTS_PER_PAGE).clear();
        }
        if(listSize <= Config.PRODUCTS_PER_PAGE){
            products.addAll(mSpiritsList);
            mSpiritsList.clear();
        }
        mSpiritsFragment.onProductsListLoaded(products);
    }

}
