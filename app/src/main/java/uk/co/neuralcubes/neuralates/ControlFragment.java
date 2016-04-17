package uk.co.neuralcubes.neuralates;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.interaxon.libmuse.ConnectionState;
import com.orbotix.ConvenienceRobot;
import com.orbotix.common.Robot;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.co.neuralcubes.neuralates.controller.RobotController;
import uk.co.neuralcubes.neuralates.muse.MuseHandler;
import uk.co.neuralcubes.neuralates.muse.PairedMuse;
import uk.co.neuralcubes.neuralates.sphero.RobotSetListener;
import uk.co.neuralcubes.neuralates.sphero.SpheroManager;

public class ControlFragment extends Fragment implements RobotSetListener {

    private static final Integer[] ELECTRODE_BUTTON_IDS = new Integer[]{R.id.fp1, R.id.fp2, R.id.tp9, R.id.tp10};

    private Spinner mSelectSphero, mSelectMuse;
    private Collection<Button> mElectrodeButtons;
    private EventBus mBus = new EventBus();
    private Optional<PairedMuse> mMuseHandler = Optional.absent();
    private Optional<ConvenienceRobot> mSphero = Optional.absent();
    private Optional<RobotController> mController = Optional.absent();
    private ImageButton panicButton, forceButton, calibrateButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_control, container, false);

        mBus.register(this);
        mSelectSphero = (Spinner) view.findViewById(R.id.sphero_selector);
        SpheroManager.getInstance().addRobotSetListener(this);
        updateRobots(SpheroManager.getInstance().getRobots());

        mSelectMuse = (Spinner) view.findViewById(R.id.muse_selector);

        List<String> muses = Lists.newArrayList();
        muses.add(getResources().getString(R.string.muse_selector_header));
        // Populate the spinner with the array
        muses.addAll(Collections2.transform(PairedMuse.getPairedMuses(), new Function<PairedMuse, String>() {
            @Override
            public String apply(PairedMuse muse) {
                return muse.toString();
            }
        }));

        ArrayAdapter<String> adapterMuse = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, muses);
        adapterMuse.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectMuse.setAdapter(adapterMuse);

        mElectrodeButtons = Collections2.transform(Arrays.asList(ELECTRODE_BUTTON_IDS), new Function<Integer, Button>() {
            @Override
            public Button apply(Integer buttonId) {
                return (Button) view.findViewById(buttonId);
            }
        });

        panicButton = (ImageButton) view.findViewById(R.id.calibrate_btn);
        forceButton = (ImageButton) view.findViewById(R.id.force_muse_btn);
        calibrateButton = (ImageButton) view.findViewById(R.id.muse_panic);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mSelectSphero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    //fix the offset
                    ControlFragment.this.mSphero = Optional.of(new ConvenienceRobot(SpheroManager.getInstance().getRobots().get(i-1)));
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        mSelectMuse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                //0 is the default "Choose muse" element in the spinner
                if (i > 0) {
                    //fix the offset
                    mMuseHandler = Optional.of(PairedMuse.getPairedMuses().get(i - 1));
                    mMuseHandler.get().connect(mBus);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }

    @Subscribe
    public void updateMuseConnectionState(final ConnectionState state) {
        this.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                TextView connectionStatusText = (TextView)
                        getView().findViewById(R.id.muse_connection_status);
                connectionStatusText.setText(state.toString());
            }
        });
    }

    @Subscribe
    public void updateMuseBattery(final MuseHandler.BatteryReading reading) {
        this.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                TextView batteryText = (TextView)
                        getView().findViewById(R.id.battery_muse);
                batteryText.setText(String.format("%.2f%%", reading.getLevel()));
                //be careful when the reading is less that 0.25
                if (reading.getLevel() < 0.25) {
                    batteryText.setBackgroundColor(Color.RED);
                }
            }
        });
    }

    @Subscribe
    public void updateMuseHorseshoe(final MuseHandler.HorseshoeReading reading) {
        this.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int i = 0;
                for (Button button : mElectrodeButtons) {
                    double quality = reading.getValues()[i++] * 100;
                    button.setText(String.format("%.2f%%", quality));

                    //Change colour to buttons,
                    //Green is ok
                    //Blue is everything else
                    if (quality >= 75.) {
                        button.setPressed(true);
                    } else {
                        button.setPressed(false);
                    }
                }
            }
        });

    }

    @Override
    public void updateRobots(final List<Robot> robots) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> tmpArraySphero = Lists.newArrayList();
                tmpArraySphero.add(getResources().getString(R.string.sphero_selector_header));
                tmpArraySphero.addAll(Collections2.transform(robots,
                        new Function<Robot, String>(){
                            @Override
                            public String apply(Robot bender) {
                                return bender.getName();
                            }
                        }

                ));
                // Populate the spinner with the array
                ArrayAdapter<String> adapterSphero = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, tmpArraySphero);
                adapterSphero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSelectSphero.setAdapter(adapterSphero);
            }
        });
    }
}
