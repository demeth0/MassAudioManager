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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.Shiraori;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap;
import com.demeth.massaudioplayer.frontend.service.AudioService;
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable;
import com.demeth.massaudioplayer.frontend.service.NotificationBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Main activity will contain the welcome page when opening the application. Will start the audio service and display a list of playables audios.
 */
public class HomeActivity extends AppCompatActivity implements AudioServiceBoundable {

    private HomeViewModel viewModel;
    private final static int PERMISSION_CODE=750;
    private AudioService.ServiceBinder binder;
    private AudioService service;
    ServiceConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Manage permissions */
        if(Build.VERSION.SDK_INT>=33){
            askPermissions(Manifest.permission.READ_MEDIA_AUDIO);
            askPermissions(Manifest.permission.POST_NOTIFICATIONS);
        }else{
            askPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        /* Create if not the notification channel used by notifications of the application */
        NotificationBuilder.createNotificationChannel(this);
        setContentView(R.layout.activity_home);

        /* Create or retrieve the view model bound to this activity used to update UI componennts */
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        connect_activity_to_service();
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

                Shiraori.setHandler("MainUI", event->{
                            if(event.getCode() == EventCodeMap.EVENT_AUDIO_START){
                                ping("Event audio started");
                            }else if(event.getCode()==EventCodeMap.EVENT_AUDIO_COMPLETED){
                                ping("Event audio completed");
                            }}
                        , service.getDependencies());
                update_list_view(Shiraori.getDatabaseEntries(service.getDependencies()));
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("[abc]","HomeActivity disconnected from service");
            }
        };
        Log.d("[abc]","binding to service");
        bindService(new Intent(this,AudioService.class),connection, Context.BIND_AUTO_CREATE);
    }

    private void ping(String arg){
        Log.d("[abc]","received call "+arg);
    }

    private class MyAdapter extends BaseAdapter {
        List<Audio> items;
        public MyAdapter(Collection<Audio> data){
            items = new ArrayList<>(data);
        }

        // override other abstract methods here

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return items.get(i).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_view_dummy_layout, container, false);
            }

            ((TextView) convertView.findViewById(R.id.list_view_item))
                    .setText(((Audio)getItem(position)).display_name);
            return convertView;
        }
    }

    private void update_list_view(Collection<Audio> audios){
        ListView lv = findViewById(R.id.list_view);
        lv.setAdapter(new MyAdapter(audios));
        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            Audio audio = (Audio) adapterView.getItemAtPosition(i);
            Shiraori.playAudio(audio, service.getDependencies());
        });
    }

    /**
     * ask for external storage reading permission
     */
    private void askPermissions(String permission){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), permission) ==
                PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{permission},PERMISSION_CODE);
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
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "permission denied the application will not be able to read audio files", Toast.LENGTH_LONG).show();
                finish();
            }else{
                Shiraori.reloadDatabase(this, service.getDependencies());
                update_list_view(Shiraori.getDatabaseEntries(service.getDependencies()));
            }
        }
    }

    @Override
    public void disconnect() {
        unbindService(connection);
        finish();
    }
}