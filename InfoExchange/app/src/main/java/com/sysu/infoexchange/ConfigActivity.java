package com.sysu.infoexchange;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConfigActivity extends AppCompatActivity {
    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        editText = (EditText) findViewById(R.id.ip);
        editText.setText("192.168.199.217");

        Button confirm_btn = (Button) findViewById(R.id.confirm_btn);
        confirm_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String in = editText.getText().toString();
                    if (in != null && !"".equals(in)) {
                        Intent intent = new Intent(ConfigActivity.this, MainActivity.class);
                        intent.putExtra("ip", in);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
