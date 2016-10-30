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

public class P2pClientActivity extends AppCompatActivity {
    private ApplicationUtil appUtil;
    private TextView textView;
    private EditText editText;
    //    socket连接时的信息
    private String receiverIp;
    private String clientName;

    //    用于接收聊天信息
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            // UI界面的更新等相关操作
            MsgText msgText = (MsgText) data.getSerializable("msg");
            p2pCommd(msgText);
            String val = "";
            if (msgText != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(msgText.getTime());
                sb.append("\t");
                sb.append(msgText.getUserName());
                sb.append(": \n\t");
                sb.append(msgText.getText());
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
            Toast.makeText(P2pClientActivity.this, val, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_p2p_client);
            Intent intent = this.getIntent();
            textView = (TextView) findViewById(R.id.text);
            editText = (EditText) findViewById(R.id.input);
            Boolean isPassive = intent.getBooleanExtra("isPassive", false);

            appUtil = (ApplicationUtil) P2pClientActivity.this.getApplication();
            if (appUtil.getClient() == null) {
                Toast.makeText(P2pClientActivity.this, "网络异常，无法与服务器进行连接！", Toast.LENGTH_SHORT).show();
            }
            appUtil.getClient().setHandler(handler);
            clientName = appUtil.receivedName;
            receiverIp = appUtil.receivedip;
            if (isPassive) {
                // 被动建立连接，进行同意
                receiverIp = intent.getStringExtra("receiverIp");
                appUtil.receivedip = receiverIp;
                sendConnectMsg("#confirmP2P");
            } else {
                // 主动发起连接
                sendConnectMsg("#P2Pchat");
            }

            Button send_btn = (Button) findViewById(R.id.send);
            send_btn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String in = editText.getText().toString();
                        if (in != null && !"".equals(in)) {
                            MsgText msgText = MsgText.fromText(appUtil.getClientName(), editText.getText().toString(), "1");
                            msgText.setDst(receiverIp);
                            msgText.setIp(appUtil.getClient().getIp());
                            appUtil.getClient().sendMsg(msgText);
                        }
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

    private void sendConnectMsg(String commd) throws Exception {
        MsgText msgText = MsgText.fromText(appUtil.getClientName(), commd, "0");
        msgText.setDst(receiverIp);
        msgText.setIp(appUtil.getClient().getIp());
        appUtil.getClient().sendMsg(msgText);
    }

    private void p2pCommd(MsgText msgText) {
        if (!"".equals(msgText.getDst()) && "0".equals(msgText.getType())) {
            String comd = msgText.getText();
            if ("#requestP2P".equals(comd)) {
                Intent intent = new Intent("android.intent.action.MY_STATICRECEIVER");
                Bundle bundle = new Bundle();
                bundle.putSerializable("msg", msgText);
                intent.putExtras(bundle);
                sendBroadcast(intent);
            } else if ("#agreeP2P".equals(comd)) {
                msgText.setText("与对方建立连接，现在可以进行聊天了!");
            } else if ("#quitP2P".equals(comd)) {
                try {
                    sendConnectMsg("#leaveP2PchatPassive");
                    msgText.setText("对方终止聊天，聊天结束");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            sendConnectMsg("#leaveP2Pchat");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
