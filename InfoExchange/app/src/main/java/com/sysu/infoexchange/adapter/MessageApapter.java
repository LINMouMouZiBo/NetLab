package com.sysu.infoexchange.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
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
        final TextView nameAndTime = (TextView)view.findViewById(R.id.nameAndTime);
        nameAndTime.setText(msg.getNameAndTime());
        final TextView text = (TextView)view.findViewById(R.id.MessageText);
        if (msg.getText() != null && msg.getText().length() != 0) {
            text.setText(msg.getText());
        } else {
            ViewGroup.LayoutParams layoutParams = text.getLayoutParams();
            layoutParams.height = 0;
            layoutParams.width = 0;
            text.setLayoutParams(layoutParams);
        }
        final ImageView photo = (ImageView)view.findViewById(R.id.MessageImage);
        if (msg.getImage() != null && msg.getImage().getByteCount() != 0) {
            photo.setImageBitmap(msg.getImage());
        } else {
            ViewGroup.LayoutParams layoutParams = photo.getLayoutParams();
            layoutParams.height = 0;
            layoutParams.width = 0;
            photo.setLayoutParams(layoutParams);
        }

        return view;
    }
}