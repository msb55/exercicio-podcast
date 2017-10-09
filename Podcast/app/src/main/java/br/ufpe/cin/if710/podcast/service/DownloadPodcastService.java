package br.ufpe.cin.if710.podcast.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import br.ufpe.cin.if710.podcast.application.MyApplication;
import br.ufpe.cin.if710.podcast.db.PodcastProvider;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadPodcastService extends IntentService {

    private static final String DOWNLOAD_COMPLETE = "br.ufpe.cin.if710.podcast.service.action.DOWNLOAD_COMPLETE";
    private PodcastProvider db;

    public DownloadPodcastService() {
        super("DownloadPodcastService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // realiza o download igual ao mostrado em sala
            //checar se tem permissao... Android 6.0+
            Log.i("downloadservice", "Downloading...");
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            root.mkdirs();
            File output = new File(root, intent.getData().getLastPathSegment());
            if (output.exists()) {
                Log.i("downloadservice", "-----Deleting exist file...");
                output.delete();
            }
            URL url = new URL(intent.getData().toString());
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            FileOutputStream fos = new FileOutputStream(output.getPath());
            BufferedOutputStream out = new BufferedOutputStream(fos);
            try {
                InputStream in = c.getInputStream();
                byte[] buffer = new byte[8192];
                int len = 0;
                while ((len = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            finally {
                System.out.println(output.getAbsolutePath());
                fos.getFD().sync();
                out.close();
                c.disconnect();
            }

            String[] selectionArgs = new String[1];
            selectionArgs[0] = intent.getData().toString();
            String selection = PodcastProviderContract.EPISODE_DOWNLOAD_LINK + "== ?";

            ContentValues cv = new ContentValues();
            cv.put(PodcastProviderContract.EPISODE_FILE_URI, output.getAbsolutePath());

            /*atualiza o item que tem o mesmo link de download do passado como argumento
            * atualizando apenas a URI do arquivo baixado*/
            int x = getContentResolver().update(
                        PodcastProviderContract.EPISODE_LIST_URI,
                        cv,
                        selection,
                        selectionArgs
                    );

            /*Thread.sleep(3000);*/
            Log.i("downloadservice", "EPISODE_FILE_URI saved, number of rows: " + 10);

            MyApplication app = (MyApplication) getApplicationContext();

            Intent i = new Intent(DOWNLOAD_COMPLETE);
            i.putExtra(PodcastProviderContract.EPISODE_TITLE, intent.getExtras().getString(PodcastProviderContract.EPISODE_TITLE));

            // verifica se a activity esta em primeiro plano (visivel)
            if(app.isActivityVisible()) {
                // notifica para o aplicativo diretamente
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            } else {
                // envia para o receiver que esta encarregado de tratar esse tipo de broadcast (DOWNLOAD_COMPLETE)
                sendBroadcast(i);
            }
        } catch (Exception e2) {
            Log.e(getClass().getName(), "Exception durante download", e2);
        }
    }
}
