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

import com.orhanobut.logger.Logger;
import com.sysu.infoexchange.pojo.MsgText;
import com.sysu.infoexchange.socket.Client;

public class MainActivity extends AppCompatActivity {
    private Client client;
    private TextView textView;
    private EditText editText;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.init("sysu");
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Intent intent = this.getIntent();
            textView = (TextView) findViewById(R.id.text);
            editText = (EditText) findViewById(R.id.input);
            client = new Client(intent.getStringExtra("ip"));
            client.setHandler(handler);

            Button send_btn = (Button) findViewById(R.id.send);
            send_btn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String in = editText.getText().toString();
                        if (in != null && !"".equals(in))
                            client.sendMsg(editText.getText().toString());
                    } catch (Exception e) {
                        client.close();
                        e.printStackTrace();
                        Logger.e(e.getMessage());
                    }
                }
            });
            new Thread(networkTask).start();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
            client.close();
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
                client.contSocket();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.getMessage());
                client.close();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            client.close();
            client = null;
        }
    }

}
