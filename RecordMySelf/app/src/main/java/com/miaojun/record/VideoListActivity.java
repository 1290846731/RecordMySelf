package com.miaojun.record;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;


import com.miaojun.record.adapter.VideoListAdapter;
import com.miaojun.record.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoListActivity extends Activity {
    private RecyclerView recyclerView;
    private VideoListAdapter adapter;
    private List<Map<String,String>> list;
    private Button delBtn;

    private FileUtil fileUtil;

    private String url = "ScreenRecord";

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if(adapter != null){
                list.clear();
                adapter.notifyDataSetChanged();//删除后刷新数据
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);


        recyclerView = (RecyclerView)findViewById(R.id.video_list);
        delBtn = (Button)findViewById(R.id.delete_btn);


        int type = getIntent().getIntExtra("type",-1);
        if(type != -1){
            url = "AudioRecord";
        }


        list = new ArrayList<>();
        adapter = new VideoListAdapter(VideoListActivity.this,list);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(VideoListActivity.this,VideoPlay2Activity.class);
                intent.putExtra("path",list.get(position).get("path"));
                intent.putExtra("name",list.get(position).get("name"));
                startActivity(intent);
            }

            @Override
            public void onLongClick(View v, int position) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM,
                        Uri.parse(list.get(position).get("path")));
                share.setType("*/*");//此处可发送多种文件
                startActivity(Intent.createChooser(share, "Share"));
            }

        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        fileUtil = new FileUtil();
        fileUtil.setFileListener(new FileUtil.FileListener() {
            @Override
            public void onListener() {
                handler.sendEmptyMessage(0);
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileUtil != null){
                    fileUtil.deleteAll(new File(fileUtil.getCacheDir(getApplication())+"/" + url+ "/"));
                }
            }
        });

        getAllList();

    }

    private void getAllList(){
        File[] files = getAllAdFile();
        if(files==null||files.length==0){
            return;
        }
        for(File file : files){
            Map<String,String> map = new HashMap<>();
            map.put("name",file.getName());
            map.put("path",file.getAbsolutePath());
            list.add(map);
        }
        adapter.notifyDataSetChanged();
    }


    private File[] getAllAdFile() {
        File file = new File(new FileUtil().getCacheDir(this), "/"+url+"/");
        if (!file.exists() || !file.isDirectory()) {
            return null;
        }

        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        return files;

    }
}
