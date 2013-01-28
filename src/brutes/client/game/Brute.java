package brutes.client.game;

import brutes.client.game.media.DataImage;
import java.util.ArrayList;

/**
 *
 * @author Karl
 */
public class Brute {

    public static final int MAX_BONUSES = 3;
    
    private int id;
    private String name;
    private short level;
    private short life;
    private short strength;
    private short speed;
    private DataImage image;
    private Bonus[] bonuses;

    public Brute(int id, String name, short level, short life, short strength, short speed, DataImage image) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.life = life;
        this.strength = strength;
        this.speed = speed;
        this.image = image;
        this.bonuses = new Bonus[Brute.MAX_BONUSES];
        for (int i = 0; i < this.bonuses.length; i++) {
            this.bonuses[i] = Bonus.EMPTY_BONUS;
        }
    }

    public Brute(int id, String name, short level, short life, short strength, short speed, DataImage image, Bonus[] bonuses) {
        this(id, name, level, life, strength, speed, image);
        System.arraycopy(bonuses, 0, this.bonuses, 0, (bonuses.length < Brute.MAX_BONUSES) ? bonuses.length : Brute.MAX_BONUSES);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public short getLevel() {
        return this.level;
    }

    public short getLife() {
        return this.life;
    }
    
    public short getStrength() {
        return this.strength;
    }

    public short getBonusStrength() {
        short sum = 0;
        for (int i = 0; i < this.bonuses.length; i++) {
            if (!this.bonuses[i].equals(Bonus.EMPTY_BONUS)) {
                sum += this.bonuses[i].getStrength();
            }
        }
        return sum;
    }

    public short getSpeed() {
        return this.speed;
    }

    public short getBonusSpeed() {
        short sum = 0;
        for (int i = 0; i < bonuses.length; i++) {
            if (!this.bonuses[i].equals(Bonus.EMPTY_BONUS)) {
                sum += bonuses[i].getSpeed();
            }
        }
        return sum;
    }

    public DataImage getImage() {
        return this.image;
    }

    public Bonus[] getBonuses() {
        return this.bonuses;
    }
    
    public int[] getBonusesIDs() {
        ArrayList<Integer> bonusesIds = new ArrayList<>(Brute.MAX_BONUSES);
        for(int i = 0; i < Brute.MAX_BONUSES; i++){
            if(this.bonuses[i] != Bonus.EMPTY_BONUS){
                bonusesIds.add(new Integer(this.bonuses[i].getId()));
            }
        }
        int[] intIDs = new int[bonusesIds.size()];
        for(int i = 0; i < intIDs.length; i++){
            intIDs[i] = bonusesIds.get(i).intValue();
        }
        return intIDs;
    }
}
