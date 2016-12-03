package com.androiditgroup.loclook.preloader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.phone_number_pkg.PhoneNumber_Activity;
import com.androiditgroup.loclook.tape_pkg.Tape_Activity;

import java.util.concurrent.TimeUnit;

/**
 * Created by OS1 on 24.05.2016.
 */
public class Preloader_Activity extends Activity {

    private SharedPreferences shPref;

    private String userId;

    private MyTask myTask;

    // final String LOG_TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // задать файл-компоновщик элементов окна
        setContentView(R.layout.activity_preloader);

        //////////////////////////////////////////////////////////////////////////////////

        // определить переменную для работы с Preferences
        shPref = getSharedPreferences("user_data", MODE_PRIVATE);

        // подгрузить данные из Preferences
        loadTextFromPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();

        myTask = new MyTask();
        myTask.execute();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * загрузка сохраненных значений из Preferences
     */
    private void loadTextFromPreferences() {
        // если параметр существует
        if(shPref.contains("user_id"))
            // получаем его значение
            userId = shPref.getString("user_id", "");
    }


    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {

                // задержка перехода в 3 секунды
                TimeUnit.SECONDS.sleep(3);

                ///////////////////////////////////////////////////////////////////////////////

                Intent intent = null;

                // если идентификатор пользователя получен
                if((userId != null) && (!userId.equals("")))
                    // переход к ленте
                    intent = new Intent(Preloader_Activity.this, Tape_Activity.class);
                // если идентификатор пользователя отсутствует
                else
                    // переход к окну ввода номера телефона
                    intent = new Intent(Preloader_Activity.this, PhoneNumber_Activity.class);

                if(intent != null)
                    startActivity(intent);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
