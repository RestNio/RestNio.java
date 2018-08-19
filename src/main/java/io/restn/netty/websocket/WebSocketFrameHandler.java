package io.restn.netty.websocket;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		if (frame instanceof TextWebSocketFrame) {
			//Send the uppercase string back.
			String request = ((TextWebSocketFrame) frame).text();
			logger.info("{} received {}", ctx.channel(), request);
			System.out.println("Received: " + request);
			ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.US) + " ddd"));
		} else {
			String message = "unsupported frame type: " + frame.getClass().getName();
			throw new UnsupportedOperationException(message);
		}
	}

}
