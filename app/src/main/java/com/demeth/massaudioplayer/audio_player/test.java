package com.demeth.massaudioplayer.audio_player;

import android.content.Context;

import com.demeth.massaudioplayer.backend.adapters.ApplicationAudioManager;
import com.demeth.massaudioplayer.backend.adapters.FileAudioPlayer;
import com.demeth.massaudioplayer.backend.adapters.HashMapDatabase;
import com.demeth.massaudioplayer.backend.adapters.LoadedAudioPlayerFactory;
import com.demeth.massaudioplayer.backend.adapters.SequentialEventManager;
import com.demeth.massaudioplayer.backend.models.adapters.AudioManager;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.adapters.Database;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;

public class test {
    public static void run(Context context){
        EventManager event_manager = new SequentialEventManager();

        Database database = new HashMapDatabase(context);

        AudioPlayer file_audio_player = new FileAudioPlayer(event_manager,database,context);

        AudioPlayerFactory audio_player_factory = new LoadedAudioPlayerFactory();
        audio_player_factory.register(AudioType.LOCAL,file_audio_player);

        AudioManager manager = new ApplicationAudioManager(audio_player_factory,event_manager);
        
    }
}
