package com.example.lolnk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lolnk.ui.ChatScreen
import com.example.lolnk.ui.ContactListScreen
import com.example.lolnk.ui.theme.LolnkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as LolnkApplication).networkService.connect()

        setContent {
            LolnkTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "contact_list") {
                    composable("contact_list") {
                        ContactListScreen(application as LolnkApplication, navController)
                    }
                    composable("chat/{nodeId}") { backStackEntry ->
                        val nodeId = backStackEntry.arguments?.getString("nodeId")?.toIntOrNull() ?: 0
                        ChatScreen(application as LolnkApplication, nodeId)
                    }
                }
            }
        }
    }

        override fun onDestroy() {

            super.onDestroy()

            (application as LolnkApplication).networkService.disconnect()

        }

    }

    