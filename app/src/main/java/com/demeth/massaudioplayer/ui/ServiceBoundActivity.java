package com.demeth.massaudioplayer.ui;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.demeth.massaudioplayer.service.AudioService;
import com.demeth.massaudioplayer.service.BoundableActivity;
import com.demeth.massaudioplayer.service.notification.NotificationBuilder;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

public abstract class ServiceBoundActivity extends AppCompatActivity implements BoundableActivity {
    protected ListViewModel listViewModel;
    protected DiffusionViewModel diffusionViewModel;
    private ServiceConnection connection;
    protected AudioService.AudioBinder binder;

    private boolean bound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=33){
            askPermissions(Manifest.permission.READ_MEDIA_AUDIO);
        }else{
            askPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        NotificationBuilder.createNotificationChannel(this);

        //TODO bind to service
        bindToService();

    }

    private void bindToService(){
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = (AudioService.AudioBinder)iBinder;
                bound=true;
                Log.d("MainActivity","connected to the service !");

                //pre init
                AudioService service = binder.getService(ServiceBoundActivity.this);
                listViewModel = new ViewModelProvider(service).get(ListViewModel.class);
                diffusionViewModel = new ViewModelProvider(service).get(DiffusionViewModel.class);

                onServiceConnection();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("MainActivity","service disconnected from me ! probably a crash !");
                bound=false;
            }
        };
        bindService(new Intent(this,AudioService.class),connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceDisconnected() {
        Log.d("MainActivity","activity disconnected from service");
        if(bound) {
            unbindService(connection);
            bound=false;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onServiceDisconnected();
    }

    private final int PERMISSION_CODE=750;
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
                Toast.makeText(this, "permission denied the application will not be able to list  all audio files", Toast.LENGTH_LONG).show();
                finish();
            }else{
                binder.getService(ServiceBoundActivity.this).reloadLibrary();
            }

        }
    }
}
