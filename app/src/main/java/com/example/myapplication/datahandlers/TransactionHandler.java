package com.example.myapplication.datahandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;


public class TransactionHandler extends SQLiteOpenHelper {

    // Columns
    public static final String TRANSACTIONS = "TRANSACTIONS";
    public static final String TYPE = "TYPE";
    public static final String CATEGORY = "CATEGORY";
    public static final String INITIAL_DATE = "INITIAL_DATE";
    public static final String RECURRENCE_TYPE = "RECURRENCE_TYPE";
    public static final String VALUE = "VALUE";
    public static final String RECURRENCE_VALUE = "RECURRENCE_" + VALUE;
    public static final String ID = "ID";

    public TransactionHandler(@Nullable Context context) {
        super(context, "transactions.db", null, 1);
    }


    /**
     * Creates the database
     * @param db The database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Type, Category, Initial_Date, Recurrency_Type, Recurrency_Value,Value


        String create_table_statement = "CREATE TABLE " + TRANSACTIONS + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TYPE + " TEXT ," + CATEGORY + " TEXT, " + INITIAL_DATE + " TEXT, " + RECURRENCE_TYPE + " TEXT," + RECURRENCE_VALUE + " FLOAT," + VALUE + " FLOAT)";


        db.execSQL(create_table_statement);
    }

    /**
     *
     * @param sqLiteDatabase
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public boolean addTransaction(TransactionModel transaction_model){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = transformModel(transaction_model);

        long insert = db.insert(TRANSACTIONS, null, cv);
        db.close();
        return insert != -1;
    }

    public boolean deleteTransaction(TransactionModel transaction_model){

        SQLiteDatabase db = this.getWritableDatabase();
        String query_string = "DELETE FROM " + TRANSACTIONS + " WHERE " + ID + " = " + transaction_model.getId();

        Cursor cursor = db.rawQuery(query_string,null);

        return cursor.moveToFirst();
    }

    public boolean deleteTransaction(int transaction_model_id){
        // Open database and define query
        SQLiteDatabase db = this.getWritableDatabase();
        String query_string = "DELETE FROM " + TRANSACTIONS + " WHERE " + ID + " = " + transaction_model_id;

        // Execute query and get result (should delete only one element)
        final int deleted_rows=db.delete(TRANSACTIONS,ID + "=?" ,new String[]{String.valueOf(transaction_model_id)});
        final boolean return_value= deleted_rows > 0;

        // Close cursor and database
        db.close();
        return return_value;
    }

    public boolean updateTransaction(int transaction_model_id , TransactionModel updated_transaction){
        SQLiteDatabase db = this.getWritableDatabase();
        String query_string = "UPDATE " +"SET "+ TRANSACTIONS + " WHERE " + ID + " = " + transaction_model_id;

        ContentValues cv = this.transformModel(updated_transaction);

        // Update the record with the desired model
        final int count=db.update(TRANSACTIONS,cv,ID + " =?",new String[]{String.valueOf(transaction_model_id)});

        // Return true if at least one row was updated
        return count!=0;
    }

    public List<TransactionModel> getAllTransactions(boolean ordered){

        List<TransactionModel> returnList = new ArrayList<>();


        String query = "SELECT * FROM " + TRANSACTIONS;
        if (ordered){
            query ="SELECT * FROM " + TRANSACTIONS+" ORDER BY " + INITIAL_DATE;
        }
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor  = db.rawQuery(query,null);

        if (cursor.moveToFirst()){
            do {
                TransactionModel tm = transformCursor(cursor);
                returnList.add(tm);
            }while (cursor.moveToNext());

        }else{
            // Database is empty
        }
        cursor.close();
        db.close();
        return returnList;
    }

    public TransactionModel getTransaction(int transaction_model_id){
        String query = "SELECT * FROM " + TRANSACTIONS + " WHERE " + ID + "= " + transaction_model_id;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor  = db.rawQuery(query,null);

        if(cursor.moveToFirst()){
            return transformCursor(cursor);
        }else{
            // Handle case of no result
            return null;
        }

    }

    public Map<String,Float> groupBy(String type, String column, int year, int month){
        SQLiteDatabase db = this.getReadableDatabase();


        // SELECT column, SUM(value) FROM transactions
        // WHERE strftime('%m',initial_date) = month AND strftime('%y',initial_date) = year AND type=type
        // GROUP BY column
        String query;


        query ="SELECT " +column+","+"SUM("+VALUE+")"+
                " FROM " + TRANSACTIONS+
                " WHERE " + TYPE +"='"+ type+ "'"+ " AND "+
                "strftime('%m',"+INITIAL_DATE+")= '"+String.format(Locale.ENGLISH,"%02d", month)+"'"+ " AND "+
                "strftime('%Y',"+INITIAL_DATE+")= '"+String.format(Locale.ENGLISH,"%04d", year)+"'"+
                " GROUP BY " + column;


        Cursor cursor  = db.rawQuery(query,null);

        // The map represents association between a category and the expenses in a given month

        Map<String,Float> itemIds = new HashMap<String, Float>();
        if (cursor.moveToFirst()){
            do {
                String grouped_name = cursor.getString(0);
                Float value = cursor.getFloat(1);
                itemIds.put(grouped_name,value);
            }while (cursor.moveToNext());

        }else{
            // Database is empty

        }
        cursor.close();
        db.close();

        return itemIds;
    }

    protected TransactionModel transformCursor(Cursor cursor){
        try{ int transaction_id = cursor.getInt(0);
            String type = cursor.getString(1);
            String category = cursor.getString(2);
            String date = cursor.getString(3);
            String recurrence_type = cursor.getString(4);
            Float recurrence_value = cursor.getFloat(5);
            Float value = cursor.getFloat(6);
            return new TransactionModel(transaction_id,type,category,date,recurrence_type,recurrence_value,value);
        }catch (Exception e){return null;}
       }

    protected ContentValues transformModel(TransactionModel transaction_model){
        ContentValues cv = new ContentValues();
        cv.put(TYPE,transaction_model.getType());
        cv.put(CATEGORY,transaction_model.getCategory());
        cv.put(INITIAL_DATE,transaction_model.getInitial_date());
        cv.put(RECURRENCE_TYPE,transaction_model.getRecurrence_category());
        cv.put(RECURRENCE_VALUE,transaction_model.getRecurrence_value());
        cv.put(VALUE,transaction_model.getValue());
        return cv;
    }
}