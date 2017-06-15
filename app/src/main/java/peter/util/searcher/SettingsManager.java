package peter.util.searcher;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private Context context;
    private static final String NAME = "setting";
    private boolean autoFullScreen;
    private boolean noTrack;

    private SettingsManager() {
    }

    private static class SingletonInstance {
        private static final SettingsManager INSTANCE = new SettingsManager();
    }

    public static SettingsManager getInstance() {
        return SingletonInstance.INSTANCE;
    }


    void init(Context context) {
        this.context = context;
        autoFullScreen = getAutoFullScreenSP();
        noTrack = getNoTrackSP();
    }

    private boolean getAutoFullScreenSP() {
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.action_auto_fullscreen), false);
    }

    public boolean isAutoFullScreen() {
        return autoFullScreen;
    }

    public void saveAutoFullScreenSp(boolean autoFullScreen) {
        this.autoFullScreen = autoFullScreen;
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(context.getString(R.string.action_auto_fullscreen), autoFullScreen).apply();
    }

    private boolean getNoTrackSP() {
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.no_track), false);
    }

    public boolean isNoTrack() {
        return noTrack;
    }

    public void saveNoTrackSp(boolean noTrack) {
        this.noTrack = noTrack;
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(context.getString(R.string.no_track), autoFullScreen).apply();
    }


}