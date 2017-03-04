package com.vitaliyhtc.lcbo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StoresResult {

    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("pager")
    private Pager pager;
    @SerializedName("result")
    private List<Store> result;



    public StoresResult(int status, String message, Pager pager, List<Store> result) {
        this.status = status;
        this.message = message;
        this.pager = pager;
        this.result = result;
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

    public List<Store> getResult() {
        return result;
    }

    public void setResult(List<Store> result) {
        this.result = result;
    }
}
