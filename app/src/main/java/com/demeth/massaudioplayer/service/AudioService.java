package com.demeth.massaudioplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;


import com.demeth.massaudioplayer.backend.AlbumLoader;
import com.demeth.massaudioplayer.backend.adapters.ApplicationAudioManager;
import com.demeth.massaudioplayer.backend.adapters.FileAudioPlayer;
import com.demeth.massaudioplayer.backend.adapters.HashMapDatabase;
import com.demeth.massaudioplayer.backend.adapters.LoadedAudioPlayerFactory;
import com.demeth.massaudioplayer.backend.adapters.SequentialEventManager;
import com.demeth.massaudioplayer.backend.models.adapters.AudioManager;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.adapters.Database;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;

import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;
import com.demeth.massaudioplayer.database.playlist.PlaylistManager;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

import java.util.List;

/**
 * foreground audio service that instantiate the audio player and provide all the player functionalities from the UI or the notification
 */
public class AudioService extends AbstractAudioService implements ViewModelStoreOwner {
    private ViewModelStore store;

    private ListViewModel listViewModel;
    private DiffusionViewModel diffusionViewModel;

    //private AudioPlayer player;

    private PlaylistManager playlist_manager;

    /**
     * interface implementation to use ViewModels in the service
     */
    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return store;
    }


    /**
     * @return the audio library
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * @return the playlist manager
     */
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
                    manager.play_next();
                    break;
                case ServiceAction.PREV_AUDIO:
                    Log.d("service","action previous");
                    manager.play_previous();
                    break;
                case ServiceAction.PAUSE_AUDIO:
                    Log.d("service","action pause");

                    if(manager.isPaused())
                        manager.play();
                    else
                        manager.pause();
                    break;
                case ServiceAction.DEVICE_UNPLUGGED:
                    manager.pause();
                    break;
            }
        }

        return ret;
    }

    private final Runnable update_loop = () -> {
        Timestamp stamp = AudioService.this.manager.timestamp();
        diffusionViewModel.setCurrentTime((int)(stamp.getDuration()* stamp.getProgress()));
        handler.postDelayed(AudioService.this.update_loop,100);
    };

    EventManager event_manager;
    Database database;
    AudioManager manager;

    private void inject_dependencies(Context context){

        event_manager = new SequentialEventManager();

        database = new HashMapDatabase(context);

        com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer file_audio_player = new FileAudioPlayer(event_manager,database,context);

        AudioPlayerFactory audio_player_factory = new LoadedAudioPlayerFactory();
        audio_player_factory.register(AudioType.LOCAL,file_audio_player);
        manager = new ApplicationAudioManager(audio_player_factory,event_manager);

        event_manager.registerHandler(event->{
            if(event.getCode() == EventCodeMap.EVENT_AUDIO_START){
                onPlay();
            }else if(event.getCode()==EventCodeMap.EVENT_AUDIO_COMPLETED){
                onPause();
            }
        });
    }

    /**
     * init the service for the first time
     */
    @Override
    public void firstInitialization() { //TODO entry point
        inject_dependencies(this);
        AlbumLoader.open(this,this);
        binder = new AudioBinder();

        store = new ViewModelStore();
        listViewModel = new ViewModelProvider(this).get(ListViewModel.class);
        diffusionViewModel = new ViewModelProvider(this).get(DiffusionViewModel.class);

       // listViewModel.setList(PlaceholderContent.ITEMS); //TODO test list

        com.demeth.massaudioplayer.database.AudioLibrary library=new com.demeth.massaudioplayer.database.AudioLibrary(this);
        // player = new AudioPlayer(this);
        // player.setListener(this);
        playlist_manager = new PlaylistManager(this,library);

        listViewModel.setList(database.getEntries());

        this.handler.post(update_loop);

        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        earphone_receiver = new AudioDeviceBroadcastReceiver();
        registerReceiver(earphone_receiver, receiverFilter);
    }

    /**
     * close the service and all providers
     */
    @Override
    void closeService() {
        super.closeService();
        AlbumLoader.close(this);
        store.clear();
        // player.close();
        unregisterReceiver(earphone_receiver);
    }

    /**
     * update the notification informations
     * @param paused the state of the pause button
     */
    @Override
    public void updateNotification(boolean paused) {
        notification_builder.setPauseButtonPaused(paused);
        notification_builder.updateNotification(manager.current());
    }

    /**
     * reload the content of the audio library fi new entries or access are given
     */
    public void reloadLibrary(){
        database.reload(this);
        listViewModel.setList(database.getEntries());
    }

    public void play(int index){
        manager.play(index);
    }

    public void play(Audio audio){
        manager.play(manager.get().indexOf(audio));
    }

    public void play(){
        boolean resuming = manager.isPaused();
        manager.play();
        if(resuming){
            onResume();
        }

    }

    public void set_playlist(List<Audio> audios){
        manager.set(audios);
        onPlaylistChanged();
        onPause();
    }

    public List<Audio> get_playlist(){
        return manager.get();
    }

    public Audio get_current_audio(){
        return manager.current();
    }

    public int get_loop_mode(){
        return manager.getLoopMode();
    }

    public void set_loop_mode(int mode){
        manager.loop(mode);
        onLoopModeChanged();
    }

    public void set_shuffle_mode(boolean mode){
        manager.shuffle(mode);
        onRandomModeChanged();
    }

    public boolean get_shuffle_mode(){
        return manager.isShuffled();
    }

    public boolean is_audio_paused(){
        return manager.isPaused();
    }

    public void pause(){
        manager.pause();
        onPause();
    }

    public void play_next_audio(){
        manager.play_next();
    }

    public void play_previous_audio(){
        manager.play_previous();
    }

    public void set_audio_progress(double progress){
        manager.setTimestampProgress(progress);
    }

    private void onPlay() {
        updateNotification(false);
        diffusionViewModel.setEntry(manager.current());
        diffusionViewModel.setTime(0,manager.timestamp().getDuration());
        diffusionViewModel.setPaused(false);
    }

    private void onPause() {
        updateNotification(true);
        diffusionViewModel.setPaused(true);
    }

    public void onPlaylistChanged() {
        listViewModel.setQueue(manager.get());
    }

    public void onResume() {
        updateNotification(false);
        diffusionViewModel.setPaused(false);
    }

    public void onLoopModeChanged() {
        diffusionViewModel.setLoopMode(manager.getLoopMode());
    }

    public void onRandomModeChanged() {
        diffusionViewModel.setRandomMode(manager.isShuffled());
    }
}
