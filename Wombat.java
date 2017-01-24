import greenfoot.*;  // imports Actor, World, Greenfoot, GreenfootImage

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Wombat. A Wombat moves forward until it can't do so anymore, at
 * which point it turns left. This wombat can not move over rocks. 
 * If a wombat finds a leaf, it eats it.
 * 
 * @author Michael Kolling
 * @version 1.0.1
 */
public class Wombat extends Actor
{
    private static final int EAST = 0;
    private static final int WEST = 1;
    private static final int NORTH = 2;
    private static final int SOUTH = 3;
    private static final int VERTICALHORIZONTAL = 10;
    private static final int DIAGONAL = 14;
    
    private HashSet<String> open = new HashSet<>();
    private HashSet<String> close = new HashSet<>();
    private HashMap<String, String> parent = new HashMap<>();
    private HashMap<String, Integer> gValue = new HashMap<>();
    private ArrayList<String> path = new ArrayList<>();
    private int ndx = -1;
    
    private int direction;
    private int leavesEaten;
    private boolean doSearch = true;
    private boolean noPath = false;
    
    private GreenfootImage wombatRight;
    private GreenfootImage wombatLeft;
    
    public Wombat()
    {
        wombatRight  = getImage();
        wombatLeft = new GreenfootImage(getImage());
        wombatLeft.mirrorHorizontally();
        
        setDirection(EAST);
        leavesEaten = 0;
    }
    
    /**
     * Do whatever the wombat likes to to just now.
     */
    public void act()
    {
        if(leavesEaten == 1) {
            return;
        }
        
        if(doSearch) {
            doSearch = false;
            // System.out.println("Running A*");
            astarSearch();
        }
        
        astarFollow();
        
        /*
        if(foundLeaf()) {
            eatLeaf();
        }
        else if(canMove()) {
            move();
        }
        else {
            turnRandom();
        }
        */
    }
    
    public String createTuple(int x, int y) {
        return x + "," + y;
    }
    
    public int unpackX(String tuple) {
        String[] split = tuple.split(",");
        return Integer.parseInt(split[0]);
    }
    
    public int unpackY(String tuple) {
        String[] split = tuple.split(",");
        return Integer.parseInt(split[1]);
    }
    
    public void astarSearch() {
        int x = getX();
        int y = getY();
        String start = createTuple(x,y);
        int g = 0;
        open.add(start);
        gValue.put(start, g);
        parent.put(start, start);
        
        int targetX = ((WombatWorld) getWorld()).theLeaf.getX();
        int targetY = ((WombatWorld) getWorld()).theLeaf.getY();
        String goal = createTuple(targetX, targetY);
        
        while(!open.isEmpty()) {
            String current = nextSquare();
            if(current.equals(goal)) {
                break;
            }
            
            // drop current square from open and add to close 
            open.remove(current);
            close.add(current);
            
            // add reachable squares to open
            addSquares(current);
        }
        
        if(open.isEmpty()) {
            noPath = true;
        } else {
            String current = goal;
            while(!current.equals(start)) {
                path.add(current);
                ndx++;
                current = parent.get(current);
            }
        }
    }
    
    public String nextSquare() {
        String ret = "";
        int minF = Integer.MAX_VALUE;
        for(String tup : open) {
            int x = unpackX(tup);
            int y = unpackY(tup);
            
            int g = gValue.get(tup);
            int h = heuristic(x, y);
            int f = g + h;
            
            if(f < minF) {
                minF = f;
                ret = tup;
            }
            
            // System.out.println("Considering " + tup + ", g = " + g + " | h = " + h + " | f = " + f);
        }
        
        // System.out.println("Returning: " + ret + "\n");
        return ret;
    }
    
    public int heuristic(int x, int y) {
        int targetX = ((WombatWorld) getWorld()).theLeaf.getX();
        int targetY = ((WombatWorld) getWorld()).theLeaf.getY();
        
        int deltaX = Math.abs(x - targetX);
        int deltaY = Math.abs(y - targetY);
        
        if(deltaX < deltaY) {
            return (deltaX * DIAGONAL) + ((deltaY - deltaX) * VERTICALHORIZONTAL); 
        } else if (deltaX > deltaY) {
            return (deltaY * DIAGONAL) + ((deltaX - deltaY) * VERTICALHORIZONTAL);
        } else {
            return deltaX * DIAGONAL;
        }

    }
    
