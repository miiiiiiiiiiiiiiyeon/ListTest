package com.example.listtest.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.example.listtest.R;
import com.example.listtest.model.DaumSearchResultModel;

public class DetailActivity extends AppCompatActivity {

    DaumSearchResultModel.Channel.Item data;
    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // 데이터 받기
        data = (DaumSearchResultModel.Channel.Item)getIntent().getSerializableExtra("data");
        System.out.println("test");
        web = (WebView)findViewById(R.id.web);  // 웹뷰 객체 획득

        web.loadUrl(data.getLink());    // 로딩
    }
}
