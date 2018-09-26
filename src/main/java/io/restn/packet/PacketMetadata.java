package io.restn.packet;

import java.util.HashMap;
import java.util.Map;

public class PacketMetadata {

	private Map<String, Object> meta;

	public PacketMetadata() {
		this(new HashMap<>());
	}

	public PacketMetadata(Map<String, Object> meta) {
		this.meta = meta;
	}

	//public PacketMetadata clone(PacketMetadata clone) {
		//return new PacketMetadata(clone.meta.)
	//}

	public PacketMetadata duplicate(PacketMetadata duplicate) {
		return new PacketMetadata(duplicate.meta);
	}

	public void clear() {
		meta.clear();
	}

	public Object put(String name, Object value) {
		return meta.put(name, value);
	}

	public Object get(String name) {
		return meta.get(name);
	}

	public Object remove(String name) {
		return meta.remove(name);
	}

	public String getString(String name) {
		return (String) meta.get(name);
	}

	public int getInt(String name) {
		return (Integer) meta.get(name);
	}

	public double getDouble(String name) {
		return (Double) meta.get(name);
	}

	public float getFloat(String name) {
		return (Float) meta.get(name);
	}

}
