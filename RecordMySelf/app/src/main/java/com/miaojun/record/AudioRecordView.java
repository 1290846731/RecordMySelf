package com.miaojun.record;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.miaojun.record.util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by miaojun on 17/3/7.
 */

public class AudioRecordView extends FrameLayout implements View.OnClickListener,Runnable{
    private Context context;
    private VoiceLineView voiceLineView;
    private Button start,save;

    private MediaRecorder mMediaRecorder;

    private boolean isAlive = true;
    private AudioRecordViewListener listener;
    private String rootDir;

    public AudioRecordView(Context context,AudioRecordViewListener listener) {
        super(context);
        this.listener = listener;
        init(context);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / 100;
            double db = 0;// 分贝
            if (ratio > 1)
                db = 30 * Math.log10(ratio);
            voiceLineView.setVolume((int) (db));
        }
    };

    private void init(Context context){
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_audio_record,this);
        voiceLineView = (VoiceLineView) findViewById(R.id.voice_line);
        start = (Button) findViewById(R.id.start);
        save = (Button) findViewById(R.id.save);

        start.setOnClickListener(this);
        save.setOnClickListener(this);
        findViewById(R.id.parent_layout).setOnClickListener(this);

        rootDir = getSaveDirectory();
        initRecord();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.start){
            start.setText("录制中");
            start.setEnabled(false);
            voiceLineView.start();
            try {
                mMediaRecorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            Thread thread = new Thread(this);
            thread.start();
        }

        if(view.getId() == R.id.save){
            if(listener != null){
                listener.onFinish();
            }
        }
    }

    private void initRecord(){
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();

            /* ②setAudioSource/setVedioSource */
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            /* ③准备 */
        File file = new File(rootDir, System.currentTimeMillis()+".mp3");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        mMediaRecorder.setMaxDuration(1000 * 60 * 10);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isAlive) {
            handler.sendEmptyMessage(0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = new FileUtil().getCacheDir(context) + "/" + "AudioRecord" + "/";

            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }

//      Toast.makeText(getApplicationContext(), rootDir, Toast.LENGTH_SHORT).show();

            return rootDir;
        } else {
            return null;
        }
    }


    public interface AudioRecordViewListener{
        void onFinish();
    }

}
