package com.example.myokhttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myokhttp.net.Call;
import com.example.myokhttp.net.HttpClient;
import com.example.myokhttp.net.Request;
import com.example.myokhttp.net.RequestBody;
import com.example.myokhttp.net.Response;
import com.example.myokhttp.net.interfaces.Callback;

public class MainActivity extends AppCompatActivity {

    private TextView responseTV;
    private Button requestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseTV = findViewById(R.id.responseTV);
        requestBtn = findViewById(R.id.requestBtn);

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request();
            }
        });

    }

    private void request(){
        HttpClient httpClient = new HttpClient();
        String url = "https://www.baidu.com/s?wd=lol";
        Request request = new Request.Builder()
                .setUrl(url)
                .get()
//                .post(new RequestBody().add("key1", "value1").add("key2", "value2"))
                .build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, Throwable throwable) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                responseTV.setText(response.getBody());
            }
        });
    }

}
