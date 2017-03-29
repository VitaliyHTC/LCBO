package com.vitaliyhtc.lcbo.data;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.data.StoresAsyncTasks.GetSearchStoresPageAsyncTask;
import com.vitaliyhtc.lcbo.data.StoresAsyncTasks.GetStoresPageAsyncTask;
import com.vitaliyhtc.lcbo.data.StoresAsyncTasks.GetStoresPageFromDbAsyncTask;
import com.vitaliyhtc.lcbo.data.StoresAsyncTasks.StoresSaveToDbAsyncTask;
import com.vitaliyhtc.lcbo.data.StoresAsyncTasks.PerformStoresSearchPageOffsetAsyncTask;
import com.vitaliyhtc.lcbo.data.StoresAsyncTasks.StoresDataManagerAsyncTaskCallbacks;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;
import com.vitaliyhtc.lcbo.model.Store;
import com.vitaliyhtc.lcbo.model.StoresResult;
import com.vitaliyhtc.lcbo.rest.ApiInterface;
import com.vitaliyhtc.lcbo.rest.RetrofitApiClient;
import com.vitaliyhtc.lcbo.util.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * StoresDataManagerImpl provides Stores data.
 * Load data from network or database, depending on network connection availability
 * and which data also saved to database.
 */
public class StoresDataManagerImpl
        implements StoresDataManager,
        StoresDataManagerAsyncTaskCallbacks {

    private static final String LOG_TAG = StoresDataManagerImpl.class.getSimpleName();

    private StoresDataManagerCallbacks dataManagerCallbacks;
    private Context context;

    private List<Store> mStoresResult = new ArrayList<>();

    private StoresSearchParameters mStoresSearchParameters;

    private DatabaseHelper mDatabaseHelper = null;



    public StoresDataManagerImpl(Context context, StoresDataManagerCallbacks dataManagerCallbacks) {
        this.dataManagerCallbacks = dataManagerCallbacks;
        this.context = context;
    }

    @Override
    public void onDestroy(){
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }
    }

    private DatabaseHelper getDatabaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }


    /**
     * Called when MainActivity in search state.
     *
     * @param storesSearchParameters    set string query and boolean flags for search.
     */
    @Override
    public void performStoresSearch(StoresSearchParameters storesSearchParameters){
        mStoresSearchParameters = storesSearchParameters;
        if (getNetworkAvailability()) {
            new PerformStoresSearchPageOffsetAsyncTask(getDatabaseHelper(), this, LOG_TAG).execute();
        } else {
            getSearchStoresPage(1);
        }
    }



    /**
     * @param offset    from which page start load data, including provided number.
     */
    @Override
    public void loadAndSaveStores(final int offset){
        mStoresResult.clear();
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        Call<StoresResult> call = apiService.getStoresResult(offset, Config.STORES_PER_PAGE, Config.LCBO_API_ACCESS_KEY);
        call.enqueue(new Callback<StoresResult>() {
            @Override
            public void onResponse(Call<StoresResult>call, Response<StoresResult> response) {

                if(response.isSuccessful()){
                    StoresResult storesResult = response.body();
                    mStoresResult = storesResult.getResult();
                    final int storesPage = storesResult.getPager().getCurrentPage();

                    new StoresSaveToDbAsyncTask(getDatabaseHelper(), mStoresResult, storesPage, LOG_TAG).execute();
                }else{
                    Log.e(LOG_TAG, "loadAndSaveStores() - response problem.");
                }

                // if mStoresResult contains stores perform new request for more stores.
                // if mStoresResult isEmpty - perform search in DB.
                if(!mStoresResult.isEmpty()){
                    int newOffset = offset+1;
                    loadAndSaveStores(newOffset);
                } else {
                    getSearchStoresPage(1);
                }
            }
            @Override
            public void onFailure(Call<StoresResult>call, Throwable t) {
                Log.e(LOG_TAG, t.toString());
                getSearchStoresPage(1);
            }
        });
    }

    @Override
    public void getSearchStoresPage(final int offset){
        new GetSearchStoresPageAsyncTask(getDatabaseHelper(), offset, mStoresSearchParameters, dataManagerCallbacks, LOG_TAG)
                .execute();
    }



    /**
     * Consumer must implement StoresDataManagerImpl.DataManagerCallbacks interface
     * for accepting of result. Method call one of two callback methods depending on
     * {@code isInitialLoading} flag: {@code onInitStoresListLoaded} or {@code onStoresListLoaded}.
     *
     * @param offset            set from which page load Stores
     * @param isInitialLoading  set true - if loading first page, for initialization of adapter
     *                          and RecyclerView. Set false - otherwise (adapter and RecyclerView
     *                          initiated, loading second and subsequent pages).
     *                          Describe which callback method to call.
     */
    @Override
    public void getStoresPage(final int offset, final boolean isInitialLoading){
        mStoresResult.clear();
        new GetStoresPageAsyncTask(getDatabaseHelper(), offset, isInitialLoading, this, LOG_TAG).execute();
    }

    @Override
    public void onPostGetStoresPageAsyncTask(int storedInDatabaseCounter, int offset, boolean isInitialLoading){
        if(dataManagerCallbacks.getCountOfStoresInAdapter() < storedInDatabaseCounter){
            getStoresPageFromDb(offset, isInitialLoading);
        } else if (getNetworkAvailability()) {
            getStoresPageFromNetwork(offset, isInitialLoading);
        } else {
            onStoresPageLoaded(offset, isInitialLoading, mStoresResult);
        }
    }

    @Override
    public void getStoresPageFromDb(final int offset, final boolean isInitialLoading){
        new GetStoresPageFromDbAsyncTask(getDatabaseHelper(), offset, isInitialLoading, this, LOG_TAG).execute();
    }

    @Override
    public void onStoresPageLoaded(int offset, boolean isInitialLoading, List<Store> stores){
        if(isInitialLoading){
            dataManagerCallbacks.onInitStoresListLoaded(stores, offset);
        }else{
            dataManagerCallbacks.onStoresListLoaded(stores, offset);
        }
    }

    @Override
    public void getStoresPageFromNetwork(final int offset, final boolean isInitialLoading){
        mStoresResult.clear();
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        Call<StoresResult> call = apiService.getStoresResult(offset, Config.STORES_PER_PAGE, Config.LCBO_API_ACCESS_KEY);
        call.enqueue(new Callback<StoresResult>() {
            @Override
            public void onResponse(Call<StoresResult>call, Response<StoresResult> response) {
                if(response.isSuccessful()){
                    StoresResult storesResult = response.body();
                    mStoresResult = storesResult.getResult();
                    final int storesPage = storesResult.getPager().getCurrentPage();

                    new StoresSaveToDbAsyncTask(getDatabaseHelper(), mStoresResult, storesPage, LOG_TAG).execute();
                }else{
                    Log.e(LOG_TAG, "getStoresPageFromNetwork() - response problem.");
                }
                onStoresPageLoaded(offset, isInitialLoading, mStoresResult);
            }
            @Override
            public void onFailure(Call<StoresResult>call, Throwable t) {
                Log.e(LOG_TAG, t.toString());
                onStoresPageLoaded(offset, isInitialLoading, mStoresResult);
            }
        });
    }



    private boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable(context);
    }

}
