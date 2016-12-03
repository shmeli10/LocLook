package com.androiditgroup.loclook.region_map_pkg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.androiditgroup.loclook.utils_pkg.DB_Helper;
import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.user_profile_pkg.User_Profile_Activity;
import com.androiditgroup.loclook.badges_pkg.Badges_Activity;
import com.androiditgroup.loclook.favorites_pkg.Favorites_Activity;
import com.androiditgroup.loclook.notifications_pkg.Notifications_Activity;
import com.androiditgroup.loclook.tape_pkg.Tape_Activity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by OS1 on 17.09.2015.
 */
public class RegionMap_Activity extends     ActionBarActivity
                                implements  View.OnClickListener,
                                            SeekBar.OnSeekBarChangeListener,
                                            OnMapReadyCallback,
                                            GoogleMap.OnMapLongClickListener,
                                            ConnectionCallbacks,
                                            OnConnectionFailedListener,
                                            LocationListener {

    private Context             context;
    private SharedPreferences   shPref;
    private DB_Helper           dbHelper;
    private GoogleMap           googleMap;
    private GoogleApiClient     googleApiClient;
    private UiSettings          UISettings;
    private Marker              marker;
    private Circle              circle;
    private EditText            regionName;
    private LinearLayout        findLocationWrapLL;
    private SeekBar             radiusBar;
    private Drawer.Result       drawerResult = null;
    private Intent              intent;

    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(60000 * 60)   // 1 hour
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    // максимально допустимое значение радиуса в км
    private final int maxRadius  = 5000;

    private final int hamburgerWrapLLResId      = R.id.RegionMap_HamburgerWrapLL;
    private final int refreshArrowWrapLLResId   = R.id.RegionMap_RefreshArrowWrapLL;
    private final int regionNameETResId         = R.id.RegionMap_RegionNameET;
    private final int findLocationResId         = R.id.RegionMap_FindLocationWrapLL;
    private final int userAvatarIVResId         = R.id.MenuHeader_UserAvatarIV;
    private final int userNameTVResId           = R.id.MenuHeader_UserNameTV;

    int user_id;

    String user_name     = "";

    LatLng  current_location;
    float   current_latitude;
    float   current_longitude;
    int     current_radius;
    String  current_region_name;
    String  current_street_name;

    LatLng  last_location;
    float   last_latitude;
    float   last_longitude;
    int     last_radius;
    String  last_region_name;
    String  last_street_name;

    LatLng  new_location;
    float   new_latitude;
    float   new_longitude;
    int     new_radius;
    String  new_region_name;
    String  new_street_name;

    float circle_radius = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_map);

        context = getApplicationContext();

        // создать объект для работы с БД
        dbHelper = new DB_Helper(this);

        //////////////////////////////////////////////////////////////////////////

        shPref = context.getSharedPreferences("user_data", context.MODE_PRIVATE);

        // подгружаем пользовательские данные из файла настроек
        loadTextFromPreferences();

        //////////////////////////////////////////////////////////////////////////

        (findViewById(hamburgerWrapLLResId)).setOnClickListener(this);
        (findViewById(refreshArrowWrapLLResId)).setOnClickListener(this);

        regionName          = (EditText) findViewById(regionNameETResId);

        findLocationWrapLL  = (LinearLayout) findViewById(findLocationResId);
        findLocationWrapLL.setOnClickListener(this);

        radiusBar           = (SeekBar) findViewById(R.id.RegionMap_ViewRadiusSB);
        radiusBar.setMax(maxRadius);
        radiusBar.setProgress(last_radius);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.RegionMap_Map);
        mapFragment.getMapAsync(this);

        // задаем координаты последнего сохраненного положения пользователя
        setMyLastLocation();

        // определяем координаты реального положения пользователя
        setMyCurrentLocation();

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
                        new PrimaryDrawerItem().withName(R.string.region_text).withIcon(getResources().getDrawable(R.drawable.geolocation_icon)).withBadge(last_region_name).withIdentifier(5)
                )
                .withFooter(R.layout.drawer_footer)
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Скрываем клавиатуру при открытии Navigation Drawer
                        InputMethodManager inputMethodManager = (InputMethodManager) RegionMap_Activity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(RegionMap_Activity.this.getCurrentFocus().getWindowToken(), 0);
                        // drawerView.fi.updateBadge(shPref.getString("region_name", ""), 5);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        if(intent != null)
                            startActivity(intent);
                        else {
                            if(new_region_name == null)
                                // переходим к точке карты где находится пользователь и размещаем на ней маркер
                                showLocation(current_location, current_region_name, current_street_name, current_radius);
                            else
                                // переходим к новой точке карты и размещаем на ней новый маркер
                                showLocation(new_location, new_region_name, new_street_name, new_radius);
                        }
                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    // Обработка клика
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {

                            int itemIdentifier = drawerItem.getIdentifier();

                            if(itemIdentifier > 0 && itemIdentifier != 5) {
                                intent = new Intent();

                                switch(drawerItem.getIdentifier()) {

                                    case 1:
                                        intent = new Intent(RegionMap_Activity.this, Tape_Activity.class);
                                        break;
                                    case 2:
                                        intent = new Intent(RegionMap_Activity.this, Favorites_Activity.class);
                                        break;
                                    case 3:
                                        intent = new Intent(RegionMap_Activity.this, Notifications_Activity.class);
                                        break;
                                    case 4:
                                        intent = new Intent(RegionMap_Activity.this, Badges_Activity.class);
                                        break;
                                }

                                if(drawerResult.isDrawerOpen())
                                    drawerResult.closeDrawer();
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
                            Toast.makeText(RegionMap_Activity.this, RegionMap_Activity.this.getString(((SecondaryDrawerItem) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelection(4);
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        googleApiClient.disconnect();
    }

    /**
     * обработка внезапного закрытия окна или приложения
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveUserNewLocation();
    }

    @Override
    public void onMapReady(GoogleMap google_map) {
        googleMap = google_map;

        // назначаем карте слушателя длинного нажатия по ней
        googleMap.setOnMapLongClickListener(this);

        // получаем ссылку к пользовательским настройкам карты
        UISettings = this.googleMap.getUiSettings();

        // зум включить
        UISettings.setZoomControlsEnabled(true);

        // вращение карты выключить
        UISettings.setRotateGesturesEnabled(false);

        // назначаем ползунку слушателя изменений радиуса круга
        radiusBar.setOnSeekBarChangeListener(this);

        // отобразить маркер с окружностью и отцентрировать карту
        showLocation(last_location, last_region_name, last_street_name, last_radius);
    }

    //
    public void onClick(View view) {

        switch(view.getId()) {

            case hamburgerWrapLLResId:
                                            // если местоположение маркера изменилось сохраняем в файл настроек и в БД
                                            saveUserNewLocation();

                                            if(new_region_name == null)
                                                drawerResult.updateBadge(current_region_name, 4);
                                            else
                                                drawerResult.updateBadge(new_region_name, 4);

                                            drawerResult.openDrawer();
                                            break;
            case refreshArrowWrapLLResId:
                                            // очищаем карту от меток
                                            googleMap.clear();

                                            setLocationName(current_location, "current");

                                            new_region_name     = current_region_name;
                                            new_street_name     = current_street_name;
                                            new_latitude        = current_latitude;
                                            new_longitude       = current_longitude;

                                            last_location = null;
                                            new_location  = null;

                                            // переходим к точке карты где находится пользователь и размещаем на ней маркер
                                            showLocation(current_location, current_region_name, current_street_name, current_radius);
                                            break;
            case findLocationResId:
                                            // очищаем карту от меток
                                            googleMap.clear();

                                            // определяем координаты нового местоположения пользователя на карте
                                            new_location = findLocationByName();

                                            new_latitude = Float.parseFloat("" + new_location.latitude);
                                            new_longitude = Float.parseFloat("" +new_location.longitude);

                                            // определяем город/область/страну и улицу по новым координатам
                                            setLocationName(new_location, "new");

                                            if(new_radius == 0)
                                                new_radius = last_radius;

                                            // переходим к новой точке карты и размещаем на ней новый маркер
                                            showLocation(new_location, new_region_name, new_street_name, new_radius);

                                            ////////////////////////////////////////////////////////////////////

                                            // при нажатии на кнопку, сворачиваем клавиатуру
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(findLocationWrapLL.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                            break;
            case userAvatarIVResId:
            case userNameTVResId:
                                            drawerResult.closeDrawer();

                                            Intent intent = new Intent(this, User_Profile_Activity.class);
                                            startActivity(intent);
                                            break;

        }
    }

    @Override
    public void onMapLongClick(LatLng point) {

        // очищаем карту от меток
        googleMap.clear();

        new_latitude = Float.parseFloat("" +point.latitude);
        new_longitude = Float.parseFloat("" + point.longitude);

        // определяем координаты нового местоположения пользователя на карте
        new_location = new LatLng(new_latitude,new_longitude);

        // определяем город/область/страну и улицу по новым координатам
        setLocationName(new_location, "new");

        if(new_radius == 0)
            new_radius = last_radius;

        showLocation(new_location, new_region_name, new_street_name, new_radius);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        int radius = radiusBar.getProgress();

        googleMap.clear();

        if(new_location != null)
            showLocation(new_location, new_region_name, new_street_name, radius);
        else if(last_location != null)
            showLocation(last_location, last_region_name, last_street_name, radius);
        else
            showLocation(current_location, current_region_name, current_street_name, radius);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onLocationChanged(Location location) {
        current_latitude = Float.parseFloat("" +location.getLatitude());
        current_longitude = Float.parseFloat("" +location.getLongitude());

        current_location = new LatLng(current_latitude, current_longitude);
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, REQUEST, this);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

    ///////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void setMyLastLocation() {
        last_location = new LatLng(last_latitude, last_longitude);
    }

    //
    private void setMyCurrentLocation() {
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        if (googleApiClient.isConnected()) {
            Location point = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            current_latitude  = Float.parseFloat("" +point.getLatitude());
            current_longitude = Float.parseFloat("" + point.getLongitude());

            current_location = new LatLng(current_latitude, current_longitude);
        }

        current_radius = last_radius;
    }

    //
    private void setLocationName(LatLng point, String flagName) {

        // получаем данные местности
        ArrayList<String> locationData = getLocationData(point);

        int locationDataSize = locationData.size();

        String region_name = "";
        String street_name = "";

        // отобразить данные в зависимости от количества фрагментов адреса объекта
        switch(locationDataSize) {

            case 1:
                // получен только город/область/страна
                region_name = locationData.get(0).toString();
                break;
            case 2:
                // получены город/область/страна и название улицы
                region_name = locationData.get(0).toString();
                street_name = locationData.get(1).toString();
                break;
        }

        // если необходимо задать значения для текущего положения пользователя
        if(flagName.equals("current")) {
            current_region_name = region_name;

            if((street_name != null) && (!street_name.equals("")))
                current_street_name = street_name;
        }
        // если необходимо задать значения для нового положения пользователя
        else {
            new_region_name = region_name;

            if((street_name != null) && (!street_name.equals("")))
                new_street_name = street_name;
        }
    }

    //
    private void showLocation(LatLng point, String region_name, String street_name, int radius) {
        CameraPosition newPoint = new CameraPosition.Builder().target(point).zoom(13.5f).bearing(0).tilt(0).build();

        // центрируем карту на заданной точке
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPoint));

        // формируем маркер в заданной точке
        marker = googleMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(false));

        // формируем окржуность в заданной точке и заданного радиуса, с заливкой
        circle = googleMap.addCircle(new CircleOptions().center(point).radius(radius).strokeWidth(3).strokeColor(Color.RED).fillColor(0x23ff0000));

        // если город/область/страна определены, выводим на экран
        if((region_name != null) && (!region_name.equals("")))
            regionName.setText(region_name);

        // если улица определена, выводим на экран
        if((street_name != null) && (!street_name.equals("")))
            marker.setTitle(street_name);

        // затираем пришедшим размером радиуса все прежние значения
        new_radius = last_radius = current_radius = radius;

        showMarkers(radius);
    }

    // вернуть координаты точки найденной на карте по названю объекта
    public LatLng findLocationByName() {

        LatLng point = last_location;

        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            List<Address> addresses = geoCoder.getFromLocationName(regionName.getText().toString(), 1);

            // если данные получены
            if (addresses.size() > 0)
                // вернуть координаты объекта на карте
                point = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // вернуть результат
        return point;
    }

    // возвращаем список данных по найденной точке на карте
    public ArrayList<String> getLocationData(LatLng point) {

        ArrayList<String> list = new ArrayList<>();

        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            List<Address> addresses = geoCoder.getFromLocation(point.latitude, point.longitude, 1);

            if (addresses.size() > 0) {

                // сформировать результат в зависимости от полученного количества фрагментов названия объекта
                switch(addresses.get(0).getMaxAddressLineIndex()) {

                    case 2:
                            // вернуть название города
                            list.add(addresses.get(0).getAddressLine(0));
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

                /*
                for(int i=0; i<addresses.get(0).getMaxAddressLineIndex(); i++)
                    Log.d("myLogs", "address part [" +i+ "] = " +addresses.get(0).getAddressLine(i));

                Log.d("myLogs", "===============================================================");
                */
            }
            else {
                // вернуть текст вместо названия города и улицы
                list.add(getResources().getString(R.string.undefined_area) + "...");
                list.add(getResources().getString(R.string.undefined_street) + "...");

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // вернуть результат
        return list;
    }

    //
    private void saveUserNewLocation() {

        LatLng point = marker.getPosition();

        String region_name = "";
        String street_name = "";

        float latitude  = Float.parseFloat("" +point.latitude);
        float longitude = Float.parseFloat("" +point.longitude);
        int   radius    = 0;

        // флаг наличия изменений в данных
        boolean newUserLocation = false;

        // если название города/области/страны изменилось, сохраняем новое значение
        if((new_region_name != null) && (!new_region_name.equals("")) && (!last_region_name.equals(new_region_name))) {
            region_name = new_region_name;
            saveTextInPreferences("region_name", new_region_name);

            // если изменений еще нет
            if(!newUserLocation)
                // сообщаем что теперь есть
                newUserLocation = true;
        }

        // если название улицы изменилось, сохраняем новое значение
        if((new_street_name != null) && (!new_street_name.equals("")) && (!last_street_name.equals(new_street_name))) {
            street_name = new_street_name;
            saveTextInPreferences("street_name", new_street_name);

            // если изменений еще нет
            if(!newUserLocation)
                // сообщаем что теперь есть
                newUserLocation = true;
        }

        // если широта изменилась, сохраняем новое значение
        if((new_latitude > 0.0f) && (new_latitude != last_latitude)) {
            latitude = new_latitude;
            saveTextInPreferences("map_latitude",  "" +new_latitude);

            // если изменений еще нет
            if(!newUserLocation)
                // сообщаем что теперь есть
                newUserLocation = true;
        }

        // если долгота изменилась, сохраняем новое значение
        if((new_longitude > 0.0f) && (new_longitude != last_longitude)) {
            longitude = new_longitude;
            saveTextInPreferences("map_longitude",  "" +new_longitude);

            // если изменений еще нет
            if(!newUserLocation)
                // сообщаем что теперь есть
                newUserLocation = true;
        }

        // если радиус изменился и он больше 0 сохраняем новое значение
        // либо если радиус был изменен на 0 также сохраняем это значение
        if((new_radius > 0) || ((new_radius == 0) && (last_radius == 0))) {
            radius = new_radius;
            saveTextInPreferences("map_radius", "" + new_radius);

            // если изменений еще нет
            if (!newUserLocation)
                // сообщаем что теперь есть
                newUserLocation = true;
        }
        // если радиус не изменился, сохраняем старое значение
        else
            radius = last_radius;

        // если изменения есть, сохраняем их в БД
        if(newUserLocation)
           setNewUserLocationInDB(latitude,longitude,radius,region_name,street_name);

        /////////////////////////////////////////////////////////////////////

        // чтобы карта не лагала и выдавала ошибку
        googleApiClient.disconnect();
        googleMap.clear();
    }

    //
    private void setNewUserLocationInDB(float latitude, float longitude, int radius, String region_name, String street_name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ////////////////////////////////////////////////////////

        dbHelper.updateTable(db, "user_data", user_id, new String[]{"map_latitude", "map_longitude", "map_radius", "region_name", "street_name"}, new String[]{"" +latitude, "" +longitude, "" +radius, region_name, street_name});
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void showMarkers(int radius) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ////////////////////////////////////////////////////////

        float latitude = Float.parseFloat("" + marker.getPosition().latitude);
        float longitude = Float.parseFloat("" +marker.getPosition().longitude);

        // более точный радиус
        circle_radius = ((Float.parseFloat("" +radius)) / 1000);

        ////////////////////////////////////////////////////////

        StringBuilder sqlQuery = new StringBuilder("select id, badge_id, map_latitude, map_longitude from publication_data where map_latitude != ? AND  map_longitude != ?" );

        Cursor cursor = db.rawQuery(sqlQuery.toString(), new String[] {"" +latitude, "" +longitude});

        // если данные в запросе получены
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // формируем массив публикаций
            do {
                int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("id")));

                float map_latitude  = Float.parseFloat(cursor.getString(cursor.getColumnIndex("map_latitude")));
                float map_longitude = Float.parseFloat(cursor.getString(cursor.getColumnIndex("map_longitude")));

                LatLng user_location = new LatLng(latitude,longitude);
                LatLng publication_location = new LatLng(map_latitude,map_longitude);

                if(getDistance(publication_location,user_location) < circle_radius) { // (radiusBar.getProgress() / 1000) ) {

                    String uri="@drawable/ic_m_" +Integer.parseInt(cursor.getString(cursor.getColumnIndex("badge_id")));

                    int imageId = getResources().getIdentifier(uri, null, this.getPackageName());

                    Marker markerTemp = googleMap.addMarker(new MarkerOptions().position(publication_location).icon(BitmapDescriptorFactory.fromResource(imageId)).draggable(false));
                    markerTemp.setTitle("" +id);
                }
            }
            while(cursor.moveToNext());
        }
    }

    //
    private double getDistance(LatLng point1, LatLng point2) {
        // радиус Земли в км
        int R = 6371;

        // получаем значения
        float dLat   = Float.parseFloat("" +getRadValue(point2.latitude - point1.latitude));
        float dLong  = Float.parseFloat("" + getRadValue(point2.longitude - point1.longitude));

        // подставляем значения в формулу
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(getRadValue(point1.latitude)) * Math.cos(getRadValue(point2.latitude)) * Math.sin(dLong/2) * Math.sin(dLong/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        // получаем расстояние от точки до центра круга
        return (R * c);
    }

    //
    private double getRadValue(double value) {
        return value*Math.PI/180;
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

    /**
     * загрузка сохраненных значений из Preferences
     */
    private void loadTextFromPreferences() {

        // если настройки содержат имя пользователя
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
            last_region_name = shPref.getString("region_name", "");
        }

        // если настройки содержат название улицы
        if(shPref.contains("street_name")) {
            // значит можно получить значение
            last_street_name = shPref.getString("street_name", "");
        }

        // если настройки содержат широту
        if(shPref.contains("map_latitude")) {
            // значит можно получить значение для геолокации
            last_latitude  = Float.parseFloat(shPref.getString("map_latitude", ""));
        }

        // если настройки содержат долготу
        if(shPref.contains("map_longitude")) {
            // значит можно получить значение для геолокации
            last_longitude = Float.parseFloat(shPref.getString("map_longitude", ""));
        }

        // если настройки содержат радиус
        if(shPref.contains("map_radius")) {
            // значит можно получить значение для геолокации
            last_radius = Integer.parseInt(shPref.getString("map_radius", "0"));
        }
    }
}