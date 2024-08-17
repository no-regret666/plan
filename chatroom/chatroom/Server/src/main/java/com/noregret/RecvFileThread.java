package com.noregret;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class RecvFileThread extends Thread {
    private final int fromPort;
    private int fileID;

    public RecvFileThread(int fromPort,int fileID) {
        this.fromPort = fromPort;
        this.fileID = fileID;
    }

    @Override
    public void run() {
        try{
            ServerSocket serverSocket = new ServerSocket(fromPort);
            Socket socket = serverSocket.accept();

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            File file = new File("/home/noregret/chatroom_file/" + fileID);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while((len = dis.read(buffer)) != -1){
                fos.write(buffer,0,len);
                fos.flush();
            }
            dis.close();
            fos.close();
            socket.close();

            System.out.println("文件接收完成!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}