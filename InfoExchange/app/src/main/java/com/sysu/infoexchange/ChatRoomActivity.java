package com.sysu.infoexchange;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sysu.infoexchange.pojo.MsgText;
import com.sysu.infoexchange.socket.Client;
import com.sysu.infoexchange.utils.ApplicationUtil;

public class ChatRoomActivity extends AppCompatActivity {
    //    private Client client;
    private ApplicationUtil appUtil;
    private TextView textView;
    private EditText editText;

    //    用于接收聊天信息
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("msg");
            // UI界面的更新等相关操作
            MsgText msgText = MsgText.toMess(val);
            if (msgText != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(msgText.getTime());
                sb.append("\t");
                sb.append(msgText.getUserName());
                sb.append(": \n\t");
                sb.append(msgText.getMsg());
                val = sb.toString();
            }
            textView.setText(val + "\n" + textView.getText());
        }
    };

    //    用于接收错误信息，给出提示
    private Handler ehandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("err");
            Toast.makeText(ChatRoomActivity.this, val, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.init("sysu");
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat_room);
            Intent intent = this.getIntent();
            textView = (TextView) findViewById(R.id.text);
            editText = (EditText) findViewById(R.id.input);

            appUtil =  (ApplicationUtil) ChatRoomActivity.this.getApplication();
            if (appUtil.getClient() == null) {
                appUtil.initClient(intent.getStringExtra("ip"), 2013);
                new Thread(networkTask).start();
            }
            appUtil.getClient().setHandler(handler);

            Button send_btn = (Button) findViewById(R.id.send);
            send_btn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String in = editText.getText().toString();
                        if (in != null && !"".equals(in))
                            appUtil.getClient().sendMsg(editText.getText().toString());
                    } catch (Exception e) {
                        appUtil.closeClient();
                        e.printStackTrace();
                        Logger.e(e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
            appUtil.closeClient();
        }
    }

    /**
     * 网络操作相关的子线程
     */
    Runnable networkTask = new Runnable() {

        @Override
        public void run() {
            try {
                //  在这里进行 socket连接
                appUtil.getClient().contSocket();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.getMessage());
                appUtil.closeClient();

                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("err", "网络异常，无法与服务器进行连接！");
                msg.setData(data);
                ehandler.sendMessage(msg);
            }
        }
    };

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (appUtil.getClient() != null) {
//            appUtil.getClient().close();
//            appUtil.getClient() = null;
//        }
//    }

}
