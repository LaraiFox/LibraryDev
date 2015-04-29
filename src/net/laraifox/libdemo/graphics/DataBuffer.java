package net.laraifox.libdemo.graphics;

import net.laraifox.libdemo.utils.BufferUtils;

import org.lwjgl.opengl.GL15;

public class DataBuffer {
	public static final int GL_DYNAMIC_DRAW = GL15.GL_DYNAMIC_DRAW;
	public static final int GL_STATIC_DRAW = GL15.GL_STATIC_DRAW;
	public static final int GL_STREAM_DRAW = GL15.GL_STREAM_DRAW;

	private int id;
	private int count;

	public DataBuffer(float[] data, int usage) {
		this.id = GL15.glGenBuffers();
		this.count = data.length;

		this.bind();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, BufferUtils.createFloatBuffer(data, true), usage);
		this.unbind();
	}

	@Override
	protected void finalize() {
		GL15.glDeleteBuffers(id);
	}

	public void bind() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
	}

	public void unbind() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public int getCount() {
		return count;
	}
}
