package com.demeth.massaudioplayer.frontend

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import com.demeth.massaudioplayer.R
import com.demeth0.massaudioplayer.backend.IShiraori
import com.demeth0.massaudioplayer.backend.models.objects.EventCodeMap
import com.demeth0.massaudioplayer.backend.models.objects.LoopMode
import com.demeth.massaudioplayer.frontend.components.SearchFieldAutoCompleteArrayAdapter
import com.demeth.massaudioplayer.frontend.fragments.AudioSelectionFragment
import com.demeth.massaudioplayer.frontend.fragments.HomeAudioControlsFragment
import com.demeth.massaudioplayer.frontend.service.AudioService
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable
import com.demeth.massaudioplayer.frontend.service.NotificationBuilder
import java.util.Timer
import java.util.TimerTask

/**
 * Main activity will contain the welcome page when opening the application. Will start the audio service and display a list of playable audios.
 */
class HomeActivity : AppCompatActivity(), AudioServiceBoundable {
    companion object{
        private const val HOME_HANDLERS = "HOME_"
        private const val PERMISSION_CODE=750
    }

    private lateinit var viewModel: HomeViewModel
    private var binder: AudioService.ServiceBinder? = null
    private var shiraori: IShiraori? = null
    private lateinit var connection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Manage permissions */
        requestExternalStoragePermission()
        if(Build.VERSION.SDK_INT>=33)
            askPermissions(Manifest.permission.POST_NOTIFICATIONS)

        /* Create if not the notification channel used by notifications of the application */
        NotificationBuilder.createNotificationChannel(this)
        setContentView(R.layout.activity_home)

        /* Create or retrieve the view model bound to this activity used to update UI components */
        viewModel = ViewModelProvider(this)[HomeViewModel::class]

