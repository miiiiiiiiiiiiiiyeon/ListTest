package com.example.listtest.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.listtest.R;
import com.example.listtest.model.DaumSearchResultModel;
import com.example.listtest.net.Net;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GridActivity extends RootActivity {
    TextView search_summary;
    EditText searchInput;
    GridView gridview;
    ImageButton changeBtn;
    GridAdapter gridAdapter;
    int columnCnt;      // 컬럼 수

    ArrayList<DaumSearchResultModel.Channel.Item> items = new ArrayList();
    ImageLoader imageLoader;
    DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        columnCnt = 3;

        search_summary = (TextView)findViewById(R.id.search_summary);
        searchInput = (EditText)findViewById(R.id.searchInput);
        gridview = (GridView)findViewById(R.id.gridview);
        changeBtn = (ImageButton) findViewById(R.id.changeBtn);


        gridAdapter = new GridAdapter();
        gridview.setAdapter(gridAdapter);
        //initUI();
        initImageLoader();
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

    public void onSearch(View view){
        search_summary.setText(null);
        items.clear();
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

                    gridAdapter.notifyDataSetChanged();     // items 변경 시 꼭 있어야 하는 메소드!
                    searchInput.setText(null);  // 검색 후 검색창 지워줌.
                }
                stopPD();
            }

            @Override
            public void onFailure(Call<DaumSearchResultModel> call, Throwable t) {
                stopPD();
            }
        });
    }

    public void onChangeView(View view){
        if( columnCnt == 3)     columnCnt = 1;
        else if(columnCnt ==1 ) columnCnt = 3;

        gridview.setNumColumns(columnCnt);

        gridAdapter = new GridAdapter();
        gridview.setAdapter(gridAdapter);

    }

    class ViewHolder{
        TextView name;
        TextView comment;
        ImageView poster;
    }

    class GridAdapter extends BaseAdapter
    {
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

            if (view == null) {
                holder = new ViewHolder();
                if (columnCnt == 3) {
                    view = getLayoutInflater().inflate(R.layout.cell_grid_layout, viewGroup, false);
                } else {
                    view = getLayoutInflater().inflate(R.layout.cell_daum_search_layout, viewGroup, false);

                    holder.comment = view.findViewById(R.id.comment);
                }
                holder.name = view.findViewById(R.id.name);
                holder.poster = view.findViewById(R.id.poster);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            DaumSearchResultModel.Channel.Item item = getItem(i);

            holder.name.setText(Html.fromHtml(item.getTitle()));
            holder.name.setText(Html.fromHtml(holder.name.getText().toString()));

            if(columnCnt==1) holder.comment.setText(String.format("공개일:%s     출처:%s", item.getPubDate(), item.getCpname()));

            imageLoader.displayImage(item.getThumbnail(), holder.poster, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });

            return view;
        }
    }

    public void initUI() {

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
    }

}
