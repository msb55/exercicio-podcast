package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

public class PodcastProvider extends ContentProvider {
    PodcastDBHelper db;

    public PodcastProvider() {
    }

    /*
    As alteracoes aqui feitas segue o modelo dado em sala
    Apenas passando as queries para o PodcastDBHelper
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        return db.getWritableDatabase().delete(
                PodcastDBHelper.DATABASE_TABLE,
                selection,
                selectionArgs
        );
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.

        long id = db.getWritableDatabase().replace(
                PodcastDBHelper.DATABASE_TABLE,
                null,
                values);
        System.out.println("ID DA INSERÇÃO: " + id);
        if(id != -1) {
            return Uri.withAppendedPath(
                    PodcastProviderContract.EPISODE_LIST_URI,
                    Long.toString(id));
        } else {
            throw new SQLException();
        }
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        db = PodcastDBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Cursor cursor = null;
        cursor = db.getReadableDatabase().query(
                PodcastDBHelper.DATABASE_TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        return db.getWritableDatabase().update(
                PodcastDBHelper.DATABASE_TABLE,
                values,
                selection,
                selectionArgs
        );
    }
}
