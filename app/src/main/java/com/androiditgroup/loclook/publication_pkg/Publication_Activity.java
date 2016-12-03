package com.androiditgroup.loclook.publication_pkg;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androiditgroup.loclook.utils_pkg.DB_Helper;
import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.tape_pkg.Tape_Activity;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by OS1 on 15.09.2015.
 */
public class Publication_Activity   extends     Activity
                                    implements  View.OnClickListener,
                                                TextWatcher,
                                                CompoundButton.OnCheckedChangeListener,
                                                Quiz_Answers_Fragment.OnQuizAnswerSumChangedListener {

    Context             context;
    SharedPreferences   shPref;
    FragmentTransaction fragmentTransaction;
    LocationManager     locationManager;

    final int arrowBackResId        = R.id.Publication_ArrowBackWrapLL;
    final int photoCameraResId      = R.id.Publication_PhotoCameraWrapLL;
    final int sendPublicationResId  = R.id.Publication_SendPublicationWrapLL;
    final int publicationTextResId  = R.id.Publication_PublicationTextET;
    final int leftCharactersResId   = R.id.Publication_LeftCharactersBodyTV;
    final int anonymousStateResId   = R.id.Publication_AnonymousStateTV;
    final int anonymousSwitchResId  = R.id.Publication_AnonymousSwitchBTN;
    final int quizStateResId        = R.id.Publication_QuizStateTV;
    final int quizSwitchResId       = R.id.Publication_QuizSwitchBTN;
    final int badgeRowResId         = R.id.Publication_BadgeRowRL;
    final int badgeNameResId        = R.id.Publication_BadgeNameTV;
    final int badgeImageResId       = R.id.Publication_BadgeImageIV;
    final int imageContainerResId   = R.id.Publication_ImageContainerLL;

    LinearLayout    sendPublicationLL;
    RelativeLayout  badgeRowRL;

    EditText        publicationTextET;
    TextView        leftCharactersSumTV;
    // ToggleButton    anonymousSwitch,quizSwitch;

    Switch          anonymousSwitch;
    Switch          quizSwitch;

    ArrayList<String[]> badgesDataArrList;

    ArrayList<Uri>      bitmapsPathList             = new ArrayList<Uri>();
    ArrayList<Bitmap>   bitmapsList                 = new ArrayList<Bitmap>();
    ArrayList<Float>    bitmapsRotateDegreesList    = new ArrayList<Float>();

    Map<String,View> answersMap = new HashMap<String,View>();

    Quiz_Answers_Fragment quizAnswersFragment;

    LinearLayout selectedImageContainer;

    float density;

    int userId;

    int imagesSum       = 0;
    int selectedImageId = -1;
    int badgeId         = 1;
    int textLimit       = 400;
    int typedTextLength = 0;

    float latitude      = 0.0f;
    float longitude     = 0.0f;

    String region_name  = "";
    String street_name  = "";
    String badgeText    = "LocLook";

    String selectedImageAction = "";

    // private final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publication);

        /////////////////////////////////////////////////////////////////////////////////

        context = getApplicationContext();
        density = context.getResources().getDisplayMetrics().density;
        shPref  = context.getSharedPreferences("user_data", context.MODE_PRIVATE);

        loadTextFromPreferences();

        /////////////////////////////////////////////////////////////////////////////////

        findViewById(arrowBackResId).setOnClickListener(this);
        findViewById(photoCameraResId).setOnClickListener(this);

        sendPublicationLL = (LinearLayout) findViewById(sendPublicationResId);
        sendPublicationLL.setOnClickListener(this);

        publicationTextET = (EditText) findViewById(publicationTextResId);
        publicationTextET.addTextChangedListener(this);

        leftCharactersSumTV = (TextView) findViewById(leftCharactersResId);

        anonymousSwitch = (Switch) findViewById(anonymousSwitchResId);
        // anonymousSwitch = (ToggleButton) findViewById(anonymousSwitchResId);
        anonymousSwitch.setOnCheckedChangeListener(this);

        quizSwitch = (Switch) findViewById(quizSwitchResId);
        // quizSwitch = (ToggleButton) findViewById(quizSwitchResId);
        quizSwitch.setOnCheckedChangeListener(this);

        badgeRowRL = (RelativeLayout) findViewById(badgeRowResId);
        badgeRowRL.setOnClickListener(this);

        /////////////////////////////////////////////////////////////////////////////////

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);

        /////////////////////////////////////////////////////////////////////////////////

        setLocationName();

        ///////////////////////////////////////////////////////////////////////////////////////////

