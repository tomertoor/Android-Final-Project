package com.example.finalproject;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class SearchParkingFragment extends Fragment {
    RecyclerView recyclerView;
    MyAdapter adapter;
    TreeMap<Integer, ParkingCell> parkingRanges = new TreeMap<Integer, ParkingCell>();

    EditText etSearchBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_parking, container, false);;
        Bundle args = getArguments();
        if(args.getBoolean("isFirst")) // if it is the first time this fragment is called then it means that a search wasnt commited.
        {
            etSearchBar = view.findViewById(R.id.searchbar);
            etSearchBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    SearchMenu menu = new SearchMenu();

                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, menu).commit();
                    return false;

                }
            });
        }
        else
        {
            FirebaseDB db = new FirebaseDB();
            List<FirebaseDB.Parking> parkings = db.getParkings();
            LatLng location = args.getParcelable("latlng");


            for(FirebaseDB.Parking parking : parkings)
            {
                ParkingCell cell = null;
                try {
                    cell = new ParkingCell(new GeoPoint(location.latitude, location.longitude), parking.location, parking.creationTime, parking.parkerName, getContext());
                } catch (IOException e) {
                    continue;
                } catch (ExecutionException e) {
                } catch (InterruptedException e) {
                }
                parkingRanges.put(cell.getDistance(), cell);
            }
            recyclerView = view.findViewById(R.id.parkingranges);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new MyAdapter(getContext(), parkingRanges);
            recyclerView.setAdapter(adapter);


        }

        return view;
    }

    public void openSearchMenu(View view)
    {
        SearchMenu menu = new SearchMenu();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, menu).commit();
    }
}