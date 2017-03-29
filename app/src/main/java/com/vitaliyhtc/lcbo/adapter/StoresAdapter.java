package com.vitaliyhtc.lcbo.adapter;

import android.os.Handler;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.ViewHolder> {
    private static final String LOG_TAG = "StoresAdapter";

    private StoreItemClickCallbacks mStoreItemClickCallbacks;
    private List<Store> mStores;



    public StoresAdapter(StoreItemClickCallbacks storeItemClickCallbacks) {
        mStoreItemClickCallbacks = storeItemClickCallbacks;
        mStores = new ArrayList<>();
    }

    public void appendToStores(List<Store> stores){
        mStores.addAll(stores);
    }

    public Store getStoreAtPosition(int position){
        return mStores.get(position);
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_main_store, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        if(Config.IS_LOG_DEBUG){
            Log.d(LOG_TAG, "Element " + position + " set.");
        }
        Store currentStore = mStores.get(position);
        viewHolder.bind(currentStore);
    }


    @Override
    public int getItemCount() {
        return mStores.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_title) TextView textView;

        ViewHolder(View v) {
            super(v);
            // TODO: 29/03/17 use ButterKnife
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mStoreItemClickCallbacks.onStoreItemClick(getAdapterPosition());
                }
            });
            ButterKnife.bind(this, v);
        }

        public void bind(Store currentStore) {
            textView.setText(currentStore.getIncrementalCounter() + " " + currentStore.getName());
        }
    }

    public interface StoreItemClickCallbacks{
        void onStoreItemClick(int positionInAdapter);
    }
}
