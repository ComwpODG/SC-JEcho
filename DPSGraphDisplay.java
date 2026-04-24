import greenfoot.*;

public class DPSGraphDisplay extends Actor {

    private static GreenfootImage currentGraph = new GreenfootImage(1000, 800);

    public DPSGraphDisplay() {
        setImage(currentGraph);
    }

    public void act() {
        setImage(currentGraph);
    }

    public static void setGraphImage(GreenfootImage img) {
        currentGraph = img;
    }
}