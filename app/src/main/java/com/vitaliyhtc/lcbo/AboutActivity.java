package com.vitaliyhtc.lcbo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.vitaliyhtc.lcbo.activity.CoreActivity;
import com.vitaliyhtc.lcbo.data.DatabaseHelper;

public class AboutActivity extends CoreActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        initiateUserInterface();

        fillVersionCodeTextView();

        setButtonsListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavigationViewCheckedItem(R.id.nav_about);
    }

    @Override
    protected void onStop() {
        super.onStop();

        releaseDatabaseHelper();
    }

    private void fillVersionCodeTextView(){
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            int versionCode = pInfo.versionCode;
            String version = "v" + versionName + ", Build " + versionCode + ";";
            TextView versionTextView = (TextView)findViewById(R.id.text_view_app_version);
            versionTextView.setText(version);
        } catch (PackageManager.NameNotFoundException exception){
            /*log it???*/
        }
    }











    private void setButtonsListeners(){
        final Button clearStoresTableButton = (Button) findViewById(R.id.button_clear_stores_table);
        final Button clearProductsTableButton = (Button) findViewById(R.id.button_clear_products_table);

        clearStoresTableButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearStoresTable();
            }
        });
        clearProductsTableButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearProductsTable();
            }
        });
    }



    private DatabaseHelper mDatabaseHelper = null;
    private DatabaseHelper getDatabaseHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }
    private void releaseDatabaseHelper(){
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }
    }

    private void clearStoresTable(){
        getDatabaseHelper().clearStoresTable();
        Toast.makeText(this, "clearStoresTable()", Toast.LENGTH_LONG).show();
    }

    private void clearProductsTable(){
        getDatabaseHelper().clearProductsTable();
        Toast.makeText(this, "clearProductsTable()", Toast.LENGTH_LONG).show();
    }
}
