package io.streamzi.eventflow.runtime;

import io.streamzi.eventflow.annotations.CloudEventComponentTimer;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Timed executor for method
 *
 * @author hhiden
 */
public class CloudEventTimer {
    private static final Logger logger = Logger.getLogger(CloudEventTimer.class.getName());

    private Object timedObject;
    private Method timerMethod;
    private int interval = 1000;
    private Timer timer;

    public CloudEventTimer() {
    }

    public CloudEventTimer(Object timedObject, Method timerMethod) {
        this.timedObject = timedObject;
        this.timerMethod = timerMethod;

        // Try and find the interval
        CloudEventComponentTimer annotation = timerMethod.getAnnotation(CloudEventComponentTimer.class);
        if (annotation != null) {
            interval = annotation.interval();
        }
    }

    public void startTimer() {
        if (timer == null) {
            timer = new Timer(timerMethod.getName(), true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        timerMethod.invoke(timedObject);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error running method: " + e.getMessage(), e);
                    }
                }
            }, 0, interval);
        }
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
