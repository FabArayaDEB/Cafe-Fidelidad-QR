package com.example.cafefidelidaqrdemo.utils;

import com.bumptech.glide.load.engine.Resource;

//Clase resource para manejar 3 tipos de diferentes estado dentro de un MutableLiveData.
public class Resources<T> {
    public enum Status { SUCCESS, ERROR, LOADING }

    public final Status status;
    public final T data;
    public final String message;

    public Resources(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resources<T> success(T data) {
        return new Resources<>(Status.SUCCESS, data, null);
    }

    public static <T> Resources<T> error(String msg, T data) {
        return new Resources<>(Status.ERROR, data, msg);
    }

    public static <T> Resources<T> loading(T data) {
        return new Resources<>(Status.LOADING, data, null);
    }
}
