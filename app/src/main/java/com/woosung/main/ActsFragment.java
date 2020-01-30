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


        return rootView;
    }

    private void setDateText(){
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);  //로케일에 맞게 자동
        formatter.setTimeZone(calendar.getTimeZone());
        btnDate.setText( formatter.format(calendar.getTime()));
    }



}
