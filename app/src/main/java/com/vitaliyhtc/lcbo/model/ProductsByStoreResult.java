package com.vitaliyhtc.lcbo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductsByStoreResult {

    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("pager")
    private Pager pager;
    @SerializedName("result")
    private List<Product> result;
    @SerializedName("store")
    private Store store;
    @SerializedName("suggestion")
    private String suggestion;



    public ProductsByStoreResult(int status, String message, Pager pager, List<Product> result, Store store, String suggestion) {
        this.status = status;
        this.message = message;
        this.pager = pager;
        this.result = result;
        this.store = store;
        this.suggestion = suggestion;
    }



    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Pager getPager() {
        return pager;
    }

    public void setPager(Pager pager) {
        this.pager = pager;
    }

    public List<Product> getResult() {
        return result;
    }

    public void setResult(List<Product> result) {
        this.result = result;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
