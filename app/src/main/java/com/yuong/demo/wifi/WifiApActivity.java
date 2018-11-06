package com.yuong.demo.wifi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yuong.demo.R;

import java.net.SocketException;
import java.util.List;

public class WifiApActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = WifiApActivity.class.getSimpleName();

    private TextView tvWifiAp;
    private ScanResult mScanResult;
    private Button
            btnWifiConnect,
            btnWifiDiscount,
            btnSendData,
            btnWifiSocketConnect,
            btnWifiSocketDisconnect;
    private EditText edt;

    private WifiManager mWifiManager;
    private WifiReceiver mReceiver;

    private String mWifiApState = "未连接";
    private boolean isApConnected;//是否已连接上热点


    private SocketThread mSocketThread;
    private boolean isServerConnected;//是否连接上服务终端

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    Log.e(TAG, "socket 通讯已连接...");
                    isServerConnected = true;
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_ap);
        Intent intent = getIntent();
        if (intent != null) {
            mScanResult = intent.getParcelableExtra("ap");
        }

        initView();

        mWifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //注册广播
        mReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(mReceiver, filter);
    }

    private void initView() {
        tvWifiAp = findViewById(R.id.wifi_ap);

        btnWifiConnect = findViewById(R.id.wifi_connect);
        btnWifiDiscount = findViewById(R.id.wifi_discount);
        btnWifiSocketConnect = findViewById(R.id.wifi_socket_connect);
        btnWifiSocketDisconnect = findViewById(R.id.wifi_socket_disconnect);
        edt = findViewById(R.id.content);
        btnSendData = findViewById(R.id.wifi_data_send);

        StringBuilder builder = new StringBuilder();
        if (mScanResult != null) {
            builder.append("SSID : ").append(mScanResult.SSID).append("\n");
            builder.append("BSSID : ").append(mScanResult.BSSID).append("\n");
            builder.append("LEVEL : ").append(mScanResult.level).append("\n");
            builder.append("capabilities : ").append(mScanResult.capabilities);

            //得到当前的ip的地址
            String ip1 = WifiUtil.getWifiIP(this);
            String ip2 = WifiUtil.getWifiRouteIPAddress(this);
            try {
                String ip3 = IPUtils.getIp(this);
                Log.e(TAG, "ip3 " + ip3);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            Log.e(TAG, "ip1 : " + ip1);
            Log.e(TAG, "ip2 : " + ip2);
        }
        tvWifiAp.setText(builder.toString());

        btnWifiConnect.setOnClickListener(this);
        btnWifiDiscount.setOnClickListener(this);
        btnWifiSocketConnect.setOnClickListener(this);
        btnWifiSocketDisconnect.setOnClickListener(this);
        btnSendData.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ap, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.option_ap_state).setTitle(mWifiApState);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_ap_state:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wifi_connect://连接热点
                connect();
                break;
            case R.id.wifi_discount://关闭热点
                discount();
                break;
            case R.id.wifi_socket_connect://建立socket连接
                String ip1 = WifiUtil.getWifiIP(this);
                String ip2 = WifiUtil.getWifiRouteIPAddress(this);

                Log.e(TAG, "ip1 : " + ip1);
                Log.e(TAG, "ip2 : " + ip2);

                connectSocketServer(ip2);
                break;
            case R.id.wifi_socket_disconnect://断开socket连接
                distSocketServer();
                break;
            case R.id.wifi_data_send://发送数据
                sendData();
                break;
        }
    }


    private void connect() {
        if (mScanResult != null) {
            final int type = WifiUtil.getEncryptedType(mScanResult.capabilities);
            if (WifiUtil.isEncrypted(mScanResult.capabilities)) {//加密，需要密码
                final EditText editText = new EditText(WifiApActivity.this);
                new AlertDialog.Builder(WifiApActivity.this)
                        .setTitle("请输入Wifi密码")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e(TAG, "密码 :" + editText.getText());
                                String password = editText.getText().toString().trim();
                                if (!TextUtils.isEmpty(password)) {
                                    connectWifi(mScanResult.SSID, password, type);
                                } else {
                                    Toast.makeText(WifiApActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                connectWifi(mScanResult.SSID, null, type);
            }
        }
    }


    /**
     * 连接wifi 参数：wifi的ssid及wifi的密码
     */
    private void connectWifi(final String ssid, final String pwd, int type) {
        Log.i(TAG, "ssid : " + ssid + " pwd : " + pwd + " type : " + type);
        mWifiManager.disconnect();
        addNetwork(CreateWifiInfo(ssid, pwd, type));
    }

    private void discount() {
        mWifiManager.disconnect();
        WifiInfo wifiInfo = getCurrentWifiInfo();
        WifiUtil.removeWifiBySsid(mWifiManager, wifiInfo.getSSID());
    }

    /**
     * 添加一个网络并连接传入参数：WIFI发生配置类WifiConfiguration
     */
    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean enable = mWifiManager.enableNetwork(wcgID, true);
        Log.e(TAG, "wcgID :  " + wcgID + "  enable : " + enable);
    }


    /**
     * 创建WifiConfiguration对象 分为三种情况：1没有密码;2用wep加密;3用wpa加密
     *
     * @param SSID
     * @param Password
     * @param Type
     * @return
     */
    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        if (Type == WifiUtil.SECURITY_NONE) {// WIFICIPHER_NOPASS
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WifiUtil.SECURITY_WEP) {// WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WifiUtil.SECURITY_WPA) {// WIFICIPHER_WPA
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            //config.allowedKeyManagement.set(4);//WPA2_PSK
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    /**
     * 获取当前手机所连接的wifi信息
     */
    public WifiInfo getCurrentWifiInfo() {
        return mWifiManager.getConnectionInfo();
    }

    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }


    public class WifiReceiver extends BroadcastReceiver {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                //主要用来判断密码错误,认证失败
                int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);

                if (WifiManager.ERROR_AUTHENTICATING == error) {
                    //密码错误,认证失败
                    Log.e(TAG, "密码错误或认证失败");
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info == null) return;
                Log.e(TAG, "State： " + info.getState());
//                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
//                    Log.e(TAG, "连接已断开");
//                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {

//                } else {
                NetworkInfo.DetailedState state = info.getDetailedState();
                //Log.e(TAG, "DetailedState ： "+state.toString());
                if (state == NetworkInfo.DetailedState.CONNECTING) {
                    Log.e(TAG, "WIFI连接中...");
                } else if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                    Log.e(TAG, "WIFI正在验证身份信息...");
                } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    Log.e(TAG, "WIFI正在获取IP地址...");
                } else if (state == NetworkInfo.DetailedState.CONNECTED) {
                    Log.e(TAG, "WIFI连接成功");
                    WifiInfo wifiInfo = getCurrentWifiInfo();
                    Toast.makeText(WifiApActivity.this, "已连接到WIFI : " + wifiInfo.getSSID(), Toast.LENGTH_SHORT).show();
                    invalidateOptionsMenu();
                    mWifiApState = "已连接";
                } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                    Log.e(TAG, "WIFI连接断开");
                    Toast.makeText(WifiApActivity.this, "WIFI连接断开", Toast.LENGTH_SHORT).show();
                    mWifiApState = "未连接";
                    invalidateOptionsMenu();
                } else if (state == NetworkInfo.DetailedState.FAILED) {
                    Log.e(TAG, "WIFI连接失败");
                    // tvWifiApSate.setText("WIFI连接失败");
                } else if (state == NetworkInfo.DetailedState.SCANNING) {
                    Log.i(TAG, "WIFI扫描中...");
                } else if (state == NetworkInfo.DetailedState.BLOCKED) {
                    Log.i(TAG, "WIFI访问被阻止");
                } else if (state == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                    Log.i(TAG, "WIFI链路连通性差");
                } else if (state == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                    Log.i(TAG, "WIFI检查网络是否是受控门户");
                }
                //}
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        distSocketServer();
    }

    //连接Socket服务终端
    private void connectSocketServer(String ip) {
        Log.e(TAG,"连接socket 终端....");
        if (!isServerConnected) {
            mSocketThread = new SocketThread(ip, mHandler);
            mSocketThread.start();
        }
    }

    private void distSocketServer() {
        Log.e(TAG,"断开socket 终端....");
        isServerConnected = false;
        if (mSocketThread != null) {
            mSocketThread.close();
        }
    }

    //发送数据
    private void sendData() {
        if (isServerConnected) {
          final String data = edt.getText().toString().trim();
            if (!TextUtils.isEmpty(data)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mSocketThread.sendData(data);
                    }
                }).start();

            } else {
                Toast.makeText(this, "内容能不能为空", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "未连接上服务终端", Toast.LENGTH_SHORT).show();
        }
    }

}
