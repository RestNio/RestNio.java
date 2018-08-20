package io.restn.netty.http;

import static io.netty.handler.codec.http.HttpHeaderNames.UPGRADE;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

public class HttpRequestUtils {

	/**
	 * Checks if the httprequest is a websocket request.
	 * @param req
	 * @return true, if the request is part of the websocket business.
	 */
	public static boolean isWebSocketRequest(FullHttpRequest req) {
		return (
				req.headers().get(HttpHeaderNames.SEC_WEBSOCKET_VERSION) != null ||
				(
					req.headers().containsValue(HttpHeaderNames.CONNECTION, UPGRADE, true) &&
					HttpHeaderValues.WEBSOCKET.contentEqualsIgnoreCase(req.headers().get(UPGRADE))
				)
		);
	}

}
