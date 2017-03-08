package com.miaojun.record;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

public class VideoPlay2Activity extends Activity {
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private String path;
    private ProgressBar progressBar;


    private long firClick,secClick;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            progressBar.setProgress(mediaPlayer.getCurrentPosition());
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_play2);


        Intent intent = getIntent();
        path = intent.getStringExtra("path");

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        progressBar = (ProgressBar)findViewById(R.id.progress);
        mediaPlayer = new MediaPlayer();

//        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                play();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {

            }
        });
    }



    private void play(){
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //设置需要播放的视频
            mediaPlayer.setDataSource(path);
            //把视频画面输出到SurfaceView
            mediaPlayer.setDisplay(surfaceView.getHolder());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    System.out.println("得到总进度----"+mediaPlayer.getDuration());
                    progressBar.setMax(mediaPlayer.getDuration());
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                Toast.makeText(VideoPlay2Activity.this,"播放完成---",Toast.LENGTH_SHORT).show();
//                    if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                    progressBar.setProgress(0);
                    Toast.makeText(VideoPlay2Activity.this,"双击屏幕重新开始",Toast.LENGTH_SHORT).show();
//                    }
//                    finish();
                }
            });
            //播放
            mediaPlayer.start();
            timeFun();

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //先判断是否正在播放
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            handler.removeCallbacksAndMessages(null);
        }
    }


    private void timeFun(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer.isPlaying()) {
                    timeFun();
                    handler.sendEmptyMessage(0);
                }
            }
        },1000);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            System.out.println("点击输出---");
            firClick = secClick;
            secClick = System.currentTimeMillis();
            if (secClick - firClick < 500) {
//                Toast.makeText(VideoPlay2Activity.this, "双击", Toast.LENGTH_LONG)
//                        .show();
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    timeFun();
                }
            }
        }

        return super.onTouchEvent(event);
    }
}
