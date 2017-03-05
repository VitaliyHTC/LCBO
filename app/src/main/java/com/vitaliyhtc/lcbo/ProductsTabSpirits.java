package com.vitaliyhtc.lcbo;

import android.os.Bundle;

public class ProductsTabSpirits extends ProductsTab {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCategory(TAB_CATEGORY_SPIRITS);
    }
}
