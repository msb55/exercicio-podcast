package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;

public class EpisodeDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        //TODO preencher com informações do episódio clicado na lista...

        TextView titulo = (TextView) findViewById(R.id.titulo);
        TextView descricao = (TextView) findViewById(R.id.descricao);
        TextView data = (TextView) findViewById(R.id.data);
        TextView link = (TextView) findViewById(R.id.link);
        Button download = (Button) findViewById(R.id.botao_download);

        Intent i = getIntent();
        titulo.setText(i.getExtras().getString("title"));
        descricao.setText(i.getExtras().getString("description"));
        data.setText(i.getExtras().getString("date"));
        link.setText(i.getExtras().getString("link"));

        download.setEnabled(false);
    }
}
