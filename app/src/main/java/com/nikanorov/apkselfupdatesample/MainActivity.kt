package com.nikanorov.apkselfupdatesample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.nikanorov.apkselfupdate.APKSelfUpdate
import com.nikanorov.apkselfupdate.value.APKSelfUpdateState
import com.nikanorov.apkselfupdatesample.ui.theme.APKSelfUpdateSampleTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apkSelfUpdate = APKSelfUpdate.instance

        setContent {
            APKSelfUpdateSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val updateState by apkSelfUpdate.state.collectAsState()

                    val  textPair by remember(updateState) {
                        mutableStateOf(
                            when (updateState) {
                                is APKSelfUpdateState.Unknown -> Pair("Check for update", "")
                                is APKSelfUpdateState.UpdateUnavailable -> Pair("Check for update", "No update found")
                                is APKSelfUpdateState.Loading -> Pair("Checking...", "Checking...")
                                is APKSelfUpdateState.UpdateAvailable -> Pair("Download update", "Update available. Version: ${(updateState as APKSelfUpdateState.UpdateAvailable).updateInfo.versionNumber}")
                                is APKSelfUpdateState.Downloading -> {
                                    val downloadPercent = (updateState as APKSelfUpdateState.Downloading).percent?.toString() ?: ""
                                    Pair("Downloading...", "Downloading $downloadPercent")
                                }
                                is APKSelfUpdateState.Downloaded -> Pair("Install update", "Successfully downloaded")
                                is APKSelfUpdateState.Error -> Pair("Retry", "Error. ${(updateState as APKSelfUpdateState.Error).message}")
                            }
                        )
                    }

                    Content(textPair.first, textPair.second, Modifier.padding(innerPadding)) {
                        apkSelfUpdate.nextStep(this)
                    }
                }
            }
        }
    }
}


@Composable
fun Content(
    buttonText: String, infoText: String, modifier: Modifier = Modifier, buttonClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = infoText,
        )
        Button(onClick = buttonClick) {
            Text(
                text = buttonText,
            )
        }
    }
}
