package com.vitaliyhtc.lcbo;

import android.os.Bundle;

import com.vitaliyhtc.lcbo.activity.CoreActivity;

public class ProductsByCategoriesActivity extends CoreActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products_by_categories_activity);
        initiateUserInterface();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_products_by_categories);
    }

}
