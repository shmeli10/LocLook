package com.androiditgroup.loclook.user_profile_pkg;

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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androiditgroup.loclook.phone_number_pkg.PhoneNumber_Activity;
import com.androiditgroup.loclook.utils_pkg.Publication_Location_Dialog;
import com.androiditgroup.loclook.utils_pkg.DB_Helper;
import com.androiditgroup.loclook.utils_pkg.HLine_Fragment;
import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.publication_pkg.Publication_Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by OS1 on 17.09.2015.
 */
public class User_Profile_Activity  extends     FragmentActivity
                                    implements  View.OnClickListener,
                                                Publication_Fragment.OnPublicationInfoClickListener,
                                                Publication_Fragment.OnAnswersClickListener {

    private Context             context;
    private SharedPreferences   shPref;
    private DB_Helper           dbHelper;

    private final int arrowBackWrapLLResId      = R.id.UserProfile_ArrowBackWrapLL;
    private final int publicationWrapLLResId    = R.id.UserProfile_PublicationWrapLL;
    private final int titleTVResId              = R.id.UserProfile_TitleTV;
    private final int settingsIVResId           = R.id.UserProfile_SettingsWrapIV;

    private final int topBgLLResId              = R.id.UserProfile_TopBgLL;
    private final int userAvatarIVResId         = R.id.UserProfile_UserAvatarIV;
    private final int exitIVResId               = R.id.UserProfile_ExitWrapIV;
    private final int userNameTVResId           = R.id.UserProfile_UserNameTV;
    private final int aboutMeTVResId            = R.id.UserProfile_AboutMeTV;
    private final int userLocationTVResId       = R.id.UserProfile_UserLocationTV;
    private final int delimiterTVResId          = R.id.UserProfile_DelimiterTV;
    private final int siteTVResId               = R.id.UserProfile_SiteTV;
    private final int publicationsLLResId       = R.id.UserProfile_PublicationsLL;

    private int userId;
    private int answersUserId;
    private int profileUserId;
    private int selectedProvocationType;

    private float density;

    private String user_name        = "";
    private String bg_path          = "";
    private String avatar_path      = "";
    private String about_me         = "";
    private String site             = "";

    private String answersUserName  = "";
    private String profileUserName  = "";

    private Bitmap avatarBitmap;

    private TextView titleTV;
    private TextView userNameTV;
    private TextView aboutMeTV;
    private TextView userLocationTV;
    private TextView delimiterTV;
    private TextView siteTV;

    private ImageView settingsIV;
    private ImageView exitIV;

    private FragmentTransaction fragmentTransaction;

    private Publication_Location_Dialog publication_loc_dialog;

    // private OnDeletePublicationClickListener deletePublicationListener;

    // Intent intent;

    // интерфейс для работы с Tape_Activity
    /*public interface OnDeletePublicationClickListener {
        void onDeletePublicationClicked(String publicationId);
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        context = getApplicationContext();
        density = context.getResources().getDisplayMetrics().density;
        shPref  = context.getSharedPreferences("user_data", context.MODE_PRIVATE);

        loadTextFromPreferences();

        // определить переменную для работы с Preferences
        shPref = getSharedPreferences("user_data", MODE_PRIVATE);

        /////////////////////////////////////////////////////////////////////////////////////

        dbHelper = new DB_Helper(this);

        /////////////////////////////////////////////////////////////////////////////////////

        Intent intent = getIntent();

        answersUserId   = intent.getIntExtra("answers_userId", -1);
        answersUserName = intent.getStringExtra("answers_userName");

        // Log.d("myLogs","0_parentActivity: " +getParent().getLocalClassName().toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // если Tape_Activity выполняет интерфейс
/*        if ((Tape_Activity) Tape_Activity.class instanceof OnDeletePublicationClickListener)
            // получаем ссылку на Tape_Activity
            deletePublicationListener = (OnDeletePublicationClickListener) User_Profile_Activity.this;
        else
            throw new ClassCastException(User_Profile_Activity.this.toString() + " must implement OnDeletePublicationClickListener");*/

        /////////////////////////////////////////////////////////////////////////////////////

        titleTV         = (TextView) findViewById(titleTVResId);
        userNameTV      = (TextView) findViewById(userNameTVResId);
        aboutMeTV       = (TextView) findViewById(aboutMeTVResId);
        userLocationTV  = (TextView) findViewById(userLocationTVResId);
        delimiterTV     = (TextView) findViewById(delimiterTVResId);
        siteTV          = (TextView) findViewById(siteTVResId);

        (findViewById(arrowBackWrapLLResId)).setOnClickListener(this);
        (findViewById(publicationWrapLLResId)).setOnClickListener(this);

        settingsIV  = (ImageView) findViewById(settingsIVResId);

        exitIV      = (ImageView) findViewById(exitIVResId);
        exitIV.setOnClickListener(this);

        // если это просмотр чужого профиля
        if((answersUserName != null) && (!answersUserName.equals(""))) {
            profileUserId   = answersUserId;
            profileUserName = answersUserName;

            // скрываем кнопки настроек профиля и выхода из программы
            settingsIV.setVisibility(View.INVISIBLE);
            exitIV.setVisibility(View.INVISIBLE);
        }
        // если это просмотр своего профиля
        else {
            profileUserId   = userId;
            profileUserName = user_name;

            settingsIV.setOnClickListener(this);
        }

        setProfileUserData();

        setPublicationsData();
    }

    //
    public void onClick(View view) {

        Intent intent = null;

        switch(view.getId()) {

            // щелчок по "стрелке назад"
            case arrowBackWrapLLResId:
                                        finish();
                                        break;
            // щелчок по "карандашу"
            case publicationWrapLLResId:
                                        /*
                                        Intent intent = new Intent(this, Publication_Activity.class);
                                        startActivity(intent);
                                        */
                                        intent = new Intent(this, Publication_Activity.class);
                                        break;
            // щелчок по "значку настроек профиля"
            case settingsIVResId:
                                        // Toast.makeText(context, "Щелчок по значку настроек профиля", Toast.LENGTH_LONG).show();

                                        // осуществляем переход к настройкам профиля
                                        Intent settingsIntent = new Intent(this, User_Profile_Settings_Activity.class);

                                        settingsIntent.putExtra("user_name",    userNameTV.getText().toString());  // user_name);
                                        settingsIntent.putExtra("bg_path",      bg_path);
                                        settingsIntent.putExtra("avatar_path",  avatar_path);
                                        settingsIntent.putExtra("about_me",     aboutMeTV.getText().toString());   // about_me);
                                        settingsIntent.putExtra("site",         siteTV.getText().toString());      // site);

                                        startActivityForResult(settingsIntent, 0);
                                        break;
            // щелчок по "значку выхода из программы"
            case exitIVResId:
                                        // Toast.makeText(context, "Щелчок по значку выхода из программы", Toast.LENGTH_LONG).show();

                                        // удаляем из Preferences номер телефона текущего пользователя
                                        saveTextInPreferences("phone_number", "");

                                        // осуществляем выход из программы
                                        intent = new Intent(this, PhoneNumber_Activity.class);
                                        break;
        }

        if(intent != null)
            startActivity(intent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

                String userNameText     = data.getStringExtra("userName");
                String bgPathText       = data.getStringExtra("bgPath");
                String avatarPathText   = data.getStringExtra("avatarPath");
                int avatarRotateOn      = data.getIntExtra("avatarRotateOn", 0);
                String aboutMeText      = data.getStringExtra("aboutMe");
                String siteText         = data.getStringExtra("site");

                /*
                Log.d("myLogs1", "userNameText = "   +userNameText);
                Log.d("myLogs1", "avatarPathText = " +avatarPathText);
                Log.d("myLogs1", "aboutMeText = "    +aboutMeText);
                Log.d("myLogs1", "siteText = "       +siteText);
                */

                // кладем имя пользователя в представвления (заголовок, имя пользователя)
                titleTV.setText(userNameText);
                userNameTV.setText(userNameText);

                // если путь к фону профиля получен
                if((bgPathText != null) && (!bgPathText.equals("")))
                    // кладем его в представление
                    setBackground(bgPathText);

                // если путь к аватару пользователя получен
                if((avatarPathText != null) && (!avatarPathText.equals("")))
                    // кладем его в представление
                    // setUserAvatar(avatarPathText);
                    setUserAvatar(avatarPathText, avatarRotateOn);

                // кладем текст описание в представление
                aboutMeTV.setText(aboutMeText);

                // если теккс сайта получен
                if(siteText.equals(""))
                    // отображаем разделитель между местоположением пользователя и адресом его сайта
                    delimiterTV.setVisibility(View.INVISIBLE);

                // кладем адрес сайта пользователя в представление
                siteTV.setText(siteText);
            }
        }
    }

    @Override
    public void onAnswersClicked() {
        try {
            if(publication_loc_dialog != null)
                publication_loc_dialog.getDialog().dismiss();

            // меняем цвет контейнера с элемнетами поддержки
            // moveToAnswersActivity();
        }
        catch(Exception exc) {}
    }

    @Override
    public void onPublicationInfoClicked(String publicationId, int publicationUserId, final String publicationText, final float latitude, final float longitude, final String regionName, final String streetName, Publication_Fragment publicationFragment) {

        // создаем диалоговое окно
        final Dialog dialog = new Dialog(User_Profile_Activity.this, R.style.InfoDialog_Theme);
        dialog.setContentView(R.layout.dialog_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // создаем обработчик нажатия в окне кнопки "Где это?"
        dialog.findViewById(R.id.InfoDialog_WhereIsItLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showPublicationLocationDialog(latitude, longitude, regionName, streetName);
            }
        });

        // создаем обработчик нажатия в окне кнопки "Поделиться"
        dialog.findViewById(R.id.InfoDialog_ShareLL).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                dialog.dismiss();
                shareTo(publicationText);
            }
        });

        // находим контейнер и кладем в него нужную кнопку, с обработчиком клика по ней
        ((LinearLayout) dialog.findViewById(R.id.InfoDialog_OwnButtonLL)).addView(getOwnButtonLL(dialog, (publicationUserId == userId), publicationId, publicationFragment));

        // создаем обработчик нажатия в окне кнопки "Закрыть"
        dialog.findViewById(R.id.InfoDialog_CloseLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // показываем сформированное диалоговое окно
        dialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void setProfileUserData() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ////////////////////////////////////////////////////////////////

        String[] profileUserDataArr = dbHelper.getProfileUserData(db, "" +profileUserId);

        if(profileUserDataArr.length > 0) {

            // кладем полученное имя пользователя в переменную
            user_name = profileUserDataArr[0];

            // кладем имя пользователя в заголовок и в текстовое представление имени пользователя
            titleTV.setText(user_name);

            // ((TextView) findViewById(userNameTVResId)).setText(profileUserDataArr[0]);
            userNameTV.setText(user_name);

            ////////////////////////////////////////////////////////////////

            // Log.d("myLogs2", "User_Profile_Activity:setProfileUserData():avatarPath = " + profileUserDataArr[1]);

            // задаем фон профиля
            setBackground(profileUserDataArr[1]);

            ////////////////////////////////////////////////////////////////

            // задаем аватар пользователя
            setUserAvatar(profileUserDataArr[2], 0);

            ////////////////////////////////////////////////////////////////

            // кладем полученное текстовое описание "Обо мне" в переменную
            about_me = profileUserDataArr[3];

            // если текстовое описание "Обо мне" получено
            if((about_me != null) && (!about_me.equals("")))
                // кладем текстовое описание "Обо мне" в текстовое представление
                aboutMeTV.setText(about_me);

            ////////////////////////////////////////////////////////////////

            // получаем текстовое описание местоположения пользователя
            String userLocationText = profileUserDataArr[4];

            // если текстовое описание местоположения пользователя получено
            if((userLocationText != null) && (!userLocationText.equals("")))
                // кладем текстовое описание местоположения пользователя в текстовое представление
                userLocationTV.setText(userLocationText);

            ////////////////////////////////////////////////////////////////

            // кладем полученный адрес сайта пользователя в переменную
            site = profileUserDataArr[6];

            // задаем значение для разделителя
            delimiterTV.setText(" - ");

            // если адрес сайта пользователя получено
            if((site != null) && (!site.equals(""))) {
                // кладем URL сайта в текстовое представление и делаем разделитель видимым
                siteTV.setText(site);
                delimiterTV.setVisibility(View.VISIBLE);
            }
            // если адрес сайта пользователя не получен
            else
                // скрываем разделитель
                delimiterTV.setVisibility(View.INVISIBLE);
        }
    }

    //
    private void setPublicationsData() {

        // получаем массив с данными сделанных пользователем публикаций
        ArrayList<String[]> publicationsDataList = getPublicationsData();

        // получаем кол-во публикаций
        int publicationsSum = publicationsDataList.size();

        // если публикации есть
        if(publicationsSum > 0) {

            // "контейнер публикаций" чистим от прежних элементов
            ((LinearLayout) findViewById(publicationsLLResId)).removeAllViews();

            // проходим циклом по массиву данных публикаций
            for(int i=0; i<publicationsSum; i++) {

                // если автор публикации не скрыт за словом "Анонимно"
                if(publicationsDataList.get(i)[4].equals("false")) {

                    // формируем под созданный ответ новый фрагмент
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Publication_Fragment publicationFragment = new Publication_Fragment();

                    //////////////////////////////////////////////////////////////////////////////////////////

                    // меняем формат представления даты публикации
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm");
                    long dateLong = Long.parseLong(publicationsDataList.get(i)[3]);
                    Date now = new Date(dateLong);
                    String dateStr = sdf.format(now);

                    // передаем фрагменту основные данные для их отображения в нем
                    publicationFragment.setListItemPosition(i);
                    publicationFragment.setAvatarBitmap(avatarBitmap);
                    publicationFragment.setPublicationUserId(profileUserId);
                    publicationFragment.setUserName(profileUserName);
                    publicationFragment.setPublicationId(publicationsDataList.get(i)[0]);
                    publicationFragment.setPublicationText(publicationsDataList.get(i)[2]);
                    publicationFragment.setPublicationTimeAgoText(dateStr);
                    publicationFragment.setBadgeId(publicationsDataList.get(i)[6]);
                    // publicationFragment.setIsAuthor(isAuthor);

                    // если получено значение-флаг для вкл/выкл. звездочки
                    if((publicationsDataList.get(i)[7] != null) && (!publicationsDataList.get(i)[7].equals("")))
                        // передаем идентификатор строки в таблице БД, которая содержит об этом информацию
                        publicationFragment.setFavoritePublicationRowId(Integer.parseInt(publicationsDataList.get(i)[7]));

                    // передаем фрагменту цифровые данные для их отображения в нем
                    publicationFragment.setAnswersSum(Integer.parseInt(publicationsDataList.get(i)[8]));
                    publicationFragment.setLikedSum(Integer.parseInt(publicationsDataList.get(i)[9]));

                    // если получено значение-флаг для вкл/выкл. сердечка
                    if((publicationsDataList.get(i)[10] != null) && (!publicationsDataList.get(i)[10].equals("")))
                        // передаем идентификатор строки в таблице БД, которая содержит об этом информацию
                        publicationFragment.setLikedPublicationRowId(Integer.parseInt(publicationsDataList.get(i)[10]));

                    // передаем фрагменту координаты местонахождения пользователя
                    publicationFragment.setLatitude(Float.parseFloat(publicationsDataList.get(i)[11]));
                    publicationFragment.setLongitude(Float.parseFloat(publicationsDataList.get(i)[12]));

                    // передаем фрагменту текстовое представление местонахождения пользователя
                    publicationFragment.setRegionName(publicationsDataList.get(i)[13]);
                    publicationFragment.setStreetName(publicationsDataList.get(i)[14]);

                    //////////////////////////////////////////////////////////////////////////////////////////

                    // добавляем фрагменты в заданный контейнер
                    ft.add(publicationsLLResId, new HLine_Fragment());
                    ft.add(publicationsLLResId, publicationFragment);
                    ft.commit();
                }
            }
        }

        /*
        PackageInfo info = null;

        try {
            info = getPackageManager().getPackageInfo("LocLook", PackageManager.GET_ACTIVITIES); // context.getResources().getString(R.string.app_name)
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        for( ActivityInfo activity : info.activities)
            Log.d("myLogs","1_parentActivity: " + activity.toString());
        */

        // intent.getParentActivityIntent().toString());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private ArrayList<String[]> getPublicationsData() {

        ArrayList<String[]> resultArrList = new ArrayList<String[]>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor;

        ////////////////////////////////////////////////////////////////////////////////////

        // dbHelper.clearTable(db, "publication_data");

        // dbHelper.clearTable(db,  "liked_publication_data");
        // dbHelper.clearTable(db,  "favorite_publication_data");
        // dbHelper.clearTable(db,  "publication_answer_data");
        // dbHelper.clearTable(db,  "claim_data");

        // dbHelper.showAllTableData(db,"publication_data");

        // соберем в переменной SQL-запрос к БД
        StringBuilder sqlQuery = new StringBuilder( "select * " +
                                                    "  from publication_data" +
                                                    " where user_id = ?     " +
                                                    " order by enter_date DESC ");

        // отправляем запрос БД и получаем его результат
        cursor = db.rawQuery(sqlQuery.toString(), new String[]{"" +profileUserId});

        // если данные в запросе получены
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // определяем номера столбцов по имени в выборке
            int publicationIdColIndex           = cursor.getColumnIndex("id");
            int publicationAuthorIdColIndex     = cursor.getColumnIndex("user_id");
            int publicationTextColIndex         = cursor.getColumnIndex("enter_text");
            int publicationDateColIndex         = cursor.getColumnIndex("enter_date");
            int publicationAnonymousColIndex    = cursor.getColumnIndex("anonymous");
            int publicationHasQuizColIndex      = cursor.getColumnIndex("quiz_added");
            int publicationBadgeIdColIndex      = cursor.getColumnIndex("badge_id");
            int publicationLatitudeColIndex     = cursor.getColumnIndex("map_latitude");
            int publicationLongitudeColIndex    = cursor.getColumnIndex("map_longitude");
            int publicationRegionNameColIndex   = cursor.getColumnIndex("region_name");
            int publicationStreetNameColIndex   = cursor.getColumnIndex("street_name");

            // формируем массив публикаций
            do {
                // float publicationLatitude  = Float.parseFloat("" +cursor.getString(cursor.getColumnIndex("map_latitude")));
                // float publicationLongitude = Float.parseFloat("" +cursor.getString(cursor.getColumnIndex("map_longitude")));

                String[] dataBlock = new String[15];

                int publicationId = Integer.parseInt(cursor.getString(publicationIdColIndex));

                dataBlock[0] = "" +publicationId;
                dataBlock[1] = cursor.getString(publicationAuthorIdColIndex);
                dataBlock[2] = cursor.getString(publicationTextColIndex);
                dataBlock[3] = cursor.getString(publicationDateColIndex);
                dataBlock[4] = cursor.getString(publicationAnonymousColIndex);
                dataBlock[5] = cursor.getString(publicationHasQuizColIndex);
                dataBlock[6] = cursor.getString(publicationBadgeIdColIndex);

                // если публикация была отмечена пользователем для избранного
                // получим идентификатор строки которая содержит об этом информацию
                dataBlock[7] = "" +getRowId(1, publicationId);

                // получаем кол-во ответов пользователей к данной публикация
                dataBlock[8] = "" +getRowsSum(1, publicationId);

                // получаем кол-во пользователей, которым понравилась публикация
                dataBlock[9] = "" +getRowsSum(2, publicationId);

                // если публикация была поддержана пользователем
                // получим идентификатор строки которая содержит об этом информацию
                dataBlock[10] = "" +getRowId(2, publicationId);

                dataBlock[11] = "" +cursor.getString(publicationLatitudeColIndex);
                dataBlock[12] = "" +cursor.getString(publicationLongitudeColIndex);

                dataBlock[13] = "" +cursor.getString(publicationRegionNameColIndex);
                dataBlock[14] = "" +cursor.getString(publicationStreetNameColIndex);

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
    public int getRowId(int flagId, int publicationId) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // задаем имя таблицы
        String table_name = "";

        // создаем переменную в которой будем хранить результат
        int rowId = 0;

        //////////////////////////////////////////////////////////

        switch(flagId) {

            // получение данных из таблицы
            case 1:
                table_name = "favorite_publication_data";
                break;
            case 2:
                table_name = "liked_publication_data";
                break;
        }

        // создаем массив значений, с названиями столбцов таблицы, которые хотим получить
        String[] columns        = new String[]{"id"};

        // создаем строку-условие
        String selection        = "publication_id = ? AND user_id = ?";

        // создаем массив-значений, для строки-условия
        String[] selectionArgs  = new String[]{"" +publicationId, "" +profileUserId};

        // пытаемся получить значения на основании заданных условий
        Cursor cursor = db.query(table_name, columns, selection, selectionArgs, null, null, null);

        // если данные были получены
        if(cursor.getCount() > 0) {
            // перемещаемся к первой строке данных
            cursor.moveToFirst();

            // получаем идентификатор искомой записи
            rowId = cursor.getInt(cursor.getColumnIndex("id"));
        }

        // закрываем курсор
        cursor.close();

        // закрываем подключение к БД
        dbHelper.close();

        // возвращаем результат
        return rowId;
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

            // получение данных из таблицы
            case 1:
                    table_name = "publication_answer_data";
                    break;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void showPublicationLocationDialog(float latitude, float longitude, String regionName, String streetName) {

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
                publication_loc_dialog.show(getFragmentManager(), "pub_loc_dialog_user_profile");
            }
        }
        catch(Exception exc) {
            Log.d("myLogs", "User_Profile_Activity: showPublicationLocationDialogError! " + exc.getStackTrace());
        }
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
    private LinearLayout getOwnButtonLL(final Dialog dialog, boolean isAuthorOfThisPost, final String publicationId, final Publication_Fragment publicationFragment) {

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
                    showDeleteDialog(publicationId, publicationFragment);
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
                    dialog.dismiss();

                    //
                    showClaimDialog(publicationId);
                }
            });
        }

        // возвращаем оранжевый контейнер-кнопку
        return orangeTextViewLL;
    }

    //
    private void showDeleteDialog(final String publicationId, final Publication_Fragment publicationFragment) {

        // создаем "диалоговое окно удаленя публикации"
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(User_Profile_Activity.this);

        deleteDialog.setTitle(context.getResources().getString(R.string.deleting_publication_text));        // заголовок
        deleteDialog.setMessage(context.getResources().getString(R.string.delete_publication_answer_text)); // сообщение

        deleteDialog.setPositiveButton(context.getResources().getString(R.string.yes_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                // удаляем публикацию из БД
                deletePublication(publicationId);

                // удаляем публикацию из контейнера фрагментов
                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.remove(publicationFragment);
                fragmentTransaction.commit();

                // удаляем публикацию из ленты
                // deletePublicationListener.onDeletePublicationClicked(publicationId);

                // сигнализируем о том, что данную публикацию надо убрать из ленты
                // deletePublication = true;

                // удаление публикации из ленты
                // deletePublicationFromTape("" +publicationId);
            }
        });

        deleteDialog.setNegativeButton(context.getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });

        deleteDialog.setCancelable(true);

        deleteDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) { }
        });

        deleteDialog.show();
    }

    //
    private void deletePublication(String publicationId) {

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
    private void showClaimDialog(final String publication_id) {

        selectedProvocationType = 0;

        // создаем "диалоговое окно отправки жалобы"
        final Dialog dialog = new Dialog(context, R.style.InfoDialog_Theme);
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
                // if(chBox.isChecked())
                // сигнализируем об этом
                // deletePublication = true;

                // удаляем публикацию из ленты
                // deletePublicationFromTape("" +publication_id);

                // закрываем "диалоговое окно отправки жалобы"
                dialog.dismiss();
            }
        });

        // показываем сформированное "диалоговое окно отправки жалобы"
        dialog.show();
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
    private void sendClaim(String publication_id, boolean hidePublication){

        DB_Helper dbHelper = new DB_Helper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////////////

        // если пользователь не выбрал ни один из пунктов "списка типов жалоб"
        if(selectedProvocationType == 0)
            // считаем что пользователь пожаловался на "Спам"
            selectedProvocationType++;

        // формируем параметры добавления жалобы в таблицу "типы жалоб"
        String table_name   = "claim_data";

        String[] columnsArr = { "publication_id",
                                "provocation_type_id",
                                "claim_user_id",
                                "claim_date",
                                "hide_publication" };

        String[] data = {   publication_id,
                            "" +selectedProvocationType,
                            "" +userId,
                            "" +System.currentTimeMillis(),
                            "" +hidePublication };

        // добавляем запись в БД
        dbHelper.fillTable(db, table_name, columnsArr, data);

        // закрываем подключение к БД
        dbHelper.close();
    }

    //
    private void setBackground(String bg_path) {

        this.bg_path = bg_path;

        // если путь получен
        if ((bg_path != null) && (!bg_path.equals(""))) {

            Bitmap bgBitmap = decodeSampledBitmapFromResource(bg_path, 200, 200);

            BitmapDrawable bgDrawable = new BitmapDrawable(bgBitmap);

            // кладем фон в представление
            findViewById(topBgLLResId).setBackground(bgDrawable);
        }
    }

    //
    // private void setUserAvatar(String avatar_path) {
    private void setUserAvatar(String avatar_path, int avatarRotateOn) {

        this.avatar_path = avatar_path;

        // если путь получен
        if ((avatar_path != null) && (!avatar_path.equals(""))) {

            Matrix matrix = new Matrix();
            matrix.postRotate(avatarRotateOn);

            Bitmap bitmapToRotate = decodeSampledBitmapFromResource(avatar_path, 200, 200);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapToRotate, 0, 0, bitmapToRotate.getWidth(), bitmapToRotate.getHeight(), matrix, true);

            ((ImageView) findViewById(userAvatarIVResId)).setImageBitmap(rotatedBitmap);
        }
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

    //////////////////////////////////////////////////////////////////////////////////////////

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
     * сохранение заданных значений в Preferences
     * @param field - поле
     * @param value - значение
     */
    private void saveTextInPreferences(String field, String value) {
        SharedPreferences.Editor ed = shPref.edit();
        ed.putString(field,value);
        ed.commit();
    }

    //
    private void loadTextFromPreferences() {

        // если настройки содержат идентификатор пользователя
        if (shPref.contains("user_id")) {
            // получаем значение
            userId = Integer.parseInt(shPref.getString("user_id", "0"));
        }

        // если настройки содержат имя пользователя
        if(shPref.contains("nick_name")) {
            // получаем значение
            user_name = shPref.getString("nick_name", "");
        }

        // если настройки содержат путь к фону профиля
        if(shPref.contains("bg_path")) {
            // получаем значение
            avatar_path = shPref.getString("bg_path", "");
        }

        // если настройки содержат путь к аватару пользователя
        if(shPref.contains("avatar_path")) {
            // получаем значение
            avatar_path = shPref.getString("avatar_path", "");
        }

        // если настройки содержат текст "Обо мне"
        if(shPref.contains("about_me")) {
            // получаем значение
            about_me = shPref.getString("about_me", "");
        }

        // если настройки содержат адрес сайта
        if(shPref.contains("site")) {
            // получаем значение
            site = shPref.getString("site", "");
        }

/*        // если настройки содержат название региона пользователя
        if(shPref.contains("region_name")) {
            // значит можно получить значение
            region_name = shPref.getString("region_name", "");
        }*/
    }
}