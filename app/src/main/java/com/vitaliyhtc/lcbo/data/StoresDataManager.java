package com.vitaliyhtc.lcbo.data;

import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.vitaliyhtc.lcbo.Config;
import com.vitaliyhtc.lcbo.MainActivity;
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

    private MainActivity mContext;

    private List<Store> mStoresResult = new ArrayList<>();
    private List<Store> mListToAdd = new ArrayList<>();

    private StoresSearchParameters mStoresSearchParameters;

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    /**
     * @param context MainActivity context
     */
    public StoresDataManager(MainActivity context) {
        this.mContext = context;
    }

    public void init(){
        if (Config.LCBO_API_ACCESS_KEY.isEmpty()) {
            Toast.makeText(mContext, "Please obtain your API ACCESS_KEY first from lcboapi.com", Toast.LENGTH_LONG).show();
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
            mDatabaseHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class);
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
            long storedInDatabaseCounter = getCountOfStoresInDatabase();
            int pagesLoaded = (int) storedInDatabaseCounter/Config.STORES_PER_PAGE;

            Toast.makeText(mContext, "loadAndSaveStores() :: from page: "+pagesLoaded, Toast.LENGTH_SHORT).show();

            loadAndSaveStores(pagesLoaded+1);
        } else {
            getSearchStoresPage(1);
        }
    }

    /**
     * TODO: write javadoc for loadAndSaveStores().
     *
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
                    int storesPage = storesResult.getPager().getCurrentPage();

                    try{
                        Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();

                        int i = 0;
                        int incrementalCounter;
                        int storeId;
                        for (Store store : mStoresResult) {
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

                    //Toast.makeText(mContext, "loadAndSaveStores() :: Page: "+storesPage, Toast.LENGTH_SHORT).show();
                }else{
                    Log.e(LOG_TAG, "loadAndSaveStores() - response problem.");
                }

                // if mStoresResult contains stores perform new request for more stores.
                // if mStoresResult isEmpty - perform search in DB.
                if(!mStoresResult.isEmpty()){
                    int newOffset = offset+1;
                    loadAndSaveStores(newOffset);
                } else {
                    Toast.makeText(mContext, "loadAndSaveStores() :: loading finished.", Toast.LENGTH_SHORT).show();
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



    public void getSearchStoresPage(int offset){
        mStoresResult.clear();
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

            Toast.makeText(mContext, "Search. Load from DataBase.", Toast.LENGTH_SHORT).show();

            mContext.onStoresSearchListLoaded(mStoresResult, offset);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in performStoresSearch()", e);
            e.printStackTrace();
        }
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
        long storedInDatabaseCounter = 0;
        long storesPerPage = Config.STORES_PER_PAGE;

        try{
            Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();
            storedInDatabaseCounter = storeDao.countOf();

            if(mContext.getCountOfStoresInAdapter() < storedInDatabaseCounter){
                // See: http://ormlite.com/javadoc/ormlite-core/com/j256/ormlite/stmt/QueryBuilder.html
                long startRow = (offset - 1) * storesPerPage;
                QueryBuilder<Store, Integer> queryBuilder = storeDao.queryBuilder();
                queryBuilder.orderBy("incrementalCounter", true);
                queryBuilder.offset(startRow);
                queryBuilder.limit(storesPerPage);
                mStoresResult.addAll(storeDao.query(queryBuilder.prepare()));

                Toast.makeText(mContext, "Load from DataBase", Toast.LENGTH_SHORT).show();
                onStoresPageLoaded(offset, isInitialLoading, mStoresResult);
            } else if (getNetworkAvailability()) {
                Toast.makeText(mContext, "Load from Network", Toast.LENGTH_SHORT).show();
                getStoresPageFromNetwork(offset, isInitialLoading);
            } else {
                Toast.makeText(mContext, ":( We no have more data in database, and no internet connection!", Toast.LENGTH_LONG).show();
                onStoresPageLoaded(offset, isInitialLoading, mStoresResult);
            }

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Database exception in getStoresPage()", e);
            e.printStackTrace();
        }
    }

    private void onStoresPageLoaded(int offset, boolean isInitialLoading, List<Store> stores){
        if(isInitialLoading){
            mContext.onInitStoresListLoaded(stores, offset);
        }else{
            mContext.onStoresListLoaded(stores, offset);
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
                    int storesPage = storesResult.getPager().getCurrentPage();

                    try{
                        Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();

                        int i = 0;
                        int incrementalCounter;
                        int storeId = 0;
                        for (Store store : mStoresResult) {
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
        return Utils.isNetworkAvailable(mContext);
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
