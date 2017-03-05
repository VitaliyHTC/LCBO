package com.vitaliyhtc.lcbo.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vitaliyhtc.lcbo.ProductsTab;
import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsByCategoriesAdapter extends RecyclerView.Adapter<ProductsByCategoriesAdapter.ViewHolder> {
    private static final String LOG_TAG = "ProductsByCatsAdapter";

    private static ProductsTab sContext;
    private List<Product> mProducts;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Do some action!
                    //sContext.startStoreDetailActivity(getAdapterPosition());
                }
            });
            textView = (TextView) v.findViewById(R.id.item_title);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    /* *
     * Initialize the dataset of the Adapter.
     *
     * @param stores List<Store> containing the data to populate views to be used by RecyclerView.
     */
    public ProductsByCategoriesAdapter(ProductsTab context){
        sContext = context;
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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.products_list_item, viewGroup, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(LOG_TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        // TODO: change item design.
        Product currentProduct = mProducts.get(position);
        viewHolder.getTextView().setText(currentProduct.getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mProducts.size();
    }
}
