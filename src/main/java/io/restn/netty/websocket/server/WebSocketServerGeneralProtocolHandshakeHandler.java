/*
 * This class is a modified version of the WebSocketServerProtocolHandshakeHandler
 * which is part of Netty and licensed under the Apache 2 license.
 */
package io.restn.netty.websocket.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslHandler;
import io.restn.netty.http.HttpRequestUtils;
import io.restn.netty.websocket.server.events.WebSocketHandshakeCompleteEvent;

import static io.netty.handler.codec.http.HttpUtil.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import static io.netty.handler.codec.http.HttpHeaderNames.*;

/**
 * Handles the HTTP handshake (the HTTP Upgrade request) for
 * {@link WebSocketServerGeneralProtocolHandler}.
 */
class WebSocketServerGeneralProtocolHandshakeHandler extends ChannelInboundHandlerAdapter {

	private final String subprotocols;
	private final boolean allowExtensions;
	private final int maxFramePayloadSize;
	private final boolean allowMaskMismatch;

	WebSocketServerGeneralProtocolHandshakeHandler(String subprotocols, boolean allowExtensions,
			int maxFrameSize, boolean allowMaskMismatch) {
		this.subprotocols = subprotocols;
		this.allowExtensions = allowExtensions;
		maxFramePayloadSize = maxFrameSize;
		this.allowMaskMismatch = allowMaskMismatch;
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		final FullHttpRequest req = (FullHttpRequest) msg;
		// In case we are not a websocket update request, fallthrough to the next handler.
		if (!HttpRequestUtils.isWebSocketRequest(req)) {
			ctx.fireChannelRead(msg);
			return;
		}
		try {
			if (req.method() != GET) {
				sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
				return;
			}

			final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
					getWebSocketLocation(ctx.pipeline(), req, req.uri()), subprotocols, allowExtensions,
					maxFramePayloadSize, allowMaskMismatch);
			final WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
			if (handshaker == null) {
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
			} else {
				final ChannelFuture handshakeFuture = handshaker.handshake(ctx.channel(), req);
				handshakeFuture.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (!future.isSuccess()) {
							ctx.fireExceptionCaught(future.cause());
						} else {
							ctx.fireUserEventTriggered(new WebSocketHandshakeCompleteEvent(req.uri(),
									req.headers(), handshaker.selectedSubprotocol()));
						}
					}
				});
				WebSocketServerGeneralProtocolHandler.setHandshaker(ctx.channel(), handshaker);
				ctx.pipeline().replace(this, "WS403Responder",
						WebSocketServerGeneralProtocolHandler.forbiddenHttpRequestResponder());
			}
		} finally {
			req.release();
		}
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private static String getWebSocketLocation(ChannelPipeline cp, HttpRequest req, String path) {
		String protocol = "ws";
		if (cp.get(SslHandler.class) != null) {
			protocol = "wss";
		}
		String host = req.headers().get(HOST);
		return protocol + "://" + host + path;
	}

}
