package com.noregret;

import ch.qos.logback.core.model.InsertFromJNDIModel;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RecvFileThread extends Thread {
    private int fromPort;
    private String ip;
    private String from;
    private String to;

    public RecvFileThread(int fromPort, String ip,String from, String to) {
        this.fromPort = fromPort;
        this.ip = ip;
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, fromPort));
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String filename = dis.readUTF();
            FileOutputStream fos = new FileOutputStream(new File("received_file.txt"));
            byte[] buffer = new byte[1024];
            int len = -1;
            while((len = dis.read(buffer)) != -1){
                fos.write(buffer,0,len);
                fos.flush();
            }
            dis.close();
            fos.close();
            socket.close();
            if(to == null) {
                System.out.println(Utils.getColoredString(33, 1, "接收到 " + from + " 发来的 " + filename + " 文件!"));
            }else{
                System.out.println(Utils.getColoredString(33,1,"接收到 " + from + " 在 " + to + " 群组中发送的 " + filename + " 文件!"));
            }
        } catch (IOException e) {
            System.out.println("文件接收失败!");
        }
    }
}
