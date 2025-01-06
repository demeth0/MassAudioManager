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
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demeth.massaudioplayer.R
import com.demeth.massaudioplayer.backend.IShiraori
import com.demeth.massaudioplayer.backend.models.objects.Audio
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap
import com.demeth.massaudioplayer.backend.models.objects.LoopMode
import com.demeth.massaudioplayer.backend.models.objects.Timestamp
import com.demeth.massaudioplayer.frontend.HomeActivityCompose.States
import com.demeth.massaudioplayer.frontend.service.AudioService
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

private var shiraori: IShiraori? = null

class HomeActivityViewModel : ViewModel() {
    private val _serviceTrigger = MutableLiveData(false)
    private val _playPauseState = MutableLiveData(false)
    private val _currentAudio = MutableLiveData<Audio?>(null)
    private val _randomMode = MutableLiveData(false)
    private val _loopMode = MutableLiveData(LoopMode.NONE)
    private val _timestamp = MutableLiveData(Timestamp(0, 0.0))

    val playPauseState: LiveData<Boolean>
        get() = _playPauseState
    val currentAudio: LiveData<Audio?>
        get() = _currentAudio
    val randomMode: LiveData<Boolean>
        get() = _randomMode
    val loopMode: LiveData<LoopMode>
        get() = _loopMode
    val timestamp: LiveData<Timestamp>
        get() = _timestamp
    val serviceTrigger: LiveData<Boolean>
        get() = _serviceTrigger

    fun triggerServiceConnected(){
        _serviceTrigger.value = true
    }

    fun setPlayState(value: Boolean) {
        _playPauseState.value = value
    }

    fun setCurrentAudio(value: Audio?) {
        _currentAudio.value = value
    }

    fun setRandomMode(mode: Boolean) {
        _randomMode.value = mode
    }

    fun setLoopMode(loopMode: LoopMode) {
        _loopMode.value = loopMode
    }

    fun setTimestamp(timestamp: Timestamp) {
        _timestamp.postValue(timestamp)
    }
}

class HomeActivityCompose : ComponentActivity(), AudioServiceBoundable {
    data object States {
        lateinit var serviceTrigger: MutableState<Boolean>
        lateinit var audioList: MutableState<List<Audio>>

        lateinit var searchFilter: MutableState<String>
        lateinit var displayedAudioList: MutableState<List<Audio>>
    }

    private lateinit var connection: ServiceConnection

    private val homeViewModel by viewModels<HomeActivityViewModel>()

    private val timestampTimer by lazy {
        Timer(false)
    }

    private val timestampTimerTask by lazy {
        object : TimerTask() {
            override fun run() {
                shiraori?.apply {
                    homeViewModel.setTimestamp(
                        getTimestamp()
                    )
                }
            }
        }
    }

