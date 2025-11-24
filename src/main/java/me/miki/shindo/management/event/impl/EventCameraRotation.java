package me.miki.shindo.management.event.impl;

import lombok.Getter;
import me.miki.shindo.management.event.Event;

@Getter
public class EventCameraRotation extends Event {

    private float yaw;
    private float pitch;
    private float roll;
    private float thirdPersonDistance;

    public EventCameraRotation(float yaw, float pitch, float roll, float thirdPersonDistance) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.thirdPersonDistance = thirdPersonDistance;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public void setThirdPersonDistance(float thirdPersonDistance) {
        this.thirdPersonDistance = thirdPersonDistance;
    }
}