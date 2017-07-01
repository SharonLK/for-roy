package com.askylol.bookaseat.utils;

import com.askylol.bookaseat.logic.Library;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Sharon on 06-Jun-17.
 */
public enum Data {
    INSTANCE;

    public String username = null;
    public Library library = null;
    public GoogleApiClient googleClient;
    public boolean isSitting = true;
}
