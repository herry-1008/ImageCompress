package com.herry.imagecompress.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.herry.imagecompress.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

public class ImageCompressUtils {
    private static final boolean DEBUG = true;
    private static final String TAG = "ImageCompressUtils";

    private static final String DIR_NAME = "image_compress";
    private static final String TYPE_JPG = "jpg";
    private static final String TYPE_PNG = "png";
    private static final String TYPE_GIF = "gif";
    /*采样的尺寸阈值*/
    private static final int SIZE_THRESHOLD = 1280;
    /*长宽比例因子阈值*/
    private static final int RATIO_THRESHOLD = 2;
    /*图片压缩质量*/
    private static final int COMPRESS_QUALITY = 62;
    /**
     * android仅提供了png,jpg,webp三种格式的图片压缩<br/>
     * 同时gif的图片暂时没有合适的压缩解决方案<br/>
     * 如果发现解析出的图片格式不在FILE_TYPE_MAP中，默认使用jpg
     */
    private static Map<String, String> FILE_TYPE_MAP;

    static {
        FILE_TYPE_MAP = new HashMap<String, String>();
        FILE_TYPE_MAP.put("ffd8ff", TYPE_JPG);
        FILE_TYPE_MAP.put("89504e", TYPE_PNG);
        FILE_TYPE_MAP.put("474946", TYPE_GIF);
    }