    public void addSquares(String current) {
        int x = unpackX(current);
        int y = unpackY(current);
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                if((x != x + i || y != y + j) && isOpenSquare(x + i, y + j)) {
                    String tup = createTuple(x + i,y + j);
                    
                    if(open.contains(tup)) {
                        if(Math.abs(i) == 1 && Math.abs(j) == 1) {
                            if(gValue.get(current) + DIAGONAL < gValue.get(tup)) {
                                gValue.put(tup, gValue.get(current) + DIAGONAL);
                                parent.put(tup, current);
                                // System.out.println("Replacing G through diagonal");
                            }
                        } else {
                            if(gValue.get(current) + VERTICALHORIZONTAL < gValue.get(tup)) {
                                gValue.put(tup, gValue.get(current) + VERTICALHORIZONTAL);
                                parent.put(tup, current);
                                // System.out.println("Replacing G through vertical/horizontal");
                            }
                        }
                    } else {
                        open.add(tup);
                        if(Math.abs(i) == 1 && Math.abs(j) == 1) {
                            gValue.put(tup, gValue.get(current) + DIAGONAL);
                        } else {
                            gValue.put(tup, gValue.get(current) + VERTICALHORIZONTAL);
                        }
                        parent.put(tup, current);

                        // System.out.println("Adding " + tup + " to open, setting parent to " + current + 
                        //    ", gValue to " + gValue.get(tup));
                    }
                }
            }
        }
    }
    
    public boolean isOpenSquare(int x, int y) {
        World myWorld = getWorld();
        if (x >= myWorld.getWidth() || y >= myWorld.getHeight()) {
            return false;
        }
        else if (x < 0 || y < 0) {
            return false;
        }
        
        String tup = createTuple(x,y);
        if(close.contains(tup)) {
            return false;
        }
        
        List rocks = myWorld.getObjectsAt(x, y, Rock.class);
        if(rocks.isEmpty()) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public void astarFollow() {
        if(!noPath) {
            if(ndx >= 0) {
                String tup = path.get(ndx--);
                int x = unpackX(tup);
                int y = unpackY(tup);
                setLocation(x, y);
                // System.out.println("Step " + ndx);
            }
            
            if(ndx == -1) {
                eatLeaf();
            }
        } else {
            // System.out.println("No path :(");
        }
    }
    
    /**
     * Check whether there is a leaf in the same cell as we are.
     */
    public boolean foundLeaf()
    {
        Actor leaf = getOneObjectAtOffset(0, 0, Leaf.class);
        if(leaf != null) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Eat a leaf.
     */
    public void eatLeaf()
    {
        Actor leaf = getOneObjectAtOffset(0, 0, Leaf.class);
        if(leaf != null) {
            // eat the leaf...
            getWorld().removeObject(leaf);
            leavesEaten = leavesEaten + 1; 
        }
    }
    
    /**
     * Move one cell forward in the current direction.
     */
    public void move()
    {
        int targetX = ((WombatWorld) getWorld()).theLeaf.getX();
        int targetY = ((WombatWorld) getWorld()).theLeaf.getY();

        if (!canMove()) {
            return;
        }
        switch(direction) {
            case SOUTH :
                setLocation(getX(), getY() + 1);
                break;
            case EAST :
                setLocation(getX() + 1, getY());
                break;
            case NORTH :
                setLocation(getX(), getY() - 1);
                break;
            case WEST :
                setLocation(getX() - 1, getY());
                break;
        }
    }

    /**
     * Test if we can move forward. Return true if we can, false otherwise.
     */
    public boolean canMove()
    {
        World myWorld = getWorld();
        int x = getX();
        int y = getY();
        switch(direction) {
            case SOUTH :
                y++;
                break;
            case EAST :
                x++;
                break;
            case NORTH :
                y--;
                break;
            case WEST :
                x--;
                break;
        }
        // test for outside border
        if (x >= myWorld.getWidth() || y >= myWorld.getHeight()) {
            return false;
        }
        else if (x < 0 || y < 0) {
            return false;
        }
        List rocks = myWorld.getObjectsAt(x, y, Rock.class);
        if(rocks.isEmpty()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Turn in a random direction.
     */
    public void turnRandom()
    {
        // get a random number between 0 and 3...
        int turns = Greenfoot.getRandomNumber(4);
        
        // ...an turn left that many times.
        for(int i=0; i<turns; i++) {
            turnLeft();
        }
    }

    /**
     * Turns towards the left.
     */
    public void turnLeft()
    {
        switch(direction) {
            case SOUTH :
                setDirection(EAST);
                break;
            case EAST :
                setDirection(NORTH);
                break;
            case NORTH :
                setDirection(WEST);
                break;
            case WEST :
                setDirection(SOUTH);
                break;
        }
    }

    /**
     * Sets the direction we're facing.
     */
    public void setDirection(int direction)
    {
        this.direction = direction;
        switch(direction) {
            case SOUTH :
                setImage(wombatRight);
                setRotation(90);
                break;
            case EAST :
                setImage(wombatRight);
                setRotation(0);
                break;
            case NORTH :
                setImage(wombatLeft);
                setRotation(90);
                break;
            case WEST :
                setImage(wombatLeft);
                setRotation(0);
                break;
            default :
                break;
        }
    }

    /**
     * Tell how many leaves we have eaten.
     */
    public int getLeavesEaten()
    {
        return leavesEaten;
    }
}