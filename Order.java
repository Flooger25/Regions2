import java.util.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Order
{
  // Order types
  public enum Actions
  {
    MOVE, ATTACK, MOD
  }
  public enum Item
  {
    RESOURCE, POPULATION, GP
  }
  public enum Target
  {
    TILE, STATE
  }
  // Param 1 - Origin
  // Definition: What is commanding the action
  // Examples: State

  // Param 2 - Action
  // Definition: What is the action to be deployed
  // Examples: Move, Attack

  // Param 3 - Item
  // Definition: Item needed for said action
  // Examples: Resources, Population, GP

  // Param 4 - Target
  // Definition: Upon what is the action going to be done
  // Examples: Tile, State

  // VALID EXAMPLES:
  // Case 1 : State -> Move -> Population -> Tile
  // Case 2 : State -> Move -> Resources -> Tile
  // Case 3 : State -> Move -> GP -> Tile
  // Case 4 : State -> Move -> GP -> State
  // Case 5 : State -> Attack -> Population -> Tile
  // Case 6 : State -> Mod -> Creature -> Tile

  // 1 being highest, and 5 being lowest
  // A level of 5 also indicates passive orders that
  //  happen continuously
  private int priority;
  // Enums
  private Actions action;
  private Item item;
  private Target target;
  // Objects to handle cases
  private State originState;
  private State targetState;
  private Coordinate originTile;
  private Coordinate targetTile;
  private Population population;
  private Resource resource;
  private Occupation occupation;
  private Occupation filter;
  private int resource_quantity;

  // Case 1 : MOVE -> POPULATION -> TILE
  public Order(State commander, int priority, Coordinate origin, Coordinate dest, Population p)
  {
    initPriority(priority);
    // Source and dest content
    this.originState = commander;
    this.originTile = origin;
    this.targetTile = dest;
    // Item being used
    this.population = p;
    // Setting types
    this.action = Actions.MOVE;
    this.item = Item.POPULATION;
    this.target = Target.TILE;
  }
  // Case 2 : MOVE -> RESOURCE -> TILE
  public Order(State commander, int priority, Coordinate origin, Coordinate dest, Resource r, int quant)
  {
    initPriority(priority);
    // Source and dest content
    this.originState = commander;
    this.originTile = origin;
    this.targetTile = dest;
    // Item being used
    this.resource = r;
    this.resource_quantity = quant;
    // Setting types
    this.action = Actions.MOVE;
    this.item = Item.RESOURCE;
    this.target = Target.TILE;
  }
  // TODO - Cases 3-5

  // Case 6 : State -> Mod -> Creature -> Tile
  public Order(State commander, int priority, Coordinate origin, Occupation o, int quant)
  {
    initPriority(priority);
    // Source and dest content
    this.originState = commander;
    this.originTile = origin;
    this.targetTile = null;
    // Item being used
    this.occupation = o;
    this.filter = null;
    this.resource_quantity = quant;
    // Setting types
    this.action = Actions.MOD;
    this.item = Item.POPULATION;
    this.target = Target.TILE;
  }

  // Case 6.5 : State -> Mod -> Creature -> Tile
  public Order(State commander, int priority, Coordinate origin, Occupation old_o, Occupation new_o, int quant)
  {
    initPriority(priority);
    // Source and dest content
    this.originState = commander;
    this.originTile = origin;
    this.targetTile = null;
    // Item being used
    this.occupation = new_o;
    this.filter = old_o;
    this.resource_quantity = quant;
    // Setting types
    this.action = Actions.MOD;
    this.item = Item.POPULATION;
    this.target = Target.TILE;
  }

  private void initPriority(int priority)
  {
    if (priority > 5) this.priority = 5;
    else if (priority < 1) this.priority = 1;
    else this.priority = priority;
  }

  public int getPriority()
  {
    return priority;
  }

  public State getOwner()
  {
    return originState;
  }

  public Coordinate getOrderOrigin()
  {
    return originTile;
  }

  public Coordinate getOrderTarget()
  {
    return targetTile;
  }

  public Population getPopulation()
  {
    return population;
  }

  public Occupation getOccupation()
  {
    return occupation;
  }

  public Occupation getOldOcc()
  {
    return filter;
  }

  public Resource getResource()
  {
    return resource;
  }

  public int getQuantity()
  {
    return resource_quantity;
  }

  public Actions getAction()
  {
    return action;
  }

  public Item getItem()
  {
    return item;
  }

  public Target getTarget()
  {
    return target;
  }
  // Just test stuff
  public static void main(String[] args)
  {
    int passes = 0;
    System.out.println("TESTS PASSED : [" + passes + "/7]");
  }
}
