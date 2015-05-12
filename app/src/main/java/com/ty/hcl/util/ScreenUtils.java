package com.ty.hcl.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by huangchuanliang on 2015/3/16.
 */
public class ScreenUtils {
    private ScreenUtils(){
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context){
        WindowManager wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context){
        WindowManager wm=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     *获取状态栏height
     * @param context
     * @return
     */
    public static int getStstusHeight(Context context){
        int height=-1;
        try{
         Class<?> clazz= Class.forName("com.android.internal.R$dimen");
          Object object = clazz.newInstance();
           height = (Integer.parseInt(clazz.getField("status_bar_height").get(object).toString()));
        }catch (Exception e){
            e.printStackTrace();
        }
        return height;
    }


}
