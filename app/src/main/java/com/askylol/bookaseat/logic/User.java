package com.askylol.bookaseat.logic;

/**
 * Created by Sharon on 22-May-17.
 */
public class User {
    private String name;

    private User() {

    }

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
