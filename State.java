import java.util.*;
import java.awt.Color;

public class State
{
  // State types
  public enum StateType
  {
    EMPIRE, KINGDOM, CITYSTATE, REGION
  }
  // Relationships
  public enum Relationship
  {
    ALLIANCE, NEUTRAL, WAR
  }
  public static long uid;
  private Color color;
  public static TileManager manager;
  private LinkedList<Order> orders;
  private Map<State, Relationship> diplomacy;

  public State(long uid, TileManager manager)
  {
    this.uid = uid;
    Random rand = new Random();
    this.color = new Color(rand.nextInt(256),rand.nextInt(256),rand.nextInt(256));
    this.manager = manager;
    this.orders = new LinkedList<Order>();
    this.diplomacy = new Hashtable<State, Relationship>();
  }

  public void addOrder(Order o)
  {
    orders.addLast(o);
  }

  public Color getColor()
  {
    return color;
  }

  public LinkedList<Order> transmitOrders()
  {
    return orders;
  }

  public void update()
  {
    // Receive events notifications
    // Gather resources
    // Modify population based on resource
    // 
  }
}
