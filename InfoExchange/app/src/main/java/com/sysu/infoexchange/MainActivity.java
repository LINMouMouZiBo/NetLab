package com.sysu.infoexchange;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sysu.infoexchange.pojo.MsgText;
import com.sysu.infoexchange.utils.ApplicationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ApplicationUtil appUtil;
    private TextView textView;
    private ListView listView;
    private String[] keySet = {"name", "ip"};
    private int[] toIds = {R.id.name, R.id.ip};
    private List<HashMap<String, Object>> listData;
    //    socket连接时的信息
    private String ip;
    private String clientName;

    //    用于接收聊天信息
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            MsgText msgText = (MsgText) data.getSerializable("msg");
            p2pCommd(msgText);
            String val = "";
            // UI界面的更新等相关操作
            if (msgText != null) {
                if ("3".equals(msgText.getType())) {
                    updateOnlineUser(msgText.getText());
                } else if ("0".equals(msgText.getType())) {
                    Toast.makeText(MainActivity.this, msgText.getText(), Toast.LENGTH_SHORT).show();
                }
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
            MsgText msgText = (MsgText) data.getSerializable("msg");
            Toast.makeText(MainActivity.this, msgText.getText(), Toast.LENGTH_SHORT).show();
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
            listView = (ListView) findViewById(R.id.online_user_list);
            clientName = intent.getStringExtra("name");
            ip = intent.getStringExtra("ip");

            appUtil =  (ApplicationUtil) MainActivity.this.getApplication();
            if (appUtil.getClient() == null) {
                appUtil.initClient(ip, 2013);
                appUtil.setClientName(clientName);
                new Thread(networkTask).start();
            }
            appUtil.getClient().setHandler(handler);


            Button send_btn = (Button) findViewById(R.id.send);
            send_btn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                    startActivity(intent);
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, P2pClientActivity.class);
                    appUtil.receivedip =  (String) listData.get(position).get("ip");
                    appUtil.receivedName =  (String) listData.get(position).get("name");
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
            appUtil.closeClient();
        }
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
            }
        }
    }

    void updateOnlineUser(String res) {
        listData = parseDataFromString(res);
        /* 设置adapter */
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,
                listData,
                R.layout.contacts_list_item,
                keySet,
                toIds);
        listView.setAdapter(adapter);
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
                Thread.sleep(100);
                appUtil.getClient().sendMsg(MsgText.fromText("系统消息", clientName, "0"));
                Thread.sleep(100);
                appUtil.getClient().sendMsg(MsgText.fromText("系统消息", "#showuser", "0"));
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(e.getMessage());
                appUtil.closeClient();

                Message msg = new Message();
                Bundle data = new Bundle();
                MsgText msgText = MsgText.fromText("系统信息", "网络异常，无法与服务器进行连接！", "0");
                data.putSerializable("err", msgText);
                msg.setData(data);
                ehandler.sendMessage(msg);
            }
        }
    };

    private List<HashMap<String, Object>> parseDataFromString(String string) {
        List<HashMap<String, Object>> list = new ArrayList<>();
        String[] res = string.split("#%#");
        HashMap<String, Object> map;
        for (int i = 0; i < res.length; i++) {
            map = new HashMap<String, Object>();
            map.put("name", res[i].substring(0, res[i].lastIndexOf('$')));
            map.put("ip", res[i].substring(res[i].lastIndexOf('$') + 1));
            list.add(map);
        }
        return list;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            appUtil.getClient().sendMsg(MsgText.fromText("系统消息", "#showuser", "0"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            appUtil.getClient().sendMsg(MsgText.fromText("系统消息", "#showuser", "0"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appUtil.getClient() != null) {
            appUtil.closeClient();
        }
    }
}
