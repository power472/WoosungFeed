package com.woosung.messages;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.woosung.R;
import com.woosung.messages.MessageFragment.OnListFragmentInteractionListener;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    //리스트뷰에서는 아이템을 위한 뷰를 보관하는데 이거는 데이터만 보관한다.
    private List<Message> mValues;
    private final OnListFragmentInteractionListener mListener;


    public MessageAdapter(List<Message> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }


    //뷰홀더가 만들어지는 시점에 호출되는 메소드(각각의 아이템을 위한 뷰홀더 객체가 처음만들어지는시점)
    //만약에 각각의 아이템을 위한 뷰홀더가 재사용될 수 있는 상태라면 호출되지않음
    // (그래서 편리함, 이건내생각인데 리스트뷰같은경우는 convertView로 컨트롤해줘야하는데 이건 자동으로해줌)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_message_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).title);
        holder.mMessageView.setText(mValues.get(position).message);
        holder.mDatetime.setText(mValues.get(position).datetime);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }


    //어댑터에서 관리하는 아이템의 개수를 반환
    @Override
    public int getItemCount() {
        return mValues.size();
    }



    //아이템을 한개 추가해주고싶을때
    public  void addItem(Message item){
        mValues.add(item);
    }

    //한꺼번에 추가해주고싶을때
    public void addItems(List<Message> items){
        mValues = items;
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mMessageView;
        public final TextView mDatetime;
        public Message mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mMessageView = (TextView) view.findViewById(R.id.message);
            mDatetime = (TextView) view.findViewById(R.id.datetime);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mMessageView.getText() + "'";
        }
    }
}
