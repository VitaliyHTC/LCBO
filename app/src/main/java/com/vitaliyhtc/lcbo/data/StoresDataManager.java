package com.vitaliyhtc.lcbo.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.helpers.StoresSearchParameters;
import com.vitaliyhtc.lcbo.model.Store;
import com.vitaliyhtc.lcbo.model.StoresResult;
import com.vitaliyhtc.lcbo.rest.ApiInterface;
import com.vitaliyhtc.lcbo.rest.RetrofitApiClient;
import com.vitaliyhtc.lcbo.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * StoresDataManager provides Stores data.
 * Load data from network or database, depending on network connection availability
 * and which data also saved to database.
 */
public class StoresDataManager {

    private static final String LOG_TAG = StoresDataManager.class.getSimpleName();

    private DataManagerCallbacks dataManagerCallbacks;
    private Context context;

    private List<Store> mStoresResult = new ArrayList<>();
    private List<Store> mListToAdd = new ArrayList<>();

    private StoresSearchParameters mStoresSearchParameters;

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    /**
     * 
     * @param dataManagerCallbacks  - implementation of DataManagerCallbacks
     * @param context               - Application Context
     */
    public StoresDataManager(DataManagerCallbacks dataManagerCallbacks, Context context) {
        this.dataManagerCallbacks = dataManagerCallbacks;
        this.context = context;
    }

