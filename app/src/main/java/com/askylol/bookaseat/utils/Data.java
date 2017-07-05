package com.askylol.bookaseat.utils;

import com.askylol.bookaseat.logic.Library;

/**
 * Created by Sharon on 06-Jun-17.
 */
public enum Data {
    INSTANCE;

    public String mail = null;
    public Library library = null;
    public boolean isSitting = true;
    public boolean isInLibrary = false;
    public boolean isInForeground = false;

    public static void reset() {
        INSTANCE.mail = null;
        INSTANCE.library = null;
        INSTANCE.isSitting = true;
        INSTANCE.isInLibrary = false;
        INSTANCE.isInForeground = false;
    }
}
