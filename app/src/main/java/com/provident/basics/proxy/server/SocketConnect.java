package com.provident.basics.proxy.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * SocketConnect class
 */

public class SocketConnect extends Thread {

    private InputStream from;
    private OutputStream to;
    private ISocketDataResponse mResponse;

    public SocketConnect(Socket from, Socket to, ISocketDataResponse response) throws IOException {
        this.from = from.getInputStream();
        this.to = to.getOutputStream();
        this.mResponse = response;
        start();
    }

    public static void connect(Socket first, Socket second, ISocketDataResponse inbound, ISocketDataResponse outbound) {
        try {
            SocketConnect sc1 = new SocketConnect(first, second, outbound);
            SocketConnect sc2 = new SocketConnect(second, first, inbound);
            try {
                sc1.join();
            } catch (InterruptedException e) {
            }
            try {
                sc2.join();
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        final byte[] buffer = new byte[512];

        try {
            while (true) {
                int r = from.read(buffer);
                if (r < 0) {
                    break;
                }
                if (mResponse != null) {
                    mResponse.add(r);
                }
                to.write(buffer, 0, r);
            }
            from.close();
            to.close();
        } catch (IOException io) {

        }
    }
}