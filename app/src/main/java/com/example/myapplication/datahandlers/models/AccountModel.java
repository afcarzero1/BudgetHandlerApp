package com.example.myapplication.datahandlers.models;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

public class AccountModel extends InstanceModel {

    public static String associatedTable;

    private String name;
    private String currency;
    private Float initialBalance;

    public enum FIELDS{
        NAME("ACCOUNT_NAME","TEXT"),
        CURRENCY("CURRENCY","TEXT"),
        INITIAL_BALANCE ("INITIAL_BALANCE","FLOAT");

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

    public AccountModel(String name, String currency, Float initialBalance) {
        this.name = name;
        this.currency = currency;
        this.initialBalance = initialBalance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getInitialBalance() {
        return initialBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setInitialBalance(Float initialBalance) {
        this.initialBalance = initialBalance;
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

        cv.put(FIELDS.NAME.getSqlName(),name);
        cv.put(FIELDS.CURRENCY.getSqlName(),currency);
        cv.put(FIELDS.INITIAL_BALANCE.getSqlName(),initialBalance);

        return cv;
    }

    @Override
    public InstanceModel fromCursor(Cursor cursor) {
        this.name = cursor.getString(0);
        this.currency = cursor.getString(1);
        this.initialBalance = cursor.getFloat(2);
        return this;
    }
}
