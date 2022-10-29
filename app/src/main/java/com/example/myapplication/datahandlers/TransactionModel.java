package com.example.myapplication.datahandlers;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.myapplication.datahandlers.models.InstanceModel;

import java.util.List;

public class TransactionModel extends InstanceModel {

    public static String associatedTable;

    //public static final TransactionModel INSTANCE = new TransactionModel();

    private int id;
    private String type;
    private String category;
    private String account;
    private String initialDate;
    private String recurrenceCategory;
    private Float recurrenceValue;
    private Float value; //todo : use integer to represent value (fixed point)

    public enum FIELDS{
        ID ("ID","INTEGER")
        , TYPE("TYPE","TEXT")
        , CATEGORY("CATEGORY","TEXT")
        , ACCOUNT("ACCOUNT","TEXT")
        , INITIAL_DATE("INITIAL_DATE","TEXT")
        , RECURRENCE_TYPE("RECURRENCE_TYPE","TEXT")
        , RECURRENCE_VALUE ("RECURRENCE_VALUE","FLOAT")
        , VALUE ("VALUE","FLOAT");

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

    public TransactionModel() {
    }


    public TransactionModel(int id, String type, String category, String account, String initialDate, String recurrenceCategory, Float recurrenceValue, Float value) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.account = account;
        this.initialDate = initialDate;
        this.recurrenceCategory = recurrenceCategory;
        this.recurrenceValue = recurrenceValue;
        this.value = value;
    }

    //public TransactionModel(){};


    public int getId() {return id;}

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getInitialDate() {return initialDate;}

    public String getRecurrenceCategory() {
        return recurrenceCategory;
    }

    public Float getRecurrenceValue() {
        return recurrenceValue;
    }

    public Float getValue() {
        return value;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setInitialDate(String initialDate) {
        this.initialDate = initialDate;
    }

    public void setRecurrenceCategory(String recurrenceCategory) {
        this.recurrenceCategory = recurrenceCategory;
    }

    public void setRecurrenceValue(Float recurrenceValue) {
        this.recurrenceValue = recurrenceValue;
    }

    public void setValue(Float value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return "TransactionModel{" +
                "type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", recurrence_category='" + recurrenceCategory + '\'' +
                ", recurrence_value=" + recurrenceValue +
                ", value=" + value +
                '}';
    }

    @Override
    public String getAssociatedTable() {
        return "TRANSACTIONS";
    }

    @Override
    public List<String> fieldNames() {
        return null;
    }

    @Override
    public ContentValues toCursor() {
        ContentValues cv = new ContentValues();

        // skip 0 because it is the id
        cv.put(FIELDS.TYPE.getSqlName(), type);
        cv.put(FIELDS.CATEGORY.getSqlName(), category);
        cv.put(FIELDS.ACCOUNT.getSqlName(), account);
        cv.put(FIELDS.INITIAL_DATE.getSqlName(), initialDate);
        cv.put(FIELDS.RECURRENCE_TYPE.getSqlName(), recurrenceCategory);
        cv.put(FIELDS.RECURRENCE_VALUE.getSqlName(), recurrenceValue);
        cv.put(FIELDS.VALUE.getSqlName(), value);
        return cv;
    }

    @Override
    public TransactionModel fromCursor(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.type = cursor.getString(1);
        this.category = cursor.getString(2);
        this.account = cursor.getString(3);
        this.initialDate= cursor.getString(4);
        this.recurrenceCategory = cursor.getString(5);
        this.recurrenceValue = cursor.getFloat(6);
        this.value = cursor.getFloat(7);
        return this;
    }
}

