package com.herry.imagecompress.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by herry on 2016/11/23.
 */

public class CompressBean implements Parcelable {
    private String imagePath;
    private long timeInterval;

    public CompressBean() {

    }

    public CompressBean(String imagePath, long timeInterval) {
        this.imagePath = imagePath;
        this.timeInterval = timeInterval;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }

    @Override
    public String toString() {
        return "CompressBean{" +
                "imagePath='" + imagePath + '\'' +
                ", timeInterval=" + timeInterval +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imagePath);
        dest.writeLong(timeInterval);
    }

    public static final Parcelable.Creator<CompressBean> CREATOR = new Parcelable.Creator<CompressBean>() {

        @Override
        public CompressBean createFromParcel(Parcel source) {
            CompressBean ret = new CompressBean();
            ret.setImagePath(source.readString());
            ret.setTimeInterval(source.readLong());
            return ret;
        }

        @Override
        public CompressBean[] newArray(int size) {
            return new CompressBean[size];
        }
    };

}
