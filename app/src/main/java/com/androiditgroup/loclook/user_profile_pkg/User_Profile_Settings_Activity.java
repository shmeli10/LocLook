package com.androiditgroup.loclook.user_profile_pkg;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.utils_pkg.DB_Helper;
import com.isseiaoki.simplecropview.CropImageView;

/**
 * Created by OS1 on 13.01.2016.
 */
public class User_Profile_Settings_Activity extends     Activity
                                            implements  View.OnClickListener {

    private Context             context;
    private SharedPreferences   shPref;

    private final int arrowBackResId        = R.id.UserProfileSettings_ArrowBackWrapLL;
    private final int saveChangesResId      = R.id.UserProfileSettings_SaveChangesWrapLL;

    private final int topBgLLResId          = R.id.UserProfileSettings_TopBGLL;
    private final int changeBgLLResId       = R.id.UserProfileSettings_ChangeBgLL;
    private final int userAvatarIVResId     = R.id.UserProfileSettings_UserAvatarIV;
    private final int changeAvatarIVResId   = R.id.UserProfileSettings_ChangeAvatarIV;
    private final int userNameLLResId       = R.id.UserProfileSettings_UserNameLL;
    private final int aboutMeLLResId        = R.id.UserProfileSettings_AboutMeLL;
    private final int siteLLResId           = R.id.UserProfileSettings_SiteLL;

    private final int userNameResId         = R.id.UserProfileSettings_UserNameET;
    private final int aboutMeResId          = R.id.UserProfileSettings_AboutMeET;
    private final int siteResId             = R.id.UserProfileSettings_SiteET;

    private final int BG_FROM_CHOOSE_IMG     = 1;
    private final int BG_FROM_CAMERA         = 2;
    private final int AVATAR_FROM_CHOOSE_IMG = 3;
    private final int AVATAR_FROM_CAMERA     = 4;

    private int user_id;

    private int avatarRotateOn;

    private String user_name    = "";
    private String bg_path      = "";
    private String avatar_path  = "";
    private String about_me     = "";
    private String site         = "";

    private Uri bg_path_uri;
    private Uri avatar_path_uri;

    private LinearLayout topBgLL;

    private ImageView userAvatarIV;

    private EditText userNameET;
    private EditText aboutMeET;
    private EditText siteET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile_settings);

        context = getApplicationContext();
        shPref  = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
        loadTextFromPreferences();

        /////////////////////////////////////////////////////////////////////////////////////

        // получаем данные при переходе со страницы профиля пользователя
        Intent intent = getIntent();

        // получаем имя пользователя
        user_name = intent.getStringExtra("user_name");

        // получаем путь к фону
        bg_path = intent.getStringExtra("bg_path");

        // получаем путь к аватару
        avatar_path = intent.getStringExtra("avatar_path");

        // получаем текст "Обо мне"
        about_me = intent.getStringExtra("about_me");

        // получаем адрес сайта
        site = intent.getStringExtra("site");

        /////////////////////////////////////////////////////////////////////////////////////

        topBgLL = (LinearLayout) findViewById(topBgLLResId);

        // задаем фон профиля
        setBackground(bg_path);

        /////////////////////////////////////////////////////////////////////////////////////

        userAvatarIV = (ImageView) findViewById(userAvatarIVResId);

        // задаем тип масштабирования изображения в представлении
        userAvatarIV.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // задаем аватар профиля
        setUserAvatar(avatar_path);

        /////////////////////////////////////////////////////////////////////////////////////

        (findViewById(arrowBackResId)).setOnClickListener(this);
        (findViewById(saveChangesResId)).setOnClickListener(this);

        (findViewById(changeBgLLResId)).setOnClickListener(this);
        (findViewById(changeAvatarIVResId)).setOnClickListener(this);
        (findViewById(userNameLLResId)).setOnClickListener(this);
        (findViewById(aboutMeLLResId)).setOnClickListener(this);
        (findViewById(siteLLResId)).setOnClickListener(this);

        userNameET  = (EditText) findViewById(userNameResId);
        userNameET.setText(user_name);

        aboutMeET   = (EditText) findViewById(aboutMeResId);
        aboutMeET.setText(about_me);

        siteET      = (EditText) findViewById(siteResId);
        siteET.setText(site);

        // скрываем клавиатуру
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onClick(View view) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        switch(view.getId()) {

            // щелчок по "стрелке назад"
            case arrowBackResId:
                                        // скрываем клавиатуру
                                        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                                        // осуществляем переход к UserProfileActivity
                                        moveToUserProfileActivity();
                                        break;
            // щелчок по "изображению сохранения изменений"
            case saveChangesResId:
                                        IBinder windowToken = null;

                                        if(userNameET.getWindowToken() != null)
                                            windowToken = userNameET.getWindowToken();
                                        else if(aboutMeET.getWindowToken() != null)
                                            windowToken = aboutMeET.getWindowToken();
                                        else if(siteET.getWindowToken() != null)
                                            windowToken = siteET.getWindowToken();

                                        if(windowToken != null)
                                            // скрываем клавиатуру
                                            imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);

                                        // сохраняем изменения в БД
                                        saveChangesInDB();

                                        if(bg_path_uri != null) // {
                                            // сохраняем путь к фону профиля, для передачи User_Profile_Activity
                                            bg_path = getRealPathFromURI(bg_path_uri);

                                            // Log.d("myLogs", "saveChangesResId: bg_path_uri= " + bg_path_uri + " bg_path= " + bg_path);
                                            // }
                                            // else
                                            // Log.d("myLogs", "saveChangesResId: bg_path_uri is NULL");

                                        if(avatar_path_uri != null) // {
                                            // сохраняем путь к аватару пользователя, для передачи User_Profile_Activity
                                            avatar_path = getRealPathFromURI(avatar_path_uri);

                                            // Log.d("myLogs", "saveChangesResId: avatar_path_uri= " + avatar_path_uri + " avatar_path= " + avatar_path);
                                            // }
                                            // else
                                            // Log.d("myLogs", "saveChangesResId: avatar_path_uri is NULL");

                                        break;
            // щелчок по "изображению изменения фона профиля пользователя"
            case changeBgLLResId:
                                        // выводим на жкран "диалоговое окно для добавления изображения"
                                        showPhotoDialog("bg");
                                        break;
            // щелчок по "изображению смены аватара пользователя"
            case changeAvatarIVResId:
                                        // выводим на жкран "диалоговое окно для добавления изображения"
                                        showPhotoDialog("avatar");
                                        break;
            // щелчок по "полю с именем пользователя"
            case userNameLLResId:
                                        // автоматически показываем клавиатуру
                                        imm.showSoftInput(userNameET, InputMethodManager.SHOW_IMPLICIT);

                                        // получаем длину имени пользователя
                                        int userNameLength = userNameET.getText().length();

                                        // если текст есть
                                        if(userNameLength > 0)
                                            // перемещаем курсор в конец имени пользователя
                                            userNameET.setSelection(userNameLength);
                                        else
                                            // перемещаем курсор в начало строки
                                            userNameET.setSelection(0);
                                        break;
            // щелчок по "полю с текстом о себе"
            case aboutMeLLResId:
                                        // автоматически показываем клавиатуру
                                        imm.showSoftInput(aboutMeET, InputMethodManager.SHOW_IMPLICIT);

                                        // получаем длину текста о себе
                                        int aboutMeLength = aboutMeET.getText().length();

                                        // если текст есть
                                        if(aboutMeLength > 0)
                                            // перемещаем курсор в конец текста о себе
                                            aboutMeET.setSelection(aboutMeLength);
                                        else
                                            // перемещаем курсор в начало строки
                                            aboutMeET.setSelection(0);
                                        break;
            // щелчок по "полю с URL сайта"
            case siteLLResId:
                                        // автоматически показываем клавиатуру
                                        imm.showSoftInput(siteET, InputMethodManager.SHOW_IMPLICIT);

                                        // получаем длину URL сайта
                                        int siteLength = siteET.getText().length();

                                        // если текст есть
                                        if(siteLength > 0)
                                            // перемещаем курсор в конец URL сайта
                                            siteET.setSelection(siteLength);
                                        else
                                            // перемещаем курсор в начало строки
                                            siteET.setSelection(0);
                                        break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentData) {
        super.onActivityResult(requestCode, resultCode, intentData);

        Toast.makeText(this, "requestCode = " +requestCode, Toast.LENGTH_SHORT).show();

        // если ответ из диалогового окна пришел
        // if((requestCode == 1) && (resultCode == RESULT_OK) && (intentData != null)) {
        if((resultCode == RESULT_OK) && (intentData != null)) {

            switch(requestCode) {

                // "Выбрать изображение" (фон профиля пользователя)
                case BG_FROM_CHOOSE_IMG:
                // "Камера" (фон профиля пользователя)
                case BG_FROM_CAMERA:
                                        // получаем путь к выбранному фону профиля
                                        bg_path_uri = intentData.getData();

                                        // кладем фон профиля в представление
                                        setBackground(getRealPathFromURI(bg_path_uri));

                                        Toast.makeText(this, "Изменение фона профиля.", Toast.LENGTH_LONG).show();
                                        break;
                // "Выбрать изображение" (аватар пользователя)
                case AVATAR_FROM_CHOOSE_IMG:
                // "Камера" (аватар пользователя)
                case AVATAR_FROM_CAMERA:
                                        // получаем путь выбранного аватара пользователя
                                        avatar_path_uri = intentData.getData();

                                        // кладем аватар пользователя в представление
                                        // Bitmap bitmap = decodeSampledBitmapFromResource(getRealPathFromURI(avatar_path_uri), 200, 200);
                                        // showImageCropDialog(bitmap);
                                        showImageCropDialog(decodeSampledBitmapFromResource(getRealPathFromURI(avatar_path_uri), 200, 200));

                                        // Toast.makeText(this, "Изменение аватара пользователя.", Toast.LENGTH_LONG).show();
                                        break;
            }
        }
        else
            Toast.makeText(this, "Изменить аватар пользователя не удалось.", Toast.LENGTH_LONG).show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void saveChangesInDB() {

        DB_Helper dbHelper = new DB_Helper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////////////

        String table_name   = "user_data";

        String userName     = userNameET.getText().toString();
        String bgPath       = bg_path;
        String avatarPath   = avatar_path;
        String aboutMe      = aboutMeET.getText().toString();
        String site         = siteET.getText().toString();

        // если идентификатор пользователя определен
        if(user_id > 0) {
            // сохраняем внесенные изменения в БД
            // dbHelper.updateTable(db, table_name, user_id, new String[]{"login", "avatar_path", "about_me", "site"}, new String[]{userName, avatarPath, aboutMe, site});
            dbHelper.updateTable(db, table_name, user_id, new String[]{"login", "bg_path", "avatar_path", "about_me", "site"}, new String[]{userName, bgPath, avatarPath, aboutMe, site});

            Toast.makeText(this, "Данные успешно сохранены", Toast.LENGTH_LONG).show();
        }
    }

    //
    private void showPhotoDialog(final String callerName) {

        // создаем "диалоговое окно для добавления изображения"
        final Dialog dialog = new Dialog(User_Profile_Settings_Activity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.dialog_camera);
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        // задаем обработчик щелчка по "кнопке Выбрать изображение"
        dialog.findViewById(R.id.Publication_ChooseImageTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int requestCode = 0;

                if(callerName.equals("bg"))
                    requestCode = BG_FROM_CHOOSE_IMG;
                else if(callerName.equals("avatar"))
                    requestCode = AVATAR_FROM_CHOOSE_IMG;

                // вызываем стандартную галерею для выбора изображения
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

                // Тип получаемых объектов - image:
                photoPickerIntent.setType("image/*");

                // Запускаем переход с ожиданием обратного результата в виде информации об изображении:
                startActivityForResult(photoPickerIntent, requestCode);

                // закрываем "диалоговое окно для добавления изображения"
                dialog.dismiss();
            }
        });

        // задаем обработчик щелчка по "кнопке Камера"
        dialog.findViewById(R.id.Publication_CameraTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    int requestCode = 0;

                    if(callerName.equals("bg"))
                        requestCode = BG_FROM_CAMERA;
                    else if(callerName.equals("avatar"))
                        requestCode = AVATAR_FROM_CAMERA;

                    // отправляем намерение для запуска камеры
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(captureIntent, requestCode);
                } catch (ActivityNotFoundException e) {
                    // Выводим сообщение об ошибке
                    String errorMessage = "Ваше устройство не поддерживает съемку";
                    Toast.makeText(User_Profile_Settings_Activity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }

                // закрываем "диалоговое окно для добавления изображения"
                dialog.dismiss();
            }
        });

        // меняем стандартные настройки Dialog(WRAP_CONTENT,WRAP_CONTENT)
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    //
    private void moveToUserProfileActivity() {

        // осуществляем переход на ленту публикаций с передачей данных
        Intent intentBack = new Intent();

            intentBack.putExtra("userName",         userNameET.getText().toString());
            intentBack.putExtra("bgPath",           bg_path);
            intentBack.putExtra("avatarPath",       avatar_path);
            intentBack.putExtra("avatarRotateOn",   avatarRotateOn);
            intentBack.putExtra("aboutMe",          aboutMeET.getText().toString());
            intentBack.putExtra("site",             siteET.getText().toString());

        // Log.d("myLogs", "avatarRotateOn= " + avatarRotateOn);

        setResult(RESULT_OK, intentBack);

        // "уничтожаем" данное активити
        finish();
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
    private void setBackground(String bg_path) {

        // если путь получен
        if ((bg_path != null) && (!bg_path.equals(""))) {

            Bitmap bgBitmap = decodeSampledBitmapFromResource(bg_path, 200, 200);

            BitmapDrawable bgDrawable = new BitmapDrawable(bgBitmap);

            // кладем фон в представление
            topBgLL.setBackground(bgDrawable);
        }
    }

    //
    private void setUserAvatar(String avatar_path) {

        Bitmap bitmap = decodeSampledBitmapFromResource(avatar_path, 200, 200);

        // showImageCropDialog(bitmap);

        // если путь получен
        if ((avatar_path != null) && (!avatar_path.equals("")))
            // кладем аватар пользователя в представление
            userAvatarIV.setImageBitmap(bitmap);
    }

    //
    public Bitmap decodeSampledBitmapFromResource(String resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 2;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }

        return inSampleSize;
    }

    //
    private void showImageCropDialog(Bitmap selectedBitmap) {

        // создаем "диалоговое окно для выбора действия над изображением"
        final Dialog dialog = new Dialog(User_Profile_Settings_Activity.this, R.style.InfoDialog_Theme);
        dialog.setContentView(R.layout.dialog_crop_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final CropImageView cropImageView = (CropImageView) dialog.findViewById(R.id.ImageCropDialog_CropIV);
        // cropImageView.setBackgroundColor(0xFFFFFFFB);
        cropImageView.setOverlayColor(0xAA1C1C1C);
        cropImageView.setFrameColor(getResources().getColor(R.color.orange));

        cropImageView.setHandleColor(getResources().getColor(R.color.orange));
        // cropImageView.setHandleShowMode(CropImageView.ShowMode.SHOW_ON_TOUCH);
        cropImageView.setHandleShowMode(CropImageView.ShowMode.SHOW_ALWAYS);
        cropImageView.setHandleSizeInDp(7);
        cropImageView.setTouchPaddingInDp(16);

        cropImageView.setGuideShowMode(CropImageView.ShowMode.NOT_SHOW);
        // cropImageView.setGuideColor(getResources().getColor(R.color.orange));

        cropImageView.setCropMode(CropImageView.CropMode.CIRCLE);

        // Set image for cropping
        cropImageView.setImageBitmap(selectedBitmap);

        dialog.findViewById(R.id.ImageCropDialog_Rotate90LeftBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);

                avatarRotateOn += 90;
            }
        });

        dialog.findViewById(R.id.ImageCropDialog_Rotate180BTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_180D);

                avatarRotateOn += 180;
            }
        });

        dialog.findViewById(R.id.ImageCropDialog_Rotate90RightBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_270D);

                avatarRotateOn += 270;
            }
        });

        // создаем обработчик нажатия на "кнопку Закрыть"
        dialog.findViewById(R.id.ImageCropDialog_CloseLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // кладем аватар пользователя в представление
                userAvatarIV.setImageBitmap(cropImageView.getCroppedBitmap());

                // avatarRotateOn = cropImageView.getRotation();

                // запоминаем путь к аватару пользователя
                // avatar_path_uri = userAvatarIV.getPat

                // закрыть "диалоговое окно для выбора действия над изображением"
                dialog.dismiss();
            }
        });

        // показываем сформированное диалоговое окно
        dialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void loadTextFromPreferences() {

        // если настройки содержат идентификатор пользователя
        if(shPref.contains("user_id")) {
            // значит можно получить значение
            user_id = Integer.parseInt(shPref.getString("user_id", "0"));
        }

        // если настройки содержат имя пользователя
        if(shPref.contains("nick_name")) {
            // значит можно получить значение
            user_name = shPref.getString("nick_name", "");
        }

        // если настройки содержат путь к фону профиля
        if(shPref.contains("bg_path")) {
            // значит можно получить значение
            bg_path = shPref.getString("bg_path", "");
        }

        // если настройки содержат путь к аватару пользователя
        if(shPref.contains("avatar_path")) {
            // значит можно получить значение
            avatar_path = shPref.getString("avatar_path", "");
        }

        // если настройки содержат текст "Обо мне"
        if(shPref.contains("about_me")) {
            // значит можно получить значение
            about_me = shPref.getString("about_me", "");
        }

        // если настройки содержат адрес сайта
        if(shPref.contains("site")) {
            // значит можно получить значение
            site = shPref.getString("site", "");
        }
    }
}