package com.androiditgroup.loclook.sms_code_pkg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.phone_number_pkg.PhoneNumber_Activity;
import com.androiditgroup.loclook.user_name_pkg.UserName_Activity;
import com.androiditgroup.loclook.tape_pkg.Tape_Activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by admin on 13.09.2015.
 */
public class SMSCode_Activity extends     Activity
                                implements  TextWatcher,
                                            View.OnClickListener {

    private SharedPreferences   shPref;

    private EditText            smsCodeET;

    private String              userPhone       = "";
    private String              userAuthCode    = "";
    private String              userIsNew       = "";
    private String              enteredSmsCode  = "";

    private final int smsCodeETResId   = R.id.SmsCode_SmsCodeET;
    private final int continueIVResId  = R.id.SmsCode_ContinueIV;
    private final int backIVResId      = R.id.SmsCode_BackIV;

    private final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // задать файл-компоновщик элементов окна
        setContentView(R.layout.activity_sms_code);

        //////////////////////////////////////////////////////////////////////////////////

        // определить переменную для работы с полем "СМС код"
        smsCodeET = (EditText) findViewById(smsCodeETResId);
        smsCodeET.addTextChangedListener(this);

        //////////////////////////////////////////////////////////////////////////////////

        // определить переменную для работы с Preferences
        shPref = getSharedPreferences("user_data", MODE_PRIVATE);

        // создаем текстовые данные в Preferences
        initPreferences();

        // подгружаем данные из Preferences
        loadTextFromPreferences();

        //////////////////////////////////////////////////////////////////////////////////

        findViewById(continueIVResId).setOnClickListener(this);
        findViewById(backIVResId).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case continueIVResId:
                                    // получаем код, введенный пользователем
                                    enteredSmsCode = smsCodeET.getText().toString();

                                    // если отсутствует номер телефона | код авторизации не введен | введенный код авторизации не совпадает с ожидаемым системой
                                    if((userPhone.equals("")) || (enteredSmsCode.equals("")) || (!enteredSmsCode.equals(userAuthCode)))
                                        // стоп
                                        return;

                                    // задать флаг в Preferences, что это не новый пользователь
                                    saveTextInPreferences("new_user",    "N");

                                    // идем вперед
                                    moveForward();
                                    break;
            case backIVResId:
                                    // переходим обратно
                                    moveBack();
                                    break;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private void moveForward() {

        // переход к "окну ввода кода доступа"
        Intent intent = new Intent(SMSCode_Activity.this, Tape_Activity.class);

        // осуществить переход к заданному окну
        startActivity(intent);
    }

    private void moveBack() {

        // переход к заданному экрану
        Intent intent = null;

        // если это не новый пользователь либо значение параметра не получено
        if((userIsNew.equals("")) || (userIsNew.equals("N")))
            // переходить будем на экран "Номер телефона"
            intent = new Intent(SMSCode_Activity.this, PhoneNumber_Activity.class);
        // если это новый пользователь
        else
            // переходим на экран "Имя пользователя"
            intent = new Intent(SMSCode_Activity.this, UserName_Activity.class);

        // если переход задан
        if(intent != null)
            // осуществить его
            startActivity(intent);
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        // если введены 6 символов в поле
        if(smsCodeET.getText().length() == 6) {
            // сворачиваем клавиатуру
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(smsCodeET.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * создание текстовых данных в Preferences
     */
    private void initPreferences() {

        Log.d(LOG_TAG, "SMSCode_Activity: initPreferences()");

        if(!shPref.contains("user_id"))
            saveTextInPreferences("user_id", "0");

        if(!shPref.contains("user_access_token"))
            saveTextInPreferences("user_access_token", "");

        if(!shPref.contains("user_name"))
            saveTextInPreferences("user_name", "");

        if(!shPref.contains("user_description"))
            saveTextInPreferences("user_description", "");

        if(!shPref.contains("user_site"))
            saveTextInPreferences("user_site", "");

        if(!shPref.contains("user_avatar"))
            saveTextInPreferences("user_avatar", "");

        if(!shPref.contains("user_page_cover"))
            saveTextInPreferences("user_page_cover", "");

        if(!shPref.contains("user_hidden_badges"))
            saveListInPreferences("user_hidden_badges", new ArrayList<String>());

        if(!shPref.contains("user_region_name"))
            saveTextInPreferences("user_region_name", "");

        if(!shPref.contains("user_street_name"))
            saveTextInPreferences("user_street_name", "");

        if(!shPref.contains("user_latitude"))
            saveTextInPreferences("user_latitude", "0.0f");

        if(!shPref.contains("user_longitude"))
            saveTextInPreferences("user_longitude", "0.0f");

        if(!shPref.contains("user_radius"))
            saveTextInPreferences("user_radius", "200");
    }

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

    /**
     * сохранение массива значений в Preferences
     */
    private void saveListInPreferences(String key, ArrayList<String> list) {
        SharedPreferences.Editor ed = shPref.edit();

        Set<String> set = new HashSet<String>();
        set.addAll(list);
        ed.putStringSet(key, set);

        ed.commit();
    }

    /**
     * загрузка сохраненных значений из Preferences
     */
    private void loadTextFromPreferences() {

        // если параметр существует
        if(shPref.contains("phone_number"))
            // получить из него значение
            userPhone = shPref.getString("phone_number", "");

        // если параметр существует
        if(shPref.contains("user_auth_code")) {
            // получить из него значение
            userAuthCode = shPref.getString("user_auth_code", "");

            // если код авторизации получен
            if(!userAuthCode.equals(""))
                // вывести на экран код авторизации, который должен ввести пользователь
                Toast.makeText(this, userAuthCode, Toast.LENGTH_LONG).show();
            // если код авторизации не получен
            else
                // вывести на экран предупреждение
                Toast.makeText(this, R.string.sms_code_noCodeErr, Toast.LENGTH_LONG).show();
        }

        // если параметр существует
        if(shPref.contains("new_user"))
            // получить из него значение
            userIsNew = shPref.getString("new_user", "");
    }
}