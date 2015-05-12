package com.ty.hcl.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ty.hcl.R;

/**
 * Created by 传良 on 2015/5/12.
 */


public class Dialog {

    private Context context;

    private ViewGroup decorView;

    private ViewGroup rootView,contentContainer;

    private View v_top,v_bottom;

    private Activity activity;//context转为Activity

    private ViewGroup vg_real_contentview;

    private TYPE curType;

    public enum TYPE {
        FULLMESSAGE,TOP,BOTTOM,CENTER
    }


    Dialog(Builder builder){
        context = builder.context;

        activity = (Activity)context;

        curType = builder.curType;

        decorView = (ViewGroup)activity.getWindow().getDecorView().findViewById(android.R.id.content);

        rootView = (ViewGroup)LayoutInflater.from(context).inflate(R.layout.base_container, null);

        contentContainer = (ViewGroup)rootView.findViewById(R.id.fl_contentConteiner);
        v_top = rootView.findViewById(R.id.v_top);

        v_bottom = rootView.findViewById(R.id.v_bottom);

        vg_real_contentview = builder.realContentView;

        createDialog();




    }

    private void createDialog() {
        initView();
        initEvents();

    }

    private void initView() {
        contentContainer.addView(vg_real_contentview);
    }

    private void initEvents() {
         if(curType == TYPE.FULLMESSAGE){
             contentContainer.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     close();
                 }
             });
         }
    }


    public void show(){
        decorView.addView(rootView);
    }


    public void close(){
        decorView.removeView(rootView);
    }

    public static class Builder {


        private TYPE curType;

        Context context;

        private ViewGroup realContentView;

        Builder(Context context) {
        this.context = context;
        }

        public Builder setContentView(ViewGroup view){
            this.realContentView = view;
            return this;
        }

        public Builder setCurType(TYPE curType) {
            this.curType = curType;
            return this;
        }


      public Dialog create(){
        return new Dialog(this);
      }
    }
}