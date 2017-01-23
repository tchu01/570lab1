import greenfoot.*;  // imports Actor, World, Greenfoot, GreenfootImage

import java.util.Random;
import java.util.List;

/**
 * A wombat world with one leaf, one wombat and a number of walls
 * such that there's always a path between the wombat and the leaf, and 
 * at least 6 squares between them.
 * @author Foaad Khosmood
 * Modified from original by Michael Kolling
 */
public class WombatWorld extends World
{
    private Random randomizer = new Random();
    public Leaf theLeaf = new Leaf();
    
    /**
     * Create a new world with 8x8 cells and
     * with a cell size of 60x60 pixels
     */
    public WombatWorld() 
    {
        super(12, 12, 50);
        
        GreenfootImage bg = new GreenfootImage("cell.jpg");
        bg.scale(50,50);
        setBackground(bg);
        populate();
    }

    /**
     * Populate the world with a fixed scenario of wombats and leaves.
     */    
    public void populate()
    {
        int width = getWidth();
        int height = getHeight();
        int x,y,wallSize,startW,endW;
        
        //draw wombat somwhere in the first 3 rows
        Wombat w1 = new Wombat();
        x = Greenfoot.getRandomNumber(width);
        y = Greenfoot.getRandomNumber(3);
        addObject(w1, x, y);
        
        //draw leaf in bottom three rows and middle 10 columns
        //this guarantees at least 6 squares between leaf and wombat
        
        x = 1 + Greenfoot.getRandomNumber(width-2);
        y = height-3 + Greenfoot.getRandomNumber(3);
        addObject(theLeaf,x,y);
        
        //draw a wall guaranteed to separate a straight line between them
        wallSize =  Math.abs(theLeaf.getX() - w1.getX());
        startW = theLeaf.getX();
        endW = w1.getX();
        if (startW > endW) {
            startW = endW;
            endW = theLeaf.getX();
        }
            
        buildWall(startW,endW,width/2-1,width/2);
        randomRocks(12);
        
        
        
    }
    // builds a wall with a small stochastic variability on the vertical 
    public void buildWall(int startX, int endX, int startY, int endY)
    {
        int index; int vDeviation = 0;
        for(index = startX; index <= endX; index++) {
            Rock rock = new Rock();
            vDeviation = startY + Greenfoot.getRandomNumber(endY+1-startY);
            addObject(rock,index,vDeviation);
        }
            
    }
    
    //Place a number of rocks, making sure they are not too close to anything else.
    public void randomRocks(int howMany)
    {
        int i,x,y,numRocks=0;
        List neighbors;
 
        for(i=0; i<100 && numRocks < howMany; i++) {
            Rock rock = new Rock();
            x = Greenfoot.getRandomNumber(getWidth());
            y = Greenfoot.getRandomNumber(getHeight());
            
            addObject(rock,x,y);
            neighbors = rock.getObjectsInRange(2,null);
            if (!neighbors.isEmpty())
                removeObject(rock);
            else
                numRocks++;
        }
    }
}