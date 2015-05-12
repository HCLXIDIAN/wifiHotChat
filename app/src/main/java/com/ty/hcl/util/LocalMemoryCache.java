package com.ty.hcl.util;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * 内存管理，主要用来缓存图片
 */
public class LocalMemoryCache {
	private static LocalMemoryCache instance;
	
	private LruCache<String, Bitmap> lruCache;

	private List<String> keys;//记录key值
	
	private LocalMemoryCache(){
        int maxMemory =(int)Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 8;
		lruCache=new LruCache<String, Bitmap>(mCacheSize){
            //必须重写此方法，来测量Bitmap的大小
				@Override
				protected int sizeOf(String key, Bitmap value) {
					return value.getRowBytes()*value.getHeight();
				}
			};
		keys=new ArrayList<String>();
	}
	
	public static LocalMemoryCache getInstance(){
		return instance==null?instance=new LocalMemoryCache():instance;
	}
	
	public void put(String key,Bitmap bitmap){
		if(bitmap!=null){
			lruCache.put(key, bitmap);
			keys.add(key);
		}
	}
	
	public Bitmap get(String key){
		return lruCache.get(key);
	}
	
	public void remove(String key){
		lruCache.remove(key);
	}
	
	public void removeAll(){
		for(String k:keys){
			lruCache.remove(k);
		}
		keys.clear();
		Log.d("LocalMemoryCache", "清除全部图片缓存");
	}
	
}
