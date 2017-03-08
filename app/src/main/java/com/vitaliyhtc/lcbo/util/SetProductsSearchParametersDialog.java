package com.vitaliyhtc.lcbo.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.helpers.ProductsSearchParameters;

public class SetProductsSearchParametersDialog extends DialogFragment {
    private ProductsSearchParameters mProductsSearchParameters;
    private Context mContext;

    public void setProductsSearchParameters(Context context, ProductsSearchParameters productsSearchParameters){
        mContext = context;
        mProductsSearchParameters = productsSearchParameters;
    }

    public void writeSharedPreferences() {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("ProductsSearchParameters_Setting", 0).edit();
        editor.putBoolean("hasValueAddedPromotion", mProductsSearchParameters.isHasValueAddedPromotion());
        editor.putBoolean("hasLimitedTimeOffer", mProductsSearchParameters.isHasLimitedTimeOffer());
        editor.putBoolean("hasBonusRewardMiles", mProductsSearchParameters.isHasBonusRewardMiles());
        editor.putBoolean("isSeasonal", mProductsSearchParameters.isSeasonal());
        editor.putBoolean("isVqa", mProductsSearchParameters.isVqa());
        editor.putBoolean("isOcb", mProductsSearchParameters.isOcb());
        editor.putBoolean("isKosher", mProductsSearchParameters.isKosher());
        editor.commit();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] optionsNames = {
                "Has Value Added Promotion",
                "Has Limited Time Offer",
                "Has Bonus Reward Miles",
                "Is Seasonal",
                "Is Vqa",
                "Is Ocb",
                "Is Kosher"
        };
        final boolean[] checkedItemsArray = {
                mProductsSearchParameters.isHasValueAddedPromotion(),
                mProductsSearchParameters.isHasLimitedTimeOffer(),
                mProductsSearchParameters.isHasBonusRewardMiles(),
                mProductsSearchParameters.isSeasonal(),
                mProductsSearchParameters.isVqa(),
                mProductsSearchParameters.isOcb(),
                mProductsSearchParameters.isKosher()
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_set_products_search_parameters_title))
                .setMultiChoiceItems(optionsNames, checkedItemsArray, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItemsArray[which] = isChecked;
                    }
                })
                .setPositiveButton(getString(R.string.dialog_ok_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        mProductsSearchParameters.setHasValueAddedPromotion(checkedItemsArray[0]);
                        mProductsSearchParameters.setHasLimitedTimeOffer(checkedItemsArray[1]);
                        mProductsSearchParameters.setHasBonusRewardMiles(checkedItemsArray[2]);
                        mProductsSearchParameters.setSeasonal(checkedItemsArray[3]);
                        mProductsSearchParameters.setVqa(checkedItemsArray[4]);
                        mProductsSearchParameters.setOcb(checkedItemsArray[5]);
                        mProductsSearchParameters.setKosher(checkedItemsArray[6]);

                        writeSharedPreferences();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_reset_all_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mProductsSearchParameters.clearAll();

                        writeSharedPreferences();
                        dialog.cancel();
                    }
                });

        return builder.create();
    }
}
