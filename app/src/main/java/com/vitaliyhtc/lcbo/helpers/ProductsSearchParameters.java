package com.vitaliyhtc.lcbo.helpers;

public class ProductsSearchParameters {

    private String searchStringQuery;

    private boolean hasValueAddedPromotion;
    private boolean hasLimitedTimeOffer;
    private boolean hasBonusRewardMiles;
    private boolean isSeasonal;
    private boolean isVqa;
    private boolean isOcb;
    private boolean isKosher;

    public ProductsSearchParameters() {
        clearAll();
    }

    public void clearAll(){
        searchStringQuery = "";
        hasValueAddedPromotion = false;
        hasLimitedTimeOffer = false;
        hasBonusRewardMiles = false;
        isSeasonal = false;
        isVqa = false;
        isOcb = false;
        isKosher = false;
    }

    public boolean containTrueValues(){
        return hasValueAddedPromotion || hasLimitedTimeOffer || hasBonusRewardMiles ||
                isSeasonal || isVqa || isOcb || isKosher;
    }

    public String getWhereStringQuery(){
        boolean isFirstInWhereQuery = true;
        StringBuilder whereQuery = new StringBuilder("");
        if(hasValueAddedPromotion){
            isFirstInWhereQuery = false;
            whereQuery.append("has_value_added_promotion");
        }
        if(hasLimitedTimeOffer){
            if(!isFirstInWhereQuery){
                whereQuery.append(",");
            }else{
                isFirstInWhereQuery = false;
            }
            whereQuery.append("has_limited_time_offer");
        }
        if(hasBonusRewardMiles){
            if(!isFirstInWhereQuery){
                whereQuery.append(",");
            }else{
                isFirstInWhereQuery = false;
            }
            whereQuery.append("has_bonus_reward_miles");
        }
        if(isSeasonal){
            if(!isFirstInWhereQuery){
                whereQuery.append(",");
            }else{
                isFirstInWhereQuery = false;
            }
            whereQuery.append("is_seasonal");
        }
        if(isVqa){
            if(!isFirstInWhereQuery){
                whereQuery.append(",");
            }else{
                isFirstInWhereQuery = false;
            }
            whereQuery.append("is_vqa");
        }
        if(isOcb){
            if(!isFirstInWhereQuery){
                whereQuery.append(",");
            }else{
                isFirstInWhereQuery = false;
            }
            whereQuery.append("is_ocb");
        }
        if(isKosher){
            if(!isFirstInWhereQuery){
                whereQuery.append(",");
            }
            whereQuery.append("is_kosher");
        }
        return whereQuery.toString();
    }

    public String getSearchStringQuery() {
        return searchStringQuery;
    }

    public void setSearchStringQuery(String searchStringQuery) {
        this.searchStringQuery = searchStringQuery;
    }

    public boolean isHasValueAddedPromotion() {
        return hasValueAddedPromotion;
    }

    public void setHasValueAddedPromotion(boolean hasValueAddedPromotion) {
        this.hasValueAddedPromotion = hasValueAddedPromotion;
    }

    public boolean isHasLimitedTimeOffer() {
        return hasLimitedTimeOffer;
    }

    public void setHasLimitedTimeOffer(boolean hasLimitedTimeOffer) {
        this.hasLimitedTimeOffer = hasLimitedTimeOffer;
    }

    public boolean isHasBonusRewardMiles() {
        return hasBonusRewardMiles;
    }

    public void setHasBonusRewardMiles(boolean hasBonusRewardMiles) {
        this.hasBonusRewardMiles = hasBonusRewardMiles;
    }

    public boolean isSeasonal() {
        return isSeasonal;
    }

    public void setSeasonal(boolean seasonal) {
        isSeasonal = seasonal;
    }

    public boolean isVqa() {
        return isVqa;
    }

    public void setVqa(boolean vqa) {
        isVqa = vqa;
    }

    public boolean isOcb() {
        return isOcb;
    }

    public void setOcb(boolean ocb) {
        isOcb = ocb;
    }

    public boolean isKosher() {
        return isKosher;
    }

    public void setKosher(boolean kosher) {
        isKosher = kosher;
    }

}
