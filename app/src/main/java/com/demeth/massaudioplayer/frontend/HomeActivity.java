package com.demeth.massaudioplayer.frontend;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.Dependencies;
import com.demeth.massaudioplayer.backend.Shiraori;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.backend.models.objects.LoopMode;
import com.demeth.massaudioplayer.frontend.components.SearchFieldAutoCompleteArrayAdapter;
import com.demeth.massaudioplayer.frontend.fragments.AudioSelectionFragment;
import com.demeth.massaudioplayer.frontend.fragments.HomeAudioControlsFragment;
import com.demeth.massaudioplayer.frontend.service.AudioService;
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable;
import com.demeth.massaudioplayer.frontend.service.NotificationBuilder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Main activity will contain the welcome page when opening the application. Will start the audio service and display a list of playables audios.
 */
public class HomeActivity extends AppCompatActivity implements AudioServiceBoundable {
    private static final String HOME_HANDLERS = "HOME_";
    private HomeViewModel viewModel;
    private final static int PERMISSION_CODE=750;
    private AudioService.ServiceBinder binder;
    private AudioService service=null;
    ServiceConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Manage permissions */
        requestExternalStoragePermission();
        if(Build.VERSION.SDK_INT>=33)
            askPermissions(Manifest.permission.POST_NOTIFICATIONS);

        /* Create if not the notification channel used by notifications of the application */
        NotificationBuilder.createNotificationChannel(this);
        setContentView(R.layout.activity_home);

