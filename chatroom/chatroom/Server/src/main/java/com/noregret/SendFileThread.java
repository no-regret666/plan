package com.noregret;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SendFileThread extends Thread {
    private final int toPort;
    private int fileID;

    public SendFileThread(int fromPort,int fileID) {
        this.toPort = fromPort;
        this.fileID = fileID;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(toPort);
            Socket socket = serverSocket.accept();

            FileInputStream fis = new FileInputStream("/home/noregret/chatroom_file/" + fileID);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            byte[] buffer = new byte[1024];
            int len;
            while((len = fis.read(buffer)) != -1){
                dos.write(buffer,0,len);
                dos.flush();
            }
            fis.close();
            dos.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
