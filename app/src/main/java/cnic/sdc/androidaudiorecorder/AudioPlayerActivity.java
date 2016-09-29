package cnic.sdc.androidaudiorecorder;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;

import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayerActivity extends AppCompatActivity
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer player;         //音频播放对象
    private GLAudioVisualizationView visualizerView;    //音频播放动画

    private String filePath;    //音频文件的存放路径
    private int color;          //音频播放界面的背景色

    private Timer timer;        //计时
    private int secondsElapsed;     //播放时的耗时
    private RelativeLayout contentLayout;

    private TextView statusView;        //状态视图(playing or recording)
    private TextView timerView;         //计时视图

    private ImageButton playView;       //播放按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

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

        contentLayout = (RelativeLayout) findViewById(R.id.audio_playder_cnt);
        statusView = (TextView) findViewById(R.id.player_status);
        timerView = (TextView) findViewById(R.id.player_timer);
        playView = (ImageButton) findViewById(R.id.play_btn_2);

        contentLayout.setBackgroundColor(Util.getDarkerColor(color));
        contentLayout.addView(visualizerView, 0);   //把visualizerView放在所有view的最底层

        if(Util.isBrightColor(color)) {
            getResources().getDrawable(R.drawable.aar_ic_clear)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            getResources().getDrawable(R.drawable.aar_ic_check)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            statusView.setTextColor(Color.BLACK);
            timerView.setTextColor(Color.BLACK);
        }

        startPlaying();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
        try {
            visualizerView.onPause();
        } catch (Exception e){ }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
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
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopPlaying();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

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

    public void togglePlaying(View v){
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
                    player.setOnCompletionListener(AudioPlayerActivity.this);
                }
            });

            timerView.setText("00:00:00");
            statusView.setText(R.string.aar_playing);
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
        statusView.setText(R.string.aar_play_over);
        playView.setImageResource(R.drawable.aar_ic_play);

        if(player != null){
            try {
                player.stop();
                player.reset();
                player.release();
                player = null;
            } catch (Exception e){

            }
        }

        stopTimer();
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
        if(isPlaying()) {
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
