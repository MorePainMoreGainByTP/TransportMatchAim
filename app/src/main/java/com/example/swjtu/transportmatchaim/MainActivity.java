package com.example.swjtu.transportmatchaim;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.example.swjtu.transportmatchaim.uploadLatLng.UploadLatLngService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String REGISTER_URL = "http://172.20.202.1/tangpeng/getId.php";
    private static final String APP_KEY = "tScr1XrPntgqC66Z6M9aIojT";

    public static String phoneIMEI;

    private CheckBox blueTeeth, WiFi;
    private TextView registerId;
    private SharedPreferences sharedPreferences;
    private Button btnRegister;

    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private TelephonyManager telephonyManager;

    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dismissDialog();
            if (msg.what == 0) {//服务器没有响应
                Toast.makeText(MainActivity.this, "网络故障！", Toast.LENGTH_SHORT).show();
            }
            if (msg.what == 1) {//注册成功
                registerSuccessfully(msg.obj);
            }
            if (msg.what == 2) {
                Toast.makeText(MainActivity.this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            }
            if (msg.what == 3) {
                Toast.makeText(MainActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //注册成功，修改textview
    private void registerSuccessfully(Object obj) {
        String jsonStr = (String) obj;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonStr);
            String id = jsonObject.getString("id");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (id != null && id.length() == 4) {
                registerId.setText(id);
                btnRegister.setClickable(false);
                btnRegister.setText("已注册");
                editor.putString("id", id);
                editor.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "registerSuccessfully: JSONException", e);
            handler.sendEmptyMessage(2);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("registerId", MODE_PRIVATE);
        //打开百度云推送服务
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, APP_KEY);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        requestQueue = Volley.newRequestQueue(this);
        phoneIMEI = telephonyManager.getDeviceId(); //获取手机唯一识别码
        Log.i(TAG, "onCreate: phoneIMEI " + phoneIMEI);

        initViews();
        initData();

        startUploadService();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("异常：" + e.getMessage());
                Log.e(TAG, "uncaughtException: ", e);
            }
        });
    }

    private void startUploadService() {
        Intent intent = new Intent(MainActivity.this, UploadLatLngService.class);
        startService(intent);
    }

    private void initViews() {
        blueTeeth = (CheckBox) findViewById(R.id.switch_blue_teeth);
        WiFi = (CheckBox) findViewById(R.id.switch_wi_fi);
        registerId = (TextView) findViewById(R.id.textview_id);
        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: ");
                commitIMEI();
            }
        });

        if (!bluetoothAdapter.isEnabled()) {//未打开蓝牙
            blueTeeth.setChecked(false);
        } else {
            blueTeeth.setChecked(true);
        }

        if (wifiManager.isWifiEnabled()) {//如果wifi是开启的
            WiFi.setChecked(true);
        } else {
            WiFi.setChecked(false);
        }

        blueTeeth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {//打开蓝牙
                    if (bluetoothAdapter.enable()) {
                        Toast.makeText(MainActivity.this, "正在打开蓝牙", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "打开蓝牙失败", Toast.LENGTH_SHORT).show();
                    }
                } else {//关闭蓝牙
                    if (bluetoothAdapter.disable()) {
                        Toast.makeText(MainActivity.this, "正在关闭蓝牙", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "关闭蓝牙失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        WiFi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {//打开wi-fi
                    if (!wifiManager.isWifiEnabled()) {//没有打开
                        wifiManager.setWifiEnabled(true);
                        Toast.makeText(MainActivity.this, "正在打开Wi-Fi", Toast.LENGTH_SHORT).show();
                    }
                } else {//关闭wi-fi
                    if (wifiManager.isWifiEnabled()) {//没有打开
                        wifiManager.setWifiEnabled(false);
                        Toast.makeText(MainActivity.this, "正在关闭Wi-Fi", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void initData() {
        //获取申请的ID
        String savedId = sharedPreferences.getString("id", "");
        if (!savedId.equals("")) {//已经注册并获得了ID
            registerId.setText(savedId);
            btnRegister.setClickable(false);
            btnRegister.setText("已注册");
        }
    }

    private void showDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在申请ID...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void dismissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    //提交IMEI申请4位数ID
    private void commitIMEI() {
        Log.i(TAG, "commitIMEI: " + phoneIMEI);
        showDialog();
        if (phoneIMEI != null) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    Log.i(TAG, "onResponse: " + s);
                    Message message = new Message();
                    message.what = 1;
                    message.obj = s;
                    handler.sendMessage(message);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e(TAG, "onErrorResponse: ", volleyError);
                    handler.sendEmptyMessage(0);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("IMEI", phoneIMEI);
                    return map;
                }
            };
            requestQueue.add(stringRequest);
        } else {
            Toast.makeText(this, "获取手机识别码失败，注册失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
