package com.example.datetimenotificationapp_bm

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationManagerCompat
import com.example.datetimenotificationapp_bm.ui.theme.DateTimeNotificationApp_BMTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DateTimeNotificationApp_BMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DateTime(modifier = Modifier.padding(innerPadding))
                    createNotificationChannel(this)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTime(modifier: Modifier = Modifier) {
    var isTimePickerShown by remember { mutableStateOf(false) }
    var timeBtn by remember { mutableStateOf("Choose a time") }
    var hour by remember { mutableStateOf(0) }
    var minute by remember { mutableStateOf(0) }

    var isDatePickerShown by remember { mutableStateOf(false) }
    var dateBtn by remember { mutableStateOf("Choose a date") }
    var dateMillis by remember { mutableStateOf(0L) }
    val context = LocalContext.current

    if (isTimePickerShown) {
        TimePickerChooser(onConfirm = {
            hour = it.hour
            minute = it.minute
            timeBtn = "$hour:$minute"
            isTimePickerShown = false
        }) {
            isTimePickerShown = false
        }
    }

    if (isDatePickerShown) {
        DatePickerChooser(onConfirm = {
            val dateFormatter = java.text.SimpleDateFormat("dd/MM/yyyy")
            dateMillis = it.selectedDateMillis ?: 0L
            dateBtn = dateFormatter.format(dateMillis)
            isDatePickerShown = false
        }) {
            isDatePickerShown = false
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        OutlinedButton(onClick = {
            isTimePickerShown = true
        }) {
            Text(text = timeBtn)
        }
        OutlinedButton(onClick = {
            isDatePickerShown = true
        }) {
            Text(text = dateBtn)
        }
        OutlinedButton(onClick = {
            sendNotification(
                context,
                "Notification Schudeled",
                "Notification for $dateBtn at $timeBtn",
            )
            schudleNotification(hour, minute, dateMillis, context)
        }) {
            Text(text = "Send notification")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerChooser(onConfirm: (TimePickerState) -> Unit, onDismiss: () -> Unit) {
    val timePickerState = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState) }) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        },
        text = { TimePicker(state = timePickerState) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerChooser(onConfirm: (DatePickerState) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onConfirm(datePickerState) }) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        },
        content = { DatePicker(state = datePickerState) }
    )
}

@Preview(showSystemUi = true)
@Composable
private fun DateTimePreview() {
    DateTime()
}

private fun createNotificationChannel(context: Context) {
    val name = "Datetime Channel "
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel("1", name, importance)
    channel.description = "Datetime scheduled notification"

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)

}

fun sendNotification(context: Context, title: String, text: String) {

    val builder = Notification.Builder(context, "1")
        .setContentTitle(title)
        .setContentText(text)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_notifcation)

    NotificationManagerCompat.from(context).notify(99, builder.build())
}

fun schudleNotification(hour: Int, minute: Int, dateMillis: Long, context: Context) {
    val i = Intent(context, NotificationReceiver::class.java)
    i.putExtra("title", "Notification Schudeled")
    i.putExtra("text", "Notification for $dateMillis at $hour:$minute")
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        25,
        i,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    try {
        manager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            dateMillis + ((hour - 3) * 3600 * 1000) + (minute * 60 * 1000),
            pendingIntent
        )
    } catch (e: SecurityException) {

        Log.d("trace", "Error: ${e.message} ")

    }


}

