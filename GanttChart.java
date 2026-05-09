package project;

import java.util.*;

public class GanttChart {

    public void printGanttChart(List<Process> result, String title) {
        System.out.println("\n=== Gantt Chart (" + title + ") ===");

        List<Process> ordered = new ArrayList<>(result);
        ordered.sort(Comparator.comparingInt(p -> p.startTime));

        StringBuilder top    = new StringBuilder("|");
        StringBuilder bottom = new StringBuilder();

        int prevEnd = 0;
        List<String[]> entries = new ArrayList<>();

        for (Process p : ordered) {
            if (p.startTime > prevEnd)
                entries.add(new String[]{"idle", String.valueOf(prevEnd), String.valueOf(p.startTime)});
            entries.add(new String[]{p.id, String.valueOf(p.startTime), String.valueOf(p.finishTime)});
            prevEnd = p.finishTime;
        }

        for (int i = 0; i < entries.size(); i++) {
            String label = " " + entries.get(i)[0] + " ";
            int width    = Math.max(label.length(), 4);
            top.append(String.format("%-" + width + "s", label)).append("|");
            String startStr = entries.get(i)[1];
            bottom.append(startStr);
            bottom.append(" ".repeat(Math.max(0, width + 1 - startStr.length())));
        }

        if (!entries.isEmpty())
            bottom.append(entries.get(entries.size() - 1)[2]);

        System.out.println(top);
        System.out.println(bottom);
    }

    public void printGanttChartFromList(List<String[]> gantt, String title) {
        System.out.println("\n=== Gantt Chart (" + title + ") ===");

        StringBuilder top    = new StringBuilder("|");
        StringBuilder bottom = new StringBuilder();

        for (int i = 0; i < gantt.size(); i++) {
            String label = " " + gantt.get(i)[0] + " ";
            int width    = Math.max(label.length(), 4);
            top.append(String.format("%-" + width + "s", label)).append("|");
            String startStr = gantt.get(i)[1];
            bottom.append(startStr);
            bottom.append(" ".repeat(Math.max(0, width + 1 - startStr.length())));
        }

        if (!gantt.isEmpty())
            bottom.append(gantt.get(gantt.size() - 1)[2]);

        System.out.println(top);
        System.out.println(bottom);
    }

    public void printResultsTable(List<Process> result, boolean showPriority, String title) {
        System.out.println("\n=== Results Table (" + title + ") ===");

        if (showPriority) {
            System.out.printf("%-6s %-8s %-6s %-5s %-7s %-7s %-6s %-6s %-6s%n",
                    "PID", "Arrival", "Burst", "Pri", "Start", "Finish", "WT", "TAT", "RT");
            System.out.println("-".repeat(63));
        } else {
            System.out.printf("%-6s %-8s %-6s %-7s %-7s %-6s %-6s %-6s%n",
                    "PID", "Arrival", "Burst", "Start", "Finish", "WT", "TAT", "RT");
            System.out.println("-".repeat(56));
        }

        double sumWT = 0, sumTAT = 0, sumRT = 0;

        for (Process p : result) {
            if (showPriority) {
                System.out.printf("%-6s %-8d %-6d %-5d %-7d %-7d %-6d %-6d %-6d%n",
                        p.id, p.arrivalTime, p.burstTime, p.priority,
                        p.startTime, p.finishTime, p.waitingTime, p.TAT, p.Rs);
            } else {
                System.out.printf("%-6s %-8d %-6d %-7d %-7d %-6d %-6d %-6d%n",
                        p.id, p.arrivalTime, p.burstTime,
                        p.startTime, p.finishTime, p.waitingTime, p.TAT, p.Rs);
            }
            sumWT  += p.waitingTime;
            sumTAT += p.TAT;
            sumRT  += p.Rs;
        }

        int n = result.size();
        if (showPriority) {
            System.out.println("-".repeat(63));
            System.out.printf("%-6s %-8s %-6s %-5s %-7s %-7s %-6.2f %-6.2f %-6.2f%n",
                    "AVG", "", "", "", "", "", sumWT / n, sumTAT / n, sumRT / n);
        } else {
            System.out.println("-".repeat(56));
            System.out.printf("%-6s %-8s %-6s %-7s %-7s %-6.2f %-6.2f %-6.2f%n",
                    "AVG", "", "", "", "", sumWT / n, sumTAT / n, sumRT / n);
        }
    }
}
