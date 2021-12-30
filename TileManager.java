import java.util.*;
import java.io.File;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class TileManager
{
  private int width;
  private int height;
  private Coordinate[][] coordinates;
  private Map<Coordinate, Tile> map;
  private Map<Coordinate, State> states;

  public TileManager(int width, int height)
  {
    this.width = width;
    this.height = height;

    this.coordinates = new Coordinate[width][height];
    this.map = new Hashtable<Coordinate, Tile>();
    this.states = new Hashtable<Coordinate, State>();

    initialize_map();
    map_neighbors();
  }

  public TileManager(int width, int height, String map_image)
  {
    this.width = width;
    this.height = height;

    this.coordinates = new Coordinate[width][height];
    this.map = new Hashtable<Coordinate, Tile>();
    this.states = new Hashtable<Coordinate, State>();

    initialize_map(map_image);
    map_neighbors();
  }
  // Create a randomized canvas
  private void initialize_map()
  {
    Random rand = new Random();
    for (int i = 0; i < width; i++)
    {
      for (int j = 0; j < height; j++)
      {
        Coordinate c = new Coordinate(i, j);
        coordinates[i][j] = c;
        if (rand.nextInt(100) < 60)
        {
          map.put(c, new Tile(Tile.TileType.SEA, c));
        }
        else if (rand.nextInt(100) < 90)
        {
          map.put(c, new Tile(Tile.TileType.GRASSLAND, c));
        }
        else
        {
          map.put(c, new Tile(Tile.TileType.MOUNTAIN, c));
        }
      }
    }
  }

  private void initialize_map(String map_image)
  {
    File f = new File(map_image);
    BufferedImage img;

    try
    {
      img = ImageIO.read(f);
      for (int i = 0; i < width; i++)
      {
        for (int j = 0; j < height; j++)
        {
          Coordinate c = new Coordinate(i, j);
          coordinates[i][j] = c;
          // Retrieving contents of a pixel
          int pixel = img.getRGB(i, j);
          // Creating a Color object from pixel value
          Color color = new Color(pixel, true);
          // Create new tile based off colors
          map.put(c, new Tile(color, c));
          // System.out.println(c.toString() + " " + color.getRed() + " " + color.getGreen() + " " + color.getBlue());
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  //   0  1  2  3...n
  // 0    N
  // 1 W  x  E
  // 2    S
  // 3
  // .
  // n
  // Going North -> -1 height
  // Going South -> +1 height
  // Going West  -> -1 width
  // Going East  -> +1 width
  private void map_neighbors()
  {
    for (int i = 0; i < width; i++)
    {
      for (int j = 0; j < height; j++)
      {
        Tile tile = map.get(coordinates[i][j]);
        // North
        if (j - 1 >= 0)
        {
          tile.setNeighbor(Tile.Direction.N, map.get(coordinates[i][j - 1]));
        }
        // South
        if (j + 1 < height)
        {
          tile.setNeighbor(Tile.Direction.S, map.get(coordinates[i][j + 1]));
        }
        // East
        if (i + 1 < width)
        {
          tile.setNeighbor(Tile.Direction.E, map.get(coordinates[i + 1][j]));
        }
        // West
        if (i - 1 >= 0)
        {
          tile.setNeighbor(Tile.Direction.W, map.get(coordinates[i - 1][j]));
        }
      }
    }
  }

  public Coordinate[][] getMap()
  {
    return coordinates;
  }

  public Coordinate getCoordinate(int x, int y)
  {
    if ((x > -1) && (x < width) &&
        (y > -1) && (y < height))
    {
      return coordinates[x][y];
    }
    return null;
  }

  private Boolean verifyCoordRange(Coordinate coordinate)
  {
    if ((coordinate.x > -1) && (coordinate.x < width) &&
        (coordinate.y > -1) && (coordinate.y < height))
    {
      return true;
    }
    return false;
  }

  public Tile getTile(Coordinate coord)
  {
    if (verifyCoordRange(coord))
    {
      return map.get(coordinates[coord.x][coord.y]);
    }
    return null;
  }
  // Verify whether the given tile exists or not
  public Boolean verifyTile(Tile t)
  {
    if (getTile(t.getCoordinate()) == null)
    {
      return false;
    }
    return true;
  }
  public void printStateCoordinates(State s)
  {
    for (Map.Entry<Coordinate, State> entry : states.entrySet())
    {
      if (entry.getValue() == s)
      {
        System.out.println(entry.getKey());
      }
    }
  }
  public void addState(State s, Coordinate c)
  {
    Random rand = new Random();
    // Assume 'c' is null here as well
    if (s == null)
    {
      s = new State(rand.nextInt(), this);
      c = new Coordinate(rand.nextInt(width), rand.nextInt(height));
      while (getState(c) != null)
      {
        c.x = rand.nextInt(width);
        c.y = rand.nextInt(height);
      }
      states.put(coordinates[c.x][c.y], s);
    }
    else if (c != null)
    {
      if (getState(c) == null)
      {
        states.put(coordinates[c.x][c.y], s);
      }
      else
      {
        System.out.println("WARNING : Cannot add state. Invalid coordinate provided");
      }
    }
    else
    {
      System.out.println("ERROR : Cannot add state. Invalid arguments provided");
    }
  }
  // Return the state who owns the given coordinate
  public State getState(Coordinate coord)
  {
    if (verifyCoordRange(coord))
    {
      return states.get(coordinates[coord.x][coord.y]);
    }
    return null;
  }
  // Add a given tile to State 's' by grabbing the coordinate
  public Boolean consumeTile(State s, Coordinate c)
  {
    // Make sure the coordinate is valid
    if (verifyCoordRange(c))
    {
      Coordinate coord = coordinates[c.x][c.y];
      if (!states.containsKey(coord))
      {
        System.out.println("INFO : Coordinate " + coord.toString() + " is being consumed by " + s.toString());
      }
      states.put(coord, s);
      return true;
    }
    return false;
  }

  private Boolean processMoveOrder(Order o)
  {
    Order.Item item = o.getItem();
    // Source and destination tiles of importance
    Tile origin_t = getTile(o.getOrderOrigin());
    Tile dest_t = getTile(o.getOrderTarget());
    switch (item)
    {
      case RESOURCE:
        Tile.Resource moved_r = o.getResource();
        int quantity = o.getQuantity();
        int extracted_resources = origin_t.extractResource(moved_r, quantity);
        dest_t.addResource(moved_r, extracted_resources);
        return true;
      case POPULATION:
        // Population profile of what we want to extract
        Population moved_p = o.getPopulation();
        if (moved_p != null)
        {
          // Extract population from origin tile
          Population extracted_population = origin_t.getPopulation().splitPopulation(moved_p.getCreatureList());
          if (extracted_population != null)
          {
            // Absorb the extracted population into the destination tile
            dest_t.getPopulation().absorbPopulation(extracted_population);
            return true;
          }
          else
          {
            System.out.println("ERROR : Failed to extract population! Cannot move...");
          }
        }
        else
        {
          System.out.println("ERROR : Invalid population order! Cannot move...");
        }
        break;
      // Moving GP is the equivalent of investing in a particular Tile
      case GP:
        break;
      default:
        break;
    }
    return false;
  }
  // Process a given Order starting with its action
  private Boolean processOrderByAction(Order o)
  {
    Order.Actions action = o.getAction();
    Order.Item item = o.getItem();
    switch (action)
    {
      case MOVE:
        // Verify the origin & target tiles are owned by the commander of the order
        if ( (getState(o.getOrderOrigin()) == o.getOwner()) &&
             (getState(o.getOrderTarget()) == o.getOwner()) )
        {
          if (o.getTarget() != Order.Target.STATE)
          {
            return processMoveOrder(o);
          }
          else if (o.getItem() == Order.Item.GP)
          {
            // processGPAssistance(o);
          }
        }
        else
        {
          System.out.println("WARNING : Cannot move to " + o.getOrderTarget().toString() + ". Not owned");
        }
        break;
      case ATTACK:
        // if (item == Order.Item.POPULATION)
        // {
        //   processAttackOrder(o);
        // }
        break;
      default:
        break;
    }
    return false;
  }
  // Process orders filtered by priority
  private LinkedList<Order> processOrderByPriority(int priority, LinkedList<Order> input_cmds)
  {
    LinkedList<Order> output_cmds = new LinkedList<Order>();
    Boolean process_order_status = false;
    Order order;
    while (input_cmds.peekFirst() != null)
    {
      order = input_cmds.poll();
      // If the current order is not the correct priority, add to output and continue
      if (order.getPriority() != priority)
      {
        output_cmds.addLast(order);
        continue;
      }
      process_order_status = processOrderByAction(order);
    }
    return output_cmds;
  }

  // public Stack shortestPath(Coordinate start, Coordinate end)
  // {
  //   Stack open = new Stack();
  //   open.push(start);
  //   Stack close = new Stack();
  //   close.push(start);

  //   // Tiles : 0,0  0,1  0,2  0,3  0,4
  //   //         1,0  1,1  1,2  1,3  1,4
  //   //         2,0  2,1  2,2  2,3  2,4
  //   //         3,0  3,1  3,2  3,3  3,4
  //   //         4,0  4,1  4,2  4,3  4,4
  //   // Time  :  1    2    2    1    1
  //   //          2    2    2    1    1
  //   //          2    3    5    1    1
  //   //          2    2    2    1    1
  //   //          2    3    2    1    2
  //   // f     :  5    8    6    4    5
  //   //          8    6    4    3    4
  //   //          6    6    5    2    3
  //   //          4    2    X    1    2
  //   //          6    6    2    2    6

  //   // Grab starting node
  //   // Generate possible routes
  //   // Grab node with least expensive route
  //   //  Distance away * time to get there on current node
  // }

  // public void generateNeighbors(Stack s, Coordinate c)
  // {
  //   Coordinate neighbor = new Coordinate(c.x + 1, c.y);
  //   if (verifyCoordRange(neighbor))
  //   {
  //     s.push(neighbor);
  //   }
  // }

  public void update()
  {
    System.out.println("-------------------------------");
    // 1 - Receive all Orders from States
    LinkedList<Order> super_orders = new LinkedList<Order>();
    for (Map.Entry<Coordinate, State> entry : states.entrySet())
    {
      LinkedList<Order> Orders = new LinkedList<Order>();
      Orders = entry.getValue().transmitOrders();
      while (!(Orders.size() == 0))
      {
        super_orders.addLast(Orders.pop());
      }
    }
    // 2 - Process commands in priority order. 1, 2, etc.
    for (int i = 1; i <= 5; i++)
    {
      super_orders = processOrderByPriority(i, super_orders);
    }
    Random rand = new Random();
    if (rand.nextInt(10) > 5)
    {
      System.out.println("Adding state");
      addState(null, null);
    }
    // 3 - Return un-processed orders to their respective origins

    // 4 - Gather resources

    // 5 - Modify population based on resources

    // 6 - Stream important events to respective states
  }
  // Just test stuff
  public static void main(String[] args)
  {
    int passes = 0;
    TileManager manager = new TileManager(10, 10);
    Coordinate center = new Coordinate(5, 5);
    Coordinate neighbor_E = new Coordinate(5, 6);
    Tile tile = manager.getTile(center);
    Population p = tile.getPopulation();
    // Setup population stuffs
    p.pushCreature(new Creature(Creature.Race.HUMAN, Creature.Occupation.PEASANT), 1000);

    tile.printTile();
    // tile.printNeighbors();
    manager.getTile(neighbor_E).printTile();
    // manager.getTile(neighbor_E).printNeighbors();

    State s = new State(1, manager);
    manager.consumeTile(s, center);
    manager.printStateCoordinates(s);
    Order move_100_wheat = new Order(s, 1, center, neighbor_E, Tile.Resource.WHEAT, 100);
    move_100_wheat.getOrderOrigin().print();
    move_100_wheat.getOrderTarget().print();
    s.addOrder(move_100_wheat);

    manager.update();
    // manager.getTile(center).printTile();
    // manager.getTile(neighbor_E).printTile();

    manager.consumeTile(s, neighbor_E);
    manager.printStateCoordinates(s);
    s.addOrder(move_100_wheat);
    manager.update();
    manager.getTile(center).printTile();
    manager.getTile(neighbor_E).printTile();

    // Test MOVE POPULATION case
    Tile east_tile = manager.getTile(neighbor_E);
    Population p2 = east_tile.getPopulation();
    Population move_100_humans = new Population();
    move_100_humans.pushCreature(new Creature(Creature.Race.HUMAN, Creature.Occupation.PEASANT), 250);
    Order move_100_human_peasants = new Order(s, 1, center, neighbor_E, move_100_humans);
    s.addOrder(move_100_human_peasants);
    manager.update();
    tile.printTile();
    east_tile.printTile();
    // p2.pushCreature()

    System.out.println("TESTS PASSED : [" + passes + "/7]");
  }
}
