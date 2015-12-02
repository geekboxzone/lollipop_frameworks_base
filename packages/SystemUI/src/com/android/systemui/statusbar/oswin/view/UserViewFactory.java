package com.android.systemui.statusbar.oswin.view;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;

import com.android.systemui.R;

/**
 * Created by tv metro on 9/3/14.
 */
public class UserViewFactory {
    private static UserViewFactory _instance;
    private final int PAGE_SIZE = 16;

    private UserViewFactory(){}
    public static UserViewFactory getInstance(){
        if(_instance == null)
            _instance = new UserViewFactory();

        return _instance;
    }

    ViewCreatorFactory mFactory = new DefautUserViewCreateFactory();

    public void setFactory(ViewCreatorFactory _factory){
        mFactory = _factory;
    }

    public ArrayList<View>  createUserView(Context context){
        return mFactory.create(context);
    }

    public int getPadding(Context context){
        int padding =  mFactory.getPadding(context);
        if(padding == 0){
            padding = context.getResources().getDimensionPixelSize(R.dimen.ITEM_DIVIDE_SIZE);
        }
        return padding;
    }

    public interface ViewCreatorFactory{
        ArrayList<View> create(Context context);
        int             getPadding(Context context);
    }

    public class DefautUserViewCreateFactory implements ViewCreatorFactory{
        @Override
        public ArrayList<View> create(Context context) {
            ArrayList<View> views = new ArrayList<View>();
            for(int i = 0; i < PAGE_SIZE; i++){
            	UserView item = new UserView(context);
            	views.add(item);
            }
            return  views;
        }

        @Override
        public int getPadding(Context context) {
            return context.getResources().getDimensionPixelSize(R.dimen.ITEM_DIVIDE_SIZE);
        }
    }

}
