package com.vitaliyhtc.lcbo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.data.ProductsByCategoriesDataManager;
import com.vitaliyhtc.lcbo.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsByCategoriesActivity extends CoreActivity
        implements ProductsByCategoriesDataManager.ProductsByCategoriesDataManagerCallbacks,
        ProductsTab.RequestForProductsByCategory {

    private ProductsTab beerFragment;
    private ProductsTab wineFragment;
    private ProductsTab spiritsFragment;

    private ProductsByCategoriesDataManager productsByCategoriesDataManager;
    private ArrayList<Product> beerList;
    private ArrayList<Product> wineList;
    private ArrayList<Product> spiritsList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products_by_categories_activity);
        initiateTabFragment();
        initiateUserInterface();

        productsByCategoriesDataManager = getProductsByCategoriesDataManager();
        initProductsByCategoriesQueues();
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
        productsByCategoriesDataManager.onDestroy();
    }



    /**
     * Lets inflate the very first fragment
     * Here , we are inflating the ProductsTabFragment as the first Fragment
     */
    private void initiateTabFragment(){
        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        ProductsTabFragment productsTabFragment = new ProductsTabFragment();
        mFragmentTransaction.replace(R.id.containerView, productsTabFragment).commit();
    }

    public void setProductsTab(ProductsTab productsTab){
        if(productsTab.getCategory() == ProductsTab.TAB_CATEGORY_BEER){
            beerFragment = productsTab;
            Toast.makeText(this, beerFragment.getCategoryString()+"tab added", Toast.LENGTH_LONG).show();
        }
        if(productsTab.getCategory() == ProductsTab.TAB_CATEGORY_WINE){
            wineFragment = productsTab;
            Toast.makeText(this, wineFragment.getCategoryString()+"tab added", Toast.LENGTH_LONG).show();
        }
        if(productsTab.getCategory() == ProductsTab.TAB_CATEGORY_SPIRITS){
            spiritsFragment = productsTab;
            Toast.makeText(this, spiritsFragment.getCategoryString()+"tab added", Toast.LENGTH_LONG).show();
        }
    }

    private ProductsByCategoriesDataManager getProductsByCategoriesDataManager(){
        ProductsByCategoriesDataManager productsByCategoriesDataManager = new ProductsByCategoriesDataManager(this);
        productsByCategoriesDataManager.init();
        return productsByCategoriesDataManager;
    }

    private void initProductsByCategoriesQueues(){
        beerList = new ArrayList<>();
        wineList = new ArrayList<>();
        spiritsList = new ArrayList<>();
    }



    @Override
    public void onDataManagerResultLoaded(List<Product> products, int category){

        for (Product product : products) {
            String categoryString = product.getPrimaryCategory();
            if(Config.PRODUCT_CATEGORY_BEER.equals(categoryString)){
                beerList.add(product);
            }
            if(Config.PRODUCT_CATEGORY_WINE.equals(categoryString)){
                wineList.add(product);
            }
            if(Config.PRODUCT_CATEGORY_SPIRITS.equals(categoryString)){
                spiritsList.add(product);
            }
        }

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



    @Override
    public void getNItemsOf(int numberOfItems, int category){
        int itemsLack;

        if(ProductsTab.TAB_CATEGORY_BEER==category){
            if(beerList.size()>=Config.PRODUCTS_PER_PAGE){
                fireBeerCallback();
            }else{
                itemsLack = Config.PRODUCTS_PER_PAGE - beerList.size();
                productsByCategoriesDataManager.getNItemsOf(itemsLack, category);
            }
        }
        if(ProductsTab.TAB_CATEGORY_WINE==category){
            if(wineList.size()>=Config.PRODUCTS_PER_PAGE){
                fireWineCallback();
            }else{
                itemsLack = Config.PRODUCTS_PER_PAGE - wineList.size();
                productsByCategoriesDataManager.getNItemsOf(itemsLack, category);
            }
        }
        if(ProductsTab.TAB_CATEGORY_SPIRITS==category){
            if(spiritsList.size()>=Config.PRODUCTS_PER_PAGE){
                fireSpiritsCallback();
            }else{
                itemsLack = Config.PRODUCTS_PER_PAGE - spiritsList.size();
                productsByCategoriesDataManager.getNItemsOf(itemsLack, category);
            }
        }

    }



    private void fireBeerCallback(){
        int listSize = beerList.size();
        List<Product> products = new ArrayList<>();
        if(listSize > Config.PRODUCTS_PER_PAGE){
            for (int i = 0; i < Config.PRODUCTS_PER_PAGE; i++) {
                products.add(beerList.get(i));
            }
            beerList.subList(0, Config.PRODUCTS_PER_PAGE).clear();
        }
        if(listSize <= Config.PRODUCTS_PER_PAGE){
            products.addAll(beerList);
            beerList.clear();
        }
        beerFragment.onProductsListLoaded(products);
    }

    private void fireWineCallback(){
        int listSize = wineList.size();
        List<Product> products = new ArrayList<>();
        if(listSize > Config.PRODUCTS_PER_PAGE){
            for (int i = 0; i < Config.PRODUCTS_PER_PAGE; i++) {
                products.add(wineList.get(i));
            }
            wineList.subList(0, Config.PRODUCTS_PER_PAGE).clear();
        }
        if(listSize <= Config.PRODUCTS_PER_PAGE){
            products.addAll(wineList);
            wineList.clear();
        }
        wineFragment.onProductsListLoaded(products);
    }

    private void fireSpiritsCallback(){
        int listSize = spiritsList.size();
        List<Product> products = new ArrayList<>();
        if(listSize > Config.PRODUCTS_PER_PAGE){
            for (int i = 0; i < Config.PRODUCTS_PER_PAGE; i++) {
                products.add(spiritsList.get(i));
            }
            spiritsList.subList(0, Config.PRODUCTS_PER_PAGE).clear();
        }
        if(listSize <= Config.PRODUCTS_PER_PAGE){
            products.addAll(spiritsList);
            spiritsList.clear();
        }
        spiritsFragment.onProductsListLoaded(products);
    }

}
