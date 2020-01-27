package com.woosung.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import android.util.Log;

import com.woosung.Constants;
import com.woosung.R;
import com.woosung.contacts.ContactManager;
import com.woosung.contacts.SyncUtils;

public class SettingsFragment extends PreferenceFragmentCompat
implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String TAG = "SettingsFragment";


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);


        final SharedPreferences sh = getPreferenceManager().getSharedPreferences() ;
        String lastUpdated = sh.getString(Constants.LAST_UPDATED,"");

        SwitchPreferenceCompat pref = findPreference(getString(R.string.contact_sync_key));
        pref.setSummaryOn(getString(R.string.contact_sync_summary_act)+" "+lastUpdated);

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference connectionPref = findPreference(key);

        if (key.equals(getString(R.string.notice_mode_key))) {
            Log.d(TAG,getString(R.string.notice_mode));

        }else if(key.equals(getString(R.string.contact_sync_key))){
            boolean isSync = sharedPreferences.getBoolean(key,false);
            if(isSync){
                SyncUtils.CreateSyncAccount(getContext());
            }else{
                //연락처 삭제
                ContactManager.deleteAllContacts(getContext());
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                        .putString(Constants.LAST_UPDATED, "").commit();
            }

        }else if (key.equals(Constants.LAST_UPDATED)){
            String lastUpdated = sharedPreferences.getString(key,"");
            SwitchPreferenceCompat pref = findPreference(getString(R.string.contact_sync_key));
            pref.setSummaryOn(getString(R.string.contact_sync_summary_act)+" "+lastUpdated);
        }

    }





    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
