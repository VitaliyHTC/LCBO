package com.vitaliyhtc.lcbo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {
    private static final String LOG_TAG = "ShoppingCartAdapter";

    private ProductItemClickCallbacks mContext;
    private List<Product> mProducts;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final ImageView imageView;
        private final TextView countTextView;

        ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.onProductItemDetailsClicked(getAdapterPosition());
                }
            });
            v.findViewById(R.id.image_view_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.onProductItemDeleteClicked(getAdapterPosition());
                }
            });
            titleTextView = (TextView) v.findViewById(R.id.item_title);
            imageView = (ImageView) v.findViewById(R.id.image_view_product_small);
            countTextView = (TextView) v.findViewById(R.id.item_count);
        }

        TextView getTitleTextView() {
            return titleTextView;
        }
        ImageView getImageView() {
            return imageView;
        }
        TextView getCountTextView() {
            return countTextView;
        }
    }

    public ShoppingCartAdapter(ProductItemClickCallbacks context){
        mContext = context;
        mProducts = new ArrayList<>();
    }

    public void appendToProducts(List<Product> products) {
        mProducts.addAll(products);
    }

    public Product getProductAtPosition(int position) {
        return mProducts.get(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ShoppingCartAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.shopping_cart_item, viewGroup, false);
        return new ShoppingCartAdapter.ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(LOG_TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        Product currentProduct = mProducts.get(position);
        viewHolder.getTitleTextView().setText(currentProduct.getName());
        String itemCount = "Qty: "+((ProductItemCount)mContext).getProductItemCountForId(getProductAtPosition(position).getId());
        viewHolder.getCountTextView().setText(itemCount);
        Picasso.with(((Context)mContext).getApplicationContext())
                .load(currentProduct.getImageThumbUrl())
                .placeholder(R.drawable.list_item_bg)
                .error(R.drawable.ic_broken_image)
                .into(viewHolder.getImageView());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    public void removeAt(int position) {
        mProducts.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mProducts.size());
    }

    public interface ProductItemClickCallbacks{
        void onProductItemDetailsClicked(int position);
        void onProductItemDeleteClicked(int position);
    }
    public interface ProductItemCount{
        int getProductItemCountForId(int id);
    }
}
