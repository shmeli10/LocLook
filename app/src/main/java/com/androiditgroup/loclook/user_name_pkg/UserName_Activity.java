package com.androiditgroup.loclook.user_name_pkg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.androiditgroup.loclook.phone_number_pkg.PhoneNumber_Activity;
import com.androiditgroup.loclook.utils_pkg.DB_Helper;
import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.sms_code_pkg.SMSCode_Activity;

/**
 * Created by admin on 13.09.2015.
 */
public class UserName_Activity extends     Activity
                                implements  View.OnClickListener {

    private Context             context;
    private DB_Helper           dbHelper;
    private SharedPreferences   shPref;

    private EditText            userNameET;

    private String              userId          = "";
    private String              userPhone       = "";
    private String              enteredUserName = "";

    private final int userNameETResId   = R.id.UserName_UserNameET;
    private final int continueIVResId   = R.id.UserName_ContinueIV;
    private final int backIVResId       = R.id.UserName_BackIV;

    private final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // задать файл-компоновщик элементов окна
        setContentView(R.layout.activity_user_name);

        //////////////////////////////////////////////////////////////////////////////////

        context = this;

        // создать объект для работы с БД
        dbHelper = new DB_Helper(context);

        //////////////////////////////////////////////////////////////////////////////////

        // определить переменную для работы с полем "Имя пользователя"
        userNameET  = (EditText) findViewById(userNameETResId);
        userNameET.setText(enteredUserName);

        //////////////////////////////////////////////////////////////////////////////////

        // определить переменную для работы с Preferences
        shPref = getSharedPreferences("user_data", MODE_PRIVATE);

        // подгрузить данные из Preferences
        loadTextFromPreferences();

        //////////////////////////////////////////////////////////////////////////////////

        findViewById(continueIVResId).setOnClickListener(this);
        findViewById(backIVResId).setOnClickListener(this);
    }

    /**
     * обработка внезапного закрытия окна или приложения
     */
    @Override
    protected void onDestroy() {
        // имя пользователя сохранить в Preferences
        saveTextInPreferences("user_name", userNameET.getText().toString());
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case continueIVResId:
                                    // получаем имя введенное пользователем
                                    enteredUserName = userNameET.getText().toString();

                                    // если имя пользователя не было введено или отсутствует номер телефона
                                    if((enteredUserName.equals("")) || (userPhone.equals("")))
                                        // стоп
                                        return;

                                    // сохраняем введенное имя пользователя в Preferences
                                    saveTextInPreferences("user_name",   enteredUserName);

                                    // если пользователь создан
                                    if(createUser()) {

                                        // если код авторизации получен
                                        if(setEnterCode())
                                            // переходим вперед
                                            moveForward();
                                        // если код авторизации получить не удалось
                                        else
                                            // переходим обратно
                                            moveBack();
                                    }

                                    break;
            case backIVResId:

                                    // сохраняем введенное имя пользователя в Preferences
                                    saveTextInPreferences("user_name", userNameET.getText().toString());

                                    // переходим обратно
                                    moveBack();
                                    break;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private void moveForward() {

        // переход к "окну ввода кода доступа"
        Intent intent = new Intent(UserName_Activity.this, SMSCode_Activity.class);

        // осуществить переход к заданному окну
        startActivity(intent);
    }

    private void moveBack() {

        // переход к "окну ввода номера телефона"
        Intent intent = new Intent(UserName_Activity.this, PhoneNumber_Activity.class);

        // осуществить переход к заданному окну
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
        if (shPref.contains("user_id"))
            // получаем его значение
            userId = shPref.getString("user_id", "");

        // если параметр существует
        if(shPref.contains("phone_number"))
            // получить из него значение
            userPhone = shPref.getString("phone_number", "");

        // если параметр существует
        if(shPref.contains("user_name")) {
            // получить из него значение
            enteredUserName = shPref.getString("user_name", "");

            // получить из него значение
            userNameET.setText(enteredUserName);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * создание нового пользователя в БД и получение его данных
     * @return массив значений с данными пользователя (id,login)
     */
    private boolean createUser() {

        boolean result = false;

        // настраиваем подключение к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // создаем нового пользователя в БД и получаем его идентификатор
        userId = dbHelper.createUser(db,enteredUserName,userPhone);

        // если пользователь создан и его идентификатор получен
        if((userId != null) && (!userId.equals(""))) {

            // сохранить данные пользователя в Preferences
            saveTextInPreferences("user_id", "" +userId);

            // возвращаем ответ, что процесс прошел успешно
            result = true;
        }

        // возвращаем результат
        return result;
    }

    /**
     * инициирование генерации временного пароля для входа пользователя
     * @return числовое значение временного пароля для входа
     */
//    private int getEnterCode(int userId) {
    private boolean setEnterCode() {

        boolean result = false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();

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

    private int getRandomValue() {

        int minVal = 111111;
        int maxVal = 999999;

        return (minVal + (int)(Math.random() * ((maxVal - minVal) + 1)));
    }
}