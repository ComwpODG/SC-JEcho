import greenfoot.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.regex.*;
import java.util.Date;
import java.util.TimeZone;

public class LogTail extends Actor {

    private File file;
    private int xPos;
    private List<String> newLines = Collections.synchronizedList(new ArrayList<>());
    private Thread tailThread;
    private boolean isCombatLog = false;
    
    
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    private Pattern combatPattern = Pattern.compile(
        "(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+CMBT\\s+\\|\\s+Damage\\s+(\\S+)\\|\\d+\\s+->\\s+(\\S+)\\|\\d+\\s+([\\d\\.]+).*\\s+(\\S+)\\s+(\\S+)"
    );
    
    public static boolean banNPCs = false;

    public static int fontSize = 15;
    
    public static void updateFontSize(int newFontSize){
        fontSize = newFontSize;
    }
    
    
    
    
    public static List<String> hiddenTerms = new ArrayList<String>();
    

    
    
    public LogTail(File file, int xPos, boolean isCombatLog) {
        this.file = file;
        this.xPos = xPos;
        this.isCombatLog = isCombatLog;

        startTailing();
    }

    private void startTailing() {
        tailThread = new Thread(() -> {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                long filePointer = raf.length();

                while (true) {
                    long length = file.length();
                    if (length < filePointer) {
                        filePointer = length; // Log rotated
                    }
                    else if (length > filePointer) {
                        raf.seek(filePointer);
                        String line;
                        while ((line = raf.readLine()) != null) {
                            String decoded = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                            newLines.add(decoded);
                        }
                        filePointer = raf.getFilePointer();
                    }
                    Thread.sleep(100);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        tailThread.setDaemon(true);
        tailThread.start();
    }

    public void act() {
        synchronized(newLines) {
            Iterator<String> it = newLines.iterator();
            while (it.hasNext()) {
                String line = it.next();
                if(line.contains("coinmanzac"))
                {
                    continue;
                }
                
                if(isCombatLog)
                {
                    LineData ld = parseCombatLine(line);
                    if(ld!=null)
                    {
                        if(!banNPCs||!line.contains("NPC")){
                            this.getWorldOfType(LogViewerWorld.class).getLDM().addCombatLine(ld);
                        }
                        
                        boolean show = true;
                        
                        
                        for(String str : hiddenTerms){
                            
                            if(line.contains(str)){
                                show = false;
                            }
                        }
                        
                        if(show){
                            World world = getWorld();
                            moveAllLogLinesUp();
                            if (world != null) {
                                world.addObject(new LogLine(ld.toString(), fontSize, xPos), xPos, world.getHeight()-10);
                                
                            }
                            
                        }
                    }
                }
                else {
                    World world = getWorld();
                    if (world != null) {
                        
                        if(line.contains("!"))
                        {
                            CommandsLibrary.tryExecuteCommand(line);
                        }
                        
                        boolean show = true;
                        for(String str : hiddenTerms){
                            if(line.contains(str)){
                                show = false;
                            }
                        }
                        
                        if(show){
                            moveAllLogLinesUp();
                            world.addObject(new LogLine(line,fontSize,xPos), xPos, world.getHeight()-10);
                        }
                        
                    }
                }
                it.remove();
            }
        }
    }
    
    public void moveAllLogLinesUp()
    {
        List<LogLine> h = getWorld().getObjects(LogLine.class);
        for(LogLine ll : h)
        {
            ll.setLocation(ll.getX(),ll.getY()-fontSize);
        }
    }
    

    
    private LineData parseCombatLine(String line) {
        Matcher m = combatPattern.matcher(line);
        if (m.find()) {
            try {
                long millis = convertToEpochMillis(m.group(1));
    
                String attacker = m.group(2);
                String defender = m.group(3);
                double damage = Double.parseDouble(m.group(4));
                String weapon = m.group(5);
                String element = m.group(6);
    
                return new LineData(millis, attacker, defender, damage, weapon, element);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private long convertToEpochMillis(String timeString) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getDefault()); // Or force UTC if you prefer
    
        Date date = sdf.parse(timeString);
    
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
    
        Calendar now = Calendar.getInstance();
    
        // Replace date fields with today’s date
        cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
    
        return cal.getTimeInMillis();
    }
}
