package io.restn.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class RestTextPacket extends RestPacket {

	private String text;

	public RestTextPacket(String text) {
		this(Unpooled.copiedBuffer(text, CharsetUtil.UTF_8), text);
	}
    public RestTextPacket(ByteBuf data) {
        this(data, data.toString(CharsetUtil.UTF_8));
    }
    private RestTextPacket(ByteBuf data, String text) {
    	super(data);
    	this.text = text;
    }
    
    @Override
    public ByteBufHolder copy() {
    	return new RestTextPacket(new String(text));
	}

	@Override
    public  ByteBufHolder duplicate() {
    	return new RestTextPacket(data, text);
	}

}
