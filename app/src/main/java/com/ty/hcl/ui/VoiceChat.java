package com.ty.hcl.ui;

import java.io.IOException;
import java.net.InetAddress;

import android.os.Bundle;

import com.ty.hcl.R;
import com.ty.hcl.listener.UDPVoiceListener;

/**
 * 语音聊天
 */
public class VoiceChat extends Base{

		private String chatterIP;//记录当前用户ip
		private UDPVoiceListener voiceListener;
		
	    @Override  
	    public void onCreate(Bundle savedInstanceState) {  
	        super.onCreate(savedInstanceState);  
	        setContentView(R.layout.video_chat);  
	        chatterIP=getIntent().getStringExtra("IP");
	        findViews();
	        try {
				voiceListener=UDPVoiceListener.getInstance(InetAddress.getByName(chatterIP));
				 voiceListener.open();
			} catch (IOException e) {
				e.printStackTrace();
//				finish();
				showToast("抱歉，语音聊天器打开失败");
			}
	    }  
	    
	    private void findViews(){
	    	
	    }
	    
	    
	    @Override  
	    protected void onDestroy() {  
	        super.onDestroy();  
	        try {
				voiceListener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }  
	
}
