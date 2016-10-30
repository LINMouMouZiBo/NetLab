package com.sysu.infoexchange;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sysu.infoexchange.pojo.MsgText;
import com.sysu.infoexchange.socket.Client;
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
    //    socket连接时的信息
    private String ip;
    private String clientName;

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
                if ("3".equals(msgText.getType())) {
                    updateOnlineUser(msgText.getMsg());
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
            String val = data.getString("err");
            Toast.makeText(MainActivity.this, val, Toast.LENGTH_SHORT).show();
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
                new Thread(networkTask).start();
            }
            appUtil.getClient().setHandler(handler);


            Button send_btn = (Button) findViewById(R.id.send);
            send_btn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                    intent.putExtra("ip", ip);
                    intent.putExtra("name", clientName);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
            appUtil.closeClient();
        }
    }

    void updateOnlineUser(String res) {
        List<HashMap<String, Object>> listData = parseDataFromString(res);
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
                appUtil.getClient().sendMsg(clientName);
                Thread.sleep(100);
                appUtil.getClient().sendMsg("showuser");
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
    protected void onRestart() {
        super.onRestart();
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
