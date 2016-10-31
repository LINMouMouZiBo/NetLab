package com.sysu.infoexchange.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sysu.infoexchange.R;
import com.sysu.infoexchange.pojo.MessageItem;

import java.util.List;

/**
 * Created by zhuangqh on 2016/10/31.
 */

public class MessageApapter extends ArrayAdapter<MessageItem> {
    private LayoutInflater lInflater;

    public MessageApapter(Context context, int textViewResourceId, List<MessageItem> msgList) {
        super(context, textViewResourceId, msgList);
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        view = lInflater.inflate(R.layout.message_item, null);
        final MessageItem msg = getItem(position);
        final TextView text = (TextView)view.findViewById(R.id.MessageText);
        text.setText(msg.getText());
        final ImageView photo = (ImageView)view.findViewById(R.id.MessageImage);
        photo.setImageBitmap(msg.getImage());

        return view;
    }
}