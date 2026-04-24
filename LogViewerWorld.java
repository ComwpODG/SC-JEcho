import greenfoot.*;
import javax.swing.*;
import java.io.File;
import java.util.*;

public class LogViewerWorld extends World {

    private boolean initialized = false;
    private LineDataManager h = new LineDataManager();
    public LogViewerWorld() {
        super(1000, 800, 1);
    }

    public void started() {
        if (!initialized) {
            initialized = true; // Prevent re-initialization if user pauses/resumes

            File folder = chooseFolder();
            if (folder != null) {
                File file1 = new File(folder, "combat.log");
                File file2 = new File(folder, "chat.log");

                addObject(new LogTail(file1, 20, true), 0, 0);
                addObject(new LogTail(file2, 20, false), 0, 0);
            }
        }
        addObject(new DPSGraphDisplay(),500,400);
        addObject(h,0,0);
        
        CommandsLibrary.registerCommand("clear", 
            args -> {
                        List<LogLine> h = getObjects(LogLine.class);
                        for(LogLine ll : h)
                        {
                            ll.setLocation(ll.getX(),-20);
                        }
                        sysMsg("The chat has been cleared.");
                    }
                    );
                    
        CommandsLibrary.registerCommand("fontsize", 
            args -> {
                        try {
                            int size = Integer.parseInt(args.trim());
                            if(size>32) {
                                sysMsg("Be reasonable. Try a value between 9 and 32.");
                            } else if(size<9) {
                                sysMsg("That font won'e be legible. Try a value between 9 and 32, or to hide chat, use \"!hide CHAT\"");
                            }
                            else{
                                LogTail.updateFontSize(size);
                                sysMsg("The Font Size has been set to "+size+".");
                            }
                            
                        } catch (NumberFormatException e) {
                            sysMsg("I'm really gonna do it this time. Command usage: !fontsize <integer>");
                        }
                    }
                    );
                    
        CommandsLibrary.registerCommand("dpsfontsize", 
            args -> {
                        try {
                            int size = Integer.parseInt(args.trim());
                            if(size>32) {
                                sysMsg("Be reasonable. Try a value between 9 and 32.");
                            } else if(size<9) {
                                sysMsg("That font won'e be legible. Try a value between 9 and 32, or to hide chat, use \"!hide CHAT\"");
                            }
                            else{
                                LineDataManager.updateDPSFontSize(size);
                                
                                sysMsg("The Font Size has been set to "+size+".");
                            }
                            
                        } catch (NumberFormatException e) {
                            sysMsg("I'm really gonna do it this time. Command usage: !dpsfontsize <integer>");
                        }
                    }
                    );
                    
        CommandsLibrary.registerCommand("hide", 
            args -> {
                        try {
                            String toHide = args.trim();
                            if(!LogTail.hiddenTerms.contains(toHide))
                            {
                                LogTail.hiddenTerms.add(toHide);
                            }
                            
                            sysMsg("Blocked Terms: "+String.join(", ", LogTail.hiddenTerms));
                        } catch (Exception e) {
                            sysMsg("Literacy challenge impossible.. Command usage: !hide <term>");
                        }
                    }
                    );
                    
 
        CommandsLibrary.registerCommand("unhide", 
            args -> {
                        try {
                            String toHide = args.trim();
                            if(toHide.equals("all"))
                            {
                                LogTail.hiddenTerms.clear();
                                sysMsg("1984! 1984! I won't hide anything anymore.");
                            }
                            else if(!LogTail.hiddenTerms.contains(toHide))
                            {
                                sysMsg("I wasn't blocking that term.");
                            }
                            else {
                                LogTail.hiddenTerms.remove(toHide);
                            }
                            sysMsg("Blocked Terms: "+String.join(", ", LogTail.hiddenTerms));
                            
                        } catch (Exception e) {
                            sysMsg("Nope. Try again.. Command usage: !unhide <term|\"all\">");
                        }
                    }
                    );
                    
        CommandsLibrary.registerCommand("cleargraph", 
            args -> {
                        try {
                            LineDataManager ldm = getObjects(LineDataManager.class).get(0);
                            ldm.cleanupOldData(System.currentTimeMillis());
                            sysMsg("Graph has been cleared.");
                            
                            
                        } catch (Exception e) {
                            sysMsg("Nope. Try again.. Command usage: !cleargraph");
                        }
                    }
                    );
                    
                    
        CommandsLibrary.registerCommand("disablenpcs", 
            args -> {
                        try {
                            LogTail.banNPCs = true;
                            sysMsg("NPCs won't have their damage logged now.");
                            
                        } catch (Exception e) {
                            sysMsg("Nope. Try again.. Command usage: !disablenpcs");
                        }
                    }
                    );
                    
        CommandsLibrary.registerCommand("enablenpcs", 
            args -> {
                        try {
                            LogTail.banNPCs = false;
                            sysMsg("NPCs will have their damage logged normally now.");
                            
                        } catch (Exception e) {
                            sysMsg("Nope. Try again.. Command usage: !disablenpcs");
                        }
                    }
                    );
                    
                    
        CommandsLibrary.registerCommand("help", 
            args -> {
                        try {
                            sysMsg("Commands:");
                            sysMsg("!dpsfontsize <int> - changes the dps font size.");
                            sysMsg("!fontsize <int> - changes the chat font size.");
                            sysMsg("!hide <term> - hides all log messages containing a term.");
                            sysMsg("!unhide <term> - removes a term from the hidden terms list.");
                            sysMsg("!unhide all - clears the list of hidden terms.");
                            sysMsg("!clear - clears the chat and combat log.");
                            sysMsg("!disablenpcs - disables logging of damage dealt by NPCs.");
                            sysMsg("!enablenpcs - enables logging of damage dealt by NPCs.");
                            sysMsg("!cleargraph - clears the graph data");
                        } catch (Exception e) {
                            sysMsg("Nope. Try again.. Command usage: !cleargraph");
                        }
                    }
                    );
    }
    
    public void sysMsg(String h)
    {
        List<LogLine> lines = getObjects(LogLine.class);
        for(LogLine ll : lines)
        {
            ll.setLocation(ll.getX(),ll.getY()-LogTail.fontSize);
        }
        addObject(new LogLine(h,LogTail.fontSize,10,Color.PINK), 10, getHeight()-10);
    }
    public LineDataManager getLDM()
    {
        return h;
    }

    private File chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
}
