package com.vitaliyhtc.lcbo.model;

import com.google.gson.annotations.SerializedName;

public class Pager {

    @SerializedName("records_per_page")
    private int recordsPerPage;
    @SerializedName("total_record_count")
    private int totalRecordCount;
    @SerializedName("current_page_record_count")
    private int currentPageRecordCount;
    @SerializedName("is_first_page")
    private boolean isFirstPage;
    @SerializedName("is_final_page")
    private boolean isFinalPage;
    @SerializedName("current_page")
    private int currentPage;
    @SerializedName("current_page_path")
    private String currentPagePath;
    @SerializedName("next_page")
    private Integer nextPage;
    @SerializedName("next_page_path")
    private String nextPagePath;
    @SerializedName("previous_page")
    private Integer previousPage;
    @SerializedName("previous_page_path")
    private String previousPagePath;
    @SerializedName("total_pages")
    private int totalPages;
    @SerializedName("total_pages_path")
    private String totalPagesPath;



    public Pager(int recordsPerPage, int totalRecordCount, int currentPageRecordCount,
                 boolean isFirstPage, boolean isFinalPage, int currentPage, String currentPagePath,
                 Integer nextPage, String nextPagePath, Integer previousPage, String previousPagePath,
                 int totalPages, String totalPagesPath) {
        this.recordsPerPage = recordsPerPage;
        this.totalRecordCount = totalRecordCount;
        this.currentPageRecordCount = currentPageRecordCount;
        this.isFirstPage = isFirstPage;
        this.isFinalPage = isFinalPage;
        this.currentPage = currentPage;
        this.currentPagePath = currentPagePath;
        this.nextPage = nextPage;
        this.nextPagePath = nextPagePath;
        this.previousPage = previousPage;
        this.previousPagePath = previousPagePath;
        this.totalPages = totalPages;
        this.totalPagesPath = totalPagesPath;
    }


    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public int getTotalRecordCount() {
        return totalRecordCount;
    }

    public void setTotalRecordCount(int totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public int getCurrentPageRecordCount() {
        return currentPageRecordCount;
    }

    public void setCurrentPageRecordCount(int currentPageRecordCount) {
        this.currentPageRecordCount = currentPageRecordCount;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(boolean firstPage) {
        isFirstPage = firstPage;
    }

    public boolean isFinalPage() {
        return isFinalPage;
    }

    public void setFinalPage(boolean finalPage) {
        isFinalPage = finalPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public String getCurrentPagePath() {
        return currentPagePath;
    }

    public void setCurrentPagePath(String currentPagePath) {
        this.currentPagePath = currentPagePath;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }

    public String getNextPagePath() {
        return nextPagePath;
    }

    public void setNextPagePath(String nextPagePath) {
        this.nextPagePath = nextPagePath;
    }

    public Integer getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(Integer previousPage) {
        this.previousPage = previousPage;
    }

    public String getPreviousPagePath() {
        return previousPagePath;
    }

    public void setPreviousPagePath(String previousPagePath) {
        this.previousPagePath = previousPagePath;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public String getTotalPagesPath() {
        return totalPagesPath;
    }

    public void setTotalPagesPath(String totalPagesPath) {
        this.totalPagesPath = totalPagesPath;
    }
}
