package com.vitaliyhtc.lcbo;

import android.os.Bundle;

import com.vitaliyhtc.lcbo.activity.CoreActivity;

public class FavoritesStoresActivity extends CoreActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_stores_activity);
        initiateUserInterface();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_favorites_stores);
    }

}
