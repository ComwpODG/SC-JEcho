import greenfoot.*;
import java.util.*;

public class LineDataManager extends Actor {

    private Deque<LineData> combatWindow = new ArrayDeque<>();
    private Map<String, Double> dpsMap = new HashMap<>();

    private Map<String, Deque<Sample>> dpsHistory = new HashMap<>();
    private long snapshotIntervalMillis = 100; // Snapshot every 100ms
    private long lastSnapshotTime = System.currentTimeMillis();
    public long displayTimeSpanMillis = 20000; // 10 seconds
    public int graphWidth = 1000;
    public int graphHeight = 800;
    public int maxDPS = 50000;
    
    
    
    public static int dpsFontSize = 15;
    
    public static void updateDPSFontSize(int newFontSize){
        dpsFontSize = newFontSize;
    }
    

    public static long now = 0;

    public void addCombatLine(LineData data) {
        now = data.timestampMillis;
        combatWindow.addLast(data);
        dpsMap.put(data.attacker, dpsMap.getOrDefault(data.attacker, 0.0) + data.damage);
    }

    public void act() {

        long realNow = System.currentTimeMillis();
        cleanupOldData2(realNow);

        if (now - lastSnapshotTime >= snapshotIntervalMillis) {
            snapshotDPS(realNow);
            lastSnapshotTime = realNow;
        }

        drawGraph(realNow);
    }
    
    public void cleanupOldData(long currentTime) {
        while (!combatWindow.isEmpty() && (currentTime - combatWindow.peekFirst().timestampMillis > 1000)) {
            LineData expired = combatWindow.removeFirst();
            dpsMap.put(expired.attacker, dpsMap.get(expired.attacker) - expired.damage); //Right here
            
            if (dpsMap.get(expired.attacker) <= 0.0) {
                dpsMap.remove(expired.attacker);
            }
    
            // Debug logging
            System.out.println("Removed from combatWindow: " + expired.attacker + " dealt " + expired.damage + " at " + expired.timestampMillis + " (current millis: "+currentTime+")");
        }
    }
    
    private void cleanupOldData2(long currentTime) {
        while (!combatWindow.isEmpty() && (currentTime - combatWindow.peekFirst().timestampMillis > 1000)) {
            LineData expired = combatWindow.removeFirst();
    
            Double currentDamage = dpsMap.get(expired.attacker);
            if (currentDamage != null) {
                double newDamage = currentDamage - expired.damage;
                if (newDamage <= 0.0) {
                    dpsMap.remove(expired.attacker);
                } else {
                    dpsMap.put(expired.attacker, newDamage);
                }
            } else {
                //System.out.println("Warning: Tried to remove " + expired.attacker + " from dpsMap, but they were not present!");
            }
    
            // Debug logging
            //System.out.println("Removed from combatWindow: " + expired.attacker + " dealt " + expired.damage + " at " + expired.timestampMillis + " (current millis: "+currentTime+")");
        }
    }



    private void snapshotDPS(long now) {
        Set<String> allPlayers = new HashSet<>(dpsHistory.keySet());
        allPlayers.addAll(dpsMap.keySet());

        for (String attacker : allPlayers) {
            dpsHistory.putIfAbsent(attacker, new ArrayDeque<>());
            Deque<Sample> history = dpsHistory.get(attacker);

            history.addLast(new Sample(now, dpsMap.getOrDefault(attacker, 0.0)));

            // Clean up old samples based on graph display time
            while (!history.isEmpty() && history.peekFirst().timestamp < now - displayTimeSpanMillis) {
                history.removeFirst();
            }
        }
    }
    
    public void drawGraph(long now) {
        GreenfootImage img = new GreenfootImage(graphWidth, graphHeight);
        img.setColor(Color.BLACK);
        img.fill();
    
        long tStart = now - displayTimeSpanMillis;
    
        int index = 0;
        for (String attacker : dpsHistory.keySet()) {
            Deque<Sample> history = dpsHistory.get(attacker);
            Color c = getColorForIndex(index);
            img.setColor(c);
    
            Sample prev = null;
    
            // Track the peak
            double peakDps = -1;
            long peakTime = 0;
    
            for (Sample s : history) {
                if (s.dps > peakDps) {
                    peakDps = s.dps;
                    peakTime = s.timestamp;
                }
    
                if (prev != null) {
                    double t1Percent = (prev.timestamp - tStart) / (double)displayTimeSpanMillis;
                    double t2Percent = (s.timestamp - tStart) / (double)displayTimeSpanMillis;
    
                    int x1 = (int)(t1Percent * graphWidth);
                    int x2 = (int)(t2Percent * graphWidth);
    
                    int y1 = graphHeight - (int)(prev.dps / maxDPS * graphHeight);
                    int y2 = graphHeight - (int)(s.dps / maxDPS * graphHeight);
    
                    img.drawLine(x1, y1, x2, y2);
                }
                prev = s;
            }
    
            // Draw peak label
            if (peakDps > 0) {
                double peakPercent = (peakTime - tStart) / (double)displayTimeSpanMillis;
                int peakX = (int)(peakPercent * graphWidth);
                int peakY = graphHeight - (int)(peakDps / maxDPS * graphHeight);
    
                String label = attacker + ": " + String.format("%.0f", peakDps);
    
                GreenfootImage textImg = new GreenfootImage(label, dpsFontSize, c, new Color(0,0,0,128));
                img.drawImage(textImg, Math.min(peakX + 5, graphWidth - textImg.getWidth()), Math.max(peakY - textImg.getHeight()/2, 0));
            }
    
            index++;
        }
    
        img.setColor(Color.GRAY);
        img.drawRect(0, 0, graphWidth - 1, graphHeight - 1);
    
        DPSGraphDisplay.setGraphImage(img);
    }


    private Color getColorForIndex(int index) {
        Color[] colors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK,
            Color.WHITE, Color.LIGHT_GRAY
        };
        return colors[index % colors.length];
    }
}
