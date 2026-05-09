package project;

import java.util.*;

public class PriorityScheduling {

    public static class GanttEntry {
        public String pid;
        public int start, end;
        GanttEntry(String pid, int start, int end) {
            this.pid = pid; this.start = start; this.end = end;
        }
    }

    public static class ProcessResult {
        public String pid;
        public int arrivalTime, burstTime, priority;
        public int waitingTime, turnaroundTime, responseTime;

        ProcessResult(Process p) {
            this.pid            = p.id;  // Process uses 'id' field
            this.arrivalTime    = p.arrivalTime;
            this.burstTime      = p.burstTime;
            this.priority       = p.priority;
            this.turnaroundTime = p.finishTime - p.arrivalTime;
            this.waitingTime    = turnaroundTime - p.burstTime;
            this.responseTime   = p.startTime   - p.arrivalTime;
        }
    }

    private static final Comparator<Process> PRIORITY_ORDER =
        Comparator.comparingInt((Process p) -> p.priority)
                  .thenComparingInt(p -> p.arrivalTime)
                  .thenComparing(p -> p.id);

    public List<ProcessResult> simulate(List<Process> processes, List<GanttEntry> gantt) {
        // reset
        for (Process p : processes) {
            p.remainingTime = p.burstTime;
            p.startTime     = -1;
            p.finishTime    = -1;
        }

        List<Process> procs = new ArrayList<>(processes);
        procs.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int time = 0, completed = 0, total = procs.size();
        Process current   = null;
        int     ganttStart = 0;

        while (completed < total) {
            List<Process> ready = new ArrayList<>();
            for (Process p : procs)
                if (p.arrivalTime <= time && p.remainingTime > 0) ready.add(p);

            if (ready.isEmpty()) {
                if (current != null && ganttStart < time) {
                    gantt.add(new GanttEntry(current.id, ganttStart, time));
                    current = null;
                }
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : procs)
                    if (p.remainingTime > 0 && p.arrivalTime > time)
                        nextArrival = Math.min(nextArrival, p.arrivalTime);
                gantt.add(new GanttEntry("idle", time, nextArrival));
                time = nextArrival; ganttStart = time;
                continue;
            }

            Process best = ready.stream().min(PRIORITY_ORDER).get();

            if (current == null || !best.id.equals(current.id)) {
                if (current != null && ganttStart < time)
                    gantt.add(new GanttEntry(current.id, ganttStart, time));
                current    = best;
                ganttStart = time;
                if (current.startTime == -1) current.startTime = time;
            }

            int runUntil = time + current.remainingTime;
            for (Process p : procs)
                if (p.arrivalTime > time && p.arrivalTime < runUntil && p.remainingTime > 0)
                    runUntil = p.arrivalTime;

            int elapsed = runUntil - time;
            current.remainingTime -= elapsed;
            time = runUntil;

            if (current.remainingTime == 0) {
                current.finishTime = time;
                gantt.add(new GanttEntry(current.id, ganttStart, time));
                ganttStart = time;
                completed++;
                current = null;
            }
        }

        List<ProcessResult> results = new ArrayList<>();
        for (Process p : processes) results.add(new ProcessResult(p));
        results.sort(Comparator.comparing(r -> r.pid));
        return results;
    }

    public void validate(List<Process> processes) {
        if (processes == null || processes.isEmpty())
            throw new IllegalArgumentException("Process list cannot be empty.");
        Set<String> seen = new HashSet<>();
        for (Process p : processes) {
            if (p.id == null || p.id.trim().isEmpty())
                throw new IllegalArgumentException("Every process must have a non-empty PID.");
            if (!seen.add(p.id))
                throw new IllegalArgumentException("Duplicate process ID: " + p.id);
            if (p.arrivalTime < 0)
                throw new IllegalArgumentException(p.id + ": arrival time cannot be negative.");
            if (p.burstTime <= 0)
                throw new IllegalArgumentException(p.id + ": burst time must be > 0.");
            if (p.priority < 1)
                throw new IllegalArgumentException(p.id + ": priority must be >= 1.");
        }
    }

    public double[] calculateAverages(List<ProcessResult> results) {
        if (results.isEmpty()) return new double[]{0, 0, 0};
        double wt = 0, tat = 0, rt = 0;
        for (ProcessResult r : results) { wt += r.waitingTime; tat += r.turnaroundTime; rt += r.responseTime; }
        int n = results.size();
        return new double[]{wt / n, tat / n, rt / n};
    }
}
