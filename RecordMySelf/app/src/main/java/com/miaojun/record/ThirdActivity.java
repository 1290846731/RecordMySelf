package com.miaojun.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;

public class ThirdActivity extends AppCompatActivity {
    private Button record_voice;

    AudioRecord mRecord = null;
    boolean mReqStop = false;

    private final int kSampleRate = 44100;
    private final int kChannelMode = AudioFormat.CHANNEL_IN_STEREO;
    private final int kEncodeFormat = AudioFormat.ENCODING_PCM_16BIT;

    private final int kFrameSize = 2048;
    private String filePath = "/sdcard/voice.pcm";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        init();

        record_voice = (Button)findViewById(R.id.record_voice);
        record_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {

                    }
                }.start();
            }
        });
    }


    private void init() {
        int minBufferSize = AudioRecord.getMinBufferSize(kSampleRate, kChannelMode,
                kEncodeFormat);
        mRecord = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX,
                kSampleRate, kChannelMode, kEncodeFormat, minBufferSize * 2);
    }


    private void recordAndPlay() {
        FileOutputStream os = null;
        mRecord.startRecording();
        try {
            os = new FileOutputStream(filePath);
            byte[] buffer = new byte[kFrameSize];
            int num = 0;
            while (!mReqStop) {
                num = mRecord.read(buffer, 0, kFrameSize);
                os.write(buffer, 0, num);
            }

            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecord.stop();
        mRecord.release();
        mRecord = null;
    }
}
