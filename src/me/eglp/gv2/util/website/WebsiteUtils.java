package me.eglp.gv2.util.website;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.json.JSONObject;

class WebsiteUtils {

	public static String success(Object data) {
		JSONObject obj = new JSONObject();
		obj.put("success", true);
		if(data!=null) obj.put("data", data);
		return obj.toString();
	}
	
	public static String error(String error) {
		JSONObject obj = new JSONObject();
		obj.put("success", false);
		obj.put("error", error);
		return obj.toString();
	}
	
	public static void writeString(DataOutputStream out, String str) throws IOException {
		byte[] bs = str.getBytes(Charset.forName("UTF-8"));
		writeInt(out, bs.length);
		out.write(bs);
	}
	
	public static String readString(DataInputStream in) throws IOException {
		int len = readInt(in);
		byte[] buf = new byte[len];
		in.read(buf);
		return new String(buf, Charset.forName("UTF-8"));
	}
	
	private static void writeInt(DataOutputStream out, int i) throws IOException{
		out.write((i >> 24) & 0xFF);
		out.write((i >> 16) & 0xFF);
		out.write((i >> 8) & 0xFF);
		out.write(i & 0xFF);
	}
	
	private static int readInt(DataInputStream in) throws IOException{
		int b1 = in.readUnsignedByte();
		int b2 = in.readUnsignedByte();
		int b3 = in.readUnsignedByte();
		int b4 = in.readUnsignedByte();
		return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
	}
	
}
