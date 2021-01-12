package site.bdsc.localization.myapplication.pdr;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class StepDetector implements SensorEventListener {
    private final String TAG = "Sensor";
    private Context context;
    private int currentStep = 0;
    protected StepCallBack stepCallBack;
    protected SensorManager sensorManager;

    public StepDetector(Context context, StepCallBack stepCallBack) {
        this.context = context;
        this.stepCallBack = stepCallBack;
    }

    public boolean registerStepSensor(){
        boolean isAvailable = false;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (sensorManager.registerListener(this,
                stepDetector,
                SensorManager.SENSOR_DELAY_GAME)) {
            isAvailable = true;
            Log.i(TAG, "计步传感器Detector可用！");
        }
        return  isAvailable;
    }

    public void unregisterStep(){
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            stepCallBack.Step(currentStep+(int)event.values[0]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
