package com.vitaliyhtc.lcbo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "shopping_cart")
public class ShoppingCart {

    @DatabaseField(id = true)
    private int productId;

    @DatabaseField
    private int count;

    @DatabaseField
    private int priceInCents;



    public ShoppingCart() {}

    public ShoppingCart(int productId, int count, int priceInCents) {
        this.productId = productId;
        this.count = count;
        this.priceInCents = priceInCents;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPriceInCents() {
        return priceInCents;
    }

    public void setPriceInCents(int priceInCents) {
        this.priceInCents = priceInCents;
    }
}