    private val requestPermLauncher by lazy{
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ granted->
            if (!granted) {
                Toast.makeText(this, "permission denied the application will not be able to read audio files", Toast.LENGTH_LONG).show()
                finish()
            }else{
                shiraori?.apply {
                    reloadDatabase(this@HomeActivityCompose)
                    States.audioList.value = getDatabaseEntries()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Manage permissions */
        requestExternalStoragePermission()
        if(Build.VERSION.SDK_INT>=33)
            askPermissions(Manifest.permission.POST_NOTIFICATIONS)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        homeViewModel.playPauseState.observe(this) {
            shiraori?.apply {
                if (isPaused() != it)
                    pauseAudio()
            }
        }
        homeViewModel.randomMode.observe(this) {
            shiraori?.apply {
                if (isRandomModeEnabled() != it)
                    setRandomModeEnabled(it)
            }
        }

        homeViewModel.loopMode.observe(this) {
            shiraori?.setLoopMode(it)
        }

        timestampTimer.schedule(timestampTimerTask, 0, 1000 / 15)

        setContent {
            CreateStates(States)
            Body(States,homeViewModel)
            connectActivityToService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timestampTimer.cancel()
    }

    private fun connectActivityToService() {/* Connect this activity to the service */
        connection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder?) {
                shiraori = AudioService.asInterface(iBinder)

                Log.d("[abc]", "HomeActivity bound to service")

                //pre init
                registerForEvents()
                homeViewModel.setPlayState(shiraori!!.isPaused())
                homeViewModel.setRandomMode(shiraori!!.isRandomModeEnabled())
                States.audioList.value = shiraori!!.getDatabaseEntries()
                homeViewModel.triggerServiceConnected()
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                Log.d("[abc]", "HomeActivity disconnected from service")
                shiraori = null
            }
        }

        Log.d("[abc]", "binding to service")
        bindService(Intent(this, AudioService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    fun registerForEvents() {
        shiraori!!.apply {
            setHandler("main_activity_on_database_reload") {
                when (it.code) {
                    EventCodeMap.EVENT_DATABASE_RELOADED -> {
                        States.audioList.value = getDatabaseEntries()
                    }

                    EventCodeMap.EVENT_AUDIO_START -> {
                        homeViewModel.setPlayState(false)
                        shiraori?.apply {
                            homeViewModel.setCurrentAudio(getCurrentAudio())
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    /**
     * ask for permission
     */
    private fun askPermissions(permission: String){
        if(ContextCompat.checkSelfPermission(applicationContext, permission) ==
            PackageManager.PERMISSION_DENIED){
            requestPermLauncher.launch(permission)
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

    override fun disconnect() {
        unbindService(connection)
        finish()
    }
}

@Composable
fun CreateStates(states: States) {
    states.apply {
        serviceTrigger = remember { mutableStateOf(false) }
        audioList = remember { mutableStateOf(listOf()) }

        searchFilter = remember { mutableStateOf("") }
        displayedAudioList = remember { mutableStateOf(listOf()) }

        LaunchedEffect(audioList.value, searchFilter.value) {
            displayedAudioList.value = audioList.value.filter {
                it.displayName.lowercase(
                    Locale.getDefault()
                ).contains(searchFilter.value.lowercase(Locale.getDefault()))
            }
        }
    }
}

@Composable
fun Body(states: States,viewModel: HomeActivityViewModel) {
    val serviceTrigger by viewModel.serviceTrigger.observeAsState(false)

    Log.i("compose", "recomposing with serviceTrigger: $serviceTrigger")
    if (!serviceTrigger) {
        Text("ERROR: Could not connect to service.")
        return
    }
    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            ToolBar(states.searchFilter)
            ListSelectionBar()
            Box(Modifier.weight(1.0f)){
                ContentList(states.displayedAudioList.value, viewModel)

                PlayAll(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                )
            }
            PlayManager(viewModel)
        }
    }
}

@Composable
fun PlayAll(modifier: Modifier) {
    IconButton({
        shiraori!!.apply {
            setRandomModeEnabled(true)
            playInPlaylist(getDatabaseEntries())
        }
    }, modifier.background(Color.LightGray)) {
        Icon(
            painter = painterResource(R.drawable.play_all_random),
            contentDescription = "",
            modifier = Modifier.requiredSize(32.dp)
        )
    }
}

@Composable
fun ControlButton(resource: Int, action: () -> Unit) {
    IconButton(action) {
        Icon(
            painter = painterResource(resource),
            contentDescription = "",
            modifier = Modifier
                .requiredSize(48.dp)
                .padding(8.dp),
            tint = Color.White
        )
    }
}

@Composable
fun PlayManager(viewModel: HomeActivityViewModel) {
    val playState: Boolean by viewModel.playPauseState.observeAsState(false)
    val randomState: Boolean by viewModel.randomMode.observeAsState(false)
    val audio: Audio? by viewModel.currentAudio.observeAsState()
    val loopState: LoopMode by viewModel.loopMode.observeAsState(LoopMode.NONE)
    val timestamp: Timestamp by viewModel.timestamp.observeAsState(Timestamp(0, 0.0))
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var timestampText by remember { mutableStateOf("00:00/00:00") }
    var sliderSeeking by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(timestamp) {
        if(!sliderSeeking){
            val slFutureValue = timestamp.progress.toFloat()
            if (!slFutureValue.isNaN())
                sliderPosition = slFutureValue
        }



        val current: Int = (timestamp.duration * timestamp.progress).toInt()
        timestampText = context.getString(
            R.string.timestamp,
            (current / 60000),
            (current / 1000) % 60, timestamp.duration / 60000,
            (timestamp.duration / 1000) % 60
        )
    }

    val loopButtonRes = when (loopState) {
        LoopMode.ALL -> R.drawable.loop_all
        LoopMode.SINGLE -> R.drawable.loop_one
        LoopMode.NONE -> R.drawable.loop_none
    }

    val playButtonRes: Int = if (!playState)
        android.R.drawable.ic_media_pause
    else
        android.R.drawable.ic_media_play

    val randomButtonRes: Int = if (randomState)
        R.drawable.random_enabled
    else
        R.drawable.random_none

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                audio?.displayName ?: "Nothing selected",
                Modifier.weight(1.0f),
                color = Color.White
            )
            Text(timestampText, color = Color.White)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.no_album),
                contentDescription = "",
                modifier = Modifier
                    .requiredSize(80.dp)
                    .padding(horizontal = 8.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 8.dp)
            ) {
                Row {
                    ControlButton(loopButtonRes) {
                        Log.i("PlayManager", "loop")

                        when (loopState) {
                            LoopMode.SINGLE -> {
                                viewModel.setLoopMode(LoopMode.NONE)
                            }

                            LoopMode.NONE -> {
                                viewModel.setLoopMode(LoopMode.ALL)
                            }

                            LoopMode.ALL -> {
                                viewModel.setLoopMode(LoopMode.SINGLE)
                            }
                        }
                    }
                    ControlButton(android.R.drawable.ic_media_previous) {
                        Log.i("PlayManager", "prev")
                        shiraori?.apply {
                            skipToPreviousAudio()
                        }
                    }
                    ControlButton(playButtonRes) {
                        Log.i("PlayManager", "Play")
                        viewModel.setPlayState(!playState)
                    }
                    ControlButton(android.R.drawable.ic_media_next) {
                        Log.i("PlayManager", "next")
                        viewModel.setCurrentAudio(null)
                        shiraori?.apply {
                            skipToNextAudio()
                        }
                    }
                    ControlButton(randomButtonRes) {
                        Log.i("PlayManager", "random")
                        viewModel.setRandomMode(!randomState)
                    }
                }
                Slider(
                    value = sliderPosition,
                    valueRange = 0f..1.0f,
                    onValueChange = {
                        sliderSeeking=true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        shiraori?.setTimestamp(sliderPosition.toDouble())
                        sliderSeeking = false
                    }
                )
            }
        }
    }
}

@Composable
fun AudioEntry(audio: Audio, curAudio: Audio?) {
    var checked by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .background(colorResource(R.color.background_light),  RoundedCornerShape(6.dp))
            .fillMaxWidth()
    ) {
        if (checked)
            Checkbox(checked, { checked = it })

        Image(
            painter = painterResource(R.drawable.no_album),
            contentDescription = "",
            modifier = Modifier
                .requiredSize(44.dp)
                .padding(4.dp)
        )
        TextButton({
            shiraori!!.apply {
                playAudio(audio)
            }
        }) {
            if (audio == curAudio)
                Text(audio.displayName, maxLines = 2, color = colorResource(R.color.foreground))
            else
                Text(audio.displayName, maxLines = 2, color = Color.White)
        }
    }
}

@Composable
fun ContentList(audioList: List<Audio>, viewModel: HomeActivityViewModel) {
    val curAudio by viewModel.currentAudio.observeAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.background(colorResource(R.color.background))
    ) {
        items(audioList.size, key = {
            audioList[it].hashCode()
        }, itemContent = {
            AudioEntry(audioList[it], curAudio)
        })
    }
}

@Composable
fun ListSelectionBar() {
    val radioOptions = listOf("ALL", "PLAYLIST", "QUEUE")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

    Row(Modifier.selectableGroup()) {
        radioOptions.forEach{ text ->
            Text(text,Modifier.selectable(
                selected = selectedOption==text,
                onClick = { onOptionSelected(text) },
                role = Role.RadioButton
            ))
        }
    }
}

@Composable
fun ToolBar(searchFilter: MutableState<String>) {
    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val filterContent = {
        searchFilter.value = searchText
    }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.padding(horizontal = 2.dp),
            placeholder = { Text("search") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                filterContent()
                keyboardController?.hide()
            }),
            maxLines = 1
        )
        Button(
            filterContent, modifier = Modifier
                .requiredSize(48.dp)
                .padding(0.dp)
        ) {
            Image(
                painter = painterResource(android.R.drawable.ic_menu_search),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.requiredSize(32.dp)
            )
        }

        Button(
            {
                TODO("Open settings screen")
            }, modifier = Modifier
                .requiredSize(48.dp)
                .padding(0.dp)
        ) {
            Image(
                painter = painterResource(android.R.drawable.ic_menu_manage),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.requiredSize(32.dp)
            )
        }
    }
}
