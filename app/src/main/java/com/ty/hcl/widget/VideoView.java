package com.ty.hcl.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;

import com.ty.hcl.ChatApplication;

public class VideoView extends View{

	Bitmap bitmap;
	public static int height= ChatApplication.height-200-50;
	public static int width=(int) (ChatApplication.height*0.75);
	Matrix matrix=new Matrix();
	
    private void init(){
    	matrix.setRotate(-90);
        matrix.postScale(5f, 2.5f);
    }
	
	public VideoView(Context context) {
		super(context);
		init();
	}
	
	
	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}


	public VideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(bitmap!=null){
            /*
            创建一个指定大小的缩略图
       * @param source 源文件(Bitmap类型)
       * @param width  压缩成的宽度
        * @param height 压缩成的高度
             */
			canvas.drawBitmap(ThumbnailUtils.extractThumbnail(bitmap,ChatApplication.width,ChatApplication.height/2),0,0,null);
//			canvas.drawBitmap(bitmap, matrix, null);
		}
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		invalidate();
	}
	
	
	
}
