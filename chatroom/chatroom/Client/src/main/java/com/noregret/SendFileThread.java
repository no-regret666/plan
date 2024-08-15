package com.noregret;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SendFileThread extends Thread {
    private int toPort;
    private String ip;
    private File file;
    public SendFileThread(int toPort, String ip, File file) {
        this.toPort = toPort;
        this.ip = ip;
        this.file = file;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, toPort));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String filename = file.getName();
        try (FileInputStream fis = new FileInputStream(file);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())){
            //写入文件名称
            dos.writeUTF(filename);

            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, len);
                dos.flush();
            }

            socket.close();

            System.out.println(Utils.getColoredString(33,1,filename + " 发送完毕!"));
        } catch (IOException e) {
            System.out.println(Utils.getColoredString(31,1,filename + " 发送失败!"));
        }
    }
}
