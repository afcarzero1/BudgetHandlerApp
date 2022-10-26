package com.example.myapplication.datahandlers;

public class TransactionModel {

    private int id;
    private String type;
    private String category;
    private String initial_date;
    private String recurrence_category;
    private Float recurrence_value;
    private Float value;

    public TransactionModel(int id,String type, String category,String initial_date ,String recurrence_category, Float recurrence_value, Float value) {
        this.id=id;
        this.type = type;
        this.category = category;
        // include assertion that format si compatible with sql lite
        this.initial_date=initial_date;
        this.recurrence_category = recurrence_category;
        this.recurrence_value = recurrence_value;
        this.value = value;
    }

    public int getId() {return id;}

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getInitial_date() {return initial_date;}

    public String getRecurrence_category() {
        return recurrence_category;
    }

    public Float getRecurrence_value() {
        return recurrence_value;
    }

    public Float getValue() {
        return value;
    }


    @Override
    public String toString() {
        return "TransactionModel{" +
                "type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", recurrence_category='" + recurrence_category + '\'' +
                ", recurrence_value=" + recurrence_value +
                ", value=" + value +
                '}';
    }
}
