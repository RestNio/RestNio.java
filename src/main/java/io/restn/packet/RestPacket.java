package io.restn.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * 
 */
public class RestPacket extends DefaultByteBufHolder {

	protected ByteBuf data;
	protected PacketMetadata metadata;

	/**
	 * Creates a new RestPacket containing the data specified.
	 * @param data 
	 */
	public RestPacket(ByteBuf data) {
		this(data, new PacketMetadata());
	}

	//Protected constructor for duplicate reasons.
	protected RestPacket(ByteBuf data, PacketMetadata metadata) {
		super(data);
		this.metadata = metadata;
	}

	/**
	 * Transforms this {@link RestPacket} to a {@link FullHttpResponse}.
	 * A {@link FullHttpResponse}  is used to send data over http.
	 * Note: This method does not retain the packet. 
	 * If the frame is sent, chances are the Packet object will be deallocated.
	 * To prevent this behaviour use {@link #retain()} before calling this method.
	 * @param version The {@link HttpVersion} used to carry the response.
	 * @param status The {@link HttpResponseStatus} to convey with the message.
	 * @return The {@link FullHttpResponse} created response.
	 */
	public FullHttpResponse toHttpResponse(HttpVersion version, HttpResponseStatus status) {
		return new DefaultFullHttpResponse(version, status, data);
	}

	/**
	 * Transforms this {@link RestPacket} to a {@link TextWebSocketFrame}.
	 * A {@link TextWebSocketFrame}  is used to send text over a websocket.
	 * This frame is specifically used to send UTF8 text, it is imperative you use
	 * {@link #toBinaryWebsocketFrame()} for anything else.
	 * Note: This method does not retain the packet. 
	 * If the frame is sent, chances are the Packet object will be deallocated.
	 * To prevent this behaviour use {@link #retain()} before calling this method.
	 * @return The {@link TextWebSocketFrame} created frame.
	 */
	public TextWebSocketFrame toTextWebsocketFrame() {
		return new TextWebSocketFrame(data);
	}

	/**
	 * Transforms this {@link RestPacket} to a {@link BinaryWebSocketFrame}.
	 * A {@link BinaryWebSocketFrame}  is used to send raw bytes over a websocket.
	 * If the packet is specifically used to send UTF8 text, it is recommended to use
	 * {@link #toTextWebsocketFrame()} instead.
	 * Note: This method does not retain the packet. 
	 * If the frame is sent, chances are the Packet object will be deallocated.
	 * To prevent this behaviour use {@link #retain()} before calling this method.
	 * @return The {@link BinaryWebSocketFrame} created frame.
	 */
	public BinaryWebSocketFrame toBinaryWebsocketFrame() {
		return new BinaryWebSocketFrame(data);
	}

	/**
	 * Gets the metadata of this packet.
	 * 
	 * @return the {@link PacketMetadata} belonging to this packet.
	 */
	public PacketMetadata getMetadata() {
		return metadata;
	}

	/**
	 * Gets the data of this packet.
	 * Note: This method does not retain the data.
	 * 
	 * @return The {@link ByteBuf} contained the packet's data.
	 * 
	 * @throws IllegalReferenceCountException if the bytebuf is already released.
	 */
	public ByteBuf getData() {
		return content();
	}

	/**
	 * Gets the data of this packet and retains its reference.
	 * @return The {@link ByteBuf} containing the retained packet's data.
	 * 
	 * @throws IllegalReferenceCountException if the bytebuf is already released.
	 */
	public ByteBuf getRetainedData() {
		return content().retain();
	}

	/**
	 * Sets the data of the packet.
	 * Note: This method releases data previously in the packet.
	 * @param data The data to replace this packets data with.
	 */
	public void setData(ByteBuf data) {
		this.data.release();
		this.data = data;
	}

	/**
	 * Copies this Packet and allows reference to old packet.
	 * This method also performs a deep-copy on the packets metadata.
	 * @return The {@link ByteBufHolder} copy of the {@link RestPacket}.
	 */
    @Override
    public ByteBufHolder copy() {
    	return new RestPacket(data.retainedDuplicate(), metadata.copy());
	}

	/**
	 * Duplicates this Packet. A new heap object is created but contensts
	 * are linked with the old packet. (Both for the metadata and the data)
	 * @return The {@link ByteBufHolder} duplicate of the {@link RestPacket}.
	 */
	@Override
    public  ByteBufHolder duplicate() {
    	return new RestPacket(data, metadata);
	}

}
