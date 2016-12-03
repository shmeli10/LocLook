package com.androiditgroup.loclook.badges_pkg;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.androiditgroup.loclook.utils_pkg.DB_Helper;
import com.androiditgroup.loclook.favorites_pkg.Favorites_Activity;
import com.androiditgroup.loclook.utils_pkg.HLine_Fragment;
import com.androiditgroup.loclook.notifications_pkg.Notifications_Activity;
import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.region_map_pkg.RegionMap_Activity;
import com.androiditgroup.loclook.tape_pkg.Tape_Activity;
import com.androiditgroup.loclook.user_profile_pkg.User_Profile_Activity;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.util.ArrayList;

/**
 * Created by OS1 on 17.09.2015.
 */
public class Badges_Activity    extends     ActionBarActivity
                                implements  View.OnClickListener,
                                            Badge_Fragment.OnSwitchStateChangedListener {

    private Context             context;
    private SharedPreferences   shPref;
    private DB_Helper           dbHelper;

    private Drawer.Result       drawerResult        = null;
    private ArrayList<String[]> badgesDataArrList   = new ArrayList<String[]>();

    private final int hamburgerWrapLLResId  = R.id.Badges_HamburgerWrapLL;
    private final int userAvatarIVResId     = R.id.MenuHeader_UserAvatarIV;
    private final int userNameTVResId       = R.id.MenuHeader_UserNameTV;

    int user_id;

    Intent intent;

    String user_name     = "";
    String region_name   = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        context = getApplicationContext();

        dbHelper = new DB_Helper(this);

        ///////////////////////////////////////////////////////////////////////////////////

        shPref = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
        loadTextFromPreferences();

        ///////////////////////////////////////////////////////////////////////////////////

        (findViewById(hamburgerWrapLLResId)).setOnClickListener(this);

        ///////////////////////////////////////////////////////////////////////////////////

        View headerView = getLayoutInflater().inflate(R.layout.drawer_header, null);

        // задаем обработчик клика по изображению пользователя в меню
        headerView.findViewById(userAvatarIVResId).setOnClickListener(this);

        // находим имя пользователя в меню и задаем ему значение и цвет текста
        TextView userNameTV = (TextView) headerView.findViewById(userNameTVResId);
        userNameTV.setText(user_name);
        userNameTV.setTextColor(Color.WHITE);

        // задаем обработчик клика по имени пользователя в меню
        userNameTV.setOnClickListener(this);

        // Инициализируем Navigation Drawer
        drawerResult = new Drawer()
                .withActivity(this)
                .withHeader(headerView)
                .withHeaderDivider(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.tape_text).withIcon(getResources().getDrawable(R.drawable.feed_icon)).withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.favorites_text).withIcon(getResources().getDrawable(R.drawable.favorite_icon)).withIdentifier(2),
                        new PrimaryDrawerItem().withName(R.string.notifications_text).withIcon(getResources().getDrawable(R.drawable.notifications_icon)).withIdentifier(3),
                        new PrimaryDrawerItem().withName(R.string.badges_text).withIcon(getResources().getDrawable(R.drawable.badges_icon)).withIdentifier(4),
                        new PrimaryDrawerItem().withName(R.string.region_text).withIcon(getResources().getDrawable(R.drawable.geolocation_icon)).withBadge(region_name).withIdentifier(5)
                )
                .withFooter(R.layout.drawer_footer)
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Скрываем клавиатуру при открытии Navigation Drawer
                        InputMethodManager inputMethodManager = (InputMethodManager) Badges_Activity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(Badges_Activity.this.getCurrentFocus().getWindowToken(), 0);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        if(intent != null)
                            startActivity(intent);
                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    // Обработка клика
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {

                            int itemIdentifier = drawerItem.getIdentifier();

                            if(itemIdentifier > 0 && itemIdentifier != 4) {

                                // Intent intent = new Intent();
                                intent = new Intent();

                                switch(drawerItem.getIdentifier()) {

                                    case 1:
                                            intent = new Intent(Badges_Activity.this, Tape_Activity.class);
                                            break;
                                    case 2:
                                            intent = new Intent(Badges_Activity.this, Favorites_Activity.class);
                                            break;
                                    case 3:
                                            intent = new Intent(Badges_Activity.this, Notifications_Activity.class);
                                            break;
                                    case 5:
                                            intent = new Intent(Badges_Activity.this, RegionMap_Activity.class);
                                            break;
                                }

                                if(drawerResult.isDrawerOpen())
                                    drawerResult.closeDrawer();

                                // startActivity(intent);
                            }
                        }
                        if (drawerItem instanceof Badgeable) {
                            Badgeable badgeable = (Badgeable) drawerItem;
                            if (badgeable.getBadge() != null) {
                                // учтите, не делайте так, если ваш бейдж содержит символ "+"
                                try {
                                    int badge = Integer.valueOf(badgeable.getBadge());
                                    if (badge > 0) {
                                        drawerResult.updateBadge(String.valueOf(badge - 1), position);
                                    }
                                } catch (Exception e) {
                                    Log.d("test", "Не нажимайте на бейдж, содержащий плюс! :)");
                                }
                            }
                        }
                    }
                })
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    // Обработка длинного клика, например, только для SecondaryDrawerItem
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof SecondaryDrawerItem) {
                            Toast.makeText(Badges_Activity.this, Badges_Activity.this.getString(((SecondaryDrawerItem) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelection(3);

        ///////////////////////////////////////////////////////////////////////////////////

        setBadgesData();
    }

    //
    public void onClick(View view) {

        switch(view.getId()) {

            case hamburgerWrapLLResId:
                                        // this.main_menu.toggleMenu();
                                        // Intent intent = new Intent(this, Main_Activity.class);
                                        // startActivity(intent);

                                        drawerResult.openDrawer();
                                        break;
            case userAvatarIVResId:
            case userNameTVResId:
                                        drawerResult.closeDrawer();

                                        Intent intent = new Intent(this, User_Profile_Activity.class);
                                        startActivity(intent);
                                        break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        // Закрываем Navigation Drawer по нажатию системной кнопки "Назад" если он открыт
        if (drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSwitchStateChanged(int badgeId, boolean switchIsOn) {

        String table_name = "user_badge_off_data";

        // если переключатель переведен в состояние "выключен"
        if(!switchIsOn) {

            // если записи по данному переключателю в таблице еще нет
            if(getUserBadgeOffRowId(user_id, badgeId) == 0)
                // добавляем запись, теперь он будет в состоянии "выключен"
                addSwitchStateOff(table_name, badgeId);
        }
        // если переключатель переведен в состояние "включен"
        else {

            // получаем идентификатор записи из таблцы по данному переключателю
            int rowId = getUserBadgeOffRowId(user_id, badgeId);

            // если идентификатор получен
            if(rowId > 0)
                // удаляем запись, теперь он будет в состоянии "включен"
                deleteSwitchStateOff(table_name, rowId);
        }

        // закрываем подключение к БД
        dbHelper.close();
    }

    ///////////////////////////////////////////////////////////////////////////////////

    //
    private void setBadgesData() {

        badgesDataArrList = getBadgesData();

        int badgesSum = badgesDataArrList.size();

        if(badgesSum > 0) {

            for(int i=0; i<badgesSum; i++){

                FragmentTransaction ft = getFragmentManager().beginTransaction();

                Badge_Fragment badgeFragment = new Badge_Fragment();

                //////////////////////////////////////////////////////////////////////////////////////////

                // Подготавливаем аргументы
                Bundle args = new Bundle();

                int badgeId = Integer.parseInt(badgesDataArrList.get(i)[0]);

                badgeFragment.setBadgeId(badgeId);

                if(i == 0)
                    args.putInt(Badge_Fragment.badgeMarginTopParam, 5);
                else if(i == (badgesSum-1))
                    args.putInt(Badge_Fragment.badgeMarginBottomParam, 5);

                args.putString(Badge_Fragment.badgeImgParam, "badge_" +badgeId);
                args.putString(Badge_Fragment.badgeNameParam, badgesDataArrList.get(i)[1]);

                String badgeState = badgesDataArrList.get(i)[2];

                if(badgeState != null && badgeState.equals("N"))
                    badgeFragment.setSwitchState(badgeState);

                badgeFragment.setArguments(args);

                //////////////////////////////////////////////////////////////////////////////////////////

                ft.add(R.id.Badges_Container_LL, badgeFragment);

                // если элементов более одного, разделитель выводить
                if(badgesSum > 1) {

                    // если это еще не последний элемент, то разделитель выводить
                    if(i < (badgesSum-1)) {

                        HLine_Fragment hLineFragment = new HLine_Fragment();

                        ft.add(R.id.Badges_Container_LL, hLineFragment);
                    }
                }

                ft.commit();
            }
        }
    }

    //
    private ArrayList<String[]> getBadgesData() {

        ArrayList<String[]> resultArrList = new ArrayList<String[]>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query("badge_data", null, null, null, null, null, null);

        ///////////////////////////////////////////////////////////////////////////////////////

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // определяем номера столбцов по имени в выборке
            int badgeIdColIndex    = cursor.getColumnIndex("id");
            int badgeNameColIndex  = cursor.getColumnIndex("name");

            do {
                String[] dataBlock = new String[3];

                dataBlock[0] = cursor.getString(badgeIdColIndex);
                dataBlock[1] = cursor.getString(badgeNameColIndex);

                // если запись в таблице по бэйджу есть, значит он должен быть в состоянии "выключен"
                if((getUserBadgeOffRowId(user_id, Integer.parseInt(cursor.getString(badgeIdColIndex)))) > 0)
                    dataBlock[2] = "N";

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
    public int getUserBadgeOffRowId(int userId,int badgeId) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // задаем имя таблицы
        String table_name = "user_badge_off_data";

        // создаем переменную в которой будем хранить результат
        int result = 0;

        //////////////////////////////////////////////////////////

        // создаем массив значений, с названиями столбцов таблицы, которые хотим считать
        String[] columns        = new String[]{"id"};

        // создаем строку-условие
        String selection        = "user_id = ? AND badge_id = ?";

        // создаем массив-значений, для строки-условия
        String[] selectionArgs  = new String[]{""+userId, ""+badgeId};

        // пытаемся получить значения на основании заданных условий
        Cursor cursor = db.query(table_name, columns, selection,selectionArgs, null, null, null);

        // если данные были получены
        if(cursor.getCount() > 0) {
            // перемещаемся к первой строке данных
            cursor.moveToFirst();

            // получаем идентификатор искомой записи
            result = cursor.getInt(cursor.getColumnIndex("id"));
        }

        // закрываем курсор
        cursor.close();

        // закрываем подключение к БД
        dbHelper.close();

        // возвращаем результат
        return result;
    }

    //
    private void addSwitchStateOff(String tableName, int badgeId) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////

        String[] columnsArr  = {"user_id", "badge_id"};
        String[] dataArr     = {"" +user_id, ""+badgeId};

        // добавляем запись о том, что заданный переключатель теперь в состоянии "выключен"
        dbHelper.fillTable(db, tableName, columnsArr, dataArr);

        // отображаем содержимое таблицы
        dbHelper.showAllTableData(db, tableName);

        // закрываем подключение к БД
        dbHelper.close();
    }

    //
    private void deleteSwitchStateOff(String tableName, int rowId) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////

        // удаляем заданную запись из таблицы
        db.delete(tableName,"id = " + rowId, null);

        // отображаем содержимое таблицы
        dbHelper.showAllTableData(db, tableName);

        // закрываем подключение к БД
        dbHelper.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void loadTextFromPreferences() {

        // если настройки содержат имя пользователя
//        if(shPref.contains("nick_name")) {
        if(shPref.contains("user_id")) {
            // значит можно получить и его идентификатор
            user_id = Integer.parseInt(shPref.getString("user_id", "0"));
        }

        // если настройки содержат имя пользователя
        if(shPref.contains("nick_name")) {
            // значит можно получить значение
            user_name = shPref.getString("nick_name", "");
        }

        // если настройки содержат название региона пользователя
        if(shPref.contains("region_name")) {
            // значит можно получить значение
            region_name = shPref.getString("region_name", "");
        }
    }
}