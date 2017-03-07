package com.vitaliyhtc.lcbo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "favorite_stores")
public class FavoriteStore {

    @DatabaseField(id = true)
    private int id;

    public FavoriteStore() {}

    public FavoriteStore(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
