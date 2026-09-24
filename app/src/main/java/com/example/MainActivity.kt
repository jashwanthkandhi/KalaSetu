package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.KalaSetuApp
import com.example.ui.theme.KalaBackground
import com.example.ui.theme.KalaSetuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KalaSetuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KalaBackground
                ) {
                    KalaSetuApp()
                }
            }
        }
    }
}
