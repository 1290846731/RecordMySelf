package com.miaojun.record;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by miaojun on 17/3/6.
 *
 * 主页面
 */

public class IndexActivity extends Activity {
    private static final int RECORD_REQUEST_CODE  = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE   = 103;

    private RadioGroup record_model;
    private RadioGroup cut_model;
    private CheckBox save_main,create_float;
    private EditText file_name;
    private TextView file_path;
    private Button creat_task,show_video_list,show_audio_list;
    private RelativeLayout save_main_layout;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;

    private int recordStatus;
    private int cutStatus;
    private boolean isServiceInit = false;
    private boolean isCreatFloat = true;
    private boolean isCut = false;
    private boolean isSaveMain = true;

    private FrameLayout parent_layout;
    private AudioRecordView audioRecordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        setContentView(R.layout.activity_index);
        findView();
        setListener();
        checkPermission();
        init();
    }

    private void findView(){
        parent_layout = (FrameLayout) findViewById(R.id.parent_layout);
        record_model = (RadioGroup) findViewById(R.id.record_model);
        cut_model = (RadioGroup) findViewById(R.id.cut_model);
        save_main = (CheckBox) findViewById(R.id.save_main);
        file_name = (EditText) findViewById(R.id.file_name);
        file_path = (TextView) findViewById(R.id.file_path);
        creat_task = (Button) findViewById(R.id.creat_task);
        show_video_list = (Button) findViewById(R.id.show_video_list);
        show_audio_list = (Button) findViewById(R.id.show_audio_list);
        create_float = (CheckBox) findViewById(R.id.create_float);
        save_main_layout = (RelativeLayout) findViewById(R.id.save_main_layout);
    }

    private void setListener(){
        record_model.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) findViewById(id);
                switch (radioButton.getText().toString()){
                    case "声音与画面":
                        recordStatus = Constants.RECORD_MODEL_1;
                        if(recordService != null){
                            recordService.setNeedVoice(true);
                        }
                        break;
                    case "画面":
                        recordStatus = Constants.RECORD_MODEL_2;
                        if(recordService != null){
                            recordService.setNeedVoice(false);
                        }
                        break;
                    case "声音":
                        recordStatus = Constants.RECORD_MODEL_3;
                        audioRecordView = new AudioRecordView(IndexActivity.this, new AudioRecordView.AudioRecordViewListener() {
                            @Override
                            public void onFinish() {
                                parent_layout.removeView(audioRecordView);
                            }
                        });
                        parent_layout.addView(audioRecordView);
                        break;
                }
            }
        });

        cut_model.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) findViewById(id);
                switch (radioButton.getText().toString()){
                    case "不剪裁":
                        save_main.setVisibility(View.GONE);
                        isCut = false;
                        cutStatus = Constants.CUT_MODEL_1;
                        save_main_layout.setVisibility(View.GONE);
                        if(recordService != null){
                            recordService.setCut(false);
                        }
                        break;
                    case "裁掉状态栏":
                        save_main.setVisibility(View.VISIBLE);
                        isCut = true;
                        cutStatus = Constants.CUT_MODEL_2;
                        save_main_layout.setVisibility(View.VISIBLE);
                        if(recordService != null){
                            recordService.setCut(true);
                        }
                        break;
                    case "自定义剪裁":
                        save_main.setVisibility(View.VISIBLE);
                        cutStatus = Constants.CUT_MODEL_3;
                        save_main_layout.setVisibility(View.VISIBLE);
                        if(recordService != null){
                            recordService.setCut(true);
                        }
                        break;
                }
            }
        });

        creat_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordFun();
            }
        });

        show_video_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IndexActivity.this,VideoListActivity.class);
                startActivity(intent);
            }
        });

        show_audio_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IndexActivity.this,VideoListActivity.class);
                intent.putExtra("type",1);
                startActivity(intent);
            }
        });

        create_float.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isCreatFloat = b;
                if(isCreatFloat){
                    updateButtonStatus(Constants.RECORD_DEFAULT);
                }else{
                    updateButtonStatus(Constants.RECORD_NOFLOAT);
                }
            }
        });

        save_main.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isSaveMain = b;
                if(recordService != null){
                    recordService.setSaveMain(isSaveMain);
                }
            }
        });
    }


    private void init(){

    }

    /**
     * 权限检查
     */
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(IndexActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(IndexActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        }
    }

    /**
     * 录制按钮相关
     *
     */
    private void recordFun(){
        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        updateButtonStatus(Constants.RECORD_INIT);

        if(recordService != null){
            if(isCreatFloat){
                recordService.creatFloatView();
                recordService.updateFloatView("开始录制");
            }
            Intent captureIntent = projectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
        }
    }

    /**
     * 更新按钮文字
     */
    private void updateButtonStatus(int status){
        switch (status){
            case Constants.RECORD_DEFAULT:
                creat_task.setText("创建悬浮窗");
                creat_task.setEnabled(true);
                break;
            case Constants.RECORD_INIT:
                creat_task.setText("初始化中。。。");
                creat_task.setEnabled(false);
                break;
            case Constants.RECORD_INITEND:
                creat_task.setText("初始化完成");
                break;
            case Constants.RECORD_RECORDING:
                creat_task.setText("录制中。。。");
                break;
            case Constants.RECORD_ENDING:
                creat_task.setText("处理中。。。");
                break;
            case Constants.RECORD_NOFLOAT:
                creat_task.setText("初始化录制");
                creat_task.setEnabled(true);
                break;
            case Constants.RECORD_STOP:
                creat_task.setText("停止录制");
                creat_task.setEnabled(true);
                break;
        }
    }


    private void startRecord(){
        if(isServiceInit){
            if(recordService.isRunning()){
                isServiceInit = false;
                if(recordService != null && isCreatFloat){
                    recordService.removeFloatView();
                }
                recordService.stopRecord();
                if(isCreatFloat){
                    updateButtonStatus(Constants.RECORD_DEFAULT);
                }else{
                    updateButtonStatus(Constants.RECORD_NOFLOAT);
                }

            }else{
                recordService.setMediaProject(mediaProjection);
                recordService.startRecord();

                if(isCreatFloat){
                    updateButtonStatus(Constants.RECORD_RECORDING);
                }else{
                    updateButtonStatus(Constants.RECORD_STOP);
                }
                recordService.updateFloatView("录制中");
            }
        }else{
            Toast.makeText(IndexActivity.this,"初始化中。。。",Toast.LENGTH_SHORT).show();
        }
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if(recordStatus != Constants.RECORD_MODEL_3){
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                RecordService.RecordBinder binder = (RecordService.RecordBinder) iBinder;
                recordService = binder.getRecordService();
                recordService.setCut(isCut);
                recordService.setSaveMain(isSaveMain);
                recordService.setNeedVoice(!(recordStatus == Constants.RECORD_MODEL_2));
                recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
                recordService.setRecordListener(new RecordService.RecordListener() {
                    @Override
                    public void onFinish(String url) {
                        if (TextUtils.isEmpty(url))return;
                        file_path.setText("文件路径："+url);
                    }
                });
                recordService.setFloatViewListener(new RecordService.FloatViewListener() {
                    @Override
                    public void onClick() {
                        startRecord();
                    }
                });


                if(recordService != null){
                    if(isCreatFloat){
                        recordService.creatFloatView();
                        recordService.updateFloatView("开始录制");
                    }
                    Intent captureIntent = projectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE);

                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            isServiceInit = true;
            if(!isCreatFloat){
                updateButtonStatus(Constants.RECORD_ENDING);
                startRecord();
                return;
            }
            updateButtonStatus(Constants.RECORD_INITEND);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
