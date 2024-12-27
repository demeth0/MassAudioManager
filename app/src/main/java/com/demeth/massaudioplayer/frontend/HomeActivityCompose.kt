package com.demeth.massaudioplayer.frontend

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class HomeActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContent {
           Body()
        }
    }
}

@Composable
fun Body(){
    Column(modifier = Modifier.fillMaxSize()) {
        ToolBar()
        ListSelectionBar()
        ContentList()
        PlayManager()
        TODO("<Play all> button in Box layout")
    }
}

@Composable
fun PlayManager(){
    TODO("Player")
}

@Composable
fun ContentList(){
    TODO("Lazy column")
}

@Composable
fun ListSelectionBar(){
    Row {
        TODO("Radio buttons Piste, Playlist...")
    }
}

@Composable
fun ToolBar(){
    var searchText by remember { mutableStateOf("search") }

    Row {
        TextField(
            "search",
            {searchText = it},
            modifier = Modifier
        )
        Button({
            TODO("start filter event")
        }) {
            TODO("Search button image")
        }

        Button({
            TODO("Open settings screen")
        }){
            TODO("Settings button image")
        }
    }
}