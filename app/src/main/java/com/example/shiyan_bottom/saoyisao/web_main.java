package com.example.shiyan_bottom.saoyisao;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shiyan_bottom.R;

public class web_main extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web);
        Bundle bundle = getIntent().getExtras();
        String result = bundle.getString("result");

        WebView wb=(WebView)findViewById(R.id.web_view);
        WebSettings webSettings=wb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wb.loadUrl(result);
    }


}
