package com.example.hellomap;
//Name: yunyi zheng
//Student number:s1923021
//description: using SQL API to generate the database, which can contain multiple locations, and the SQL can be read and write by the other modules.

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class   DatabaseHelper extends SQLiteOpenHelper {

    //Some information about the database
    public static final String DATABASE_NAME="Address.db";
    private static final String TABLE_NAME = "address_table";
    private static final String COL1 = "ID";
    private static final String COL2 = "NAME";
    private static final String COL3 = "LAT";
    private static final String COL4 = "LNT";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
                                                            //PRIMARY KEY for identification
        db.execSQL(" create table "+ TABLE_NAME+"(ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,LAT DOUBLE,LNT DOUBLE)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }
    // this block is designed when the user want to enter multiple lcation, because this will allow the program insert more locations
    public boolean insertLocation(String name,String lat,String lnt)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2,name);
        contentValues.put(COL3,lat);
        contentValues.put(COL4,lnt);
        //Insert instruction here
        long returnResult = db.insert(TABLE_NAME,null,contentValues);
        //If not success, it would return -1
        if(returnResult== -1)
            return false;
        else
            return true;
    }
    //show all the location on the map
    public Cursor getAllLocation()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from "+TABLE_NAME,null);
    }

    public Cursor findExistedLocation(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query(TABLE_NAME,null,COL1+"="+id,null,null,null,null);
    }


    public boolean updateData(String id,String name,String lat,String lnt)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1,id);
        contentValues.put(COL2,name);
        contentValues.put(COL3,lat);
        contentValues.put(COL4,lnt);
        db.update(TABLE_NAME,contentValues,"ID = ?", new String[]{id});
        return true;
    }

    public boolean deleteALLData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,null,null);
        return true;
    }
    public boolean deleteData(String id,String name,String lat,String lnt) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,"ID = ?", new String[]{id});
        return true;
    }




}
