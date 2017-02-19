package com.herry.imagecompress.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.herry.imagecompress.R;
import com.herry.imagecompress.zoom.GestureImageView;

import java.io.File;
import java.util.List;

/**
 * Created by herry on 2016/11/23.
 */

public class ImagePreviewAdapter extends PagerAdapter
{
    private List<String> mImageList;
    private int mItemWidth;
    private int mItemHeight;

    public ImagePreviewAdapter(Context context, List<String> imageList)
    {
        mImageList = imageList;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mItemWidth = dm.widthPixels;
        mItemHeight = dm.heightPixels;
    }

    @Override
    public int getCount()
    {
        return mImageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.image_preview_item_layout, container, false);
        GestureImageView iv = (GestureImageView) view.findViewById(R.id.item_zoom_view);
        ImageView normalIV = (ImageView) view.findViewById(R.id.item_normal_view);
        String imagePath = mImageList.get(position);
        if (imagePath.endsWith("gif"))
        {
            iv.setVisibility(View.GONE);
            normalIV.setVisibility(View.VISIBLE);
            Glide.with(container.getContext()).load(new File(imagePath)).override(100, 100).fitCenter().into(normalIV);
        }
        else
        {
            iv.setVisibility(View.VISIBLE);
            normalIV.setVisibility(View.GONE);
            Glide.with(container.getContext()).load(new File(imagePath)).override(mItemWidth, mItemHeight).fitCenter().into(iv);
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        container.removeView((View) object);
    }
}
