package com.herry.imagecompress.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.herry.imagecompress.PhotoPreviewActivity;
import com.herry.imagecompress.R;
import com.herry.imagecompress.bean.CompressBean;
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

public class CompressAdapter extends RecyclerView.Adapter {

    private List<CompressBean> mImageList;

    public CompressAdapter() {
        mImageList = new ArrayList<CompressBean>();
    }

    public CompressAdapter(Activity act, List<CompressBean> imageList) {
        mImageList = imageList;
    }

    public void clear() {
        mImageList.clear();
        notifyDataSetChanged();
    }

    public void addImage(CompressBean compressBean) {
        mImageList.add(compressBean);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.compress_image_item_layout, parent, false);
        return new CompressViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((CompressViewHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    public class CompressViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.item_icon)
        ImageView iconView;
        @BindView(R.id.item_desc)
        TextView descView;

        public CompressViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        protected void bindView(int position) {
            CompressBean compressBean = mImageList.get(position);
            Glide.with(itemView.getContext()).load(new File(compressBean.getImagePath())).override(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.icon_size), itemView.getContext().getResources().getDimensionPixelSize(R.dimen.icon_size)).centerCrop().into(iconView);
            descView.setText(ImageCompressUtils.decodeImageSize(compressBean.getImagePath()));
            descView.append("\n");
            descView.append(CommonUtils.calcFileSize(compressBean.getImagePath()));
            descView.append("\n");
            descView.append(CommonUtils.formatTimeInterval(compressBean.getTimeInterval()));
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent i = new Intent(context, PhotoPreviewActivity.class);
            i.putExtra(Constants.EXTRA_PREVIEW_START_POSITION, getAdapterPosition());
            i.putExtra(Constants.EXTRA_PREVIEW_DATA_TYPE, Constants.DATA_TYPE_COMPRESSED);
            i.putParcelableArrayListExtra(Constants.EXTRA_PREVIEW_DATA, (ArrayList<? extends Parcelable>) mImageList);
            context.startActivity(i);
        }
    }
}
