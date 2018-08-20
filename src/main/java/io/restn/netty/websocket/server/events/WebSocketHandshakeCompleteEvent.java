package io.restn.netty.websocket.server.events;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * Event called when websocket handshake is completed on the server.
 * After the event is called all variables (uri, headers and subprotocol) will be loaded into attributes. 
 */
public class WebSocketHandshakeCompleteEvent {

	private String requestUri;
	private HttpHeaders requestHeaders;
	private String selectedSubprotocol;

	public WebSocketHandshakeCompleteEvent(String requestUri, HttpHeaders requestHeaders, String selectedSubprotocol) {
		this.requestUri = requestUri;
		this.requestHeaders = requestHeaders;
		this.selectedSubprotocol = selectedSubprotocol;
	}

	public String requestUri() {
		return requestUri;
	}

	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}

	public HttpHeaders requestHeaders() {
		return requestHeaders;
	}

	public void setRequestHeaders(HttpHeaders requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public String selectedSubprotocol() {
		return selectedSubprotocol;
	}

	public void setSelectedSubProtocol(String selectedSubprotocol) {
		this.selectedSubprotocol = selectedSubprotocol;
	}

}
