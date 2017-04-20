package com.example.qlj.touristguide.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qlj.touristguide.Activity.MainActivity;
import com.example.qlj.touristguide.R;

/**
 * Created by Qljqian on 2017/4/17.
 */

public class Fragment_Share extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_share, container, false);
        return view;
    }
}
