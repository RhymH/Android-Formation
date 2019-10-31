package insa.com.mybluetoothapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class ChatListAdapter extends ArrayAdapter<ChatMsg> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<ChatMsg> msgs;
    private int  mViewResourceId;

    public ChatListAdapter(Context context, int tvResourceId, ArrayList<ChatMsg> msgs){
        super(context, tvResourceId, msgs);
        this.msgs = msgs;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);

        ChatMsg chatMsg = msgs.get(position);

        if (chatMsg != null) {
            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView msg = (TextView) convertView.findViewById(R.id.msg);

            if (name != null) {
                name.setText(chatMsg.getName());
            }
            if (msg != null) {
                msg.setText(chatMsg.getMsg());
            }
        }

        return convertView;
    }

}
