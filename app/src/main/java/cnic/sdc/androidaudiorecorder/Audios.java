package cnic.sdc.androidaudiorecorder;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Audios {

    public static final List<AudioItem> ITEMS = new ArrayList<>();
    private static MyAudioItemRecyclerViewAdapter audio_adapter;

    public static void addItem(AudioItem item) {
        ITEMS.add(item);
        audio_adapter.notifyDataSetChanged();
    }

    public static void setAdapter(MyAudioItemRecyclerViewAdapter adapter){
        audio_adapter = adapter;
    }

    public static class AudioItem {
        public final File audio;
        public final int duration;
        public final Date ctime;

        public AudioItem(File audio, Date ctime, int duration) {
            this.audio = audio;
            this.ctime = ctime;
            this.duration = duration;
        }

        public String getCtime(){
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            return df.format(this.ctime);
        }

        public String getDuration(){
            return String.valueOf(this.duration)+"秒";
        }

        @Override
        public String toString() {
            return audio.getName();
        }
    }
}
