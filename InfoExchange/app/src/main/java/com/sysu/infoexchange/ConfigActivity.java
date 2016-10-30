package com.sysu.infoexchange;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sysu.infoexchange.utils.ApplicationUtil;

public class ConfigActivity extends AppCompatActivity {
    private EditText ip_editText;
    private EditText name_editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        ip_editText = (EditText) findViewById(R.id.ip);
        ip_editText.setText("192.168.199.217");
        name_editText = (EditText) findViewById(R.id.name);
        name_editText.setText("test");

        Button confirm_btn = (Button) findViewById(R.id.confirm_btn);
        confirm_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String in = ip_editText.getText().toString();
                    String name = name_editText.getText().toString();
                    if (in != null && !"".equals(in) && name != null && !"".equals(name)) {
                        Intent intent = new Intent(ConfigActivity.this, MainActivity.class);
                        intent.putExtra("ip", in);
                        intent.putExtra("name", name);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public  void finish() {
        super.finish();
        // 没有界面可以后退时，就断开socket，退出app
        if (isTaskRoot()) {
            ApplicationUtil appUtil =  (ApplicationUtil) getApplication();
            appUtil.closeClient();
            Toast.makeText(this, "已经退出程序", Toast.LENGTH_LONG).show();
        }
    }
}
