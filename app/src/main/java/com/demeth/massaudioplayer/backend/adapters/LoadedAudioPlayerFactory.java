package com.demeth.massaudioplayer.backend.adapters;

import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;

import java.util.HashMap;

public class LoadedAudioPlayerFactory implements AudioPlayerFactory {
    private HashMap<AudioType, AudioPlayer> registered_dependencies;

    public LoadedAudioPlayerFactory(){

        registered_dependencies=new HashMap<>();
    }

    @Override
    public void register(AudioType type,AudioPlayer player){
        registered_dependencies.put(type,player);
    }

    @Override
    public AudioPlayer provide(AudioType type) throws PlayerNotImplementedException{
        if(registered_dependencies.containsKey(type))
            return this.registered_dependencies.get(type);
        throw new PlayerNotImplementedException(type.toString());
    }
}
