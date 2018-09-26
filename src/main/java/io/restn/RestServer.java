package io.restn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.restn.netty.http.HttpFrameHandler;
import io.restn.netty.util.EventLoopUtils;
import io.restn.netty.websocket.WebSocketFrameHandler;
import io.restn.netty.websocket.server.WebSocketServerGeneralProtocolHandler;

public class RestServer implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(RestServer.class);

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private ServerBootstrap bootstrap;
	private final boolean websocketEnabled;
	private final int port;
	

	public RestServer(int port) {
		this(port, true);
	}

	public RestServer(int port, boolean websocketEnabled) {
		this(port, null, websocketEnabled);
	}

	public RestServer(int port, SslContext sslCtx, boolean websocketEnabled) {
		this(port, sslCtx, websocketEnabled, true);
	}

	public RestServer(int port, SslContext sslCtx, boolean websocketEnabled, boolean preferEpoll) {
		this.port = port;
		this.websocketEnabled = websocketEnabled;
		bossGroup = EventLoopUtils.newEventLoopGroup(preferEpoll);
		workerGroup = EventLoopUtils.newEventLoopGroup(preferEpoll);
		bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup)
				 .channel(EventLoopUtils.getServerSocketChannel(preferEpoll))
				 .handler(new LoggingHandler(LogLevel.INFO))
				 .childHandler(new RestServerInitializer(sslCtx, websocketEnabled));
	}

	@Override
	public void run() {
		try {
			start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			stop();
		}
	}

	public void start() throws InterruptedException {
		logger.warn("MEEEEEEP");
		Channel channel = bootstrap.bind(port).sync().channel();
		logger.info("Server started at port " + port + ".");
		channel.closeFuture().sync();
	}

	public void stop() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	public boolean isWebsocketEnabled() {
		return websocketEnabled;
	}

	protected static class RestServerInitializer extends ChannelInitializer<SocketChannel> {

		private final SslContext sslCtx;
		private final boolean websocketEnabled;

		protected RestServerInitializer(SslContext sslCtx, boolean websocketEnabled) {
			this.sslCtx = sslCtx;
			this.websocketEnabled = websocketEnabled;
		}

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			if (sslCtx != null) pipeline.addLast("SSL", sslCtx.newHandler(ch.alloc()));
			pipeline.addLast("HttpCodec", new HttpServerCodec());
			pipeline.addLast("HttpAggregator", new HttpObjectAggregator(65536));
			if (websocketEnabled) pipeline.addLast("WebsocketComression", new WebSocketServerCompressionHandler());
			if (websocketEnabled) pipeline.addLast("WebsocketProtocol", new WebSocketServerGeneralProtocolHandler());
			pipeline.addLast("HttpFrame", new HttpFrameHandler());
			if (websocketEnabled) pipeline.addLast("WebsocketFrame", new WebSocketFrameHandler());
		}

	}

}
