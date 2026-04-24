import greenfoot.*;

public class LogLine extends Actor {

    private String text;
    private GreenfootImage img;
    private static int leftCol = 0;
    
    public LogLine(String text, int fontSize, int x) {
        this.text = text;
        img = new GreenfootImage(text, fontSize, Color.WHITE, new Color(0,0,0,0));
        setImage(img);
    }
    
    public LogLine(String text, int fontSize, int x, Color c) {
        this.text = text;
        img = new GreenfootImage(text, fontSize, c, new Color(0,0,0,0));
        setImage(img);
    }
    
    public void addedToWorld(World w)
    {
        this.setLocation(getLeftAlignedX(getX()),getY());
    }

    public void act() {
        //setLocation(getX(), getY() - 4); // Scroll up

        if (getY() < 20) {
            getWorld().removeObject(this);
        }
    }
    
    public int getLeftAlignedX(int targetLeftX) {
        return targetLeftX + getImage().getWidth() / 2;
    }
}