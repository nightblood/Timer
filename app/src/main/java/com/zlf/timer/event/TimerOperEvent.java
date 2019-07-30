package com.zlf.timer.event;

public class TimerOperEvent {
    public String oper;
    public int count;
    public TimerOperEvent(String oper, int count) {
        this.oper = oper;
        this.count = count;
    }
}
