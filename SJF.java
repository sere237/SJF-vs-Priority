package project;

import java.util.*;

/**
 * Preemptive SJF Scheduling (SRTF — Shortest Remaining Time First)
 * -----------------------------------------------------------------
 * - في كل وحدة زمن بنختار الـ process اللي عندها أقل remainingTime
 * - Tie-break 1: أقل arrivalTime (FCFS)
 * - Tie-break 2: أصغر ID أبجدياً
 */
public class SJF {

    public static class GanttEntry {
        public String pid;
        public int start, end;
        public GanttEntry(String pid, int start, int end) {
            this.pid = pid; this.start = start; this.end = end;
        }
    }

    // ── الـ method الرئيسية ───────────────────────────────────────────────────
    public List<Process> schedule(List<Process> processes, List<GanttEntry> gantt) {

        // reset كل process قبل البدء
        for (Process p : processes) {
            p.remainingTime = p.burstTime;
            p.startTime     = -1;
            p.finishTime    = -1;
            p.waitingTime   = 0;
            p.TAT           = 0;
            p.Rs            = 0;
        }

        int n           = processes.size();
        int currentTime = 0;
        int completed   = 0;

        String lastPid    = null;   // اسم الـ process اللي شغّالة
        int    blockStart = 0;      // بداية الـ Gantt block الحالي

        while (completed < n) {

            // ── اختار أفضل process في الـ ready queue ──────────────────────
            Process best = null;
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                    if (best == null) { best = p; continue; }
                    // أقل remaining
                    if (p.remainingTime < best.remainingTime) { best = p; continue; }
                    // تعادل → أقدم arrival
                    if (p.remainingTime == best.remainingTime
                            && p.arrivalTime < best.arrivalTime) { best = p; continue; }
                    // تعادل تاني → أصغر ID
                    if (p.remainingTime == best.remainingTime
                            && p.arrivalTime == best.arrivalTime
                            && p.id.compareTo(best.id) < 0) { best = p; }
                }
            }

            // ── CPU فاضي → قفز للـ process الجاية ─────────────────────────
            if (best == null) {
                // اقفل idle block في الـ Gantt
                if (lastPid != null) {
                    gantt.add(new GanttEntry(lastPid, blockStart, currentTime));
                    lastPid = null;
                }
                if (!"idle".equals(lastPid)) {
                    gantt.add(new GanttEntry("idle", currentTime, currentTime + 1));
                }
                currentTime++;
                continue;
            }

            // ── Preemption: لو في process جديدة بتشتغل ────────────────────
            if (!best.id.equals(lastPid)) {
                if (lastPid != null && blockStart < currentTime)
                    gantt.add(new GanttEntry(lastPid, blockStart, currentTime));
                lastPid    = best.id;
                blockStart = currentTime;
            }

            // ── أول مرة تشتغل → سجّل startTime ────────────────────────────
            if (best.startTime == -1)
                best.startTime = currentTime;

            // ── شغّل وحدة زمن واحدة ────────────────────────────────────────
            best.remainingTime--;
            currentTime++;

            // ── خلصت؟ ──────────────────────────────────────────────────────
            if (best.remainingTime == 0) {
                best.finishTime  = currentTime;
                best.TAT         = best.finishTime - best.arrivalTime;
                best.waitingTime = best.TAT - best.burstTime;
                best.Rs          = best.startTime - best.arrivalTime;
                completed++;

                // اقفل الـ Gantt block
                gantt.add(new GanttEntry(best.id, blockStart, currentTime));
                lastPid    = null;
                blockStart = currentTime;
            }
        }

        return processes;
    }

    // ── Validation ────────────────────────────────────────────────────────────
    public boolean validateInput(List<Process> processes) {
        if (processes == null || processes.isEmpty()) return false;
        Set<String> ids = new HashSet<>();
        for (Process p : processes) {
            if (p.arrivalTime < 0)    { System.out.println("Error: Negative arrival time for " + p.id); return false; }
            if (p.burstTime <= 0)     { System.out.println("Error: Invalid burst time for " + p.id);    return false; }
            if (ids.contains(p.id))   { System.out.println("Error: Duplicate ID " + p.id);              return false; }
            ids.add(p.id);
        }
        return true;
    }

    // ── Averages ──────────────────────────────────────────────────────────────
    public double[] calculateAverages(List<Process> result) {
        if (result.isEmpty()) return new double[]{0, 0, 0};
        double totalWT = 0, totalTAT = 0, totalRT = 0;
        for (Process p : result) {
            totalWT  += p.waitingTime;
            totalTAT += p.TAT;
            totalRT  += p.Rs;
        }
        int n = result.size();
        return new double[]{totalWT / n, totalTAT / n, totalRT / n};
    }
}
