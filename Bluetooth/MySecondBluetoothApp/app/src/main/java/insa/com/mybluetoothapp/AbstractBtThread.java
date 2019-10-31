package insa.com.mybluetoothapp;

/**
 * Created by georges on 06/10/2018.
 */

abstract class AbstractBtThread extends Thread {

    abstract void write(byte[] out);
    abstract void cancel();
}
