package com.yuong.demo.wifi;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * 用于读取服务端数据
 * Created by yuandong on 2018/10/31.
 */

public class ReadThread extends Thread {
    private static String TAG = ReadThread.class.getSimpleName();

    private Socket mSocket;
    private Handler mHandler;
    private boolean isRun;
    private InputStream ips;

    public void setRun(boolean run) {
        isRun = run;
    }

    public ReadThread(Socket socket, Handler handler) {
        this.mSocket = socket;
        this.mHandler = handler;
    }


    @Override
    public void run() {
        super.run();
        while (isRun) {
            readData();
        }
    }


    //读取数据
    private void readData() {

        InputStreamReader ipsr = null;
        BufferedReader br = null;
        try {
            Log.i(TAG, "---------------- receiver ------------------");
            ips = mSocket.getInputStream();
            if (ips.available() > 0) {
                ipsr = new InputStreamReader(ips);
                br = new BufferedReader(ipsr);
                String s = "";
                while ((s = br.readLine()) != null) {
                    System.out.println("客户端 ：" + s);
                    if (s.trim().equals("01")) {
                        Message message = new Message();
                        message.what = 100;
                        mHandler.sendMessage(message);
                    }
                    if (s.trim().equals("end")) {
                        break;
                    }
                }
                Log.i(TAG, "---------------- completed ------------------");
            }

            try {
                Thread.sleep(5000);//每隔1秒读取一次
            } catch (InterruptedException e) {
                e.printStackTrace();
                isRun = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            isRun = false;
        }
    }

    public void close() {
        try {
            if (ips != null) {
                ips.close();
                ips = null;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
