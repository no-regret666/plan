package com.noregret;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

public class test {
    public static void main(String[] args) {
        byte[] msg = "12{Hello,World!}".getBytes();
        ByteBuf buf = Unpooled.compositeBuffer(msg.length);
        buf.writeBytes(msg);
        int i = 0;
        while(i < msg.length) {
            if(buf.getByte(i) == 0x7B){
                System.out.println("true");
            }
            i++;
        }
        System.out.println("false");
    }
}
