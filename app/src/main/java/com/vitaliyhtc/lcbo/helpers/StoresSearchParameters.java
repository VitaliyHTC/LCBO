package com.vitaliyhtc.lcbo.helpers;

public class StoresSearchParameters {

    private String searchStringQuery;

    private boolean hasWheelchairAccessability;
    private boolean hasBilingualServices;
    private boolean hasProductConsultant;
    private boolean hasTastingBar;
    private boolean hasBeerColdRoom;
    private boolean hasSpecialOccasionPermits;
    private boolean hasVintagesCorner;
    private boolean hasParking;
    private boolean hasTransitAccess;

    public StoresSearchParameters() {
        clearAll();
    }

    public void clearAll(){
        searchStringQuery = "";
        hasWheelchairAccessability = false;
        hasBilingualServices = false;
        hasProductConsultant = false;
        hasTastingBar = false;
        hasBeerColdRoom = false;
        hasSpecialOccasionPermits = false;
        hasVintagesCorner = false;
        hasParking = false;
        hasTransitAccess = false;
    }

    public boolean containTrueValues(){
        return hasWheelchairAccessability || hasBilingualServices || hasProductConsultant ||
                hasTastingBar || hasBeerColdRoom || hasSpecialOccasionPermits ||
                hasVintagesCorner || hasParking || hasTransitAccess;
    }



    public String getSearchStringQuery() {
        return searchStringQuery;
    }

    public void setSearchStringQuery(String searchStringQuery) {
        this.searchStringQuery = searchStringQuery;
    }

    public boolean isHasWheelchairAccessability() {
        return hasWheelchairAccessability;
    }

    public void setHasWheelchairAccessability(boolean hasWheelchairAccessability) {
        this.hasWheelchairAccessability = hasWheelchairAccessability;
    }

    public boolean isHasBilingualServices() {
        return hasBilingualServices;
    }

    public void setHasBilingualServices(boolean hasBilingualServices) {
        this.hasBilingualServices = hasBilingualServices;
    }

    public boolean isHasProductConsultant() {
        return hasProductConsultant;
    }

    public void setHasProductConsultant(boolean hasProductConsultant) {
        this.hasProductConsultant = hasProductConsultant;
    }

    public boolean isHasTastingBar() {
        return hasTastingBar;
    }

    public void setHasTastingBar(boolean hasTastingBar) {
        this.hasTastingBar = hasTastingBar;
    }

    public boolean isHasBeerColdRoom() {
        return hasBeerColdRoom;
    }

    public void setHasBeerColdRoom(boolean hasBeerColdRoom) {
        this.hasBeerColdRoom = hasBeerColdRoom;
    }

    public boolean isHasSpecialOccasionPermits() {
        return hasSpecialOccasionPermits;
    }

    public void setHasSpecialOccasionPermits(boolean hasSpecialOccasionPermits) {
        this.hasSpecialOccasionPermits = hasSpecialOccasionPermits;
    }

    public boolean isHasVintagesCorner() {
        return hasVintagesCorner;
    }

    public void setHasVintagesCorner(boolean hasVintagesCorner) {
        this.hasVintagesCorner = hasVintagesCorner;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public void setHasParking(boolean hasParking) {
        this.hasParking = hasParking;
    }

    public boolean isHasTransitAccess() {
        return hasTransitAccess;
    }

    public void setHasTransitAccess(boolean hasTransitAccess) {
        this.hasTransitAccess = hasTransitAccess;
    }
}
