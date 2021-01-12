package site.bdsc.localization.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import site.bdsc.localization.myapplication.pdr.OrientSensor;
import site.bdsc.localization.myapplication.pdr.StepCallBack;
import site.bdsc.localization.myapplication.pdr.StepDetectionByAcc;
import site.bdsc.localization.myapplication.pojo.NNfingerprint;
import site.bdsc.localization.myapplication.tools.WiFiScanHelper;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements StepCallBack, OrientSensor.OrientCallBack {
    static final String TAG = "MainActivity";
//    private static final String SERVER_URL = "http://47.112.138.173:20558";
    private static final MediaType JSON =  MediaType.parse("application/json; charset=utf-8");
    private static final String SERVER_URL = "http://192.168.123.24:20558";
    private Button collectbutton;
    private EditText rpXaxis;
    private EditText rpYaxis;
    private EditText showOrient;
    private EditText showSteps;
    private EditText collectedTimes;   //ui界面相应的组件的对象
    private WiFiScanHelper wiFiScanHelper; // 封装WiFi扫描相应API的工具类
    private List<NNfingerprint> fps = new ArrayList<>(); // 采集的无线信号特征
    private final int LOCATION_REQUEST_CODE = 1;
    private final int ACTIVITY_REQUEST_CODE = 1;
    private int collectingTimes; //当前采集的次数
    private StepDetectionByAcc mStepDetector;
    private OrientSensor mOrientSensor;
    private int currentOrient;
    private int currentStep;
    private Gson gson = new Gson();
    private OkHttpClient client;

    SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);// 加载布局
        collectbutton = (Button) findViewById(R.id.btn_start);
        rpXaxis = (EditText) findViewById(R.id.edit_RPX);
        rpYaxis= (EditText) findViewById(R.id.edit_PRY);
        showOrient = (EditText) findViewById(R.id.text_showorient);
        showSteps = (EditText) findViewById(R.id.text_showstep);
        collectedTimes = (EditText) findViewById(R.id.edit_CTime);
        CollectButtonListener cbtnListener = new CollectButtonListener();
        collectbutton.setOnClickListener(cbtnListener); //为开始采集按钮添加相应点击事件
        wiFiScanHelper = new WiFiScanHelper(MainActivity.this);
        requestPermission();
        mStepDetector = new StepDetectionByAcc(this, this);
        mStepDetector.registerStepListener();
        mOrientSensor = new OrientSensor(this, this);
        mOrientSensor.registerOrientSensor();
    }

    @Override
    public void Orient(int orient) {
        currentOrient = orient;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showOrient.setText(String.valueOf(currentOrient));
            }
        });
    }

    @Override
    public void Step(int stepNum) throws InterruptedException {
        currentStep = stepNum;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showSteps.setText(String.valueOf(currentStep));
            }
        });
    }

    @Override
    protected void onDestroy() {
        mOrientSensor.unregisterOrient();
        mStepDetector.unregisterStep();
        super.onDestroy();
    }

    class CollectButtonListener implements View.OnClickListener { //点击事件的具体内容
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onClick(View v) {
            fps.clear();
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this); //生成一个对话框，来显示采集进度
            final LayoutInflater inflater = getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_main_info,null);
            final Button btn_cancel = (Button) view.findViewById(R.id.id_btn_cancel); //停止采集的按钮
            final TextView tv_times = (TextView) view.findViewById(R.id.id_tv_times); //当前采集的次数
            final TextView tv_status = (TextView) view.findViewById(R.id.id_tv_status);
            final AlertDialog dialog = alertDialogBuilder.setView(view).create();
            dialog.setCancelable(true);
            dialog.show();
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    client = new OkHttpClient();
                    String json = gson.toJson(fps);
                    RequestBody body = RequestBody.create(JSON, json);
                    Request request = new Request.Builder()
                            .url(SERVER_URL+"/V2I/fpsCollection")
                            .post(body)
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            final String errorMMessage = e.getMessage();
                            if(Looper.getMainLooper().getThread()==Thread.currentThread()){
                                Log.d(TAG, "onFailure: ON MAIN THREAD");
                            }else {
                                Log.d(TAG, "onFailure: Not main thread");
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("FeedBack")//设置对话框的标题
                                            .setMessage("服务器连接失败，错误原因:"+errorMMessage)//设置对话框的内容
                                            //设置对话框的按钮
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).create();
                                    dialog.show();
                                }
                            });
                        }
                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {

                        }
                    });
                }
            });
            collectingTimes = 0;
             //设置定时任务来采集无线信号特征
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(wiFiScanHelper.WifiStartScan()){
                        NNfingerprint fp = dataAssemble(wiFiScanHelper.getScanResults()); //无线信号的采集与数据的处理
                        fps.add(fp);
                        Log.i("scan", fps.toString());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_times.setText(""+collectingTimes+"次");
                        }
                    });
                    collectingTimes ++;
                    if(collectingTimes == Integer.parseInt(collectedTimes.getText().toString())){
                        timer.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("数据采集完成");
                                tv_status.setTextColor(Color.parseColor("#FF0000"));
                                btn_cancel.setText("完成");
                            }
                        });
                    }
                }
            }, 1000, 1000);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage("位置权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage("位置权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "PERMISSION 'ACTIVITY_RECOGNITION' NOT GRANTED");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_REQUEST_CODE);
        } else {
            Log.d("TAG", "PERMISSION 'ACTIVITY_RECOGNITION' GRANTED");
        }
    } //动态请求权限

    NNfingerprint dataAssemble(List<ScanResult> scanResults){
        Map<String, Integer> rssi = new HashMap<>();
        for (ScanResult result: scanResults) {
            rssi.put(result.BSSID, result.level);
        }
        Date date = new Date(System.currentTimeMillis());
        NNfingerprint fp = new NNfingerprint();
        fp.setXaxis(Float.parseFloat(rpXaxis.getText().toString()));
        fp.setYaxis(Float.parseFloat(rpYaxis.getText().toString()));
        fp.setWiFiFeatures(gson.toJson(rssi));
        fp.setTimeStamp(df.format(System.currentTimeMillis()));
        return fp;
    } //数据处理

}
