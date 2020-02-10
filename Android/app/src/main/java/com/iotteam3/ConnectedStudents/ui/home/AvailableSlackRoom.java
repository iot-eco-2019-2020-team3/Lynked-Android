package com.iotteam3.ConnectedStudents.ui.home;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.iotteam3.ConnectedStudents.R;



public class AvailableSlackRoom {

    private static int notificationId = 0;
    private static int lastNotificationId = 0;
    private final String ChannelName;
    private final String url;
    private final android.content.Context Context;


    /***
     * Create a new SlackRoomObject
     * @param context the context
     * @param channelName Name des Channels
     */
     public AvailableSlackRoom(Context context, String channelName, String url)
     {
         this.Context = context;
         this.ChannelName = channelName;
         this.url = url;
         this.notificationId = lastNotificationId;
         lastNotificationId++;
     }

    /***
     * Get the Channel name
     * @return Channel Name
     */
    public  String getChannelName()
{
    return ChannelName;
}

    /***
     * Send an Notification to the Device
     */
    public void SendNotification()
    {
        // hier asncheinend url wichtig
        Uri webpage = Uri.parse(url);//https://testconnectedstudents.slack.com/app_redirect?channel=test-connected-students"); // open in the current state only Slack, without selecting a Channel
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        PendingIntent openPendingIntent =
                PendingIntent.getActivity(Context, 0, webIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(Context, Context.getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(Context.getString(R.string.app_name))
                .setContentText(this.ChannelName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_launcher, Context.getString(R.string.open_slack),openPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.Context);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(this.notificationId, builder.build());
    }

    public void CloseNotification()
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.Context);
        notificationManager.cancel(this.notificationId);
    }

    public void OpenSlack() {
        //Action zum öffnen von Slack
        //"slack://channel?team={TR0A3UUBA}&id={CQSC5J49X}"
        //spielt anscheinend keine rolle
        Uri webpage = Uri.parse("shit");//https://testconnectedstudents.slack.com/app_redirect?channel=test-connected-students"); // open in the current state only Slack, without selecting a Channel
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        Context.startActivity(webIntent);

    }

}
