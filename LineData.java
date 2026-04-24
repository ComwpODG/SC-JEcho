/**
 * Write a description of class LineData here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class LineData  
{
    public long timestampMillis;
    public String attacker;
    public String defender;
    public double damage;
    public String weapon;
    public String element;

    public LineData(long timestampMillis, String attacker, String defender, double damage, String weapon, String element) {
        this.timestampMillis = timestampMillis;
        this.attacker = attacker;
        this.defender = defender;
        this.damage = damage;
        this.weapon = weapon;
        this.element = element;
    }

    @Override
    public String toString() {
        return attacker + " -> " + defender + ": " + damage + " " + element + " via " + weapon;
    }
}
