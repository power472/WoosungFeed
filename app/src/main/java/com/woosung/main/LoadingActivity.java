package com.woosung.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.woosung.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoadingActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = "WoosungCRM";

    private Context mContext;
    private View mLayout;
    private String telNumber="";
    private String token="";



    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;



    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        mContext = getApplicationContext();
        mLayout = findViewById(R.id.loading_layout);


        // 1. 퍼미션을 가지고 있는지 체크
        int writeContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS);
        int readContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int readPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);





        if ( writeContactsPermission != PackageManager.PERMISSION_GRANTED
                || readContactsPermission != PackageManager.PERMISSION_GRANTED
                || readPhonePermission != PackageManager.PERMISSION_GRANTED) {
            //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.


            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "연락처 동기화와 자동 로그인 인증에 연락처권한과 전화권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자에게 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( LoadingActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);

                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }else{

            sendRegistrationToServer();
        }
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }



            if ( check_result ) {
                // 모든 퍼미션을 허용했다면 시작합니다.
                sendRegistrationToServer();

            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else {

                    // “다시 묻지 않음”을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
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


    private void sendRegistrationToServer() {

        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if(mTelephonyManager != null){
            if(ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){

            }else if(mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_UNKNOWN
                    || mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
                Snackbar.make(mLayout, "유심이 없거나 알 수 없는 유심입니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                }).show();
            }else{
                telNumber = mTelephonyManager.getLine1Number();
                telNumber = telNumber.replace("+82", "0");
            }
        }


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        token = task.getResult().getToken();


                        OkHttpClient client = new OkHttpClient();
                        RequestBody formBody = new FormBody.Builder()
                                .add("tel", telNumber)
                                .add("token", token)
                                .build();
                        Request request = new Request.Builder()
                                .url(getString(R.string.url_token))
                                .post(formBody)
                                .build();

                        client.newCall(request).enqueue(getRegServerCallback);


                    }
                });

    }

    private Callback getRegServerCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.e(TAG, "ERROR Message : " + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String s = "";
            try {

                s = response.body().string();
                JSONArray output1 = (new JSONObject(s)).getJSONObject("contents").getJSONArray("output1");

                if(output1.length()>0){

                    JSONObject row_data = output1.getJSONObject(0).getJSONObject("row_data");

                    SharedPreferences pref = getSharedPreferences("Variable", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("EMPLNAME", row_data.getString("EMPLNAME") );
                    editor.putString("DEPTNAME", row_data.getString("DEPTNAME") );
                    editor.putString("RANKNAME", row_data.getString("RANKNAME") );
                    editor.putString("EMPLCODE", row_data.getString("EMPLCODE") );
                    editor.commit();


                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                    finish();
                }else{
                    Snackbar.make(mLayout, telNumber+"은 인증되지 않은 번호이거나 토큰이 정상적으로 등록되지 않았습니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }


            } catch (JSONException e) {
                Log.e(TAG,s);
                Log.e(TAG,e.getMessage());
                e.printStackTrace();
            }
        }
    };
}
