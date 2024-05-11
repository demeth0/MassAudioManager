package com.demeth.massaudioplayer.backend.adapters;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.Database;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.Event;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.backend.models.objects.Metadata;

import java.io.IOException;

public class FileAudioPlayer implements AudioPlayer {
    private MediaPlayer mp;

    private final EventManager event_manager;
    private final Context context;
    private final Database database;

    private boolean timestamp_access_ok = false;

    public FileAudioPlayer(EventManager event_manager, Database database, Context context){
        this.event_manager = event_manager;
        this.context = context;
        this.database=database;
        mp = new MediaPlayer();
        mp.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) //music player
                        .setUsage(AudioAttributes.USAGE_MEDIA) //on media
                        .build()
        );

        mp.setOnCompletionListener((_mp)->{
            this.event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_COMPLETED));
        });
        mp.setOnPreparedListener((_mp)->{
            mp.start();
            timestamp_access_ok = true;
            this.event_manager.trigger(new Event(EventCodeMap.EVENT_AUDIO_START));
        });
    }

    @Override
    public void play(Audio audio) {
        // call prepare async
        //begin playing when prepare finish

        try {
            timestamp_access_ok = false;
            mp.reset();
            Metadata.FileAudioMetadata metadata = (Metadata.FileAudioMetadata) database.getMetadata(audio);
            mp.setDataSource(this.context, metadata.getUri()); //TODO find solution maybe Database
            mp.prepareAsync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pause() {
        mp.pause();
    }

    @Override
    public void resume() {
        mp.start();
    }

    @Override
    public void set_progress(double progress) {
        mp.seekTo((int) (progress * duration()));
    }

    @Override
    public double progress() {
        // return 0 if unavailable
        if(timestamp_access_ok)
            return mp.getCurrentPosition();
        else
            return 0;
    }

    @Override
    public int duration() {
        // return 0 if unavailable
        if(timestamp_access_ok)
            return mp.getDuration();
        else
            return 0;
    }

    @Override
    public void stop() {

            mp.stop();

    }
}
