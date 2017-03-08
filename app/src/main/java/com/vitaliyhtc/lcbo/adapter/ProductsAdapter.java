package com.vitaliyhtc.lcbo.adapter;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vitaliyhtc.lcbo.ProductsSearchActivity;
import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {
    private static final String LOG_TAG = "ProductsAdapter";

    private ProductsSearchActivity mContext;
    private List<Product> mProducts;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final ImageView imageView;

        ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.onProductItemDetailsClicked(getAdapterPosition());
                }
            });
            v.findViewById(R.id.cart_image_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.onProductItemCartClicked(getAdapterPosition());
                }
            });
            titleTextView = (TextView) v.findViewById(R.id.item_title);
            imageView = (ImageView) v.findViewById(R.id.image_view);
        }

        TextView getTitleTextView() {
            return titleTextView;
        }
        ImageView getImageView() {
            return imageView;
        }
    }

    public ProductsAdapter(ProductsSearchActivity context){
        mContext = context;
        mProducts = new ArrayList<>();
    }

    public void appendToProducts(List<Product> products) {
        mProducts.addAll(products);
    }

    public Product getProductAtPosition(int position) {
        return mProducts.get(position);
    }

    public void clearProductsList(){
        final int oldItemsCount = getItemCount();
        mProducts.clear();
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                notifyItemRangeRemoved(0, oldItemsCount);
            }
        };
        handler.post(r);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.products_list_item, viewGroup, false);
        return new ProductsAdapter.ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(LOG_TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        Product currentProduct = mProducts.get(position);
        viewHolder.getTitleTextView().setText(currentProduct.getName());
        Picasso.with(mContext.getApplicationContext())
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

    public interface ProductItemClickCallbacks{
        void onProductItemDetailsClicked(int position);
        void onProductItemCartClicked(int position);
    }
}
