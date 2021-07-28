package com.carina.eventme;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class results extends Fragment {

    private String date;
    private String time;
    private String title;

    results(String date, String time, String title){
        this.date = date;
        this.time = time;
        this.title = this.title;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //initialize view:
        View view = inflater.inflate(R.layout.fragment_results, container, false);
        TextView Datum = view.findViewById(R.id.datum);
        TextView Zeit = view.findViewById(R.id.zeit);
        TextView Titel = view.findViewById(R.id.titleevent);
        String toShow3;
        String toShow1;
        String toShow2;
        //no date has been found:
        if (this.date.equals("Unknown")) {
            toShow1 = "We could not find your party!";
            Datum.setText(toShow1);
            toShow2= "Please go Back!";
            Zeit.setText(toShow2);
            toShow3 ="Sorry!";
            Titel.setText(toShow3);
        } else {
            if (this.time.equals("Unknown")) this.time = "20:00";
            toShow1 = date;
            Datum.setText(toShow1);
            toShow2 = time;
            Zeit.setText(toShow2);
            toShow3 = title;
            Titel.setText(toShow3);
        }
        return view;

    }


}