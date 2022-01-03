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
  public static TileManager manager;
  private LinkedList<Order> orders;
  private LinkedList<State> sub_states;
  private Map<State, Relationship> diplomacy;
  private Color color;
  private long balance;

  public State(long uid, TileManager manager)
  {
    this.uid = uid;
    Random rand = new Random();
    this.color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    this.manager = manager;
    this.orders = new LinkedList<Order>();
    this.sub_states = new LinkedList<State>();
    this.diplomacy = new Hashtable<State, Relationship>();
    this.balance = 0;
  }

  public Boolean addSubState(State s)
  {
    if (!sub_states.contains(s))
    {
      return sub_states.add(s);
    }
    return false;
  }

  public Boolean removeSubState(State s)
  {
    if (sub_states.contains(s))
    {
      return sub_states.remove(s);
    }
    return false;
  }

  public void modifyRelationship(State s, Relationship r)
  {
    if (diplomacy.get(s) != null)
    {
      diplomacy.put(s, r);
    }
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
