package com.demeth.massaudioplayer.audio_player;

import android.content.Context;
import android.provider.MediaStore;

import com.demeth.massaudioplayer.audio_player.implementation.LocalAudioPlayer;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * manage a set of AudioPlayer implementation and determine which one to use depending on the audio track given. Dynamically load and unload players resources allocations.
 */
public abstract class PlayersManager implements Playable {
    private final Context context;
    private AbstractAudioPlayer<? extends IdentifiedEntry> active_player=null;
    private final LinkedList<AbstractAudioPlayer<? extends IdentifiedEntry>> players=new LinkedList<>();

    /**
     * build a manager and base all initialisations to the given context, should be a foreground service context
     * @param context the context bound to the manager
     */
    public PlayersManager(Context context){
        this.context = context;
        players.add(new LocalAudioPlayer(this));
    }

    /**
     * @return the context bound to this manager
     */
    public Context getContext(){
        return context;
    }

    /**
     * free all resources
     */
    public void close(){
        if(active_player!=null){
            active_player.close();
        }
    }

    //region events

    /**
     * handler for play callback
     */
    public abstract void onPlay();

    /**
     * handler for completed callback
     */
    public abstract void onCompleted();

    /**
     * handler for pause callback
     */
    public abstract void onPause();

    /**
     * handler for resume callback
     */
    public abstract void onResume();

    /**
     * load a track in the corresponding audio player
     * @param a the track to load
     */
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
