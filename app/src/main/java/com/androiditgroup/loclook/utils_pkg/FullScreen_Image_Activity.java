package com.androiditgroup.loclook.utils_pkg;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.androiditgroup.loclook.R;

import java.io.File;

/**
 * Created by OS1 on 18.12.2015.
 */
public class FullScreen_Image_Activity  extends     Activity
                                        implements  View.OnClickListener {

    private ImageView imageView;

    String imagePath = "";

    float rotateDegree = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // задать файл-компоновщик элементов окна
        setContentView(R.layout.dialog_fullscreen_image);

        ///////////////////////////////////////////////////////////////////////////////////

        imageView = (ImageView) findViewById(R.id.FullScreen_Image_IV);

        ///////////////////////////////////////////////////////////////////////////////////

        Intent intent = getIntent();

        imagePath    = intent.getStringExtra("imagePath");
        rotateDegree = intent.getFloatExtra("rotateDegree", 0.0f);

        ///////////////////////////////////////////////////////////////////////////////////

        setImage();
    }

    //
    private void setImage() {

        if(!imagePath.equals("")) {

            Matrix matrix = new Matrix();
            matrix.postRotate(rotateDegree);

            Bitmap bitmapToRotate = reDecodeFile();
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapToRotate, 0, 0, bitmapToRotate.getWidth(), bitmapToRotate.getHeight(), matrix, true);

            // задаем тип масштабирования изображения в представлении
            // imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // кладем изображение в представление
            imageView.setImageBitmap(rotatedBitmap);

            // задаем обработчик щелчка по изображению
            imageView.setOnClickListener(this);
        }
    }

    //
    private Bitmap reDecodeFile() {

        // устанавливаем значения для режима когда добавлено одно изображение
        int reqWidth    = 200;
        int reqHeight   = 200;

        return decodeFile(Uri.parse(imagePath), reqWidth, reqHeight);
    }

    //
    private Bitmap decodeFile(Uri photoPath, int reqWidth, int reqHeight) {

        // получаем изображение-заглушку
        Bitmap bitmap = ((BitmapDrawable) getApplication().getResources().getDrawable(R.drawable.no_photo_red)).getBitmap();

        try {

            // находим файл изображения
            File file = new File(getRealPathFromURI(photoPath));

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);

            // Calculate inSampleSize
            bmOptions.inSampleSize = calculateInSampleSize(bmOptions, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            bmOptions.inJustDecodeBounds = false;

            // получаем изображение
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
        }
        catch(Exception exc) {

            rotateDegree = 0.0f;
        }

        return bitmap;
    }

    //
    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    //
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // задаем размеры изображения
        final int height = options.outHeight;
        final int width = options.outWidth;

        // задаем коэфиициент сжатия
        int inSampleSize = 1;

        // если необходимо вычислить коэффициент сжатия
        if (height > reqHeight || width > reqWidth) {

            // получаем половину высоты и ширины изображения
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // если сжимть еще нужно
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                // увеличиваем коэффициент сжатия
                inSampleSize *= 2;
            }
        }

        // возвращаем результат
        return inSampleSize;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onClick(View view) {
        finish();
    }

    //
    public void onBackPressed() {
        finish();
    }
}