package com.example.myapplication.datahandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;


public class TransactionHandler extends SQLiteOpenHelper {

    // Columns of transactions table
    public static final String TRANSACTIONS_TABLE_NAME = "TRANSACTIONS";
    public static final String TRANSACTIONS_TYPE = "TYPE";
    public static final String TRANSACTIONS_CATEGORY_NAME = "CATEGORY";
    public static final String TRANSACTIONS_INITIAL_DATE = "INITIAL_DATE";
    public static final String TRANSACTIONS_RECURRENCE_TYPE = "RECURRENCE_TYPE";
    public static final String TRANSACTIONS_VALUE = "VALUE";
    public static final String TRANSACTIONS_RECURRENCE_VALUE = "RECURRENCE_" + TRANSACTIONS_VALUE;
    public static final String TRANSACTIONS_ID = "ID";
    public static final String TRANSACTIONS_CATEGORY_ID = "TRANSACTION_CATEGORY_ID";

    // Columns of categories table
    public static final String CATEGORIES_TABLE = "CATEGORIES";
    public static final String CATEGORIES_NAME = "NAME";
    private static final String CATEGORIES_TYPE = "TYPE";
    private static final String CATEGORIES_ID = "ID";

    //
    public static final String TYPE_EXPENSE="Expense";
    public static final String TYPE_INCOME="Income";

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
        String create_table_statement = "CREATE TABLE " + CATEGORIES_TABLE + " (" + CATEGORIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CATEGORIES_NAME +" TEXT," + CATEGORIES_TYPE + " TEXT)";


        db.execSQL(create_table_statement);

        String create_transaction_table_statement = "CREATE TABLE " + TRANSACTIONS_TABLE_NAME + " (" + TRANSACTIONS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TRANSACTIONS_TYPE + " TEXT ," + TRANSACTIONS_CATEGORY_NAME + " TEXT, " + TRANSACTIONS_INITIAL_DATE + " TEXT, " + TRANSACTIONS_RECURRENCE_TYPE + " TEXT," +
                TRANSACTIONS_RECURRENCE_VALUE + " FLOAT," + TRANSACTIONS_VALUE + " FLOAT,"+TRANSACTIONS_CATEGORY_ID
                +"INTEGER,FOREIGN KEY (" + TRANSACTIONS_CATEGORY_ID + ") REFERENCES " + CATEGORIES_TABLE +"("+CATEGORIES_ID+"))";


