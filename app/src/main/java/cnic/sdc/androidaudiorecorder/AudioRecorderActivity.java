package cnic.sdc.androidaudiorecorder;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.czt.mp3recorder.MP3Recorder;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AudioRecorderActivity extends AppCompatActivity
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer player;         //音频播放对象
    private MP3Recorder mRecorder;      //mp3录制对象

    private GLAudioVisualizationView visualizerView;    //音频播放动画
    private VisualizerHandler visualizerHandler;    //自定义dBm处理器

    private MenuItem saveMenuItem;  //音频保存按钮
    private String filePath;    //音频文件的存放路径
    private int color;          //音频播放界面的背景色

    private Timer timer;        //计时
    private int secondsElapsed;     //录音或播放时的耗时
    private boolean isRecording;    //是否正在录音

    private RelativeLayout contentLayout;
    private TextView statusView;        //状态视图(playing or recording)
    private TextView timerView;         //计时视图
    private ImageButton restartView;    //重置按钮
    private ImageButton recordView;     //开始录音按钮
    private ImageButton playView;       //播放按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        if(savedInstanceState != null) {
            filePath = savedInstanceState.getString(AudioManager.EXTRA_FILE_PATH);
            color = savedInstanceState.getInt(AudioManager.EXTRA_COLOR);
        } else {
            filePath = getIntent().getStringExtra(AudioManager.EXTRA_FILE_PATH);
            color = getIntent().getIntExtra(AudioManager.EXTRA_COLOR, Color.BLACK);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Util.getDarkerColor(color)));
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.aar_ic_clear));
        }

        // 设置播放时的外观
        visualizerView = new GLAudioVisualizationView.Builder(this)
                .setLayersCount(1)
                .setWavesCount(6)
                .setWavesHeight(R.dimen.aar_wave_height)
                .setWavesFooterHeight(R.dimen.aar_footer_height)
                .setBubblesPerLayer(20)
                .setBubblesSize(R.dimen.aar_bubble_size)
                .setBubblesRandomizeSize(true)
                .setBackgroundColor(Util.getDarkerColor(color))
                .setLayerColors(new int[]{color})
                .build();

        contentLayout = (RelativeLayout) findViewById(R.id.audio_recorder_cnt);
        statusView = (TextView) findViewById(R.id.recorder_status);
        timerView = (TextView) findViewById(R.id.recorder_timer);
        restartView = (ImageButton) findViewById(R.id.restart);
        recordView = (ImageButton) findViewById(R.id.record);
        playView = (ImageButton) findViewById(R.id.play_btn_1);

        contentLayout.setBackgroundColor(Util.getDarkerColor(color));
        contentLayout.addView(visualizerView, 0);   //把visualizerView放在所有view的最底层

        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);

        if(Util.isBrightColor(color)) {
            getResources().getDrawable(R.drawable.aar_ic_clear)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            getResources().getDrawable(R.drawable.aar_ic_check)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            statusView.setTextColor(Color.BLACK);
            timerView.setTextColor(Color.BLACK);
            restartView.setColorFilter(Color.BLACK);
            recordView.setColorFilter(Color.BLACK);
            playView.setColorFilter(Color.BLACK);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            visualizerView.onResume();
        } catch (Exception e){ }
    }

    @Override
    protected void onPause() {
        stopRecording();
        try {
            visualizerView.onPause();
        } catch (Exception e){ }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED, null);
        try {
            visualizerView.release();
        } catch (Exception e){ }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(AudioManager.EXTRA_FILE_PATH, filePath);
        outState.putInt(AudioManager.EXTRA_COLOR, color);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.aar_audio_recorder, menu);
        saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setIcon(getResources().getDrawable(R.drawable.aar_ic_check));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        //两者都是返回主界面,不删除生成的音频文件
        if (i == android.R.id.home) {
            onBackPressed();    //setResult(RESULT_CANCELED),告诉MainActivity用户不想保存结果
        } else if (i == R.id.action_save) {
            selectAudio();      //setResult(RESULT_OK),告诉MainActivity用户想保留此次录音
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        if(mediaPlayer != null){

        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopPlaying();
    }

    private void selectAudio() {
        Intent audio_info = new Intent();
        audio_info.putExtra("duration", secondsElapsed);
        audio_info.putExtra("filepath", filePath);
        setResult(RESULT_OK, audio_info);
        finish();
    }

    public void toggleRecording(View v) {
        stopPlaying();
        Util.wait(100, new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });
    }

    public void togglePlaying(View v){
        stopRecording();
        Util.wait(100, new Runnable() {
            @Override
            public void run() {
                if(isPlaying()){
                    stopPlaying();
                } else {
                    startPlaying();
                }
            }
        });
    }

    /**
     * 重置此次录音
     */
    public void restartRecording(View v){
        stopRecording();
        stopPlaying();
        saveMenuItem.setVisible(false);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.aar_ic_rec);
        timerView.setText("00:00:00");
        secondsElapsed = 0;
    }

    /**
     * 开始录音
     */
    private void startRecording() {
        isRecording = true;
        saveMenuItem.setVisible(false);
        timerView.setText("00:00:00");
        statusView.setText(R.string.aar_recording);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.aar_ic_stop);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerHandler = new VisualizerHandler();
        visualizerView.linkTo(visualizerHandler);   //Link view to custom implementation of {@link DbmHandler}.

