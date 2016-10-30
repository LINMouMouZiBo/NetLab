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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sysu.infoexchange.pojo.MsgText;
import com.sysu.infoexchange.socket.Client;
import com.sysu.infoexchange.utils.ApplicationUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ChatRoomActivity extends AppCompatActivity {
    //    private Client client;
    private ApplicationUtil appUtil;
    private TextView textView;
    private EditText editText;

    public static final int CUT_PICTURE = 1;
    public static final int SHOW_PICTURE = 2;
    private Button takePhoto;
    private Button chooseFromAlbum;
    private ImageView picture;
    private Uri imageUri;

    //    用于接收聊天信息，Android的UI更新不能在线程中操作，需要用一个handler来
    //    获取线程操作结果，然后在handler中更新UI
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
                StringBuilder sb = new StringBuilder();
                sb.append(msgText.getTime());
                sb.append("\t");
                sb.append(msgText.getUserName());
                sb.append(": \n\t");
                sb.append(msgText.getText());
                val = sb.toString();
            }
            // 如果是p2p连接指令，不需要输出，这里起排除的作用
            if (!(!"".equals(msgText.getDst()) && "0".equals(msgText.getType()))) {
                textView.setText(val + "\n" + textView.getText());
            }
        }
    };

    //    用于接收错误信息，给出提示
    private Handler ehandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            MsgText msgText = (MsgText) data.getSerializable("msg");
            Toast.makeText(ChatRoomActivity.this, msgText.getText(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.init("sysu");
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat_room);
            textView = (TextView) findViewById(R.id.text);
            editText = (EditText) findViewById(R.id.input);

            takePhoto = (Button) findViewById(R.id.Take);
            chooseFromAlbum = (Button) findViewById(R.id.Choose);
            picture = (ImageView) findViewById(R.id.picture);


            appUtil = (ApplicationUtil) ChatRoomActivity.this.getApplication();
            if (appUtil.getClient() == null) {
                Toast.makeText(ChatRoomActivity.this, "网络异常，无法与服务器进行连接！", Toast.LENGTH_SHORT).show();
            }
            appUtil.getClient().setHandler(handler);
            appUtil.getClient().sendMsg(MsgText.fromText(appUtil.getClientName(), "#enterRoom", "0"));

            Button send_btn = (Button) findViewById(R.id.send);
            send_btn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String in = editText.getText().toString();
                        if (in != null && !"".equals(in))
                            appUtil.getClient().sendMsg(MsgText.fromText(appUtil.getClientName(), editText.getText().toString(), "1"));
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
                        picture.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            appUtil.getClient().sendMsg(MsgText.fromText(appUtil.getClientName(), "#leaveRoom", "0"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        try {
//            appUtil.getClient().sendMsg(MsgText.fromText(appUtil.getClientName(), "#leaveRoom", "0"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
