package com.vitaliyhtc.lcbo.data;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.data.tasks.StoresSearchPageAsyncTask;
import com.vitaliyhtc.lcbo.data.tasks.StoresPageAsyncTask;
import com.vitaliyhtc.lcbo.data.tasks.StoresPageFromDbAsyncTask;
import com.vitaliyhtc.lcbo.data.tasks.StoresSaveToDbAsyncTask;
import com.vitaliyhtc.lcbo.data.tasks.StoresSearchPageOffsetAsyncTask;
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
        implements StoresDataManager {

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
            new StoresSearchPageOffsetAsyncTask(
                    getDatabaseHelper(),
                    new StoresSearchPageOffsetAsyncTask.ProcessResult() {
                        @Override
                        public void onProcessResult(Integer pagesLoaded) {
                            loadAndSaveStores(pagesLoaded+1);
                        }
                    }).execute();
        } else {
            getSearchStoresPage(1);
        }
    }



    /**
     * @param offset    from which page start load data, including provided number.
     */
    private void loadAndSaveStores(final int offset){
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

                    new StoresSaveToDbAsyncTask(getDatabaseHelper(), mStoresResult, storesPage).execute();
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
        long storesPerPage = Config.STORES_PER_PAGE;
        long startRow = (offset - 1) * storesPerPage;
        new StoresSearchPageAsyncTask(
                getDatabaseHelper(),
                startRow,
                storesPerPage,
                mStoresSearchParameters,
                new StoresSearchPageAsyncTask.ProcessResult() {
                    @Override
                    public void onProcessResult(List<Store> stores) {
                        dataManagerCallbacks.onStoresSearchListLoaded(stores, offset);
                    }
                })
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
        new StoresPageAsyncTask(
                getDatabaseHelper(),
                new StoresPageAsyncTask.ProcessResult() {
                    @Override
                    public void onProcessResult(Integer storedInDatabaseCounter) {
                        onPostGetStoresPageAsyncTask(storedInDatabaseCounter, offset, isInitialLoading);
                    }
                }
        ).execute();
    }

    private void onPostGetStoresPageAsyncTask(int storedInDatabaseCounter, int offset, boolean isInitialLoading){
        if(dataManagerCallbacks.getCountOfStoresInAdapter() < storedInDatabaseCounter){
            getStoresPageFromDb(offset, isInitialLoading);
        } else if (getNetworkAvailability()) {
            getStoresPageFromNetwork(offset, isInitialLoading);
        } else {
            onStoresPageLoaded(offset, isInitialLoading, mStoresResult);
        }
    }

    private void getStoresPageFromDb(final int offset, final boolean isInitialLoading){
        new StoresPageFromDbAsyncTask(
                getDatabaseHelper(),
                offset,
                new StoresPageFromDbAsyncTask.ProcessResult() {
                    @Override
                    public void onProcessResult(List<Store> stores) {
                        onStoresPageLoaded(offset, isInitialLoading, stores);
                    }
                }).execute();
    }

    private void onStoresPageLoaded(int offset, boolean isInitialLoading, List<Store> stores){
        if(isInitialLoading){
            dataManagerCallbacks.onInitStoresListLoaded(stores, offset);
        }else{
            dataManagerCallbacks.onStoresListLoaded(stores, offset);
        }
    }

    private void getStoresPageFromNetwork(final int offset, final boolean isInitialLoading){
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

                    new StoresSaveToDbAsyncTask(getDatabaseHelper(), mStoresResult, storesPage).execute();
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
