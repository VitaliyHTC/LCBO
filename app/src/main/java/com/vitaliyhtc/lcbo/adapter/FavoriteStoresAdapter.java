package com.vitaliyhtc.lcbo.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.model.Store;

import java.util.ArrayList;
import java.util.List;

public class FavoriteStoresAdapter extends RecyclerView.Adapter<FavoriteStoresAdapter.ViewHolder> {
    private static final String LOG_TAG = "FavoriteStoresAdapter";

    private StoreItemClickCallbacks mContext;
    private List<Store> mStores;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.onStoreItemDetailsClicked(getAdapterPosition());
                }
            });
            v.findViewById(R.id.image_view_remove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.onStoreItemRemoveClicked(getAdapterPosition());
                }
            });
            textView = (TextView) v.findViewById(R.id.item_title);
        }

        TextView getTextView() {
            return textView;
        }
    }

    public FavoriteStoresAdapter(StoreItemClickCallbacks context){
        mContext = context;
        mStores = new ArrayList<>();
    }

    public void appendToStores(List<Store> stores){
        mStores.addAll(stores);
    }

    public Store getStoreAtPosition(int position){
        return mStores.get(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FavoriteStoresAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_favorite_store, viewGroup, false);
        return new FavoriteStoresAdapter.ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FavoriteStoresAdapter.ViewHolder viewHolder, final int position) {
        if(Config.IS_LOG_DEBUG){
            Log.d(LOG_TAG, "Element " + position + " set.");
        }
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        Store currentStore = mStores.get(position);
        viewHolder.getTextView().setText(currentStore.getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mStores.size();
    }

    public void removeAt(int position){
        mStores.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mStores.size());
    }

    public interface StoreItemClickCallbacks{
        void onStoreItemDetailsClicked(int position);
        void onStoreItemRemoveClicked(int position);
    }
}
