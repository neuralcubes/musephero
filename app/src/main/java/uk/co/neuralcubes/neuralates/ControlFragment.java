package uk.co.neuralcubes.neuralates;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ControlFragment extends Fragment {

    private static final Integer[] ELECTRODE_BUTTON_IDS = new Integer[] {R.id.fp1, R.id.fp2, R.id.tp9, R.id.tp10};

    private Spinner mSelectSphero, mSelectMuse;
    private TextView mBatterySphero, mBatteryMuse;
    private Collection<Button> mElectrodeButtons;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_control, container, false);

        mSelectSphero = (Spinner) view.findViewById(R.id.sphero_selector);
        // TODO Get the list of devices got from listSpheros
        // Test array
        List<String> tmpArraySphero = new ArrayList<>();
        tmpArraySphero.add(getResources().getString(R.string.sphero_selector_header));
        // Populate the spinner with the array
        ArrayAdapter<String> adapterSphero = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, tmpArraySphero);
        adapterSphero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectSphero.setAdapter(adapterSphero);

        mSelectMuse = (Spinner) view.findViewById(R.id.muse_selector);
        // TODO Get the list of devices got from listMuses
        // Test array
        List<String> tmpArrayMuse = new ArrayList<>();
        tmpArrayMuse.add(getResources().getString(R.string.muse_selector_header));
        // Populate the spinner with the array
        ArrayAdapter<String> adapterMuse = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, tmpArrayMuse);
        adapterMuse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectMuse.setAdapter(adapterMuse);
        //System.out.println(view.getResources().getString(R.string.player)
        //        view.findViewById(R.id.player_label));

        // Get a reference to some resources
        mBatteryMuse = (TextView) view.findViewById(R.id.battery_muse);
        mBatterySphero = (TextView) view.findViewById(R.id.battery_sphero);
        mElectrodeButtons = Collections2.transform(Arrays.asList(ELECTRODE_BUTTON_IDS), new Function<Integer, Button>() {
            @Override
            public Button apply(Integer buttonId) {
                return (Button) view.findViewById(buttonId);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mSelectSphero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sphero_id = adapterView.getItemAtPosition(i).toString();
                //TODO Connect to selected Sphero
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        mSelectMuse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
