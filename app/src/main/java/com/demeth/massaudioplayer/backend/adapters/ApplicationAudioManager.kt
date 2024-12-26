package com.demeth.massaudioplayer.backend.adapters;

import com.demeth.massaudioplayer.backend.models.adapters.AudioManager;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.adapters.AudioProvider;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;

/**
 * Implement the version of the audio manager for Android application.
 */
public class ApplicationAudioManager implements AudioManager {
    private final static int PAUSED=1,PLAYING=0,INACTIVE=3;

    // dependencies
    private final AudioPlayerFactory audio_players_factory;
    private final AudioProvider audio_provider;

    // fields

    private int play_status=INACTIVE;

    /**
     * Create a audio manager specific for this project implementation.
     * @param player_factory The provider that will give correct adapters to read the audio entries.
     * @param event_manager The event manager to react to audio player's events.
     */
    public ApplicationAudioManager(AudioPlayerFactory player_factory, EventManager event_manager, AudioProvider audio_provider){
        this.audio_players_factory=player_factory;
        this.audio_provider=audio_provider;
        event_manager.registerHandler("AudioManager",(event)->{
            // handle events
            if(event.getCode()== EventCodeMap.EVENT_AUDIO_COMPLETED){
                audio_provider.advance_to_next();
                if(audio_provider.get_audio()==null){
                    set_play_status(INACTIVE);
                }else{
                    play();
                }
            }
        });
    }
    @Override
    public void play_previous(){
        Timestamp stamp = this.timestamp();
        if(stamp.getDuration()*stamp.getProgress()>4){
            setTimestampProgress(0d);
        }else {
            AudioPlayer player = get_audio_player();
            if(player!=null) player.stop();
            set_play_status(INACTIVE);
            audio_provider.move_to_prev();
        }
        play();
    }


    @Override
    public void play_next(){
        AudioPlayer player = get_audio_player();
        if(player!=null) player.stop();
        set_play_status(INACTIVE);
        audio_provider.move_to_next();
        play();
    }

    /**
     * @return The audio player compatible with the current audio file. TODO Or crash the app for now.
     */
    private AudioPlayer get_audio_player(){
        Audio audio = audio_provider.get_audio();
        if(audio==null)
            return null;
        try {
            return this.audio_players_factory.provide(audio.type);
        } catch (AudioPlayerFactory.PlayerNotImplementedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Timestamp timestamp() {
        AudioPlayer audio_player = get_audio_player();
        if(audio_player==null)
            return new Timestamp(0,0);
        return new Timestamp(audio_player.duration(),audio_player.progress()/audio_player.duration());
    }

    @Override
    public void setTimestampProgress(double progress) {
        AudioPlayer audio_player = get_audio_player();
        if(audio_player==null)
            return;
        audio_player.set_progress(progress);
    }

    @Override
    public void play() {
        Audio audio = audio_provider.get_audio();
        AudioPlayer audio_player = get_audio_player();

        if(play_status==PAUSED){
            audio_player.resume();
        }else{
            if(audio == null)
                return;
            audio_player.play(audio);
        }
        set_play_status(PLAYING); // Event handler will change this value in case of exceptions
    }

    @Override
    public void pause() {
        if(play_status!=PLAYING)
            return;

        AudioPlayer audio_player = get_audio_player();
        if(audio_player==null) return;
        audio_player.pause();
        set_play_status(PAUSED);
    }

    @Override
    public boolean isPaused() {
        return this.play_status!=PLAYING;
    }

    private void set_play_status(int status){
        play_status = status;
    }
}
