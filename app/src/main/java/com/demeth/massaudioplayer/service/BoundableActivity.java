package com.demeth.massaudioplayer.service;

/**
 * implement to define an activity that can bind to the audio service
 * @see AudioService
 */
public interface BoundableActivity {
    void onServiceDisconnected();
    void onServiceConnection();
}
