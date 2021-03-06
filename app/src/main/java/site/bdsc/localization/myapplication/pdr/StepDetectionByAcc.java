package site.bdsc.localization.myapplication.pdr;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class StepDetectionByAcc implements SensorEventListener {
    private final String TAG = "DetectingStep";
    //存放三轴数据
    private final int valueNum = 5;
    //用于存放计算阈值的波峰波谷差值
    private float[] tempValue = new float[valueNum];
    private int tempCount = 0;
    //是否上升的标志位
    boolean isDirectionUp = false;
    //持续上升次数
    private int continueUpCount = 0;
    //上一点的持续上升的次数，为了记录波峰的上升次数
    private int continueUpFormerCount = 0;
    //上一点的状态，上升还是下降
    private boolean lastStatus = false;
    //波峰值
    private float peakOfWave = 0;
    //波谷值
    private float valleyOfWave = 0;
    //此次波峰的时间
    private long timeOfThisPeak = 0;
    //上次波峰的时间
    private long timeOfLastPeak = 0;
    //当前的时间
    private long timeOfNow = 0;
    //当前传感器的值
    float gravityNew = 0;
    //上次传感器的值
    private float gravityOld = 0;
    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    private final float initialValue = (float) 1.7;
    //初始阈值
    private float ThreadValue = (float) 2.0;

    //初始范围
    private float minValue = 11f;
    private float maxValue = 19.6f;
    private float average;
    private int CURRENT_SETP;
    private Context context;
    private StepCallBack stepCallBack;
    private SensorManager sensorManager;

    public StepDetectionByAcc(Context context, StepCallBack stepCallBack) {
        this.context = context;
        this.stepCallBack = stepCallBack;
    }

    public void registerStepListener() {
        // 注册加速度传感器
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        boolean isAvailable = sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        if (isAvailable) {
            Log.i(TAG, "加速度传感器可用！");
        } else {
            Log.i(TAG, "加速度传感器不可用！");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                calc_step(event);

            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void unregisterStep(){
        sensorManager.unregisterListener(this);
    }
    /**
     *  计算三轴的加速度的平均值
     **/
    synchronized private void calc_step(SensorEvent event) {
        average = (float) Math.sqrt(Math.pow(event.values[0], 2)
                + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
        detectingNewStep(average);
    }

    /*
     * 检测步子，并开始计步
     * 1.传入sersor中的数据
     * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
     * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
     */

    public void detectingNewStep(float values) {
        if (gravityOld == 0) {
            gravityOld = values;
        } else {
            if (DetectingPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();

                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= ThreadValue) && (timeOfNow - timeOfLastPeak) <= 2000) {
                    timeOfThisPeak = timeOfNow;
                    //更新界面的处理，不涉及到算法
                    preStep();
                }
                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= initialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = values;
    }

    private void preStep() {
        CURRENT_SETP++;
        try {
            stepCallBack.Step(CURRENT_SETP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于1.2g,小于2g
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * */
    public boolean DetectingPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

//        Log.v(TAG, "oldValue:" + oldValue);
        if (!isDirectionUp && lastStatus
                && (continueUpFormerCount >= 2 && (oldValue >= minValue && oldValue < maxValue))) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }

    /*
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     * */
    public float Peak_Valley_Thread(float value) {
        float tempThread = ThreadValue;
        if (tempCount < valueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, valueNum);
            for (int i = 1; i < valueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[valueNum - 1] = value;
        }
        return tempThread;
    }

    /*
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * */
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / valueNum;
        if (ave >= 8) {
//            Log.v(TAG, "超过8");
            ave = (float) 4.3;
        } else if (ave >= 7 && ave < 8) {
//            Log.v(TAG, "7-8");
            ave = (float) 3.3;
        } else if (ave >= 4 && ave < 7) {
//            Log.v(TAG, "4-7");
            ave = (float) 2.3;
        } else if (ave >= 3 && ave < 4) {
//            Log.v(TAG, "3-4");
            ave = (float) 2.0;
        } else {
//            Log.v(TAG, "else");
            ave = (float) 1.7;
        }
        return ave;
    }
}
