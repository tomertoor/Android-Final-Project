package com.example.finalproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchParkingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchParkingFragment extends Fragment {

    EditText etSearchBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_parking, container, false);;
        etSearchBar = view.findViewById(R.id.searchbar);
        etSearchBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SearchMenu menu = new SearchMenu();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, menu).commit();
                return false;
            }
        });
        return view;
    }

    public void openSearchMenu(View view)
    {
        SearchMenu menu = new SearchMenu();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, menu).commit();
    }
}