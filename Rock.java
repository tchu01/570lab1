import greenfoot.*;  // imports Actor, World, Greenfoot, GreenfootImage
import java.util.List;
/**
 * Rock - a class for representing rocks.
 *
 * @author Michael Kolling
 * @version 1.0.1
 */
public class Rock extends Actor
{
    public Rock()
    {
    }
    
    public List getObjectsInRange(int distance, Class cls)
    {
        return super.getObjectsInRange(distance, cls);
    }
}