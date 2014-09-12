package com.manas.asyncimageloader;

/**
 * Created by Manas on 9/12/2014.
 */
public class ImageDimensions {
    private float reqWidth;
    private float reqHeight;

    public ImageDimensions(float reqWidth, float reqHeight) {
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }

    public float getReqWidth() {
        return reqWidth;
    }

    public float getReqHeight() {
        return reqHeight;
    }
}
