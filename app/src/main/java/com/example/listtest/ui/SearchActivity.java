package com.example.listtest.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.listtest.R;
import com.example.listtest.model.DaumSearchResultModel;
import com.example.listtest.net.Net;
import com.example.listtest.util.U;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends RootActivity {
    SearchActivity self;
    EditText searchInput;
    ListView listView;
    SearchAdapter searchAdapter;

    // 통신 후 받은 결과를 여기서 참조한다.
    ArrayList<DaumSearchResultModel.Channel.Item> items;

    // 네트워크 이미지 처리 오픈 소스
    DisplayImageOptions options;
    ImageLoader imageLoader;
    TextView search_summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;    // 자기 자신 객체인 this의 참조값을 담은 전역변수
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        items = new ArrayList();
        search_summary = (TextView)findViewById(R.id.search_summary);
        searchInput = (EditText)findViewById(R.id.searchInput);
        listView = (ListView)findViewById(R.id.listView);
        searchAdapter = new SearchAdapter();
        listView.setAdapter(searchAdapter);
        initImageLoader();

        //  리스트에 이벤트 등록하여 클릭 혹은 탭 하였을 때 반응 처리
/*        listView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                U.getInstance().log("OnClickListener click");

            }
        });     */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                U.getInstance().log("OnItemClickListener click");

                Intent intent = new Intent(self, DetailActivity.class);
                intent.putExtra("data", items.get(position));
                startActivity(intent);

            }   // 웹뷰 전환해주는거
        });
//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                U.getInstance().log("onItemLongClick click");
//                return false;
//            }
//        });
//        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                U.getInstance().log("OnItemSelectedListener click");
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                U.getInstance().log("onNothingSelected click");
//
//            }
//        });

    }

    public void initImageLoader()
    {
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build();

        imageLoader = ImageLoader.getInstance();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this.getBaseContext()).build();
        imageLoader.init(configuration);
    }

    class ViewHolder{
        TextView name;
        TextView comment;
        ImageView poster;
    }

    class SearchAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public DaumSearchResultModel.Channel.Item getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if( view == null ) {
                view = self.getLayoutInflater().inflate(R.layout.cell_daum_search_layout, viewGroup, false);
                holder = new ViewHolder();
                // 화면에 셀을 로테이션 시킬 정도 양이 될때까지 계속 호출된다.
                // ViewHolder
                holder.name = (TextView)view.findViewById(R.id.name);
                holder.comment = (TextView)view.findViewById(R.id.comment);
                holder.poster = (ImageView)view.findViewById(R.id.poster);
                view.setTag(holder);
            }else{
                holder = (ViewHolder)view.getTag();
            }


            // 데이터 획득
            DaumSearchResultModel.Channel.Item item = getItem(i);
            holder.name.setText( Html.fromHtml(item.getTitle()) );     // &lt;b&gt; -> <b>
            holder.name.setText( Html.fromHtml(holder.name.getText().toString()) );   // <b> -> b

            holder.comment.setText( String.format("공개일:%s 출처:%s", item.getPubDate(), item.getCpname()) );

            imageLoader.displayImage(item.getThumbnail(), holder.poster, options);

            return view;
        }
    }
    // 검색 진행
    public void onSearch(View view) {
        search_summary.setText(null);
        items.clear();  // 검색 결과를 지운다.
        final String keyword = searchInput.getText().toString();

        Map<String, String> params = new HashMap<>();
        params.put("apikey", "74c74d27c5ec50183ccb8a8341dcb1c2");
        params.put("q", keyword);
        params.put("output", "json");

        showPD();   // 응답 후 화면 갱신

        Net.getInstance().getDaumFactoryIm().searchImage(params);
        Call<DaumSearchResultModel> res = Net.getInstance().getDaumFactoryIm().searchImage(params);
        res.enqueue(new Callback<DaumSearchResultModel>() {
            @Override
            public void onResponse(Call<DaumSearchResultModel> call, Response<DaumSearchResultModel> response) {
                if( response != null && response.isSuccessful() ) {
                    items.addAll(response.body().getChannel().getItem());

                    int totalCount = Integer.parseInt(response.body().getChannel().getTotalCount());
                    int result = items.size();      // 누적된 수
                    if( totalCount > 0 ) {
                        search_summary.setText( "["+keyword+"] 총 검색된 수(" + totalCount +") / 현재 누적 수 (" + result + ")");
                    }else{
                        search_summary.setText( "검색된 결과가 없습니다." );
                    }



                    //for( DaumSearchResultModel.Channel.Item item : response.body().getChannel().getItem() ) {
                        //Log.i("T", item.getTitle());
                   // }

                    searchAdapter.notifyDataSetChanged();
                    searchInput.setText(null);  // 검색 후 검색창 비워준다.
                }
                stopPD();
            }

            @Override
            public void onFailure(Call<DaumSearchResultModel> call, Throwable t) {
                stopPD();
            }
        });

    }
    // 뷰 변환
    public void onChangeView(View view) {
        stopPD();
    }

}



