package com.androiditgroup.loclook.utils_pkg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.androiditgroup.loclook.R;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by admin on 13.09.2015.
 */
public class DB_Helper extends SQLiteOpenHelper {

    static String DB_NAME = "LocLookDB";

    private Context context;

    static int DB_VER     = 1;
    static int DB_OLD_VER = (DB_VER - 1);
    static int DB_NEW_VER = DB_VER;

    // final String LOG_TAG = "myLogs_01_10_2015_" + DB_VER;
    final String LOG_TAG = "myLogs";

    public DB_Helper(Context context) {
        // конструктор суперкласса
        super(context, DB_NAME, null, DB_VER);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(LOG_TAG, "--- onCreate database ---");

        badgeMethod(db);
        userDataMethod(db);
        userAuthDataMethod(db);
        userBadgeOffMethod(db);
        publicationDataMethod(db);
        quizDataMethod(db);
        quizAnswerDataMethod(db);
        favoritePublicationMethod(db);
        likedPublicationMethod(db);
        publicationAnswerMethod(db);
        provocationTypeDataMethod(db);
        claimDataMethod(db);
        userQuizAnswerMethod(db);
        publicationImageDataMethod(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d(LOG_TAG, " --- onUpgrade database from " +oldVersion+ " to " +newVersion+ " version --- ");

        if (oldVersion == DB_OLD_VER && newVersion == DB_NEW_VER) {

            db.beginTransaction();
            try {
                // badgeMethod(db);
                // userDataMethod(db);
                // userAuthDataMethod(db);
                // userBadgeOffMethod(db);
                // publicationDataMethod(db);
                // quizDataMethod(db);
                // quizAnswerDataMethod(db);
                // favoritePublicationMethod(db);
                // likedPublicationMethod(db);
                // publicationAnswerMethod(db);
                // provocationTypeDataMethod(db);
                // claimDataMethod(db);
                // userQuizAnswerMethod(db);
                // publicationImageDataMethod(db);

                // db.execSQL("DROP TABLE IF EXISTS claim_data");
                // onCreate(db);

                db.setTransactionSuccessful();
            }
            finally {
                db.endTransaction();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //
    private void createTable(SQLiteDatabase db,String tableName, String columns) {
        Log.d(LOG_TAG, "--- create table " + tableName + " ---");

        // String createQuery = "create table " +tableName+ " (" + "id integer primary key autoincrement," +columns+ ");";

        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("create table ");
        sqlQuery.append(tableName);
        sqlQuery.append(" (id integer primary key autoincrement, ");
        sqlQuery.append(columns);
        sqlQuery.append(");");

        // создаем таблицу с полями
        // db.execSQL(createQuery);
        db.execSQL(sqlQuery.toString());
    }

    //
    protected void clearTable(SQLiteDatabase db,String tableName) {
        int clearCount = db.delete(tableName, null, null);
        // Log.d(LOG_TAG, "deleted rows count = " + clearCount+ " from " +tableName);
    }

    //
    public void fillTable(SQLiteDatabase db, String tableName, String[] columnsArr, String[] dataArr) {
        ContentValues cv = new ContentValues();

        for(int i=0; i<columnsArr.length; i++)
            cv.put(columnsArr[i], dataArr[i]);

        // вставляем запись и получаем ее ID
        db.insert(tableName, null, cv);
        // Log.d(LOG_TAG, "row inserted in table " +tableName);
    }

    //
    public int addRow(SQLiteDatabase db, String tableName, String[] columnsArr, String[] dataArr) {
        ContentValues cv = new ContentValues();
        long rowID;

        for(int i=0; i<columnsArr.length; i++)
            cv.put(columnsArr[i], dataArr[i]);

        // вставляем запись и получаем ее ID
        rowID = db.insert(tableName, null, cv);
        // Log.d(LOG_TAG, "row inserted, ID = " + rowID + " in table " +tableName);

        return (int) rowID;
    }

    //
    public void updateTable(SQLiteDatabase db,String tableName,int rowId,String[] columnsArr,String[] dataArr) {
        ContentValues cv = new ContentValues();

        for(int i=0; i<columnsArr.length; i++) {
            cv.put(columnsArr[i], dataArr[i]);
        }

        db.update(tableName, cv, "id = ?", new String[]{"" + rowId});
        // Log.d(LOG_TAG, "row updated, ID = " +rowId+ " in table " +tableName);
    }

    public void showAllTableData(SQLiteDatabase db,String table_name) {
        Cursor cursor = db.query(table_name, null, null, null, null, null, null);

        String[] cursorArr = cursor.getColumnNames();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            Log.d(LOG_TAG, "----- Table: " + table_name + " -----");

            StringBuilder sb1 = new StringBuilder();

            for(int i=0; i<cursorArr.length; i++) {
                sb1.append("" + cursorArr[i] + " \t\t");
            }

            Log.d(LOG_TAG, sb1.toString());

            do {
                StringBuilder sb2 = new StringBuilder();

                for(int i=0; i<cursorArr.length; i++) {
                    sb2.append("" +cursor.getString(cursor.getColumnIndex(cursorArr[i])) + "\t\t");
                }

                Log.d(LOG_TAG, sb2.toString());

            } while (cursor.moveToNext());
        }

        cursor.close();

        Log.d(LOG_TAG, "--------------------------------------------------");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void badgeMethod(SQLiteDatabase db) {

        String table_name = "badge_data";

        String table_columns = "name text";

        /////////////////////////////////////////////////////////////////////////////////////

        createTable(db,table_name,table_columns);

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        String[] columnsArr = {"name"};

//        String[] badgesArr = new String[] { "LocLook",          "Интересные места",
//                "Одобрение",        "Осуждение",
//                "Важное",           "Вопрос",
//                "Спорт",            "Объявления",
//                "ДТП",              "Сплетни",
//                "Олень",            "Срочная новость",
//                "Митинг",           "Плохое обслуживание",
//                "Работа",           "Мероприятие",
//                "Спросить дорогу",  "Учеба",
//                "Игры",             "Новость",
//                "День рождения",    "ЧП",
//                "Тусовка",          "Отношения"};

        String[] badgesArr = context.getResources().getStringArray(R.array.badges);

        for(int i=0; i<badgesArr.length; i++)
            fillTable(db,table_name,columnsArr,new String[]{badgesArr[i]});

        /*fillTable(db,table_name,columnsArr,news_type1);
        fillTable(db,table_name,columnsArr,news_type2);
        fillTable(db,table_name,columnsArr,news_type3);
        fillTable(db,table_name,columnsArr,news_type4);
        fillTable(db,table_name,columnsArr,news_type5);
        fillTable(db,table_name,columnsArr,news_type6);*/

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    //
    private void userDataMethod(SQLiteDatabase db) {

        String table_name = "user_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("login");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("phone_number");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("rate");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("bg_path");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("avatar_path");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("about_me");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("site");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("map_latitude");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("map_longitude");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("map_radius");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("region_name");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("street_name");
        columnsSB.append(" text");

        // String table_columns = "login text," + "phone_number integer," + "rate integer," + "map_latitude text," + "map_longitude text," + "map_radius integer," + "region_name text," + "street_name text";
        // String table_columns = "login text," + "phone_number integer," + "rate integer," + "bg_path text," + "avatar_path text," + "about_me text," + "site text," + "map_latitude text," + "map_longitude text," + "map_radius integer," + "region_name text," + "street_name text";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

/*        String[] columnsArr = {"login","phone_number","rate","bg_path","avatar_path","about_me","site","map_latitude","map_longitude","map_radius","region_name","street_name"};

        String[][] user_data = {
                                    {"Иван Лось",           "9011234567",   "0",    "", "", "", "", "54.5165",      "36.2419",      "200", "", ""},
                                    {"Михаил Делягин",      "9021234567",   "150",  "", "", "", "", "53.239627",    "50.2818155",   "200", "", ""},
                                    {"Василий Тихомиров",   "9031234567",   "275",  "", "", "", "", "59.9396974",   "29.5302838",   "200", "", ""},
                                    {"Арсений Гаврилов",    "9041234567",   "40",   "", "", "", "", "46.3601519",   "47.9166724",   "200", "", ""},
                                    {"Евгений Полищук",     "9051234567",   "59",   "", "", "", "", "54.8088526",   "55.7406911",   "200", "", ""},
                                    {"Виталий Денисов",     "9061234567",   "249",  "", "", "", "", "51.7910686",   "54.9623392",   "200", "", ""}
                                };

        for(int i=0; i<user_data.length; i++)
        fillTable(db,table_name,columnsArr,user_data[i]);*/

        /////////////////////////////////////////////////////////////////////////////////////

        /*
        db.execSQL("alter table user_data add column map_latitude text;");
        db.execSQL("alter table user_data add column map_longitude text;");
        db.execSQL("alter table user_data add column map_radius integer;");
        db.execSQL("alter table user_data add column region_name text;");
        db.execSQL("alter table user_data add column street_name text;");
        */

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void userAuthDataMethod(SQLiteDatabase db) {

        String table_name = "user_auth_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("user_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("enter_date");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("enter_code");
        columnsSB.append(" integer");

        // String table_columns = "user_id integer," + "enter_date text," + "enter_code integer";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void userBadgeOffMethod(SQLiteDatabase db) {

        String table_name = "user_badge_off_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("user_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("badge_id");
        columnsSB.append(" integer");

        // String table_columns = "user_id integer," + "badge_id integer";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void publicationDataMethod(SQLiteDatabase db) {

        String table_name = "publication_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("user_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("enter_text");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("enter_date");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("anonymous");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("quiz_added");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("badge_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("map_latitude");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("map_longitude");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("region_name");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("street_name");
        columnsSB.append(" text");

        // String table_columns = "user_id integer," + "enter_text text," + "enter_date text," + "anonymous text," + "quiz_added text,"  + "badge_id integer, " + "map_latitude text," + "map_longitude text," + "region_name text," + "street_name text";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void quizDataMethod(SQLiteDatabase db) {

        String table_name = "quiz_data";

        String table_columns = "publication_id integer";

        /////////////////////////////////////////////////////////////////////////////////////

        createTable(db,table_name,table_columns);

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void quizAnswerDataMethod(SQLiteDatabase db) {

        String table_name = "quiz_answer_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("quiz_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("answer");
        columnsSB.append(" text");

        // String table_columns = "quiz_id integer," + "answer text";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void favoritePublicationMethod(SQLiteDatabase db) {

        String table_name = "favorite_publication_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("publication_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("user_id");
        columnsSB.append(" integer");

        // String table_columns = "publication_id integer," + "user_id integer";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void likedPublicationMethod(SQLiteDatabase db) {

        String table_name = "liked_publication_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("publication_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("user_id");
        columnsSB.append(" integer");

        // String table_columns = "publication_id integer," + "user_id integer";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void publicationAnswerMethod(SQLiteDatabase db) {

        String table_name = "publication_answer_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("publication_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("user_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("recipient_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("answer_text");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("answer_date");
        columnsSB.append(" text");

        // String table_columns =  "publication_id integer," + "user_id integer," + "recipient_id integer," + "answer_text text," + "answer_date text";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db, table_name);

        // db.execSQL("alter table " +table_name+ " add column answer_date text;");

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void provocationTypeDataMethod(SQLiteDatabase db) {

        String table_name = "provocation_type_data";

        String table_columns = "type_name text";

        /////////////////////////////////////////////////////////////////////////////////////

        createTable(db,table_name,table_columns);

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        String[] columnsArr = {"type_name"};

        // String[] provocationTypeArr = new String[] { "Спам", "Оскорбление", "Материал для взрослых", "Пропаганда наркотиков", "Детская порнография", "Насилие/экстремизм" };
        String[] provocationTypeArr = context.getResources().getStringArray(R.array.provocation_types);

        for(int i=0; i<provocationTypeArr.length; i++)
            fillTable(db,table_name,columnsArr,new String[]{provocationTypeArr[i]});

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void claimDataMethod(SQLiteDatabase db) {

        String table_name = "claim_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("publication_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("provocation_type_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("claim_user_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("claim_date");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("hide_publication");
        columnsSB.append(" text");

//        String table_columns =  "publication_id integer," + "provocation_type_id integer," + "claim_user_id integer," + "claim_date text";
        // String table_columns =  "publication_id integer," + "provocation_type_id integer," + "claim_user_id integer," + "claim_date text," + "hide_publication text";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db, table_name);

        // db.execSQL("alter table " +table_name+ " add column answer_date text;");

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void userQuizAnswerMethod(SQLiteDatabase db) {

        String table_name = "user_quiz_answer_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("quiz_answer_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("user_id");
        columnsSB.append(" integer");

        // String table_columns = "quiz_answer_id integer," + "user_id integer";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    private void publicationImageDataMethod(SQLiteDatabase db) {

        String table_name = "publication_image_data";

        StringBuilder columnsSB = new StringBuilder("");

        columnsSB.append("publication_id");
        columnsSB.append(" integer");
        columnsSB.append(",");

        columnsSB.append("image_path");
        columnsSB.append(" text");
        columnsSB.append(",");

        columnsSB.append("rotate_degree");
        columnsSB.append(" real");

        // String table_columns =  "publication_id integer," + "image_path text," + "rotate_degree real";

        /////////////////////////////////////////////////////////////////////////////////////

        // createTable(db,table_name,table_columns);
        createTable(db,table_name,columnsSB.toString());

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db,table_name);

        /////////////////////////////////////////////////////////////////////////////////////

        // clearTable(db, table_name);

        // db.execSQL("alter table " +table_name+ " add column answer_date text;");

        /////////////////////////////////////////////////////////////////////////////////////

        showAllTableData(db,table_name);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String createUser(SQLiteDatabase db,String userName,String phoneNumber) {
        // int userId = 0;

        String userId = "";

        String table_name = "user_data";

        String[] userDataArr = getUserData(db, phoneNumber);

        if(userDataArr.length == 0 || userDataArr[0] == null || userDataArr[0] == "") {
            String[] columnsArr = {"login", "phone_number", "rate"};
            String[] dataArr    = {userName, phoneNumber, "0"};

            userId = "" +addRow(db, table_name, columnsArr, dataArr);
        }
        else {
            String[] columnsArr = {"login"};
            String[] dataArr    = {userName};

            userId = userDataArr[0];

            updateTable(db,table_name,Integer.parseInt(userId),columnsArr,dataArr);
        }

        return userId;
    }

    //
    public String getUserId(SQLiteDatabase db, String phoneNumber) {

        String result = "";

        String tableName        = "user_data";
        String selection        = "phone_number = ?";
        String[] selectionArgs  = new String[]{phoneNumber};

        Cursor cursor = db.query(tableName, null, selection, selectionArgs, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            result = cursor.getString(cursor.getColumnIndex("id"));
        }

        return result;
    }

    //
    public String getUserName(SQLiteDatabase db, String userId) {

        String result = "";

        String[] columns        = new String[]{"login"};
        String selection        = "id = ?";
        String[] selectionArgs  = new String[]{userId};

        Cursor cursor = db.query("user_data", columns, selection, selectionArgs, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            result = cursor.getString(cursor.getColumnIndex("login"));
        }

        return result;
    }

    //
    public String[] getProfileUserData(SQLiteDatabase db, String profileUserId) {

        ArrayList<String> userDataArrList = new ArrayList<String>();

        String tableName        = "user_data";
        String[] columns        = new String[]{"login","bg_path","avatar_path","about_me","site","region_name","street_name"};
        String selection        = "id = ?";
        String[] selectionArgs  = new String[]{profileUserId};

        Cursor cursor = db.query(tableName, columns, selection, selectionArgs, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            userDataArrList.add(cursor.getString(cursor.getColumnIndex("login")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("bg_path")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("avatar_path")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("about_me")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("region_name")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("street_name")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("site")));
        }

        // приводим список с ответами к текстовому массиву
        String[] userData = userDataArrList.toArray(new String[userDataArrList.size()]);

        return userData;

    }

    //
    public String[] getUserData(SQLiteDatabase db,String phoneNumber) {

        ArrayList<String> userDataArrList = new ArrayList<String>();

        String tableName        = "user_data";
        String[] columns        = new String[]{"id","login","map_latitude","map_longitude","map_radius","region_name","street_name"};
        String selection        = "phone_number = ?";
        String[] selectionArgs  = new String[]{phoneNumber};

        Cursor cursor = db.query(tableName, columns, selection, selectionArgs, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            userDataArrList.add(cursor.getString(cursor.getColumnIndex("id")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("login")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("map_latitude")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("map_longitude")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("map_radius")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("region_name")));
            userDataArrList.add(cursor.getString(cursor.getColumnIndex("street_name")));
        }

        // приводим список с ответами к текстовому массиву
        String[] userData = userDataArrList.toArray(new String[userDataArrList.size()]);

        return userData;
    }

    public boolean userExists(SQLiteDatabase db,String phoneNumber) {

        // ответ по-умолчанию отрицательный
        boolean result = false;

        String tableName        = "user_data";
        // String[] columns        = new String[]{"id","login","map_latitude","map_longitude","map_radius","region_name","street_name"};
        String selection        = "phone_number = ?";
        String[] selectionArgs  = new String[]{phoneNumber};

        Cursor cursor = db.query(tableName, null, selection, selectionArgs, null, null, null);

        // если пользователь найден
        if(cursor.getCount() > 0)
            // вернуть положительный ответ
            result = true;

        return result;
    }
    //
    // public void setEnterCode(SQLiteDatabase db,int userId, int randomValue) {
    public void setEnterCode(SQLiteDatabase db,String userId, int randomValue) {

        String table_name   = "user_auth_data";

        String[] columnsArr = {"user_id", "enter_date", "enter_code"};

        String[] user_auth_data = { userId,"" + Calendar.getInstance().getTime(),"" +randomValue};

        //////////////////////////////////////////////////////////////////////////////

        String[] columns        = new String[]{"id"};
        String selection        = "user_id = ?";
        String[] selectionArgs  = new String[]{userId};

        Cursor uadCursor = db.query("user_auth_data", columns, selection, selectionArgs, null, null, null);

        if(uadCursor.getCount() > 0) {

            uadCursor.moveToFirst();

            int rowId = Integer.parseInt(uadCursor.getString(uadCursor.getColumnIndex("id")));

            updateTable(db,table_name,rowId,new String[] {"enter_date","enter_code"},new String[] {"" +Calendar.getInstance().getTime(),"" +randomValue});
        }
        else
            fillTable(db,table_name,columnsArr,user_auth_data);
    }
}