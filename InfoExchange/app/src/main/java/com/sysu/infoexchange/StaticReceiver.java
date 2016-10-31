package com.sysu.infoexchange;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.sysu.infoexchange.pojo.MsgText;

public class StaticReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MsgText msgText = (MsgText) intent.getSerializableExtra("msg");
        sendNotification(context, msgText);
    }

    private void sendNotification(Context context, MsgText msgText) {
        //【1】获取Notification 管理器的参考
        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //【2】设置通知。PendingIntent表示延后触发，是在用户下来状态栏并点击通知时触发，触发时PendingIntent发送intent，本例为打开浏览器到指定页面。

        Intent intent = new Intent();
        intent.setClass(context, P2pClientActivity.class);

        Bundle data = new Bundle();
        data.putBoolean("isPassive", true);
        data.putString("receiverIp", msgText.getIp());
        intent.putExtras(data);

        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(context)
                .setTicker("其他用户发起跟你的聊天！")
                .setSmallIcon(R.drawable.note)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.note))
                .setContentTitle("p2p聊天申请")
                .setContentText("其他用户发起跟你的聊天，点击信息开始聊天！")
                .setContentIntent(pi)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL; //点击后删除，如果是FLAG_NO_CLEAR则不删除，FLAG_ONGOING_EVENT用于某事正在进行，例如电话，具体查看参考。
        //【3】发送通知到通知管理器。第一个参数是这个通知的唯一标识，通过这个id可以在以后cancel通知，更新通知（发送一个具有相同id的新通知）。这个id在应用中应该是唯一的。
        notifyMgr.notify(0, notification);
    }
}