        db.execSQL(create_transaction_table_statement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean addTransaction(TransactionModel transaction_model){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = transactionModelToCursor(transaction_model);

        long insert = db.insert(TRANSACTIONS_TABLE_NAME, null, cv);
        db.close();
        return insert != -1;
    }

    public boolean deleteTransaction(TransactionModel transaction_model){

        SQLiteDatabase db = this.getWritableDatabase();
        String query_string = "DELETE FROM " + TRANSACTIONS_TABLE_NAME + " WHERE " + TRANSACTIONS_ID + " = " + transaction_model.getId();

        Cursor cursor = db.rawQuery(query_string,null);

        return cursor.moveToFirst();
    }

    public boolean deleteTransaction(int transaction_model_id){
        // Open database and define query
        SQLiteDatabase db = this.getWritableDatabase();
        String query_string = "DELETE FROM " + TRANSACTIONS_TABLE_NAME + " WHERE " + TRANSACTIONS_ID + " = " + transaction_model_id;

        // Execute query and get result (should delete only one element)
        final int deleted_rows=db.delete(TRANSACTIONS_TABLE_NAME, TRANSACTIONS_ID + "=?" ,new String[]{String.valueOf(transaction_model_id)});
        final boolean return_value= deleted_rows > 0;

        // Close cursor and database
        db.close();
        return return_value;
    }

    public boolean updateTransaction(int transaction_model_id , TransactionModel updated_transaction){
        SQLiteDatabase db = this.getWritableDatabase();
        String query_string = "UPDATE " +"SET "+ TRANSACTIONS_TABLE_NAME + " WHERE " + TRANSACTIONS_ID + " = " + transaction_model_id;

        ContentValues cv = this.transactionModelToCursor(updated_transaction);

        // Update the record with the desired model
        final int count=db.update(TRANSACTIONS_TABLE_NAME,cv, TRANSACTIONS_ID + " =?",new String[]{String.valueOf(transaction_model_id)});

        // Return true if at least one row was updated
        return count!=0;
    }

    public List<TransactionModel> getAllTransactions(boolean ordered){

        List<TransactionModel> returnList = new ArrayList<>();


        String query = "SELECT * FROM " + TRANSACTIONS_TABLE_NAME;
        if (ordered){
            query ="SELECT * FROM " + TRANSACTIONS_TABLE_NAME +" ORDER BY " + TRANSACTIONS_INITIAL_DATE;
        }
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor  = db.rawQuery(query,null);

        if (cursor.moveToFirst()){
            do {
                TransactionModel tm = cursorToTransactionModel(cursor);
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
        String query = "SELECT * FROM " + TRANSACTIONS_TABLE_NAME + " WHERE " + TRANSACTIONS_ID + "= " + transaction_model_id;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor  = db.rawQuery(query,null);

        if(cursor.moveToFirst()){
            return cursorToTransactionModel(cursor);
        }else{
            // Handle case of no result
            return null;
        }

    }

    public Map<String,Float> groupTransactionsBy(String type, String column, int year, int month){
        SQLiteDatabase db = this.getReadableDatabase();


        // SELECT column, SUM(value) FROM transactions
        // WHERE strftime('%m',initial_date) = month AND strftime('%y',initial_date) = year AND type=type
        // GROUP BY column
        String query;


        query ="SELECT " +column+","+"SUM("+ TRANSACTIONS_VALUE +")"+
                " FROM " + TRANSACTIONS_TABLE_NAME +
                " WHERE " + TRANSACTIONS_TYPE +"='"+ type+ "'"+ " AND "+
                "strftime('%m',"+ TRANSACTIONS_INITIAL_DATE +")= '"+String.format(Locale.ENGLISH,"%02d", month)+"'"+ " AND "+
                "strftime('%Y',"+ TRANSACTIONS_INITIAL_DATE +")= '"+String.format(Locale.ENGLISH,"%04d", year)+"'"+
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

    protected TransactionModel cursorToTransactionModel(Cursor cursor){
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

    protected ContentValues transactionModelToCursor(TransactionModel transaction_model){
        ContentValues cv = new ContentValues();
        cv.put(TRANSACTIONS_TYPE,transaction_model.getType());
        cv.put(TRANSACTIONS_CATEGORY_NAME,transaction_model.getCategory());
        cv.put(TRANSACTIONS_INITIAL_DATE,transaction_model.getInitial_date());
        cv.put(TRANSACTIONS_RECURRENCE_TYPE,transaction_model.getRecurrence_category());
        cv.put(TRANSACTIONS_RECURRENCE_VALUE,transaction_model.getRecurrence_value());
        cv.put(TRANSACTIONS_VALUE,transaction_model.getValue());
        return cv;
    }

    public boolean addCategory(CategoriesModel category_model){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = categoriesModelToCursor(category_model);

        final long insert = db.insert(CATEGORIES_TABLE, null, cv);
        db.close();
        return insert != -1;
    }

    public boolean deleteCategory(int category_id){
        SQLiteDatabase db = this.getWritableDatabase();
        // Execute query and get result (should delete only one element)
        final int deleted_rows=db.delete(CATEGORIES_TABLE, CATEGORIES_ID + "=?" ,new String[]{String.valueOf(category_id)});
        final boolean return_value= deleted_rows > 0;

        // todo : ensure consistency (no transaction of this category)

        // Close cursor and database
        db.close();
        return return_value;
    }

    public List<CategoriesModel> getAllCategories(){
        List<CategoriesModel> returnList = new ArrayList<>();
        String query = "SELECT * FROM " + CATEGORIES_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor  = db.rawQuery(query,null);

        if (cursor.moveToFirst()){
            do {
                int transaction_id = cursor.getInt(0);
                String category_name = cursor.getString(1);
                String expense_type = cursor.getString(2);
                CategoriesModel cm = new CategoriesModel(transaction_id,category_name,expense_type);
                returnList.add(cm);
            }while (cursor.moveToNext());

        }else{
            // Database is empty
        }
        cursor.close();
        db.close();
        return returnList;
    }

    public List<CategoriesModel> getAllCategories(String type){
        List<CategoriesModel> returnList = new ArrayList<>();
        String query = "SELECT * FROM " + CATEGORIES_TABLE + " WHERE "+ CATEGORIES_TYPE +" = \""+ type +"\"";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor  = db.rawQuery(query,null);

        if (cursor.moveToFirst()){
            do {
                int transaction_id = cursor.getInt(0);
                String category_name = cursor.getString(1);
                String expense_type = cursor.getString(2);
                CategoriesModel cm = new CategoriesModel(transaction_id,category_name,expense_type);
                returnList.add(cm);
            }while (cursor.moveToNext());

        }else{
            // Result is empty
        }
        cursor.close();
        db.close();
        return returnList;

    }

    public Map<String,Float> getAllCategoriesSum(TransactionHandler th,String type,int year, int month){
        // Call the groupby function of the transaction handler
        Map<String,Float> values = this.groupTransactionsBy(type,TransactionHandler.TRANSACTIONS_CATEGORY_NAME,year,month);

        return values;
    }

    protected ContentValues categoriesModelToCursor(CategoriesModel cm){
        ContentValues cv = new ContentValues();
        cv.put(CATEGORIES_NAME,cm.getName());
        cv.put(CATEGORIES_TYPE,cm.getType());
        return cv;
    }



}