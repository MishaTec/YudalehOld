package il.ac.huji.yudaleh;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Accepts alarms on the due date of the debt
 */
public class DueDateAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Raise the notification about his debt
        Integer.parseInt(intent.getData().getSchemeSpecificPart());
        Uri intentData = intent.getData();
        int id;
        if (intentData == null) {
            id = 1;
            System.out.println("NULL **************************");
        } else {
            id = Integer.parseInt(intentData.getSchemeSpecificPart());
            System.out.println("***** NOT_NULL **************************");
        }
        createNotification(context, "Debt return", "Time to return the debt", "Alert", id);
//        Toast.makeText(context, "Alarm" + intent.getData().getSchemeSpecificPart() + " fires!!!! YEAH", Toast.LENGTH_LONG).show();
        System.out.println("id = " + id);
//        createNotification(context, "Debt return", "Time to return the debt", "Alert",2);
    }

    public void createNotification(Context context, String title, String text, String alert, int id) {
        PendingIntent notificationIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);
        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setTicker(alert)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(notificationIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .addAction(0, "Call ...", notificationIntent) //todo phone
                .build();
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

//        Toast.makeText(context, "Alarm" + id + " fires!!!! YEAH", Toast.LENGTH_LONG).show();
        mNotificationManager.notify(id, notification);
    }
}
