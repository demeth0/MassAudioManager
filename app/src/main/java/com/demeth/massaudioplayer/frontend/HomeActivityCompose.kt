package com.demeth.massaudioplayer.frontend

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.demeth.massaudioplayer.R
import com.demeth.massaudioplayer.frontend.HomeActivityCompose.States
import com.demeth.massaudioplayer.frontend.service.AudioService
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable
import com.demeth0.massaudioplayer.backend.IShiraori
import com.demeth0.massaudioplayer.backend.models.objects.Audio
import com.demeth0.massaudioplayer.backend.models.objects.LoopMode
import com.demeth0.massaudioplayer.backend.models.objects.Timestamp
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

enum class ListCategories {
    ALL, PLAYLIST, QUEUE
}

class HomeActivityCompose : ComponentActivity(), AudioServiceBoundable {
    data object States {
        lateinit var searchFilter: MutableState<String>
        lateinit var displayedAudioList: MutableState<List<Audio>>

        lateinit var category: MutableState<ListCategories>
    }

    private lateinit var connection: ServiceConnection

    private val requestPermLauncher by lazy {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(
                    this,
                    "permission denied the application will not be able to read audio files",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                homeViewModel.reloadAudioList(this@HomeActivityCompose)
            }
        }
    }

    private val homeViewModel by viewModels<ShiraoriViewModel>()

    private val timestampTimer by lazy {
        Timer(false)
    }

    private val timestampTimerTask by lazy {
        object : TimerTask() {
            override fun run() {
                homeViewModel.updateTimestamp()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)/* Manage permissions */
        // we need to start the service first before asking for storage permission
        askPermissions(Manifest.permission.READ_MEDIA_AUDIO)
        askPermissions(Manifest.permission.POST_NOTIFICATIONS)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        timestampTimer.schedule(timestampTimerTask, 0, 1000 / 15)

        setContent {
            CreateStates(States, homeViewModel)
            Body(States, homeViewModel)
            connectActivityToService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timestampTimer.cancel()
    }

    private fun connectActivityToService() {/* Connect this activity to the service */
        var shiraori: IShiraori?
        connection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder?) {
                shiraori = AudioService.asInterface(iBinder)

                Log.d("[abc]", "HomeActivity bound to service")

                //pre init
                homeViewModel.connectService(shiraori!!, this@HomeActivityCompose)
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                Log.d("[abc]", "HomeActivity disconnected from service")
                shiraori = null
            }
        }

        Log.d("[abc]", "binding to service")
        bindService(Intent(this, AudioService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    /**
     * ask for permission
     */
    private fun askPermissions(permission: String) {
        if (ContextCompat.checkSelfPermission(
                applicationContext, permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermLauncher.launch(permission)
        }
    }

    override fun disconnect() {
        unbindService(connection)
        finish()
    }
}

@Composable
fun CreateStates(states: States, viewModel: ShiraoriViewModel) {
    val randomMode by viewModel.randomMode.observeAsState()
    val audioList by viewModel.audioList.observeAsState()

    states.apply {

        category = remember { mutableStateOf(ListCategories.ALL) }

        searchFilter = remember { mutableStateOf("") }
        displayedAudioList = remember { mutableStateOf(listOf()) }

        LaunchedEffect(audioList, searchFilter.value, category.value, randomMode) {
            when (category.value) {
                ListCategories.ALL -> {
                    Log.i("aaa", "$audioList")
                    displayedAudioList.value = audioList!!.filter {
                        it.displayName.lowercase(
                            Locale.getDefault()
                        ).contains(searchFilter.value.lowercase(Locale.getDefault()))
                    }.sorted()
                }

                ListCategories.PLAYLIST -> {
                    displayedAudioList.value = listOf()
                }

                ListCategories.QUEUE -> {
                    displayedAudioList.value = viewModel.getWaitingList() ?: emptyList()
                }
            }
        }
    }
}

@Composable
fun Body(states: States, viewModel: ShiraoriViewModel) {
    val serviceTrigger by viewModel.serviceTrigger.observeAsState(false)

    Log.i("compose", "recomposing with serviceTrigger: $serviceTrigger")
    if (!serviceTrigger) {
        Text("ERROR: Could not connect to service.")
        return
    }
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.background_mid_deep))
        ) {
            ToolBar(states.searchFilter)
            ListSelectionBar(states.category)
            Box(Modifier.weight(1.0f)) {
                ContentList(states.displayedAudioList.value, viewModel)

                PlayAll(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp), viewModel
                )
            }
            PlayManager(viewModel)
        }
    }
}

