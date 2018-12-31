package com.mine.manager2;

public class RepresentationContainer {

    private SubtraceRepresentation representation;
    private long startTime;
    private long endTime;

    public RepresentationContainer(SubtraceRepresentation representation, long startTime) {
        this.representation = representation;
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public SubtraceRepresentation getRepresentation() {
        return representation;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
