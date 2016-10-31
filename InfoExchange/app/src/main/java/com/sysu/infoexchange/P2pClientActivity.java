package com.sysu.infoexchange;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sysu.infoexchange.adapter.MessageApapter;
import com.sysu.infoexchange.pojo.MessageItem;
import com.sysu.infoexchange.pojo.MsgText;
import com.sysu.infoexchange.socket.Client;
import com.sysu.infoexchange.utils.ApplicationUtil;
import com.sysu.infoexchange.utils.BitmapUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class P2pClientActivity extends AppCompatActivity {
    private ApplicationUtil appUtil;
    private EditText editText;
    //    socket连接时的信息
    private String receiverIp;
    private String clientName;

    public static final int CUT_PICTURE = 1;
    public static final int SHOW_PICTURE = 2;
    private Button takePhoto;
    private Button chooseFromAlbum;
    private Uri imageUri;
    private ListView listView;
    private MessageApapter msgAdapter;
    private List<MessageItem> listData;

    //    用于接收聊天信息
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            // UI界面的更新等相关操作
            MsgText msgText = (MsgText) data.getSerializable("msg");
            p2pCommd(msgText);

            MessageItem msgItem = new MessageItem();
            msgItem.setNameAndTime(msgText.getUserName() + " " + msgText.getTime());
            msgItem.setText(msgText.getText());
            msgItem.setImage(BitmapUtils.getBitmap(msgText.getImage()));
            listData.add(0, msgItem);
            msgAdapter.notifyDataSetChanged();
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
            editText = (EditText) findViewById(R.id.input);

            takePhoto = (Button) findViewById(R.id.Take);
            chooseFromAlbum = (Button) findViewById(R.id.Choose);
            listView = (ListView) findViewById(R.id.contacts_list);

            listData = new ArrayList<>();
            msgAdapter = new MessageApapter(this, R.id.contacts_list, listData);
            listView.setAdapter(msgAdapter);

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
                            editText.setText("");
                        }
                    } catch (Exception e) {
                        appUtil.closeClient();
                        e.printStackTrace();
                        Logger.e(e.getMessage());
                    }
                }
            });


            takePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //创建File对象，用于存储拍照后的图片
                    //将此图片存储于SD卡的根目录下
                    File outputImage = new File(Environment.getExternalStorageDirectory(),
                            "output_image.jpg");
                    try {
                        if (outputImage.exists()) {
                            outputImage.delete();
                        }
                        outputImage.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //将File对象转换成Uri对象
                    //Uri表标识着图片的地址
                    imageUri = Uri.fromFile(outputImage);
                    //隐式调用照相机程序
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    //拍下的照片会被输出到output_image.jpg中去
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    //此处是使用的startActivityForResult（）
                    //因此在拍照完后悔有结果返回到onActivityResult（）中去，返回值即为TAKE_PHOTO
                    //onActivityResult（）中主要是实现图片裁剪
                    startActivityForResult(intent, CUT_PICTURE);
                }
            });

            chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File outputImage = new File(Environment.getExternalStorageDirectory(),
                            "output_image.jpg");
                    imageUri = Uri.fromFile(outputImage);

                    try {
                        if (outputImage.exists()) {
                            outputImage.delete();
                        }
                        outputImage.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    //此处调用了图片选择器
                    //如果直接写intent.setDataAndType("image/*");
                    //调用的是系统图库
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, CUT_PICTURE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
            appUtil.closeClient();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CUT_PICTURE:
                if (resultCode == RESULT_OK) {
                    //此处启动裁剪程序
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    //此处注释掉的部分是针对android 4.4路径修改的一个测试
                    //有兴趣的读者可以自己调试看看
                    if (data == null) {
                        intent.setDataAndType(imageUri, "image/*");
                    } else {
                        intent.setDataAndType(data.getData(), "image/*");
                    }
                    // 跳过剪裁步骤，否则会运行出bug
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, SHOW_PICTURE);
                }
                break;
            case SHOW_PICTURE:
                if (resultCode == RESULT_OK) {
                    try {
//                        将output_image.jpg对象解析成Bitmap对象，然后设置到ImageView中显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageUri.getPath()));

                        MsgText msgText = MsgText.fromText(appUtil.getClientName(), editText.getText().toString(), "3");
                        msgText.setDst(receiverIp);
                        msgText.setIp(appUtil.getClient().getIp());
                        msgText.setImage(BitmapUtils.getBytes(bitmap));
                        appUtil.getClient().sendMsg(msgText);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
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
