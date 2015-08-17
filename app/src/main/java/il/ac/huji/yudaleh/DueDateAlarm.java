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
    final static int DEFAULT_ALARM_ID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Raise the notification about his debt
        Uri intentData = intent.getData();
        long alarmId;
        if (intentData == null) {
            alarmId = DEFAULT_ALARM_ID;
        } else {
            alarmId = Integer.parseInt(intentData.getSchemeSpecificPart());
        }
        createNotification(context, "Debt return " + alarmId, "Time to return the debt", "Alert", alarmId);
    }

    /**
     * Creates and shows notification to the user.
     *
     * @param context app context for the intent
     * @param title   short content
     * @param text    few more details
     * @param alert   shows on the top bar for one second
     * @param alarmId must be unique
     */
    public void createNotification(Context context, String title, String text, String alert, long alarmId) {
        PendingIntent notificationIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);
        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setTicker(alert)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(notificationIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .addAction(0, "Call ...", notificationIntent) //todo contact's phone
                .build();
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) alarmId, notification); //todo check int cast, make unique
    }
}
