package com.woosung.messages;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.woosung.R;
import com.woosung.appDefault.AppDefaultFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MessageFragment extends AppDefaultFragment {

    private OnListFragmentInteractionListener mListener;
    private static final String TAG = "MessageFragment";
    private List<Message> messages = new ArrayList<Message>();
    private MessageAdapter messageAdpter;
    private Context context;


    protected int layoutRes() {
        return R.layout.fragment_message_item;
    }


    public static MessageFragment newInstance() {
        return new MessageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);
        if (view instanceof RecyclerView) {

            context = view.getContext();
            messageAdpter = new MessageAdapter(messages, mListener);

            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(messageAdpter);
        }
        requestMessageToServer();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Message item);


    }





    private void requestMessageToServer() {

        SharedPreferences pref = getActivity().getSharedPreferences("Variable", Activity.MODE_PRIVATE);
        String empl = pref.getString("EMPLCODE", "");

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("format", "json")
                .add("sqlfilename", "messages")
                .add("sqlnumber", "1")
                .add("receiver", empl)
                .build();
        Request request = new Request.Builder().url(getString(R.string.url_select)).post(formBody).build();
        client.newCall(request).enqueue(getCallback);

    }


    private Callback getCallback = new Callback() {
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

                for(int i=0; i< output1.length(); i++) {
                    JSONObject row = output1.getJSONObject(i).getJSONObject("row_data");

                    messageAdpter.addItem(new Message (
                            row.getString("TITLE"),
                            row.getString("MESSAGE"),
                            row.getString("WORKTIME").substring(0,19)));
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messageAdpter.notifyDataSetChanged();
                    }
                });





            } catch (JSONException e) {
                Log.e(TAG,s);
                Log.e(TAG,e.getMessage());
                e.printStackTrace();
            }
        }
    };

}
