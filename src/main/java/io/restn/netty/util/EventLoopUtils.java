package io.restn.netty.util;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class EventLoopUtils {

	/**
	 * Creates an eventLoopGroup based on if epoll is preferred and enabled.
	 * @param preferEpoll
	 * @return
	 */
	public static EventLoopGroup newEventLoopGroup(boolean preferEpoll) {
		if (preferEpoll && Epoll.isAvailable()) {
			return new EpollEventLoopGroup();
		} else {
			return new NioEventLoopGroup();
		}
	}
}
