package unifr.sensorrecorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import unifr.sensorrecorder.Evaluation.HandwashEvaluation;
import unifr.sensorrecorder.Evaluation.OverallEvaluation;
import unifr.sensorrecorder.EventHandlers.EvaluationReceiver;

public class NotificationSpawner {

    private static final String RECORDING_CHANNEL_ID = "ForegroundServiceChannel";
    private static final String PREDICTION_CHANNEL_ID = "PredictionChannel";
    public static final int DAILY_REMINDER_REQUEST_CODE = 13;
    public static final int EVALUATION_REQUEST_CODE = 12;
    private static Intent recordingServiceIntent;

    private static int notificationCounter = 2;

    public static Notification createRecordingNotification(Context context, Intent recordingServiceIntent){
        createNotificationChannel(context, RECORDING_CHANNEL_ID, "Foreground Service Channel");
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, 0);

        Intent handwashIntent = new Intent(recordingServiceIntent);
        handwashIntent.putExtra("trigger", "handWash");
        PendingIntent pintHandWash = PendingIntent.getService(context, 579, handwashIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Intent openIntent = new Intent(intent);
        Intent openIntent = new Intent(context, MainActivity.class);

        openIntent.putExtra("trigger", "open");
        PendingIntent pintOpen = PendingIntent.getActivity(context, 579, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, RECORDING_CHANNEL_ID)
                .setContentTitle(context.getResources().getString(R.string.not_running))
                .setContentText(context.getResources().getString(R.string.not_sen_rec_active))
                //.setStyle(new NotificationCompat.BigTextStyle()
                //        .bigText("Sensor recorder is active"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.preference_wrapped_icon)
                .addAction(R.drawable.action_item_background, context.getResources().getString(R.string.not_btn_hw), pintHandWash)
                // .addAction(R.drawable.action_item_background, "Open", pintOpen);
                .setContentIntent(pintOpen);

        return notificationBuilder.build();
    }

    public static void spawnHandWashPredictionNotification(Context context, long timestamp){
        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        createNotificationChannel(context, PREDICTION_CHANNEL_ID, "Prediction Channel");
        try {
            closeOldPredictionNotification(notificationManager);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        /*
        Intent handwashIntent = new Intent(context, SensorRecordingManager.class);
        // Log.d("not", "send notification with ts " + timestamp);
        handwashIntent.putExtra("trigger", "handWashTS");
        handwashIntent.putExtra("timestamp", timestamp);
        PendingIntent pintHandWash = PendingIntent.getService(context, 579, handwashIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         */
        /*
        Intent confirmHandWashIntent = new Intent(context, SensorManager.class);
        confirmHandWashIntent.putExtra("trigger", "handWashConfirm");
        confirmHandWashIntent.putExtra("timestamp", timestamp);
        PendingIntent pintConfirmHandWash = PendingIntent.getService(context, 571, confirmHandWashIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         */

        Intent confirmHandWashIntent = new Intent(context, EvaluationReceiver.class);
        confirmHandWashIntent.putExtra("trigger", "handWashConfirm");
        confirmHandWashIntent.putExtra("timestamp", timestamp);
        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //stackBuilder.addNextIntentWithParentStack(confirmHandWashIntent);
//        confirmHandWashIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent pintConfirmHandWash = PendingIntent.getBroadcast(context, EVALUATION_REQUEST_CODE, confirmHandWashIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent pintConfirmHandWash = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent declineHandWashIntent = new Intent(context, EvaluationReceiver.class);
        declineHandWashIntent.putExtra("trigger", "handWashDecline");
        declineHandWashIntent.putExtra("timestamp", timestamp);
        PendingIntent pintDeclineHandWash = PendingIntent.getBroadcast(context, EVALUATION_REQUEST_CODE+1, declineHandWashIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeHandWashIntent = new Intent(context, EvaluationReceiver.class);
        closeHandWashIntent.putExtra("trigger", "close");
        closeHandWashIntent.putExtra("timestamp", timestamp);
        PendingIntent pintClose = PendingIntent.getBroadcast(context, EVALUATION_REQUEST_CODE+2, closeHandWashIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("trigger", "open");
        PendingIntent pintOpen = PendingIntent.getActivity(context, EVALUATION_REQUEST_CODE, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        */
        /*
        Intent fsIntent = new Intent(context, HandwashEvaluation.class);
        PendingIntent pintFS = PendingIntent.getActivity(context, 0, fsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         */

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PREDICTION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hand_wash)
                .setContentTitle(context.getResources().getString(R.string.not_just_washed_hands))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //.setContentIntent(pintOpen)
                .addAction(R.drawable.ic_check, context.getResources().getString(R.string.not_btn_yes), pintConfirmHandWash)
                .addAction(R.drawable.ic_close, context.getResources().getString(R.string.not_btn_no), pintDeclineHandWash)
                //.setFullScreenIntent(pintFS, true)
                //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(pintClose)
                //.setAutoCancel(true)
        ;
        notificationManager.notify(EVALUATION_REQUEST_CODE, builder.build());
    }


    public static void showOverallEvaluationNotification(Context context){
        Intent startEvalIntent = new Intent(context, OverallEvaluation.class);
        startEvalIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, DAILY_REMINDER_REQUEST_CODE, startEvalIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);



        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, RECORDING_CHANNEL_ID)
                .setContentTitle(context.getResources().getString(R.string.not_oar_title))
                .setContentText(context.getResources().getString(R.string.not_oar_text))
                //.setStyle(new NotificationCompat.BigTextStyle()
                //        .bigText("Sensor recorder is active"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.preference_wrapped_icon)
                //.setVibrate(new long[]{1000, 500, 1000, 500})
                // .setSound(alarmSound)
                //.addAction(R.drawable.action_item_background, context.getResources().getString(R.string.not_btn_hw), pintHandWash)
                // .addAction(R.drawable.action_item_background, "Open", pintOpen);
                .setContentIntent(resultPendingIntent);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(DAILY_REMINDER_REQUEST_CODE, notificationBuilder.build());
    }


    private static void closeOldPredictionNotification(NotificationManager notificationManager) throws PendingIntent.CanceledException {
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification notification: activeNotifications){
            if(notification.getId() == 2){
                notification.getNotification().deleteIntent.send();
                break;
            }
        }
    }

    private static void createNotificationChannel(Context context, String chanelID, String chanelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    chanelID,
                    chanelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.enableVibration(true);
            serviceChannel.setVibrationPattern(new long[]{1000, 500, 1000, 5000});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}