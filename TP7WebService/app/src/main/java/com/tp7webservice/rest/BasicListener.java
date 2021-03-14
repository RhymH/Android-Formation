package com.tp7webservice.rest;

interface BasicListener {
    void onSuccess();

    void onError(int code);
}
