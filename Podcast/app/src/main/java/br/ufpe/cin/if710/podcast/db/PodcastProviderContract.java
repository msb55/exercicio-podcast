package br.ufpe.cin.if710.podcast.db;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by leopoldomt on 9/19/17.
 */

public class PodcastProviderContract {

    /* ????
    public static final String _ID = "_id";
    public static final String TITLE = "nome";
    public static final String DATE = "cpf";
    public static final String DESCRIPTION = "email";
    public static final String EPISODE_LINK = "media";
    public static final String DOWNLOAD_LINK = "media";
    public static final String EPISODE_URI = "media";
    public static final String EPISODE_TABLE = "episodes";
     */

    /*
    * os atributos setados anteriormente apresentavam algumas inconsistencias
    * esses abaixo foram trazidos da classe Helper
    */

    public final static String _ID = "_id";
    public final static String EPISODE_TITLE = "title";
    public final static String EPISODE_DATE = "pubDate";
    public final static String EPISODE_LINK = "link";
    public final static String EPISODE_DESC = "description";
    public final static String EPISODE_DOWNLOAD_LINK = "downloadLink";
    public final static String EPISODE_FILE_URI = "downloadUri";
    public static final String EPISODE_TABLE = "episodes";

    public final static String[] ALL_COLUMNS = {
            _ID, EPISODE_TITLE, EPISODE_DATE, EPISODE_LINK,EPISODE_DESC, EPISODE_DOWNLOAD_LINK, EPISODE_FILE_URI};

    private static final Uri BASE_LIST_URI = Uri.parse("content://br.ufpe.cin.if710.podcast.feed/");
    //URI para tabela
    public static final Uri EPISODE_LIST_URI = Uri.withAppendedPath(BASE_LIST_URI, EPISODE_TABLE);

    // Mime type para colecao de itens
    public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/PodcastProvider.data.text";

    // Mime type para um item especifico
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/PodcastProvider.data.text";

}