        /* Create or retrieve the view model bound to this activity used to update UI componennts */
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        connect_activity_to_service();
    }

    private void loadFragments(){
        Bundle bun = new Bundle();
        bun.putBinder("audio_service",binder);
        getSupportFragmentManager().beginTransaction().replace(R.id.controller_fragment_container, HomeAudioControlsFragment.class,bun).setReorderingAllowed(true).commit();

        FragmentContainerView controller = findViewById(R.id.controller_fragment_container);
        viewModel.getControllerVisibility().observe(this, aBoolean -> {
            if(aBoolean){
                controller.setVisibility(View.VISIBLE);
            }else{
                controller.setVisibility(View.GONE);
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.audio_selection_fragment_container, AudioSelectionFragment.class,bun).setReorderingAllowed(true).commit();
    }

    private void connect_activity_to_service(){
        /* Connect this activity to the service */
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = (AudioService.ServiceBinder)iBinder;
                Log.d("[abc]","HomeActivity bound to service");

                //pre init
                service = binder.getService(HomeActivity.this);
                //TODO bind too fast, service don't have time to init dependencies sometimes.

                Shiraori.setHandler("MainUI", event->{
                            if(event.getCode() == EventCodeMap.EVENT_AUDIO_START){
                                ping("Event audio started");
                            }else if(event.getCode()==EventCodeMap.EVENT_AUDIO_COMPLETED){
                                ping("Event audio completed");
                            }}
                        , service.getDependencies());
                loadFragments();
                bindViewModel(service.getDependencies());
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("[abc]","HomeActivity disconnected from service");
                unbindViewModel(service.getDependencies());
            }
        };
        Log.d("[abc]","binding to service");
        bindService(new Intent(this,AudioService.class),connection, Context.BIND_AUTO_CREATE);
    }

    private void bindViewModel(Dependencies dep){
        Shiraori.setHandler(HOME_HANDLERS+"random",event -> {
            if(event.getCode() == EventCodeMap.EVENT_RANDOM_MODE_CHANGED){
                if(event.getData()!=null)
                    viewModel.setRandomModeUI((boolean)event.getData());
            }
        }, dep);

        viewModel.setRandomModeUI(Shiraori.isRandomModeEnabled(dep));

        Shiraori.setHandler(HOME_HANDLERS+"loop",event -> {
            if(event.getCode() == EventCodeMap.EVENT_LOOP_MODE_CHANGED){
                if(event.getData()!=null)
                    viewModel.setLoopModeUI((LoopMode)event.getData());
            }
        },dep);

        viewModel.setLoopModeUI(Shiraori.getLoopMode(dep));

        Shiraori.setHandler(HOME_HANDLERS+"controller_visibility",event -> {
            if(event.getCode() == EventCodeMap.EVENT_AUDIO_START){
                viewModel.setControllerVisibility(true);
                Shiraori.unsetHandler(HOME_HANDLERS+"controller_visibility",dep);
            }
        },dep);
        //TODO if currently music is playing enable visibility !

        Shiraori.setHandler(HOME_HANDLERS+"play_pause_state",event -> {
            if(event.getCode() == EventCodeMap.EVENT_AUDIO_START || event.getCode() == EventCodeMap.EVENT_AUDIO_RESUME){
                viewModel.setPlayPauseStateUI(true);
            }else if(event.getCode() == EventCodeMap.EVENT_AUDIO_COMPLETED || event.getCode() == EventCodeMap.EVENT_AUDIO_PAUSED){
                viewModel.setPlayPauseStateUI(false);
            }
        },dep);

        Shiraori.setHandler(HOME_HANDLERS+"update_current_audio_info",event -> {
            if(event.getCode() == EventCodeMap.EVENT_AUDIO_START){
                viewModel.setCurrentAudioUI(Shiraori.getCurrentAudio(dep));
            }
        },dep);

        setupSearchBar(dep);
        init_timestamp_timer(dep);
    }

    private Timer timestamp_timer = null;
    private TimerTask timestamp_timer_task;

    private void init_timestamp_timer(Dependencies dep){
        timestamp_timer_task = new TimerTask() {
            @Override
            public void run() {
                viewModel.setAudioTimestamp(Shiraori.getTimestamp(dep));
            }
        };
        timestamp_timer = new Timer(false);
        timestamp_timer.schedule(timestamp_timer_task,0,1000/15);
    }

    private void unbindViewModel(Dependencies dep){
        Shiraori.unsetHandler(HOME_HANDLERS+"random",dep);
    }

    private void setupSearchBar(Dependencies dep){
        AutoCompleteTextView search_field = findViewById(R.id.search_bar);
        SearchFieldAutoCompleteArrayAdapter searchFieldAutoCompleter = new SearchFieldAutoCompleteArrayAdapter(this,android.R.layout.simple_list_item_1);
        search_field.setAdapter(searchFieldAutoCompleter);
        search_field.setThreshold(1);
        searchFieldAutoCompleter.setContent(Shiraori.getDatabaseEntries(dep)); //TODO use MVVM

        ImageButton search_button = findViewById(R.id.search_button);
        search_button.setOnClickListener(v -> {
            viewModel.setSearchQuery(search_field.getText().toString());
            search_field.clearFocus();
        });
        search_field.setOnEditorActionListener((v, actionId, event) -> {
            boolean ret = true;
            //IME Input Methode
            if(actionId== EditorInfo.IME_ACTION_DONE){
                viewModel.setSearchQuery(search_field.getText().toString());
                search_field.clearFocus();
                ret = false;
            }
            //If action was consumed ? (success or fail) to close or not the keyboard
            return ret;
            });
    }

    private void ping(String arg){
        Log.d("[abc]","received call "+arg);
    }

    /**
     * ask for permission
     */
    private void askPermissions(String permission){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), permission) ==
                PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{permission},PERMISSION_CODE);
        }
    }

    private void requestExternalStoragePermission(){
        // we need to start the service first before asking for storage permission
        if(Build.VERSION.SDK_INT>=33){
            askPermissions(Manifest.permission.READ_MEDIA_AUDIO);
        }else{
            askPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * check if permission allowed or denied
     * @param requestCode code that identify our request
     * @param permissions the permissions asked
     * @param grantResults the result of the request for each permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "permission denied the application will not be able to read audio files", Toast.LENGTH_LONG).show();
                finish();
            }else{
                if(service!=null)
                    Shiraori.reloadDatabase(this, service.getDependencies());
            }
        }
    }

    @Override
    public void disconnect() {
        unbindService(connection);
        finish();
    }
}