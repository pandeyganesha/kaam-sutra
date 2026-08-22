package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(netWorth: Int, modifier: Modifier = Modifier){
    Column(modifier = modifier.fillMaxSize()) {
        NetWorthCard(netWorth = netWorth)
    }
}