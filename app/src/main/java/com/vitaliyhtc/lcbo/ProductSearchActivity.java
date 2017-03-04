package com.vitaliyhtc.lcbo;

import android.os.Bundle;

import com.vitaliyhtc.lcbo.activity.CoreActivity;

public class ProductSearchActivity extends CoreActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products_search_activity);
        initiateUserInterface();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_products_search);
    }

}
