package com.androiditgroup.loclook.tape_pkg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androiditgroup.loclook.utils_pkg.DB_Helper;
import com.androiditgroup.loclook.favorites_pkg.Favorites_Activity;
import com.androiditgroup.loclook.utils_pkg.FullScreen_Image_Activity;
import com.androiditgroup.loclook.notifications_pkg.Notifications_Activity;
import com.androiditgroup.loclook.publication_pkg.Publication_Activity;
import com.androiditgroup.loclook.utils_pkg.Publication_Location_Dialog;
import com.androiditgroup.loclook.R;
import com.androiditgroup.loclook.region_map_pkg.RegionMap_Activity;
import com.androiditgroup.loclook.user_profile_pkg.User_Profile_Activity;
import com.androiditgroup.loclook.badges_pkg.Badges_Activity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by admin on 13.09.2015.
 */
public class Tape_Activity  extends     FragmentActivity
                            implements  View.OnClickListener,
                                        GoogleApiClient.ConnectionCallbacks,
                                        GoogleApiClient.OnConnectionFailedListener,
                                        LocationListener,
                                        Tape_Adapter.OnBadgeClickListener,
                                        Tape_Adapter.OnFavoritesClickListener,
                                        Tape_Adapter.OnLikedClickListener,
                                        Tape_Adapter.OnPublicationInfoClickListener,
                                        Tape_Adapter.OnAnswersClickListener {
                                        // User_Profile_Activity.OnDeletePublicationClickListener {
                                        // Publication_Fragment.OnQuizAnswerClickListener {

    private Context                     context;
    private SharedPreferences           shPref;
    private DB_Helper dbHelper;
    private GoogleApiClient             googleApiClient;
    private LatLng                      location;
    private Tape_Adapter                adapter;
    private RecyclerView                recyclerView;
    private ProgressDialog              progressDialog;
    private FloatingActionButton fabButton;
    private Publication_Location_Dialog publication_loc_dialog;

    private Drawer.Result               drawerResult            = null;
    private List<Tape_ListItems>        listItemsTapeList       = new ArrayList<Tape_ListItems>();
    private ArrayList<String[]>         publicationsDataArrList = new ArrayList<String[]>();
    private final LinearLayoutManager   linearLayoutManager     = new LinearLayoutManager(this);

    private final int hamburgerWrapLLResId      = R.id.Tape_HamburgerWrapLL;
    private final int publicationWrapLLResId    = R.id.Tape_PublicationWrapLL;
    private final int userAvatarIVResId         = R.id.MenuHeader_UserAvatarIV;
    private final int userNameTVResId           = R.id.MenuHeader_UserNameTV;

    private int user_id;

    String user_name     = "";
    String region_name   = "";

    private int limit = 25; // 5;
    private int itemsSum;
    private int lastItemId;
    private int focusPosition;
    private int selectedProvocationType;

    private float latitude;
    private float longitude;
    private float density;

    private double radius;

    Intent intent;

    // private final String LOG_TAG = "myLogs";

    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
                                                                  .setInterval(60000 * 60)   // 1 hour
                                                                  .setFastestInterval(16)    // 16ms = 60fps
                                                                  .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tape);

        context = getApplicationContext();
        density = context.getResources().getDisplayMetrics().density;

        dbHelper = new DB_Helper(this);

        //////////////////////////////////////////////////////////////////////////////////

        (findViewById(hamburgerWrapLLResId)).setOnClickListener(this);
        (findViewById(publicationWrapLLResId)).setOnClickListener(this);

        shPref = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
        loadTextFromPreferences();

        ///////////////////////////////////////////////////////////////////////////////////

        recyclerView = (RecyclerView) findViewById(R.id.Tape_PublicationsRV);
        recyclerView.setLayoutManager(linearLayoutManager);

        ///////////////////////////////////////////////////////////////////////////////////

        // если координаты нахождения пользователя не известны
        if(latitude == 0 && longitude == 0)
            // получить и запомнить их, определить название региона и улицы
            setMyCurrentLocation();

        ///////////////////////////////////////////////////////////////////////////////////

        setLocationName();

        // загружаем первые публикации
        setTapeData();

        recyclerView.addOnScrollListener(new Tape_EndlessOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
//                Log.d("focusPosition", "onLoadMore: focusPosition=" +focusPosition);
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(focusPosition);
                loadMore();
            }
        });

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
                        InputMethodManager inputMethodManager = (InputMethodManager) Tape_Activity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(Tape_Activity.this.getCurrentFocus().getWindowToken(), 0);
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

                            if (itemIdentifier > 0 && itemIdentifier != 1) {
                                intent = new Intent();

                                switch (drawerItem.getIdentifier()) {

                                    case 2:
                                        intent = new Intent(Tape_Activity.this, Favorites_Activity.class);
                                        break;
                                    case 3:
                                        intent = new Intent(Tape_Activity.this, Notifications_Activity.class);
                                        break;
                                    case 4:
                                        intent = new Intent(Tape_Activity.this, Badges_Activity.class);
                                        break;
                                    case 5:
                                        intent = new Intent(Tape_Activity.this, RegionMap_Activity.class);
                                        break;
                                }

                                if (drawerResult.isDrawerOpen())
                                    drawerResult.closeDrawer();
                            }
                        }
                    }
                })
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    // Обработка длинного клика, например, только для SecondaryDrawerItem
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof SecondaryDrawerItem) {
                            Toast.makeText(Tape_Activity.this, Tape_Activity.this.getString(((SecondaryDrawerItem) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                })
                .build();
        drawerResult.setSelection(0);

        ///////////////////////////////////////////////////////////////////////////////////////////

        Drawable lockLookDrawable = context.getResources().getDrawable(R.drawable.badge_1);

        fabButton = new FloatingActionButton.Builder(this)
                    .withDrawable(lockLookDrawable)
                    .withButtonColor(Color.WHITE)
                    .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                    .withMargins(0, 0, 16, 16)
                    .create();

        fabButton.hideFloatingActionButton();
                // setVisibility(View.INVISIBLE);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setTapeData();

                // fabButton.setVisibility(View.INVISIBLE);
                fabButton.hideFloatingActionButton();
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////

/*        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(googleApiClient != null)
            googleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(googleApiClient != null)
           googleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude  = Float.parseFloat("" +location.getLatitude());
        longitude = Float.parseFloat("" +location.getLongitude());

        saveTextInPreferences("map_latitude",  "" +latitude);
        saveTextInPreferences("map_longitude", "" +longitude);

        setLocationName();
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

    //
    public void onClick(View view) {

        int focusPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

        if((focusPosition + 1) <= itemsSum)
            saveTextInPreferences("focusPosition", "" +(focusPosition + 1));
        else
            saveTextInPreferences("focusPosition", "" + focusPosition);

        Intent intent = null;

        switch(view.getId()) {

            case hamburgerWrapLLResId:
                                        drawerResult.openDrawer();
                                        break;
            case publicationWrapLLResId:

                                        intent = new Intent(this, Publication_Activity.class);
                                        break;
            case userAvatarIVResId:
            case userNameTVResId:
                                        drawerResult.closeDrawer();

                                        intent = new Intent(this, User_Profile_Activity.class);
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
        }
        else {
            super.onBackPressed();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBadgeClicked(int badgeId, int badgeDrawable) {

        int tapeSize = listItemsTapeList.size(); // recyclerView.getChildCount();

        // int elementsToHide = 0;

        // Log.d("myLogs", "tapeSize= " +tapeSize);

        ArrayList<Integer> itemsToRemoveList = new ArrayList<Integer>();

        for(int i=0; i<tapeSize; i++) {

            // Log.d("myLogs", "publication badgeId= " +listItemsTapeList.get(i).getBadgeId()+ " badgeId= " +badgeId);

            if(listItemsTapeList.get(i).getBadgeId() != badgeId)
                //
                itemsToRemoveList.add(i);
        }

        int deletedPublicationsSum = 0;

        for(int i=0; i<itemsToRemoveList.size(); i++) {

            // Log.d("myLogs", "itemsToRemoveList.get(i)= " + itemsToRemoveList.get(i));
            // Log.d("myLogs", "deletedPublicationsSum= " + deletedPublicationsSum);

            int position = (itemsToRemoveList.get(i) - deletedPublicationsSum);

            // Log.d("myLogs", "next publication to remove position= " + position);

            if((position >= 0) && (position < listItemsTapeList.size())) {

                listItemsTapeList.remove(position);
                adapter.notifyItemRemoved(position);

                deletedPublicationsSum++;

                // Log.d("myLogs", "publication at position= " + position+ " removed");
            }
        }

        // if(elementsToHide > 0)
            // adapter.notifyDataSetChanged();

        fabButton.setFloatingActionButtonDrawable(context.getResources().getDrawable(badgeDrawable));

        // fabButton.setVisibility(View.VISIBLE);
        fabButton.showFloatingActionButton();

        // Log.d("myLogs", "==================================================================");
    }

    @Override
    public void onFavoritesClicked(String operationName, Tape_ListItems tapeListItems, int favoritesPublicationRowId, int publicationId) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // задаем имя таблицы
        String table_name = "favorite_publication_data";

        //////////////////////////////////////////////////////////

        if(operationName.equals("add")) {

            int rowId = dbHelper.addRow(db,table_name,new String[]{"publication_id","user_id"},new String[]{"" +publicationId, "" +user_id});

            if(rowId > 0) {
                tapeListItems.setFavoritePublicationRowId(rowId);
                tapeListItems.setPublicationIsFavorite(true);
            }
        }
        else {

            db.delete(table_name, " id = ?", new String[]{"" + favoritesPublicationRowId});
            tapeListItems.setPublicationIsFavorite(false);
        }

        // закрываем подключение к БД
        dbHelper.close();
    }

    @Override
    public void onAnswersClicked() {
        try {
            if(publication_loc_dialog != null)
                publication_loc_dialog.getDialog().dismiss();
        }
        catch(Exception exc) {}
    }

    @Override
    public void onLikedClicked(String operationName, Tape_ListItems tapeListItems, int likedPublicationRowId, int publicationId) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // задаем имя таблицы
        String table_name = "liked_publication_data";

        //////////////////////////////////////////////////////////

        if(operationName.equals("add")) {

            int rowId = dbHelper.addRow(db,table_name,new String[]{"publication_id","user_id"},new String[]{"" +publicationId, "" +user_id});

            if(rowId > 0) {
                tapeListItems.setLikedPublicationRowId(rowId);
                tapeListItems.sePublicationIsLiked(true);
            }
        }
        else {
            db.delete(table_name, " id = ?", new String[]{"" + likedPublicationRowId});
            tapeListItems.sePublicationIsLiked(false);
        }

        // закрываем подключение к БД
        dbHelper.close();

        // меняем значение в параметре "кол-во лайков"
        tapeListItems.setLikedSum("" + getRowsSum(2, publicationId));
    }

    @Override
    public void onPublicationInfoClicked(final int publicationId, int authorId, final float latitude, final float longitude, final String regionName, final String streetName, final String publicationText) {

        // создаем диалоговое окно
        final Dialog dialog = new Dialog(Tape_Activity.this, R.style.InfoDialog_Theme);
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
        ((LinearLayout) dialog.findViewById(R.id.InfoDialog_OwnButtonLL)).addView(getOwnButtonLL(dialog,(authorId == user_id), publicationId));

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

    /*@Override
    public void onDeletePublicationClicked(String publicationId) {
        // удаление публикации из ленты
        deletePublicationFromTape(publicationId);
    }*/

    /*
    @Override
    public void onQuizAnswerClicked(int publicationId, int newAllAnswerSum, int selectedAnswerPos, int newAnswersSum) { // int authorId, float latitude, float longitude, String regionName, String streetName, String publicationText) {

        // получаем "поле с кол-вом пользователей выбравших данный ответ в опросе"
        // TextView answersSumTV = (TextView) quizAnswerDataTextLL.findViewWithTag("answersSumTV");

        // получаем кол-вом всех проголосовавших пользователей в данном опросе
        // и увеличиваем его на только что проголосовавшего
        // int newAllAnswerSum = (allAnswersSum + 1);

        // получаем позицию ответа в списке ответов
        // int selectedAnswerPos = quizAnswersList.indexOf(arr);

        // будем хранить числовое значение содержащееся в "поле с кол-вом пользователей выбравших данный ответ в опросе"
        // int newAnswersSum = (Integer.parseInt(answersSumTV.getText().toString()) + 1);



        int listItemPosition = getListItemPositionByPublicationId(publicationId);

        if(listItemPosition >= 0) {

            // listItemsTapeList.remove(itemPosition);
            // adapter.notifyItemRemoved(itemPosition);

            LinearLayout quizLL = (LinearLayout) linearLayoutManager.findViewByPosition(listItemPosition).findViewById(R.id.TapeRow_QuizContainerLL).findViewWithTag("quizLL");

            // int allAnswersSum = Integer.parseInt(allAnswersSumValue);

            // resetQuizAnswersBG(quizLL, allAnswersSum, selectedAnswerPositionValue, selectedAnswerNewSumValue);

            // запускаем обновление опроса: фонов под ответами и общего кол-ва проголосовавших в опросе
            resetQuizAnswersBG(quizLL, newAllAnswerSum, selectedAnswerPos, newAnswersSum);
        }
    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

                String itemPosition             = data.getStringExtra("itemPosition");
                String isFavoriteChanged        = data.getStringExtra("isFavoriteChanged");
                String isLikedChanged           = data.getStringExtra("isLikedChanged");
                String answersSumValue          = data.getStringExtra("answersSumValue");

                String isQuizAnswerSelected     = data.getStringExtra("isQuizAnswerSelected");
                String allAnswersSumValue       = data.getStringExtra("allAnswersSumValue");

                String selectedAnswerPosition   = data.getStringExtra("selectedAnswerPosition");
                String selectedAnswerNewSum     = data.getStringExtra("selectedAnswerNewSum");

                String deletePublication        = data.getStringExtra("deletePublication");
                String publicationId            = data.getStringExtra("publicationId");

                int listPosition                = Integer.parseInt(itemPosition);
                int selectedAnswerPositionValue = Integer.parseInt(selectedAnswerPosition);
                int selectedAnswerNewSumValue   = Integer.parseInt(selectedAnswerNewSum);

                if(isFavoriteChanged.equals("true")) {

                    try {
                        linearLayoutManager.findViewByPosition(listPosition).findViewById(R.id.TapeRow_FavoritesWrapLL).performClick();
                    }
                    catch (Exception ex) {
                        Log.d("myLogs", "Tape_Activity: onActivityResult: Favorites Error!");
                    }
                }

                if(isLikedChanged.equals("true")) {

                    try {
                        linearLayoutManager.findViewByPosition(listPosition).findViewById(R.id.TapeRow_LikedWrapLL).performClick();
                    }
                    catch (Exception ex) {
                        Log.d("myLogs", "Tape_Activity: onActivityResult: Liked Error!");
                    }
                }

                if(isQuizAnswerSelected.equals("true")) {

                    try {
                        LinearLayout quizLL = (LinearLayout) linearLayoutManager.findViewByPosition(listPosition).findViewById(R.id.TapeRow_QuizContainerLL).findViewWithTag("quizLL");

                        int allAnswersSum = Integer.parseInt(allAnswersSumValue);

                        resetQuizAnswersBG(quizLL, allAnswersSum, selectedAnswerPositionValue, selectedAnswerNewSumValue);
                    }
                    catch (Exception ex) {
                        Log.d("myLogs", "Tape_Activity: onActivityResult: ResetQuizAnswersBG Error!");
                    }
                }

                // если публикацию необходимо удалить
                if(deletePublication.equals("true"))
                    // удаляем из ленты публикацию
                    deletePublicationFromTape(publicationId);
                // если публикацию не надо удалять
                else
                    // обновляем кол-во ответов для публикации
                    ((TextView) linearLayoutManager.findViewByPosition(listPosition).findViewById(R.id.TapeRow_AnswersSumTV)).setText(answersSumValue);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setTapeData() {

        // Log.d("focusPosition", "setTapeData: focusPosition=" +focusPosition);

        adapter = new Tape_Adapter(this, listItemsTapeList);
        recyclerView.setAdapter(adapter);

        adapter.clearAdapter();

        showPD();

        publicationsDataArrList = getPublicationsData();

        itemsSum = publicationsDataArrList.size();

        if(itemsSum > 0) {

            if(itemsSum < limit)
                lastItemId = itemsSum;
            else
                lastItemId = limit;

            if((focusPosition <= itemsSum) && (limit < focusPosition)) {
                limit = focusPosition;

                // устанавливаем фокус на заданной публикации
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(focusPosition);
            }

            addItemsInList(0, lastItemId);
        }

        hidePD();

        adapter.notifyDataSetChanged();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    //
    private void showPublicationLocationDialog(float latitude, float longitude, String regionName, String streetName) {

        try {
            // если диалоговое окно существует уже
            if(publication_loc_dialog != null) {
                publication_loc_dialog.setLocation(latitude, longitude);
                publication_loc_dialog.setRegionName(regionName);
                publication_loc_dialog.setStreetName(streetName);
                publication_loc_dialog.resetLocation();

                publication_loc_dialog.getDialog().show();
            }
            // если диалоговое окно не существует
            else {
                publication_loc_dialog = new Publication_Location_Dialog();
                publication_loc_dialog.setLocation(latitude, longitude);
                publication_loc_dialog.setRegionName(regionName);
                publication_loc_dialog.setStreetName(streetName);
                publication_loc_dialog.show(getFragmentManager(), "pub_loc_dialog_tape");
            }
        }
        catch(Exception exc) {
            Log.d("myLogs", "Tape_Activity: showPublicationLocationDialogError: " +exc.getStackTrace());
        }
    }

    //
    private void shareTo(String publicationText) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, publicationText);
        sendIntent.setType("text/plain");
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

    ///////////////////////////////////////////////////////////////////////////////////////////

    //
    private void loadMore() {

        // если записи получены были из БД
        if(itemsSum > 0) {

            // если записей больше чем то, что уже было загружено в ленту
            if(itemsSum > lastItemId) {

                showPD();

                // получаем кол-во строк которые еще предстоит вывести
                int rowsToShowSum = (itemsSum - lastItemId);

                // будем хранить новое значение последнего выведенного элемента в ленту
                int newLastItemId = 0;

                // если остаток данных больше лимита
                if(rowsToShowSum > limit)
                    // получаем новое значение для последнего выведенного элемента
                    newLastItemId = (lastItemId + limit);
                    // если надо вывести остаток меньший лимита
                else
                    // получаем новое значение для последнего выведенного элемента
                    newLastItemId = (lastItemId + rowsToShowSum);

                addItemsInList(lastItemId, newLastItemId);

                // запоминаем значение последнего элемента что был выведен в ленту
                lastItemId = newLastItemId;

                hidePD();

                adapter.notifyDataSetChanged();
            }
        }
    }

    private void addItemsInList(int startElementId, int endElementId) {

        for (int i = startElementId; i < endElementId; i++) {
            Tape_ListItems item = new Tape_ListItems();

            item.setPublicationId(Integer.parseInt(publicationsDataArrList.get(i)[0]));

            int authorId = Integer.parseInt(publicationsDataArrList.get(i)[1]);

            item.setAuthorId(authorId);

            item.setUserAvatar(R.drawable._anonymous_avatar_grey);

            //////////////////////////////////////////////////////////////////////////////////

            // если режим анонимности включен
            if((publicationsDataArrList.get(i)[4]).equals("true"))
                // передаем в качестве имени пользователя "Анонимно"
                item.setUserName(getResources().getString(R.string.publication_anonymous_text));
                // если режим анонимности выключен
            else {
                // подключаемся к БД
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // получаем имя пользователя на основании его идентификатора
                String userName = dbHelper.getUserName(db, "" + authorId);

                // если имя пользователя получено
                if(userName.length() > 0)
                    // передаем его в фрагмент для отображения
                    item.setUserName(userName);
                // если имя пользователя не получено
                else
                    // передаем в качестве имени пользователя "Анонимно"
                    item.setUserName(getResources().getString(R.string.publication_anonymous_text));
            }

            //////////////////////////////////////////////////////////////////////////////////

            // меняем формат представления даты публикации
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm");
            long dateLong = Long.parseLong(publicationsDataArrList.get(i)[3]);
            Date now = new Date(dateLong);
            String dateStr = sdf.format(now);

            item.setPublicationDate(dateStr);

            //////////////////////////////////////////////////////////////////////////////////

            int badgeId = Integer.parseInt(publicationsDataArrList.get(i)[6]);

            item.setBadgeId(badgeId);
            item.setBadgeImage(getResources().getIdentifier("@drawable/badge_" +badgeId, null, getPackageName()));

            //////////////////////////////////////////////////////////////////////////////////

            item.setPublicationText(publicationsDataArrList.get(i)[2]);

            //////////////////////////////////////////////////////////////////////////////////

            int favoritePublicationRowId = Integer.parseInt(publicationsDataArrList.get(i)[7]);

            if(favoritePublicationRowId > 0) {
                item.setFavorites(R.drawable.star_icon_active);

                // звездочку надо подсветить
                item.setPublicationIsFavorite(true);
            }
            // если значение не получено - значит такой записи нет
            else {
                item.setFavorites(R.drawable.star_icon);

                // звездочку не надо подсвечивать
                item.setPublicationIsFavorite(false);
            }

            item.setFavoritePublicationRowId(favoritePublicationRowId);

            //////////////////////////////////////////////////////////////////////////////////

            item.setAnswersSum(publicationsDataArrList.get(i)[8]);
            item.setAnswers(R.drawable.comments_icon);

            //////////////////////////////////////////////////////////////////////////////////

            item.setLikedSum(publicationsDataArrList.get(i)[9]);

            // получаем идентификатор записи о том, что публикация добавлена в liked
            int likedPublicationRowId = Integer.parseInt(publicationsDataArrList.get(i)[10]);

            if(likedPublicationRowId > 0) {
                item.setLikes(R.drawable.like_icon_active);

                // сердечко надо подсветить
                item.sePublicationIsLiked(true);
            }

            else {
                item.setLikes(R.drawable.like_icon);

                // сердечко не надо подсвечивать
                item.sePublicationIsLiked(false);
            }

            item.setLikedPublicationRowId(likedPublicationRowId);

            //////////////////////////////////////////////////////////////////////////////////

            item.setPublicationInfo(R.drawable.publication_info_icon);

            //////////////////////////////////////////////////////////////////////////////////

            // получаем координаты точки, где была написана публикация
            item.setLatitude(publicationsDataArrList.get(i)[11]);
            item.setLongitude(publicationsDataArrList.get(i)[12]);

            // получаем адрес точки, где была написана публикация
            item.setRegionName(publicationsDataArrList.get(i)[13]);
            item.setStreetName(publicationsDataArrList.get(i)[14]);

            //////////////////////////////////////////////////////////////////////////////////

            listItemsTapeList.add(item);
        }
    }

    //
    public void addImagesToPublication(LinearLayout photoContainer, int publicationId) {

        setPaddings(photoContainer, 0, 10, 0, 0);

        // получаем список массивов данных изображений в публикации
        final ArrayList<String[]> imagesDataList = getImagesDataList(publicationId);

        // если данные изображений получены
        if(!imagesDataList.isEmpty()) {

            // получаем кол-во добавленных изображений
            int imagesSum = imagesDataList.size();

            // создаем "список путей добавленных изображений"
            ArrayList<Uri>      bitmapsPathList             = new ArrayList<Uri>();

            // создаем "список кол-ва градусов для поворота изображений"
            ArrayList<Float>    bitmapsRotateDegreesList    = new ArrayList<Float>();

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
            setImagesContainer(photoContainer, imagesSum, bitmapsPathList, bitmapsRotateDegreesList);

            // раскладываем представления с изображениями в "контейнеры под *-ое изображение"
            setImages(photoContainer, reDecodeFiles(bitmapsPathList, bitmapsRotateDegreesList , imagesSum), bitmapsRotateDegreesList);
        }
    }

    //
    private ArrayList<String[]> getImagesDataList(int publicationId) {

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

            // формируем массив публикаций, на которые жаловался пользователь и их надо скрыть
            do {
                   String[] dataArr = new String[2];

                   dataArr[0] = cursor.getString(cursor.getColumnIndex("image_path"));
                   dataArr[1] = "" +cursor.getFloat(cursor.getColumnIndex("rotate_degree"));

                   resultList.add(dataArr);
            } while (cursor.moveToNext());
        }

        // закрываем 1-й курсор
        cursor.close();

        // возвращем результат
        return resultList;
    }

    //
    private void setImagesContainer(LinearLayout imagesContainer, int imagesSum, final ArrayList<Uri> bitmapsPathList,  final ArrayList<Float> bitmapsRotateDegreesList) {

        LinearLayout.LayoutParams lp;


        // получаем размер экрана
        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = d.getWidth();

        // задаем размеры для "контейнер под *-ое изображение" для каждого из трех режимов
        int size_3 = ((width - 20) / 3);   // добавлено 3 изображения
        int size_2 = ((width - 20) / 2);   // добавлено 2 изображения
        int size_1 = (size_2 + size_3);    // добавлено 1 изображение

        // задаем размеры для "контейнер под *-ое изображение" для каждого из трех режимов
        /*
        int size_3 = ((imagesContainer.getWidth() - 20) / 3);   // добавлено 3 изображения
        int size_2 = ((imagesContainer.getWidth() - 20) / 2);   // добавлено 2 изображения
        int size_1 = (size_2 + size_3);                         // добавлено 1 изображение
        */

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
    private ArrayList<Bitmap> reDecodeFiles(ArrayList<Uri> bitmapsPathList, ArrayList<Float> bitmapsRotateDegreesList, int imagesSum) {

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
            for(int i=0; i<bitmapsPathList.size(); i++)
                // кладем в "список изображений после изменения их размера" очередное изображение после его обработки
                tempBitmapList.add(decodeFile(bitmapsPathList.get(i), bitmapsRotateDegreesList, i, reqWidth, reqHeight));
        }

        // возвращаем результат
        return tempBitmapList;
    }

    //
    private Bitmap decodeFile(Uri photoPath, ArrayList<Float> bitmapsRotateDegreesList, int pos, int reqWidth, int reqHeight) {

        // получаем изображение-заглушку
        Bitmap bitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.no_photo_red)).getBitmap();

        try {

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
        Cursor cursor=managedQuery(contentUri, proj, null, null, null);

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
    private void setImages(LinearLayout imagesContainer, ArrayList<Bitmap> bitmapsList, ArrayList<Float> bitmapsRotateDegreesList) {

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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    //
    public void addQuizToPublication(LinearLayout quizContainer, int publicationId) {

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

            // получаем кол-во всех проголосовавших пользователей в данном опросе
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
                    // задаем цвет фона песочного цвета
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
                            int newAllAnswerSum = (allAnswersSum + 1);

                            // получаем позицию ответа в списке ответов
                            int selectedAnswerPos = quizAnswersList.indexOf(arr);

                            // будем хранить числовое значение содержащееся в "поле с кол-вом пользователей выбравших данный ответ в опросе"
                            int newAnswersSum = (Integer.parseInt(answersSumTV.getText().toString()) + 1);

                            // запускаем обновление опроса: фонов под ответами и общего кол-ва проголосовавших в опросе
                            resetQuizAnswersBG(quizLL, newAllAnswerSum, selectedAnswerPos, newAnswersSum);
                        }
                    }
                });

                // создаем "поле с текстом ответа"
                TextView answerTextTV = new TextView(context);

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

                // ограничиваем ответ опроса, чтоб было видно кол-во пользователей выбравших данный ответ
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

                // формируем массив публикаций, на которые жаловался пользователь и их надо скрыть
                do {

                    String[] dataArr = new String[3];

                    dataArr[0] = cursor2.getString(cursor2.getColumnIndex("id"));
                    dataArr[1] = cursor2.getString(cursor2.getColumnIndex("quiz_id"));
                    dataArr[2] = cursor2.getString(cursor2.getColumnIndex("answer"));

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

                int selectedAnswerSum = 0;

                if((selectedAnswerPosition >= 0) && (i == selectedAnswerPosition)) {
                    // обновляем значение в "поле с кол-вом пользователей выбравших данный ответ в опросе"
                    answersSumTV.setText("" +selectedAnswerNewSum);

                    // запоминаем новое значение "поля с кол-вом пользователей выбравших данный ответ в опросе"
                    selectedAnswerSum = selectedAnswerNewSum;
                }
                else
                    // получаем числовое значение содержащееся в "поле с кол-вом пользователей выбравших данный ответ в опросе"
                    selectedAnswerSum = Integer.parseInt(answersSumTV.getText().toString());

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
    private void lockQuiz(LinearLayout quizLL) {
        // проходим циклом по опросу
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
    private int getQuizAnswersSum(String quiz_id, boolean forCurrentUser) {

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<String> quizAnswersIdList = new ArrayList<String>();

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

            // формируем массив идентификаторов ответов в заданной опросе
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
            quizAnswersIdList.add("" + user_id);
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
    private boolean isQuizAnsweredByUser(String quiz_id) {

        if(getQuizAnswersSum(quiz_id, true) > 0 )
            return true;
        else
            return false;
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

        int userAnswerId = dbHelper.addRow(db, table_name, new String[]{"quiz_answer_id", "user_id"}, new String[]{"" + quiz_answer_id, "" + user_id});

        if(userAnswerId > 0)
            return true;
        else
            return false;
    }

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
        StringBuilder sqlQuery = new StringBuilder("select * from publication_data ");

        ArrayList<String> sqlDataList = new ArrayList<String>();

        ////////////////////////////////////////////////////////////////////////////////////

        // получаем кол-во бейджиков в состоянии "выключен"
        int badgesOffSum = getUserBadgesOffList().size();

        // получаем кол-во публикаций, на которые пожаловался пользователь и их надо срыть
        int claimedPublicationsSum = getClaimedPublicationsList().size();

        // будем хранить здесь идентификаторы скрываемых публикаций
        String[] claimedPublicationsArr = null;

        ////////////////////////////////////////////////////////////////////////////////////

        // если выключенные бейджики у пользователя есть
        if((badgesOffSum > 0) || (claimedPublicationsSum > 0)) {

            // формируем условие для отсева публикаций с таким типом бейджика
            sqlQuery.append("where ");

            if(badgesOffSum > 0) {

                sqlQuery.append("badge_id NOT IN(? ");

                // накапливаем символы подстановки для каждого выключенного бейджа
                for(int i=1; i<badgesOffSum; i++)
                    sqlQuery.append(",?");

                // добавляем данные в список для отсева публикаций
                sqlDataList = getUserBadgesOffList();

                // завершаем формирование условия
                sqlQuery.append(") ");

                if(claimedPublicationsSum > 0)
                    sqlQuery.append(" AND ");
            }

            if(claimedPublicationsSum > 0) {

                sqlQuery.append("id NOT IN(? ");

                // накапливаем символы подстановки для каждой скрываемой публикации
                for(int i=1; i<claimedPublicationsSum; i++)
                    sqlQuery.append(",?");

                // получаем массив идентификаторов выключенных бейджиков
                claimedPublicationsArr = getClaimedPublicationsList().toArray(new String[claimedPublicationsSum]);

                // добавляем данные в список для отсева публикаций
                for(String str: claimedPublicationsArr)
                    sqlDataList.add(str);

                // завершаем формирование условия отсева публикаций
                sqlQuery.append(") ");
            }
        }

        // формируем в запросе условие для сортировки публикаций
        sqlQuery.append("order by enter_date DESC ");

        // отправляем запрос БД и получаем его результат
        cursor = db.rawQuery(sqlQuery.toString(), sqlDataList.toArray(new String[sqlDataList.size()]));

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
            int publicationSreetNameColIndex    = cursor.getColumnIndex("street_name");

            // формируем массив публикаций
            do {
                float publicationLatitude  = Float.parseFloat("" +cursor.getString(cursor.getColumnIndex("map_latitude")));
                float publicationLongitude = Float.parseFloat("" +cursor.getString(cursor.getColumnIndex("map_longitude")));

                // если новость попадает в радиус пользователя
                if(getDistance(new LatLng(publicationLatitude, publicationLongitude),location) < radius) {

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
                    dataBlock[14] = "" +cursor.getString(publicationSreetNameColIndex);

                    resultArrList.add(dataBlock);
                }
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
    private ArrayList<String> getUserBadgesOffList() {

        ArrayList<String> resultList = new ArrayList<String>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ////////////////////////////////////////////////////////////////////////////////////

        // формируем запрос к БД
        Cursor cursor = db.rawQuery("select badge_id" +
                        "  from user_badge_off_data " +
                        " where user_id = ? ",
                new String[]{"" + user_id});

        // если данные в запросе получены
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // формируем массив выключенных бейджиков пользователя
            do {
                resultList.add(cursor.getString(cursor.getColumnIndex("badge_id")));
            } while (cursor.moveToNext());
        }

        // закрываем курсор
        cursor.close();

        // возвращем результат
        return resultList;
    }

    //
    private ArrayList<String> getClaimedPublicationsList() {

        ArrayList<String> resultList = new ArrayList<String>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ////////////////////////////////////////////////////////////////////////////////////

        // формируем запрос к БД
        Cursor cursor = db.rawQuery("select publication_id" +
                                    "  from claim_data " +
                                    " where claim_user_id = ? " +
                                    "   and hide_publication = ?",
                                    new String[]{"" + user_id, "true"});

        // если данные в запросе получены
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // формируем массив публикаций, на которые жаловался пользователь и их надо скрыть
            do {
                resultList.add(cursor.getString(cursor.getColumnIndex("publication_id")));
            } while (cursor.moveToNext());
        }

        // закрываем курсор
        cursor.close();

        // возвращем результат
        return resultList;
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
        String[] selectionArgs  = new String[]{"" +publicationId, "" +user_id};

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
                    dialog.dismiss();

                    //
                    showClaimDialog(publicationId);
                }
            });
        }

        // возвращаем оранжевый контейнер-кнопку
        return orangeTextViewLL;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void setMyCurrentLocation() {
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        if (googleApiClient.isConnected()) {
            Location point = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            latitude  = Float.parseFloat("" +point.getLatitude());
            longitude = Float.parseFloat("" + point.getLongitude());

            saveTextInPreferences("map_latitude",  "" +latitude);
            saveTextInPreferences("map_longitude",  "" +longitude);
        }

        //
        googleApiClient.disconnect();
    }

    //
    private void setLocationName() {

        // получаем данные местности
        ArrayList<String> locationData = getLocationData(new LatLng(latitude, longitude));

        int locationDataSize = locationData.size();

        // отобразить данные в зависимости от количества фрагментов адреса объект
        switch(locationDataSize) {

            case 1:
                // получен только город/область/страна
                saveTextInPreferences("region_name", locationData.get(0).toString());
                break;
            case 2:
                // получены город/область/страна и название улицы
                saveTextInPreferences("region_name", locationData.get(0).toString());
                saveTextInPreferences("street_name", locationData.get(1).toString());
                break;
        }
    }

    //
    private void showDeleteDialog(final int publicationId) {

        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(Tape_Activity.this);

        // deleteDialog.setTitle("Удаление публикации.");  // заголовок
        deleteDialog.setTitle(context.getResources().getString(R.string.deleting_publication_text));  // заголовок

        // deleteDialog.setMessage("Хотите удалить публикацию? "); // сообщение
        deleteDialog.setMessage(context.getResources().getString(R.string.delete_publication_answer_text)); // сообщение

        // deleteDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
        deleteDialog.setPositiveButton(context.getResources().getString(R.string.yes_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                // Toast.makeText(context, "Публикация будет удалена...", Toast.LENGTH_LONG).show();

                // удаление публикации из БД
                deletePublication(publicationId);

                // удаление публикации из ленты
                deletePublicationFromTape("" +publicationId);
            }
        });

        deleteDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
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

        // создаем диалоговое окно
        final Dialog dialog = new Dialog(Tape_Activity.this, R.style.InfoDialog_Theme);
        dialog.setContentView(R.layout.dialog_claim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // получаем идентификаторы цветов, для раскраски фонов и текста в окне
        final int whiteColor    = context.getResources().getColor(R.color.white);
        final int orangeColor   = context.getResources().getColor(R.color.selected_item_orange);
        final int blueColor     = context.getResources().getColor(R.color.user_name_blue);

        // создаем "чекбокс необходимости скрыть публикацию из ленты жалующегося пользователя"
        final CheckBox chBox    = (CheckBox) dialog.findViewById(R.id.ClaimDialog_HidePublicationChBox);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.row_provocation_type, getProvocationTypesArray());

        ListView listView = (ListView) dialog.findViewById(R.id.ClaimDialog_ProvocationTypeLV);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView textView;

                for (int i = 0; i < parent.getChildCount(); i++) {
                    textView = (TextView) parent.getChildAt(i);
                    textView.setTextColor(blueColor);
                    textView.setBackgroundColor(Color.TRANSPARENT);
                }

                view.setSelected(true);

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

                // отправляем жалобу пользователя в БД
                sendClaim(publication_id, chBox.isChecked());

                // если публикацию необходимо удалить из ленты
                if(chBox.isChecked())
                    // удаляем публикацию из ленты
                    deletePublicationFromTape("" +publication_id);

                // закрываем "диалоговое окно отправки жалобы"
                dialog.dismiss();
            }
        });

        // показываем сформированное диалоговое окно
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

        ArrayList<String> dataList = new ArrayList<String>();

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String sqlQuery = "select * from provocation_type_data";

        ///////////////////////////////////////////////////////////////////////////////////////

        Cursor cursor = db.rawQuery(sqlQuery.toString(), new String[]{});

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // определяем номера столбцов по имени в выборке
            int provocationTypeIdColIndex   = cursor.getColumnIndex("id");
            int provocationTypeNameColIndex = cursor.getColumnIndex("type_name");

            do {
                if(cursor.getInt(provocationTypeIdColIndex) != 1)
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
        this.selectedProvocationType = selectedItemPosition;
    }

    //
    private void sendClaim(int publication_id, boolean hidePublication){

        DB_Helper dbHelper = new DB_Helper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //////////////////////////////////////////////////////////////////

        if(selectedProvocationType == 0)
            selectedProvocationType++;

        String table_name   = "claim_data";

        String[] columnsArr = { "publication_id",
                                "provocation_type_id",
                                "claim_user_id",
                                "claim_date",
                                "hide_publication" };

        String[] data = {   "" +publication_id,
                            "" +selectedProvocationType,
                            "" +user_id,
                            "" +System.currentTimeMillis(),
                            "" +hidePublication };

        // добавляем запись в БД, получая ее идентификатор на выходе
        dbHelper.fillTable(db, table_name, columnsArr, data);

        // закрываем подключение к БД
        dbHelper.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //
    // private int getListItemPositionByPublicationId(String publication_id) {
    private int getListItemPositionByPublicationId(int publicationId) {

        int itemPosition = -1;

        // int publicationId = Integer.parseInt(publication_id);

        for(int i=0; i<listItemsTapeList.size(); i++) {

            if(listItemsTapeList.get(i).getPublicationId() == publicationId)
                itemPosition = i;
        }

        return itemPosition;
    }

    //
    private void deletePublicationFromTape(String publication_id) {

        // получаем идентификатор публикации для удаления из ленты
        int publicationId = Integer.parseInt(publication_id);

        // получаем позицию публикации в ленте
        int itemPosition = getListItemPositionByPublicationId(publicationId);

        // если позиция получена
        if(itemPosition >= 0) {

            // удаляем публикацию из списка публикаций и перезагружаем ленту
            listItemsTapeList.remove(itemPosition);
            adapter.notifyItemRemoved(itemPosition);
        }
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

    // возвращаем список данных по найденной точке на карте
    private ArrayList<String> getLocationData(LatLng point) {

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

        // вернуть результат
        return list;
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

    //
    private void showPD() {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    //
    private void hidePD() {
        if(progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
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
            region_name = shPref.getString("region_name", "");
        }

        // если настройки содержат широту
        if(shPref.contains("map_latitude")) {
            // значит можно получить значение для геолокации
            latitude  = Float.parseFloat(shPref.getString("map_latitude", "0"));
        }

        // если настройки содержат долготу
        if(shPref.contains("map_longitude")) {
            // значит можно получить значение для геолокации
            longitude = Float.parseFloat(shPref.getString("map_longitude", "0"));
        }

        // если настройки содержат радиус
        if(shPref.contains("map_radius")) {
            // значит можно получить значение для геолокации
            radius = (Double.parseDouble(shPref.getString("map_radius", "0")) / 1000);
        }

        // если настройки содержат публикацию на которую надо установить фокус
        if(shPref.contains("focusPosition")) {

            String focus_position = shPref.getString("focusPosition", "0");

            if((focus_position != null) && (!focus_position.equals("")))
              // значит можно получить значение
              focusPosition = Integer.parseInt(focus_position);
        }

        location = new LatLng(latitude, longitude);
    }
}