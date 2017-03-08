package com.miaojun.record;

/**
 * Created by miaojun on 17/3/6.
 */

public class Constants {
    public final static int RECORD_DEFAULT = 0;//默认情况
    public final static int RECORD_INIT = 1;//初始化中
    public final static int RECORD_INITEND = 2;//初始化完成
    public final static int RECORD_RECORDING = 3;//录制中
    public final static int RECORD_ENDING = 4;//录制结束
    public final static int RECORD_NOFLOAT = 5;//不创建悬浮窗
    public final static int RECORD_STOP = 6;//停止录制

    public final static int RECORD_MODEL_1 = 0;//声音与画面
    public final static int RECORD_MODEL_2 = 1;//画面
    public final static int RECORD_MODEL_3 = 2;//声音

    public final static int CUT_MODEL_1 = 0;//不剪裁
    public final static int CUT_MODEL_2 = 1;//裁掉状态栏
    public final static int CUT_MODEL_3 = 2;//自定义剪裁
}
