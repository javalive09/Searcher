package peter.util.searcher;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String NAME = "setting";
    private boolean autoFullScreen;
    private boolean noTrack;

    private SettingsManager() {
        autoFullScreen = getAutoFullScreenSP();
        noTrack = getNoTrackSP();
    }

    private static class SingletonInstance {
        private static final SettingsManager INSTANCE = new SettingsManager();
    }

    public static SettingsManager getInstance() {
        return SingletonInstance.INSTANCE;
    }


    private boolean getAutoFullScreenSP() {
        SharedPreferences sp = Searcher.context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(Searcher.context.getString(R.string.action_auto_fullscreen), false);
    }

    public boolean isAutoFullScreen() {
        return autoFullScreen;
    }

    public void saveAutoFullScreenSp(boolean autoFullScreen) {
        this.autoFullScreen = autoFullScreen;
        SharedPreferences sp = Searcher.context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(Searcher.context.getString(R.string.action_auto_fullscreen), autoFullScreen).apply();
    }

    private boolean getNoTrackSP() {
        SharedPreferences sp = Searcher.context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(Searcher.context.getString(R.string.no_track), false);
    }

    public boolean isNoTrack() {
        return noTrack;
    }

    public void saveNoTrackSp(boolean noTrack) {
        this.noTrack = noTrack;
        SharedPreferences sp = Searcher.context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(Searcher.context.getString(R.string.no_track), autoFullScreen).apply();
    }


}