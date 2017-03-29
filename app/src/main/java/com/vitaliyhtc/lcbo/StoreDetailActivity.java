package com.vitaliyhtc.lcbo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;
import com.vitaliyhtc.lcbo.data.FavoriteStoreDataManager;
import com.vitaliyhtc.lcbo.model.FavoriteStore;
import com.vitaliyhtc.lcbo.model.Store;
import com.vitaliyhtc.lcbo.model.StoreResult;
import com.vitaliyhtc.lcbo.rest.ApiInterface;
import com.vitaliyhtc.lcbo.rest.RetrofitApiClient;
import com.vitaliyhtc.lcbo.util.Utils;

import java.sql.SQLException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = StoreDetailActivity.class.getSimpleName();

    public static final int ACTIVITY_MAIN = 380;
    public static final int ACTIVITY_FAVORITES = 381;

    public static final String KEY_TARGET_STORE_ID = "targetStoreId";
    public static final String KEY_ACTIVITY_FIRST = "activityFirst";
    public static final String KEY_TARGET_STORE_NAME = "targetStoreName";

    private static final int REQUEST_PHONE_CALL = 0xB0B;
    private Intent makeCallIntent = null;

    private Store mStore;

    private FavoriteStoreDataManager mFavoriteStoreDataManager = new FavoriteStoreDataManager(this);

    // You'll need this in your class to cache the helper in the class.
    private DatabaseHelper mDatabaseHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_store_detail);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int targetStoreId = getIntent().getExtras().getInt(KEY_TARGET_STORE_ID);
        getStoreById(targetStoreId);
    }

    /**
     * You'll need this in your class to release the helper when done.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }
        mFavoriteStoreDataManager.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent;
                int activityFirst = getIntent().getExtras().getInt(KEY_ACTIVITY_FIRST);
                if (activityFirst == ACTIVITY_MAIN){
                    intent = new Intent(this, MainActivity.class);
                } else if (activityFirst == ACTIVITY_FAVORITES){
                    intent = new Intent(this, FavoritesStoresActivity.class);
                } else {
                    intent = NavUtils.getParentActivityIntent(this);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * You'll need this in your class to get the helper from the manager once per class.
     */
    private DatabaseHelper getDatabaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }

    /**
     * This method provide search of store in DB and on server. May return null if Store not found
     * or no internet connection to get them from server (if it is present on server).
     *
     * Call {@code onGetStoreByIdResult(Store store)} where in store passed Store if it present
     * in DB or on server, or <code>null</code> if no store found in db or server or no internet
     * connection available, and store not loaded into db.
     *
     * @param storeId   target store ID, for store which we need to retrieve
     */
    private void getStoreById(final int storeId) {
        AsyncTask<Integer, Void, Store> getStoreByIdAsyncTask = new AsyncTask<Integer, Void, Store>() {
            @Override
            protected Store doInBackground(Integer... params) {
                Store store = null;
                try {
                    Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();
                    // @return The object that has the ID field which equals id or null if no matches.
                    store = storeDao.queryForId(params[0]);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Database exception in getStoreByIdAsyncTask()", e);
                    e.printStackTrace();
                }
                return store;
            }
            @Override
            protected void onPostExecute(Store store) {
                if (store != null) {
                    onGetStoreByIdResult(store);
                }
                // try to load from server
                if (store == null) {
                    if (getNetworkAvailability()) {
                        getStoreByIdFromServer(storeId);
                    }
                }
            }
        };
        getStoreByIdAsyncTask.execute(storeId);
    }

    private void getStoreByIdFromServer(int storeId) {
        ApiInterface apiService = RetrofitApiClient.getClient().create(ApiInterface.class);

        Call<StoreResult> call = apiService.getOneStore(storeId, Config.LCBO_API_ACCESS_KEY);
        call.enqueue(new Callback<StoreResult>() {
            @Override
            public void onResponse(Call<StoreResult> call, Response<StoreResult> response) {
                Store store = null;

                if (response.isSuccessful()) {
                    StoreResult storeResult = response.body();
                    store = storeResult.getResult();

                    final Store storeSaveToDb = store;
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int storesInDatabaseCount =
                                        (int) getDatabaseHelper().getStoreDao().countOf();
                                Dao<Store, Integer> storeDao = getDatabaseHelper().getStoreDao();
                                if (storeDao.queryBuilder().where().eq("id", storeSaveToDb.getId()).countOf() == 0) {
                                    storeSaveToDb.setIncrementalCounter(storesInDatabaseCount+1);
                                    storeDao.create(storeSaveToDb);
                                }
                            } catch (SQLException e) {
                                Log.e(LOG_TAG, "Database exception in getStoreByIdFromServer", e);
                                e.printStackTrace();
                            }
                        }
                    });

                } else {
                    Log.e(LOG_TAG, "getStoreByIdFromServer() - response problem.");
                }

                onGetStoreByIdResult(store);
            }

            @Override
            public void onFailure(Call<StoreResult> call, Throwable t) {
                // Log error here since request failed
                Log.e(LOG_TAG, t.toString());

                // Continue work.
                onGetStoreByIdResult(null);
            }
        });
    }

    private void onGetStoreByIdResult(Store store) {
        RelativeLayout mainErrorRelativeLayout = (RelativeLayout) findViewById(R.id.main_error_relative_layout);
        LinearLayout mainStoreDetailLinearLayout = (LinearLayout) findViewById(R.id.main_store_detail_linear_layout);
        if (store != null) {
            mainErrorRelativeLayout.setVisibility(View.GONE);
            mainStoreDetailLinearLayout.setVisibility(View.VISIBLE);
            fillStoreDetailByData(store);
        } else {
            //Store not found
            mainErrorRelativeLayout.setVisibility(View.VISIBLE);
            mainStoreDetailLinearLayout.setVisibility(View.GONE);
        }
    }

    private void fillStoreDetailByData(Store store) {

        mStore = store;
        setMakeCallOnClickListener();
        setShowProductsOnClickListener();
        setOpenMapOnClickListener();

        setFavoriteStarState();
        setFavoriteStarOnClickListener();

        setActivityTitle(store.getName());

        ((TextView) findViewById(R.id.store_value_name)).setText(store.getName());
        ((TextView) findViewById(R.id.store_value_tags)).setText(store.getTags());
        String storeId = "" + store.getId();
        ((TextView) findViewById(R.id.store_value_id)).setText(storeId);
        ((TextView) findViewById(R.id.store_value_address_line_1)).setText(store.getAddressLine1());
        ((TextView) findViewById(R.id.store_value_address_line_2)).setText(store.getAddressLine2());
        ((TextView) findViewById(R.id.store_value_city)).setText(store.getCity());
        ((TextView) findViewById(R.id.store_value_postal_code)).setText(store.getPostalCode());
        ((TextView) findViewById(R.id.store_value_telephone)).setText(store.getTelephone());
        ((TextView) findViewById(R.id.store_value_fax)).setText(store.getFax());
        String productsCount = "" + store.getProductsCount();
        ((TextView) findViewById(R.id.store_value_products_count)).setText(productsCount);

        String hasWheelchairAccessability = "" + store.isHasWheelchairAccessability();
        ((TextView) findViewById(R.id.store_value_has_wheelchair_accessability)).setText(hasWheelchairAccessability);
        String hasBilingualServices = "" + store.isHasBilingualServices();
        ((TextView) findViewById(R.id.store_value_has_bilingual_services)).setText(hasBilingualServices);
        String hasProductConsultant = "" + store.isHasProductConsultant();
        ((TextView) findViewById(R.id.store_value_has_product_consultant)).setText(hasProductConsultant);
        String hasTastingBar = "" + store.isHasTastingBar();
        ((TextView) findViewById(R.id.store_value_has_tasting_bar)).setText(hasTastingBar);
        String hasBeerColdRoom = "" + store.isHasBeerColdRoom();
        ((TextView) findViewById(R.id.store_value_has_beer_cold_room)).setText(hasBeerColdRoom);
        String hasSpecialOccasionPermits = "" + store.isHasSpecialOccasionPermits();
        ((TextView) findViewById(R.id.store_value_has_special_occasion_permits)).setText(hasSpecialOccasionPermits);
        String hasVintagesCorner = "" + store.isHasVintagesCorner();
        ((TextView) findViewById(R.id.store_value_has_vintages_corner)).setText(hasVintagesCorner);
        String hasParking = "" + store.isHasParking();
        ((TextView) findViewById(R.id.store_value_has_parking)).setText(hasParking);
        String hasTransitAccess = "" + store.isHasTransitAccess();
        ((TextView) findViewById(R.id.store_value_has_transit_access)).setText(hasTransitAccess);

        ((TextView) findViewById(R.id.store_value_sunday)).setText(
                getTimeStringFromRawNumbers(store.getSundayOpen(), store.getSundayClose()));
        ((TextView) findViewById(R.id.store_value_monday)).setText(
                getTimeStringFromRawNumbers(store.getMondayOpen(), store.getMondayClose()));
        ((TextView) findViewById(R.id.store_value_tuesday)).setText(
                getTimeStringFromRawNumbers(store.getTuesdayOpen(), store.getTuesdayClose()));
        ((TextView) findViewById(R.id.store_value_wednesday)).setText(
                getTimeStringFromRawNumbers(store.getWednesdayOpen(), store.getWednesdayClose()));
        ((TextView) findViewById(R.id.store_value_thursday)).setText(
                getTimeStringFromRawNumbers(store.getThursdayOpen(), store.getThursdayClose()));
        ((TextView) findViewById(R.id.store_value_friday)).setText(
                getTimeStringFromRawNumbers(store.getFridayOpen(), store.getFridayClose()));
        ((TextView) findViewById(R.id.store_value_saturday)).setText(
                getTimeStringFromRawNumbers(store.getSundayOpen(), store.getSaturdayClose()));

        String latitudeLongitude = "" + store.getLatitude() + ", " + store.getLongitude();
        ((TextView) findViewById(R.id.store_value_latitude_longitude)).setText(latitudeLongitude);

    }



    private void setMakeCallOnClickListener() {
        final Button button = (Button) findViewById(R.id.store_make_call_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                makeCallToPhoneOfStore();
            }
        });
    }

    private void makeCallToPhoneOfStore() {
        String phoneNumber = mStore.getTelephone();

        makeCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
        } else {
            startActivity(makeCallIntent);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHONE_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(makeCallIntent!=null){
                        startActivity(makeCallIntent);
                    }
                } else {
                    Toast.makeText(this, "Unable to make call. Permission denied!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    private void setShowProductsOnClickListener(){
        final Button button = (Button) findViewById(R.id.store_show_products_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showProductsOfStore();
            }
        });
    }

    private void showProductsOfStore(){
        Intent intent = new Intent(this, ProductsByStoreActivity.class);
        intent.putExtra(KEY_TARGET_STORE_ID, mStore.getId());
        intent.putExtra(KEY_TARGET_STORE_NAME, mStore.getName());
        startActivity(intent);
    }



    private void setOpenMapOnClickListener(){
        final Button button = (Button) findViewById(R.id.store_open_map_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openMapWithMarkerOfStore();
            }
        });
    }

    private void openMapWithMarkerOfStore(){
        String latitude = Double.toString(mStore.getLatitude());
        String longitude = Double.toString(mStore.getLongitude());
        String encodedLabel = Uri.encode(mStore.getName());
        String zoom = Integer.toString(Config.INITIAL_MAP_ZOOM);

        String geoUri = "geo:"+latitude+","+longitude+"?z="+zoom+"&q="+latitude+","+longitude+"("+encodedLabel+")";

        Intent geoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
        startActivity(geoIntent);
    }



    private void setFavoriteStarOnClickListener(){
        final CheckBox button = (CheckBox) findViewById(R.id.store_favorite_star);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onFavoriteStarClick();
            }
        });
    }

    private void onFavoriteStarClick(){
        AsyncTask<Void, Void, Boolean> onFavoriteStarClickAsyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                int storeId = mStore.getId();
                Boolean isStoreFavorite = mFavoriteStoreDataManager.isStoreFavoriteById(storeId);
                if(isStoreFavorite){
                    mFavoriteStoreDataManager.removeFavoriteStoreById(storeId);
                } else {
                    mFavoriteStoreDataManager.saveFavoriteStoreToDb(new FavoriteStore(storeId));
                }
                return isStoreFavorite;
            }
            @Override
            protected void onPostExecute(Boolean isStoreFavorite) {
                CheckBox favoriteCheckBox = (CheckBox) findViewById(R.id.store_favorite_star);
                if(isStoreFavorite){
                    favoriteCheckBox.setChecked(false);
                } else {
                    favoriteCheckBox.setChecked(true);
                }
            }
        };
        onFavoriteStarClickAsyncTask.execute();
    }

    private void setFavoriteStarState(){
        AsyncTask<Void, Void, Boolean> setFavoriteStarStateAsyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return mFavoriteStoreDataManager.isStoreFavoriteById(mStore.getId());
            }
            @Override
            protected void onPostExecute(Boolean isStoreFavorite) {
                CheckBox favoriteCheckBox = (CheckBox) findViewById(R.id.store_favorite_star);
                if(isStoreFavorite){
                    favoriteCheckBox.setChecked(true);
                } else {
                    favoriteCheckBox.setChecked(false);
                }
            }
        };
        setFavoriteStarStateAsyncTask.execute();
    }



    private String getTimeStringFromRawNumbers(int openTimeRaw, int closeTimeRaw){
        String result;
        if(Config.IS_24_HOURS_FORMAT){
            result = Utils.rawTimeTo24String(openTimeRaw)+" - "+Utils.rawTimeTo24String(closeTimeRaw);
        }else{
            result = Utils.rawTimeTo12String(openTimeRaw)+" - "+Utils.rawTimeTo12String(closeTimeRaw);
        }
        return result;
    }

    private void setActivityTitle(String title){
        this.setTitle(title);
    }

    private boolean getNetworkAvailability() {
        return Utils.isNetworkAvailable(this);
    }
}
