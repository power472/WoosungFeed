package com.woosung.contacts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.woosung.Constants;
import com.woosung.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    /**
     * URL to fetch content from during a sync.
     *
     * <p>This points to the Android Developers Blog. (Side note: We highly recommend reading the
     * Android Developer Blog to stay up to date on the latest Android platform developments!)
     */


    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 30000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 20000;  // 10 seconds



    /**
     * Content resolver, for performing database operations.
     */
    private final AccountManager mAccountManager;
    private final Context mContext;
    private final ContentResolver mContentResolver;



    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mContext = context;
        mAccountManager = AccountManager.get(context);

        Log.d(TAG, "Sync Adapter created.");
    }



    @Override
    public void onPerformSync(Account account, Bundle extras,
                              String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        Log.d(TAG, "Sync Adapter called.");

        final ArrayList<User> users = new ArrayList<User>();

        try {

            String lastUpdated = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Constants.LAST_UPDATED,"");
            String sqlnumber = "1";
            if(!lastUpdated.equals("")) sqlnumber+=",3";

            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(mContext.getString(R.string.url_select)).newBuilder();
            urlBuilder.addEncodedQueryParameter("format", "json");
            urlBuilder.addEncodedQueryParameter("sqlfilename", "contacts");
            urlBuilder.addEncodedQueryParameter("sqlnumber", sqlnumber);
            urlBuilder.addEncodedQueryParameter("last", lastUpdated);




            String requestUrl = urlBuilder.build().toString();
            Request request = new Request.Builder().url(requestUrl).build();

            try {
                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.e(TAG,"연락처 업데이트 에러: "+e.getMessage());
                        Toast.makeText(getContext(),"연착처 동기화하는 중 에러 (+"+e.getMessage()+") 다시 시도해 주십시요.",Toast.LENGTH_LONG).show();

                        call.cancel();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                        try {

                            String rtn = response.body().string();
                            JSONArray output = (new JSONObject(rtn)).getJSONObject("contents").getJSONArray("output1");
                            for (int i = 0; i < output.length(); i++) {
                                users.add(User.valueOf(output.getJSONObject(i).getJSONObject("row_data")));
                            }


                            if(users.size()>0){
                                // update platform contacts.
                                ContactManager.syncContacts(mContext, users);
/* 서버 방화벽 관계로 잠시 중단
                                OkHttpClient client = new OkHttpClient();
                                for (final User user : users) {
                                    if(!user.isDeleted()){

                                        String userId = user.getUserId();
                                        Request request = new Request.Builder().url("http://ica.wsfeed.co.kr/Image/picture/"+userId+".bmp").build();
                                        try {
                                            Response resp = client.newCall(request).execute();
                                            ContactManager.writeDisplayPhoto(mContext, userId, resp.body().bytes());

                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage()+" http://ica.wsfeed.co.kr/Image/picture/"+userId+".bmp");
                                        }
                                    }

                                }
*/
                            }else{
                                Log.i(TAG,"연락처 업데이트 인원 없음");
                            }


                            // 설정창에 최근 업데이트 일시 표시 위해
                            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                                    .putString(Constants.LAST_UPDATED, formater.format(new Date())).commit();



                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }catch(Exception e){
            Log.e(TAG, e.getMessage());
        }












        Log.i(TAG, "Network synchronization complete");

    }

}
