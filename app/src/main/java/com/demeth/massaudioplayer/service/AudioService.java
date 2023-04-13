package com.demeth.massaudioplayer.service;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.demeth.massaudioplayer.audio_player.AudioPlayer;
import com.demeth.massaudioplayer.database.AlbumLoader;
import com.demeth.massaudioplayer.database.AudioLibrary;
import com.demeth.massaudioplayer.database.IdentifiedEntry;
import com.demeth.massaudioplayer.database.playlist.PlaylistManager;
import com.demeth.massaudioplayer.placeholder.PlaceholderContent;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

import java.util.Collections;

public class AudioService extends AbstractAudioService implements ViewModelStoreOwner, AudioPlayer.AudioPlayerListener {
    private ViewModelStore store;

    private ListViewModel listViewModel;
    private DiffusionViewModel diffusionViewModel;

    private AudioLibrary library;
    private AudioPlayer player;

    private PlaylistManager playlist_manager;

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return store;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public AudioLibrary getLibrary() {
        return library;
    }

    public PlaylistManager getPlaylistManager() {
        return playlist_manager;
    }

    /**
     * classe du binder pour étre lié comme il faut a la cclasse AudioService
     */
    public class AudioBinder extends AbstractAudioBinder<AudioService>{
        @Override
        protected AudioService _getService() {
            return AudioService.this;
        }
    }

    /**
     * le binder de ce service
     */
    private AudioBinder binder;

    private BroadcastReceiver earphone_receiver;

    @Override
    protected AbstractAudioBinder<? extends AbstractAudioService> getBinder() {
        return binder;
    }

    /**
     * résultat d'intent (actions de fin et de début gérés dans une super classe)
     * @param intent the intent of the client who started the service
     * @param flags the flags
     * @param startId the id of the intent
     * @return comportement futur du service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        if(intent!=null){
            switch(intent.getAction()){
                case ServiceAction.NEXT_AUDIO:
                    Log.d("service","action next");
                    player.next();
                    break;
                case ServiceAction.PREV_AUDIO:
                    Log.d("service","action previous");
                    player.previous();
                    break;
                case ServiceAction.PAUSE_AUDIO:
                    Log.d("service","action pause");
                    if(player.getState().equals(AudioPlayer.State.PAUSED))
                        player.play();
                    else
                        player.pause();
                    break;
                case ServiceAction.DEVICE_UNPLUGGED:
                    player.pause();
                    break;
            }
        }

        return ret;
    }

    private final Runnable update_loop = () -> {
        diffusionViewModel.setCurrentTime(player.getPosition());
        handler.postDelayed(AudioService.this.update_loop,100);
    };

    @Override
    public void firstInitialization() {
        AlbumLoader.open(this,this);
        binder = new AudioBinder();

        store = new ViewModelStore();
        listViewModel = new ViewModelProvider(this).get(ListViewModel.class);
        diffusionViewModel = new ViewModelProvider(this).get(DiffusionViewModel.class);

       // listViewModel.setList(PlaceholderContent.ITEMS); //TODO test list

        library=new AudioLibrary(this);
        player = new AudioPlayer(this);
        player.setListener(this);
        playlist_manager = new PlaylistManager(this,library);

        listViewModel.setList(library.getAll());

        this.handler.post(update_loop);

        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        earphone_receiver = new AudioDeviceBroadcastReceiver();
        registerReceiver(earphone_receiver, receiverFilter);
    }

    @Override
    void closeService() {
        super.closeService();
        AlbumLoader.close(this);
        store.clear();
        player.close();
        unregisterReceiver(earphone_receiver);
    }

    @Override
    public void updateNotification(boolean paused) {
        notification_builder.setPauseButtonPaused(paused);
        notification_builder.updateNotification(player.getCurrentAudio());
    }

    public void reloadLibrary(){
        library.loadMusics(this);
        listViewModel.setList(library.getAll());
    }

    @Override
    public void onPlay(AudioPlayer player) {
        updateNotification(false);
        diffusionViewModel.setEntry(player.getCurrentAudio());
        diffusionViewModel.setTime(0,player.getDuration());
        diffusionViewModel.setPaused(false);
    }

    @Override
    public void onPause(AudioPlayer player) {
        updateNotification(true);
        diffusionViewModel.setPaused(true);
    }

    @Override
    public void onCompleted(AudioPlayer player) {

    }

    @Override
    public void onPlaylistChanged(AudioPlayer player) {
        listViewModel.setQueue(player.getPlaylist(true));
    }

    @Override
    public void onResume(AudioPlayer player) {
        updateNotification(false);
        diffusionViewModel.setPaused(false);
    }

    @Override
    public void onLoopModeChanged(AudioPlayer player) {
        diffusionViewModel.setLoopMode(player.getLoopMode());
    }

    @Override
    public void onRandomModeChanged(AudioPlayer player) {
        diffusionViewModel.setRandomMode(player.isRandom());
    }
}
