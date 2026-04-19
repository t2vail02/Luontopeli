// 📁 sensor/StepCounterManager.kt
package com.example.luontopeli.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Sensorien hallintapalvelu askelmittarille ja gyroskoopille.
 */
class StepCounterManager(context: Context) {

    /** Android-järjestelmän SensorManager sensorien rekisteröimiseen */
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /** Askeltunnistin-sensori (null jos laite ei tue sitä) */
    // TYPE_STEP_DETECTOR laukeaa jokaisen yksittäisen askeleen kohdalla
    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    /** Gyroskooppi-sensori (null jos laite ei tue sitä) */
    private val gyroSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    /** Askelmittarin SensorEventListener-kuuntelija */
    private var stepListener: SensorEventListener? = null
    /** Gyroskoopin SensorEventListener-kuuntelija */
    private var gyroListener: SensorEventListener? = null

    /**
     * Käynnistää askelten laskemisen.
     * @param onStep Callback-funktio joka kutsutaan jokaisen askeleen kohdalla
     */
    fun startStepCounting(onStep: () -> Unit) {
        stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                    onStep()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        // Rekisteröi kuuntelija vain jos laite tukee askeltunnistinta
        stepSensor?.let {
            sensorManager.registerListener(
                stepListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /** Pysäyttää askelten laskemisen. */
    fun stopStepCounting() {
        stepListener?.let { sensorManager.unregisterListener(it) }
        stepListener = null
    }

    /**
     * Käynnistää gyroskoopin lukemisen.
     * @param onRotation Callback joka saa parametreina x, y, z -kiertonopeudet (rad/s)
     */
    fun startGyroscope(onRotation: (Float, Float, Float) -> Unit) {
        gyroListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    // values[0]=x, values[1]=y, values[2]=z kiertonopeudet rad/s
                    onRotation(event.values[0], event.values[1], event.values[2])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        gyroSensor?.let {
            sensorManager.registerListener(
                gyroListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    /** Pysäyttää gyroskoopin lukemisen. */
    fun stopGyroscope() {
        gyroListener?.let { sensorManager.unregisterListener(it) }
        gyroListener = null
    }

    /** Pysäyttää kaikki sensorit. */
    fun stopAll() {
        stopStepCounting()
        stopGyroscope()
    }

    /** Tarkistaa tukeeko laite askeltunnistinta. */
    fun isStepSensorAvailable(): Boolean = stepSensor != null

    companion object {
        /** Keskimääräinen askelpituus metreinä matkan laskemiseen */
        const val STEP_LENGTH_METERS = 0.74f
    }
}