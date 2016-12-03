package com.androiditgroup.loclook.phone_number_pkg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.androiditgroup.loclook.sms_code_pkg.SMSCode_Activity;
import com.androiditgroup.loclook.utils_pkg.DB_Helper;
import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.user_name_pkg.UserName_Activity;


public class PhoneNumber_Activity extends     Activity
                                    implements  View.OnClickListener {

    private Context             context;
    private DB_Helper           dbHelper;
    private SharedPreferences   shPref;

    private EditText            phoneBodyET;
    private Button              enterButton;

    private String              userId   = "";

    // private String   login       = "";
    // private String   latitude    = "0";
    // private String   longitude   = "0";
    // private String   radius      = "0";
    // private String   region_name = "";
    // private String   street_name = "";

    private int phoneNumberETResId  = R.id.Phone_Number_PhoneBodyET;
    private int enterBTNResId       = R.id.Phone_Number_EnterBTN;

    private final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // задать файл-компоновщик элементов окна
        setContentView(R.layout.activity_phone_number);

        //////////////////////////////////////////////////////////////////////////////////

        context = this;

        // создать объект для работы с БД
        dbHelper = new DB_Helper(context);

        //////////////////////////////////////////////////////////////////////////////////

        // поле "Номер телефона"
        phoneBodyET = (EditText) findViewById(phoneNumberETResId);

        // кнопка "ВОЙТИ"
        enterButton = (Button) findViewById(enterBTNResId);
        enterButton.setOnClickListener(this);

        //////////////////////////////////////////////////////////////////////////////////

        // определить переменную для работы с Preferences
        shPref = getSharedPreferences("user_data", MODE_PRIVATE);

        // подгрузить данные из Preferences
        loadTextFromPreferences();
    }

    /**
     * обработка внезапного закрытия окна или приложения
     */
    @Override
    protected void onDestroy() {
        // номер телефона сохранить в Preferences
        saveTextInPreferences("phone_number", phoneBodyET.getText().toString());
        super.onDestroy();
    }

    /**
     * обработка щелчка по кнопке "Вперед"
     */
    @Override
    public void onClick(View v) {

        // меняем оформление кнопки
        // changeEnterBtnColor();

        // если номер телефона при этом не был введен
        if((phoneBodyET.length() == 0) || (phoneBodyET.length() < 10))
            // стоп
            return;

        // сохраняем введенный номер телефона в Preferences
        saveTextInPreferences("phone_number", phoneBodyET.getText().toString());

        // двигаемся к следущему окну приложения
        moveForward(userExists());

        //////////////////////////////////////////////////////////////////////////

        /*
        // определяем переменную для осуществления перехода между окнами
        // переход по-умолчанию - окно "СМС Код"
        Intent intent = new Intent(this, SMSCode_Activity.class);

        // получаем данные пользователя из БД,
        // на основании введенного номера телефона
        String[] userDataArr = getUserData();

        if(userDataArr.length > 0) {

            // если ID пользователя получен
            if (userDataArr[0] != null)
                // сконвертировать его в Integer и сохранить в переменной
                userId = Integer.parseInt(userDataArr[0]);

            // если Login пользователя получен
            if (userDataArr[1] != null)
                // сохранить его в переменной
                login = userDataArr[1];

            ///////////////////////////////////////////////////////////////////////

            // если широта получена
            if (userDataArr[2] != null)
                // сохранить его в переменной
                latitude = userDataArr[2];

            // если долгота получена
            if (userDataArr[3] != null)
                // сохранить его в переменной
                longitude = userDataArr[3];

            // если радиус получен
            if (userDataArr[4] != null)
                // сохранить его в переменной
                radius = userDataArr[4];

            // если название города/области/страны получено
            if (userDataArr[5] != null)
                // сохранить его в переменной
                region_name = userDataArr[5];

            // если название улицы получено
            if (userDataArr[6] != null)
                // сохранить его в переменной
                street_name = userDataArr[6];
        }

//        // если ID пользователя получен
//        if (userDataArr[0] != null)
//            // сконвертировать его в Integer и сохранить в переменной
//            userId = Integer.parseInt(userDataArr[0]);
//
//        // если Login пользователя получен
//        if (userDataArr[1] != null)
//            // сохранить его в переменной
//            login = userDataArr[1];
//
//        ///////////////////////////////////////////////////////////////////////
//
//        // если широта получена
//        if (userDataArr[2] != null)
//            // сохранить его в переменной
//            latitude = userDataArr[2];
//
//        // если долгота получена
//        if (userDataArr[3] != null)
//            // сохранить его в переменной
//            longitude = userDataArr[3];
//
//        // если радиус получен
//        if (userDataArr[4] != null)
//            // сохранить его в переменной
//            radius = userDataArr[4];
//
//        // если название города/области/страны получено
//        if (userDataArr[5] != null)
//            // сохранить его в переменной
//            region_name = userDataArr[5];
//
//        // если название улицы получено
//        if (userDataArr[6] != null)
//            // сохранить его в переменной
//            street_name = userDataArr[6];

        ///////////////////////////////////////////////////////////////////////

        // если в результате запроса к БД, стало известно что пользователь в ней есть
        if (userId > 0) {
            // инициировать генерацию для него временного пароля
//            enterCode = getEnterCode(userId);
//            enterCode = getRandomValue();

            // если пароль получен
//            if (enterCode > 0)
            if(setEnterCode(userId))
                // сохранить его в Preferences
                saveTextInPreferences("enter_code", "" + enterCode);

            // задать флаг в Preferences, что это не новый пользователь
            saveTextInPreferences("new_user", "N");
        }
        // если в результате запроса к БД, стало известно что этого пользователя в ней нет
        else {
                // задать флаг в Preferences, что это новый пользователь
                saveTextInPreferences("new_user", "Y");

                // задать окно для перехода "Ввод имени пользователя"
                intent = new Intent(this, UserName_Activity.class);
        }

        // сохранить данные пользователя в Preferences
        saveTextInPreferences("user_id",        "" +userId);
        saveTextInPreferences("nick_name",      "" +login);
        saveTextInPreferences("phone_number",   phoneBodyET.getText().toString());

        saveTextInPreferences("map_latitude",   "" +latitude);
        saveTextInPreferences("map_longitude",  "" +longitude);
        saveTextInPreferences("map_radius",     "" +radius);

        saveTextInPreferences("region_name",    "" +region_name);
        saveTextInPreferences("street_name",    "" +street_name);

        // осуществить переход к заданному окну
        startActivity(intent);
        */
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    //
/*    private void changeEnterBtnColor() {

        enterButton.setBackgroundResource(R.drawable.rounded_rect_button_orange);
        enterButton.setTextColor(context.getResources().getColor(R.color.white));

        // Execute some code after 2 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                enterButton.setBackgroundResource(R.drawable.rounded_rect_button_white);
                enterButton.setTextColor(context.getResources().getColor(R.color.orange));
            }
        }, 1000);
    }*/

    /////////////////////////////////////////////////////////////////////////////////////////

    private boolean userExists() {

        // настраиваем подключение к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // возвращаем ответ от БД
        return dbHelper.userExists(db, phoneBodyET.getText().toString());
    }

    private boolean setUserId() {

        boolean result = false;

        // настраиваем подключение к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // получаем ответ от БД
        userId = dbHelper.getUserId(db,phoneBodyET.getText().toString());

        // если идентификатор пользователя получен из БД
        if((userId != null) && (!userId.equals(""))) {

            // сохранить данные пользователя в Preferences
            saveTextInPreferences("user_id", "" +userId);

            // вернем ответ, что установить идентификатор получилось
            result = true;
        }

        Log.d(LOG_TAG, "PhoneNumber_Activity: setUserId(): result=" + result);

        // возвращам результат
        return result;
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * инициирование генерации временного пароля для входа пользователя
     * @return числовое значение временного пароля для входа
     */
    private boolean setEnterCode() {

        boolean result = false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // получаем код авторизации
        int enterCode = getRandomValue();

        // если код авторизации получен
        if(enterCode > 0) {

            // сохранить код в Preferences
            saveTextInPreferences("user_auth_code", "" + enterCode);

            // сохраняем значение в БД
            dbHelper.setEnterCode(db, userId, enterCode);

            // код авторизации получен
            result = true;
        }

        // возвращаем результат
        return result;
    }

    //
    private int getRandomValue() {

        int minVal = 111111;
        int maxVal = 999999;

        return (minVal + (int)(Math.random() * ((maxVal - minVal) + 1)));
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    //
    // private void moveForward(String success) {
    private void moveForward(boolean userExists) {

        Intent intent = null;

        Log.d(LOG_TAG, "PhoneNumber_Activity: moveForward(): userExists=" + userExists);

        // если это существующий в БД пользователь
        if(userExists) {

            Log.d(LOG_TAG, "PhoneNumber_Activity: moveForward(): user exists");

            // задать флаг в Preferences, что это НЕ новый пользователь
            saveTextInPreferences("new_user", "N");

            // если идентификатор пользователя не получен из Preferences
            if((userId == null) || (userId.equals(""))) {

                Log.d(LOG_TAG, "PhoneNumber_Activity: moveForward(): userId is null OR userId is empty");

                // если идентификатор не удалось получить из БД и установить в переменную
                if(!setUserId()) {

                    Log.d(LOG_TAG, "PhoneNumber_Activity: moveForward(): userId получить из БД не удалось!");

                    // стоп
                    return;
                }
            }

            Log.d(LOG_TAG, "PhoneNumber_Activity: moveForward(): userId=" + userId);

            // если код авторизации не получен
            if(!setEnterCode()) {

                Log.d(LOG_TAG, "PhoneNumber_Activity: moveForward(): получить код авторизации не удалось!");

                // стоп
                return;
            }

            Log.d(LOG_TAG, "PhoneNumber_Activity: moveForward(): код авторизации получен");

            // осуществляем переход к "окну ввода кода доступа"
            intent = new Intent(PhoneNumber_Activity.this, SMSCode_Activity.class);
        }
        // если пользователь еще не существует в БД
        else {

            Log.d(LOG_TAG, "PhoneNumber_Activity: moveForward(): new user");

            // задать флаг в Preferences, что это новый пользователь
            saveTextInPreferences("new_user", "Y");

            // осуществляем переход к "окну ввода имени пользователя"
            intent = new Intent(PhoneNumber_Activity.this, UserName_Activity.class);
        }

        // если переход задан
        if(intent != null)
            // осуществляем его
            startActivity(intent);
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * сохранение заданных значений в Preferences
     * @param field - поле
     * @param value - значение
     */
    private void saveTextInPreferences(String field, String value) {
        Editor ed = shPref.edit();
        ed.putString(field,value);
        ed.commit();
    }

    /**
     * загрузка сохраненных значений из Preferences
     */
    private void loadTextFromPreferences() {
        // если параметр существует
        if (shPref.contains("phone_number"))
            // получить из него значение
            phoneBodyET.setText(shPref.getString("phone_number", ""));

        // если параметр существует
        if (shPref.contains("user_id"))
            // получаем его значение
            userId = shPref.getString("user_id", "");
    }
}