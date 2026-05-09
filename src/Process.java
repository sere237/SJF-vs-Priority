package project;

public class Process {
    public String id;
    public int arrivalTime;
    public int burstTime;
    public int priority;
    public int startTime;
    public int finishTime;
    public int waitingTime;
    public int TAT;
    public int Rs;
    public int remainingTime;

    public Process(String id, int arrivalTime, int burstTime) {
        this.id          = id;
        this.arrivalTime = arrivalTime;
        this.burstTime   = burstTime;
        this.priority    = 0;
        this.remainingTime = burstTime;
    }

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id            = id;
        this.arrivalTime   = arrivalTime;
        this.burstTime     = burstTime;
        this.priority      = priority;
        this.remainingTime = burstTime;
    }
}
