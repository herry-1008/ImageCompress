package com.herry.imagecompress;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.herry.imagecompress.adapter.ImagePreviewAdapter;
import com.herry.imagecompress.bean.CompressBean;
import com.herry.imagecompress.util.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoPreviewActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.indicator)
    TextView mIndicatorView;

    private int mDataType;
    private int mPreviewStartPosition;
    private List<String> mImageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);
        ButterKnife.bind(this);
        extractIntentData();
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.removeOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setIndicator();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void extractIntentData() {
        Intent i = getIntent();
        mPreviewStartPosition = i.getIntExtra(Constants.EXTRA_PREVIEW_START_POSITION, 0);
        mDataType = i.getIntExtra(Constants.EXTRA_PREVIEW_DATA_TYPE, -1);
        switch (mDataType) {
            case Constants.DATA_TYPE_ORIGIN:
                mImageList = i.getStringArrayListExtra(Constants.EXTRA_PREVIEW_DATA);
                Log.e("ttt", "mImageList : " + mImageList);
                break;
            case Constants.DATA_TYPE_COMPRESSED:
                List<CompressBean> compressImageList = i.getParcelableArrayListExtra(Constants.EXTRA_PREVIEW_DATA);
                Log.e("ttt", "compressImageList : " + compressImageList);
                mImageList = convertData(compressImageList);
                break;
        }
    }

    private void initUI() {
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(new ImagePreviewAdapter(getApplicationContext(), mImageList));
        mViewPager.setCurrentItem(mPreviewStartPosition, true);
        setIndicator();
    }

    private void setIndicator() {
        mIndicatorView.setText(new StringBuilder().append(mViewPager.getCurrentItem() + 1).append(" / ").append(mImageList.size()));
    }

    private List<String> convertData(List<CompressBean> compressImageList) {
        List<String> ret = new ArrayList<String>(compressImageList.size());
        for (CompressBean bean : compressImageList) {
            ret.add(bean.getImagePath());
        }
        return ret;
    }
}
