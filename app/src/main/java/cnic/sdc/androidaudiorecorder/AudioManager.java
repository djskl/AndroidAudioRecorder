package cnic.sdc.androidaudiorecorder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;

public class AudioManager {
    public static final String EXTRA_FILE_PATH = "filePath";
    public static final String EXTRA_COLOR = "color";

    private Activity activity;  //启动AudioRecorderActivity的Activity

    private String filePath = null;
    private int color = Color.parseColor("#546E7A");
    private int requestCode = 0;

    private AudioManager(Activity activity) {
        this.activity = activity;
    }

    public static AudioManager with(Activity activity) {
        return new AudioManager(activity);
    }

    /**
     * 设置音频文件的存放路径
     */
    public AudioManager setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    /**
     * 设置音频界面的背景色
     */
    public AudioManager setColor(int color) {
        this.color = color;
        return this;
    }

    public AudioManager setRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    // 启动AudioRecorderActivity
    public void record() {
        if(filePath == null){
            return;
        }
        Intent intent = new Intent(activity, AudioRecorderActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        intent.putExtra(EXTRA_COLOR, color);
        activity.startActivityForResult(intent, requestCode);
    }

    // 启动AudioPlayerActivity
    public void play(){
        if(filePath == null){
            return;
        }
        Intent intent = new Intent(activity, AudioPlayerActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        intent.putExtra(EXTRA_COLOR, color);
        activity.startActivity(intent);
    }

}