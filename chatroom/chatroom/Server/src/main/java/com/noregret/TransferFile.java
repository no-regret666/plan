package com.noregret;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TransferFile extends Thread{
    private final int fromPort;
    private final int toPort;
    public TransferFile(int fromPort, int toPort) {
        this.fromPort = fromPort;
        this.toPort = toPort;
    }

    @Override
    public void run() {
        try {
            ServerSocket from = new ServerSocket(fromPort);
            ServerSocket to = new ServerSocket(toPort);

            Socket fromSocket = from.accept();
            Socket toSocket = to.accept();

            InputStream inputStream = fromSocket.getInputStream();
            OutputStream outputStream = toSocket.getOutputStream();

            byte[] buffer = new byte[1024];
            int length = -1;
            while((length = inputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,length);
                outputStream.flush();
            }

            fromSocket.close();
            toSocket.close();
            inputStream.close();
            outputStream.close();
            System.out.println("File transferred");
        } catch (IOException e) {
            System.out.println("File transfer failed");
        }
    }
}
