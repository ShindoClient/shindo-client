package me.miki.shindo.utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TimerUtils {

    public long lastMs;

    public TimerUtils() {
        this.lastMs = 0L;
    }

    public void reset() {
        this.lastMs = System.currentTimeMillis();
    }

    public boolean delay(long nextDelay) {
        return System.currentTimeMillis() - lastMs >= nextDelay;
    }

    public boolean delay(float nextDelay, boolean reset) {
        if (System.currentTimeMillis() - lastMs >= nextDelay) {
            if (reset) {
                this.reset();
            }
            return true;
        }
        return false;
    }

    public boolean isDelayComplete(double valueState) {
        return System.currentTimeMillis() - lastMs >= valueState;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.lastMs;
    }

}