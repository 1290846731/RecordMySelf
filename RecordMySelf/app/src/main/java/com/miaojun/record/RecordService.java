package com.miaojun.record;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.miaojun.record.util.FileUtil;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static java.security.AccessController.getContext;


public class RecordService extends Service {
  private MediaProjection mediaProjection;
  private MediaRecorder mediaRecorder;
  private VirtualDisplay virtualDisplay;

  private boolean running;
  private int width = 1080;
  private int height = 1920;
  private int dpi;
  private String videoFile;

  private FfmpegController fc;

  private boolean isCut = false;
  private boolean isSaveMain = true;
  private boolean isNeedVoice = true;

  private RecordListener recordListener;



  /************悬浮窗创建相关*************/
  private RelativeLayout mFloatLayout;
  private WindowManager.LayoutParams wmParams;
  //创建浮动窗口设置布局参数的对象
  private WindowManager mWindowManager;

  private TextView mFloatView;

  private FloatViewListener floatViewListener;

  /************悬浮窗创建相关*************/


  public void setCut(boolean cut) {
    isCut = cut;
  }

  public void setSaveMain(boolean saveMain) {
    isSaveMain = saveMain;
  }

  public void setNeedVoice(boolean needVoice) {
    isNeedVoice = needVoice;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return new RecordBinder();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    HandlerThread serviceThread = new HandlerThread("service_thread",
        android.os.Process.THREAD_PRIORITY_BACKGROUND);
    serviceThread.start();
    running = false;
    mediaRecorder = new MediaRecorder();

    try {
      fc = new FfmpegController(
              this, new FileUtil().getCacheDir(this));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if(mFloatLayout != null){
      mWindowManager.removeView(mFloatLayout);
    }
  }

  public void setMediaProject(MediaProjection project) {
    mediaProjection = project;
  }

  public boolean isRunning() {
    return running;
  }

  public void setConfig(int width, int height, int dpi) {
    this.width = width;
    this.height = height;
    this.dpi = dpi;
  }

  public boolean startRecord() {
    if (mediaProjection == null || running) {
      return false;
    }

    videoFile = getSaveDirectory() + System.currentTimeMillis() + ".mp4";

//    initAudioManager();
    initRecorder();
    createVirtualDisplay();

    mediaRecorder.start();
    running = true;
    return true;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public boolean stopRecord() {
    Toast.makeText(getApplicationContext(),"处理中，请稍后",Toast.LENGTH_SHORT).show();
    if (!running) {
      return false;
    }
    running = false;
    mediaRecorder.stop();
    mediaRecorder.reset();
    virtualDisplay.release();
    mediaProjection.stop();

    if(recordListener != null){
      recordListener.onFinish(videoFile);
    }

    if(isCut){
      initFfmepeg();
    }
    return true;
  }


  private void initFfmepeg(){
    int statusHigh = getStatusBarHeight();
    try {
      final String cutFile = getSaveDirectory() + System.currentTimeMillis() + ".mp4";
      fc.compress_clipVideo(videoFile,
              cutFile, 0, width, height-statusHigh, 0, statusHigh,
              new ShellUtils.ShellCallback() {

                @Override
                public void shellOut(String shellLine) {
                }

                @Override
                public void processComplete(int exitValue) {
                  if(recordListener != null){
                    recordListener.onFinish(cutFile);
                  }
                  if(!isSaveMain){
                    new FileUtil().deleteFile(new File(videoFile));
                  }
                  Toast.makeText(getApplicationContext(),"处理完成，请到文件列表查看",Toast.LENGTH_SHORT).show();
                }
              }
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private void createVirtualDisplay() {
    virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
  }

  private void initRecorder() {
    if(isNeedVoice){
      mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);//AudioSource.MIC 音频采集MIC
    }else{
      mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);//只有声音
    }


    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);//视频类型
//    mediaRecorder.setAudioChannels();
    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//设置输出格式
    mediaRecorder.setOutputFile(videoFile);//设置输出文件路径
    mediaRecorder.setVideoSize(width, height);//视频尺寸
    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);//视频编码
    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//音频编码
    mediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024);//编码比特率
    mediaRecorder.setVideoFrameRate(25);//帧率   不能低于24
    try {
      mediaRecorder.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getSaveDirectory() {
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      String rootDir = new FileUtil().getCacheDir(this) + "/" + "ScreenRecord" + "/";

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

  public class RecordBinder extends Binder {
    public RecordService getRecordService() {
      return RecordService.this;
    }
  }


  /**
   * 获取状态栏的高度
   */
  public int getStatusBarHeight(){
    Class<?> c = null;
    Object obj = null;
    Field field = null;
    int x = 0, statusBarHeight = 0;
    try {
      c = Class.forName("com.android.internal.R$dimen");
      obj = c.newInstance();
      field = c.getField("status_bar_height");
      x = Integer.parseInt(field.get(obj).toString());
      statusBarHeight = getResources().getDimensionPixelSize(x);
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    return statusBarHeight;
  }


    /**
     * 创建悬浮窗
     */
  public void creatFloatView(){
    wmParams = new WindowManager.LayoutParams();
    mWindowManager = (WindowManager)getApplication().getSystemService(WINDOW_SERVICE);
    wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;//设置weindow type
    wmParams.format = PixelFormat.RGBA_8888;//设置背景透明
    wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//设置浮动窗口不可变焦
    wmParams.gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;//设置布局位置
    wmParams.x = 0;
    wmParams.y = 0;//设置初始位置
    wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
    wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;//设置悬浮窗的宽高
    mFloatLayout = (RelativeLayout) LayoutInflater.from(getApplication()).inflate(R.layout.view_float_layout,null);
    mWindowManager.addView(mFloatLayout,wmParams);//add到layout
    mFloatView = (TextView)mFloatLayout.findViewById(R.id.float_btn);

    mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
            View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
            .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));//重绘

    mFloatView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(floatViewListener != null){
            floatViewListener.onClick();
        }
      }
    });
  }

  /**
   * 移除悬浮窗
   */
  public void removeFloatView(){
    if(mFloatLayout != null){
      mWindowManager.removeView(mFloatLayout);
    }
  }

  /**
   * 更新悬浮窗文字
   */
  public void updateFloatView(String msg){
    if(mFloatView != null && !TextUtils.isEmpty(msg)){
      mFloatView.setText(msg);
    }
  }


  public void setFloatViewListener(FloatViewListener floatViewListener) {
    this.floatViewListener = floatViewListener;
  }

  public interface FloatViewListener{
    void onClick();
  }

  public void setRecordListener(RecordListener recordListener) {
    this.recordListener = recordListener;
  }

  public interface RecordListener{
    void onFinish(String url);
  }

}