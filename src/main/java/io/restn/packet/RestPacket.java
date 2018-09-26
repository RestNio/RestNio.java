package io.restn.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * 
 */
public class RestPacket implements ByteBufHolder {

	protected ByteBuf data;
	protected PacketMetadata metadata;

	public RestPacket(ByteBuf data) {
		this(data, new PacketMetadata());
	}

	public RestPacket(ByteBuf data, PacketMetadata metadata) {
		this.data = data;
		this.metadata = metadata;
	}

	public FullHttpResponse toHttpResponse(HttpVersion version, HttpResponseStatus status) {
		return new DefaultFullHttpResponse(version, status, data);
	}

	public TextWebSocketFrame toTextWebsocketFrame() {
		return new TextWebSocketFrame(data);
	}

	public BinaryWebSocketFrame toBinaryWebsocketFrame() {
		return new BinaryWebSocketFrame(data);
	}

	public PacketMetadata getMetadata() {
		return metadata;
	}

    @Override
    public int refCnt() {
        return data.refCnt();
    }

    @Override
    public boolean release() {
        return data.release();
    }

    @Override
    public boolean release(int arg0) {
        return data.release(arg0);
    }

    @Override
    public ByteBuf content() {
        return data;
    }

    @Override
    public ByteBufHolder copy() {
    	return new RestPacket(data.copy());
    }

    @Override
    public  ByteBufHolder duplicate() {
    	return new RestPacket(data);
    }

    @Override
    public ByteBufHolder replace(ByteBuf replacement) {
        data = replacement;
        return this;
    }

    @Override
    public ByteBufHolder retain() {
        data.retain();
        return this;
    }

    @Override
    public ByteBufHolder retain(int arg0) {
        data.retain();
        return this;
    }

    @Override
    public ByteBufHolder retainedDuplicate() {
        return this.duplicate().retain();
    }

    @Override
    public ByteBufHolder touch() {
		data.touch();
        return this;
    }

    @Override
    public ByteBufHolder touch(Object toucher) {
    	data.touch(toucher);
        return this;
    }

}
