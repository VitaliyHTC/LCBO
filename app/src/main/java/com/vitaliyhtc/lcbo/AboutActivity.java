package com.vitaliyhtc.lcbo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
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
            TextView versionTextView = (TextView)findViewById(R.id.textViewAppVersion);
            versionTextView.setText(version);
        } catch (PackageManager.NameNotFoundException exception){
            /*log it???*/
        }
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

    public void clearStoresTable(View view){
        getDatabaseHelper().clearStoresTable();
        Toast.makeText(this, "clearStoresTable()", Toast.LENGTH_LONG).show();
    }

    public void clearProductsTable(View view){
        getDatabaseHelper().clearProductsTable();
        Toast.makeText(this, "clearProductsTable()", Toast.LENGTH_LONG).show();
    }
}
