package com.woosung.main;


import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.woosung.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ActsFragment extends Fragment {

    private TextView txtString;
    private Button btnDate;
    private ImageButton btnPrev, btnNext;
    private Calendar calendar=Calendar.getInstance();

    public ActsFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)   {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_acts, container, false);

        btnDate = rootView.findViewById(R.id.btnDate);
        setDateText();
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog( view.getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                calendar.set(year, monthOfYear, dayOfMonth);
                                setDateText();
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();

            }
        });

        btnPrev = rootView.findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.DATE,-1);
                setDateText();
            }
        });

        btnNext = rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.DATE,1);
                setDateText();
            }
        });


        txtString = rootView.findViewById(R.id.DeptName);
        NameTask task = new NameTask();
        task.execute();

        return rootView;
    }

    private void setDateText(){
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);  //로케일에 맞게 자동
        formatter.setTimeZone(calendar.getTimeZone());
        btnDate.setText( formatter.format(calendar.getTime()));
    }







    class NameTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();

            HttpUrl.Builder urlBuilder = HttpUrl.parse(getString(R.string.url_select)).newBuilder();
            urlBuilder.addEncodedQueryParameter("format", "json");
            urlBuilder.addEncodedQueryParameter("sqlfilename", "intro");
            urlBuilder.addEncodedQueryParameter("sqlnumber", "1");
            urlBuilder.addEncodedQueryParameter("no", "0472");
            String requestUrl = urlBuilder.build().toString();
            Request request = new Request.Builder().url(requestUrl).build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String s) {

            try {
                JSONArray output1 = (new JSONObject(s)).getJSONObject("contents").getJSONArray("output1");
                JSONObject row_data = output1.getJSONObject(0).getJSONObject("row_data");
                String name = row_data.getString("EMPLNAME");
                txtString.setText(name+"님 반갑습니다");

            } catch (JSONException e) {
                e.printStackTrace();

            } catch (NullPointerException n) {
                txtString.setText("이름을 가져오지 못했습니다");
            }

        }

    }

}
