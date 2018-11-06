package com.yuong.demo.wifi;

import android.os.Handler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by yuandong on 2018/10/30.
 */

public class SocketThread extends Thread {
    private static String TAG = SocketThread.class.getSimpleName();

    /**
     * 端口号
     */
    private static final int PORT = 5210;
    private Socket mSocket;
    private String ip;
    private Handler mHandler;

    private ReadThread mReadThread;
    private OutputStream ops;

    public SocketThread(String ip, Handler handler) {
        this.ip = ip;
        this.mHandler = handler;

    }

    public Socket getSocket() {
        return mSocket;
    }


    @Override
    public void run() {
        try {
            mSocket = new Socket(ip, PORT);
            //发送数据
            sendData("01");

            //启动读取数据线程读取服务端数据
            mReadThread = new ReadThread(mSocket, mHandler);
            mReadThread.setRun(true);
            mReadThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //发送数据
    public void sendData(String data) {
        if (mSocket == null) {
            return;
        }
        System.out.println("-------------------------- send data --------------------");
        try {
            //向服务端发送数据
            ops = mSocket.getOutputStream();
            OutputStreamWriter opsw = new OutputStreamWriter(ops);
            BufferedWriter bw = new BufferedWriter(opsw);
            bw.write(data + "\r\n\r\n");
            bw.write("end\r\n\r\n");
            bw.flush();

           // bw.close();
           // opsw.close();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    //关闭socket连接
    public void close() {
        try {
            if (mReadThread != null) {
                mReadThread.setRun(false);
                mReadThread.close();
                mReadThread = null;
            }

            if (ops != null) {
                ops.close();
                ops = null;
            }
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
