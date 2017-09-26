/*
 * Copyright 2014 A.C.R. Development
 */
package peter.util.searcher.utils;

import android.content.Context;
import android.util.TypedValue;

import peter.util.searcher.SettingsManager;

public final class Constants {

    private Constants() {
    }

    private static int actionBarHeight = 0;

    public static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";

    public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";

    static final String INTENT_ORIGIN = "URL_INTENT_ORIGIN";

    public static final String ABOUT = "about:";
    public static final String MAIL_SCHAME = "mailto:";
    public static final String INTENT_SCHAME = "intent://";

    public static int getActionBarH(Context context) {
        if (actionBarHeight == 0) {
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        }

        if (!SettingsManager.getInstance().isAutoFullScreen()) {
            return 0;
        }

        return actionBarHeight;
    }
}
