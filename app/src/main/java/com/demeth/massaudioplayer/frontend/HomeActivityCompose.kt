package com.demeth.massaudioplayer.frontend

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.demeth.massaudioplayer.R
import com.demeth.massaudioplayer.backend.IShiraori
import com.demeth.massaudioplayer.backend.models.objects.Audio
import com.demeth.massaudioplayer.backend.models.objects.EventCodeMap
import com.demeth.massaudioplayer.backend.models.objects.LoopMode
import com.demeth.massaudioplayer.frontend.HomeActivityCompose.States
import com.demeth.massaudioplayer.frontend.service.AudioService
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable
import java.util.*

private var shiraori: IShiraori? = null

class HomeActivityCompose : ComponentActivity(), AudioServiceBoundable {
    data object States {
        lateinit var serviceTrigger: MutableState<Boolean>
        lateinit var audioList: MutableState<List<Audio>>

        lateinit var searchFilter: MutableState<String>
        lateinit var displayedAudioList: MutableState<List<Audio>>
    }

    private lateinit var connection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContent {
            CreateStates(States)
            Body(States)
            connectActivityToService()
        }
    }

    private fun connectActivityToService() {/* Connect this activity to the service */
        connection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder?) {
                val binder = iBinder as AudioService.ServiceBinder
                shiraori = AudioService.asInterface(iBinder)

                Log.d("[abc]", "HomeActivity bound to service")

                //pre init
                registerForEvents()
                States.serviceTrigger.value = true
                States.audioList.value = shiraori!!.getDatabaseEntries()
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
                if (it.code == EventCodeMap.EVENT_DATABASE_RELOADED) {
                    States.audioList.value = getDatabaseEntries()
                }
            }
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
fun Body(states: States) {
    Log.i("compose", "recomposing with serviceTrigger: ${states.serviceTrigger}")
    if (!states.serviceTrigger.value) {
        Text("ERROR: Could not connect to service.")
        return
    }
    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            ToolBar(states.searchFilter)
            // ListSelectionBar()
            Box(Modifier.weight(1.0f)){
                ContentList(states.displayedAudioList.value)

                PlayAll(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                )
            }
            PlayManager()
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
        Icon(painter = painterResource(R.drawable.play_all_random),
            contentDescription = "",
            modifier = Modifier.requiredSize(32.dp))
    }
}

@Composable
fun ControlButton(resource : Int, action: ()->Unit){
    IconButton(action) {
        Icon(painter = painterResource(resource),
            contentDescription = "",
            modifier = Modifier.requiredSize(48.dp).padding(8.dp),
            tint = Color.White)
    }
}

@Composable
fun PlayManager(loop : LoopMode = LoopMode.NONE, playState: Boolean = false, random: Boolean = false) {
    val loopButtonRes = when(loop){
        LoopMode.ALL -> R.drawable.loop_all
        LoopMode.SINGLE -> R.drawable.loop_one
        LoopMode.NONE -> R.drawable.loop_none
    }
    val playButtonRes: Int = if(playState)
        android.R.drawable.ic_media_pause
    else
        android.R.drawable.ic_media_play
    val randomButtonRes: Int = if(random)
        R.drawable.random_enabled
    else
        R.drawable.random_none

    var sliderPosition by remember { mutableFloatStateOf(0f) }

    Column(modifier = Modifier.fillMaxWidth().background(colorResource(R.color.background))) {

        Row(Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Audio title", Modifier.weight(1.0f), color = Color.White)
            Text("00:00/00:00", color = Color.White)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.no_album),
                contentDescription = "",
                modifier = Modifier.requiredSize(80.dp).padding(horizontal = 8.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp,0.dp,8.dp,8.dp)) {
                Row {
                    ControlButton(loopButtonRes){
                        Log.i("PlayManager", "loop")
                    }
                    ControlButton(android.R.drawable.ic_media_previous){
                        Log.i("PlayManager", "prev")
                    }
                    ControlButton(playButtonRes){
                        Log.i("PlayManager", "Play")
                    }
                    ControlButton(android.R.drawable.ic_media_next){
                        Log.i("PlayManager", "next")
                    }
                    ControlButton(randomButtonRes){
                        Log.i("PlayManager", "random")
                    }
                }
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it }
                )
            }
        }
    }
}

@Composable
fun ContentList(audioList: List<Audio>) {
    LazyColumn {
        items(audioList.size, key = {
            audioList[it].hashCode()
        }, itemContent = {
            TextButton({
                shiraori!!.apply {
                    playAudio(audioList[it])
                }
            }){
                Text(audioList[it].displayName)
            }
        })
    }
}

@Composable
fun ListSelectionBar() {
    Row {
        TODO("Radio buttons Piste, Playlist...")
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