//        原版录制wav的代码
//        recorder = OmRecorder.wav(
//                new PullTransport.Default(Util.getMic(), AudioRecorderActivity.this),
//                new File(filePath));
//        recorder.startRecording();

        mRecorder = new MP3Recorder(new File(filePath));
        try {
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        startTimer();
    }

    /**
     * 停止录音
     */
    private void stopRecording() {
        isRecording = false;
        if(!isFinishing()) {
            saveMenuItem.setVisible(true);
        }
        statusView.setText("");
        statusView.setVisibility(View.INVISIBLE);
        restartView.setVisibility(View.VISIBLE);
        playView.setVisibility(View.VISIBLE);
        recordView.setImageResource(R.drawable.aar_ic_rec);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerView.release();

        if(visualizerHandler != null) {
            visualizerHandler.stop();
        }

        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder = null;
        }

        stopTimer();
    }

    /**
     * 开始播放
     */
    private void startPlaying(){
        try {
            player = new MediaPlayer();
            player.setDataSource(filePath);
            player.prepare();
            player.start();

            visualizerView.linkTo(DbmHandler.Factory.newVisualizerHandler(this, player));
            visualizerView.post(new Runnable() {
                @Override
                public void run() {
                    player.setOnCompletionListener(AudioRecorderActivity.this);
                }
            });

            timerView.setText("00:00:00");
            statusView.setText(R.string.aar_playing);
            statusView.setVisibility(View.VISIBLE);
            playView.setImageResource(R.drawable.aar_ic_pause);

            startTimer();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    private void stopPlaying(){
        statusView.setText("");
        statusView.setVisibility(View.INVISIBLE);
        playView.setImageResource(R.drawable.aar_ic_play);

        if(player != null){
            try {
                player.stop();
                player.reset();
                player.release();
            } catch (Exception e){ }
        }

        stopTimer();
    }

    /**
     * 是否正在播放
     */
    private boolean isPlaying(){
        try {
            return player != null && player.isPlaying();
        } catch (Exception e){
            return false;
        }
    }

    /**
     * 开始计时
     */
    private void startTimer(){
        stopTimer();
        secondsElapsed = 0;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    /**
     * 停止计时
     */
    private void stopTimer(){
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    /**
     * 更新时间轴
     */
    private void updateTimer() {
        if(isRecording || isPlaying()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    secondsElapsed++;
                    timerView.setText(Util.formatSeconds(secondsElapsed));
                }
            });
        }
    }
}
