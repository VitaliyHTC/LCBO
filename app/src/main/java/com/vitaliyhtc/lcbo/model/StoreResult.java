package com.vitaliyhtc.lcbo.model;

import com.google.gson.annotations.SerializedName;

public class StoreResult {

    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("result")
    private Store result;



    public StoreResult(int status, String message, Store result) {
        this.status = status;
        this.message = message;
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

    public Store getResult() {
        return result;
    }

    public void setResult(Store result) {
        this.result = result;
    }
}
