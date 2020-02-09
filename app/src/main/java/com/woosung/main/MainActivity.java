package com.woosung.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.preference.PreferenceManager;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.woosung.R;
import com.woosung.about.AboutActivity;
import com.woosung.contacts.SyncUtils;
import com.woosung.messages.MessageActivity;
import com.woosung.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "WoosungCRM";

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;

    private Context mContext;
    private View mLayout;

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;



    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS
    };




    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ContentsPagerAdapter mContentPagerAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mContext = getApplicationContext();
        mLayout = findViewById(R.id.main_layout);




        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        actionBar.setHomeAsUpIndicator(R.mipmap.ic_launcher);
//        actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu_white_24dp);







        mTabLayout = findViewById(R.id.layout_tab);

        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.tab_title_1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.tab_title_2)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.tab_title_3)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.tab_title_4)));

        mViewPager = findViewById(R.id.pager_content);
        mContentPagerAdapter = new ContentsPagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(mContentPagerAdapter);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });





        drawerLayout = findViewById(R.id.dl_main_drawer_root);
        navigationView = findViewById(R.id.nv_main_navigation_root);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences pref = getSharedPreferences("Variable", Activity.MODE_PRIVATE);

        View header = navigationView.getHeaderView(0);
        TextView emplName = header.findViewById(R.id.EmplName);
        TextView deptName = header.findViewById(R.id.DeptName);
        TextView rankName = header.findViewById(R.id.RankName);

        emplName.setText(pref.getString("EMPLNAME", ""));
        deptName.setText(pref.getString("DEPTNAME", ""));
        rankName.setText(pref.getString("RANKNAME", ""));





        // 동기화 할래 안할래?
        if(PreferenceManager
                .getDefaultSharedPreferences(mContext).getBoolean(getString(R.string.contact_sync_key), false)){
            SyncUtils.CreateSyncAccount(mContext);
        }



    }






    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_config :
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;

            case R.id.action_about :
                i = new Intent(this, AboutActivity.class);
                startActivity(i);
                break;

            case R.id.action_message :
                i = new Intent(this, MessageActivity.class);
                startActivity(i);
                break;

            default :
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }







    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if ( requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }



            if ( !check_result ) {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "권한요청이 거부되었습니다. 앱을 다시 실행하여 권한을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                }else {


                    // “다시 묻지 않음”을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "권한요청이 거부되었습니다. 설정(앱 정보)에서 권한을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }


    }







    class ContentsPagerAdapter extends FragmentStatePagerAdapter {
        private int mPageCount;

        private ContentsPagerAdapter(FragmentManager fm, int mPageCount) {
            super(fm);
            this.mPageCount = mPageCount;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return new SalesFragment();
                case 1:
                    return new ActsFragment();
                case 2:
                    return new CustFragment();
                case 3:
                    return new PropFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mPageCount;
        }
    }
}

