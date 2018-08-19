package io.restn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.restn.netty.http.HttpFrameHandler;
import io.restn.netty.websocket.WebSocketFrameHandler;

public class RestServer {

	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;
	ServerBootstrap bootstrap;

	
	public RestServer () {
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup)
				 .channel(NioServerSocketChannel.class)
				 .handler(new LoggingHandler(LogLevel.INFO))
				 .childHandler(new RestServerInitializer());
	}

	public void start() {
		try {
			Channel channel = bootstrap.bind(8080).sync().channel();
			System.out.println("SERVER STARTED AT PORT 8080!");
			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	private static class RestServerInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast(new HttpServerCodec());
			pipeline.addLast(new HttpObjectAggregator(65536));
			pipeline.addLast(new WebSocketServerCompressionHandler());
			pipeline.addLast(new WebSocketServerProtocolHandler("/websocket", null, true));
			pipeline.addLast(new HttpFrameHandler());
			pipeline.addLast(new WebSocketFrameHandler());
		}

	}
}
