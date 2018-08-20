/*
 * This class is a modified version of the WebSocketProtocolHandler
 * which is part of Netty and licensed under the Apache 2 license.
 */
package io.restn.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

public abstract class WebSocketGeneralProtocolHandler extends MessageToMessageDecoder<WebSocketFrame> {

    private final boolean dropPongFrames;

    /**
     * Creates a new {@link WebSocketProtocolHandler} that will <i>drop</i> {@link PongWebSocketFrame}s.
     */
    protected WebSocketGeneralProtocolHandler() {
        this(true);
    }

    /**
     * Creates a new {@link WebSocketProtocolHandler}, given a parameter that determines whether or not to drop {@link
     * PongWebSocketFrame}s.
     *
     * @param dropPongFrames
     *            {@code true} if {@link PongWebSocketFrame}s should be dropped
     */
    protected WebSocketGeneralProtocolHandler(boolean dropPongFrames) {
        this.dropPongFrames = dropPongFrames;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        if (frame instanceof PingWebSocketFrame) {
            frame.content().retain();
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content()));
            return;
        }
        if (frame instanceof PongWebSocketFrame && dropPongFrames) {
            return;
        }

        out.add(frame.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }
}
