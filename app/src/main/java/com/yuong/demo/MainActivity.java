package com.yuong.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yuong.demo.wifi.IPUtils;
import com.yuong.demo.wifi.IpUtil;
import com.yuong.demo.wifi.WifiAdapter;
import com.yuong.demo.wifi.WifiApActivity;
import com.yuong.demo.wifi.WifiUtil;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = MainActivity.class.getSimpleName();

    private WifiReceiver mReceiver;
    private Button btnWifiOpen, btnWifiClose, btnWifiScan;
    private ListView listView;
    private ProgressBar progressBar;

    private WifiManager mWifiManager;
    private List<ScanResult> mWifiList;
    private List<WifiConfiguration> mWifiConfiguration;
    private WifiAdapter mAdapter;
    private List<ScanResult> mList;

    private String  mWifiState="未开启";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        mWifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //TODO 测试
        //1.
        try {
            String ip3 = IPUtils.getIp(this);
            Log.e(TAG, "ip3 " + ip3);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //2.
        try {
            IpUtil.getIpInLAN();
            IpUtil.getIpInLAN2();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        System.out.println( "路由器ip："+WifiUtil.getWifiRouteIPAddress(this));

    }


    private void initView() {
        btnWifiOpen = findViewById(R.id.wifi_open);
        btnWifiClose = findViewById(R.id.wifi_close);
        btnWifiScan = findViewById(R.id.wifi_scan);
        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.wifi_list);

        mList = new ArrayList<>();
        mAdapter = new WifiAdapter(this, mList);
        listView.setAdapter(mAdapter);

        btnWifiOpen.setOnClickListener(this);
        btnWifiClose.setOnClickListener(this);
        btnWifiScan.setOnClickListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("ap", mList.get(position));
                intent.setClass(MainActivity.this, WifiApActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.meun_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.option_wifi_state).setTitle(mWifiState);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_wifi_state:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wifi_open:
                if (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                }
                break;
            case R.id.wifi_close:
                if (mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(false);
                }
                break;
            case R.id.wifi_scan:
                if (mWifiManager.isWifiEnabled()) {
                    progressBar.setVisibility(View.VISIBLE);
                    startScan();
                    mAdapter.setData(null);
                } else {
                    Toast.makeText(this, "WiFi没有开启", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册WiFi状态广播
        registerReceiver();

        if (mWifiManager.isWifiEnabled()) {
            progressBar.setVisibility(View.VISIBLE);
            startScan();
            mAdapter.setData(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
    }

    public void startScan() {
        mWifiManager.startScan();
    }


    private void getWiFiList() {
        //得到扫描结果
        List<ScanResult> results = mWifiManager.getScanResults();
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();

        if (results == null) {
            int wifiState = mWifiManager.getWifiState();
            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    Toast.makeText(this, "当前区域没有无线网络", Toast.LENGTH_SHORT).show();
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Toast.makeText(this, "wifi正在开启,请稍后扫描", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "WiFi没有开启", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            mWifiList = new ArrayList();
            for (ScanResult result : results) {
                if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for (ScanResult item : mWifiList) {
                    if (item.SSID.equals(result.SSID) && item.capabilities.equals(result.capabilities)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    mWifiList.add(result);
                }
            }
        }

        if (mWifiList != null && mWifiList.size() > 0) {
            for (ScanResult result : mWifiList) {

                int nSigLevel = WifiManager.calculateSignalLevel(result.level, 4);
                Log.e(TAG, "SSID : "
                        + result.SSID + " BSSID : "
                        + result.BSSID + " RSSI : " + nSigLevel);
                Log.e(TAG, "capabilities : " + result.capabilities);
            }

            //显示列表
            mAdapter.setData(mWifiList);
        }

    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                switch (wifistate) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        //wifi已关闭
                        Log.e(TAG, "WIFI 断开");
                        invalidateOptionsMenu();
                        mWifiState="未开启";
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        //wifi已打开
                        Log.e(TAG, "WIFI 开启");
                        mWifiState="已开启";
                        invalidateOptionsMenu();
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        //wifi正在打开
                        Log.e(TAG, "WIFI 开启中...");
                        mWifiState="开启中...";
                        invalidateOptionsMenu();
                        break;
                    default:
                        break;
                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                progressBar.setVisibility(View.GONE);
                boolean isScanned = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                Log.e(TAG, "isScanned : " + isScanned);
                getWiFiList();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void registerReceiver() {
        mReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(mReceiver, filter);
    }

}
