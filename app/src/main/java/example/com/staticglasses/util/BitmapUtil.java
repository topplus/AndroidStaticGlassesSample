package example.com.staticglasses.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import threethird.it.sephiroth.android.library.exif2.ExifInterface;

/**
 * @author fandong
 * @date 2016/9/13
 * @description
 */

public class BitmapUtil {

    public static Bitmap decodeSampledBitmapFromResource(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((height / inSampleSize) >= reqHeight && (width / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //根据图片rotation信息，旋转图片
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.Orientation.TOP_LEFT:
                return bitmap;
            case ExifInterface.Orientation.TOP_RIGHT:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.Orientation.BOTTOM_RIGHT:
                matrix.setRotate(180);
                break;
            case ExifInterface.Orientation.BOTTOM_LEFT:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.Orientation.LEFT_TOP:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.Orientation.RIGHT_TOP:
                matrix.setRotate(90);
                break;
            case ExifInterface.Orientation.RIGHT_BOTTOM:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.Orientation.LEFT_BOTTOM:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean loadImage(View view, String url) {
        try {
            if (null != view) {
                Context context = view.getContext();
                Bitmap bitmap = null;
                int width = view.getWidth();
                int height = view.getHeight();
                if (null != context && !TextUtils.isEmpty(url)) {
                    if (url.startsWith("assets://")) {
                        AssetManager manager = context.getAssets();
                        String fileName = url.substring("assets://".length());
                        if (null != manager) {
                            InputStream ins = manager.open(fileName);
                            if (null != ins) {
                                //1.宽高，计算sample
                                bitmap = inputStreamBitmap(ins, width, height);
                            }
                        }
                    } else if (url.startsWith("drawable://")) {
                        String fileName = url.substring("drawable://".length());
                        InputStream inputStream = context.getResources().openRawResource(Integer.valueOf(fileName));
                        if (null != inputStream) {
                            bitmap = inputStreamBitmap(inputStream, width, height);
                        }
                    } else if (url.startsWith("file://")) {
                        String fileName = url.substring("file://".length());
                        if (!TextUtils.isEmpty(fileName)) {
                            File file = new File(fileName);
                            if (file.exists()) {
                                bitmap = inputStreamBitmap(new FileInputStream(file), width, height);
                            }
                        }
                    }
                }
                if (null != bitmap) {
                    if (view instanceof ImageView) {
                        ((ImageView) view).setImageBitmap(bitmap);
                        return true;
                    }
                    if (view instanceof ImageSwitcher) {
                        ((ImageSwitcher) view).setImageDrawable(new BitmapDrawable(bitmap));
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private static Bitmap inputStreamBitmap(InputStream inputStream, int width, int height) {
        Bitmap bitmap = null;
        try {
            if (width > 0 && height > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = inputStream.read(buffer)) > 0) {
                    bos.write(buffer, 0, length);
                }
                inputStream.close();
                byte[] data = bos.toByteArray();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, options);

                int sample = calculateInSampleSize(options, width, height);
                sample = sample > 1 ? sample : 1;
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = sample;
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
            } else {
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
