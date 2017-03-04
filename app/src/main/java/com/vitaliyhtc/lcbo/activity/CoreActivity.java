package com.vitaliyhtc.lcbo.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.vitaliyhtc.lcbo.AboutActivity;
import com.vitaliyhtc.lcbo.FavoritesStoresActivity;
import com.vitaliyhtc.lcbo.MainActivity;
import com.vitaliyhtc.lcbo.ProductSearchActivity;
import com.vitaliyhtc.lcbo.ProductsByCategoriesActivity;
import com.vitaliyhtc.lcbo.R;
import com.vitaliyhtc.lcbo.ShoppingCartActivity;

/**
 * Class realize UI initialization functionality that common for
 * many activities. Such as: ActionBar, NavigationDrawer, Menus
 * inflation and so on.
 */
public class CoreActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent=null;

        if (id == R.id.nav_stores) {
            intent = new Intent(this, MainActivity.class);
        } else if(id == R.id.nav_favorites_stores) {
            intent = new Intent(this, FavoritesStoresActivity.class);
        } else if(id == R.id.nav_products_by_categories) {
            intent = new Intent(this, ProductsByCategoriesActivity.class);
        } else if(id == R.id.nav_products_search) {
            intent = new Intent(this, ProductSearchActivity.class);
        } else if(id == R.id.nav_shopping_cart) {
            intent = new Intent(this, ShoppingCartActivity.class);
        } else if(id == R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class);
        }

        if(intent!=null){
            if(!this.getClass().getCanonicalName().equals(intent.getClass().getCanonicalName())){
                startActivity(intent);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Used in onCreate() method for basic UI initialization.
     */
    protected void initiateUserInterface(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void setNavigationViewCheckedItem(int id){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(id);
    }






}
