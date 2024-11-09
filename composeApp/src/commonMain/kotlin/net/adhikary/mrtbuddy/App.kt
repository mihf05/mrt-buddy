package net.adhikary.mrtbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.adhikary.mrtbuddy.dao.DemoDao
import net.adhikary.mrtbuddy.database.AppDatabase
import net.adhikary.mrtbuddy.managers.RescanManager
import net.adhikary.mrtbuddy.model.CardState
import net.adhikary.mrtbuddy.model.Transaction
import net.adhikary.mrtbuddy.nfc.getNFCManager
import net.adhikary.mrtbuddy.ui.screens.MainScreen
import net.adhikary.mrtbuddy.ui.theme.MRTBuddyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(dao: DemoDao) {
    val scope = rememberCoroutineScope()
    val nfcManager = getNFCManager()
    val rescanManager = RescanManager.getInstance()
    val cardState by nfcManager.cardState.collectAsStateWithLifecycle()
    val transactions by nfcManager.transactions.collectAsStateWithLifecycle()

    val McardState = remember { mutableStateOf<CardState>(CardState.WaitingForTap) }
    val Mtransactions = remember { mutableStateOf<List<Transaction>>(emptyList()) }

    if(rescanManager.isRescanRequested.value){
        nfcManager.startScan()
        rescanManager.resetRescanRequest()
    }

    scope.launch {
        nfcManager.transactions.collectLatest {
            Mtransactions.value = it
        }
    }
    scope.launch {
        nfcManager.cardState.collectLatest {
            McardState.value = it
        }
    }

    nfcManager.startScan()
    
    MRTBuddyTheme {
        MainScreen(
            cardState = McardState.value,
            transactions = Mtransactions.value
        )
    }
}