/*        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP*/
    }

    //
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            // получаем координаты местоположения пользователя
            latitude  = Float.parseFloat("" +location.getLatitude());
            longitude = Float.parseFloat("" +location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) {

            Location myLocation = locationManager.getLastKnownLocation(provider);

            // получаем координаты местоположения пользователя
            latitude  = Float.parseFloat("" +myLocation.getLatitude());
            longitude = Float.parseFloat("" +myLocation.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) { }
    };

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            // щелчок по "стрелке назад"
            case arrowBackResId:
                                        // переходим на Ленту
                                        finish();
                                        break;
            // щелчок по "камере"
            case photoCameraResId:
                                        // если добавленных изображений меньше 3
                                        if(imagesSum < 3)
                                            // выводим на жкран "диалоговое окно для добавления изображения"
                                            showPhotoDialog();
                                        break;
            // щелчок по "изображению отправки публикации"
            case sendPublicationResId:
                                        // если публикация содержит текст
                                        if(typedTextLength > 0) {

                                            // если запись публикации в БД совершена
                                            if(sendPublication()) {

                                                // отключаем кликабельность "кнопки отправки публикации"
                                                sendPublicationLL.setClickable(false);

                                                // переходим на Ленту
                                                Intent intent = new Intent(Publication_Activity.this, Tape_Activity.class);
                                                startActivity(intent);
                                            }
                                        }
                                        break;
            // щелчок по "контейнеру выбора бейджика"
            case badgeRowResId:
                                        // выводим на жкран "диалоговое окно для выбора бейджика"
                                        showBadgesDialog();
                                        break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        // получаем кол-во напечатанных символов в поле
        typedTextLength = publicationTextET.length();

        // вычисляем и отображаем кол-во символов, которые еще можно внести в поле
        leftCharactersSumTV.setText("" + (textLimit - typedTextLength));
    }

    @Override
    public void afterTextChanged(Editable editable) { }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        fragmentTransaction = getFragmentManager().beginTransaction();

        switch (compoundButton.getId()) {

            // щелчок по "переключателю вкл./выкл. анонимность"
            case anonymousSwitchResId:
                // если режим "Анонимность" включен
                if(b)
                    // задаем текст "вкл."
                    ((TextView) findViewById(anonymousStateResId)).setText(R.string.state_on_text);
                    // если режим "Анонимность" выключен
                else
                    // задаем текст "выкл."
                    ((TextView) findViewById(anonymousStateResId)).setText(R.string.state_off_text);
                break;
            // щелчок по "переключателю вкл./выкл. опрос"
            case quizSwitchResId:
                // если режим "Опрос" включен
                if(b){
                    // задаем текст "вкл."
                    ((TextView) findViewById(quizStateResId)).setText(R.string.state_on_text);

                    // получаем фрагмент с ответами для опроса
                    quizAnswersFragment = new Quiz_Answers_Fragment();

                    // добавляем фрагмент с ответами для опроса в контейнер
                    fragmentTransaction.add(R.id.Publication_QuizAnswersLL, quizAnswersFragment);
                }
                // если режим "Опрос" выключен
                else {
                    // задаем текст "выкл."
                    ((TextView) findViewById(quizStateResId)).setText(R.string.state_off_text);

                    // удаляем фрагмент с ответами для опроса из контейнера
                    fragmentTransaction.remove(quizAnswersFragment);
                }
                break;
        }

        fragmentTransaction.commit();
    }

    @Override
    public void onQuizAnswerSumChanged(String operationName, String answerETTagName, EditText answerETLink) {

        // если это операция добавления ответа в опрос
        if(operationName.equals("add"))
            // кладем его в "коллекцию ответов опроса"
            answersMap.put(answerETTagName,answerETLink);
            // если это операция удаления ответа из опроса
        else
            // удаляем его из "коллекции ответов опроса"
            answersMap.remove(answerETTagName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentData) {
        super.onActivityResult(requestCode, resultCode, intentData);

        // если ответ из диалогового окна пришел
        if((requestCode == 1) && (resultCode == RESULT_OK) && (intentData != null))
            // добавляем выбранное изображение
            addSelectedImage(intentData.getData());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void showPhotoDialog() {

        // создаем "диалоговое окно для добавления изображения"
        final Dialog dialog = new Dialog(Publication_Activity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.dialog_camera);
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        // задаем обработчик щелчка по "кнопке Выбрать изображение"
        dialog.findViewById(R.id.Publication_ChooseImageTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // вызываем стандартную галерею для выбора изображения
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

                // Тип получаемых объектов - image:
                photoPickerIntent.setType("image/*");

                // Запускаем переход с ожиданием обратного результата в виде информации об изображении:
                startActivityForResult(photoPickerIntent, 1);

                // закрываем "диалоговое окно для добавления изображения"
                dialog.dismiss();
            }
        });

        // задаем обработчик щелчка по "кнопке Камера"
        dialog.findViewById(R.id.Publication_CameraTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    // отправляем намерение для запуска камеры
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(captureIntent, 2);
                } catch (ActivityNotFoundException e) {
                    // Выводим сообщение об ошибке
                    String errorMessage = "Ваше устройство не поддерживает съемку";
                    Toast.makeText(Publication_Activity.this, errorMessage, Toast.LENGTH_SHORT).show();
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
    private void addSelectedImage(Uri photoPath) {

        // "забываем" прежнее выбранное изображение
        this.selectedImageId = -1;

        // увеличиваем счетчик изображений
        imagesSum++;

        // запускаем сборку контейнеров изображений
        setImagesContainer();

        // добавляем значение в "список путей добавляемых изображений"
        bitmapsPathList.add(photoPath);

        // добавляемое значение по-умолчанию в "список кол-ва градусов для поворота изображений"
        bitmapsRotateDegreesList.add(0.0f);

        // меняем размеры добавленных изображений
        reDecodeFiles();

        // раскладываем представления с изображениями в "контейнеры под *-ое изображение"
        setImages();
    }

    //
    private void setImagesContainer() {

        // находим "контейнер для добавляемых изображений"
        final LinearLayout imagesContainer = (LinearLayout) findViewById(imageContainerResId);

        LinearLayout.LayoutParams lp;

        // задаем размеры для "контейнера под *-ое изображение" на каждый из трех режимов
        int size_3 = ((imagesContainer.getWidth() - 20) / 3);   // добавлено 3 изображения
        int size_2 = ((imagesContainer.getWidth() - 20) / 2);   // добавлено 2 изображения
        int size_1 = (size_2 + size_3);                         // добавлено 1 изображение

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

                        // выводим на жкран "диалоговое окно для работы с изображением"
                        showImageDialog(imageLL_0, selectedImageId_0);
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

                    // позициия добавляемого элемента
                    final int elementPos = i;

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

                            // выводим на жкран "диалоговое окно для работы с изображением"
                            showImageDialog(imageLL_1, selectedImageId_1);
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

                    // позициия добавляемого элемента
                    final int elementPos = i;

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

                            // выводим на жкран "диалоговое окно для работы с изображением"
                            showImageDialog(imageLL_2, selectedImageId_2);
                        }
                    });

                    // добавляем "контейнер под *-ое изображение" в "контейнер для добавляемых изображений"
                    imagesContainer.addView(imageLL_2);
                }
                break;
        }
    }

    //
    private void showImageDialog(final LinearLayout imageContainer, int selectedImageId) {

        ArrayList<String> actionsArrayList = new ArrayList<String>();
        actionsArrayList.add(context.getResources().getString(R.string.rotate_on_90_degrees_left_text));
        actionsArrayList.add(context.getResources().getString(R.string.rotate_on_90_degrees_right_text));
        actionsArrayList.add(context.getResources().getString(R.string.delete_text));

        // создаем "диалоговое окно для выбора действия над изображением"
        final Dialog dialog = new Dialog(Publication_Activity.this, R.style.InfoDialog_Theme);
        dialog.setContentView(R.layout.dialog_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // запоминаем ссылку на "контейнер под *-ое изображение", по которому был сделан клик
        selectedImageContainer  = imageContainer;

        // "запоминаем" id выбранного изображения
        this.selectedImageId = selectedImageId;

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // создаем параметры отображения компонентов

        LinearLayout.LayoutParams actionLP           = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        actionLP.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        setMargins(actionLP, 10, 0, 10, 5);

        LinearLayout.LayoutParams actionsContainerLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams actionTextLP       = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout.LayoutParams strutLP            = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ((int) (20 * density)), 1.0f);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // находим "представление с прокруткой"
        final ScrollView scrollViewSV = (ScrollView) dialog.findViewById(R.id.ImagesDialog_ScrollViewSV);

        // создаем "контейнер действий по работе с изображением"
        LinearLayout actionsContainer = new LinearLayout(context);
        actionsContainer.setLayoutParams(actionsContainerLP);
        actionsContainer.setOrientation(LinearLayout.VERTICAL);

        // получаем кол-во действий по работе с изображением
        int actionsSum = actionsArrayList.size();

        // в цикле собираем элементы в "контейнер действий по работе с изображением"
        for(int i=0; i<actionsSum; i++) {

            // создаем "контейнер действия над изображением"
            final LinearLayout actionLL = new LinearLayout(context);
            actionLL.setLayoutParams(actionLP);
            actionLL.setOrientation(LinearLayout.HORIZONTAL);
            actionLL.setBackgroundColor(context.getResources().getColor(R.color.white));

            switch(i) {
                case 0:
                    actionLL.setTag("rotate_on_90_degrees_left");
                    break;
                case 1:
                    actionLL.setTag("rotate_on_90_degrees_right");
                    break;
                case 2:
                    actionLL.setTag("delete");
                    break;
            }

            setPaddings(actionLL, 5, 5, 5, 5);

            ///////////////////////////////////////////////////////////////////////////////////////

            // создаем "представление с названием действия"
            TextView actionTextTV = new TextView(context);
            actionTextTV.setLayoutParams(actionTextLP);
            actionTextTV.setTextColor(getResources().getColor(R.color.user_name_blue));
            actionTextTV.setTextSize(14);
            actionTextTV.setTypeface(Typeface.DEFAULT_BOLD);
            actionTextTV.setTag("actionTextTV");

            // задаем значение для "представления с названием действия"
            String actionText = actionsArrayList.get(i);
            actionTextTV.setText(actionText);

            // создаем распорку чтобы прижать элементы к левому краю "контейнера действия над изображением"
            View rightLineStrut = new View(context);
            rightLineStrut.setLayoutParams(strutLP);

            ///////////////////////////////////////////////////////////////////////////////////////

            // задаем обработчик щелчка по "контейнеру действия над изображением"
            actionLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // меняем цветовое оформление действий при выборе одного из них
                    selectImageAction(scrollViewSV, actionLL.getTag().toString());

                    // осуществляем выбранное действие над изображением
                    makeSelectedImageAction();

                    // закрыть "диалоговое окно для выбора действия над изображением"
                    dialog.dismiss();
                }
            });

            ///////////////////////////////////////////////////////////////////////////////////////

            // добавляем элементы в "контейнер действия над изображением"
            actionLL.addView(actionTextTV);
            actionLL.addView(rightLineStrut);

            // кладем "контейнер действия над изображением" в "контейнер действий над изображением"
            actionsContainer.addView(actionLL);
        }

        // кладем "контейнер действий над изображениями" в "представление с прокруткой"
        scrollViewSV.addView(actionsContainer);

        // создаем обработчик нажатия на "кнопку Закрыть"
        dialog.findViewById(R.id.ImagesDialog_CloseLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // "забываем" на "контейнер под *-ое изображение", по которому был сделан клик
                selectedImageContainer = null;

                // "забываем" прежние действия с изображением
                selectedImageAction     = "";

                // закрыть "диалоговое окно для выбора действия над изображением"
                dialog.dismiss();
            }
        });

        // показываем сформированное диалоговое окно
        dialog.show();
    }

    //
    private void selectImageAction(ScrollView scrollViewSV, String selectedActionTag) {

        // задаем цвета для оформления действий с изображениями
        final int whiteColor    = context.getResources().getColor(R.color.white);
        final int orangeColor   = context.getResources().getColor(R.color.selected_item_orange);
        final int blueColor     = context.getResources().getColor(R.color.user_name_blue);

        // находим "контейнер действий над изображением" в "представлении с прокруткой"
        LinearLayout actionsContainer = (LinearLayout) scrollViewSV.getChildAt(0);

        // получаем кол-во действий над изображением
        int actionsSum = actionsContainer.getChildCount();

        // перебираем в цикле все "контейнеры действия над изображением" и меняем их оформление
        for(int i=0; i<actionsSum; i++) {

            // находим "контейнер действия над изображением"
            LinearLayout actionLL = (LinearLayout) actionsContainer.getChildAt(i);

            // находим по тегу "представление с названием действия"
            TextView actionTextTV = (TextView) actionLL.findViewWithTag("actionTextTV");

            // получаем тег "контейнера действия над изображением"
            String actionLLTag = actionLL.getTag().toString();

            // если тег "контейнер действия над изображением" совпадает с тегом "контейнера действия над изображением" который был выбран
            if(actionLLTag.equals(selectedActionTag)) {

                // задаем оформление выбранного элемента списка

                // меняем цвет текста "представления с названием действия"
                actionTextTV.setTextColor(whiteColor);

                // меняем фон "контейнера действия над изображением"
                actionLL.setBackgroundColor(orangeColor);

                // "запоминаем" действие над изображением, которое надо осуществить
                selectedImageAction = selectedActionTag;
            }
            // если тег "контейнера действия над изображением" НЕ совпадает с тегом "контейнера действия над изображением" который был выбран
            else {

                // задаем оформление обычного элемента списка

                // меняем цвет текста "представление с названием действия"
                actionTextTV.setTextColor(blueColor);

                // меняем фон "контейнера действия над изображением"
                actionLL.setBackgroundColor(whiteColor);
            }
        }
    }

    //
    private void makeSelectedImageAction() {

        // если выбрано действие "Повернуть на 90 градусов влево"
        if(selectedImageAction.equals("rotate_on_90_degrees_left"))
            rotateImage(selectedImageContainer, -90.0f);
            // если выбрано действие "Повернуть на 90 градусов вправо"
        else if(selectedImageAction.equals("rotate_on_90_degrees_right"))
            rotateImage(selectedImageContainer, 90.0f);
            // если выбрано действие "Удалить"
        else if(selectedImageAction.equals("delete")) {

            // если "контейнер под *-ое изображение", по которому был сделан клик и порядковый номер изображения зафиксированы
            if(selectedImageContainer != null && selectedImageId != -1) {

                // удаляем "контейнер под *-ое изображение" из "контейнера добавляемых изображений"
                LinearLayout parentContainer = (LinearLayout) selectedImageContainer.getParent();
                parentContainer.removeView(selectedImageContainer);

                // уменьшаем кол-во изображений в "контейнере добавляемых изображений"
                imagesSum--;

                // удаляем выбранный элемент из списка
                bitmapsList.remove(selectedImageId);
                bitmapsPathList.remove(selectedImageId);
                bitmapsRotateDegreesList.remove(selectedImageId);

                // "забываем" прежнее выбранное изображение
                this.selectedImageId = -1;

                // если еще осталось хоть одно изображение
                if(imagesSum > 0) {

                    // запускаем пересборку контейнеров изображений
                    setImagesContainer();

                    // меняем размеры добавленных изображений
                    reDecodeFiles();

                    // раскладываем представления с изображениями в "контейнеры под *-ое изображение"
                    setImages();
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        // "забываем" на "контейнер под *-ое изображение", по которому был сделан клик
        selectedImageContainer = null;

        // "забываем" прежние действия с изображением
        selectedImageAction     = "";
    }

    //
    private void reDecodeFiles() {

        // устанавливаем значения для режима когда добавлено одно изображение
        int reqWidth    = 200;
        int reqHeight   = 200;

        // если хоть одно изображение для изменения размеров есть
        if(imagesSum > 0) {
            // создаем "список изображений после изменения их размера"
            ArrayList<Bitmap> tempBitmapList = new ArrayList<Bitmap>();

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

            // проходим циклом по "списку путей добавляемых изображений"
            for(int i=0; i<bitmapsPathList.size(); i++) {

                // получаем изображение на основании адреса
                Bitmap bitmap = decodeFile(bitmapsPathList.get(i), reqWidth, reqHeight);

                // получаем "кол-во градусов для поворота изображения"
                float rotateDegrees = bitmapsRotateDegreesList.get(i);

//                Log.d("myLogs", "reDecodeFiles(): i= " +i+ " rotateDegrees= " +rotateDegrees);

                // если "кол-во градусов для поворота изображения" больше или меньше 0, но не равно 0
                if(rotateDegrees != 0.0f) {

                    // пересоздаем изображение повернутое на "кол-во градусов для поворота изображения"
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotateDegrees);
                    Bitmap bitmapRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    // кладем в "список изображений после изменения их размера" очередное изображение после его обработки
                    tempBitmapList.add(bitmapRotated);
                }
                else
                    // кладем в "список изображений после изменения их размера" очередное изображение после его обработки
                    tempBitmapList.add(bitmap);
            }

            // запоминаем ссылку на "список изображений после изменения их размера", с ним и будем дальше работать
            bitmapsList = tempBitmapList;
        }
    }

    //
    private void setImages() {

        // находим "контейнер для добавляемых изображений"
        final LinearLayout imagesContainer = (LinearLayout) findViewById(imageContainerResId);

        // проходим циклом по "списку добавленных изображений"
        for(int i=0; i<bitmapsPathList.size(); i++) {

            // создаем представление для добавляемого изображения
            final ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));

            // задаем тип масштабирования изображения в представлении
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // кладем изображение в представление
            imageView.setImageBitmap(bitmapsList.get(i));

            // кладем представление в приготовленный для него заранее "контейнер под *-ое изображение"
            LinearLayout imageContainer = (LinearLayout) imagesContainer.getChildAt(i);
            imageContainer.addView(imageView);
        }
    }

    //
    private Bitmap decodeFile(Uri photoPath, int reqWidth, int reqHeight) {

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
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
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
    private void rotateImage(LinearLayout container, float rotateOnDegrees) {

        // задаем кол-во градусов для поворота изображения
        float newRotateOnDegrees = rotateOnDegrees;

        // если выполняется действие для выбранного изображения
        if((selectedImageId != -1) && (selectedImageId < bitmapsRotateDegreesList.size())) {

            // вычисляем новое кол-во градусов для поворота изображения с учетом того что было ранее выбрано и сейчас
            newRotateOnDegrees = (bitmapsRotateDegreesList.get(selectedImageId) + rotateOnDegrees);

            // если кол-во градусов для поворота изображения достигло максимума/минимума
            if((newRotateOnDegrees == 360.0f) || (newRotateOnDegrees == -360.0f))
                // обнуляем кол-во градусов для поворота изображения
                newRotateOnDegrees = 0.0f;

            // запоминаем в "списке кол-ва градусов для поворота изображений" новое значение для выбранного изображения
            bitmapsRotateDegreesList.set(selectedImageId, newRotateOnDegrees);
        }

        // получаем представление с изображением
        ImageView myImageView = (ImageView) container.getChildAt(0);

        // создаем анимацию
        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);

        // задаем параметры анимации
        final RotateAnimation animRotate = new RotateAnimation(0.0f, newRotateOnDegrees, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animRotate.setDuration(150);
        animRotate.setFillAfter(true);
        animSet.addAnimation(animRotate);

        // запускаем анимированный поворот выбранного изображения
        myImageView.startAnimation(animSet);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean sendPublication() {

        boolean result = false;

        //////////////////////////////////////////////////////////////////////////////////////////

        int publicationId;
        int quizId = 0;

        String[] answersArr = new String[]{};

        // если надо включить опрос в публикацию
        if(quizSwitch.isChecked()) {

            // получаем все ответы опроса
            answersArr = getQuizAnswers();

            // если ответов для опроса меньше 2
            if (answersArr.length < 2) {
                // вывести тревожное сообщение
                Toast.makeText(this, context.getString(R.string.need_more_answers_text), Toast.LENGTH_SHORT).show();

                // запись публикации в БД не произведена
                return false;
            }
        }

        // добавляем публкацию в БД и получаем ее идентификатор
        publicationId = addPublication();

        // если получен идентификатор публикации
        if(publicationId > 0) {

            // если надо включить опрос в публикацию
            if(quizSwitch.isChecked()) {

                // добавляем опрос в БД и получаем его идентификатор
                quizId = addQuiz(publicationId);

                // если получен идентификатор опроса
                if(quizId > 0) {

                    // проходим циклом по массиву ответов
                    for(String answer:answersArr)
                        // добавляем ответы в БД
                        addQuizAnswer(quizId,answer);
                }
            }

            // проходим циклом по спискам данных добавляемых изображений
            for(int i=0; i<imagesSum; i++)
                // добаввляем очередное изображение в БД
                addPublicationImage(publicationId, bitmapsPathList.get(i), bitmapsRotateDegreesList.get(i));

            // запись публикации в БД успешна
            result = true;
        }

        // возвращаем результат
        return result;
    }

    //
    private String[] getQuizAnswers() {

        // создаем "список для имен полей ответов опроса"
        ArrayList<String> answersTagList = new ArrayList<String>();

        // создаем список для ответов
        ArrayList<String> answersList = new ArrayList<String>();

        // проходим циклом по коллекции накопленных ответов
        for(Map.Entry<String,View> entry:answersMap.entrySet())
            // добавляем в "список для имен полей ответов опроса" значения
            answersTagList.add(entry.getKey());

        // сортируем "список для имен полей ответов опроса"
        Collections.sort(answersTagList);

        // проходим циклом по ответам опроса
        for(int i=0; i<answersTagList.size(); i++) {

            // получаем из коллекции ссылку на поле с ответом и читаем из него значение
            // ключ коллекции - полученное из списка название поля с ответом
            String answer = (((EditText) answersMap.get(answersTagList.get(i))).getText().toString()).trim();

            // если ответ не пустой
            if(answer.length() > 0) {
                // кладем его в список с ответами
                answersList.add(answer);
            }
        }

        // приводим список с ответами к текстовому массиву
        String[] answersArr = answersList.toArray(new String[answersList.size()]);

        // возвращаем результат
        return answersArr;
    }

    private int addPublication() {

        DB_Helper dbHelper = new DB_Helper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////////////

        if((latitude > 0.0f) && (longitude > 0.0f))
            setLocationName();

        String table_name   = "publication_data";

        String[] columnsArr = { "user_id",
                                "enter_text",
                                "enter_date",
                                "anonymous",
                                "quiz_added",
                                "badge_id",
                                "map_latitude",
                                "map_longitude",
                                "region_name",
                                "street_name"
        };

        String[] data       = { "" +userId,
                                publicationTextET.getText().toString(),
                                "" +System.currentTimeMillis(),
                                "" +anonymousSwitch.isChecked(),
                                "" +quizSwitch.isChecked(),
                                "" +badgeId,
                                "" +latitude,
                                "" +longitude,
                                region_name,
                                street_name
        };

        // добавляем запись в БД, получая ее идентификатор на выходе
        int publicationId = dbHelper.addRow(db, table_name, columnsArr, data);

        //////////////////////////////////////////////////////////////////////////////////////////

        // закрываем подключение к БД
        dbHelper.close();

        // возвращаем полученный идентификатор
        return publicationId;
    }

    //
    private int addQuiz(int publicationId) {

        DB_Helper dbHelper = new DB_Helper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////////////////////////////////////

        String table_name   = "quiz_data";
        String[] columnsArr = {"publication_id"};
        String[] data       = {"" +publicationId};

        // добавляем запись в БД, получая ее идентификатор на выходе
        int quizId = dbHelper.addRow(db, table_name, columnsArr, data);

        //////////////////////////////////////////////////////////////////

        // закрываем подключение к БД
        dbHelper.close();

        // возвращаем полученный идентификатор
        return quizId;
    }

    //
    private void addQuizAnswer(int quizId, String answer) {

        DB_Helper dbHelper = new DB_Helper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////////////

        String table_name   = "quiz_answer_data";

        String[] columnsArr = { "quiz_id",
                "answer" };

        String[] data       = { "" +quizId,
                answer };

        // добавляем запись в БД
        dbHelper.addRow(db, table_name, columnsArr, data);

        //////////////////////////////////////////////////////////////////

        // закрываем подключение к БД
        dbHelper.close();
    }

    //
    private void addPublicationImage(int publicationId, Uri imagePath, Float rotateDegree) {

        DB_Helper dbHelper = new DB_Helper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////////////

        String table_name   = "publication_image_data";

        String[] columnsArr = { "publication_id",
                "image_path",
                "rotate_degree" };

        String[] data       = { "" +publicationId,
                imagePath.toString(),
                "" +rotateDegree };

        // добавляем запись в БД
        dbHelper.addRow(db, table_name, columnsArr, data);

        //////////////////////////////////////////////////////////////////

        // закрываем подключение к БД
        dbHelper.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void showBadgesDialog() {

        // создаем "диалоговое окно для выбора бейджика"
        final Dialog dialog = new Dialog(Publication_Activity.this, R.style.InfoDialog_Theme);
        dialog.setContentView(R.layout.dialog_badges);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // создаем параметры отображения компонентов

        LinearLayout.LayoutParams badgesLP          = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        badgesLP.gravity = Gravity.LEFT;
        setMargins(badgesLP, 10, 0, 10, 5);

        LinearLayout.LayoutParams badgesContainerLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams imageLP           = new LinearLayout.LayoutParams(((int) (40 * density)), ((int) (40 * density)), 0.0f);
        imageLP.gravity = Gravity.CENTER_VERTICAL;

        LinearLayout.LayoutParams badgeTextLP       = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
        setMargins(badgeTextLP, 15, 10, 0, 0);

        LinearLayout.LayoutParams strutLP           = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ((int) (20 * density)), 1.0f);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // находим "представление с прокруткой"
        final ScrollView scrollViewSV = (ScrollView) dialog.findViewById(R.id.BadgesDialog_ScrollViewSV);

        // создаем "контейнер бейджиков"
        LinearLayout badgesContainer = new LinearLayout(context);
        badgesContainer.setLayoutParams(badgesContainerLP);
        badgesContainer.setOrientation(LinearLayout.VERTICAL);

        // получаем данные бейджиков
        badgesDataArrList = getBadgesData();

        // получаем кол-во бейджиков
        int badgesSum = badgesDataArrList.size();

        // в цикле собираем бейджики в "контейнер бейджиков"
        for(int i=0; i<badgesSum; i++) {

            // создаем "контейнер бейджа"
            final LinearLayout badgeLL = new LinearLayout(context);
            badgeLL.setLayoutParams(badgesLP);
            badgeLL.setOrientation(LinearLayout.HORIZONTAL);
            badgeLL.setBackgroundColor(context.getResources().getColor(R.color.white));
            badgeLL.setTag("badge_" + (i + 1));

            setPaddings(badgeLL, 5, 5, 5, 5);

            ///////////////////////////////////////////////////////////////////////////////////////

            // создаем "представление с изображением бейджа"
            CircleImageView badgeIV = new CircleImageView(context);
            badgeIV.setLayoutParams(imageLP);

            // задаем значение для "представления с изображение бейджа"
            String uri="@drawable/badge_" +(i+1);
            badgeIV.setImageResource(getResources().getIdentifier(uri, null, context.getPackageName()));

            ///////////////////////////////////////////////////////////////////////////////////////

            // создаем "представление с названием бейджа"
            TextView badgeTextTV = new TextView(context);
            badgeTextTV.setLayoutParams(badgeTextLP);
            badgeTextTV.setTextColor(getResources().getColor(R.color.user_name_blue));
            badgeTextTV.setTextSize(14);
            badgeTextTV.setTypeface(Typeface.DEFAULT_BOLD);
            badgeTextTV.setTag("badgeTextTV");

            // задаем значение для "представления с названием бейджа"
            String badgeText = badgesDataArrList.get(i)[1];
            badgeTextTV.setText(badgeText);

            // создаем распорку чтобы прижать элементы к левому краю "контейнера бейджа"
            View rightLineStrut = new View(context);
            rightLineStrut.setLayoutParams(strutLP);

            ///////////////////////////////////////////////////////////////////////////////////////

            // задаем обработчик щелчка по "контейнеру бейджа"
            badgeLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // меняем цветовое оформление для бейджиков при выборе одного из них
                    selectBadge(scrollViewSV, badgeLL.getTag().toString());

                    // меняем текст и изображение бейждика выбранного для публикации
                    changePublicationBadge();

                    // закрыть "диалоговое окно для выбора бейджика"
                    dialog.dismiss();
                }
            });

            ///////////////////////////////////////////////////////////////////////////////////////

            // добавляем компоненты в "контейнер бейджа"
            badgeLL.addView(badgeIV);
            badgeLL.addView(badgeTextTV);
            badgeLL.addView(rightLineStrut);

            // кладем "контейнер бейджа" в "контейнер бейджиков"
            badgesContainer.addView(badgeLL);
        }

        // кладем "контейнер бейджиков" в "представление с прокруткой"
        scrollViewSV.addView(badgesContainer);

        // создаем обработчик нажатия на "кнопку Закрыть"
        dialog.findViewById(R.id.BadgesDialog_CloseLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // закрыть "диалоговое окно для выбора бейджика"
                dialog.dismiss();
            }
        });

        // показываем сформированное диалоговое окно
        dialog.show();
    }

    //
    private ArrayList<String[]> getBadgesData() {

        DB_Helper dbHelper = new DB_Helper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////////////////////////////////////

        // создаем "список данных ответов опроса"
        ArrayList<String[]> resultArrList = new ArrayList<String[]>();

        // получаем данные из БД
        Cursor cursor = db.query("badge_data", null, null, null, null, null, null);

        // если данные получены
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // определяем номера столбцов по имени в выборке
            int badgeIdColIndex    = cursor.getColumnIndex("id");
            int badgeNameColIndex  = cursor.getColumnIndex("name");

            do {
                // создаем "массив данных ответа"
                String[] dataBlock = new String[2];

                // кладем "массив данных ответа" значения
                dataBlock[0] = cursor.getString(badgeIdColIndex);
                dataBlock[1] = cursor.getString(badgeNameColIndex);

                // добавляем очередной "массив данных ответа" в "список данных ответов опроса"
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
    private void selectBadge(ScrollView scrollViewSV, String selectedBadgeTag) {

        // задаем цвета для оформления бейждиков
        final int whiteColor    = context.getResources().getColor(R.color.white);
        final int orangeColor   = context.getResources().getColor(R.color.selected_item_orange);
        final int blueColor     = context.getResources().getColor(R.color.user_name_blue);

        // находим "контейнер бейджиков" в "представлении с прокруткой"
        LinearLayout badgesContainer = (LinearLayout) scrollViewSV.getChildAt(0);

        // получаем кол-во бейджиков
        int badgesSum = badgesContainer.getChildCount();

        // перебираем в цикле все "контейнеры бейджа" и меняем их оформление
        for(int i=0; i<badgesSum; i++) {

            // находим "контейнер бейджа"
            LinearLayout badgeLL = (LinearLayout) badgesContainer.getChildAt(i);

            // находим по тегу "представление с названием бейджа"
            TextView badgeTextTV = (TextView) badgeLL.findViewWithTag("badgeTextTV");

            // получаем тег "контейнера бейджа"
            String badgeLLTag = badgeLL.getTag().toString();

            // если тег "контейнера бейджа" совпадает с тегом "контейнер бейджа" который был выбран
            if(badgeLLTag.equals(selectedBadgeTag)) {

                // задаем оформление выбранного элемента списка

                // меняем цвет текста "представление с названием бейджа"
                badgeTextTV.setTextColor(whiteColor);

                // меняем фон "контейнера бейджа"
                badgeLL.setBackgroundColor(orangeColor);

                // запоминаем идентификатор выбранного бейджика
                badgeId = (i+1);

                // запоминаем название выбранного бейджика
                badgeText = badgeTextTV.getText().toString();
            }
            // если тег "контейнера бейджа" НЕ совпадает с тегом "контейнер бейджа" который был выбран
            else {

                // задаем оформление обычного элемента списка

                // меняем цвет текста "представление с названием бейджа"
                badgeTextTV.setTextColor(blueColor);

                // меняем фон "контейнера бейджа"
                badgeLL.setBackgroundColor(whiteColor);
            }
        }
    }

    //
    private void changePublicationBadge() {

        // находим "представление с названием бейджа" в активности и меняем его значение
        TextView badgeNameTV    = (TextView) findViewById(badgeNameResId);
        badgeNameTV.setText(badgeText);

        // находим "представление с изображение бейджа" в активности
        ImageView badgeImageIV  = (ImageView) findViewById(badgeImageResId);

        //  меняем его значение
        String uri="@drawable/badge_" +badgeId;
        badgeImageIV.setImageResource(getResources().getIdentifier(uri, null, context.getPackageName()));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void setLocationName() {

        // получаем данные местности
        ArrayList<String> locationData = getLocationData(new LatLng(latitude, longitude));

        // получаем кол-во полученных частей данных
        int locationDataSize = locationData.size();

        // отобразить данные в зависимости от количества фрагментов адреса объекта
        switch(locationDataSize) {

            // получен только город/область/страна
            case 1:
                // запоминаем название города/области/страны
                region_name = locationData.get(0).toString();
                break;
            // получены город/область/страна и название улицы
            case 2:
                // запоминаем название города/области/страны
                region_name = locationData.get(0).toString();

                // запоминаем название название улицы
                street_name = locationData.get(1).toString();
                break;
        }
    }

    //
    private ArrayList<String> getLocationData(LatLng point) {

        // создаем список для частей данных местоположения определенных по точке на карте
        ArrayList<String> list = new ArrayList<>();

        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            // получаем данные на основании заданных координат
            List<Address> addresses = geoCoder.getFromLocation(point.latitude, point.longitude, 1);

            // если данные получены
            if (addresses.size() > 0) {

                // сформировать результат в зависимости от полученного количества фрагментов названия объекта
                switch(addresses.get(0).getMaxAddressLineIndex()) {

                    case 2:
                        // вернуть название города
                        list.add(addresses.get(0).getAddressLine(0));
                        // address = addresses.get(0).getAddressLine(0);
                        break;
                    case 3:
                        // вернуть названия города и улицы
                        list.add(addresses.get(0).getAddressLine(1));
                        list.add(addresses.get(0).getAddressLine(0));
                        break;
                    case 4:
                        // вернуть названия города и улицы
                        list.add(addresses.get(0).getAddressLine(1));
                        list.add(addresses.get(0).getAddressLine(0));
                        break;
                    default:
                        // вернуть название города
                        list.add(addresses.get(0).getAddressLine(0));
                }
            }
            else {
                // вернуть текст вместо названия города и улицы
                list.add("Неизвестная область...");
                list.add("Неизвестная улица...");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // возвращаем результат
        return list;
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
        int paddingBottom = (int) (bottom * density);

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * загрузка сохраненных значений из Preferences
     */
    private void loadTextFromPreferences() {

        // если настройки содержат имя пользователя
        if(shPref.contains("user_id")) {
            // значит можно получить и его идентификатор
            userId = Integer.parseInt(shPref.getString("user_id", "0"));
        }
    }
}