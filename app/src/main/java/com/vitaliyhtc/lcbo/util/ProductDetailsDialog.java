package com.vitaliyhtc.lcbo.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.model.Product;

public class ProductDetailsDialog extends DialogFragment {
    private Context mContext;
    private Product mProduct;
    private View view;

    public void setContextAndProduct(Context mContext, Product mProduct) {
        this.mContext = mContext;
        this.mProduct = mProduct;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.products_details_dialog_fragment, null);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        fillProductData();
    }

    private void fillProductData(){

        View v = view;

        Picasso.with(mContext.getApplicationContext())
                .load(mProduct.getImageUrl())
                .placeholder(R.drawable.list_item_bg)
                .error(R.drawable.ic_broken_image)
                .into((ImageView) v.findViewById(R.id.big_image_view));


        ((TextView)v.findViewById(R.id.product_value_name)).setText(mProduct.getName());
        ((TextView)v.findViewById(R.id.product_value_tags)).setText(mProduct.getTags());
        String id = ""+mProduct.getId();
        ((TextView)v.findViewById(R.id.product_value_id)).setText(id);
        String priceInCents = ""+mProduct.getPriceInCents();
        ((TextView)v.findViewById(R.id.product_value_price_in_cents)).setText(priceInCents);
        String regularPriceInCents = ""+mProduct.getRegularPriceInCents();
        ((TextView)v.findViewById(R.id.product_value_regular_price_in_cents)).setText(regularPriceInCents);
        String limitedTimeOfferSavingsInCents = ""+mProduct.getLimitedTimeOfferSavingsInCents();
        ((TextView)v.findViewById(R.id.product_value_limited_time_offer_savings_in_cents)).setText(limitedTimeOfferSavingsInCents);
        ((TextView)v.findViewById(R.id.product_value_limited_time_offer_ends_on)).setText(mProduct.getLimitedTimeOfferEndsOn());
        String bonusRewardMiles = ""+mProduct.getBonusRewardMiles();
        ((TextView)v.findViewById(R.id.product_value_bonus_reward_miles)).setText(bonusRewardMiles);
        ((TextView)v.findViewById(R.id.product_value_bonus_reward_miles_ends_on)).setText(mProduct.getBonusRewardMilesEndsOn());
        ((TextView)v.findViewById(R.id.product_value_stock_type)).setText(mProduct.getStockType());
        ((TextView)v.findViewById(R.id.product_value_primary_category)).setText(mProduct.getPrimaryCategory());
        ((TextView)v.findViewById(R.id.product_value_secondary_category)).setText(mProduct.getSecondaryCategory());
        ((TextView)v.findViewById(R.id.product_value_origin)).setText(mProduct.getOrigin());
        ((TextView)v.findViewById(R.id.product_value_package)).setText(mProduct.getPackageOfProduct());
        ((TextView)v.findViewById(R.id.product_value_package_unit_type)).setText(mProduct.getPackageUnitType());
        String packageUnitVolumeInMilliliters = ""+mProduct.getPackageUnitVolumeInMilliliters();
        ((TextView)v.findViewById(R.id.product_value_package_unit_volume_in_milliliters)).setText(packageUnitVolumeInMilliliters);
        String totalPackageUnits = ""+mProduct.getTotalPackageUnits();
        ((TextView)v.findViewById(R.id.product_value_total_package_units)).setText(totalPackageUnits);
        String volumeInMilliliters = ""+mProduct.getVolumeInMilliliters();
        ((TextView)v.findViewById(R.id.product_value_volume_in_milliliters)).setText(volumeInMilliliters);
        String alcoholContent = ""+mProduct.getAlcoholContent();
        ((TextView)v.findViewById(R.id.product_value_alcohol_content)).setText(alcoholContent);
        String pricePerLiterOfAlcoholInCents = ""+mProduct.getPricePerLiterOfAlcoholInCents();
        ((TextView)v.findViewById(R.id.product_value_price_per_liter_of_alcohol_in_cents)).setText(pricePerLiterOfAlcoholInCents);
        String pricePerLiterInCents = ""+mProduct.getPricePerLiterInCents();
        ((TextView)v.findViewById(R.id.product_value_price_per_liter_in_cents)).setText(pricePerLiterInCents);
        ((TextView)v.findViewById(R.id.product_value_sugar_content)).setText(mProduct.getSugarContent());
        ((TextView)v.findViewById(R.id.product_value_producer_name)).setText(mProduct.getProducerName());
        ((TextView)v.findViewById(R.id.product_value_released_on)).setText(mProduct.getReleasedOn());
        String hasValueAddedPromotion = ""+mProduct.isHasValueAddedPromotion();
        ((TextView)v.findViewById(R.id.product_value_has_value_added_promotion)).setText(hasValueAddedPromotion);
        String hasLimitedTimeOffer = ""+mProduct.isHasLimitedTimeOffer();
        ((TextView)v.findViewById(R.id.product_value_has_limited_time_offer)).setText(hasLimitedTimeOffer);
        String hasBonusRewardMiles = ""+mProduct.isHasBonusRewardMiles();
        ((TextView)v.findViewById(R.id.product_value_has_bonus_reward_miles)).setText(hasBonusRewardMiles);
        String isSeasonal = ""+mProduct.isSeasonal();
        ((TextView)v.findViewById(R.id.product_value_is_seasonal)).setText(isSeasonal);
        String isVqa = ""+mProduct.isVqa();
        ((TextView)v.findViewById(R.id.product_value_is_vqa)).setText(isVqa);
        String isOcb = ""+mProduct.isOcb();
        ((TextView)v.findViewById(R.id.product_value_is_ocb)).setText(isOcb);
        String isKosher = ""+mProduct.isKosher();
        ((TextView)v.findViewById(R.id.product_value_is_kosher)).setText(isKosher);
        ((TextView)v.findViewById(R.id.product_value_value_added_promotion_description)).setText(mProduct.getValueAddedPromotionDescription());
        ((TextView)v.findViewById(R.id.product_value_description)).setText(mProduct.getDescription());
        ((TextView)v.findViewById(R.id.product_value_serving_suggestion)).setText(mProduct.getServingSuggestion());
        ((TextView)v.findViewById(R.id.product_value_tasting_note)).setText(mProduct.getTastingNote());
        ((TextView)v.findViewById(R.id.product_value_varietal)).setText(mProduct.getVarietal());
        ((TextView)v.findViewById(R.id.product_value_style)).setText(mProduct.getStyle());
        ((TextView)v.findViewById(R.id.product_value_tertiary_category)).setText(mProduct.getTertiaryCategory());
        ((TextView)v.findViewById(R.id.product_value_sugar_in_grams_per_liter)).setText(mProduct.getSugarInGramsPerLiter());

    }

}