    public void init(){
        if (Config.LCBO_API_ACCESS_KEY.isEmpty()) {
            Toast.makeText(context, "Please obtain your API ACCESS_KEY first from lcboapi.com", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * You'll need this in your class to release the helper when done.
     */
    public void onDestroy(){
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }
    }

    /**
     * You'll need this in your class to get the helper from the manager once per class.
     */
    private DatabaseHelper getDatabaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }

    private long getCountOfStoresInDatabase(){
        long storedInDatabaseCounter = 0;
        try {
            storedInDatabaseCounter = getDatabaseHelper().getStoreDao().countOf();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in getCountOfStoresInDatabase()", e);
            e.printStackTrace();
        }
        return storedInDatabaseCounter;
    }



    /**
     * Called when MainActivity in search state.
     *
     * @param storesSearchParameters    set string query and boolean flags for search.
     */
    public void performStoresSearch(StoresSearchParameters storesSearchParameters){
        mStoresSearchParameters = storesSearchParameters;

        if (getNetworkAvailability()) {
            AsyncTask<Void, Void, Integer> nonameAsyncTask = new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    long storedInDatabaseCounter = getCountOfStoresInDatabase();
                    return (int) storedInDatabaseCounter/Config.STORES_PER_PAGE;
                }
                @Override
                protected void onPostExecute(Integer pagesLoaded) {
                    Toast.makeText(context, "loadAndSaveStores() :: Loading started.", Toast.LENGTH_SHORT).show();
                    loadAndSaveStores(pagesLoaded+1);
                }
            };
            nonameAsyncTask.execute();
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
                    mListToAdd.clear();
                    mStoresResult = storesResult.getResult();
                    final int storesPage = storesResult.getPager().getCurrentPage();

                    final List<Store> storesToDb = new ArrayList<>();
                    storesToDb.addAll(mStoresResult);

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();

                                int i = 0;
                                int incrementalCounter;
                                int storeId;
                                for (Store store : storesToDb) {
                                    storeId = store.getId();
                                    if(storeDao.queryBuilder().where().eq("id", storeId).countOf() == 0){
                                        // Why storesPage-1 ? On first page elements numbers starts from 0!
                                        incrementalCounter = (storesPage-1)*Config.STORES_PER_PAGE + i;
                                        store.setIncrementalCounter(incrementalCounter);
                                        i++;
                                        mListToAdd.add(store);
                                    }
                                }
                                storeDao.create(mListToAdd);
                            } catch (SQLException e) {
                                Log.e(LOG_TAG, "Database exception", e);
                                e.printStackTrace();
                            }
                        }
                    });
                    //Toast.makeText(dataManagerCallbacks, "loadAndSaveStores() :: Page: "+storesPage, Toast.LENGTH_SHORT).show();
                }else{
                    Log.e(LOG_TAG, "loadAndSaveStores() - response problem.");
                }

                // if mStoresResult contains stores perform new request for more stores.
                // if mStoresResult isEmpty - perform search in DB.
                if(!mStoresResult.isEmpty()){
                    int newOffset = offset+1;
                    loadAndSaveStores(newOffset);
                } else {
                    Toast.makeText(context, "loadAndSaveStores() :: Loading finished.", Toast.LENGTH_SHORT).show();
                    getSearchStoresPage(1);
                }

            }

            @Override
            public void onFailure(Call<StoresResult>call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());

                // Continue work.
                getSearchStoresPage(1);
            }
        });

    }



    public void getSearchStoresPage(final int offset){
        mStoresResult.clear();

        AsyncTask<Void, Void, List<Store>> getSearchStoresPageAsyncTask = new AsyncTask<Void, Void, List<Store>>() {
            @Override
            protected List<Store> doInBackground(Void... params) {
                try{
                    Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();
                    long storesPerPage = Config.STORES_PER_PAGE;

                    // See: http://ormlite.com/javadoc/ormlite-core/com/j256/ormlite/stmt/QueryBuilder.html
                    long startRow = (offset - 1) * storesPerPage;
                    QueryBuilder<Store, Integer> queryBuilder = storeDao.queryBuilder();

                    Where where = null;
                    boolean containsTrueValues = mStoresSearchParameters.containTrueValues();
                    String searchQuery = mStoresSearchParameters.getSearchStringQuery();

                    if(containsTrueValues || (!searchQuery.isEmpty())){
                        where = queryBuilder.where();
                    }
                    if(!searchQuery.isEmpty()){
                        where.like("name", "%"+searchQuery+"%");
                        where.like("tags", "%"+searchQuery+"%");
                        where.like("addressLine1", "%"+searchQuery+"%");
                        where.like("addressLine2", "%"+searchQuery+"%");
                        where.like("city", "%"+searchQuery+"%");
                        where.or(5);
                        if(containsTrueValues){
                            where.and();
                        }
                    }

                    if(containsTrueValues){
                        if(mStoresSearchParameters.isHasWheelchairAccessability()){
                            where.eq("hasWheelchairAccessability", true);
                            where.and();
                        }
                        if(mStoresSearchParameters.isHasBilingualServices()){
                            where.eq("hasBilingualServices", true);
                            where.and();
                        }
                        if(mStoresSearchParameters.isHasProductConsultant()){
                            where.eq("hasProductConsultant", true);
                            where.and();
                        }
                        if(mStoresSearchParameters.isHasTastingBar()){
                            where.eq("hasTastingBar", true);
                            where.and();
                        }
                        if(mStoresSearchParameters.isHasBeerColdRoom()){
                            where.eq("hasBeerColdRoom", true);
                            where.and();
                        }
                        if(mStoresSearchParameters.isHasSpecialOccasionPermits()){
                            where.eq("hasSpecialOccasionPermits", true);
                            where.and();
                        }
                        if(mStoresSearchParameters.isHasVintagesCorner()){
                            where.eq("hasVintagesCorner", true);
                            where.and();
                        }
                        if(mStoresSearchParameters.isHasParking()){
                            where.eq("hasParking", true);
                            where.and();
                        }
                        if(mStoresSearchParameters.isHasTransitAccess()){
                            where.eq("hasTransitAccess", true);
                            where.and();
                        }
                        where.eq("isDead", false);
                    }

                    queryBuilder.orderBy("incrementalCounter", true);
                    queryBuilder.offset(startRow);
                    queryBuilder.limit(storesPerPage);
                    mStoresResult.addAll(storeDao.query(queryBuilder.prepare()));
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Database exception in performStoresSearch()", e);
                    e.printStackTrace();
                }
                return mStoresResult;
            }
            @Override
            protected void onPostExecute(List<Store> stores) {
                Toast.makeText(context, "Search. Load from DataBase.", Toast.LENGTH_SHORT).show();

                dataManagerCallbacks.onStoresSearchListLoaded(stores, offset);
            }
        };
        getSearchStoresPageAsyncTask.execute();
    }



    /**
     * Consumer must implement StoresDataManager.DataManagerCallbacks interface
     * for accepting of result. Method call one of two callback methods depending on
     * {@code isInitialLoading} flag: {@code onInitStoresListLoaded} or {@code onStoresListLoaded}.
     *
     *
     *
     * @param offset            set from which page load Stores
     * @param isInitialLoading  set true - if loading first page, for initialization of adapter
     *                          and RecyclerView. Set false - otherwise (adapter and RecyclerView
     *                          initiated, loading second and subsequent pages).
     *                          Describe which callback method to call.
     */
    public void getStoresPage(final int offset, final boolean isInitialLoading){
        mStoresResult.clear();

        AsyncTask<Void, Void, Integer> getStoresPageAsyncTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int storedInDatabaseCounter = 0;
                try {
                    Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();
                    storedInDatabaseCounter = (int)storeDao.countOf();
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Database exception in getStoresPageAsyncTask()", e);
                    e.printStackTrace();
                }
                return storedInDatabaseCounter;
            }
            @Override
            protected void onPostExecute(Integer storedInDatabaseCounter) {
                if(dataManagerCallbacks.getCountOfStoresInAdapter() < storedInDatabaseCounter){
                    getStoresPageFromDb(offset, isInitialLoading);
                } else if (getNetworkAvailability()) {
                    Toast.makeText(context, "Load from Network", Toast.LENGTH_SHORT).show();
                    getStoresPageFromNetwork(offset, isInitialLoading);
                } else {
                    Toast.makeText(context, ":( We no have more data in database, and no internet connection!", Toast.LENGTH_LONG).show();
                    onStoresPageLoaded(offset, isInitialLoading, mStoresResult);
                }
            }
        };
        getStoresPageAsyncTask.execute();
    }

    private void getStoresPageFromDb(final int offset, final boolean isInitialLoading){
        AsyncTask<Void, Void, List<Store>> getStoresPageFromDbAsyncTask = new AsyncTask<Void, Void, List<Store>>() {
            @Override
            protected List<Store> doInBackground(Void... params) {
                List<Store> stores = new ArrayList<>();
                try {
                    Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();
                    // See: http://ormlite.com/javadoc/ormlite-core/com/j256/ormlite/stmt/QueryBuilder.html
                    long startRow = (offset - 1) * Config.STORES_PER_PAGE;
                    QueryBuilder<Store, Integer> queryBuilder = storeDao.queryBuilder();
                    queryBuilder.orderBy("incrementalCounter", true);
                    queryBuilder.offset(startRow);
                    queryBuilder.limit((long)Config.STORES_PER_PAGE);
                    stores.addAll(storeDao.query(queryBuilder.prepare()));
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Database exception in getStoresPageFromDbAsyncTask()", e);
                    e.printStackTrace();
                }
                return stores;
            }
            @Override
            protected void onPostExecute(List<Store> stores) {
                Toast.makeText(context, "Load from DataBase", Toast.LENGTH_SHORT).show();
                onStoresPageLoaded(offset, isInitialLoading, stores);
            }
        };
        getStoresPageFromDbAsyncTask.execute();
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
                    mListToAdd.clear();
                    mStoresResult = storesResult.getResult();
                    final int storesPage = storesResult.getPager().getCurrentPage();

                    final List<Store> storesToDb = new ArrayList<>();
                    storesToDb.addAll(mStoresResult);

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();

                                int i = 0;
                                int incrementalCounter;
                                int storeId;
                                for (Store store : storesToDb) {
                                    storeId = store.getId();
                                    if(storeDao.queryBuilder().where().eq("id", storeId).countOf() == 0){
                                        // Why storesPage-1 ? On first page elements numbers starts from 0!
                                        incrementalCounter = (storesPage-1)*Config.STORES_PER_PAGE + i;
                                        store.setIncrementalCounter(incrementalCounter);
                                        i++;
                                        mListToAdd.add(store);
                                    }
                                }
                                storeDao.create(mListToAdd);
                            } catch (SQLException e) {
                                Log.e(LOG_TAG, "Database exception in getStoresPageFromNetwork()", e);
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    Log.e(LOG_TAG, "getStoresPageFromNetwork() - response problem.");
                }

                onStoresPageLoaded(offset, isInitialLoading, mStoresResult);

            }

            @Override
            public void onFailure(Call<StoresResult>call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());

                // Continue work.
                onStoresPageLoaded(offset, isInitialLoading, mStoresResult);
            }
        });

    }



    private boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable(context);
    }

    /**
     * Callback methods. One for first page loading due adapter and RecyclerView initialization.
     * Second for the second and subsequent pages.
     * Also, one method for Search results.
     */
    public interface DataManagerCallbacks {
        void onInitStoresListLoaded(List<Store> stores, int offset);
        void onStoresListLoaded(List<Store> stores, int offset);
        void onStoresSearchListLoaded(List<Store> stores, int offset);
        int getCountOfStoresInAdapter();
    }
}
