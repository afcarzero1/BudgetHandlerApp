package com.example.myapplication.datahandlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//todo : extend this class using a adjacent table to represent tree
// At the moment we will use only strings for simplicity
public class CategoriesHandler extends SQLiteOpenHelper {

    public static final String CATEGORIES_TABLE = "CATEGORIES";
    public static final String CATEGORIES_NAME = "NAME";
    private static final String CATEGORIES_TYPE = "TYPE";
    private static final String CATEGORIES_ID = "ID";

    public static final String TYPE_EXPENSE="Expense";
    public static final String TYPE_INCOME="Income";


    public CategoriesHandler(@Nullable Context context){
        super(context, "categories.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //category_name
        String create_table_statement = "CREATE TABLE " + CATEGORIES_TABLE + " (" + CATEGORIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CATEGORIES_NAME +" TEXT," + CATEGORIES_TYPE + " TEXT)";

        db.execSQL(create_table_statement);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

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
         Map<String,Float> values = th.groupTransactionsBy(type,TransactionHandler.TRANSACTIONS_CATEGORY_NAME,year,month);

         return values;
    }

    protected ContentValues categoriesModelToCursor(CategoriesModel cm){
        ContentValues cv = new ContentValues();
        cv.put(CATEGORIES_NAME,cm.getName());
        cv.put(CATEGORIES_TYPE,cm.getType());
        return cv;
    }

}
