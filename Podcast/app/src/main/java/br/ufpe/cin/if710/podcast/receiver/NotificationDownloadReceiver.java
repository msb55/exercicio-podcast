package br.ufpe.cin.if710.podcast.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.ui.MainActivity;

/**
 * Created by barre on 09/10/2017.
 */

public class NotificationDownloadReceiver extends BroadcastReceiver {
    // Notification ID permite associar e agrupar notificacoes no futuro
    private static final int MY_NOTIFICATION_ID = 1;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    private final CharSequence tickerText = "O aplicativo de podcast notifica que um download foi concluído. Verifique.";
    private final CharSequence contentTitle = "Episódio disponível";
    private final CharSequence contentText = "Download concluído: ";

    // Actions (intents a serem transmitidos)
    private Intent mNotificationIntent;
    private PendingIntent mContentIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        String EPISODE_TITLE = intent.getExtras().getString(PodcastProviderContract.EPISODE_TITLE);

        // chama o servico de notificacao
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // especifica no intent qual activity abrir ao clicar na notificacao
        mNotificationIntent = new Intent(context, MainActivity.class);
        mContentIntent = PendingIntent.getActivity(context, 0, mNotificationIntent, 0);

        // controi a estrutura da notificacao
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setTicker(tickerText)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText + EPISODE_TITLE)
                .setContentIntent(mContentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // passa notificacao para o notification manager
        mNotifyManager.notify(MY_NOTIFICATION_ID, mBuilder.build());
    }
}
