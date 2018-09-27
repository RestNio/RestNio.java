package io.restn.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class RestTextPacket extends RestPacket {

	private String text;

	public RestTextPacket(String text) {
		this(Unpooled.copiedBuffer(text, CharsetUtil.UTF_8), new PacketMetadata(), text);
	}
    public RestTextPacket(ByteBuf data) {
        this(data, new PacketMetadata(), data.toString(CharsetUtil.UTF_8));
    }
    protected RestTextPacket(ByteBuf data, PacketMetadata metadata, String text) {
    	super(data, metadata);
    	metadata.put("content", "meep");
    	this.text = text;
    }

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		setData(Unpooled.copiedBuffer(text, CharsetUtil.UTF_8));
	}

    @Override
    public ByteBufHolder copy() {
    	return new RestTextPacket(new String(text));
	}

	@Override
    public  ByteBufHolder duplicate() {
    	return new RestTextPacket(data, metadata, text);
	}

}
