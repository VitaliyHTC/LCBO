package com.vitaliyhtc.lcbo.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;

public class SetStoresSearchParametersDialog extends DialogFragment {
    private StoresSearchParameters mStoresSearchParameters;
    private Context mContext;

    public void setStoresSearchParameters(Context context, StoresSearchParameters storesSearchParameters){
        mContext = context;
        mStoresSearchParameters = storesSearchParameters;
    }

    public void writeSharedPreferences() {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("StoresSearchParameters_Setting", 0).edit();
        editor.putBoolean("hasWheelchairAccessability", mStoresSearchParameters.isHasWheelchairAccessability());
        editor.putBoolean("hasBilingualServices", mStoresSearchParameters.isHasBilingualServices());
        editor.putBoolean("hasProductConsultant", mStoresSearchParameters.isHasProductConsultant());
        editor.putBoolean("hasTastingBar", mStoresSearchParameters.isHasTastingBar());
        editor.putBoolean("hasBeerColdRoom", mStoresSearchParameters.isHasBeerColdRoom());
        editor.putBoolean("hasSpecialOccasionPermits", mStoresSearchParameters.isHasSpecialOccasionPermits());
        editor.putBoolean("hasVintagesCorner", mStoresSearchParameters.isHasVintagesCorner());
        editor.putBoolean("hasParking", mStoresSearchParameters.isHasParking());
        editor.putBoolean("hasTransitAccess", mStoresSearchParameters.isHasTransitAccess());
        editor.commit();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] optionsNames = {
                "Has Wheelchair Accessability",
                "Has Bilingual Services",
                "Has Product Consultant",
                "Has Tasting Bar",
                "Has Beer Cold Room",
                "Has Special Occasion Permits",
                "Has Vintages Corner",
                "Has Parking",
                "Has Transit Access"
        };
        final boolean[] checkedItemsArray = {
                mStoresSearchParameters.isHasWheelchairAccessability(),
                mStoresSearchParameters.isHasBilingualServices(),
                mStoresSearchParameters.isHasProductConsultant(),
                mStoresSearchParameters.isHasTastingBar(),
                mStoresSearchParameters.isHasBeerColdRoom(),
                mStoresSearchParameters.isHasSpecialOccasionPermits(),
                mStoresSearchParameters.isHasVintagesCorner(),
                mStoresSearchParameters.isHasParking(),
                mStoresSearchParameters.isHasTransitAccess()
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_set_stores_search_parameters_title))
                .setMultiChoiceItems(optionsNames, checkedItemsArray, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                checkedItemsArray[which] = isChecked;
                            }
                        })
                .setPositiveButton(getString(R.string.dialog_ok_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                mStoresSearchParameters.setHasWheelchairAccessability(checkedItemsArray[0]);
                                mStoresSearchParameters.setHasBilingualServices(checkedItemsArray[1]);
                                mStoresSearchParameters.setHasProductConsultant(checkedItemsArray[2]);
                                mStoresSearchParameters.setHasTastingBar(checkedItemsArray[3]);
                                mStoresSearchParameters.setHasBeerColdRoom(checkedItemsArray[4]);
                                mStoresSearchParameters.setHasSpecialOccasionPermits(checkedItemsArray[5]);
                                mStoresSearchParameters.setHasVintagesCorner(checkedItemsArray[6]);
                                mStoresSearchParameters.setHasParking(checkedItemsArray[7]);
                                mStoresSearchParameters.setHasTransitAccess(checkedItemsArray[8]);

                                writeSharedPreferences();
                            }
                        })
                .setNegativeButton(getString(R.string.dialog_reset_all_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mStoresSearchParameters.clearAll();

                        writeSharedPreferences();
                        dialog.cancel();
                    }
                });

        return builder.create();
    }
}
