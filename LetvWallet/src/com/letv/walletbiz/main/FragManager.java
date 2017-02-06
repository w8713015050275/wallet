package com.letv.walletbiz.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;

import com.letv.wallet.common.fragment.BaseFragment;
import com.letv.walletbiz.main.fragment.MainFragment;

import java.util.ArrayList;

/**
 * 一个activity拥有一个fragmanager,fragment的实例由activity拥有,FragManager不提供获取具体fragment实例的方法
 * Created by zhuchuntao on 16-12-21.
 */

public class FragManager<T extends MainFragment> {

    private static FragmentManager fragmentManager;
    private T topFragment;

    private SparseArray<ArrayList<T>> fragments;


    public FragManager(AppCompatActivity activity) {
        fragmentManager = activity.getSupportFragmentManager();
        fragments = new SparseArray<>();
    }

    /**
     * 此方法暂时不用，
     *
     * @param fragmentContainer
     * @param baseFragment
     */
    private void showFragmentReplace(int fragmentContainer, T baseFragment) {
        if (null == baseFragment) {
            return;
        }
        Fragment t = fragmentManager.findFragmentById(fragmentContainer);
        //如果当前页面不做任何事情，直接返回
        if (null != t && (t.getClass().equals(baseFragment.getClass()))) {
            return;
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(fragmentContainer, baseFragment);
        ft.commit();
    }

    public void showFragmentAdd(int fragmentContainer, T baseFragment) {
        if (null == baseFragment) {
            return;
        }
        hideAll(fragmentContainer);
        FragmentTransaction ft = fragmentManager.beginTransaction();

        Fragment tagFragment = fragmentManager.findFragmentByTag(baseFragment.getClass().toString());
        if (null == tagFragment) {
            addFragement(fragmentContainer, baseFragment);
            ft.add(fragmentContainer, baseFragment, baseFragment.getClass().toString());
            //ft.addToBackStack(baseFragment.getClass().toString());
        } else {
            ft.show(tagFragment);
        }
        topFragment=baseFragment;
        ft.commit();
    }

    public T getTopFragment(){
        return topFragment;
    }

    /**
     * 隐藏所有的fragment
     */
    private void hideAll(int fragmentContainer) {
        if (null != fragments.get(fragmentContainer)) {
            for (BaseFragment fragment : fragments.get(fragmentContainer)) {
                fragmentManager.beginTransaction().hide(fragment).commit();
            }
        }
    }

    /**
     * 添加一个fragment
     *
     * @param fragment
     */
    private void addFragement(int fragmentContainer, T fragment) {
        ArrayList<T> lists = fragments.get(fragmentContainer);
        if (null == lists) {
            lists = new ArrayList<T>();
            lists.add(fragment);
            fragments.put(fragmentContainer, lists);
        } else {
            lists.add(fragment);
            fragments.put(fragmentContainer, lists);
        }
    }

    public void destroy() {
        fragments.clear();
    }
}