    static String obtainSdcardPath(Context context) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download" + File.separator + context.getPackageName() + File.separator + DIR_NAME + File.separator;
    }

    static String buildCompressPath(Context context, String fileType) {
        return new StringBuilder().append(obtainSdcardPath(context)).append("image-").append(System.currentTimeMillis()).append("-.").append(fileType).toString();
    }

    static void createCompressFile(File compressFile) throws IOException {
        compressFile.getParentFile().mkdirs();
        if (!compressFile.exists()) {
            compressFile.createNewFile();
        }
    }

    /*获取图片文件类型*/
    static String extractImageType(String imagePath) {
        long extractStart = System.currentTimeMillis();
        String sampleData = "";
        BufferedSource bufferedSource = null;
        try {
            bufferedSource = Okio.buffer(Okio.source(new File(imagePath)));
            //读取前三个字节
            ByteString headers = bufferedSource.readByteString(3);
            sampleData = headers.hex().toLowerCase();
        } catch (FileNotFoundException e) {
            // nothing
        } catch (IOException e) {
            //nothing
        } finally {
            CommonUtils.silentClose(bufferedSource);
        }
        long extractEnd = System.currentTimeMillis();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("extract type time interval : %d ms.", extractEnd - extractStart));
        }
        return determineImageType(sampleData);
    }

    /*根据读取的数据，判断图片类型标识*/
    static String determineImageType(String dataSample) {
        if (CommonUtils.isNull(dataSample)) {
            return TYPE_JPG;
        }
        String ret = FILE_TYPE_MAP.get(dataSample);
        if (CommonUtils.isNull(ret)) {
            return TYPE_JPG;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("image type : %s.", ret));
        }
        return ret;
    }

    /**
     * 根据图片的Orientation，获取旋转的角度
     *
     * @return
     */
    static int calcRotateDegree(String imagePath) {
        long exifStart = System.currentTimeMillis();
        int ret = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    ret = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    ret = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    ret = 180;
                    break;
            }
        } catch (IOException e) {
            //Nothing
        }
        long exifEnd = System.currentTimeMillis();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("extract exif time interval : %d ms.", exifEnd - exifStart));
            Log.d(TAG, String.format("rotate degree : %d.", ret));
        }
        return ret;
    }

    /**
     * 计算采样比例
     *
     * @param reqWidth
     * @param reqHeight
     * @param width
     * @param height
     * @param options
     */
    static void calcInSampleSize(int reqWidth, int reqHeight, int width, int height, BitmapFactory.Options options) {
        long calcStart = System.currentTimeMillis();
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio;
            final int widthRatio;
            if (reqHeight == 0) {
                sampleSize = (int) Math.floor((float) width / (float) reqWidth);
            } else if (reqWidth == 0) {
                sampleSize = (int) Math.floor((float) height / (float) reqHeight);
            } else {
                heightRatio = (int) Math.floor((float) height / (float) reqHeight);
                widthRatio = (int) Math.floor((float) width / (float) reqWidth);
                sampleSize = Math.min(heightRatio, widthRatio);
            }
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        long calcEnd = System.currentTimeMillis();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("calc inSample time interval : %d ms.", calcEnd - calcStart));
        }
    }

    static void decodeImageBoundary(String originalImagePath, BitmapFactory.Options options) {
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originalImagePath, options);
    }

    /*计算压缩的目标尺寸*/
    static int[] calcCompressTargetSize(Context context, BitmapFactory.Options options) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int originWidth = options.outWidth;
        int originHeight = options.outHeight;
        int compressWidth = screenWidth;
        int compressHeight = screenHeight;
        if (originWidth <= SIZE_THRESHOLD && originHeight <= SIZE_THRESHOLD) {
            //都小于1280,保留原尺寸
            return new int[]{originWidth, originHeight};
        } else if (originWidth > SIZE_THRESHOLD && originHeight > SIZE_THRESHOLD) {
            //都大于1280
            boolean widthDetermine = originWidth > originHeight;
            int ratio = widthDetermine ? originWidth / originHeight : originHeight / originWidth;
            if (ratio < RATIO_THRESHOLD) {
                if (widthDetermine) {
                    compressWidth = SIZE_THRESHOLD;
                    compressHeight = Math.round(originHeight * SIZE_THRESHOLD * 1.0f / originWidth);
                } else {
                    compressHeight = SIZE_THRESHOLD;
                    compressWidth = Math.round(originWidth * SIZE_THRESHOLD * 1.0f / originHeight);
                }
                return new int[]{compressWidth, compressHeight};
            } else {
                if (widthDetermine) {
                    compressHeight = SIZE_THRESHOLD;
                    compressHeight = Math.round(originWidth * SIZE_THRESHOLD * 1.0f / originHeight);
                } else {
                    compressWidth = SIZE_THRESHOLD;
                    compressHeight = Math.round((originHeight * SIZE_THRESHOLD * 1.0f / originWidth));
                }
                return new int[]{compressWidth, compressHeight};
            }
        } else {
            //其中一边大于1280，另一边小于1280
            boolean widthDetermine = originWidth > originHeight;
            int ratio = widthDetermine ? originWidth / originHeight : originHeight / originWidth;
            if (ratio < RATIO_THRESHOLD) {
                if (widthDetermine) {
                    compressWidth = SIZE_THRESHOLD;
                    compressHeight = Math.round(originHeight * SIZE_THRESHOLD * 1.0f / originWidth);
                } else {
                    compressHeight = SIZE_THRESHOLD;
                    compressWidth = Math.round(originWidth * SIZE_THRESHOLD * 1.0f / originHeight);
                }
                return new int[]{compressWidth, compressHeight};
            } else {
                //原尺寸返回
                return new int[]{originWidth, originHeight};
            }
        }
    }

    /**
     * 针对图片的目标尺寸以及旋转角度进行处理
     */
    static Bitmap scaleAndRotateIfNeeded(Matrix matrix, Bitmap bitmap, int[] targetSizes, int rotateDegree) {
        long start = System.currentTimeMillis();
        boolean changed = false;
        if (targetSizes[0] != bitmap.getWidth() && targetSizes[1] != bitmap.getHeight()) {
            changed = true;
            float scaleWidth = ((float) targetSizes[0]) / bitmap.getWidth();
            float scaleHeight = ((float) targetSizes[1]) / bitmap.getHeight();
            matrix.postScale(scaleWidth, scaleHeight);
        }
        if (rotateDegree > 0) {
            changed = true;
            matrix.postRotate(rotateDegree);
        }
        Bitmap newBitmap = null;
        if (changed) {
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            newBitmap = bitmap;
        }
        long end = System.currentTimeMillis();
        if (changed && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("scale and rotate time interval : %d ms.", end - start));
        }
        return newBitmap;
    }

    /*压缩图片，输出到指定的文件*/
    static String doFinalCompress(Context context, Bitmap bitmap, String fileType) {
        long compressStart = System.currentTimeMillis();
        String ret = null;
        File compressFile = new File(buildCompressPath(context, fileType));
        BufferedSink bufferedSink = null;
        try {
            createCompressFile(compressFile);
            bufferedSink = Okio.buffer(Okio.sink(compressFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, bufferedSink.outputStream());
            ret = compressFile.getAbsolutePath();
        } catch (IOException e) {
            //Nothing
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            CommonUtils.silentClose(bufferedSink);
        }
        long compressEnd = System.currentTimeMillis();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("compress time interval : %d ms.", compressEnd - compressStart));
        }
        return ret;
    }

    /**
     * 压缩图片，返回压缩后的缓存地址
     *
     * @param context
     * @param originalImagePath
     * @return
     */
    public static String compress(Context context, String originalImagePath) {
        String fileType = extractImageType(originalImagePath);
        //Gif图片暂时没有找到合适的压缩算法，直接略过，返回原地址
        if (TextUtils.equals(fileType, TYPE_GIF)) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, ">>>>>>>>>>>>>>>>>ignore gif file,return origin file. <<<<<<<<<<<<<<<<<");
            }
            return originalImagePath;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        decodeImageBoundary(originalImagePath, options);
        int[] targetSizes = calcCompressTargetSize(context, options);
        calcInSampleSize(targetSizes[0], targetSizes[1], options.outWidth, options.outHeight, options);
        //do real work here
        Bitmap bitmap = BitmapFactory.decodeFile(originalImagePath, options);
        //protection
        //解析失败，可能传入的不是图片文件等，则原地址返回
        if (bitmap == null) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, ">>>>>>>>>>decode file fail,return origin file. <<<<<<<<<<<<<<<<<");
            }
            return originalImagePath;
        }
        //scale to appointed size
        int rotateDegree = calcRotateDegree(originalImagePath);
        Matrix matrix = new Matrix();
        bitmap = scaleAndRotateIfNeeded(matrix, bitmap, targetSizes, rotateDegree);
        String ret = doFinalCompress(context, bitmap, fileType);
        //压缩出错，返回原地址
        if (CommonUtils.isNull(ret)) {
            return originalImagePath;
        }
        if (BuildConfig.DEBUG) {
            Log.e(TAG, ">>>>>>>>>>compress single image accomplished.<<<<<<<<<<<<");
        }
        return ret;
    }

    /**
     * 任何时候退出压缩图片逻辑，都需要调用clear方法清理掉之前压缩的文件
     */
    public static void clear(Context context) {
        synchronized (ImageCompressUtils.class) {
            String rootPath = obtainSdcardPath(context);
            File parentFile = new File(rootPath);
            File[] list = parentFile.listFiles();
            if (list == null || list.length <= 0) {
                return;
            }
            for (File file : list) {
                file.delete();
            }
        }
    }

    //for demo usage
    public static String decodeImageSize(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new StringBuilder().append(options.outWidth).append(" * ").append(options.outHeight).toString();
    }
}
