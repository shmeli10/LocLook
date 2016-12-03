package com.androiditgroup.loclook.notifications_pkg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.region_map_pkg.RegionMap_Activity;
import com.androiditgroup.loclook.user_profile_pkg.User_Profile_Activity;
import com.androiditgroup.loclook.badges_pkg.Badges_Activity;
import com.androiditgroup.loclook.favorites_pkg.Favorites_Activity;
import com.androiditgroup.loclook.publication_pkg.Publication_Activity;
import com.androiditgroup.loclook.tape_pkg.Tape_Activity;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

/**
 * Created by OS1 on 17.09.2015.
 */
public class Notifications_Activity extends     ActionBarActivity
                                    implements  View.OnClickListener {

    private Context             context;
    private SharedPreferences   shPref;
    private Drawer.Result       drawerResult = null;

    String user_name     = "";
    String region_name   = "";

    private final int hamburgerWrapLLResId  = R.id.Notifications_HamburgerWrapLL;
    private final int publicationWrapLLResId= R.id.Notifications_PublicationWrapLL;
    private final int userAvatarIVResId     = R.id.MenuHeader_UserAvatarIV;
    private final int userNameTVResId       = R.id.MenuHeader_UserNameTV;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        context = getApplicationContext();

        /////////////////////////////////////////////////////////////////////////////////

        shPref = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
        loadTextFromPreferences();

        /////////////////////////////////////////////////////////////////////////////////////

        (findViewById(hamburgerWrapLLResId)).setOnClickListener(this);
        (findViewById(publicationWrapLLResId)).setOnClickListener(this);

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
                        InputMethodManager inputMethodManager = (InputMethodManager) Notifications_Activity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(Notifications_Activity.this.getCurrentFocus().getWindowToken(), 0);
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

                            if(itemIdentifier > 0 && itemIdentifier != 3) {

                                // Intent intent = new Intent();
                                intent = new Intent();

                                switch(drawerItem.getIdentifier()) {

                                    case 1:
                                        intent = new Intent(Notifications_Activity.this, Tape_Activity.class);
                                        break;
                                    case 2:
                                        intent = new Intent(Notifications_Activity.this, Favorites_Activity.class);
                                        break;
                                    case 4:
                                        intent = new Intent(Notifications_Activity.this, Badges_Activity.class);
                                        break;
                                    case 5:
                                        intent = new Intent(Notifications_Activity.this, RegionMap_Activity.class);
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
                            Toast.makeText(Notifications_Activity.this, Notifications_Activity.this.getString(((SecondaryDrawerItem) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelection(2);
    }

    //
    public void onClick(View view) {

        Intent intent = null;

        switch(view.getId()) {

            case hamburgerWrapLLResId:
                                        // this.main_menu.toggleMenu();
                                        // intent = new Intent(this, Main_Activity.class);

                                        drawerResult.openDrawer();
                                        break;
            case publicationWrapLLResId:
                                        intent = new Intent(this, Publication_Activity.class);
                                        // startActivity(intent);
                                        break;
            case userAvatarIVResId:
            case userNameTVResId:
                                        drawerResult.closeDrawer();

                                        intent = new Intent(this, User_Profile_Activity.class);
                                        // startActivity(intent);
                                        break;
        }

        if(intent != null)
            startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Закрываем Navigation Drawer по нажатию системной кнопки "Назад" если он открыт
        if (drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * загрузка сохраненных значений из Preferences
     */
    private void loadTextFromPreferences() {

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