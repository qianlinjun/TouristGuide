package com.example.qlj.touristguide.Fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Qljqian on 2017/4/15.
 */

public class FragAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList;


    public FragAdapter(FragmentManager fm) {
        super(fm);
    }

    public FragAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragmentList = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    /**
     * 重写，不让Fragment销毁
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }


    /**
     * 每次更新完成ViewPager的内容后，调用该接口，此处复写主要是为了让导航按钮上层的覆盖层能够动态的移动
     */
//    @Override
//    public void finishUpdate(ViewGroup container)
//    {
//        super.finishUpdate(container);//这句话要放在最前面，否则会报错
//        //获取当前的视图是位于ViewGroup的第几个位置，用来更新对应的覆盖层所在的位置
//        int currentItem=mViewPager.getCurrentItem();
//        if (currentItem==currenttab)
//        {
//            return ;
//        }
//        imageMove(mViewPager.getCurrentItem());
//        currenttab=mViewPager.getCurrentItem();
//    }
//
//}
//
//    /**
//     * 移动覆盖层
//     * @param moveToTab 目标Tab，也就是要移动到的导航选项按钮的位置
//     * 第一个导航按钮对应0，第二个对应1，以此类推
//     */
//    private void imageMove(int moveToTab)
//    {
//        int startPosition=0;
//        int movetoPosition=0;
//
//        startPosition=currenttab*(screenWidth/4);
//        movetoPosition=moveToTab*(screenWidth/4);
//        //平移动画
//        TranslateAnimation translateAnimation=new TranslateAnimation(startPosition,movetoPosition, 0, 0);
//        translateAnimation.setFillAfter(true);
//        translateAnimation.setDuration(200);
//        imageviewOvertab.startAnimation(translateAnimation);
//    }
}
