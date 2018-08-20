/*
 * This class is a modified version of WebSocketServerProtocolHandler
 * which is part of Netty and licensed under the Apache 2 license.
 */
package io.restn.netty.websocket.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.Utf8FrameValidator;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.util.AttributeKey;
import io.restn.netty.websocket.WebSocketGeneralProtocolHandler;

import java.util.List;

import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * This handler does all the heavy lifting for you to run a websocket server.
 *
 * It takes care of websocket handshaking as well as processing of control
 * frames (Close, Ping, Pong). Text and Binary data frames are passed to the
 * next handler in the pipeline (implemented by you) for processing.
 *
 * See <tt>io.netty.example.http.websocketx.html5.WebSocketServer</tt> for
 * usage.
 *
 * The implementation of this handler assumes that you just want to run a
 * websocket server alongside a http response server. You can turn this off
 * by constructing using false for allowExtensions.
 *
 * To know once a handshake was done you can intercept the
 * {@link ChannelInboundHandler#userEventTriggered(ChannelHandlerContext, Object)}
 * and check if the event was instance of {@link HandshakeComplete}, the event
 * will contain extra information about the handshake such as the request and
 * selected subprotocol.
 */
public class WebSocketServerGeneralProtocolHandler extends WebSocketGeneralProtocolHandler {

	private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR_KEY = AttributeKey
			.valueOf(WebSocketServerHandshaker.class, "HANDSHAKER");
	public static final AttributeKey<String> URI = AttributeKey.valueOf("uri");
	public static final AttributeKey<HttpHeaders> HEADERS = AttributeKey.valueOf("headers");
	public static final AttributeKey<String> SUBPROTOCOL = AttributeKey.valueOf("subprotocol");

	private final String subprotocols;
	private final boolean allowExtensions;
	private final int maxFramePayloadLength;
	private final boolean allowMaskMismatch;

	public WebSocketServerGeneralProtocolHandler() {
		this(null);
	}

	public WebSocketServerGeneralProtocolHandler(String subprotocols) {
		this(subprotocols, true);
	}

	public WebSocketServerGeneralProtocolHandler(String subprotocols, boolean allowExtensions) {
		this(subprotocols, allowExtensions, 65536);
	}

	public WebSocketServerGeneralProtocolHandler(String subprotocols, boolean allowExtensions, int maxFrameSize) {
		this(subprotocols, allowExtensions, maxFrameSize, false);
	}

	public WebSocketServerGeneralProtocolHandler(String subprotocols, boolean allowExtensions, int maxFrameSize,
			boolean allowMaskMismatch) {
		this(subprotocols, allowExtensions, maxFrameSize, allowMaskMismatch, true);
	}

	public WebSocketServerGeneralProtocolHandler(String subprotocols, boolean allowExtensions, int maxFrameSize,
			boolean allowMaskMismatch, boolean dropPongFrames) {
		super(dropPongFrames);
		this.subprotocols = subprotocols;
		this.allowExtensions = allowExtensions;
		maxFramePayloadLength = maxFrameSize;
		this.allowMaskMismatch = allowMaskMismatch;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		ChannelPipeline cp = ctx.pipeline();
		if (cp.get(WebSocketServerGeneralProtocolHandshakeHandler.class) == null) {
			// Add the WebSocketHandshakeHandler before this one.
			ctx.pipeline().addBefore(ctx.name(), WebSocketServerGeneralProtocolHandshakeHandler.class.getName(),
					new WebSocketServerGeneralProtocolHandshakeHandler(subprotocols, allowExtensions,
							maxFramePayloadLength, allowMaskMismatch));
		}
		if (cp.get(Utf8FrameValidator.class) == null) {
			// Add the UFT8 checking before this one.
			ctx.pipeline().addBefore(ctx.name(), Utf8FrameValidator.class.getName(), new Utf8FrameValidator());
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
		if (frame instanceof CloseWebSocketFrame) {
			WebSocketServerHandshaker handshaker = getHandshaker(ctx.channel());
			if (handshaker != null) {
				frame.retain();
				handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame);
			} else {
				ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
			}
			return;
		}
		super.decode(ctx, frame, out);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof WebSocketHandshakeException) {
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
					Unpooled.wrappedBuffer(cause.getMessage().getBytes()));
			ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		} else {
			ctx.fireExceptionCaught(cause);
			ctx.close();
		}
	}

	static WebSocketServerHandshaker getHandshaker(Channel channel) {
		return channel.attr(HANDSHAKER_ATTR_KEY).get();
	}

	static void setHandshaker(Channel channel, WebSocketServerHandshaker handshaker) {
		channel.attr(HANDSHAKER_ATTR_KEY).set(handshaker);
	}

	static ChannelHandler forbiddenHttpRequestResponder() {
		return new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				if (msg instanceof FullHttpRequest) {
					((FullHttpRequest) msg).release();
					FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.FORBIDDEN);
					ctx.channel().writeAndFlush(response);
				} else {
					ctx.fireChannelRead(msg);
				}
			}
		};
	}

}