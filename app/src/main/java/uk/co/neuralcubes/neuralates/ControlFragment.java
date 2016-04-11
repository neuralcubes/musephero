package uk.co.neuralcubes.neuralates;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ControlFragment extends Fragment {
    private Spinner selectSphero, selectMuse;
    private TextView batterySphero, batteryMuse;
    private List<Button> electrodes;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_control, container, false);

        selectSphero = (Spinner) view.findViewById(R.id.sphero_selector);
        // TODO Get the list of devices got from listSpheros
        // Test array
        List<String> tmpArraySphero = new ArrayList<>();
        tmpArraySphero.add(getResources().getString(R.string.sphero_selector_header));
        // Populate the spinner with the array
        ArrayAdapter<String> adapterSphero = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, tmpArraySphero);
        adapterSphero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectSphero.setAdapter(adapterSphero);

        selectMuse = (Spinner) view.findViewById(R.id.muse_selector);
        // TODO Get the list of devices got from listMuses
        // Test array
        List<String> tmpArrayMuse = new ArrayList<>();
        tmpArrayMuse.add(getResources().getString(R.string.muse_selector_header));
        // Populate the spinner with the array
        ArrayAdapter<String> adapterMuse = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, tmpArrayMuse);
        adapterMuse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectMuse.setAdapter(adapterMuse);
        //System.out.println(view.getResources().getString(R.string.player)
        //        view.findViewById(R.id.player_label));

        // Get a reference to some resources
        Resources res = getResources();
        batteryMuse = (TextView) view.findViewById(R.id.battery_muse);
        batterySphero = (TextView) view.findViewById(R.id.battery_sphero);
        electrodes = new ArrayList<Button>();
        electrodes.add((Button) view.findViewById(R.id.fp1));
        electrodes.add((Button) view.findViewById(R.id.fp2));
        electrodes.add((Button) view.findViewById(R.id.tp9));
        electrodes.add((Button) view.findViewById(R.id.tp10));
        // Assign default battery values: these will be updated when connected
        batteryMuse.setText(res.getString(R.string.battery_level, 100));
        batterySphero.setText(res.getString(R.string.battery_level, 100));
        // Assign default electrodes strength
        for (Button b: electrodes) {
            b.setText(res.getString(R.string.electrode_strength, 100));
        }
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        selectSphero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sphero_id = adapterView.getItemAtPosition(i).toString();
                //TODO Connect to selected Sphero
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        selectMuse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String muse_id = adapterView.getItemAtPosition(i).toString();
                //TODO Connect to selected Muse
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }
}
