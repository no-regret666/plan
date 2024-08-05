package com.noregret;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.Getter;

public class LengthMsgDecoder extends LengthFieldBasedFrameDecoder {
    public static ByteBuf msg = Unpooled.compositeBuffer(1024 * 1024);

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        msg = in.copy();
        ByteBuf rawData = in.copy();
        return new DecodeResult(frame, rawData);
    }

    @Getter
    public static class DecodeResult {
        private final ByteBuf decodedFrame;
        private final ByteBuf rawData;

        public DecodeResult(ByteBuf decodedFrame, ByteBuf rawData) {
            this.decodedFrame = decodedFrame;
            this.rawData = rawData;
        }
    }

    private static int getLength(ByteBuf in) {
        int i = 0;
        while (i < in.readableBytes()) {
            if (in.getByte(i) == 0x7B) {
                return i;
            }
            i++;
        }
        return 0;
    }

    public static int length = getLength(msg);

    public LengthMsgDecoder() {
        super(1024 * 1024, 0, length,0,length,true);
    }
}
