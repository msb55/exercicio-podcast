package br.ufpe.cin.if710.podcast.application;

import android.app.Application;

/**
 * Created by barre on 09/10/2017.
 */

public class MyApplication extends Application {
    private static boolean activityVisible;
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }
}
