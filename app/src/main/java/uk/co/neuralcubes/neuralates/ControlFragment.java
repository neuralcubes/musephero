package uk.co.neuralcubes.neuralates;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.interaxon.libmuse.ConnectionState;
import com.orbotix.ConvenienceRobot;
import com.orbotix.command.GetPowerStateResponse;
import com.orbotix.common.Robot;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.co.neuralcubes.neuralates.controller.ColorMap;
import uk.co.neuralcubes.neuralates.controller.RobotController;
import uk.co.neuralcubes.neuralates.muse.MuseHandler;
import uk.co.neuralcubes.neuralates.muse.PairedMuse;
import uk.co.neuralcubes.neuralates.sphero.SpheroEventListener;
import uk.co.neuralcubes.neuralates.sphero.SpheroManager;

public class ControlFragment extends Fragment implements SpheroEventListener, AdapterView.OnItemSelectedListener {

    private static final Integer[] ELECTRODE_BUTTON_IDS = new Integer[]{R.id.fp1, R.id.fp2, R.id.tp9, R.id.tp10};

    private Spinner mSelectSphero, mSelectMuse;
    private Collection<Button> mElectrodeButtons;
    private EventBus mBus = new EventBus();
    private Optional<PairedMuse> mMuseHandler = Optional.absent();
    private Optional<ConvenienceRobot> mSphero = Optional.absent();
    private Optional<RobotController> mController = Optional.absent();
    private View[] mSpheroActions;
    private View[] mMuseActions;
    private Handler mStopCalibrationHandler;
    private View mConcentrationBar;
    private ColorMap mColorMap = ColorMap.GREENISH;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_control, container, false);

        mBus.register(this);
        mSelectSphero = (Spinner) view.findViewById(R.id.sphero_selector);
        mSelectSphero.setOnItemSelectedListener(this);
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
        mSelectMuse.setOnItemSelectedListener(this);

        mElectrodeButtons = Collections2.transform(Arrays.asList(ELECTRODE_BUTTON_IDS), new Function<Integer, Button>() {
            @Override
            public Button apply(Integer buttonId) {
                return (Button) view.findViewById(buttonId);
            }
        });

        View panicButton = view.findViewById(R.id.sphero_panic_btn);
        panicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSphero.isPresent()) {
                    mSphero.get().stop();
                }
            }
        });

        final View calibrateRightButton = view.findViewById(R.id.sphero_calibrate_right_btn);
        final View calibrateLeftButton = view.findViewById(R.id.sphero_calibrate_left_btn);
        View.OnClickListener calibrateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSphero.isPresent()) {
                    float newHeading = v == calibrateRightButton ? 30 : -30;
                    final ConvenienceRobot robot = mSphero.get();
                    robot.calibrating(true);
                    robot.rotate((robot.getLastHeading() + newHeading + 360) % 360);
                    if (mStopCalibrationHandler != null) {
                        mStopCalibrationHandler.removeCallbacksAndMessages(null);
                    }
                    mStopCalibrationHandler = new Handler();
                    mStopCalibrationHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            robot.calibrating(false);
                        }
                    }, 3000);
                }
            }
        };
        calibrateLeftButton.setOnClickListener(calibrateClickListener);
        calibrateRightButton.setOnClickListener(calibrateClickListener);


        View horizonButton = view.findViewById(R.id.muse_reset_horizon_btn);
        horizonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                if (v.isSelected()) {
                    if (mController.isPresent()) {
                        mController.get().setBaseReading();
                    }
                }
            }
        });
        ToggleButton toggleButton = (ToggleButton) view.findViewById(R.id.toggle_override);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mController.isPresent()) {
                    mController.get().setOverrideFocus(isChecked);
                }
            }
        });
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mController.isPresent()) {
                    mController.get().setOverrideValue(((double) progress) / seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSpheroActions = new View[]{calibrateLeftButton, calibrateRightButton, panicButton};
        mMuseActions = new View[]{toggleButton, horizonButton};

        mConcentrationBar = view.findViewById(R.id.concentrationBar);
        mConcentrationBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColorMap();
            }
        });

        return view;
    }

    void changeColorMap() {
        mColorMap = ColorMap.next(mColorMap);
        if (mController.isPresent()) {
            mController.get().setColorMap(mColorMap);
        }
    }

    // BEGINNING - AdapterView.OnItemSelectedListener

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i <= 0) {
            onNothingSelected(adapterView);
            return;
        }
        if (adapterView == mSelectSphero) {
            //fix the offset
            mSphero = Optional.of(new ConvenienceRobot(SpheroManager.getInstance().getRobots().get(i - 1)));
            setEnabledStateForViews(mSpheroActions, mSphero.isPresent());
        } else if (adapterView == mSelectMuse) {
            mMuseHandler = Optional.of(PairedMuse.getPairedMuses().get(i - 1));
            mMuseHandler.get().connect(mBus);

            setEnabledStateForViews(mMuseActions, true);

            if (mSphero.isPresent()) {
                if (mController.isPresent()) {
                    mController.get().unlink();
                }
                mController = Optional.of(new RobotController(mSphero.get(), mBus, mColorMap));
            }
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
        if (adapterView == mSelectSphero) {
            setEnabledStateForViews(mSpheroActions, false);
            onPowerStateUpdate(null, null);
            if (mController.isPresent()) {
                mController.get().unlink();
            }
            mController = Optional.absent();
            mSphero = Optional.absent();
        } else if (adapterView == mSelectMuse) {
            setEnabledStateForViews(mMuseActions, false);
            if (mController.isPresent()) {
                mController.get().unlink();
            }
            mController = Optional.absent();
            mMuseHandler = Optional.absent();
        }
    }

    // END - AdapterView.OnItemSelectedListener

    public void onPowerStateUpdate(Robot robot, GetPowerStateResponse.PowerState powerState) {
        TextView batteryText = (TextView) getView().findViewById(R.id.battery_sphero);
        ImageView batteryIcon = (ImageView) getView().findViewById(R.id.ic_battery_sphero);

        if (powerState == null) {
            batteryIcon.setImageResource(R.drawable.ic_battery_unknown_black_24dp);
            batteryText.setText(R.string.ellipsis);
            return;
        } else if (!mSphero.isPresent() || robot != mSphero.get().getRobot()) {
            return;
        }

        batteryText.setText(powerState.name());
        switch (powerState) {
            case OK:
                batteryIcon.setImageResource(R.drawable.ic_battery_80_black_24dp);
                break;
            case LOW:
                batteryIcon.setImageResource(R.drawable.ic_battery_20_black_24dp);
                break;
            case CRITICAL:
                batteryIcon.setImageResource(R.drawable.ic_battery_alert_black_24dp);
                break;
            case CHARGING:
                batteryIcon.setImageResource(R.drawable.ic_battery_charging_60_black_24dp);
                break;
            default:
                batteryIcon.setImageResource(R.drawable.ic_battery_unknown_black_24dp);
                break;
        }
    }

    @Subscribe
    public void updateMuseConnectionState(final ConnectionState state) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView connectionStatusText = (TextView) getView().findViewById(R.id.muse_connection_status);
                connectionStatusText.setText(state.toString());
            }
        });
    }

    @Subscribe
    public void updateMuseBatteryLevel(final MuseHandler.BatteryReading reading) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView batteryText = (TextView) getView().findViewById(R.id.battery_muse);
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

                    // Change colour to buttons,
                    // Green is ok
                    // Blue is everything else
                    if (quality >= 75.) {
                        button.setPressed(true);
                    } else {
                        button.setPressed(false);
                    }
                }
            }
        });
    }

    @Subscribe
    public void updateConcentration(final MuseHandler.FocusReading reading) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateConcentrationColor(reading.getFocus());
            }
        });
    }

    void updateConcentrationColor(double value) {
        int[] border = mColorMap.map(0.3);
        int[] rgbColor = mColorMap.inverseMap(value);

        mConcentrationBar.setBackgroundColor(Color.rgb(rgbColor[0], rgbColor[1], rgbColor[2]));
        ((View) mConcentrationBar.getParent()).setBackgroundColor(Color.rgb(border[0], border[1], border[2]));
        mConcentrationBar.invalidate();
    }

    @Override
    public void updateRobots(final List<Robot> robots) {
        List<String> tmpArraySphero = Lists.newArrayList();
        tmpArraySphero.add(getResources().getString(R.string.sphero_selector_header));
        tmpArraySphero.addAll(Collections2.transform(robots,
                new Function<Robot, String>() {
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

    private void setEnabledStateForViews(@NonNull View[] views, boolean enabled) {
        for (View view : views) {
            view.setEnabled(enabled);
        }
    }
}
