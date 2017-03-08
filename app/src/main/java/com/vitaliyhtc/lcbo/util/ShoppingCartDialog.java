package com.vitaliyhtc.lcbo.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.data.ShoppingCartDataManager;
import com.vitaliyhtc.lcbo.helpers.ShoppingCartDialogCloseListener;
import com.vitaliyhtc.lcbo.model.Product;
import com.vitaliyhtc.lcbo.model.ShoppingCart;

public class ShoppingCartDialog extends DialogFragment {
    private Context mContext;
    private Product mProduct;
    private View view;

    private ShoppingCartDataManager shoppingDataManager;

    private ShoppingCart shoppingCart;

    private EditText quantityEditText;

    public void setContextAndProduct(Context context, Product product){
        this.mContext = context;
        this.mProduct = product;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.shopping_cart_dialog_fragment, null);
        builder.setView(view);
        builder.setPositiveButton("Add to cart", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int qty = Integer.parseInt(quantityEditText.getText().toString());
                if(qty>0){
                    shoppingCart.setProductId(mProduct.getId());
                    shoppingCart.setCount(qty);
                    shoppingCart.setPriceInCents(mProduct.getPriceInCents());
                    shoppingDataManager.saveShoppingCartToDb(shoppingCart);
                }else{
                    shoppingDataManager.removeShoppingCartById(mProduct.getId());
                }
                if(mContext instanceof ShoppingCartDialogCloseListener){
                    ((ShoppingCartDialogCloseListener)mContext).handleShoppingCartDialogClose();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do some action(). Nothing. We don't save shoppingCart to DB.

            }
        });
        builder.setCancelable(true);
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        shoppingDataManager = new ShoppingCartDataManager(mContext);

        fillProductData();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        shoppingDataManager.onDestroy();
    }

    private void fillProductData(){

        View v = view;

        shoppingCart = shoppingDataManager.getShoppingCartByProductId(mProduct.getId());

        Picasso.with(mContext.getApplicationContext())
                .load(mProduct.getImageUrl())
                .placeholder(R.drawable.list_item_bg)
                .error(R.drawable.ic_broken_image)
                .into((ImageView) v.findViewById(R.id.big_image_view));

        ((TextView)v.findViewById(R.id.title_text_view)).setText(mProduct.getName());
        float price = mProduct.getPriceInCents()/100f;
        String priceString = ""+price;
        ((TextView)v.findViewById(R.id.product_value_price)).setText(priceString);

        Float totalPrice = mProduct.getPriceInCents()*shoppingCart.getCount()/100f;
        String totalPriceString = ""+totalPrice;
        ((TextView)view.findViewById(R.id.product_value_price_total)).setText(totalPriceString);


        quantityEditText = (EditText)v.findViewById(R.id.editQuantity);
        String shoppingCartCount = ""+shoppingCart.getCount();
        quantityEditText.setText(shoppingCartCount);


        final Button minusButton = (Button) v.findViewById(R.id.qty_minus_button);
        minusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onMinusClick();
            }
        });
        final Button plusButton = (Button) v.findViewById(R.id.qty_plus_button);
        plusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPlusClick();
            }
        });

    }

    private void onMinusClick(){
        int qty = Integer.parseInt(quantityEditText.getText().toString());
        qty--;
        if(qty<0){
            qty=0;
        }
        String qtyString = ""+qty;
        quantityEditText.setText(qtyString);
        calcTotalPrice();
    }
    private void onPlusClick(){
        int qty = Integer.parseInt(quantityEditText.getText().toString());
        qty++;
        String qtyString = ""+qty;
        quantityEditText.setText(qtyString);
        calcTotalPrice();
    }
    private void calcTotalPrice(){
        String totalPriceString;
        int qty = Integer.parseInt(quantityEditText.getText().toString());
        Float totalPrice = (qty * mProduct.getPriceInCents())/100f;
        totalPriceString = ""+totalPrice;
        ((TextView)view.findViewById(R.id.product_value_price_total)).setText(totalPriceString);
    }

}
