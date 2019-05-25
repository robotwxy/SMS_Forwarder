package com.example.carolluo.myfirstapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsReciver extends BroadcastReceiver {

    private StringBuffer msgText = new StringBuffer();
    private StringBuffer lastMsgText = new StringBuffer();
    private SmsMessage msg = null;
    private MyApp myApp;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("123", "onReceive");

        Bundle bundle = intent.getExtras();
        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                msg = SmsMessage.createFromPdu((byte[]) object);
                Date date = new Date(msg.getTimestampMillis());//时间
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String receiveTime = format.format(date);
                System.out.println("number:" + msg.getOriginatingAddress()
                        + "   body:" + msg.getDisplayMessageBody() + "  time:"
                        + msg.getTimestampMillis());
                myApp = (MyApp)context.getApplicationContext();
                String nowSms = msg.getMessageBody()+"\n"+receiveTime;
                Log.e("123", nowSms);
                if(nowSms.equalsIgnoreCase(myApp.getLastSms())){
                    Log.e("123", "same message, ignore");
                    return;
                }
                myApp.setlastSms(nowSms);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection connection = null;
                        BufferedReader reader = null;
                        try {
                            URL url = new URL("http://47.88.173.175/test");
                            StringBuilder buf = new StringBuilder();
                            buf.append(myApp.getLastSms());
                            byte[] data = buf.toString().getBytes("UTF-8");
                            connection = (HttpURLConnection) url.openConnection();
                            Log.e("123", "openConnection");
                            connection.setRequestMethod("POST");
                            connection.setDoOutput(true);
                            Log.e("123", "setDoOutput");
                            OutputStream out = connection.getOutputStream();
                            Log.e("123", "getOutputStream");
                            out.write(data);
                            Log.e("123", "write");
                            if (connection.getResponseCode() == 200) {
                                InputStream in = new BufferedInputStream(connection.getInputStream());
                                Log.e("123", "getInputStream");
                                reader = new BufferedReader(new InputStreamReader(in));
                                StringBuilder result = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    result.append(line);
                                }
                                Log.e("123", result.toString());
                                //show(result.toString());
                            } else {
                                Log.e("123", "connect failed");
                                //show("connect failed");
                            }
                            //connection.setConnectTimeout(10000);
                            //connection.setReadTimeout(10000);

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (connection != null) {//关闭连接
                                connection.disconnect();
                            }
                        }
                    }
                }).start();

            }
        }
    }
}
