package com.vitaliyhtc.lcbo.helpers;

import android.util.Log;

import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;

public class StoresSearchParameters {
    private static final String LOG_TAG = "StoresSearchParameters";

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

    public boolean isWhereNotEmpty(){
        return (containTrueValues() || (!searchStringQuery.isEmpty()));
    }

    public void configureWhere(Where where){
        try {
            if (!searchStringQuery.isEmpty()) {
                where.like("name", "%" + searchStringQuery + "%");
                where.like("tags", "%" + searchStringQuery + "%");
                where.like("addressLine1", "%" + searchStringQuery + "%");
                where.like("addressLine2", "%" + searchStringQuery + "%");
                where.like("city", "%" + searchStringQuery + "%");
                where.or(5);
                if (containTrueValues()) {
                    where.and();
                }
            }
            if (containTrueValues()) {
                if (isHasWheelchairAccessability()) {
                    where.eq("hasWheelchairAccessability", true);
                    where.and();
                }
                if (isHasBilingualServices()) {
                    where.eq("hasBilingualServices", true);
                    where.and();
                }
                if (isHasProductConsultant()) {
                    where.eq("hasProductConsultant", true);
                    where.and();
                }
                if (isHasTastingBar()) {
                    where.eq("hasTastingBar", true);
                    where.and();
                }
                if (isHasBeerColdRoom()) {
                    where.eq("hasBeerColdRoom", true);
                    where.and();
                }
                if (isHasSpecialOccasionPermits()) {
                    where.eq("hasSpecialOccasionPermits", true);
                    where.and();
                }
                if (isHasVintagesCorner()) {
                    where.eq("hasVintagesCorner", true);
                    where.and();
                }
                if (isHasParking()) {
                    where.eq("hasParking", true);
                    where.and();
                }
                if (isHasTransitAccess()) {
                    where.eq("hasTransitAccess", true);
                    where.and();
                }
                where.eq("isDead", false);
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in getWhere()", e);
            e.printStackTrace();
        }
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
