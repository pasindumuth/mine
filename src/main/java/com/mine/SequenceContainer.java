package com.mine;

public class SequenceContainer {

    private Sequence sequence;
    private long startTime;
    private long endTime;

    public SequenceContainer(Sequence sequence, long startTime) {
        this.sequence = sequence;
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Sequence getSequence() {
        return this.sequence;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}