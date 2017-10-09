package br.ufpe.cin.if710.podcast.ui.adapter;

import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.service.DownloadPodcastService;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;

public class XmlFeedAdapter extends ArrayAdapter<ItemFeed> {

    int linkResource;

    public XmlFeedAdapter(Context context, int resource, List<ItemFeed> objects) {
        super(context, resource, objects);
        linkResource = resource;
    }

    /**
     * public abstract View getView (int position, View convertView, ViewGroup parent)
     * <p>
     * Added in API level 1
     * Get a View that displays the data at the specified position in the data set. You can either create a View manually or inflate it from an XML layout file. When the View is inflated, the parent View (GridView, ListView...) will apply default layout parameters unless you use inflate(int, android.view.ViewGroup, boolean) to specify a root view and to prevent attachment to the root.
     * <p>
     * Parameters
     * position	The position of the item within the adapter's data set of the item whose view we want.
     * convertView	The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not possible to convert this view to display the correct data, this method can create a new view. Heterogeneous lists can specify their number of view types, so that this View is always of the right type (see getViewTypeCount() and getItemViewType(int)).
     * parent	The parent that this view will eventually be attached to
     * Returns
     * A View corresponding to the data at the specified position.
     */


	/*
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.itemlista, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.item_title);
		textView.setText(items.get(position).getTitle());
	    return rowView;
	}
	/**/

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class ViewHolder {
        TextView item_title;
        TextView item_date;
        Button btn; // adicionado o botao de cada item

        MediaPlayer mPlayer; // player para o podcast
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(getContext(), linkResource, null);
            holder = new ViewHolder();
            holder.item_title = (TextView) convertView.findViewById(R.id.item_title);
            holder.item_date = (TextView) convertView.findViewById(R.id.item_date);
            holder.btn = (Button) convertView.findViewById(R.id.item_action);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.item_title.setText(getItem(position).getTitle());
        holder.item_date.setText(getItem(position).getPubDate());

        final ItemFeed item = getItem(position); // item 'selecionado'

        if(!item.getFile_uri().isEmpty()) {
            /*
            esta havendo inconsistencia nos botoes, alguns itens mesmo sem URI do arquivo,
            tem seu botao como PLAY e não BAIXAR
            */
            holder.btn.setText("PLAY");
            holder.mPlayer = MediaPlayer.create(getContext(), Uri.parse(item.getFile_uri()));
            /*System.out.println(item.getTitle() + " - " + item.getDownloadLink());*/
        }

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn_name = holder.btn.getText().toString().toLowerCase();
                if(btn_name.equals("play") || btn_name.equals("pause")) { // caso ja tenha o podcast baixado
                    /*do somethin to play podcast*/
                    if(item.getFile_uri().isEmpty()) { // inconsistência com os botoes
                        holder.btn.setText("BAIXAR");
                    } else if(btn_name.equals("play") && holder.mPlayer != null) {
                        holder.mPlayer.start();
                        holder.btn.setText("PAUSE");
                    } else if(btn_name.equals("pause") && holder.mPlayer != null) {
                        holder.mPlayer.pause();
                        holder.btn.setText("PLAY");
                    }
                    /*System.out.println("STUB PLAYING PODCAST " + item.getFile_uri().isEmpty());*/
                } else if(btn_name.equals("baixar")) { // para baixar o podcast
                    holder.btn.setText("BAIXANDO");

                    // iniciar um IntentService que fara o download do podcast, passando como argumento o link
                    Intent downloadPodcast = new Intent(getContext(), DownloadPodcastService.class);
                    downloadPodcast.setData(Uri.parse(item.getDownloadLink()));
                    getContext().startService(downloadPodcast);
                } else {
                    Toast.makeText(getContext(), "Download em andamento", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.item_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), EpisodeDetailActivity.class);
                i.putExtra("title", item.getTitle());
                i.putExtra("date", item.getPubDate());
                i.putExtra("description", item.getDescription());
                i.putExtra("link", item.getLink());
                i.putExtra("download_link", item.getDownloadLink());

                getContext().startActivity(i);
            }
        });

        return convertView;
    }
}