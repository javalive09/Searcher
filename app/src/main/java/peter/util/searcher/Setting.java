package peter.util.searcher;

import android.content.Context;
import android.content.SharedPreferences;

public class Setting {

    private Context context;
    private static final String NAME = "setting";

    private boolean autoFullScreen;

    public boolean DESK_TOP_UA;


    private Setting() {
    }

    private static class SingletonInstance {
        private static final Setting INSTANCE = new Setting();
    }

    public static Setting getInstance() {
        return SingletonInstance.INSTANCE;
    }


    public void init(Context context) {
        this.context = context;
        autoFullScreen = getAutoFullScreenSP();
    }

    public boolean getAutoFullScreenSP() {
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        boolean show = sp.getBoolean("fullscreen_auto", false);
        return show;
    }

    public boolean isAutoFullScreen() {
        return autoFullScreen;
    }

    public void saveAutoFullScreenSp(boolean autoFullScreen) {
        this.autoFullScreen = autoFullScreen;
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean("fullscreen_auto", autoFullScreen).apply();
    }



}