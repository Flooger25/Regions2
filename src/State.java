import java.util.*;
import java.awt.Color;

// State types
enum StateType
{
  // Emperor, King, Duke, Count, Baron, Others..
  EMPIRE, KINGDOM, DUCHY, COUNTY, FIEFDOM, CITYSTATE
}
// Relationships
enum Relationship
{
  ALLIANCE, FRIENDLY, NEUTRAL, RIVALRY, WAR
}

public class State
{
  public static TileManager manager;
  private long uid;
  private String name;

  private StateType type;

  private LinkedList<Order> orders;
  private LinkedList<State> sub_states;
  private Map<State, Relationship> diplomacy;
  private Color color;
  private long balance;

  private State parent;
  private Policy policy;

  public State(long uid, TileManager manager, StateType type)
  {
    this.manager = manager;
    this.uid = uid;
    this.name = "";
    this.type = type;
    initialization();
  }

  public State(long uid, TileManager manager, StateType type, String name)
  {
    this.manager = manager;
    this.uid = uid;
    this.name = name;
    this.type = type;
    initialization();
  }

  private void initialization()
  {
    this.parent = null;
    Random rand = new Random();
    this.color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    this.orders = new LinkedList<Order>();
    this.sub_states = new LinkedList<State>();
    this.diplomacy = new Hashtable<State, Relationship>();
    this.balance = 0;
    this.policy = new Policy();
  }

  public String toString(int layer)
  {
    // Basic information on high level state
    String info = uid + " : " + name + " : " + type.name() + "\n";
    // Add info on sub-states
    for (int i = 0; i < sub_states.size(); i++)
    {
      for (int l = 0; l < layer; l++)
      {
        info += " ";
      }
      info += sub_states.get(i).toString(layer + 1);
    }
    return info;
  }

  public void printState()
  {
    System.out.println(toString(1));
  }

  public Policy getPolicy()
  {
    return policy;
  }

  public LinkedList<State> getSubStateList()
  {
    return sub_states;
  }

  public State getParent()
  {
    return parent;
  }

  public Boolean hasParent()
  {
    if (getParent() != null)
    {
      return true;
    }
    return false;
  }

  public Boolean setParent(State p)
  {
    // if (p == null)
    // {
    //   return false;
    // }
    // // If we already have a parent, tell it to remove
    // //  us as a sub state.
    // if (parent != null)
    // {
    //   parent.removeSubState(this);
    // }
    // Can only set a parent if we currently do not have one
    if (parent == null)
    {
      parent = p;
      return true;
    }
    return false;
  }

  // Return the parent belonging to the given state directly
  public State getDirectParent()
  {
    return parent;
  }

  // Return the parent at the highest level. Simply climb
  // the parent latter until we get to the top
  public State getHighestParent()
  {
    if (parent == null)
    {
      return null;
    }
    State top_parent = parent;
    while (top_parent.getDirectParent() != null)
    {
      top_parent = top_parent.getDirectParent();
    }
    return top_parent;
  }

  // Recursive function to see if a sub-state is nested in this instance
  public Boolean containsSubState(State s)
  {
    if (s == null)
    {
      return false;
    }
    return containsSubStateHelper(s, this);
  }

  private Boolean containsSubStateHelper(State query, State current)
  {
    if (query == null)
    {
      return false;
    }
    if (query == current)
    {
      return true;
    }
    LinkedList<State> subs = current.getSubStateList();
    for (int i = 0; i < subs.size(); i++)
    {
      if (containsSubStateHelper(query, subs.get(i)))
      {
        return true;
      }
    }
    return false;
  }

  public Boolean addSubState(State s)
  {
    if (s == null)
    {
      return false;
    }
    // Have to not have a parent + not be embedded
    if (s.hasParent() || containsSubState(s))
    {
      return false;
    }
    sub_states.add(s);
    s.setParent(this);
    return true;
  }

  // Absorb another state, regardless if they have a parent
  public Boolean absorbState(State s)
  {
    if (s == null)
    {
      return false;
    }
    // Make sure we don't already have it somewhere
    if (containsSubState(s))
    {
      return false;
    }
    // If 's' already has a parent, tell its parent to remove
    //  's' as a sub state.
    if (s.hasParent())
    {
      s.getParent().removeSubState(s);
    }
    // Then just add the sub state normally
    sub_states.add(s);
    s.setParent(this);
    return true;
  }

  // NOTE : Only removes sub state at current level. To
  //  remove an embedded sub state, one has to find the actual
  //  owner.
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
