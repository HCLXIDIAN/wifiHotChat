package com.ty.hcl.util;

public class Constant {
	public static final String ENCOD="gbk";
	
	/**文本消息端口*/
	public static final int MESSAGE_PORT=42836;
	/**语音消息端口*/
	public static final int VOICE_PORT=42837;
	/**文件消息端口*/
	public static final int FILE_PORT=42838;
	/**视屏流端口*/
	public static final int VIDEO_PORT=42839;
	/**局域网内所有ip
     * //和你的主机位于同一网段的主机都会受到目的地址是255.255.255.255的广播包,但是发送此信息的主机不会收到
     * */
	public static final String ALL_ADDRESS="255.255.255.255";

	
	public static final String id="d6228819a8cca82a";
	public static final String key="bc72c19fc590f2bc";
}
