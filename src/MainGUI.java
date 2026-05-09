package project;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MainGUI extends JFrame {

    // ── tables & display ────────────────────────────────────────────────────
    private JTable inputTable;
    private JTable sjfResultTable, priResultTable, compTable;
    private GanttChartPanel ganttSJFPanel, ganttPRIPanel;
    private JTextArea analysisArea, conclusionArea;
    private JLabel sjfAvgLabel, priAvgLabel;

    public MainGUI() {
        setTitle("SJF vs Priority Scheduling — Comparison Tool");
        setSize(1200, 820);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildLeft(),  BorderLayout.WEST);
        add(buildRight(), BorderLayout.CENTER);
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LEFT PANEL — input
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildLeft() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(360, 820));
        p.setBorder(BorderFactory.createTitledBorder("Control Panel"));

        // ── process count ──
        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countRow.add(new JLabel("Number of Processes:"));
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        countRow.add(spinner);
        JButton applyBtn = new JButton("Apply");
        countRow.add(applyBtn);
        p.add(countRow);

        // ── input table ──
        String[] cols = {"PID", "Arrival", "Burst", "Priority"};
        DefaultTableModel inputModel = new DefaultTableModel(cols, 0);
        inputTable = new JTable(inputModel);
        inputTable.setRowHeight(22);
        JScrollPane inputScroll = new JScrollPane(inputTable);
        inputScroll.setPreferredSize(new Dimension(340, 160));
        p.add(new JLabel("  Process Input:"));
        p.add(inputScroll);
        p.add(Box.createVerticalStrut(8));

        // ── scenario buttons ──
        p.add(new JLabel("  Load Test Scenario:"));
        JPanel scenPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        scenPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
        String[] scens = {"Scenario A: Basic", "Scenario B: Conflict",
                          "Scenario C: Starvation", "Scenario D: Validation"};
        for (int i = 0; i < 4; i++) {
            final int si = i;
            JButton b = new JButton(scens[i]);
            b.setFont(new Font("Arial", Font.PLAIN, 11));
            b.addActionListener(e -> loadScenario(si, inputModel, spinner));
            scenPanel.add(b);
        }
        p.add(scenPanel);
        p.add(Box.createVerticalStrut(12));

        // ── run button ──
        JButton runBtn = new JButton("▶  Run Simulation");
        runBtn.setFont(new Font("Arial", Font.BOLD, 14));
        runBtn.setBackground(new Color(66, 133, 244));
        runBtn.setForeground(Color.WHITE);
        runBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        runBtn.addActionListener(e -> runSimulation(inputModel));
        p.add(runBtn);

        // ── apply action ──
        applyBtn.addActionListener(e -> {
            int cnt = (int) spinner.getValue();
            inputModel.setRowCount(0);
            for (int i = 1; i <= cnt; i++)
                inputModel.addRow(new Object[]{"P" + i, "", "", ""});
        });

        // default rows
        for (int i = 1; i <= 4; i++)
            inputModel.addRow(new Object[]{"P" + i, "", "", ""});

        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RIGHT PANEL — results
    // ════════════════════════════════════════════════════════════════════════
    private JScrollPane buildRight() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(6, 6, 6, 6));

        p.add(buildAlgoSection("SJF — Shortest Job First", true));
        p.add(Box.createVerticalStrut(8));
        p.add(buildAlgoSection("Priority Scheduling  (lower # = higher urgency)", false));
        p.add(Box.createVerticalStrut(8));
        p.add(buildCompSection());
        p.add(Box.createVerticalStrut(8));
        p.add(buildConcSection());

        JScrollPane sp = new JScrollPane(p);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel buildAlgoSection(String title, boolean isSJF) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBorder(BorderFactory.createTitledBorder(title));

        // gantt visual panel
        GanttChartPanel gantt = new GanttChartPanel();
        JScrollPane gScroll = new JScrollPane(gantt);
        gScroll.setPreferredSize(new Dimension(700, 100));
        gScroll.getHorizontalScrollBar().setUnitIncrement(16);
        gScroll.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));

        // result table
        String[] cols = isSJF
            ? new String[]{"PID","Arrival","Burst","Start","Finish","WT","TAT","RT"}
            : new String[]{"PID","Arrival","Burst","Priority","Start","Finish","WT","TAT","RT"};
        JTable tbl = new JTable(new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        tbl.setRowHeight(20);
        JScrollPane tScroll = new JScrollPane(tbl);
        tScroll.setPreferredSize(new Dimension(700, 110));
        tScroll.setBorder(BorderFactory.createTitledBorder("Results Table"));

        JLabel avgLbl = new JLabel("Averages: —");
        avgLbl.setFont(new Font("Consolas", Font.BOLD, 12));
        avgLbl.setForeground(new Color(30, 100, 200));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(gScroll);
        inner.add(tScroll);
        inner.add(avgLbl);

        card.add(inner, BorderLayout.CENTER);

        if (isSJF) { ganttSJFPanel = gantt; sjfResultTable = tbl; sjfAvgLabel = avgLbl; }
        else        { ganttPRIPanel = gantt; priResultTable = tbl; priAvgLabel = avgLbl; }

        return card;
    }

    private JPanel buildCompSection() {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBorder(BorderFactory.createTitledBorder("📊  Comparison Summary"));

        compTable = new JTable(new DefaultTableModel(
            new String[]{"Metric", "SJF", "Priority"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        compTable.setRowHeight(22);
        JScrollPane cs = new JScrollPane(compTable);
        cs.setPreferredSize(new Dimension(700, 90));

        analysisArea = new JTextArea(4, 60);
        analysisArea.setEditable(false);
        analysisArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        analysisArea.setText("Analysis will appear here after simulation.");

        card.add(cs, BorderLayout.NORTH);
        card.add(new JScrollPane(analysisArea), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildConcSection() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createTitledBorder("✅  Final Conclusion"));
        conclusionArea = new JTextArea(4, 60);
        conclusionArea.setEditable(false);
        conclusionArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        conclusionArea.setForeground(new Color(0, 120, 60));
        conclusionArea.setText("Conclusion will appear here after simulation.");
        card.add(new JScrollPane(conclusionArea), BorderLayout.CENTER);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SCENARIOS
    // ════════════════════════════════════════════════════════════════════════
    private void loadScenario(int idx, DefaultTableModel model, JSpinner spinner) {
        Object[][][] data = {
            // A: basic
            {{"P1","0","8","3"},{"P2","1","4","1"},{"P3","2","9","2"},{"P4","3","5","4"},{"P5","4","2","5"}},
            // B: conflict
            {{"P1","0","2","5"},{"P2","0","10","1"},{"P3","1","3","4"},{"P4","2","1","3"}},
            // C: starvation
            {{"P1","0","5","3"},{"P2","0","3","1"},{"P3","0","4","1"},{"P4","0","2","1"},{"P5","0","6","2"}},
            // D: validation (bad data)
            {{"P1","0","5","1"},{"P2","-2","3","2"},{"P3","1","abc","3"}}
        };

        if (idx == 3) {
            spinner.setValue(3);
            model.setRowCount(0);
            for (Object[] row : data[3]) model.addRow(row);
            JOptionPane.showMessageDialog(this,
                "Scenario D loaded with invalid data:\n- P2 has negative arrival (-2)\n- P3 has non-numeric burst (abc)\n\nClick Run Simulation to see validation.",
                "Validation Scenario", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        spinner.setValue(data[idx].length);
        model.setRowCount(0);
        for (Object[] row : data[idx]) model.addRow(row);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RUN SIMULATION
    // ════════════════════════════════════════════════════════════════════════
    private void runSimulation(DefaultTableModel model) {
        // ── parse & validate ──────────────────────────────────────────────
        List<Process> sjfProcs = new ArrayList<>();
        List<Process> priProcs = new ArrayList<>();
        StringBuilder errors   = new StringBuilder();

        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                String pid = model.getValueAt(i, 0).toString().trim();
                int at  = Integer.parseInt(model.getValueAt(i, 1).toString().trim());
                int bt  = Integer.parseInt(model.getValueAt(i, 2).toString().trim());
                int pri = Integer.parseInt(model.getValueAt(i, 3).toString().trim());

                if (at < 0)  errors.append(pid).append(": Arrival Time must be ≥ 0\n");
                if (bt <= 0) errors.append(pid).append(": Burst Time must be > 0\n");
                if (pri < 1) errors.append(pid).append(": Priority must be ≥ 1\n");

                if (at >= 0 && bt > 0 && pri >= 1) {
                    sjfProcs.add(new Process(pid, at, bt));
                    priProcs.add(new Process(pid, at, bt, pri));
                }
            } catch (Exception ex) {
                errors.append("Row ").append(i + 1).append(": Invalid input — ").append(ex.getMessage()).append("\n");
            }
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this,
                "❌ Validation Errors:\n\n" + errors,
                "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ── run SJF ───────────────────────────────────────────────────────
        SJF sjf = new SJF();
        List<SJF.GanttEntry> sjfGantt = new ArrayList<>();
        List<Process> sjfResult = sjf.schedule(sjfProcs, sjfGantt);
        double[] sjfAvg = sjf.calculateAverages(sjfResult);

        // ── run Priority ──────────────────────────────────────────────────
        PriorityScheduling ps = new PriorityScheduling();
        List<PriorityScheduling.GanttEntry> priGantt = new ArrayList<>();
        List<PriorityScheduling.ProcessResult> priResult = ps.simulate(priProcs, priGantt);
        double[] priAvg = ps.calculateAverages(priResult);

        // ── fill SJF Gantt ────────────────────────────────────────────────
        ganttSJFPanel.loadFromSJF(sjfGantt);

        // ── fill Priority Gantt ───────────────────────────────────────────
        ganttPRIPanel.loadFromPriority(priGantt);

        // ── fill SJF table ────────────────────────────────────────────────
        DefaultTableModel m1 = (DefaultTableModel) sjfResultTable.getModel();
        m1.setRowCount(0);
        for (Process p : sjfResult)
            m1.addRow(new Object[]{p.id, p.arrivalTime, p.burstTime, p.startTime, p.finishTime, p.waitingTime, p.TAT, p.Rs});
        sjfAvgLabel.setText(String.format("Avg WT: %.2f  |  Avg TAT: %.2f  |  Avg RT: %.2f", sjfAvg[0], sjfAvg[1], sjfAvg[2]));

        // ── fill Priority table ───────────────────────────────────────────
        DefaultTableModel m2 = (DefaultTableModel) priResultTable.getModel();
        m2.setRowCount(0);
        for (PriorityScheduling.ProcessResult r : priResult) {
        	int startTime = r.arrivalTime + r.responseTime;
            m2.addRow(new Object[]{r.pid, r.arrivalTime, r.burstTime, r.priority, startTime, r.arrivalTime + r.turnaroundTime, r.waitingTime, r.turnaroundTime, r.responseTime});}
        priAvgLabel.setText(String.format("Avg WT: %.2f  |  Avg TAT: %.2f  |  Avg RT: %.2f", priAvg[0], priAvg[1], priAvg[2]));

        // ── comparison table ──────────────────────────────────────────────
        DefaultTableModel cm = (DefaultTableModel) compTable.getModel();
        cm.setRowCount(0);
        cm.addRow(new Object[]{"Avg Waiting Time",     String.format("%.2f", sjfAvg[0]), String.format("%.2f", priAvg[0])});
        cm.addRow(new Object[]{"Avg Turnaround Time",  String.format("%.2f", sjfAvg[1]), String.format("%.2f", priAvg[1])});
        cm.addRow(new Object[]{"Avg Response Time",    String.format("%.2f", sjfAvg[2]), String.format("%.2f", priAvg[2])});

        // ── analysis ─────────────────────────────────────────────────────
        StringBuilder analysis = new StringBuilder();
        analysis.append("=== ANALYSIS ===\n\n");
        analysis.append(sjfAvg[0] <= priAvg[0]
            ? "✔ SJF achieves lower average waiting time — short jobs are served faster.\n"
            : "✔ Priority achieves lower waiting time — urgent processes run first.\n");
        analysis.append(sjfAvg[1] <= priAvg[1]
            ? "✔ SJF has better turnaround time — overall faster completion.\n"
            : "✔ Priority has better turnaround time for this workload.\n");
        analysis.append(sjfAvg[2] <= priAvg[2]
            ? "✔ SJF gives better response time — processes start sooner on average.\n"
            : "✔ Priority gives better response time for high-priority processes.\n");

        // starvation check
        boolean sjfStarve = sjfResult.stream().anyMatch(p -> p.waitingTime > 2 * sjfAvg[0] + 1);
        boolean priStarve = priResult.stream().anyMatch(r -> r.waitingTime > 2 * priAvg[0] + 1);
        analysis.append("\nStarvation risk — SJF: ").append(sjfStarve ? "⚠ YES" : "✔ No").append("\n");
        analysis.append("Starvation risk — Priority: ").append(priStarve ? "⚠ YES" : "✔ No").append("\n");
        analysisArea.setText(analysis.toString());

        // ── conclusion ────────────────────────────────────────────────────
        StringBuilder conc = new StringBuilder();
        String betterWT  = sjfAvg[0] <= priAvg[0] ? "SJF" : "Priority";
        String betterTAT = sjfAvg[1] <= priAvg[1] ? "SJF" : "Priority";
        String betterRT  = sjfAvg[2] <= priAvg[2] ? "SJF" : "Priority";
        conc.append("• Best Avg Waiting Time:      ").append(betterWT).append("\n");
        conc.append("• Best Avg Turnaround Time:   ").append(betterTAT).append("\n");
        conc.append("• Best Avg Response Time:     ").append(betterRT).append("\n\n");
        conc.append("Trade-off: SJF minimises wait by favouring short jobs (may starve long ones).\n");
        conc.append("Priority ensures urgent tasks run first (may starve low-priority ones).\n\n");
        String rec = (sjfAvg[0] + sjfAvg[1]) <= (priAvg[0] + priAvg[1]) ? "SJF" : "Priority";
        conc.append("Recommended for this workload: ").append(rec).append(" (based on combined WT + TAT).");
        conclusionArea.setText(conc.toString());
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(MainGUI::new);
    }
}
