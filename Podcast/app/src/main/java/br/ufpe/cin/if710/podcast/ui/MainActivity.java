package br.ufpe.cin.if710.podcast.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.application.MyApplication;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.service.DownloadPodcastService;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast

    private ListView items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = (ListView) findViewById(R.id.items);

        // verificando permissoes de escrita(para download)
        if (!canWrite()) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public boolean canWrite() {
        return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(!canWrite()) {
                    Toast.makeText(this, "Permissões de armazenamento não concedidas.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registrando intentfilter para notificacoes de download concluido
        IntentFilter f = new IntentFilter("br.ufpe.cin.if710.podcast.service.action.DOWNLOAD_COMPLETE");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(onDownloadCompleteEvent, f);
        MyApplication.activityResumed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // verificacao de internet
        connectivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // removendo registro
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(onDownloadCompleteEvent);
        MyApplication.activityPaused();
    }

    @Override
    protected void onStop() {
        super.onStop();
        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
        adapter.clear();
    }

    // ao receber um broadcast...
    private BroadcastReceiver onDownloadCompleteEvent=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Atualizando...", Toast.LENGTH_LONG).show();
            System.out.println("RECEIVER...");

            //Adapter Personalizado atualizado getItemFeedsBD()...
            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, getItemFeedsBD());

            //atualizar o list view
            items.setAdapter(adapter);
            items.deferNotifyDataSetChanged(); // notifica o adapter que houve mudancas
            Toast.makeText(context, "Atualizado pelo receiver...", Toast.LENGTH_LONG).show();
        }
    };

    private class DownloadXmlTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "iniciando...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... params) {
            List<ItemFeed> itemList;
            try {
                itemList = XmlFeedParser.parse(getRssFeed(params[0]));
                // faz comparacao do que esta no XML e no BD
                constraintInsertDB(itemList);

                // aos novos itens...
                for (ItemFeed item: itemList) {
                    ContentValues cv = new ContentValues();

                    cv.put(PodcastProviderContract.EPISODE_TITLE, item.getTitle());
                    cv.put(PodcastProviderContract.EPISODE_DATE, item.getPubDate());
                    cv.put(PodcastProviderContract.EPISODE_DESC, item.getDescription());
                    cv.put(PodcastProviderContract.EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
                    cv.put(PodcastProviderContract.EPISODE_LINK, item.getLink());
                    cv.put(PodcastProviderContract.EPISODE_FILE_URI, "");

                    // ...sao persistidos no BD
                    Uri uri = getContentResolver().insert(PodcastProviderContract.EPISODE_LIST_URI, cv);
                    /*System.out.println(uri.toString());*/
                    if(uri == null) System.out.println("Erro no item: " + item.getTitle());
                }

                /* Apenas para "limpar" os registros do banco (p/ testes)
                    getContentResolver().delete(PodcastProviderContract.EPISODE_LIST_URI, null, null);
                    System.out.println("DELETE ALL ITENS.");
                */
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            /*Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();*/
            // ao finalizar as 'novidades' no BD, chama a AsyncTask que busca todos os itens do BD para o Adapter
            new ConsultDBTask().execute();
        }

        private void constraintInsertDB (List<ItemFeed> itens) {
            for(ItemFeed item : getItemFeedsBD()) {
                if(itens.contains(item)) itens.remove(item);
            }
        }
    }

    protected ItemFeed cursorToItemFeed (Cursor cursor) {
        /* columns
            EPISODE_TITLE
            EPISODE_DATE
            EPISODE_LINK
            EPISODE_DESC
            EPISODE_DOWNLOAD_LINK
         */
        int EPISODE_TITLE = cursor.getColumnIndex(PodcastProviderContract.EPISODE_TITLE),
            EPISODE_DATE = cursor.getColumnIndex(PodcastProviderContract.EPISODE_DATE),
            EPISODE_LINK = cursor.getColumnIndex(PodcastProviderContract.EPISODE_LINK),
            EPISODE_DESC = cursor.getColumnIndex(PodcastProviderContract.EPISODE_DESC),
            EPISODE_DOWNLOAD_LINK = cursor.getColumnIndex(PodcastProviderContract.EPISODE_DOWNLOAD_LINK),
            EPISODE_FILE_URI = cursor.getColumnIndex(PodcastProviderContract.EPISODE_FILE_URI);

        // para cada linha da tabela, transforma em um objeto ItemFeed
        return new ItemFeed(
                cursor.getString(EPISODE_TITLE),
                cursor.getString(EPISODE_LINK),
                cursor.getString(EPISODE_DATE),
                cursor.getString(EPISODE_DESC),
                cursor.getString(EPISODE_DOWNLOAD_LINK),
                cursor.getString(EPISODE_FILE_URI)
        );
    }

    private class ConsultDBTask extends AsyncTask<Void, Void, List<ItemFeed>> {

        @Override
        protected List<ItemFeed> doInBackground(Void... voids) {
            // realiza uma busca no BD
            return getItemFeedsBD();
        }

        @Override
        protected void onPostExecute(List<ItemFeed> itemFeeds) {
            if(itemFeeds.size() == 0) {
                Toast.makeText(getApplicationContext(), "Não foi encontrado registro no banco.", Toast.LENGTH_SHORT).show();
            } else {
                //Adapter Personalizado
                XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, itemFeeds);

                //atualizar o list view
                items.setAdapter(adapter);
                items.setTextFilterEnabled(true);
                Toast.makeText(getApplicationContext(), "Atualizado!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    private List<ItemFeed> getItemFeedsBD() {
        Cursor cursor = getContentResolver().query(
                PodcastProviderContract.EPISODE_LIST_URI,
                null,
                null,
                null,
                null);
        System.out.println(cursor.getCount());

        if(cursor == null) {
            cursor.close();
            return new ArrayList<>();
        } else {
            List<ItemFeed> retorno = new ArrayList<>();

            while(cursor.moveToNext()) {
                ItemFeed item = cursorToItemFeed(cursor);
                retorno.add(item);
            }
            cursor.close();
            return retorno;
        }
    }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet
    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }

    private void connectivity() {
        // consulta o manager de conectividade
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        // checa se existe alguma conexao com a internet
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) { // caso tenha realiza o download e parse do XML
            System.out.println("TEM CONEXÃO COM A INTERNET");
            new DownloadXmlTask().execute(RSS_FEED);
        } else { // caso nao tenha, utiliza o que esta no BD
            System.out.println("SEM CONEXÃO COM A INTERNET");
            new ConsultDBTask().execute();
        }
    }
}
