package com.vitaliyhtc.lcbo.adapter;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vitaliyhtc.lcbo.MainActivity;
import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.model.Store;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide views to RecyclerView with data from mStores.
 */
public class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.ViewHolder>  {
    private static final String TAG = "StoresAdapter";

    private static MainActivity sContext;
    private List<Store> mStores;

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
                    Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                    makeToastForItemOnClick("Element " + getAdapterPosition() + " clicked.");
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
    public StoresAdapter(MainActivity context) {
        sContext = context;
        mStores = new ArrayList<>();
    }

    public void appendToStores(List<Store> stores){
        mStores.addAll(stores);
    }

    public void clearStoresList(){
        final int oldItemsCount = getItemCount();
        mStores.clear();
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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.main_list_item_store, viewGroup, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        // TODO: change item design.
        Store currentStore = mStores.get(position);
        viewHolder.getTextView().setText(currentStore.getIncrementalCounter() + " " + currentStore.getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mStores.size();
    }



    // TODO: remove after testing complete.
    private static void makeToastForItemOnClick(String message){
        Toast.makeText(sContext, message, Toast.LENGTH_SHORT).show();
    }

}
