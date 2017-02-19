package com.herry.imagecompress.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.herry.imagecompress.PhotoPreviewActivity;
import com.herry.imagecompress.R;
import com.herry.imagecompress.util.CommonUtils;
import com.herry.imagecompress.util.Constants;
import com.herry.imagecompress.util.ImageCompressUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by herry on 2016/11/23.
 */

public class OriginAdapter extends RecyclerView.Adapter
{
    private List<String> mImageList;

    public OriginAdapter()
    {
        mImageList = new ArrayList<String>();
    }

    public OriginAdapter(List<String> imageList)
    {
        mImageList = imageList;
    }

    @Override
    public int getItemCount()
    {
        return mImageList.size();
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        ((OriginViewHolder) holder).bindView(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.origin_image_item_layout, parent, false);
        return new OriginViewHolder(itemView);
    }

    public class OriginViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        @BindView(R.id.item_icon)
        ImageView iconView;
        @BindView(R.id.item_desc)
        TextView descView;

        public OriginViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        protected void bindView(int position)
        {
            String imagePath = mImageList.get(position);
            Glide.with(itemView.getContext()).load(new File(imagePath)).override(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.icon_size), itemView.getContext().getResources().getDimensionPixelSize(R.dimen.icon_size)).centerCrop().into(iconView);
            descView.setText(ImageCompressUtils.decodeImageSize(imagePath));
            descView.append("\n" + CommonUtils.calcFileSize(imagePath));
        }

        @Override
        public void onClick(View v)
        {
            Context context = v.getContext();
            Intent i = new Intent(context, PhotoPreviewActivity.class);
            i.putExtra(Constants.EXTRA_PREVIEW_START_POSITION, getAdapterPosition());
            i.putExtra(Constants.EXTRA_PREVIEW_DATA_TYPE, Constants.DATA_TYPE_ORIGIN);
            i.putStringArrayListExtra(Constants.EXTRA_PREVIEW_DATA, (ArrayList<String>) mImageList);
            context.startActivity(i);
        }
    }


    public void addData(List<String> imageList)
    {
        mImageList.addAll(imageList);
        notifyDataSetChanged();
    }

    public void clear()
    {
        mImageList.clear();
        notifyDataSetChanged();
    }
}
