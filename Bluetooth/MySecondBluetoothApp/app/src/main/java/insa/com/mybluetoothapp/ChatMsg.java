package insa.com.mybluetoothapp;

/**
 * Created by georges on 06/10/2018.
 */

public class ChatMsg {
    private String name;
    private String msg;

    public ChatMsg(String from, String message) {
        this.name = from;
        this.msg = message;
    }

    public String getName() {
        return name;
    }

    public String getMsg() {
        return msg;
    }
}
