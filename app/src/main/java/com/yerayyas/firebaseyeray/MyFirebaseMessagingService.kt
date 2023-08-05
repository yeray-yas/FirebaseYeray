package com.yerayyas.firebaseyeray

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by YAS on 04/07/2023
 */

const val CHANNEL_ID = "NOTIFICATION_CHANNEL"
const val CHANNEL_NAME = "tutorial2"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("TAG", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        //sendRegistrationToServer(token)
    }

    //Generar notificacion
    private fun generateNotification(title: String, message: String) {
        val intent = Intent(this, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        //var pendingIntent =   PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        //channel id, channel name
        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000,1000,1000,1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        //Attach la notificacion creada a un layout custom
        builder = builder.setContent(getRemoteView(title, message))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //verificar si android es mayor a android Oreo
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0, builder.build())


//    override fun onMessageReceived(message: RemoteMessage) {
//        // Create an executor that executes tasks in the main thread.
//        val mainExecutor = ContextCompat.getMainExecutor(this)
//
//// Execute a task in the main thread
//        mainExecutor.execute {
//           Toast.makeText(baseContext,message.notification?.title,
//               Toast.LENGTH_LONG).show()
//        }
//    }

    }

    @SuppressLint("RemoteViewLayout")
    private fun getRemoteView(title: String, message: String) : RemoteViews {
        val remoteView = RemoteViews("com.yerayyas.firebaseyeray", R.layout.notification)
        remoteView.setTextViewText(R.id.title, title)
        remoteView.setTextViewText(R.id.message, message)
        remoteView.setImageViewResource(R.id.image, R.drawable.ic_notification)

        return remoteView
    }

    //mostrar la notificacion
    override fun onMessageReceived(message: RemoteMessage) {
        if(message.notification !=null){
            generateNotification(message.notification!!.title!!, message.notification!!.body!!)
        }
    }
}