@Composable
fun PlayAll(modifier: Modifier, viewModel: ShiraoriViewModel) {
    IconButton({
        viewModel.playAllAudio()
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
fun PlayManager(viewModel: ShiraoriViewModel) {
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
        if (!sliderSeeking) {
            val slFutureValue = timestamp.progress.toFloat()
            if (!slFutureValue.isNaN()) sliderPosition = slFutureValue
        }


        val current: Int = (timestamp.duration * timestamp.progress).toInt()
        timestampText = context.getString(
            R.string.timestamp,
            (current / 60000),
            (current / 1000) % 60,
            timestamp.duration / 60000,
            (timestamp.duration / 1000) % 60
        )
    }

    val loopButtonRes = when (loopState) {
        LoopMode.ALL -> R.drawable.loop_all
        LoopMode.SINGLE -> R.drawable.loop_one
        LoopMode.NONE -> R.drawable.loop_none
    }

    val playButtonRes: Int = if (!playState) android.R.drawable.ic_media_pause
    else android.R.drawable.ic_media_play

    val randomButtonRes: Int = if (randomState) R.drawable.random_enabled
    else R.drawable.random_none

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
                audio?.displayName ?: "Nothing selected", Modifier.weight(1.0f), color = Color.White
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
                        viewModel.skipToPrevAudio()
                    }
                    ControlButton(playButtonRes) {
                        Log.i("PlayManager", "Play")
                        viewModel.setPlayState(!playState)
                    }
                    ControlButton(android.R.drawable.ic_media_next) {
                        Log.i("PlayManager", "next")
                        viewModel.setCurrentAudio(null)
                        viewModel.skipToNextAudio()
                    }
                    ControlButton(randomButtonRes) {
                        Log.i("PlayManager", "random")
                        viewModel.setRandomMode(!randomState)
                    }
                }
                Slider(value = sliderPosition, valueRange = 0f..1.0f, onValueChange = {
                    sliderSeeking = true
                    sliderPosition = it
                }, onValueChangeFinished = {
                    viewModel.setTimestamp(sliderPosition.toDouble())

                    sliderSeeking = false
                }, colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    inactiveTickColor = colorResource(R.color.background_mid_deep),
                    activeTrackColor = colorResource(R.color.foreground)
                )
                )
            }
        }
    }
}

@Composable
fun AudioEntry(audio: Audio, curAudio: Audio?, viewModel: ShiraoriViewModel) {
    var checked by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
        .background(
            colorResource(R.color.background_light), RoundedCornerShape(6.dp)
        )
        .fillMaxWidth()
        .clickable {
            viewModel.playAudio(audio)
        }) {
        if (checked) Checkbox(checked, { checked = it })
        Image(
            painter = painterResource(R.drawable.no_album),
            contentDescription = "",
            modifier = Modifier
                .requiredSize(44.dp)
                .padding(4.dp)
        )

        if (audio == curAudio) {
            Text(
                audio.displayName, maxLines = 2, color = colorResource(R.color.foreground)
            )
        } else {
            Text(audio.displayName, maxLines = 2, color = Color.White)
        }

    }
}

@Composable
fun ContentList(audioList: List<Audio>, viewModel: ShiraoriViewModel) {
    val curAudio by viewModel.currentAudio.observeAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .background(colorResource(R.color.background))
            .padding(2.dp)
    ) {
        items(audioList.size, key = {
            audioList[it].hashCode()
        }, itemContent = {
            AudioEntry(audioList[it], curAudio, viewModel)
        })
    }
}

@Composable
fun Modifier.radioButtonSelectBackground(selected: Boolean): Modifier {
    return if (selected) this.background(
        Brush.verticalGradient(
            listOf(
                colorResource(R.color.foreground),
                colorResource(R.color.background_light),
                colorResource(R.color.foreground)
            )
        ), RoundedCornerShape(5.dp)
    )
    else this.background(
        colorResource(R.color.background_very_light), RoundedCornerShape(5.dp)
    )
}

@Composable
fun ListSelectionBar(selectedOption: MutableState<ListCategories>) {
    Row(
        Modifier
            .selectableGroup()
            .padding(4.dp)
    ) {
        ListCategories.entries.forEach { cat ->

            Text(
                text = cat.name,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .selectable(
                        selected = selectedOption.value == cat,
                        onClick = { selectedOption.value = cat },
                        role = Role.RadioButton,
                    )
                    .radioButtonSelectBackground(selectedOption.value == cat)
                    .padding(
                        horizontal = 8.dp, vertical = 8.dp
                    )
            )
            Spacer(Modifier.padding(horizontal = 1.dp))
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
        Modifier
            .fillMaxWidth()
            .padding(4.dp, 8.dp, 4.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BasicTextField(value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .padding(0.dp, 0.dp, 4.dp, 0.dp)
                .background(Color.White, RoundedCornerShape(6.dp))
                .height(36.dp)
                .weight(1.0f),
            // placeholder = { Text("search") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                filterContent()
                keyboardController?.hide()
            }),
            maxLines = 1,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp, 0.dp, 4.dp, 0.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchText.isEmpty()) Text("search", color = Color.LightGray)
                    innerTextField()
                }
            })
        Button(
            filterContent, modifier = Modifier
                .requiredSize(48.dp, 40.dp)
                .padding(horizontal = 4.dp)
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
                .requiredSize(48.dp, 40.dp)
                .padding(horizontal = 4.dp)
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
