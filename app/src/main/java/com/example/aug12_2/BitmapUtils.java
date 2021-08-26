package com.example.aug12_2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

@SuppressWarnings({"unused", "WeakerAccess"})
public class BitmapUtils {
    private static final int LEN_ZERO = 0;

    private static byte[] mColorPalette = new byte[1024];
    private static byte[] mBMPFileHeader = new byte[14];
    private static byte[] mDIBHeader = new byte[40];

    /**
     * Rotates given Bitmap by "angle" degrees.
     *
     * @param bitmap Bitmap to rotate.
     * @param angle  Degrees to rotate Bitmap by.
     * @return Rotated Bitmap.
     */
    @Nullable
    public static Bitmap
    rotate(Bitmap bitmap,       // Returns the BMP object back after rotating at an angle
           int angle) {

        if (null == bitmap)
            return null;

        Matrix mat = new Matrix();
        mat.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
    }

    /**
     * Converts a given Bitmap to byte array.
     *
     * @param bitmap Bitmap to convert to byte array.
     * @return If Bitmap is null empty byte array is returned, else converted Bitmap in byte form.
     */
    @Nullable
    public static byte[]        // Returns a byte array of the bitmap object provided in the args
    toBytes(Bitmap bitmap) {

        if (null == bitmap)
            return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Nullable
    public static Bitmap        // Returns bmp object from a the arguments: byte array, width and height
    decodeRAW(byte[] src,
              int width,
              int height) {

        if (null == src || LEN_ZERO == src.length)
            return null;

        byte[] Bits = new byte[src.length * 4];
        int i;
        for (i = 0; i < src.length; i++) {
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = src[i];
            Bits[i * 4 + 3] = -1;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bitmap;
    }

    /* Convert byte array to Bitmap.
     *
     * @param data Byte array to convert to Bitmap.
     *
     * @return If data is not Bitmap then NULL is returned.
     */
    public static Bitmap
    decode(byte[] data) {

        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception ignore) {
            return null;
        }
    }

    /* Convert Bitmap to gray scale color format.
     *
     * @param bmpOriginal Bitmap to convert.
     * @return
     */
    public static Bitmap
    toGrayScale(Bitmap bitmap) {

        /* Create a new Bitmap with same dimensions as given one. */
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        /* Set up objects to change Bitmap "paint" colors. */
        Canvas c = new Canvas(bmp);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);

        /* Create new Bitmap to copy with new paint scheme. */
        c.drawBitmap(bitmap, 0, 0, paint);

        return bmp;
    }

    /* Convert Bitmap to gray scale color format; also copies Bitmap to rawArray.
     *
     * @param bmpOriginal Bitmap to convert.
     * @return
     */
    public static Bitmap
    toGrayScale(Bitmap bitmap,
                ByteBuffer rawArray) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        if (rawArray.capacity() >= (height * width)) {
            rawArray.rewind();

            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x)
                    rawArray.put((byte) Color.red(bitmap.getPixel(x, y)));
            }
            rawArray.rewind();
        }

        return BitmapUtils.toGrayScale(bitmap);
    }

    public static Bitmap
    cropToSquare(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width) ? height - (height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0) ? 0 : cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0) ? 0 : cropH;

        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
    }

    /* --------------------------------------------------------------------------------------------
     *
     * Private helpers.
     *
     * --------------------------------------------------------------------------------------------
     */

    private static void
    create_parts(Bitmap img) {

        byte[] mBitmapData = toGrayScaleByteArray(img);

        copy(mBMPFileHeader, new byte[]{(byte) 'B', (byte) 'M'}, 0);

        copy(mBMPFileHeader,
                writeInt(mBMPFileHeader.length +
                        mDIBHeader.length +
                        mColorPalette.length +
                        mBitmapData.length),
                2);

        copy(mBMPFileHeader, new byte[]{(byte) 'M', (byte) 'C', (byte) 'A', (byte) 'T'}, 6);

        copy(mBMPFileHeader,
                writeInt(mBMPFileHeader.length +
                        mDIBHeader.length +
                        mColorPalette.length),
                /* Bitmap raw data offset. */
                10);

        /* DIB Header length. */
        copy(mDIBHeader, writeInt(mDIBHeader.length), 0);
        /* Image width. */
        copy(mDIBHeader, writeInt(img.getWidth()), 4);
        /* Image height. */
        copy(mDIBHeader, writeInt(img.getHeight()), 8);
        /* Color planes. */
        copy(mDIBHeader, new byte[]{(byte) 1, (byte) 0}, 12);
        /* Bits per pixel. */
        copy(mDIBHeader, new byte[]{(byte) 8, (byte) 0}, 14);
        /* Compression method: BI_RGB = 0. */
        copy(mDIBHeader, writeInt(0), 16);
        /* Length of raw bitmap data. */
        copy(mDIBHeader, writeInt(mBitmapData.length), 20);
        /* Horizontal resolution. */
        copy(mDIBHeader, writeInt(1000), 24);
        /* Vertical resolution. */
        copy(mDIBHeader, writeInt(1000), 28);
        /* Numbers of colors in palette. */
        copy(mDIBHeader, writeInt(256), 32);
        /* Number of important colors used, all colors are important. */
        copy(mDIBHeader, writeInt(0), 36);

        mColorPalette = createColorPalette();
    }

    private static void
    copy(byte[] destination,
         byte[] source,
         int index) {

        System.arraycopy(source, 0, destination, index, source.length);
    }

    /* Convert Bitmap to gray scale and return in byte array format. */
    public static byte[]
    toGrayScaleByteArray(Bitmap source) {

        /* Determine padding needed for Bitmap file. */
        int padding = (source.getWidth() % 4) != 0 ? 4 - (source.getWidth() % 4) : 0;

        /* Create array to contain bitmap data with padding. */
        byte[] bytes = new byte[source.getWidth() * source.getHeight() + padding * source.getHeight()];

        for (int y = 0; y < source.getHeight(); ++y) {

            for (int x = 0; x < source.getWidth(); ++x) {
                int pixel = source.getPixel(x, y);

                /* Calculate gray scale value [0, 255] from RGB values.*/
                int g = (int) (0.3 * Color.red(pixel) +
                        0.59 * Color.green(pixel) +
                        0.11 * Color.blue(pixel));

                bytes[(source.getHeight() - 1 - y) * source.getWidth() +
                        (source.getHeight() - 1 - y) * padding + x] = (byte) g;
            }

            /* add padding. */
            for (int i = 0; i < padding; ++i)
                bytes[(source.getHeight() - y) * source.getWidth() +
                        (source.getHeight() - 1 - y) * padding + i] = (byte) 0;
        }
        return bytes;
    }

    private static byte[]
    writeInt(int value) {

        byte[] b = new byte[4];
        b[0] = (byte) (value & 0x000000FF);
        b[1] = (byte) ((value & 0x0000FF00) >> 8);
        b[2] = (byte) ((value & 0x00FF0000) >> 16);
        b[3] = (byte) ((value & 0xFF000000) >> 24);
        return b;
    }

    private static byte[]
    createColorPalette() {

        byte[] color_palette = new byte[1024];
        for (int i = 0; i < 256; i++) {
            color_palette[i * 4] = (byte) (i); /* Blue. */
            color_palette[i * 4 + 1] = (byte) (i); /* Green. */
            color_palette[i * 4 + 2] = (byte) (i); /* Red. */
            color_palette[i * 4 + 3] = (byte) 0; /* Padding. */
        }
        return color_palette;
    }
}