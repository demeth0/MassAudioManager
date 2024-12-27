package com.demeth.massaudioplayer.frontend.service;

/**
 * This interface should be implemented by all activity requiring the service functionalities.
 */
public interface AudioServiceBoundable {
    /**
     * Callback called by the bound service when it close before or after the activity.
     */
    void disconnect();
}
