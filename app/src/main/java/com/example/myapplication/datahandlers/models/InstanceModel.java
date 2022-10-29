package com.example.myapplication.datahandlers.models;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

/**
 * This class represents the generic instance of an SQL table.
 */
public abstract class InstanceModel {

    public abstract String getAssociatedTable();

    /**
     * Create an instance of class from a cursor. It uses the method fromCursor of the object
     * @param cv : Cursor from which object is created
     * @param clazz : Class of the object to be created
     * @param <T> : Class of the obejct to be created
     * @return : New instance.
     */
    public static <T extends InstanceModel> T instanceFromCursor(Cursor cv, Class<T> clazz){

        try {
            T myObject = clazz.newInstance();
            myObject.fromCursor(cv);
            return myObject;
        } catch (Exception e){
            throw new RuntimeException("Failed to create instance");
        }
    }

    public abstract  List<String> fieldNames();
    public abstract ContentValues toCursor();
    public abstract InstanceModel fromCursor(Cursor cv);
}
