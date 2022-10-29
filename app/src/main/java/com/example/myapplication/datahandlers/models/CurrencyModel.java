package com.example.myapplication.datahandlers.models;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

public class CurrencyModel extends InstanceModel {

    public static String associatedTable;

    private String name;

    public enum FIELDS{

        NAME("CURRENCY_NAME","TEXT");


        private final String sqlName;
        private final String sqlType;
        FIELDS(String sqlName,String sqlType) {
            this.sqlName =sqlName;
            this.sqlType=sqlType;
        }
        public String getSqlName() {
            return sqlName;
        }
        public String getSqlType(){
            return sqlType;
        }
    }

    public CurrencyModel(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getAssociatedTable() {
        return associatedTable;
    }

    @Override
    public List<String> fieldNames() {
        return null;
    }

    @Override
    public ContentValues toCursor() {

        ContentValues cv = new ContentValues();

        cv.put(FIELDS.NAME.getSqlName(), name);

        return cv;
    }

    @Override
    public InstanceModel fromCursor(Cursor cursor) {

        this.name = cursor.getString(0);

        return this;
    }
}