        connectActivityToService()
    }

    private fun loadFragments(){
        val bun = Bundle()
        bun.putBinder("audio_service",binder)
        supportFragmentManager.beginTransaction().replace(R.id.controller_fragment_container, HomeAudioControlsFragment::class.java,bun).setReorderingAllowed(true).commit()

        val controller: FragmentContainerView = findViewById(R.id.controller_fragment_container)
        viewModel.getControllerVisibility().observe(this) {
            if (it) {
                controller.visibility = View.VISIBLE
            } else {
                controller.visibility = View.GONE
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.audio_selection_fragment_container, AudioSelectionFragment::class.java,bun).setReorderingAllowed(true).commit()
    }

    private fun connectActivityToService(){
        /* Connect this activity to the service */
        connection = object: ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder?) {
                binder = iBinder as AudioService.ServiceBinder
                shiraori = AudioService.asInterface(iBinder)
                Log.d("[abc]","HomeActivity bound to service")

                //pre init
                //TODO bind too fast, service don't have time to init dependencies sometimes.
                shiraori?.apply {
                    setHandler("MainUI") {
                        if(it.code == EventCodeMap.EVENT_AUDIO_START){
                            ping("Event audio started")
                        }else if(it.code == EventCodeMap.EVENT_AUDIO_COMPLETED){
                            ping("Event audio completed")
                        }
                    }

                    loadFragments()
                    bindViewModel()
                }
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                Log.d("[abc]","HomeActivity disconnected from service")
                unbindViewModel()
            }
        }
        Log.d("[abc]","binding to service")
        bindService(Intent(this,AudioService::class.java),connection, Context.BIND_AUTO_CREATE)
    }

    private fun bindViewModel(){
        shiraori!!.apply {
            setHandler(HOME_HANDLERS+"random"){
                if(it.code == EventCodeMap.EVENT_RANDOM_MODE_CHANGED){
                    if(it.data !=null)
                        viewModel.setRandomModeUI(it.data as Boolean)
                }
            }

            viewModel.setRandomModeUI(isRandomModeEnabled())

            setHandler(HOME_HANDLERS+"loop"){
                if(it.code == EventCodeMap.EVENT_LOOP_MODE_CHANGED){
                    if(it.data !=null)
                        viewModel.setLoopModeUI(it.data as LoopMode)
                }
            }

            viewModel.setLoopModeUI(getLoopMode())

            setHandler(HOME_HANDLERS+"controller_visibility"){
                if(it.code == EventCodeMap.EVENT_AUDIO_START){
                    viewModel.setControllerVisibility(true)
                    unsetHandler(HOME_HANDLERS+"controller_visibility")
                }
            }
            //TODO if currently music is playing enable visibility !

            setHandler(HOME_HANDLERS+"play_pause_state"){
                if(it.code == EventCodeMap.EVENT_AUDIO_START || it.code == EventCodeMap.EVENT_AUDIO_RESUME){
                    viewModel.setPlayPauseStateUI(true)
                }else if(it.code == EventCodeMap.EVENT_AUDIO_COMPLETED || it.code == EventCodeMap.EVENT_AUDIO_PAUSED){
                    viewModel.setPlayPauseStateUI(false)
                }
            }

            setHandler(HOME_HANDLERS+"update_current_audio_info"){
                if(it.code == EventCodeMap.EVENT_AUDIO_START){
                    viewModel.setCurrentAudioUI(getCurrentAudio())
                }
            }
        }

        setupSearchBar()
        initTimestampTimer()
    }

    private var timestampTimer: Timer? = null
    private lateinit var timestampTimerTask: TimerTask

    private fun initTimestampTimer(){
        timestampTimerTask = object: TimerTask() {
            override fun run() {
                viewModel.setAudioTimestamp(shiraori!!.getTimestamp())
            }
        }
        timestampTimer = Timer(false)
        timestampTimer!!.schedule(timestampTimerTask,0,1000/15)
    }

    private fun unbindViewModel(){
        shiraori?.unsetHandler(HOME_HANDLERS+"random")
    }

    private fun setupSearchBar(){
        val searchField: AutoCompleteTextView = findViewById(R.id.search_bar)
        val searchFieldAutoCompleter  = SearchFieldAutoCompleteArrayAdapter(this,android.R.layout.simple_list_item_1)
        searchField.setAdapter(searchFieldAutoCompleter)
        searchField.threshold = 1
        searchFieldAutoCompleter.setContent(shiraori!!.getDatabaseEntries()) //TODO use MVVM

        val searchButton: ImageButton = findViewById(R.id.search_button)
        searchButton.setOnClickListener {
            viewModel.setSearchQuery(searchField.text.toString())
            searchField.clearFocus()
        }
        searchField.setOnEditorActionListener { _, actionId, _ ->
            var ret = true
            //IME Input Methode
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.setSearchQuery(searchField.text.toString())
                searchField.clearFocus()
                ret = false
            }
            //If action was consumed ? (success or fail) to close or not the keyboard
            ret
        }
    }

    private fun ping(arg: String){
        Log.d("[abc]","received call $arg")
    }

    /**
     * ask for permission
     */
    private fun askPermissions(permission: String){
        if(ContextCompat.checkSelfPermission(applicationContext, permission) ==
                PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,arrayOf(permission),PERMISSION_CODE)
        }
    }

    private fun requestExternalStoragePermission(){
        // we need to start the service first before asking for storage permission
        if(Build.VERSION.SDK_INT>=33){
            askPermissions(Manifest.permission.READ_MEDIA_AUDIO)
        }else{
            askPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * check if permission allowed or denied
     * @param requestCode code that identify our request
     * @param permissions the permissions asked
     * @param grantResults the result of the request for each permission
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "permission denied the application will not be able to read audio files", Toast.LENGTH_LONG).show()
                finish()
            }else{
                shiraori?.apply {
                    reloadDatabase(this@HomeActivity)
                }
            }
        }
    }

    override fun disconnect() {
        unbindService(connection)
        finish()
    }
}