package com.demeth.massaudioplayer.backend;

import android.content.Context;
import android.util.Log;

import com.demeth.massaudioplayer.backend.adapters.ApplicationAudioManager;
import com.demeth.massaudioplayer.backend.adapters.FileAudioPlayer;
import com.demeth.massaudioplayer.backend.adapters.HashMapDatabase;
import com.demeth.massaudioplayer.backend.adapters.LoadedAudioPlayerFactory;
import com.demeth.massaudioplayer.backend.adapters.LocalFileDatabaseProvider;
import com.demeth.massaudioplayer.backend.adapters.SequentialEventManager;
import com.demeth.massaudioplayer.backend.adapters.SmartAudioProvider;
import com.demeth.massaudioplayer.backend.models.adapters.AudioManager;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.adapters.AudioProvider;
import com.demeth.massaudioplayer.backend.models.adapters.Database;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;

public class Dependencies {
    EventManager event_manager;
    Database database;
    AudioManager audio_manager;
    AudioProvider audio_provider;
    private Dependencies(){}

    public static Dependencies inject_dependencies(Context context){
        Dependencies dep = new Dependencies();
        Log.d("[abc]","initializing dependencies");
        dep.event_manager = new SequentialEventManager();
        LocalFileDatabaseProvider local_provider = new LocalFileDatabaseProvider();
        dep.database = new HashMapDatabase(context,local_provider);

        AudioPlayer file_audio_player = new FileAudioPlayer(dep.event_manager,dep.database,context);

        AudioPlayerFactory audio_player_factory = new LoadedAudioPlayerFactory();
        audio_player_factory.register(AudioType.LOCAL,file_audio_player);
        dep.audio_provider = new SmartAudioProvider();
        dep.audio_manager = new ApplicationAudioManager(audio_player_factory,dep.event_manager,dep.audio_provider);

        return dep;
    }


}
