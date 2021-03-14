package cnam.com.chuck.rest;

import android.graphics.Bitmap;

public interface BasicListener {
    void onIconReceived(Bitmap image);

    void onSuccess();

    void onError(int code);
}
