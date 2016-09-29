package cnic.sdc.androidaudiorecorder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements AudioItemFragment.OnListFragmentInteractionListener {

    AudioItemFragment audio_frag;
    private static final int RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audio_frag = new AudioItemFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, audio_frag).commit();

        ImageButton record_btn = (ImageButton) findViewById(R.id.record_now);
        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
                String audio_path = Environment.getExternalStorageDirectory() + "/"+df.format(new Date())+".mp3";
                recordAudio(audio_path);
            }
        });

    }

    private void recordAudio(String filepath){
        AudioManager.with(MainActivity.this)
                .setFilePath(filepath)
                .setColor(getResources().getColor(R.color.recorder_bg))
                .setRequestCode(RECORD_AUDIO)
                .record();
    }

    /**
     * 录音结束后的回调函数
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RECORD_AUDIO) {
            if (resultCode == RESULT_OK && data != null) {
                String audio_path = data.getStringExtra("filepath");
                int audio_duration = data.getIntExtra("duration", 0);
                File audio_file = new File(audio_path);
                Audios.AudioItem item = new Audios.AudioItem(audio_file, new Date(), audio_duration);
                Audios.addItem(item);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "录音文件未保存", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onListFragmentInteraction(Audios.AudioItem item) {
        String filepath = item.audio.getAbsolutePath();
        playAudio(filepath);
    }

    private void playAudio(String filepath){
        AudioManager.with(MainActivity.this)
                .setFilePath(filepath)
                .setColor(getResources().getColor(R.color.recorder_bg))
                .play();
    }

}
