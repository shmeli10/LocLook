package com.androiditgroup.loclook.answers_pkg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androiditgroup.loclook.utils_pkg.DB_Helper;
import com.androiditgroup.loclook.utils_pkg.FullScreen_Image_Activity;
import com.androiditgroup.loclook.publication_pkg.Publication_Activity;
import com.androiditgroup.loclook.utils_pkg.Publication_Location_Dialog;
import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.user_profile_pkg.User_Profile_Activity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by OS1 on 09.10.2015.
 */
public class Answers_Activity   extends     FragmentActivity
                                implements  View.OnClickListener,
                                            TextWatcher,
                                            Answer_Fragment.OnRecipientDataClickListener,
                                            Answer_Fragment.OnSelectRecipientClickListener {

    private Context             context;
    private SharedPreferences   shPref;
    private DB_Helper           dbHelper;

    private final int arrowBackWrapLLResId      = R.id.Answers_ArrowBackWrapLL;
    private final int publicationWrapLLResId    = R.id.Answers_PublicationWrapLL;
    private final int userAvatarIVResId         = R.id.Answers_UserAvatarIV;
    private final int userNameTVResId           = R.id.Answers_UserNameTV;
    private final int publicationDateTVResId    = R.id.Answers_PublicationDateTV;
    private final int badgeIVResId              = R.id.Answers_BadgeIV;
    private final int publicationTextTVResId    = R.id.Answers_PublicationTextTV;

    private final int photoContainerLLResId     = R.id.Answers_PhotoContainerLL;
    private final int quizContainerLLResId      = R.id.Answers_QuizContainerLL;

    private final int favoritesIVResId          = R.id.Answers_FavoritesIV;
    private final int favoritesWrapLLResId      = R.id.Answers_FavoritesWrapLL;
    private final int publicationInfoLLResId    = R.id.Answers_PublicationInfoWrapLL;
    private final int likedDataLLResId          = R.id.Answers_LikedDataLL;
    private final int likedTextTVResId          = R.id.Answers_LikedTextTV;
    private final int likedSumTVResId           = R.id.Answers_LikedSumTV;
    private final int likedIVResId              = R.id.Answers_LikedIV;
    private final int userAnswerTextETResId     = R.id.Answers_UserAnswerTextET;
    private final int answersContainerLLResId   = R.id.Answers_AnswersContainerLL;
    private final int answersSumValueTVResId    = R.id.Answers_AnswersSumValueTV;
    private final int scrollViewSVResId         = R.id.Answers_ScrollViewSV;
    private final int sendUserAnswerWrapLLResId = R.id.Answers_SendUserAnswerWrapLL;

    private TextView        userNameTV;
    private TextView        publicationDateTV;
    private TextView        publicationTextTV;
    private TextView        likedTextTV;
    private TextView        likedSumTV;
    private TextView        answersSumTV;

    private ImageView       badgeIV;
    private ImageView       favoritesIV;
    private ImageView       likedIV;

    private ScrollView      scrollViewSV;
    private EditText        userAnswerTextET;

    private LinearLayout    arrowBackWrapLL;
    private LinearLayout    publicationWrapLL;

    private LinearLayout    favoritesWrapLL;
    private LinearLayout    infoWrapLL;

    private LinearLayout    photoDataLL;
    private LinearLayout    quizDataLL;
    private LinearLayout    likedDataLL;
    private LinearLayout    sendUserAnswerWrapLL;
    private LinearLayout    answersContainerLL;

    private float           density;

    private boolean         isFavorite;
    private boolean         isFavoriteChanged;

    private boolean         isLiked;
    private boolean         isLikedChanged;

    private boolean         isQuizAnswerSelected;

    private boolean         deletePublication;

    private int             publicationId;
    private int             userId;
    private int             authorId;
    private int             recipientId;
    private int             itemPosition;

    private int             answersSumValue;
    private int             likedSumValue;
    private int             allAnswersSumValue;

    private int             recipientNameLength;
    private int             selectedProvocationType;

    private int             selectedAnswerPosition = -1;
    private int             selectedAnswerNewSum;

    private float           latitude;
    private float           longitude;

    private String          authorName;
    private String          regionName;
    private String          streetName;

    private ArrayList<String[]> answersDataArrList          = new ArrayList<String[]>();
    private ArrayList<Uri>      bitmapsPathList             = new ArrayList<Uri>();
    private ArrayList<Float>    bitmapsRotateDegreesList    = new ArrayList<Float>();

    private Publication_Location_Dialog publication_loc_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // задать файл-компоновщик элементов окна
        setContentView(R.layout.activity_answers);

        context = getApplicationContext();
        density = context.getResources().getDisplayMetrics().density;

        ///////////////////////////////////////////////////////////////////////////////////

        shPref = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
        loadTextFromPreferences();

        ///////////////////////////////////////////////////////////////////////////////////

        dbHelper = new DB_Helper(this);

        ///////////////////////////////////////////////////////////////////////////////////

        arrowBackWrapLL     = (LinearLayout) findViewById(arrowBackWrapLLResId);
        publicationWrapLL   = (LinearLayout) findViewById(publicationWrapLLResId);

        favoritesWrapLL     = (LinearLayout) findViewById(favoritesWrapLLResId);
        infoWrapLL          = (LinearLayout) findViewById(publicationInfoLLResId);

        photoDataLL         = (LinearLayout) findViewById(photoContainerLLResId);
        quizDataLL          = (LinearLayout) findViewById(quizContainerLLResId);
        likedDataLL         = (LinearLayout) findViewById(likedDataLLResId);
        sendUserAnswerWrapLL= (LinearLayout) findViewById(sendUserAnswerWrapLLResId);
        answersContainerLL  = (LinearLayout) findViewById(answersContainerLLResId);

        scrollViewSV        = (ScrollView) findViewById(scrollViewSVResId);

        userNameTV          = (TextView) findViewById(userNameTVResId);
        publicationDateTV   = (TextView) findViewById(publicationDateTVResId);
        publicationTextTV   = (TextView) findViewById(publicationTextTVResId);
        likedTextTV         = (TextView) findViewById(likedTextTVResId);
        likedSumTV          = (TextView) findViewById(likedSumTVResId);
        answersSumTV        = (TextView) findViewById(answersSumValueTVResId);

        badgeIV             = (ImageView) findViewById(badgeIVResId);
        favoritesIV         = (ImageView) findViewById(favoritesIVResId);
        likedIV             = (ImageView) findViewById(likedIVResId);

        userAnswerTextET    = (EditText) findViewById(userAnswerTextETResId);
        userAnswerTextET.addTextChangedListener(this);

        ///////////////////////////////////////////////////////////////////////////////////

        Intent intent = getIntent();

        authorName      = intent.getStringExtra("userName");

        publicationDateTV.setText(intent.getStringExtra("publicationDate"));
        badgeIV.setImageResource(intent.getIntExtra("badgeImg", R.drawable.badge_1));
        publicationTextTV.setText(intent.getStringExtra("publicationText"));

        isFavorite      = intent.getBooleanExtra("isFavorite", false);
        isLiked         = intent.getBooleanExtra("isLiked", false);

        publicationId   = intent.getIntExtra("publicationId", -1);
        itemPosition    = intent.getIntExtra("itemPosition", -1);
        authorId        = intent.getIntExtra("authorId", -1);

        latitude        = intent.getFloatExtra("latitude", 0);
        longitude       = intent.getFloatExtra("longitude", 0);

        regionName      = intent.getStringExtra("regionName");
        streetName      = intent.getStringExtra("streetName");

        ///////////////////////////////////////////////////////////////////////////////////

        addImagesToPublication(photoDataLL, publicationId);
        addQuizToPublication(quizDataLL,    publicationId);

        ///////////////////////////////////////////////////////////////////////////////////

        userNameTV.setText(authorName);

        answersSumValue = getRowsSum(1, publicationId);
        answersSumTV.setText(String.valueOf(answersSumValue));

        likedSumValue   = getRowsSum(2, publicationId);
        likedSumTV.setText(String.valueOf(likedSumValue));

        ///////////////////////////////////////////////////////////////////////////////////

        if(isFavorite)
            favoritesIV.setImageResource(R.drawable.star_icon_active);

        if (isLiked) {
            likedDataLL.setBackgroundResource(R.drawable.rounded_rect_with_red_stroke);
            likedTextTV.setTextColor(getResources().getColor(R.color.red_text));
            likedSumTV.setTextColor(getResources().getColor(R.color.red_text));
            likedIV.setImageResource(R.drawable.like_icon_active);
        }

        ///////////////////////////////////////////////////////////////////////////////////

        arrowBackWrapLL.setOnClickListener(this);
        publicationWrapLL.setOnClickListener(this);

        (findViewById(userAvatarIVResId)).setOnClickListener(this);
        userNameTV.setOnClickListener(this);

        sendUserAnswerWrapLL.setOnClickListener(this);

        favoritesWrapLL.setOnClickListener(this);
        infoWrapLL.setOnClickListener(this);
        likedDataLL.setOnClickListener(this);

        ///////////////////////////////////////////////////////////////////////////////////

        if(answersSumValue > 0)
            setAnswersData();
    }

    @Override
    public void onClick(View view) {

        Intent intent = null;

        switch(view.getId()) {

            // нажата "стрелка назад" на верхней панели
            case arrowBackWrapLLResId:
                                            // запускаем переход к ленте публикаций
                                            moveToTapeActivity();
                                            break;
            // нажат "карандаш" на верхней панели
            case publicationWrapLLResId:
                                            // запускаем переход к окну создания публикации
                                            // Intent intent = new Intent(this,Publication_Activity.class);
                                            intent = new Intent(this,Publication_Activity.class);
                                            // startActivity(intent);
                                            break;
            // сделан щелчок по избранному
            case favoritesWrapLLResId:
                                            // включаем/выключаем звездочку
                                            favoritesChange();
                                            break;
            // сделан щелчок по информации
            case publicationInfoLLResId:
                                            // показываем "диалоговое окно информации"
                                            showInfoDialog();
                                            break;
            // отдан голос в поддержку публикации
            case likedDataLLResId:
                                            // меняем цвет контейнера с элемнетами поддержки
                                            likedChange();
                                            break;
            // нажата кнопка добавления ответа в обсуждение публикации
            case sendUserAnswerWrapLLResId:
                                            // получаем текст ответа
                                            String userAnswerTextValue = userAnswerTextET.getText().toString();

                                            // если пользователь ничего не написал
                                            if((userAnswerTextValue.length() - recipientNameLength) == 0)
                                                // отменяем отправку ответа
                                                return;

                                            // если получатель отправляемого ответа не выбран
                                            if(recipientId == 0)
                                                // им будет автор публикации
                                                sendUserAnswer(authorId, userAnswerTextValue);
                                            // если получатель отправляемого ответа был выбран
                                            else {
                                                // указываем получателя ответа
                                                sendUserAnswer(recipientId, userAnswerTextValue);

                                                // "забываем" получателя, после отправки ответа
                                                recipientId = 0;
                                            }
                                            break;
            //
            case userAvatarIVResId:
            case userNameTVResId:
                                            // если имя автора не скрыто за словом "Анонимно"
                                            if(!authorName.equals(context.getResources().getString(R.string.publication_anonymous_text))) {
                                                // осуществляем переход к профилю автора публикации
                                                intent = new Intent(this, User_Profile_Activity.class);
                                                intent.putExtra("answers_userId", authorId);
                                                intent.putExtra("answers_userName", authorName);
                                            }

                                            break;
        }

        if(intent != null)
            startActivity(intent);
    }

    //
    public void onBackPressed() {
        // запускаем переход к ленте публикаций
        moveToTapeActivity();
    }


    @Override
    public void onRecipientDataClick(int recipientId, String recipientName) {

        Intent intent = new Intent(this, User_Profile_Activity.class);
        intent.putExtra("answers_userId",   recipientId);
        intent.putExtra("answers_userName", recipientName);
        startActivity(intent);
    }

    @Override
    public void onSelectRecipientClick(int selectedRecipientId, String selectedRecipientName) {

        // запоминаем длину имени выбранного получателя ответа
        recipientNameLength = (selectedRecipientName.length() + 2);

        // запоминаем идентификатор выбранного получателя ответа
        recipientId         = selectedRecipientId;

        // вставляем в поле ответа, имя выбранного получателя ответа
        userAnswerTextET.setText("" + selectedRecipientName + ", ");

        // автоматически показываем клавиатуру
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(userAnswerTextET, InputMethodManager.SHOW_IMPLICIT);

        // перемещаем курсор в конец вставленного имени получателя ответа
        userAnswerTextET.setSelection(recipientNameLength);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        // если был выбран получатель сообщения
        if(recipientNameLength > 0) {

            // получаем длину всего текста в поле с сообщением
            int answerLength = userAnswerTextET.getText().length();

            // если имя выбранного получателя удаляется
            if(answerLength < recipientNameLength) {

                // затереть данные о выбранном получателе сообщения
                recipientNameLength = 0;
                recipientId         = 0;
                userAnswerTextET.setText("");
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) { }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void moveToTapeActivity() {

        // осуществляем переход на ленту публикаций с передачей данных
        Intent intentBack = new Intent();

        intentBack.putExtra("itemPosition",             String.valueOf(itemPosition));
        intentBack.putExtra("isFavoriteChanged",        String.valueOf(isFavoriteChanged));
        intentBack.putExtra("isLikedChanged",           String.valueOf(isLikedChanged));
        intentBack.putExtra("answersSumValue",          String.valueOf(answersSumValue));

        intentBack.putExtra("isQuizAnswerSelected",     String.valueOf(isQuizAnswerSelected));
        intentBack.putExtra("allAnswersSumValue",       String.valueOf(allAnswersSumValue));
        intentBack.putExtra("selectedAnswerPosition",   String.valueOf(selectedAnswerPosition));
        intentBack.putExtra("selectedAnswerNewSum",     String.valueOf(selectedAnswerNewSum));
        intentBack.putExtra("deletePublication",        String.valueOf(deletePublication));
        intentBack.putExtra("publicationId",            String.valueOf(publicationId));

        setResult(RESULT_OK, intentBack);

        // "уничтожаем" данное активити
        finish();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void addImagesToPublication(LinearLayout photoContainer, int publicationId) {

        // получаем список массивов данных изображений в публикации
        final ArrayList<String[]> imagesDataList = getImagesDataList(publicationId);

        // если данные изображений получены
        if(!imagesDataList.isEmpty()) {

            // получаем кол-во добавленных изображений
            int imagesSum = imagesDataList.size();

            // проходим циклом по полученному массиву данных добавленных изображений
            for(int i=0; i<imagesSum; i++) {

                // получаем адрес изображения
                Uri uri = Uri.parse(imagesDataList.get(i)[0]);

                // кладем адрес в "список путей добавленных изображений"
                bitmapsPathList.add(uri);

                // получаем "кол-во градусов для поворота изображения"
                Float rotateDegree = Float.parseFloat(imagesDataList.get(i)[1]);
                bitmapsRotateDegreesList.add(rotateDegree);
            }

            // запускаем сборку контейнеров изображений
            setImagesContainer(photoContainer, imagesSum);

            // раскладываем представления с изображениями в "контейнеры под *-ое изображение"
            setImages(photoContainer, reDecodeFiles());
        }
    }

    //
    private ArrayList<String[]> getImagesDataList(int publicationId) {

        // создаем "список данных по изображениям в публикации"
        ArrayList<String[]> resultList = new ArrayList<String[]>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ////////////////////////////////////////////////////////////////////////////////////

        // формируем запрос к БД
        Cursor cursor = db.rawQuery("select image_path,             " +
                                    "       rotate_degree           " +
                                    "  from publication_image_data  " +
                                    " where publication_id = ? ",
                                    new String[]{"" + publicationId});

        // если данные в запросе получены
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // формируем массив данных по изображениям добавленным в публикацию
            do {
                String[] dataArr = new String[2];

                // получаем путь изображения в памями телефона
                dataArr[0] = cursor.getString(cursor.getColumnIndex("image_path"));

                // получаем кол-во градусов на которое должно быть повернуто изображение перед отображением
                dataArr[1] = "" +cursor.getFloat(cursor.getColumnIndex("rotate_degree"));

                // добавляем сформированный массив данных в "список данных по изображениям в публикации"
                resultList.add(dataArr);
            } while (cursor.moveToNext());
        }

        // закрываем курсор
        cursor.close();

        // возвращем "список данных по изображениям в публикации"
        return resultList;
    }

    //
    private void setImagesContainer(LinearLayout imagesContainer, int imagesSum) {

        LinearLayout.LayoutParams lp;

        // получаем размер экрана
        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = d.getWidth();

        // задаем размеры для "контейнер под *-ое изображение" для каждого из трех режимов
        int size_3 = ((width - 20) / 3);   // добавлено 3 изображения
        int size_2 = ((width - 20) / 2);   // добавлено 2 изображения
        int size_1 = (size_2 + size_3);    // добавлено 1 изображение

        switch(imagesSum) {

            // готовим контейнер под одно изображение
            case 1:
                    // чистим "контейнер для добавляемых изображений" от всех вложений
                    imagesContainer.removeAllViews();

                    // создаем компоновщик
                    lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size_1);

                    // создаем "контейнер под 1-ое изображение"
                    final LinearLayout imageLL_0 = new LinearLayout(context);
                    imageLL_0.setLayoutParams(lp);

                    imageLL_0.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    imageLL_0.setBackgroundColor(Color.BLACK);


                    // "запоминаем" id выбранного изображения
                    final int selectedImageId_0 = 0;

                    // задаем обработчик щелчка по "контейнеру под 1-ое изображение"
                    imageLL_0.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // выводим изображение на полный экран
                            moveToFullscreenImageActivity(bitmapsPathList.get(selectedImageId_0), bitmapsRotateDegreesList.get(selectedImageId_0));
                        }
                    });

                    // добавляем "контейнер под 1-ое изображение" в "контейнер для добавляемых изображений"
                    imagesContainer.addView(imageLL_0);
                    break;
            // готовим контейнеры под два изображения
            case 2:
                    // чистим "контейнер для добавляемых изображений" от всех вложений
                    imagesContainer.removeAllViews();

                    // в цикле создаем контейнеры под изображения
                    for(int i=0; i<2; i++) {

                        // создаем компоновщик без отступов
                        lp = new LinearLayout.LayoutParams(size_2, size_2);

                        // создаем компоновщик с отступом
                        LinearLayout.LayoutParams lp_1 = new LinearLayout.LayoutParams(size_2, size_2);
                        setMargins(lp_1, 0, 0, 5, 0);

                        // создаем "контейнер под *-ое изображение"
                        final LinearLayout imageLL_1 = new LinearLayout(context);

                        switch(i) {

                            // если это "контейнер под 1-ое изображение" из двух
                            case 0:
                                    imageLL_1.setLayoutParams(lp_1);
                                    break;
                            // если это "контейнер под 2-ое изображение" из двух
                            case 1:
                                    imageLL_1.setLayoutParams(lp);
                                    break;
                        }

                        // указываем выравнивание содержимого в "контейнере под *-ое изображение"
                        imageLL_1.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                        // указываем фон для "контейнера под *-ое изображение"
                        imageLL_1.setBackgroundColor(Color.BLACK);


                        // "запоминаем" id выбранного изображения
                        final int selectedImageId_1 = i;

                        // задаем обработчик щелчка по "контейнеру под *-ое изображение"
                        imageLL_1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // выводим изображение на полный экран
                                moveToFullscreenImageActivity(bitmapsPathList.get(selectedImageId_1), bitmapsRotateDegreesList.get(selectedImageId_1));
                            }
                        });

                        // добавляем "контейнер под *-ое изображение" в "контейнер для добавляемых изображений"
                        imagesContainer.addView(imageLL_1);
                    }
                    break;
            // готовим контейнеры под три изображения
            case 3:
                    // чистим "контейнер для добавляемых изображений" от всех вложений
                    imagesContainer.removeAllViews();

                    // в цикле создаем контейнеры под изображения
                    for(int i=0; i<3; i++) {

                        // создаем компоновщик без отступов
                        lp = new LinearLayout.LayoutParams(size_3, size_3);

                        // создаем компоновщик с отступом
                        LinearLayout.LayoutParams lp_2 = new LinearLayout.LayoutParams(size_3, size_3);
                        setMargins(lp_2, 0, 0, 3, 0);

                        // создаем "контейнер под *-ое изображение"
                        final LinearLayout imageLL_2 = new LinearLayout(context);

                        switch(i) {

                            // если это "контейнер под 1-ое изображение" из трех
                            case 0:
                                // если это "контейнер под 2-ое изображение" из трех
                            case 1:
                                imageLL_2.setLayoutParams(lp_2);
                                break;
                            // если это "контейнер под 3-е изображение" из трех
                            case 2:
                                imageLL_2.setLayoutParams(lp);
                                break;
                        }

                        // указываем выравнивание содержимого в "контейнере под *-ое изображение"
                        imageLL_2.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                        // указываем фон для "контейнера под *-ое изображение"
                        imageLL_2.setBackgroundColor(Color.BLACK);


                        // "запоминаем" id выбранного изображения
                        final int selectedImageId_2 = i;

                        // задаем обработчик щелчка по "контейнеру под *-ое изображение"
                        imageLL_2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // выводим изображение на полный экран
                                moveToFullscreenImageActivity(bitmapsPathList.get(selectedImageId_2), bitmapsRotateDegreesList.get(selectedImageId_2));
                            }
                        });

                        // добавляем "контейнер под *-ое изображение" в "контейнер для добавляемых изображений"
                        imagesContainer.addView(imageLL_2);
                    }
                    break;
        }
    }

    //
    private void moveToFullscreenImageActivity(Uri imagePath, Float rotateDegree) {

        Intent intent = new Intent(context,FullScreen_Image_Activity.class);

        intent.putExtra("imagePath",    imagePath.toString());
        intent.putExtra("rotateDegree", rotateDegree);

        startActivity(intent);

        /*
        Intent intent = new Intent(Intent.ACTION_VIEW, imagePath);
        startActivity(intent);
        */
    }

    //
    private ArrayList<Bitmap> reDecodeFiles() {

        int imagesSum = bitmapsPathList.size();

        // устанавливаем значения для режима когда добавлено одно изображение
        int reqWidth    = 200;
        int reqHeight   = 200;

        // создаем "список изображений после изменения их размера"
        ArrayList<Bitmap> tempBitmapList = new ArrayList<Bitmap>();

        // если хоть одно изображение для изменения размеров есть
        if(imagesSum > 0) {

            // подбор режима в зависимости от кол-ва добавленных изображений
            switch(imagesSum) {

                // добавлены 2 изображения
                case 2:
                    // задаем новый размер для изображений
                    reqWidth    = 150;
                    reqHeight   = 150;
                    break;
                // добавлены 3 изображения
                case 3:
                    // задаем новый размер для изображений
                    reqWidth    = 100;
                    reqHeight   = 100;
                    break;
            }

            // проходим циклом по "список путей добавляемых изображений"
            for(int i=0; i<imagesSum; i++)
                // кладем в "список изображений после изменения их размера" очередное изображение после его обработки
                tempBitmapList.add(decodeFile(i, reqWidth, reqHeight));
        }

        // возвращаем результат
        return tempBitmapList;
    }

    //
    private Bitmap decodeFile(int pos, int reqWidth, int reqHeight) {

        // получаем изображение-заглушку
        Bitmap bitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.no_photo_red)).getBitmap();

        try {
            // получаем адрес изображения
            Uri photoPath = bitmapsPathList.get(pos);

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

            // "затираем" кол-во градусов для поворота изображения
            bitmapsRotateDegreesList.set(pos, 0.0f);
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

    //
    private void setImages(LinearLayout imagesContainer, ArrayList<Bitmap> bitmapsList) {

        // проходим циклом по "списку добавленных изображений"
        for(int i=0; i<bitmapsList.size(); i++) {

            Matrix matrix = new Matrix();
            matrix.postRotate(bitmapsRotateDegreesList.get(i));

            Bitmap bitmapToRotate = bitmapsList.get(i);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapToRotate, 0, 0, bitmapToRotate.getWidth(), bitmapToRotate.getHeight(), matrix, true);

            // создаем представление для добавляемого изображения
            final ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));

            // задаем тип масштабирования изображения в представлении
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // кладем изображение в представление
            imageView.setImageBitmap(rotatedBitmap);

            // кладем представление в приготовленный для него заранее "контейнер под *-ое изображение"
            LinearLayout imageContainer = (LinearLayout) imagesContainer.getChildAt(i);
            imageContainer.addView(imageView);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    protected void addQuizToPublication(LinearLayout quizContainer, int publicationId) {

        // получаем список ответов в опросе, привязанном к заданной публикации
        final ArrayList<String[]> quizAnswersList = getQuizAnswersList(publicationId);

        // если ответы получены
        if(!quizAnswersList.isEmpty()) {

            // задаем параметры расположения
            LinearLayout.LayoutParams layoutParamsFW = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
            LinearLayout.LayoutParams layoutParamsFF = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout.LayoutParams layoutParamsWW = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams answerLP       = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
            LinearLayout.LayoutParams strutLP        = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ((int) (20 * density)), 1.0f);

            // задаем отступ сверху
            setMargins(answerLP, 0, 2, 0, 0);

            // создаем "контейнер опроса"
            final LinearLayout quizLL = new LinearLayout(context);
            quizLL.setLayoutParams(layoutParamsFW);
            quizLL.setOrientation(LinearLayout.VERTICAL);
            quizLL.setTag("quizLL");

            final String quizId = quizAnswersList.get(0)[1];

            // получаем кол-вом всех проголосовавших пользователей в данном опросе
            final int allAnswersSum = getQuizAnswersSum(quizId, false);

            // получаем булево значение, сигнализирующее отвечал ли пользователь в данном опросе
            boolean userAnsweredOnQuiz = isQuizAnsweredByUser(quizId);

            // проходим циклом по списку вопросов
            for(final String[] arr: quizAnswersList) {

                // получаем идентификатор ответа
                final int quiz_answer_id = Integer.parseInt(arr[0]);

                // получаем кол-во пользователей выбравших данный ответ
                int answerSelectedSum = getAnswersSum(quiz_answer_id);

                // создаем "контейнер с ответом"
                LinearLayout quizAnswerLL = new LinearLayout(context);
                quizAnswerLL.setLayoutParams(answerLP);
                quizAnswerLL.setOrientation(LinearLayout.HORIZONTAL);
                quizAnswerLL.setTag("quizAnswerLL");

                // если пользователь уже отвечал в данном опросе
                if(userAnsweredOnQuiz)
                    // подсвечиваем контейнер песочным цветом
                    quizAnswerLL.setBackgroundResource(R.drawable.rounded_rect_quiz_answer);
                else
                    // подсвечиваем контейнер серым цветом
                    quizAnswerLL.setBackgroundResource(R.drawable.rounded_rect_quiz_answer_grey);

                // создаем "контейнер с данными ответа" (он будет содержать "контейнер с фонами" и "контейнер с текстовыми данными ответа")
                FrameLayout quizAnswerDataLL = new FrameLayout(context);
                quizAnswerDataLL.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                quizAnswerDataLL.setTag("quizAnswerDataLL");

                // создаем "контейнер с фонами"
                LinearLayout quizAnswerDataBgLL = new LinearLayout(context);
                quizAnswerDataBgLL.setLayoutParams(layoutParamsFF);
                quizAnswerDataBgLL.setOrientation(LinearLayout.HORIZONTAL);
                quizAnswerDataBgLL.setTag("quizAnswerDataBgLL");

                // получаем ширину для "правого контейнера закрашивающего фон ответа"
                float answerRightBGPercents = 0.0f;

                // если пользователь уже отвечал в данном опросе
                if(userAnsweredOnQuiz)
                    answerRightBGPercents = getAnswerRightBGPercents(allAnswersSum, answerSelectedSum);

                // получаем ширину для "левого контейнера закрашивающего фон ответа"
                float answerLeftBGPercents = ((float) 1 - answerRightBGPercents);

                // получаем "левый контейнер закрашивающий фон ответа"
                LinearLayout quizAnswerLeftBgLL = new LinearLayout(context);
                quizAnswerLeftBgLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, answerLeftBGPercents));
                quizAnswerLeftBgLL.setOrientation(LinearLayout.HORIZONTAL);
                quizAnswerLeftBgLL.setBackgroundResource(R.drawable.rounded_rect_quiz_answer_selected);
                quizAnswerLeftBgLL.setTag("quizAnswerLeftBgLL");

                // если пользователь еще не отвечал в данном опросе
                if(!userAnsweredOnQuiz)
                    // указываем "левому контейнеру закрашивающему фон ответа" стать невидимым
                    quizAnswerLeftBgLL.setVisibility(View.INVISIBLE);

                // получаем "правый контейнер закрашивающий фон ответа"
                LinearLayout quizAnswerRightBgLL = new LinearLayout(context);
                quizAnswerRightBgLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, answerRightBGPercents));
                quizAnswerRightBgLL.setOrientation(LinearLayout.HORIZONTAL);
                quizAnswerRightBgLL.setBackgroundResource(R.drawable.rounded_rect_quiz_answer);
                quizAnswerRightBgLL.setTag("quizAnswerRightBgLL");

                // если пользователь уже отвечал в данном опросе
                if(!userAnsweredOnQuiz)
                    // указываем "правому контейнеру закрашивающему фон ответа" стать невидимым
                    quizAnswerRightBgLL.setVisibility(View.INVISIBLE);

                // добавляем левый и правый контейнеры в "контейнер с фонами"
                quizAnswerDataBgLL.addView(quizAnswerLeftBgLL);
                quizAnswerDataBgLL.addView(quizAnswerRightBgLL);

                // получаем "контейнер с текстовыми данными ответа"
                final LinearLayout quizAnswerDataTextLL = new LinearLayout(context);
                quizAnswerDataTextLL.setLayoutParams(layoutParamsFF);
                quizAnswerDataTextLL.setOrientation(LinearLayout.HORIZONTAL);
                quizAnswerDataTextLL.setGravity(Gravity.CENTER_VERTICAL);
                setPaddings(quizAnswerDataTextLL, 10, 10, 10, 10);
                quizAnswerDataTextLL.setTag("quizAnswerDataTextLL");

                // создаем обработчик щелчка по "контейнеру с текстовыми данными ответа"
                quizAnswerDataTextLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // отправляем в БД выбранный пользователем ответ
                        if (sendUserQuizAnswer(quiz_answer_id)) {

                            // получаем "поле с кол-вом пользователей выбравших данный ответ в опросе"
                            TextView answersSumTV = (TextView) quizAnswerDataTextLL.findViewWithTag("answersSumTV");

                            // получаем кол-вом всех проголосовавших пользователей в данном опросе
                            // и увеличиваем его на только что проголосовавшего
                            int newAllAnswersSum = (allAnswersSum + 1);

                            // получаем позицию ответа в списке ответов
                            int selectedAnswerPos = quizAnswersList.indexOf(arr);

                            // получаем числовое значение содержащееся в "поле с кол-вом пользователей выбравших данный ответ в опросе"
                            // и увеличиваем его на только что проголосовавшего
                            int newAnswersSum = (Integer.parseInt(answersSumTV.getText().toString()) + 1);

                            // запускаем обновление опроса: фонов под ответами и общего кол-ва проголосовавших в опросе
                            resetQuizAnswersBG(quizLL, newAllAnswersSum, selectedAnswerPos, newAnswersSum);

                            isQuizAnswerSelected    = true;
                            allAnswersSumValue      = newAllAnswersSum;
                            selectedAnswerPosition  = selectedAnswerPos;
                            selectedAnswerNewSum    = newAnswersSum;
                        }
                    }
                });

                // FrameLayout.LayoutParams answerTextLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);

                // создаем "поле с текстом ответа"
                TextView answerTextTV = new TextView(context);
                // answerTextTV.setLayoutParams(answerTextLP);

                // если пользователь уже отвечал в опросе
                if(userAnsweredOnQuiz)
                    // указываем "полю с текстом ответа" что выводить текст надо коричневым цветом
                    answerTextTV.setTextColor(context.getResources().getColor(R.color.quiz_answer_text));
                else
                    // указываем "полю с текстом ответа" что выводить текст надо синим цветом
                    answerTextTV.setTextColor(context.getResources().getColor(R.color.user_name_blue));

                // кладем текст ответа в "поле с текстом ответа"
                answerTextTV.setText(arr[2]);

                // получаем размер экрана
                Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                // ограничиваем ответ опроса по ширине, чтоб было видно кол-во пользователей выбравших данный ответ
                answerTextTV.setLayoutParams(new FrameLayout.LayoutParams((d.getWidth() - 150), FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT));

                answerTextTV.setTag("answerTextTV");
                answerTextTV.setGravity(Gravity.LEFT);

                // создаем горизонтальную распорку для полей в строке ответа
                View horizontalStrut = new View(context);
                horizontalStrut.setLayoutParams(strutLP);
                horizontalStrut.setTag("horizontalStrut");

                // создаем "поле с кол-вом пользователей выбравших данный ответ в опросе"
                TextView answersSumTV = new TextView(context);
                answersSumTV.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
                answersSumTV.setGravity(Gravity.RIGHT);
                answersSumTV.setTextColor(Color.BLACK);
                answersSumTV.setTypeface(Typeface.DEFAULT_BOLD);
                answersSumTV.setText("" + answerSelectedSum);
                answersSumTV.setTag("answersSumTV");

                // если пользователь еще не отвечал в данном опросе
                if(!userAnsweredOnQuiz)
                    // указываем "полю с кол-вом пользователей выбравших данный ответ в опросе" стать невидимым
                    answersSumTV.setVisibility(View.INVISIBLE);

                // кладем созданные элементы в "контейнер с текстовыми данными ответа"
                quizAnswerDataTextLL.addView(answerTextTV);
                quizAnswerDataTextLL.addView(horizontalStrut);
                quizAnswerDataTextLL.addView(answersSumTV);

                // кладем "контейнер с фонами" и "контейнер с текстовыми данными ответа" в "контейнер с данными ответа"
                quizAnswerDataLL.addView(quizAnswerDataBgLL);
                quizAnswerDataLL.addView(quizAnswerDataTextLL);

                // кладем "контейнер с данными ответа" в "контейнер с ответом"
                quizAnswerLL.addView(quizAnswerDataLL);

                // добавляем очередной "контейнер с ответом" в "контейнер опроса"
                quizLL.addView(quizAnswerLL);
            }

            // создаем "контейнер с кол-вом всех проголосовавших в данном опросе"
            LinearLayout quizAllAnswersSumLL = new LinearLayout(context);
            quizAllAnswersSumLL.setLayoutParams(layoutParamsFW);
            quizAllAnswersSumLL.setOrientation(LinearLayout.HORIZONTAL);
            setPaddings(quizAllAnswersSumLL, 0, 10, 10, 0);
            quizAllAnswersSumLL.setTag("quizAllAnswersSumLL");

            // создаем горизонтальную распорку чтобы прижать поля к правому краю "контейнера с кол-вом всех проголосовавших в данном опросе"
            View leftStrut = new View(context);
            leftStrut.setLayoutParams(strutLP);
            leftStrut.setTag("leftStrut");

            // создаем "текстовое поле с текстом "Всего проголосовало:""
            TextView quizAllAnswersSumTextTV = new TextView(context);
            quizAllAnswersSumTextTV.setLayoutParams(layoutParamsWW);
            quizAllAnswersSumTextTV.setTextColor(context.getResources().getColor(R.color.dark_grey));
            quizAllAnswersSumTextTV.setText(context.getResources().getString(R.string.voted_users_text));
            quizAllAnswersSumTextTV.setTag("quizAllAnswersSumTextTV");

            // создаем "текстовое поле с кол-вом всех проголосовавших в данном опросе"
            TextView quizAllAnswersSumNumberTV = new TextView(context);
            quizAllAnswersSumNumberTV.setLayoutParams(layoutParamsWW);
            quizAllAnswersSumNumberTV.setTextColor(context.getResources().getColor(R.color.dark_grey));
            quizAllAnswersSumNumberTV.setText("" + allAnswersSum);
            quizAllAnswersSumNumberTV.setTypeface(Typeface.DEFAULT_BOLD);
            quizAllAnswersSumNumberTV.setTag("quizAllAnswersSumNumberTV");
            setPaddings(quizAllAnswersSumNumberTV, 5, 0, 0, 0);

            // кладем элементы в "контейнер с кол-вом всех проголосовавших в данном опросе"
            quizAllAnswersSumLL.addView(leftStrut);
            quizAllAnswersSumLL.addView(quizAllAnswersSumTextTV);
            quizAllAnswersSumLL.addView(quizAllAnswersSumNumberTV);

            // добавляем "контейнер с кол-вом всех проголосовавших в данном опросе" в "контейнер опроса"
            quizLL.addView(quizAllAnswersSumLL);

            // если пользователь уже отвечал в данном опросе
            if(userAnsweredOnQuiz)
                // блокируем для него данный опрос
                lockQuiz(quizLL);

            // добавляем "контейнер опроса" в контейнер заданный в файле компоновщике, для отображения его в ленте
            quizContainer.addView(quizLL);
        }
    }

    //
    private ArrayList<String[]> getQuizAnswersList(int publicationId) {

        // создаем "список данных по ответам опроса заданной публикации"
        ArrayList<String[]> resultList = new ArrayList<String[]>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ////////////////////////////////////////////////////////////////////////////////////

        // формируем запрос к БД
        Cursor cursor1 = db.rawQuery("select id" +
                                     "  from quiz_data" +
                                     " where publication_id = ? ",
                                     new String[]{"" +publicationId});

        // если данные в запросе получены
        if(cursor1.getCount() > 0) {
            cursor1.moveToFirst();

            // формируем запрос к БД
            Cursor cursor2 = db.rawQuery("select *" +
                                         "  from quiz_answer_data " +
                                         " where quiz_id = ? ",
                                         new String[]{cursor1.getString(cursor1.getColumnIndex("id"))});

            // если данные в запросе получены
            if(cursor2.getCount() > 0) {
                cursor2.moveToFirst();

                // формируем массив данных по ответам в заданном опросе
                do {

                    String[] dataArr = new String[3];

                    // получеем идентификатор ответа
                    dataArr[0] = cursor2.getString(cursor2.getColumnIndex("id"));

                    // получаем идентификатор опроса к которому относится данный ответ
                    dataArr[1] = cursor2.getString(cursor2.getColumnIndex("quiz_id"));

                    // получаем текст ответа
                    dataArr[2] = cursor2.getString(cursor2.getColumnIndex("answer"));

                    // кладем сформированный массив данных в "список данных по ответам опроса заданной публикации"
                    resultList.add(dataArr);

                } while (cursor2.moveToNext());
            }

            // закрываем 2-й курсор
            cursor2.close();
        }

        // закрываем 1-й курсор
        cursor1.close();

        // возвращем результат
        return resultList;
    }

    //
    private int getQuizAnswersSum(String quiz_id, boolean forCurrentUser) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // создаем "список для идентификаторов ответов в заданном опросе"
        ArrayList<String> quizAnswersIdList = new ArrayList<String>();

        // переменная для результата
        // * - если forCurrentUser = true   -> получаем как результат отвечал ли пользователь в данном опросе
        // * - если forCurrentUser = false  -> получаем как результат общую сумму ответов всех пользователей в данном опросе
        int result = 0;

        ////////////////////////////////////////////////////////////////////////////////////

        // формируем запрос к БД
        Cursor cursor1 = db.rawQuery("select id" +
                                     "  from quiz_answer_data" +
                                     " where quiz_id = ? ",
                                     new String[]{quiz_id});

        // если данные в запросе получены
        if(cursor1.getCount() > 0) {
            cursor1.moveToFirst();

            // формируем массив идентификаторов ответов в заданном опросе
            do {
                quizAnswersIdList.add(cursor1.getString(cursor1.getColumnIndex("id")));
            } while (cursor1.moveToNext());
        }

        // закрываем 1-й курсор
        cursor1.close();

        ////////////////////////////////////////////////////////////////////////////////////

        // формируем запрос к БД
        StringBuilder sqlQuery = new StringBuilder("select count(*) as RowsSum");
        sqlQuery.append(" from user_quiz_answer_data");
        sqlQuery.append(" where quiz_answer_id IN (? ");

        // накапливаем символы подстановки для каждой скрываемой публикации
        for(int i=1; i<quizAnswersIdList.size(); i++)
            sqlQuery.append(",?");

        if(forCurrentUser) {
            // завершаем формирование запроса
            sqlQuery.append(" ) AND user_id = ?");

            // дополняем массив данных для запроса идентификатором пользователя
            quizAnswersIdList.add("" + userId);
        }
        else
            // завершаем формирование запроса
            sqlQuery.append(" )");

        // получаем данные в результате запроса к БД
        Cursor cursor2 = db.rawQuery(sqlQuery.toString(), quizAnswersIdList.toArray(new String[quizAnswersIdList.size()]));

        // если данные получены
        if(cursor2.getCount() > 0) {
            cursor2.moveToFirst();

            // получаем кол-во ответов
            result = Integer.parseInt(cursor2.getString(cursor2.getColumnIndex("RowsSum")));
        }

        // закрываем 2-й курсор
        cursor2.close();

        // закрываем подключение к БД
        dbHelper.close();

        // возвращаем результат
        return result;
    }

    //
    private int getAnswersSum(int quiz_answer_id) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // задаем имя таблицы
        String table_name = "user_quiz_answer_data";

        // создаем переменную в которой будем хранить результат
        int rowsSum = 0;

        // получим сумму записей удовлетворяющих критериям запроса
        String[] columns        = new String[]{"count(*) as RowsSum"};

        // создаем строку-условие
        String selection        = "quiz_answer_id = ?";

        // создаем массив-значений, для строки-условия
        String[] selectionArgs  = new String[]{"" +quiz_answer_id};

        // пытаемся получить значения на основании заданных условий
        Cursor cursor = db.query(table_name, columns, selection, selectionArgs, null, null, null);

        // если данные были получены
        if(cursor.getCount() > 0) {
            // перемещаемся к первой строке данных
            cursor.moveToFirst();

            // получаем идентификатор искомой записи
            rowsSum = cursor.getInt(cursor.getColumnIndex("RowsSum"));
        }

        // закрываем курсор
        cursor.close();

        // закрываем подключение к БД
        dbHelper.close();

        // возвращаем результат
        return rowsSum;

    }

    //
    private float getAnswerRightBGPercents(int allAnswersSum,int selectedAnswerSum) {

        float result = 0.0f;

        // если проголосовал хотя бы один человек
        if(allAnswersSum > 0)
            // вычисляем ширину для "правого контейнера закрашивающего фон ответа"
            result = ((((float) 100/allAnswersSum) * selectedAnswerSum) / 100);

        return result;
    }

    //
    private boolean sendUserQuizAnswer(int quiz_answer_id) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // задаем имя таблицы
        String table_name = "user_quiz_answer_data";

        //////////////////////////////////////////////////////////

        int userAnswerId = dbHelper.addRow(db, table_name, new String[]{"quiz_answer_id", "user_id"}, new String[]{"" + quiz_answer_id, "" + userId});

        if(userAnswerId > 0)
            return true;
        else
            return false;
    }

    //
    private void resetQuizAnswersBG(LinearLayout quizLL, int allAnswersSum, int selectedAnswerPosition, int selectedAnswerNewSum) {

        // получаем общее кол-во контейнеров вложенных в контейнер опроса
        int quizRowsSum = quizLL.getChildCount();

        // проходим циклом по опросу
        for(int i=0; i<quizRowsSum; i++) {

            // если мы еще не вышли за пределы контейнеров с ответами
            if(i != (quizRowsSum-1)) {

                // получаем "контейнер с ответом"
                LinearLayout quizAnswerLL = (LinearLayout) quizLL.getChildAt(i);
                // подсвечиваем контейнер песочным цветом
                quizAnswerLL.setBackgroundResource(R.drawable.rounded_rect_quiz_answer);

                // получаем "контейнер с данными ответа" (он содержит "контейнер с фонами" и "контейнер с текстовыми данными ответа")
                FrameLayout quizAnswerDataLL = (FrameLayout) quizAnswerLL.getChildAt(0);

                // получаем "контейнер с текстовыми данными ответа"
                LinearLayout quizAnswerDataTextLL = (LinearLayout) quizAnswerDataLL.getChildAt(1);
                // блокируем кликабельность "контейнера с текстовыми данными ответа"
                quizAnswerDataTextLL.setClickable(false);

                // получаем "поле с кол-вом пользователей выбравших данный ответ в опросе"
                TextView answersSumTV = (TextView) quizAnswerDataTextLL.findViewWithTag("answersSumTV");
                // указываем "полю с кол-вом пользователей выбравших данный ответ в опросе" стать видимым
                answersSumTV.setVisibility(View.VISIBLE);

                // получаем "поле с текстом ответа"
                TextView answerTextTV = (TextView) quizAnswerDataTextLL.findViewWithTag("answerTextTV");
                // указываем "полю с текстом ответа" что выводить текст надо коричневым цветом
                answerTextTV.setTextColor(context.getResources().getColor(R.color.quiz_answer_text));

                // будем хранить кол-во пользователей выбравших данный ответ в опросе
                int selectedAnswerSum = 0;

                // если это тот ответ, который пользователь только что выбрал в опросе
                if((selectedAnswerPosition >= 0) && (i == selectedAnswerPosition)) {
                    // обновляем значение в "поле с кол-вом пользователей выбравших данный ответ в опросе"
                    answersSumTV.setText("" +selectedAnswerNewSum);

                    // запоминаем новое значение "поля с кол-вом пользователей выбравших данный ответ в опросе"
                    selectedAnswerSum = selectedAnswerNewSum;
                }
                // если это НЕ тот ответ, который пользователь только что выбрал в опросе
                else {
                    // получаем числовое значение содержащееся в "поле с кол-вом пользователей выбравших данный ответ в опросе"
                    selectedAnswerSum = Integer.parseInt(answersSumTV.getText().toString());

                    // перезаписываем прежнее значение в "поле с кол-вом пользователей выбравших данный ответ в опросе"
                    answersSumTV.setText("" +selectedAnswerSum);
                }

                // получаем ширину для "правого контейнера закрашивающего фон ответа"
                float answerRightBGPercents = getAnswerRightBGPercents(allAnswersSum, selectedAnswerSum);

                // получаем ширину для "левого контейнера закрашивающего фон ответа"
                float answerLeftBGPercents = ((float) 1 - answerRightBGPercents);

                // получаем "контейнер с фонами"
                LinearLayout quizAnswerDataBgLL = (LinearLayout) quizAnswerDataLL.getChildAt(0);

                // получаем "левый контейнер закрашивающий фон ответа"
                LinearLayout quizAnswerLeftBgLL = (LinearLayout) quizAnswerDataBgLL.findViewWithTag("quizAnswerLeftBgLL");
                // указываем "левому контейнеру закрашивающему фон ответа" стать видимым
                quizAnswerLeftBgLL.setVisibility(View.VISIBLE);

                // получаем параметры распложения "левого контейнера закрашивающего фон ответа"
                LinearLayout.LayoutParams answerLeftBgLP = (LinearLayout.LayoutParams) quizAnswerLeftBgLL.getLayoutParams();
                // задаем "левому контейнеру закрашивающему фон ответа" новое значение ширины
                answerLeftBgLP.weight = answerLeftBGPercents;

                // получаем "правый контейнер закрашивающий фон ответа"
                LinearLayout quizAnswerRightBgLL = (LinearLayout) quizAnswerDataBgLL.findViewWithTag("quizAnswerRightBgLL");
                // указываем "правому контейнеру закрашивающему фон ответа" стать видимым
                quizAnswerRightBgLL.setVisibility(View.VISIBLE);

                // получаем параметры распложения "правого контейнера закрашивающего фон ответа"
                LinearLayout.LayoutParams answerRightBgLP = (LinearLayout.LayoutParams) quizAnswerRightBgLL.getLayoutParams();
                // задаем "правому контейнеру закрашивающему фон ответа" новое значение ширины
                answerRightBgLP.weight = answerRightBGPercents;
            }
            // если мы вышли за пределы контейнеров с ответами
            else {

                // получаем "контейнер с кол-вом всех проголосовавших пользователей в данном опросе"
                LinearLayout quizAllAnswersSumLL = (LinearLayout) quizLL.getChildAt(i);

                // получаем "текстовое поле с кол-вом всех проголосовавших пользователей в данном опросе"
                TextView quizAllAnswersSumNumberTV = (TextView) quizAllAnswersSumLL.getChildAt(2);
                // кладем новое значение в "текстовое поле с кол-вом всех проголосовавших пользователей в данном опросе"
                quizAllAnswersSumNumberTV.setText("" +allAnswersSum);
            }
        }
    }

    //
    private boolean isQuizAnsweredByUser(String quiz_id) {

        // если получено значение больше 0
        if(getQuizAnswersSum(quiz_id, true) > 0 )
            // значит пользователь отвечал уже в данном опросе
            return true;
        // если получено значение <= 0
        else
            // значит пользователь НЕ отвечал в данном опросе
            return false;
    }

    //
    private void lockQuiz(LinearLayout quizLL) {
        // проходим циклом по контейнеру с опросом
        for(int i=0; i<(quizLL.getChildCount()-1); i++) {

            // получаем контейнер очередного ответа
            LinearLayout quizAnswerLL = (LinearLayout) quizLL.getChildAt(i);

            // получаем контейнер содержащий два контейнера: один с фонами, другой с текстовыми данными
            FrameLayout quizAnswerDataLL = (FrameLayout) quizAnswerLL.getChildAt(0);

            // получаем контейнер с текстовыми данными ответа
            LinearLayout quizAnswerDataTextLL = (LinearLayout) quizAnswerDataLL.getChildAt(1);

            // блокируем его кликабельность
            quizAnswerDataTextLL.setClickable(false);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    public void favoritesChange() {

        // если надо снять выделение
        if(isFavorite) {
            // задаем изображение как неактивная звезда
            favoritesIV.setImageResource(R.drawable.star_icon);

            // сигнализируем, что звезда в неактивном состоянии
            isFavorite = false;
        }
        // если надо установить выделение
        else {
            // задаем изображение как активная звезда
            favoritesIV.setImageResource(R.drawable.star_icon_active);

            // сигнализируем, что звезда в активном состоянии
            isFavorite = true;
        }

        // меняем значение сигнализатора о том что публикация добавлена/исключена из избранного, на противоположное
        isFavoriteChanged = (!isFavoriteChanged);
    }

    //
    private void showInfoDialog() {

        // создаем "диалоговое окно информации"
        final Dialog dialog = new Dialog(Answers_Activity.this, R.style.InfoDialog_Theme);
        dialog.setContentView(R.layout.dialog_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // создаем обработчик нажатия в окне кнопки "Где это?"
        dialog.findViewById(R.id.InfoDialog_WhereIsItLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showPublicationLocationDialog();
            }
        });

        // создаем обработчик нажатия в окне кнопки "Поделиться"
        dialog.findViewById(R.id.InfoDialog_ShareLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                shareTo(publicationTextTV.getText().toString());
            }
        });

        // находим контейнер и кладем в него нужную кнопку, с обработчиком клика по ней
        ((LinearLayout) dialog.findViewById(R.id.InfoDialog_OwnButtonLL)).addView(getOwnButtonLL(dialog, (authorId == userId), publicationId));

        // создаем обработчик нажатия в окне кнопки "Закрыть"
        dialog.findViewById(R.id.InfoDialog_CloseLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // показываем сформированное "диалоговое окно информации"
        dialog.show();
    }

    //
    private void showPublicationLocationDialog() {

        try {
            // если диалоговое окно уже существует
            if(publication_loc_dialog != null) {
                // передаем в него данные для верного отображения адреса публикации
                publication_loc_dialog.setLocation(latitude, longitude);
                publication_loc_dialog.setRegionName(regionName);
                publication_loc_dialog.setStreetName(streetName);
                publication_loc_dialog.resetLocation();

                // показываем "диалоговое окно отображения места создания публикации на карте города"
                publication_loc_dialog.getDialog().show();
            }
            // если диалоговое окно не существует
            else {
                // создаем окно и передаем в него данные для верного отображения адреса публикации
                publication_loc_dialog = new Publication_Location_Dialog();
                publication_loc_dialog.setLocation(latitude, longitude);
                publication_loc_dialog.setRegionName(regionName);
                publication_loc_dialog.setStreetName(streetName);

                // показываем сформированное "диалоговое окно отображения места создания публикации на карте города"
                publication_loc_dialog.show(getFragmentManager(), "pub_loc_dialog");
            }
        }
        catch(Exception exc) {
            Log.d("myLogs", "Answers_Activity: showPublicationLocationDialogError! " + exc.getStackTrace());
        }
    }

    private void showDeleteDialog(final int publicationId) {

        // создаем "диалоговое окно удаленя публикации"
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(Answers_Activity.this);

        deleteDialog.setTitle(context.getResources().getString(R.string.deleting_publication_text));          // заголовок
        deleteDialog.setMessage(context.getResources().getString(R.string.delete_publication_answer_text)); // сообщение

        deleteDialog.setPositiveButton(context.getResources().getString(R.string.yes_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                // удаляем публикацию из БД
                deletePublication(publicationId);

                // сигнализируем о том, что данную публикацию надо убрать из ленты
                deletePublication = true;
            }
        });

        deleteDialog.setNegativeButton(context.getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) { }
        });

        deleteDialog.setCancelable(true);

        deleteDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) { }
        });

        deleteDialog.show();
    }

    //
    private void showClaimDialog(final int publication_id) {

        selectedProvocationType = 0;

        // создаем "диалоговое окно отправки жалобы"
        final Dialog dialog = new Dialog(Answers_Activity.this, R.style.InfoDialog_Theme);
        dialog.setContentView(R.layout.dialog_claim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // получаем идентификаторы цветов, для раскраски фонов и текста в окне
        final int whiteColor    = context.getResources().getColor(R.color.white);
        final int orangeColor   = context.getResources().getColor(R.color.selected_item_orange);
        final int blueColor     = context.getResources().getColor(R.color.user_name_blue);

        // создаем "чекбокс необходимости скрыть публикацию из ленты жалующегося пользователя"
        final CheckBox chBox    = (CheckBox) dialog.findViewById(R.id.ClaimDialog_HidePublicationChBox);

        // создаем "адаптер формирования списка с типами жалоб"
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.row_provocation_type, getProvocationTypesArray());

        // создаем "список из типов жалоб"
        ListView listView = (ListView) dialog.findViewById(R.id.ClaimDialog_ProvocationTypeLV);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // создаем обработчик щелчка по одному из пунктов "списка из типов жалоб"
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // будем хранить ссылку на представление - пункт "списка из типов жалоб"
                TextView textView;

                // проходим циклом по всем пунктам "списка из типов жалоб"
                for (int i = 0; i < parent.getChildCount(); i++) {
                    // приводим очередной пункт списка к оформлению по-умолчанию
                    textView = (TextView) parent.getChildAt(i);
                    textView.setTextColor(blueColor);
                    textView.setBackgroundColor(Color.TRANSPARENT);
                }

                // помечаем представление, по которому был сделан щелчок, как выбранное
                view.setSelected(true);

                // приводим очередной пункт списка к оформлению выбранного пункта меню
                textView = (TextView) view;
                textView.setTextColor(whiteColor);
                textView.setBackgroundColor(orangeColor);

                // привеодим идентификатор к тому что в БД
                setSelectedPosition(position + 2);
            }
        });

        // создаем обработчик нажатия в окне кнопки "Отмена"
        dialog.findViewById(R.id.ClaimDialog_CancelTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // создаем обработчик нажатия в окне кнопки "Отправить"
        dialog.findViewById(R.id.ClaimDialog_SendTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // sendClaim(chBox.isChecked());
                sendClaim(publication_id, chBox.isChecked());

                // если необходимо скрыть публикацию из ленты жалующегося пользователя
                if(chBox.isChecked())
                    // сигнализируем об этом
                    deletePublication = true;

                // закрываем "диалоговое окно отправки жалобы"
                dialog.dismiss();
            }
        });

        // показываем сформированное "диалоговое окно отправки жалобы"
        dialog.show();
    }

    //
    private void deletePublication(int publicationId) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String table_name = "publication_data";

        //////////////////////////////////////////////////////////

        // удаляем заданную запись из таблицы
        db.delete(table_name, "id = " + publicationId, null);

        // отображаем содержимое таблицы
        // dbHelper.showAllTableData(db, table_name);

        // закрываем подключение к БД
        dbHelper.close();
    }

    //
    private String[] getProvocationTypesArray() {

        // создаем "список типов жалоб"
        ArrayList<String> dataList = new ArrayList<String>();

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // готовим запрос для получения всех данных из "таблицы типы жалоб"
        String sqlQuery = "select * from provocation_type_data";

        ///////////////////////////////////////////////////////////////////////////////////////

        // получаем данные из "таблицы типы жалоб"
        Cursor cursor = db.rawQuery(sqlQuery.toString(), new String[]{});

        // если данные получены
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // определяем номера столбцов в выборке по их именам
            int provocationTypeIdColIndex   = cursor.getColumnIndex("id");
            int provocationTypeNameColIndex = cursor.getColumnIndex("type_name");

            do {
                // если это не тип жалобы "Спам"
                if(cursor.getInt(provocationTypeIdColIndex) != 1)
                    // добавляем его в "список типов жалоб"
                    dataList.add(cursor.getString(provocationTypeNameColIndex));

            } while (cursor.moveToNext());
        }

        // закрываем курсор
        cursor.close();

        // закрываем подключение к БД
        dbHelper.close();

        // возвращаем результат, приводя список к строковому массиву
        return dataList.toArray(new String[dataList.size()]);
    }

    //
    public void setSelectedPosition(int selectedItemPosition) {
        // запоминаем идентификатор позиции выбранного типа жалобы
        this.selectedProvocationType = selectedItemPosition;
    }

    //
    private void sendClaim(int publication_id, boolean hidePublication){

        DB_Helper dbHelper = new DB_Helper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////////////

        // если пользователь не выбрал ни один из пунктов "списка типов жалоб"
        if(selectedProvocationType == 0)
            // считаем что пользователь пожаловался на "Спам"
            selectedProvocationType++;

        // формируем параметры добавления жалобы в таблицу "типы жалоб"
        String table_name   = "claim_data";

        String[] columnsArr =   {   "publication_id",
                                    "provocation_type_id",
                                    "claim_user_id",
                                    "claim_date",
                                    "hide_publication"
                                };

        String[] data       =   {   "" +publication_id,
                                    "" +selectedProvocationType,
                                    "" +userId,
                                    "" +System.currentTimeMillis(),
                                    "" +hidePublication
                                 };

        // добавляем запись в БД
        dbHelper.fillTable(db, table_name, columnsArr, data);

        // закрываем подключение к БД
        dbHelper.close();
    }

    //
    private void shareTo(String publicationText) {
        // обращаемся к системе с запросом на отправку текстового сообщения
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, publicationText);
        sendIntent.setType("text/plain");

        // в итоге получим окно выбора приложений, с помощью которых система может осуществить данную отправку
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.choose_action)));

        /*
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));


        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        imageUris.add(imageUri1); // Add your image URIs here
        imageUris.add(imageUri2);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share images to.."));
        */
    }

    //
    public void likedChange() {

        // если надо снять выделение
        if(isLiked) {
            // задаем неактивность "контейнеру поддержки публикации и всем его элементам" в виде синего цвета
            likedDataLL.setBackgroundResource(R.drawable.rounded_rect_with_blue_stroke);
            likedTextTV.setTextColor(getResources().getColor(R.color.user_name_blue));
            likedSumTV.setTextColor(getResources().getColor(R.color.user_name_blue));

            // уменьшаем кол-во пользователей поддержавших данную публикацию на 1
            likedSumTV.setText("" +(--likedSumValue));

            // задаем изображение неактивного сердца
            likedIV.setImageResource(R.drawable.like_icon);

            // сигнализируем что поддержка публикации отменена
            isLiked = false;
        }
        // если надо установить выделение
        else {
            // задаем активность "контейнеру поддержки публикации и всем его элементам" в виде красного цвета
            likedDataLL.setBackgroundResource(R.drawable.rounded_rect_with_red_stroke);
            likedTextTV.setTextColor(getResources().getColor(R.color.red_text));
            likedSumTV.setTextColor(getResources().getColor(R.color.red_text));

            // увеличиваем кол-во пользователей поддержавших данную публикацию на 1
            likedSumTV.setText("" + (++likedSumValue));

            // задаем изображение активного сердца
            likedIV.setImageResource(R.drawable.like_icon_active);

            // сигнализируем что пользователь поддержал данную публикацию
            isLiked = true;
        }

        // меняем значение сигнализатора о том что поддержка включена/выключена на противоположное
        isLikedChanged = (!isLikedChanged);
    }

    //
    public int getRowsSum(int flagId, int publicationId) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // задаем имя таблицы
        String table_name = "";

        // создаем переменную в которой будем хранить результат
        int rowsSum = 0;

        //////////////////////////////////////////////////////////

        switch(flagId) {

            // данные будет брать из таблицы "publication_answer_data"
            case 1:
                    table_name = "publication_answer_data";
                    break;
            // данные будет брать из таблицы "liked_publication_data"
            case 2:
                    table_name = "liked_publication_data";
                    break;
        }

        // создаем массив значений, с названиями столбцов таблицы, которые хотим получить
        String[] columns        = new String[]{"count(*) as RowsSum"};

        // создаем строку-условие
        String selection        = "publication_id = ?";

        // создаем массив-значений, для строки-условия
        String[] selectionArgs  = new String[]{"" +publicationId};

        // пытаемся получить значения на основании заданных условий
        Cursor cursor = db.query(table_name, columns, selection, selectionArgs, null, null, null);

        // если данные были получены
        if(cursor.getCount() > 0) {
            // перемещаемся к первой строке данных
            cursor.moveToFirst();

            // получаем идентификатор искомой записи
            rowsSum = cursor.getInt(cursor.getColumnIndex("RowsSum"));
        }

        // закрываем курсор
        cursor.close();

        // закрываем подключение к БД
        dbHelper.close();

        // возвращаем результат
        return rowsSum;
    }

    //
    private LinearLayout getOwnButtonLL(final Dialog dialog, boolean isAuthorOfThisPost, final int publicationId) {

        // создаем параметризатор настроек компоновщика для текстового поля с X
        LinearLayout.LayoutParams layoutParamsWW  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);

        // создаем параметризатор настроек компоновщика для оранжевого контейнера-кнопки
        LinearLayout.LayoutParams layoutParamsFW = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
        // задаем ему отступы слева и справа по 10dp
        setMargins(layoutParamsFW, 10, 0, 10, 0);

        // создаем параметризатор настроек компоновщика для изображения в оранжевом контейнере-кнопке
        LinearLayout.LayoutParams imageLP = new LinearLayout.LayoutParams(((int) (30 * density)), ((int) (30 * density)), 0.0f);

        // создаем параметризатор настроек компоновщика для распорки
        LinearLayout.LayoutParams strutLP  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ((int) (20 * density)), 1.0f);

        // создаем оранжевый контейнер-кнопку
        LinearLayout orangeTextViewLL = new LinearLayout(context);
        orangeTextViewLL.setLayoutParams(layoutParamsFW);
        orangeTextViewLL.setOrientation(LinearLayout.HORIZONTAL);
        orangeTextViewLL.setGravity(Gravity.CENTER_VERTICAL);
        orangeTextViewLL.setBackgroundResource(R.drawable.rounded_rect_orange_info_btn);
        setPaddings(orangeTextViewLL, 10, 10, 10, 10);

        // создаем первую распорку, она будет слева от текста на кнопке
        View strut1 = new View(context);
        strut1.setLayoutParams(strutLP);

        // создаем вторую распорку, она будет справа от текста на кнопке
        View strut2 = new View(context);
        strut2.setLayoutParams(strutLP);

        // если это собственная публикация
        if(isAuthorOfThisPost) {

            // создаем текстовое отображение с "X"
            TextView deletePostTV = new TextView(context);
            deletePostTV.setLayoutParams(imageLP);
            deletePostTV.setGravity(Gravity.CENTER);
            deletePostTV.setTextSize(16);
            deletePostTV.setTypeface(Typeface.DEFAULT_BOLD);
            deletePostTV.setTextColor(context.getResources().getColor(R.color.white));
            deletePostTV.setText("X");

            // создаем надпись "Удалить"
            TextView deletePostTextTV = new TextView(context);
            deletePostTextTV.setLayoutParams(layoutParamsWW);
            deletePostTextTV.setTextSize(16);
            deletePostTextTV.setTextColor(context.getResources().getColor(R.color.white));
            deletePostTextTV.setText(context.getString(R.string.delete_text));

            // добавляем созданные элементы и распорки в оранжевый контейнер-кнопку
            orangeTextViewLL.addView(deletePostTV);
            orangeTextViewLL.addView(strut1);
            orangeTextViewLL.addView(deletePostTextTV);
            orangeTextViewLL.addView(strut2);

            // задаем обработчик нажатия на оранжевый контейнер-кнопку
            orangeTextViewLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                // закрыть "диалоговое окно информации"
                dialog.dismiss();

                // показать "диалоговое окно удаления публикации"
                showDeleteDialog(publicationId);
                }
            });
        }
        else {

            // создаем изображение для кнопки "Пожаловаться"
            ImageView complainIV = new ImageView(context);
            complainIV.setLayoutParams(imageLP);
            complainIV.setBackgroundResource(R.drawable._complain);

            // создаем надпись "Пожаловаться"
            TextView complainTextTV = new TextView(context);
            complainTextTV.setLayoutParams(layoutParamsWW);
            complainTextTV.setTextSize(16);
            complainTextTV.setTextColor(context.getResources().getColor(R.color.white));
            complainTextTV.setText(context.getString(R.string.complain_text));

            // добавляем созданные элементы и распорки в оранжевый контейнер-кнопку
            orangeTextViewLL.addView(complainIV);
            orangeTextViewLL.addView(strut1);
            orangeTextViewLL.addView(complainTextTV);
            orangeTextViewLL.addView(strut2);

            // задаем обработчик нажатия на оранжевый контейнер-кнопку
            orangeTextViewLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // закрываем "диалоговое окно информации"
                    dialog.dismiss();

                    // вызываем "диалоговое окно отправки жалобы"
                    showClaimDialog(publicationId);
                }
            });
        }

        // возвращаем оранжевый контейнер-кнопку
        return orangeTextViewLL;
    }

    //
    private void sendUserAnswer(int recipient_id, String userAnswerText) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // формируем параметры для записи ответа пользователя в "таблицу ответов на публикацию"
        String table_name = "publication_answer_data";

        String[] columnsArr  = {    "publication_id",
                                    "user_id",
                                    "recipient_id",
                                    "answer_text",
                                    "answer_date"
                               };
        String[] data = {
                            "" +publicationId,
                            "" +userId,
                            "" +recipient_id,
                            userAnswerText,
                            "" +System.currentTimeMillis()
                        };

        // добавляем запись в БД, получая ее идентификатор на выходе
        if(dbHelper.addRow(db, table_name, columnsArr, data) > 0) {

            // формируем под созданный ответ новый фрагмент
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            Answer_Fragment answerFragment = new Answer_Fragment();

            //////////////////////////////////////////////////////////////////////////////////////////

            // передаем созданному фрагменту части данных ответа пользователя
            answerFragment.setUserId(userId);
            answerFragment.setUserName(dbHelper.getUserName(db, "" + userId));
            answerFragment.setAnswerText(userAnswerTextET.getText().toString());
            answerFragment.setAnswerTimeAgoText(getResources().getString(R.string.few_seconds_ago));
            answerFragment.setIsRecipientSelectable(false);

            //////////////////////////////////////////////////////////////////////////////////////////

            // добавляем фрагмент в заданный контейнер
            ft.add(answersContainerLLResId, answerFragment);
            ft.commit();

            ////////////////////////////////////////////////////////////////

            // получаем общее кол-во ответов на публикацию
            answersSumValue = getRowsSum(1, publicationId);

            // обновляем значение в "поле с общим кол-во ответов пользователей на публикацию"
            answersSumTV.setText(String.valueOf(answersSumValue));

            // чистим "поле с текстом ответа пользователя"
            userAnswerTextET.setText("");

            // получаем доступ к системной клавиатуре и плавно скрываем ее
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(userAnswerTextET.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            // прокручиваем контенер ответов на последний ответ
            scrollViewSV.post(new Runnable() {
                @Override
                public void run() {
                    scrollViewSV.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

        //////////////////////////////////////////////////////////////////////////////////////////

        // dbHelper.showAllTableData(db, "publication_answer_data");

        //////////////////////////////////////////////////////////////////////////////////////////

        // закрываем подключение к БД
        dbHelper.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void setAnswersData() {

        // получаем данные по ответам
        answersDataArrList = getAnswersData();

        // получаем кол-во полученных ответов
        int answersSum = answersDataArrList.size();

        // формируем под каждый ответ свой фрагмент
        for(int i=0; i<answersSum; i++){

            FragmentTransaction ft = getFragmentManager().beginTransaction();

            Answer_Fragment answerFragment = new Answer_Fragment();

            //////////////////////////////////////////////////////////////////////////////////////////

            // получаем идентификатор автора ответа
            int answerAuthorId = Integer.parseInt(answersDataArrList.get(i)[0]);

            // передаем в фрагмент остальные части ответа пользователя
            answerFragment.setUserId(answerAuthorId);
            answerFragment.setUserName(answersDataArrList.get(i)[1]);
            answerFragment.setAnswerText(answersDataArrList.get(i)[2]);
            answerFragment.setAnswerTimeAgoText(answersDataArrList.get(i)[3]);

            // если пользователь не является автором данного ответа
            if(answerAuthorId != userId)
                // показать кнопку "Ответить"
                answerFragment.setIsRecipientSelectable(true);
            // если пользователь - автор ответа
            else
                // спрятать кнопку "Ответить"
                answerFragment.setIsRecipientSelectable(false);

            //////////////////////////////////////////////////////////////////////////////////////////

            // добавляем сформированный фрагмент в заданный контейнер
            ft.add(answersContainerLLResId, answerFragment);
            ft.commit();
        }
    }

    //
    private ArrayList<String[]> getAnswersData() {

        // создаем "список ответов пользователей"
        ArrayList<String[]> resultArrList = new ArrayList<String[]>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor;

        // формируем запрос к БД на получение всех данных по ответам пользователей для заданной публикации
        String sqlQuery = "select *                         " +
                          "  from publication_answer_data   " +
                          " where publication_id = ?";

        ///////////////////////////////////////////////////////////////////////////////////////

        // получаем данные из "таблицы ответов на публикацию"
        cursor = db.rawQuery(sqlQuery.toString(), new String[] {"" +publicationId});

        // если данные получены
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // определяем номера столбцов в выборке по их именам
            int answerAuthorIdColIndex      = cursor.getColumnIndex("user_id");
            int answerTextColIndex          = cursor.getColumnIndex("answer_text");
            int answerDateColIndex          = cursor.getColumnIndex("answer_date");

            do {
                // создаем строковый массив для отдельных частей ответа пользователя
                String[] dataBlock = new String[4];

                dataBlock[0] = "" +cursor.getInt(answerAuthorIdColIndex);
                dataBlock[1] = dbHelper.getUserName(db, "" +cursor.getInt(answerAuthorIdColIndex));
                dataBlock[2] = cursor.getString(answerTextColIndex);
                dataBlock[3] = getTimeAgoText(cursor.getString(answerDateColIndex));

                // добавляем ответ пользователя в "список ответов пользователей"
                resultArrList.add(dataBlock);
            } while (cursor.moveToNext());
        }

        // закрываем курсор
        cursor.close();

        // закрываем подключение к БД
        dbHelper.close();

        // возвращем результат
        return resultArrList;
    }

    //
    private String getTimeAgoText(String answerDate) {

        // создаем переменную для формирования информационного текста о минувшем времени написания ответа
        StringBuilder resultStr = new StringBuilder();

        // получаем числовое представление(в милисекундах) даты написания ответа пользователем
        long answerDateLong = Long.parseLong(answerDate);

        // получаем числовое представление(в секундах) даты написания ответа пользователем
        long secondsAgo     = ((System.currentTimeMillis() - answerDateLong) / 1000);

        // формируем переменные для временных отрезков
        int oneMinute   = 60;
        int oneHour     = oneMinute * 60;
        int oneDay      = oneHour   * 24;
        int oneWeek     = oneDay    * 7;
        int oneMonth    = oneWeek   * 4;
        int oneYear     = oneMonth  * 12;

        // если прошло меньше часа
        if(secondsAgo < oneHour) {
            // несколько секунд назад
            if(secondsAgo < oneMinute)
                resultStr.append("" +getResources().getString(R.string.few_seconds_ago));
            // минуту назад
            else if((secondsAgo >= oneMinute) && (secondsAgo < (oneMinute * 2)))
                resultStr.append("" +getResources().getString(R.string.one_minute_ago));
                // 2 минуты назад
            else if((secondsAgo >= (oneMinute * 2)) && (secondsAgo < (oneMinute * 5)))
                resultStr.append("" +getResources().getString(R.string.two_minutes_ago));
                // 5 минут назад
            else if((secondsAgo >= (oneMinute * 5)) && (secondsAgo < (oneMinute * 10)))
                resultStr.append("" +getResources().getString(R.string.five_minutes_ago));
                // 10 минут назад
            else if((secondsAgo >= (oneMinute * 10)) && (secondsAgo < (oneMinute * 30)))
                resultStr.append("" +getResources().getString(R.string.ten_minutes_ago));
                // 30 минут назад
            else if(secondsAgo >= (oneMinute * 30))
                resultStr.append("" +getResources().getString(R.string.thirty_minutes_ago));
        }
        // если прошло больше часа, но меньше дня
        else if(secondsAgo < oneDay) {
            // час назад
            if((secondsAgo >= oneHour) && (secondsAgo < (oneHour * 2)))
                resultStr.append("" +getResources().getString(R.string.one_hour_ago));
                // 2 часа назад
            else if((secondsAgo >= (oneHour * 2)) && (secondsAgo < (oneHour * 5)))
                resultStr.append("" +getResources().getString(R.string.two_hours_ago));
                // 5 часов назад
            else if((secondsAgo >= (oneHour * 5)) && (secondsAgo < (oneHour * 10)))
                resultStr.append("" +getResources().getString(R.string.five_hours_ago));
                // 10 часов назад
            else if(secondsAgo >= (oneHour * 10))
                resultStr.append("" +getResources().getString(R.string.ten_hours_ago));
        }
        // если прошло больше дня, но меньше недели
        else if(secondsAgo < oneWeek) {
            // 1 день назад
            if((secondsAgo >= oneDay) && (secondsAgo < (oneDay * 2)))
                resultStr.append("" +getResources().getString(R.string.one_day_ago));
                // 2 дня назад
            else if((secondsAgo >= (oneDay * 2)) && (secondsAgo < (oneDay * 5)))
                resultStr.append("" +getResources().getString(R.string.two_days_ago));
                // 5 дней назад
            else if(secondsAgo >= (oneDay * 5))
                resultStr.append("" +getResources().getString(R.string.five_days_ago));
        }
        // если прошло больше недели, но меньше месяца
        else if(secondsAgo < oneMonth) {
            // неделю назад
            if((secondsAgo >= oneWeek) && (secondsAgo < (oneWeek * 2)))
                resultStr.append("" +getResources().getString(R.string.one_week_ago));
                // 2 недели назад
            else if((secondsAgo >= (oneWeek * 2)) && (secondsAgo < (oneWeek * 3)))
                resultStr.append("" +getResources().getString(R.string.two_weeks_ago));
                // 3 недели назад
            else if(secondsAgo >= (oneWeek * 3))
                resultStr.append("" +getResources().getString(R.string.three_weeks_ago));
        }
        // если прошло больше месяца, но меньше года
        else if(secondsAgo < oneYear) {
            // месяц назад
            if((secondsAgo >= oneMonth) && (secondsAgo < (oneMonth * 2)))
                resultStr.append("" +getResources().getString(R.string.one_month_ago));
                // 2 месяца назад
            else if((secondsAgo >= (oneMonth * 2)) && (secondsAgo < (oneMonth * 6)))
                resultStr.append("" +getResources().getString(R.string.two_month_ago));
                // пол-года назад
            else if((secondsAgo >= (oneMonth * 6)) && (secondsAgo < oneYear))
                resultStr.append("" +getResources().getString(R.string.six_month_ago));
        }
        // если прошло меньше 10 лет
        else if(secondsAgo < (oneYear * 10)) {
            // год назад
            if((secondsAgo >= oneYear) && (secondsAgo < (oneYear * 2)))
                resultStr.append("" +getResources().getString(R.string.one_year_ago));
                // 2 года назад
            else if((secondsAgo >= (oneYear * 2)) && (secondsAgo < (oneYear * 5)))
                resultStr.append("" +getResources().getString(R.string.two_years_ago));
                // 5 лет назад
            else if((secondsAgo >= (oneYear * 5)))
                resultStr.append("" +getResources().getString(R.string.five_years_ago));
        }
        // время не определено
        else
            resultStr.append("" +getResources().getString(R.string.some_time_ago));

        // вернуть результат
        return resultStr.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void setMargins(LinearLayout.LayoutParams layout,int left, int top, int right, int bottom) {

        int marginLeft     = (int)(left * density);
        int marginTop      = (int)(top * density);
        int marginRight    = (int)(right * density);
        int marginBottom   = (int)(bottom * density);

        layout.setMargins(marginLeft, marginTop, marginRight, marginBottom);
    }

    //
    private void setPaddings(View view, int left, int top, int right, int bottom) {

        float density = context.getResources().getDisplayMetrics().density;

        int paddingLeft     = (int)(left * density);
        int paddingTop      = (int)(top * density);
        int paddingRight    = (int)(right * density);
        int paddingBottom   = (int)(bottom * density);

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * загрузка сохраненных значений из Preferences
     */
    private void loadTextFromPreferences() {

        // если настройки содержат имя пользователя
        if (shPref.contains("user_id")) {
            // значит можно получить и его идентификатор
            userId = Integer.parseInt(shPref.getString("user_id", "0"));
        }
    }
}