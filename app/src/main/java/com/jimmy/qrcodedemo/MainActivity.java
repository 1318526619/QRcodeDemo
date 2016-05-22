package com.jimmy.qrcodedemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    public static int QRCODE_REQUEST = 0x00;
    private TextView textView;
    private ImageView imageView;
    //定义2种颜色 黑色,白色
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text);
        imageView = (ImageView) findViewById(R.id.image);
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
        }
    }

    /**
     * 跳转google提供的扫描界面
     *
     * @param v
     */
    public void btnScanQRCode(View v) {
        //Action是"com.google.zxing.client.android.SCAN"
        //就是CaptureActivity界面的跳转添加
        Intent intent = new Intent(Intents.Scan.ACTION);

        //处理的图形类型 (Camera界面处理的就是QR code)
        intent.putExtra(Intents.Scan.MODE, BarcodeFormat.QR_CODE);

        //二维码扫描区的框有多大(正常情况宽度和高度相等的)
        //根据实际情况定义大小
        intent.putExtra(Intents.Scan.WIDTH, getQrSize(this));
        intent.putExtra(Intents.Scan.HEIGHT, getQrSize(this));
        startActivityForResult(intent, QRCODE_REQUEST);
    }

    //等待二维码扫描结果的返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QRCODE_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                //返回解码格式的key
                String result = data.getStringExtra(Intents.Scan.RESULT);
                //通过界面显示就可以了
                textView.setText(result);
            }
        }
    }

    /**
     * 实际的文字内容转化成二维码
     */
    public void btnCreateQRCode(View v) {
        //跳转到EncodeActivity
        Intent intent = new Intent(Intents.Encode.ACTION);
        //接收实际字符串的数据
        intent.putExtra(Intents.Encode.DATA, "这里是威哥");
        //定义转换二维码的格式
        //文本类型的
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        //生成的QRCode
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE);
        //启动界面
        startActivity(intent);
    }


    /**
     * 获取二维码扫描框的大小
     *
     * @param context
     * @return 实际大小
     */
    public int getQrSize(Context context) {
        int ret = 0;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        //获取小的值
        ret = screenWidth < screenHeight ? screenWidth : screenHeight;
        //向右位移表示/2的意思
        //8421(//1000 >>1    0100 >>1   0010)
        ret = ret >> 1;
        // ret = ret * 3/4;
        return ret;
    }


    /**
     * 自定义带有logo的二维码
     * 可以自己进行定制的二维码
     */
    public void btnCreateRawQRCode(View v) {
        //提供的封装的思路
//        Bitmap bm = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
//        Bitmap qrCodeBitmap = getQRCodeBitmap("123", 100, null);
//        Bitmap qrCodeBitmap2 = getQRCodeBitmap("123", 100, R.mipmap.ic_launcher);

        //1.EncodeHintType里进行基本设置
        HashMap<EncodeHintType, Object> hints = new HashMap<>();
        // (charset编码格式,utf-8)
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // (error correction)
        //使用ErrorCorrectionLevel.H的容错率是最高的(30%)
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        //使用write进行图片的生成(write的一个子类QR code的解码)
        QRCodeWriter writer = new QRCodeWriter();
        //写入数据(调用encode方法)
        try {
            //返回的是图像矩阵
            BitMatrix bitMatrix = writer.encode(
                    "helloworld", //文本信息内容
                    BarcodeFormat.QR_CODE, //生成的二维码
                    getQrSize(this),//宽度
                    getQrSize(this),//高度
                    hints
            );
            //获取返回矩阵宽度和高度
            int width = bitMatrix.getWidth();    // 3
            int height = bitMatrix.getHeight();  // 3
            //建立像素的数组
            //[0 *3 +0]
            //[0 *3 +1]
            //[0 *3 +2]
            //[1 *3 +0]
            //[1 *3 +1]
            //[1 *3 +2]
            //[2 *3 +0]
            int[] piexls = new int[width * height]; //所有的像素点都要进行体现
            for (int y = 0; y < height; y++) {    //记录有多少行
                for (int x = 0; x < width; x++) { //保证数组里面的每个像素都进行处理
                    // bitMatrix.get(x, y)判断true表示填入黑色, false填写白色
                    piexls[y * width + x] = bitMatrix.get(x, y) ? BLACK : WHITE;
                }
            }
            //创建了Bitmap视图,定义了宽度和高度,没有填任何数据但是定义了Bitmap.Config
            Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bm.setPixels(
                    piexls, //像素的集合点
                    0, //第一个绘制的像素颜色
                    width, //多少行
                    0, //起始的x坐标
                    0, //起始的y坐标
                    width, //图片的宽度
                    height //图片的高度
            );

            //公司的Logo
            Bitmap bmLogo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            //1.新建新的Bitmap (里面什么都没有)
            Bitmap bmNew = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            //2.自定义Canvas,存放bmNew(包含了bm和bmLogo)
            Canvas canvas = new Canvas(bmNew);
            //开始视图绘制
            /**
             * 第一个参数:图片源
             * 第二/三个参数:左/上角
             * 第四个参数:画笔填写null
             */
            canvas.drawBitmap(bm, 0, 0, null);
            //4.相对画布进行缩放
            //计算缩放比例的公式 (0.6左右的值)
            float scale = bm.getWidth() / bmLogo.getWidth() / 5.0f;
            //进行画布的缩放(画布的中心位置),中心位置就是原始图片的中心位置
            canvas.scale(scale, scale, bm.getWidth() / 2, bm.getHeight() / 2);
            //重新再画布上绘制Logo
            int x = (bm.getWidth() - bmLogo.getWidth()) / 2; //希望左侧的坐标
            int y = (bm.getHeight() - bmLogo.getHeight()) / 2;
            //需要求一下x,y坐标点
            canvas.drawBitmap(bmLogo, x, y, null);
            canvas.save(Canvas.ALL_SAVE_FLAG); //保存到BitmapNew中
            //获取的图片是上一次保存的状态
            canvas.restore(); //不可能调用的次数超过save方法的次数

            //显示图片(带有二维码+Logo)
            imageView.setImageBitmap(bmNew);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param data 数据源
     * @param size 大小
     * @param logo bitmap
     * @return 实际的二维码
     */
    public Bitmap getQRCodeBitmap(String data, int size ,Bitmap logo) {
        if(logo == null){
            //图片默认的效果
        }else {
            //图片累加的效果
        }
        return null;
    }

    /**
     *
     * @param data 数据源
     * @param size 大小
     * @param resId
     * @return 实际的二维码
     */
    public Bitmap getQRCodeBitmap(String data, int size ,int resId) {
        if(resId == 0){
            //图片默认的效果
        }else {
            //图片累加的效果
        }

        return null;
    }


}
