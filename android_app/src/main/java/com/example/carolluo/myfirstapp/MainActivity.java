package com.example.carolluo.myfirstapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private MyApp myApp;
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE123";
    private TextView mTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.textView_response);
    }

    /**
     * Called when the user taps the Send button
     */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        //myApp = (MyApp)getApplication();
        Log.e("123", "before intend");
        startActivity(intent);
        Log.e("123", "after intend");
        // Do something in response to button
    }

    private void send() {
        //开启线程，发送请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("http://47.88.173.175/test");
                    StringBuilder buf = new StringBuilder();
                    buf.append(URLEncoder.encode("hello server","UTF-8"));
                    byte[]data = buf.toString().getBytes("UTF-8");
                    connection = (HttpURLConnection) url.openConnection();
                    Log.e("123", "openConnection");
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    OutputStream out = connection.getOutputStream();
                    out.write(data);
                    if(connection.getResponseCode()==200) {
                        InputStream in = new BufferedInputStream(connection.getInputStream());
                        Log.e("123", "getInputStream");
                        reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        show(result.toString());
                    }else{
                        show("connect failed");
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

    private void show(final String result) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(result);
            }
        });
    }


    int counter = 0;

    public void sendRequest(View view) {

        mTextView.setText("button clicked"+counter);
        counter = counter + 1;
        Log.e("123", "send request");
        send();
    }

}
