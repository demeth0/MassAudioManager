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
import com.demeth.massaudioplayer.backend.adapters.LocalFileDatabaseProvider;
import com.demeth.massaudioplayer.backend.adapters.SequentialEventManager;
import com.demeth.massaudioplayer.backend.adapters.SmartAudioProvider;
import com.demeth.massaudioplayer.backend.models.adapters.AudioManager;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayer;
import com.demeth.massaudioplayer.backend.models.adapters.AudioPlayerFactory;
import com.demeth.massaudioplayer.backend.models.adapters.AudioProvider;
import com.demeth.massaudioplayer.backend.models.adapters.Database;
import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.AudioType;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.backend.models.objects.Playlist;
import com.demeth.massaudioplayer.backend.models.objects.Timestamp;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

import java.util.List;

/**
 * foreground audio service that instantiate the audio player and provide all the player functionalities from the UI or the notification
 */
public class AudioService extends AbstractAudioService implements ViewModelStoreOwner {
    public enum AudioListSource{
        QUEUE,
        PLAYLIST
    }

    private ViewModelStore store;

    private ListViewModel listViewModel;
    private DiffusionViewModel diffusionViewModel;

    //private AudioPlayer player;

    //private PlaylistManager playlist_manager;

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
    //public PlaylistManager getPlaylistManager() {
    //    return null;//playlist_manager;
    //}

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
        if(intent!=null && intent.getAction()!=null){
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
    AudioProvider audio_provider;

    private void inject_dependencies(Context context){

        event_manager = new SequentialEventManager();
        LocalFileDatabaseProvider local_provider = new LocalFileDatabaseProvider();
        database = new HashMapDatabase(context, local_provider);

        AudioPlayer file_audio_player = new FileAudioPlayer(event_manager,database,context);

        AudioPlayerFactory audio_player_factory = new LoadedAudioPlayerFactory();
        audio_player_factory.register(AudioType.LOCAL,file_audio_player);
        audio_provider = new SmartAudioProvider();
        manager = new ApplicationAudioManager(audio_player_factory,event_manager,audio_provider);

        event_manager.registerHandler("Service",event->{
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

        //com.demeth.massaudioplayer.database.AudioLibrary library=new com.demeth.massaudioplayer.database.AudioLibrary(this);
        // player = new AudioPlayer(this);
        // player.setListener(this);
        //playlist_manager = new PlaylistManager(this,library);

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
        notification_builder.updateNotification(audio_provider.get_audio());
    }

    /**
     * reload the content of the audio library fi new entries or access are given
     */
    public void reloadLibrary(){
        database.reload(this);
        listViewModel.setList(database.getEntries());
    }

    public void play(int index,AudioListSource source){
        switch(source){
            case PLAYLIST:
                audio_provider.set_audio_from_playlist(index);
                break;
            case QUEUE:
                audio_provider.set_audio_from_queue(index);
                break;
        }
        manager.play();
    }

    public void play(Audio audio, AudioListSource source){
        List<Audio> lookup;
        switch(source){
            case PLAYLIST:
                lookup=audio_provider.view_playlist();
                audio_provider.set_audio_from_playlist(lookup.indexOf(audio));
                break;
            case QUEUE:
                lookup=audio_provider.view_queue();
                audio_provider.set_audio_from_queue(lookup.indexOf(audio));
                break;
        }

        manager.play();
    }

    public void play(){
        boolean resuming = manager.isPaused();
        manager.play();
        if(resuming){
            onResume();
        }

    }

    public void set_playlist(List<Audio> audios){
        audio_provider.set_playlist(new Playlist(audios));
        onPlaylistChanged();
        onPause();
    }

    public List<Audio> get_playlist(){
        List<Audio> all_audio = audio_provider.view_queue();
        all_audio.addAll(audio_provider.view_playlist());
        return all_audio;
    }

    public Audio get_current_audio(){
        return audio_provider.get_audio();
    }

    public LoopMode get_loop_mode(){
        return audio_provider.get_loop();
    }

    public void set_loop_mode(LoopMode mode){
        audio_provider.set_loop(mode);
        onLoopModeChanged();
    }

    public void set_shuffle_mode(boolean mode){
        audio_provider.set_random(mode);
        onRandomModeChanged();
    }

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    public boolean get_shuffle_mode(){
            return audio_provider.get_random();
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
        diffusionViewModel.setEntry(audio_provider.get_audio());
        diffusionViewModel.setTime(0,manager.timestamp().getDuration());
        diffusionViewModel.setPaused(false);
    }

    private void onPause() {
        updateNotification(true);
        diffusionViewModel.setPaused(true);
    }

    public void onPlaylistChanged() {
        listViewModel.setQueue(get_playlist());
    }

    public void onResume() {
        updateNotification(false);
        diffusionViewModel.setPaused(false);
    }

    public void onLoopModeChanged() {
        diffusionViewModel.setLoopMode(audio_provider.get_loop());
    }

    public void onRandomModeChanged() {
        diffusionViewModel.setRandomMode(audio_provider.get_random());
    }
}
