package com.ty.hcl.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ty.hcl.R;

/**
 * Created by huangchuanliang on 2015/3/16.
 */
public class MenuFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_left_layout, container, false);
    }
}
