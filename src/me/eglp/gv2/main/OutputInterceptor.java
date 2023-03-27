package me.eglp.gv2.main;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class OutputInterceptor extends PrintStream {
	
	private Consumer<String> handler;
	
	public OutputInterceptor(OutputStream out, Consumer<String> handler) {
		super(out);
		this.handler = handler;
	}
	
	@Override
	public void write(byte[] buf, int off, int len) {
		super.write(buf, off, len);
		handler.accept(new String(buf, off, len));
	}

}
