package com.herry.imagecompress;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.herry.imagecompress.adapter.CompressAdapter;
import com.herry.imagecompress.adapter.OriginAdapter;
import com.herry.imagecompress.bean.CompressBean;
import com.herry.imagecompress.util.ImageCompressUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private static final int REQUEST_IMAGE = 0x001;
    private static final int REQUEST_PERMISSION = 0x002;

    @BindView(R.id.choose_image_btn)
    Button mChoosePhotoBtn;
    @BindView(R.id.recyclerview_origin)
    RecyclerView mOriginRecyclerView;
    @BindView(R.id.compress_image_btn)
    Button mCompressPhotoBtn;
    @BindView(R.id.recyclerview_compressed)
    RecyclerView mCompressRecyclerView;


    private OriginAdapter mOriginAdapter;
    private CompressAdapter mCompressAdapter;

    private List<String> mImageList;

    private boolean mCompressing;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ImageCompressUtils.clear(getApplicationContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE)
        {
            if (resultCode == RESULT_OK)
            {
                // Get the result list of select image paths
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                // do your logic ....
                mImageList.clear();
                mImageList.addAll(path);
                mOriginAdapter.clear();
                mOriginAdapter.addData(path);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                requestImage();
            }
            else
            {
                Toast.makeText(this, "Permission Deny", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.choose_image_btn)
    public void choosePhoto()
    {
        //TODO
        Log.d(TAG, "choosePhoto");
        int reqPermissionCode = 0x002;
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, permissions, reqPermissionCode);
            return;
        }
        requestImage();
    }


    @OnClick(R.id.compress_image_btn)
    public void compressPhoto()
    {
        Log.d(TAG, "compressPhoto");
        if (mCompressing)
        {
            Toast.makeText(this, "Compress ongoing, please waiting...", Toast.LENGTH_SHORT).show();
            return;
        }
        mCompressing = true;
        mCompressAdapter.clear();
        new CompressTask().execute();
    }


    private void requestImage()
    {
        Intent intent = new Intent(this, MultiImageSelectorActivity.class);

        // whether show camera
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);

        // max select image amount
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);

        // select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void initData()
    {
        mCompressing = false;
        mImageList = new ArrayList<String>();
        mOriginAdapter = new OriginAdapter();

        mOriginRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mOriginRecyclerView.setHasFixedSize(true);
        mOriginRecyclerView.setAdapter(mOriginAdapter);
        mCompressAdapter = new CompressAdapter();

        mCompressRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mCompressRecyclerView.setHasFixedSize(true);
        mCompressRecyclerView.setAdapter(mCompressAdapter);
    }


    private class CompressTask extends AsyncTask<Void, CompressBean, Void>
    {
        @Override
        protected Void doInBackground(Void[] params)
        {
            for (int i = 0; i < mImageList.size(); i++)
            {
                long start = System.currentTimeMillis();
                String compressPath = ImageCompressUtils.compress(getApplicationContext(), mImageList.get(i));
                long end = System.currentTimeMillis();
                publishProgress(new CompressBean(compressPath, end - start));
            }
            mCompressing = false;//reset
            return null;

        }


        @Override
        protected void onProgressUpdate(CompressBean... values)
        {
            super.onProgressUpdate(values);
            mCompressAdapter.addImage(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "Compress Completed", Toast.LENGTH_SHORT).show();
        }
    }
}
