package io.restn.netty.websocket.server;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.restn.netty.websocket.server.events.WebSocketHandshakeCompleteEvent;

import static io.restn.netty.websocket.server.WebSocketServerGeneralProtocolHandler.*;

public class WebSocketServerFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketServerFrameHandler.class);

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
		if (event instanceof WebSocketHandshakeCompleteEvent) {
			// Proceed in child channels first.
			ctx.fireUserEventTriggered(event);
			WebSocketHandshakeCompleteEvent shake = (WebSocketHandshakeCompleteEvent) event;
			ctx.channel().attr(URI).set(shake.requestUri());
			ctx.channel().attr(HEADERS).set(shake.requestHeaders());
			ctx.channel().attr(SUBPROTOCOL).set(shake.selectedSubprotocol());
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		if (frame instanceof TextWebSocketFrame) {
			// Send the uppercase string back.
			String request = ((TextWebSocketFrame) frame).text();
			logger.info("{} received {}", ctx.channel(), request);
			System.out.println("Received: " + request);
			ctx.channel().write(new TextWebSocketFrame(request.toUpperCase(Locale.US) + " ddd"));
		} else {
			String message = "unsupported frame type: " + frame.getClass().getName();
			throw new UnsupportedOperationException(message);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
		ctx.fireChannelReadComplete();
	}

}
