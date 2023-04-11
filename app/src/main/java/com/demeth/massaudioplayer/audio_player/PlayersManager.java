package com.demeth.massaudioplayer.audio_player;

import android.content.Context;
import android.provider.MediaStore;

import com.demeth.massaudioplayer.audio_player.implementation.LocalAudioPlayer;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class PlayersManager implements Playable {
    private final Context context;
    private AbstractAudioPlayer<? extends IdentifiedEntry> active_player=null;
    private final LinkedList<AbstractAudioPlayer<? extends IdentifiedEntry>> players=new LinkedList<>();

    public PlayersManager(Context context){
        this.context = context;
        players.add(new LocalAudioPlayer(this));
    }

    public Context getContext(){
        return context;
    }

    public void close(){
        if(active_player!=null){
            active_player.close();
        }
    }

    //region events
    public abstract void onPlay();

    public abstract void onCompleted();

    public abstract void onPause();

    public abstract void onResume();

    public void load(IdentifiedEntry a) {
        if(active_player!=null && active_player.type.equals(a.getType())){
            //si le lecteur actuel est déja le bon lecteur a utiliser
            active_player.load(a);
        }else{
            //sinon charger le lecteur correspondant
            if(active_player!=null) active_player.close();
            Iterator<AbstractAudioPlayer<? extends IdentifiedEntry>> it = players.iterator();
            if(active_player==null)active_player=it.next();
            while(!active_player.type.equals(a.getType()) && it.hasNext()){
                active_player = it.next();
            }
            //si le lecteur existe
            if(active_player.type.equals(a.getType())){
                active_player.open();
                active_player.load(a);
            }else{
                active_player=null;
            }

        }
    }

    @Override
    public boolean play() {
        return active_player!=null && active_player.play();
    }

    @Override
    public boolean pause() {
        return active_player!=null && active_player.pause();
    }

    @Override
    public boolean resume() {
        return active_player!=null && active_player.resume();
    }

    @Override
    public boolean seekTo(int pos) {
        return active_player!=null && active_player.seekTo(pos);
    }

    @Override
    public int getDuration() {
        return active_player!=null?active_player.getDuration():0;
    }

    @Override
    public int getPosition() {
        return active_player!=null?active_player.getPosition():0;
    }
    //endregion
}
