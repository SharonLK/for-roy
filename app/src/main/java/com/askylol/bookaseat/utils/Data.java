package com.askylol.bookaseat.utils;

import com.askylol.bookaseat.logic.Library;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Sharon on 06-Jun-17.
 */
public enum Data {
    INSTANCE;

    public String mail = null;
    public Library library = null;
    public boolean isSitting = true;
    public boolean isInLibrary = false;
    public AtomicBoolean isInForeground = new AtomicBoolean(false);
    public boolean keepNotification = false;

    public static void reset() {
        INSTANCE.mail = null;
        INSTANCE.library = null;
        INSTANCE.isSitting = true;
        INSTANCE.isInLibrary = false;
        INSTANCE.isInForeground.set(false);
    }
}
