package com.example.myapplication.datahandlers.models;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

public class CategoriesModel extends InstanceModel {

    public static String associatedTable;

    private String name;
    private String type;

    private String parentName;
    private String parentType;

    public enum FIELDS{
        NAME("NAME","TEXT"),
        TYPE("TYPE","TEXT"),
        PARENT_NAME("PARENT_NAME","TEXT"),
        PARENT_TYPE("PARENT_TYPE","TEXT");

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

    public CategoriesModel(){}

    public CategoriesModel(String name, String type, String parentName, String parentType) {
        this.name = name;
        this.type = type;
        this.parentName = parentName;
        this.parentType = parentType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentType() {
        return parentType;
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
        cv.put(FIELDS.TYPE.getSqlName(), type);
        cv.put(FIELDS.PARENT_NAME.getSqlName(), parentName);
        cv.put(FIELDS.PARENT_TYPE.getSqlName(), parentType);

        return cv;
    }

    @Override
    public InstanceModel fromCursor(Cursor cv) {
        this.name= cv.getString(0);
        this.type = cv.getString(1);
        this.parentName = cv.getString(2);
        this.parentType = cv.getString(3);
        return this;
    }
}
