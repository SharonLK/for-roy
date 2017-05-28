package com.askylol.bookaseat.utils;

/**
 * Created by Sharon on 28-May-17.
 */

public class Pair<T, S> {
    public final T first;
    public final S second;

    public Pair() {
        first = null;
        second = null;
    }

    public Pair(T first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
