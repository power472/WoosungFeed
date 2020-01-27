package com.woosung.main;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.woosung.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SalesFragment extends Fragment {

    TextView txtString;
    Button btnDate;
    ImageButton btnPrev, btnNext;
    Calendar calendar=Calendar.getInstance();
    ExpandableListView listView;
    ArrayList<SalesGigu> dataList;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private static final String TAG = "WoosungCRM";

    public SalesFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)   {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_sales, container, false);



        listView = (ExpandableListView) rootView.findViewById(R.id.listView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload_Main();
            }
        });



        btnDate = (Button) rootView.findViewById(R.id.btnDate);
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog( view.getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                calendar.set(year, monthOfYear, dayOfMonth);
                                setDateLoad();
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();

            }
        });

        btnPrev = (ImageButton) rootView.findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.DATE,-1);
                setDateLoad();
            }
        });

        btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.DATE,1);
                setDateLoad();
            }
        });

        setDateLoad();
        return rootView;
    }





    //로케일에 맞게 자동날짜형식으로
    private void setDateLoad(){
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.FULL);
        formatter.setTimeZone(calendar.getTimeZone());
        btnDate.setText( formatter.format(calendar.getTime()));

        mSwipeRefreshLayout.setRefreshing(true);
        reload_Main();

    }








    private void reload_Main() {

        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
        String pDate = s.format(calendar.getTime());


        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(getString(R.string.url_sales)).newBuilder();
        urlBuilder.addEncodedQueryParameter("date", pDate);
        String requestUrl = urlBuilder.build().toString();
        Request request = new Request.Builder().url(requestUrl).build();

        try{
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, e.getMessage());
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    try {

                        dataList = new ArrayList<SalesGigu>();

                        SalesGigu jigu=null;
                        SalesJyuk jyuk=null;

                        String rtn = response.body().string();
                        JSONArray output = (new JSONObject(rtn)).getJSONObject("contents").getJSONArray("output");


                        for(int i=0; i< output.length(); i++){
                            JSONObject row = output.getJSONObject(i);
                            if(row.getString("TYPE").equals("A")){
                                jigu = new SalesGigu(
                                    row.getString("CODE"),
                                    row.getString("NAME"),
                                    row.getString("TYPE"),
                                    row.getDouble("CDAMT"),
                                    row.getDouble("CMAMT"),
                                    row.getDouble("PMAMT"),
                                    row.getDouble("LMAMT"),
                                    row.getDouble("EMAMT"),
                                    new ArrayList<SalesJyuk>()
                                    );
                                dataList.add(jigu);
                            }else{
                                jyuk = new SalesJyuk(
                                    row.getString("CODE"),
                                    row.getString("NAME"),
                                    row.getString("TYPE"),
                                    row.getDouble("CDAMT"),
                                    row.getDouble("CMAMT"),
                                    row.getDouble("PMAMT"),
                                    row.getDouble("LMAMT"),
                                    row.getDouble("EMAMT")
                                );

                                jigu.salesJyuks.add(jyuk);
                            }
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SalesListAdapter adapter = new SalesListAdapter(getContext(), dataList);
                                listView.setAdapter(adapter);

                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });





                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    }






    public class SalesListAdapter extends BaseExpandableListAdapter {

        Context mContext;
        ArrayList<SalesGigu> mSalesGigu;


        public SalesListAdapter(Context context, ArrayList<SalesGigu> salesGigu) {
            this.mContext = context;
            this.mSalesGigu = salesGigu;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mSalesGigu.get(groupPosition).salesJyuks.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            View view;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view =inflater.inflate(R.layout.fragment_main_sales_list_item, null);
            } else {
                view = convertView;
            }


            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(mSalesGigu.get(groupPosition).salesJyuks.get(childPosition).name);

            TextView cdamt = (TextView) view.findViewById(R.id.cdamt);
            cdamt.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).salesJyuks.get(childPosition).cdamt));

            TextView cmamt = (TextView) view.findViewById(R.id.cmamt);
            cmamt.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).salesJyuks.get(childPosition).cmamt));

            double amt = Double.valueOf(mSalesGigu.get(groupPosition).salesJyuks.get(childPosition).cmamt);
            amt = amt - Double.valueOf(mSalesGigu.get(groupPosition).salesJyuks.get(childPosition).pmamt);
            TextView pmamt = (TextView) view.findViewById(R.id.pmamt);
            pmamt.setText(String.format("%1$,.0f", amt));
            if(amt<0) pmamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorAccent));
            else pmamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorBaseText));

            TextView pmamt_ = (TextView) view.findViewById(R.id.pmamt_);
            if (pmamt_!=null)
                pmamt_.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).salesJyuks.get(childPosition).pmamt));

            TextView emamt = (TextView) view.findViewById(R.id.emamt);
            if (emamt!=null)
                emamt.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).salesJyuks.get(childPosition).emamt));

            TextView lmamt = (TextView) view.findViewById(R.id.lmamt);
            if (lmamt!=null)
                lmamt.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).salesJyuks.get(childPosition).lmamt));


            return view;
        }




        @Override
        public int getChildrenCount(int groupPosition) {
            return mSalesGigu.get(groupPosition).salesJyuks.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mSalesGigu.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return mSalesGigu.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            View view;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view =inflater.inflate(R.layout.fragment_main_sales_list, null);
            } else {
                view = convertView;
            }


            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(mSalesGigu.get(groupPosition).name);

            TextView cdamt = (TextView) view.findViewById(R.id.cdamt);
            cdamt.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).cdamt));

            TextView cmamt = (TextView) view.findViewById(R.id.cmamt);
            cmamt.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).cmamt));


            TextView pmamt = (TextView) view.findViewById(R.id.pmamt);
            double amt = Double.valueOf(mSalesGigu.get(groupPosition).cmamt);
            amt = amt - Double.valueOf(mSalesGigu.get(groupPosition).pmamt);
            pmamt.setText(String.format("%1$,.0f", amt));


            String sCode=mSalesGigu.get(groupPosition).code;
            if(sCode.equals("0000")||sCode.equals("")){
                view.setBackgroundResource(R.color.colorSecondaryBackground);
                name.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorSecondaryText));
                cdamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorSecondaryText));
                cmamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorSecondaryText));
                if(amt<0) pmamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorAccent));
                else pmamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorSecondaryText));

            }else{
                view.setBackgroundResource(R.color.colorBaseBackground);
                name.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorBaseText));
                cdamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorBaseText));
                cmamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorBaseText));
                if(amt<0) pmamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorAccent));
                else pmamt.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorBaseText));
            }


            TextView pmamt_ = (TextView) view.findViewById(R.id.pmamt_);
            if(pmamt_!=null)
                pmamt_.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).pmamt));

            TextView emamt = (TextView) view.findViewById(R.id.emamt);
            if(emamt!=null)
                emamt.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).emamt));

            TextView lmamt = (TextView) view.findViewById(R.id.lmamt);
            if(lmamt!=null)
                lmamt.setText(String.format("%1$,.0f", mSalesGigu.get(groupPosition).lmamt));


            return view;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return super.areAllItemsEnabled();
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }





    public class SalesGigu{
        @Override
        public String toString() {
            return "SalesGigu{" +
                    "code='" + code + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", cdamt=" + cdamt +
                    ", cmamt=" + cmamt +
                    ", pmamt=" + pmamt +
                    ", lmamt=" + lmamt +
                    ", emamt=" + emamt +
                    ", salesJyuks=" + salesJyuks +
                    '}';
        }

        String code;
        String name;
        String type;
        double cdamt;
        double cmamt;
        double pmamt;
        double lmamt;
        double emamt;
        ArrayList<SalesJyuk> salesJyuks;


        public SalesGigu(String code, String name, String type, double cdamt, double cmamt, double pmamt, double lmamt, double emamt, ArrayList<SalesJyuk> salesJyuks) {
            this.code = code;
            this.name = name;
            this.type = type;
            this.cdamt = cdamt;
            this.cmamt = cmamt;
            this.pmamt = pmamt;
            this.lmamt = lmamt;
            this.emamt = emamt;
            this.salesJyuks = salesJyuks;
        }

    }


    public class SalesJyuk{
        @Override
        public String toString() {
            return "SalesJyuk{" +
                    "code='" + code + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", cdamt=" + cdamt +
                    ", cmamt=" + cmamt +
                    ", pmamt=" + pmamt +
                    ", lmamt=" + lmamt +
                    ", emamt=" + emamt +
                    '}';
        }

        String code;
        String name;
        String type;
        double cdamt;
        double cmamt;
        double pmamt;
        double lmamt;
        double emamt;

        public SalesJyuk(String code, String name, String type, double cdamt, double cmamt, double pmamt, double lmamt, double emamt) {
            this.code = code;
            this.name = name;
            this.type = type;
            this.cdamt = cdamt;
            this.cmamt = cmamt;
            this.pmamt = pmamt;
            this.lmamt = lmamt;
            this.emamt = emamt;
        }

    }

}
