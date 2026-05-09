package project;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GanttChartPanel extends JPanel {

    public static class Entry {
        public String pid;
        public int start, end;
        public Entry(String pid, int start, int end) {
            this.pid = pid; this.start = start; this.end = end;
        }
    }

    private static final Color[] PALETTE = {
        new Color(66,  133, 244),
        new Color(52,  168, 83),
        new Color(251, 188, 4),
        new Color(234, 67,  53),
        new Color(155, 89,  182),
        new Color(26,  188, 156),
        new Color(230, 126, 34),
        new Color(52,  152, 219),
    };
    private static final Color IDLE_COLOR = new Color(200, 200, 200);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color IDLE_TEXT  = new Color(80, 80, 80);
    private static final Color TIME_COLOR = new Color(60, 60, 60);
    private static final Color BG_COLOR   = new Color(245, 245, 248);

    private static final int BAR_HEIGHT  = 44;
    private static final int TOP_PAD     = 16;
    private static final int BOTTOM_PAD  = 28;
    private static final int LEFT_PAD    = 10;
    private static final int RIGHT_PAD   = 10;
    private static final int MIN_BAR_W   = 36;

    private List<Entry>        entries  = new ArrayList<>();
    private Map<String, Color> colorMap = new HashMap<>();
    private double             scale    = 1.0;

    public GanttChartPanel() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(700, TOP_PAD + BAR_HEIGHT + BOTTOM_PAD + 8));
    }

    public void loadFromSJF(List<SJF.GanttEntry> gantt) {
        entries.clear();
        colorMap.clear();
        for (SJF.GanttEntry e : gantt)
            entries.add(new Entry(e.pid, e.start, e.end));
        assignColors();
        recalcSize();
        repaint();
    }

    public void loadFromPriority(List<PriorityScheduling.GanttEntry> gantt) {
        entries.clear();
        colorMap.clear();
        for (PriorityScheduling.GanttEntry e : gantt)
            entries.add(new Entry(e.pid, e.start, e.end));
        assignColors();
        recalcSize();
        repaint();
    }

    private void recalcSize() {
        if (entries.isEmpty()) return;
        int totalTime = entries.get(entries.size() - 1).end;
        int minUnit   = entries.stream().mapToInt(e -> e.end - e.start).min().orElse(1);
        scale = Math.max(600.0 / Math.max(totalTime, 1),
                         (double) MIN_BAR_W / Math.max(minUnit, 1));
        int neededW = LEFT_PAD + RIGHT_PAD + (int)(totalTime * scale) + MIN_BAR_W;
        int neededH = TOP_PAD + BAR_HEIGHT + BOTTOM_PAD;
        setPreferredSize(new Dimension(Math.max(neededW, 600), neededH));
        revalidate();
    }

    private void assignColors() {
        int idx = 0;
        for (Entry e : entries) {
            if (!e.pid.equals("idle") && !colorMap.containsKey(e.pid)) {
                colorMap.put(e.pid, PALETTE[idx % PALETTE.length]);
                idx++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (entries.isEmpty()) {
            g.setColor(new Color(160, 160, 160));
            g.setFont(new Font("Arial", Font.ITALIC, 13));
            g.drawString("Run simulation to see Gantt Chart", 20, TOP_PAD + BAR_HEIGHT / 2 + 5);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font labelFont = new Font("Arial", Font.BOLD,  13);
        Font timeFont  = new Font("Arial", Font.PLAIN, 11);
        FontMetrics lFm = g2.getFontMetrics(labelFont);
        FontMetrics tFm = g2.getFontMetrics(timeFont);

        int barY = TOP_PAD;

        for (Entry e : entries) {
            int x = LEFT_PAD + (int)(e.start * scale);
            int w = Math.max((int)((e.end - e.start) * scale), MIN_BAR_W);

            boolean isIdle = e.pid.equals("idle");
            Color fill = isIdle ? IDLE_COLOR : colorMap.getOrDefault(e.pid, PALETTE[0]);

            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(x + 2, barY + 2, w - 2, BAR_HEIGHT, 10, 10);

            g2.setColor(fill);
            g2.fillRoundRect(x, barY, w - 2, BAR_HEIGHT, 10, 10);

            g2.setColor(fill.darker());
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, barY, w - 2, BAR_HEIGHT, 10, 10);

            g2.setFont(labelFont);
            g2.setColor(isIdle ? IDLE_TEXT : TEXT_COLOR);
            int lw = lFm.stringWidth(e.pid);
            if (lw + 6 <= w - 2)
                g2.drawString(e.pid, x + (w - 2 - lw) / 2,
                              barY + BAR_HEIGHT / 2 + lFm.getAscent() / 2 - 2);
        }

        g2.setFont(timeFont);
        Set<Integer> drawn = new HashSet<>();
        for (Entry e : entries) {
            for (int t : new int[]{e.start, e.end}) {
                if (drawn.contains(t)) continue;
                drawn.add(t);
                int tx = LEFT_PAD + (int)(t * scale);
                String ts = String.valueOf(t);
                int tw = tFm.stringWidth(ts);
                g2.setColor(TIME_COLOR);
                g2.drawString(ts, tx - tw / 2, barY + BAR_HEIGHT + 16);
                g2.setColor(new Color(120, 120, 120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(tx, barY + BAR_HEIGHT, tx, barY + BAR_HEIGHT + 5);
            }
        }
    }
}
