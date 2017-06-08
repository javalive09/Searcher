/*
 * Copyright 2014 A.C.R. Development
 */
package peter.util.searcher.utils;

import android.content.Context;
import android.util.TypedValue;

public final class Constants {

    private Constants() {
    }

    private static int actionBarHeight = 0;

    public static final String INTENT_ORIGIN = "URL_INTENT_ORIGIN";

    public static int getActionBarH(Context context) {
        if(actionBarHeight == 0) {
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        }
        return actionBarHeight;
    }
}
