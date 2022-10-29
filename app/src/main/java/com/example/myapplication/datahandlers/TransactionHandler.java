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

import com.example.myapplication.datahandlers.models.AccountModel;
import com.example.myapplication.datahandlers.models.CategoriesModel;
import com.example.myapplication.datahandlers.models.CurrencyModel;
import com.example.myapplication.datahandlers.models.InstanceModel;


public class TransactionHandler extends SQLiteOpenHelper {

    // Transaction table name
    public static final String TRANSACTIONS = "TRANSACTIONS";

    // Columns of account table
    public static final String ACCOUNTS = "ACCOUNTS";

    // Columns of currencies table
    public static final String CURRENCIES = "CURRENCIES";

    public static final String CATEGORIES = "CATEGORIES";


    public static final String TYPE_EXPENSE = "expense";
    public static final String TYPE_INCOME = "income";

    public TransactionHandler(@Nullable Context context) {
        super(context, "transactions.db", null, 1);

        // Set the table names in the static fields
        TransactionModel.associatedTable= TRANSACTIONS;
        AccountModel.associatedTable= ACCOUNTS;
        CurrencyModel.associatedTable = CURRENCIES;
        CategoriesModel.associatedTable = CATEGORIES;
    }


    /**
     * Create the database tables.
     * @param db The database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Type, Category, Initial_Date, Recurrency_Type, Recurrency_Value,Value



        String createCurrenciesTableStatement = "CREATE TABLE " + CURRENCIES + " (" +
                CurrencyModel.FIELDS.NAME.getSqlName() + " TEXT PRIMARY KEY)";

        String createAccountTableStatement = "CREATE TABLE " + ACCOUNTS + " (" +
                AccountModel.FIELDS.NAME.getSqlName() + " " +AccountModel.FIELDS.NAME.getSqlType() +" NOT NULL PRIMARY KEY, " +
                AccountModel.FIELDS.CURRENCY.getSqlName() + " " + AccountModel.FIELDS.CURRENCY.getSqlType() + " ," +
                AccountModel.FIELDS.INITIAL_BALANCE.getSqlName()+ " " + AccountModel.FIELDS.INITIAL_BALANCE.getSqlType() + " ," +
                " FOREIGN KEY ("+AccountModel.FIELDS.CURRENCY.getSqlName()+") REFERENCES "+CURRENCIES+" ( "+CurrencyModel.FIELDS.NAME.getSqlName()+" ) )";


        String createCategoriesTableStatement = "CREATE TABLE " +CATEGORIES + " (" +
                CategoriesModel.FIELDS.NAME.getSqlName() + " " + CategoriesModel.FIELDS.NAME.getSqlType() + " ," +
                CategoriesModel.FIELDS.TYPE.getSqlName() + " " + CategoriesModel.FIELDS.TYPE.getSqlType() + " ," +
                CategoriesModel.FIELDS.PARENT_NAME.getSqlName() + " " + CategoriesModel.FIELDS.PARENT_NAME.getSqlType() + " ," +
                CategoriesModel.FIELDS.PARENT_TYPE.getSqlName() + " " + CategoriesModel.FIELDS.PARENT_TYPE.getSqlType() + " ," +
                " PRIMARY KEY ("+CategoriesModel.FIELDS.NAME.getSqlName()+","+CategoriesModel.FIELDS.TYPE.getSqlName()+")" + " ," +
                "FOREIGN KEY ("+CategoriesModel.FIELDS.PARENT_NAME.getSqlName()+","+CategoriesModel.FIELDS.PARENT_TYPE.getSqlName()+") REFERENCES "+
                CATEGORIES+" ("+CategoriesModel.FIELDS.NAME.getSqlName()+","+CategoriesModel.FIELDS.TYPE.getSqlName() +"))";


        String createTransactionTableStatement = "CREATE TABLE " + TRANSACTIONS + " (" +
                TransactionModel.FIELDS.ID.getSqlName() + " " +TransactionModel.FIELDS.ID.getSqlType()+" PRIMARY KEY AUTOINCREMENT," +
                TransactionModel.FIELDS.TYPE.getSqlName() + " " +TransactionModel.FIELDS.TYPE.getSqlType()+" ,"+
                TransactionModel.FIELDS.CATEGORY.getSqlName() + " " +TransactionModel.FIELDS.CATEGORY.getSqlType()+" ,"+
                TransactionModel.FIELDS.ACCOUNT.getSqlName() + " " +TransactionModel.FIELDS.ACCOUNT.getSqlType()+" ,"+
                TransactionModel.FIELDS.INITIAL_DATE.getSqlName() + " " +TransactionModel.FIELDS.INITIAL_DATE.getSqlType()+" ,"+
                TransactionModel.FIELDS.RECURRENCE_TYPE.getSqlName() + " " +TransactionModel.FIELDS.RECURRENCE_TYPE.getSqlType()+" ,"+
                TransactionModel.FIELDS.RECURRENCE_VALUE.getSqlName() + " " +TransactionModel.FIELDS.RECURRENCE_VALUE.getSqlType()+" ,"+
                TransactionModel.FIELDS.VALUE.getSqlName() + " " +TransactionModel.FIELDS.VALUE.getSqlType()+" ,"+
                "FOREIGN KEY ("+TransactionModel.FIELDS.ACCOUNT.getSqlName()+") REFERENCES "+ACCOUNTS+" ( "+AccountModel.FIELDS.NAME.getSqlName()+" ),"+
                "FOREIGN KEY ("+TransactionModel.FIELDS.CATEGORY.getSqlName()+","+TransactionModel.FIELDS.TYPE.getSqlName()+") REFERENCES "+
                CATEGORIES+" ("+CategoriesModel.FIELDS.NAME.getSqlName()+","+CategoriesModel.FIELDS.TYPE.getSqlName() +"))";


        db.execSQL(createCurrenciesTableStatement);
        db.execSQL(createAccountTableStatement);
        db.execSQL(createCategoriesTableStatement);
        db.execSQL(createTransactionTableStatement);

        Cursor cv=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='CURRENCIES';",null);
        cv.moveToFirst();
        String name=cv.getString(0);

        cv=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='ACCOUNTS';",null);
        cv.moveToFirst();
        name=cv.getString(0);

        cv=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='CATEGORIES';",null);
        cv.moveToFirst();
        name=cv.getString(0);

        cv=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='TRANSACTIONS';",null);
        cv.moveToFirst();
        name=cv.getString(0);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // TRANSACTION TABLE METHODS

    public boolean addTransaction(TransactionModel transaction_model) {
        transaction_model.setAccount("test");//todo : remove
        return addInstance(transaction_model);
    }

    public boolean deleteTransaction(TransactionModel transaction_model){

        SQLiteDatabase db = this.getWritableDatabase();
        String query_string = "DELETE FROM " + TRANSACTIONS + " WHERE " + TransactionModel.FIELDS.ID.getSqlName() + " = " + transaction_model.getId();

        Cursor cursor = db.rawQuery(query_string,null);

        return cursor.moveToFirst();
    }

    public boolean deleteTransaction(int transaction_model_id){
        // Open database and define query
        SQLiteDatabase db = this.getWritableDatabase();
        String query_string = "DELETE FROM " + TRANSACTIONS + " WHERE " + TransactionModel.FIELDS.ID.getSqlName() + " = " + transaction_model_id;

        // Execute query and get result (should delete only one element)
        final int deleted_rows=db.delete(TRANSACTIONS,TransactionModel.FIELDS.ID.getSqlName() + "=?" ,new String[]{String.valueOf(transaction_model_id)});
        final boolean return_value= deleted_rows > 0;

        // Close cursor and database
        db.close();
        return return_value;
    }

    public boolean updateTransaction(int transaction_model_id , TransactionModel updated_transaction){
        SQLiteDatabase db = this.getWritableDatabase();
        String query_string = "UPDATE " +"SET "+ TRANSACTIONS + " WHERE " + TransactionModel.FIELDS.ID.getSqlName() + " = " + transaction_model_id;

        ContentValues cv = updated_transaction.toCursor();

        // Update the record with the desired model
        final int count=db.update(TRANSACTIONS,cv,TransactionModel.FIELDS.ID.getSqlName() + " =?",new String[]{String.valueOf(transaction_model_id)});

        // Return true if at least one row was updated
        return count!=0;
    }

    public List<TransactionModel> getAllTransactions(boolean ordered){
        String query = "SELECT * FROM " + TRANSACTIONS;
        if (ordered){
            query ="SELECT * FROM " + TRANSACTIONS+" ORDER BY " + TransactionModel.FIELDS.INITIAL_DATE.getSqlName();
        }

        List<TransactionModel> toReturn = getAllFromQuery(query,TransactionModel.class);

        return toReturn;
    }

    public TransactionModel getTransaction(int transaction_model_id){
        String query = "SELECT * FROM " + TRANSACTIONS + " WHERE " + TransactionModel.FIELDS.ID.getSqlName() + "= " + transaction_model_id;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor  = db.rawQuery(query,null);

        List<TransactionModel> result = getAllFromQuery(query,TransactionModel.class);

        if (result.isEmpty()){return null;}else{return result.get(0);}
    }

    public Map<String,Float> groupTransactionsBy(String type, String column, int year, int month){
        SQLiteDatabase db = this.getReadableDatabase();


        // SELECT column, SUM(value) FROM transactions
        // WHERE strftime('%m',initial_date) = month AND strftime('%y',initial_date) = year AND type=type
        // GROUP BY column
        String query;


        query ="SELECT " +column+","+"SUM("+TransactionModel.FIELDS.VALUE.getSqlName()+")"+
                " FROM " + TRANSACTIONS+
                " WHERE " + TransactionModel.FIELDS.TYPE.getSqlName() +"='"+ type+ "'"+ " AND "+
                "strftime('%m',"+TransactionModel.FIELDS.INITIAL_DATE.getSqlName()+")= '"+String.format(Locale.ENGLISH,"%02d", month)+"'"+ " AND "+
                "strftime('%Y',"+TransactionModel.FIELDS.INITIAL_DATE.getSqlName()+")= '"+String.format(Locale.ENGLISH,"%04d", year)+"'"+
                " GROUP BY " + column + " ORDER BY " + "SUM("+TransactionModel.FIELDS.VALUE.getSqlName()+")";


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

    // CATEGORY TABLE METHODS

    public boolean addCategory(CategoriesModel categoriesModel){return addInstance(categoriesModel);}

    public boolean deleteCategory(String categoryName,String categoryType){
        SQLiteDatabase db = this.getWritableDatabase();

        final int deleted_rows=db.delete(CATEGORIES,CategoriesModel.FIELDS.NAME.getSqlName() + "=? AND "+CategoriesModel.FIELDS.TYPE.getSqlName() + "=?" ,new String[]{categoryName,categoryType});

        db.close();
        return deleted_rows>0;
    }

    public List<CategoriesModel> getAllCategories(boolean ordered){
        String query = "SELECT * FROM " + CATEGORIES;
        if(ordered){
            query = "SELECT * FROM " + CATEGORIES + " ORDER BY " + CategoriesModel.FIELDS.TYPE.getSqlName();
        }
        return getAllFromQuery(query,CategoriesModel.class);
    }

    public List<CategoriesModel> getAllCategories(String type){
        String query = "SELECT * FROM " + CATEGORIES + " WHERE "+ CategoriesModel.FIELDS.TYPE.getSqlName() +" = \""+ type +"\"";
        return getAllFromQuery(query,CategoriesModel.class);
    }


    //ACCOUNTS TABLE METHODS

    public boolean addAccount(AccountModel accountModel){
        return addInstance(accountModel);
    }

    public List<AccountModel> getAllAccounts(boolean ordered){
        String query = "SELECT * FROM " + ACCOUNTS;

        if(ordered){
            query = "SELECT * FROM " + ACCOUNTS + " ORDER BY " + AccountModel.FIELDS.NAME.getSqlName();
        }

        return getAllFromQuery(query,AccountModel.class);
    }

    //CURRENCY TABLE METHODS

    public boolean addCurrency(CurrencyModel currencyModel){
        return addInstance(currencyModel);
    }

    public List<CurrencyModel> getAllCurrencies(boolean ordered){
        String query = "SELECT * FROM " + CURRENCIES;

        if(ordered){
            query = "SELECT * FROM " + CURRENCIES + " ORDER BY " + CurrencyModel.FIELDS.NAME.getSqlName();
        }

        return getAllFromQuery(query,CurrencyModel.class);
    }

    //GENERAL METHODS

    protected boolean addInstance(InstanceModel model){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = model.toCursor();

        long insert = db.insert(model.getAssociatedTable(),null,cv);
        db.close();

       return insert!=-1;
    }

    protected <T extends InstanceModel> List<T> getAllFromQuery(String query,Class<T> clazz){
        List<T> returnList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor  = db.rawQuery(query,null);

        if(cursor.moveToFirst()){
            do{
                returnList.add(InstanceModel.instanceFromCursor(cursor,clazz));
            }while (cursor.moveToNext());
        }
        return returnList;
    }
}

