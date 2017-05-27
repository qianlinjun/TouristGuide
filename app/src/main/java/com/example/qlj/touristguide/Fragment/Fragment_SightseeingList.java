package com.example.qlj.touristguide.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.qlj.touristguide.Activity.PanoramaShow;
import com.example.qlj.touristguide.Euclid.EuclidFragment;
import com.example.qlj.touristguide.Euclid.EuclidListAdapter;
import com.example.qlj.touristguide.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by Qljqian on 2017/4/15.
 */

public class Fragment_SightseeingList extends EuclidFragment {
    String iddd;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("fragment_sightseeing"," onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_sightseeinglist, container, false);
        mWrapper = (RelativeLayout) view.findViewById(R.id.wrapper);
        mListView = (ListView) view.findViewById(R.id.list_view);
        mToolbar = (FrameLayout) view.findViewById(R.id.toolbar_list);
        mToolbarProfile = (RelativeLayout) view.findViewById(R.id.toolbar_profile);
        mProfileDetails = (LinearLayout) view.findViewById(R.id.wrapper_profile_details);
        mTextViewProfileName = (TextView) view.findViewById(R.id.text_view_profile_name);
        mTextViewProfileDescription = (TextView) view.findViewById(R.id.text_view_profile_description);


        mFullscene = view.findViewById(R.id.button_profile);
        mFullscene.post(new Runnable() {
            @Override
            public void run() {
                mInitialProfileButtonX = mFullscene.getX();
            }
        });
        mFullscene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent2=new Intent();
                intent2.setClass(getContext(), PanoramaShow.class);
                startActivity(intent2);
            }
        });

        view.findViewById(R.id.toolbar_profile_back).setOnClickListener(this);

        sScreenWidth = getResources().getDisplayMetrics().widthPixels;
        sProfileImageHeight = getResources().getDimensionPixelSize(R.dimen.height_profile_image);
        sOverlayShape = buildAvatarCircleOverlay();

        initList();
        Log.d("fragment_sightseeing"," onCreateView");
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }




    @Override
    public BaseAdapter getAdapter() {
        Map<String, Object> profileMap;
        List<Map<String, Object>> profilesList = new ArrayList<>();

        int[] avatars = {
                R.drawable.picture1,
                R.drawable.picture2,
                R.drawable.picture3,
                R.drawable.picture4,
                R.drawable.picture5,
                R.drawable.picture6,
                R.drawable.picture7,
                R.drawable.picture8,
                R.drawable.picture9,
                R.drawable.picture10,
                R.drawable.picture11,
                R.drawable.picture12,
                R.drawable.picture13,
                R.drawable.picture14,
                R.drawable.picture15
        };
        String[] names = Fragment_Map.array_Name;//名称
        String[] shortinfos = getResources().getStringArray(R.array.short_info);
        String[] longinfos = getResources().getStringArray(R.array.array_specificInfo);

        for (int i = 0; i < avatars.length; i++) {
            profileMap = new HashMap<>();
            profileMap.put(EuclidListAdapter.KEY_AVATAR, avatars[i]);
            profileMap.put(EuclidListAdapter.KEY_NAME, names[i]);
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_SHORT, shortinfos[i]);
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_FULL, longinfos[i]);
            profilesList.add(profileMap);
        }

        return new EuclidListAdapter(getContext(), R.layout.list_item, profilesList);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
