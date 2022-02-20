import java.util.*;
import java.io.File;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class TileManager
{
  public static final Double hypotenuse = 1.4142135623730951;
  private int width;
  private int height;
  // Integers => Coordinates
  private Coordinate[][] coordinates;
  private Map<Coordinate, Tile> map;
  private Map<Coordinate, State> states;
  // Coordinates => Paths on Coordinate
  private Map<Coordinate, ArrayList<Path>> paths;

  public TileManager(int width, int height)
  {
    this.width = width;
    this.height = height;

    this.coordinates = new Coordinate[width][height];
    this.map = new Hashtable<Coordinate, Tile>();
    this.states = new Hashtable<Coordinate, State>();
    this.paths = new Hashtable<Coordinate, ArrayList<Path>>();

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
    this.paths = new Hashtable<Coordinate, ArrayList<Path>>();

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
        paths.put(c, new ArrayList<Path>());
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
          paths.put(c, new ArrayList<Path>());
          // Retrieving contents of a pixel
          int pixel = img.getRGB(i, j);
          // Creating a Color object from pixel value
          Color color = new Color(pixel, true);
          // Create new tile based off colors
          map.put(c, new Tile(color, c));
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
          tile.setNeighbor(Direction.N, map.get(coordinates[i][j - 1]));
        }
        // South
        if (j + 1 < height)
        {
          tile.setNeighbor(Direction.S, map.get(coordinates[i][j + 1]));
        }
        // East
        if (i + 1 < width)
        {
          tile.setNeighbor(Direction.E, map.get(coordinates[i + 1][j]));
        }
        // West
        if (i - 1 >= 0)
        {
          tile.setNeighbor(Direction.W, map.get(coordinates[i - 1][j]));
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

  private Boolean verifyCoordRange(int x, int y)
  {
    if ((x > -1) && (x < width) &&
        (y > -1) && (y < height))
    {
      return true;
    }
    return false;
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

  public Tile getTile(int x, int y)
  {
    if (verifyCoordRange(x, y))
    {
      return map.get(coordinates[x][y]);
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

  public void printTiles()
  {
    for (Map.Entry<Coordinate, Tile> entry : map.entrySet())
    {
      entry.getValue().printTile();
    }
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
        Resource moved_r = o.getResource();
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
            int emigrated = extracted_population.getPopulation();
            origin_t.migrate(emigrated);
            // Absorb the extracted population into the destination tile
            dest_t.getPopulation().absorbPopulation(extracted_population);
            dest_t.migrate(emigrated);
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

  private Boolean processModOrder(Order o)
  {
    Order.Item item = o.getItem();
    // Get tile we're modifying
    Tile origin_t = getTile(o.getOrderOrigin());
    switch (item)
    {
      case POPULATION:
        // Get Occupation we're trying to generate
        Occupation new_o = o.getOccupation();
        int quant = o.getQuantity();
        if (new_o != null || quant < 1)
        {
          // Farm off handling to the Tile itself then examine the status returned
          int status = origin_t.processOccupationOrder(new_o, quant);
          if (status > 0)
          {
            System.out.println("WARNING : Failed to generate " + status + " / " + quant + " " + new_o.name());
          }
        }
        else
        {
          System.out.println("ERROR : Invalid mod occupation order! Cannot process...");
        }
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
      case MOD:
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

  private class CoordNode
  {
    public int x;
    public int y;
    public Double value;
    public Direction direction;

    public CoordNode(Coordinate c)
    {
      this.x = c.x;
      this.y = c.y;
      this.value = 0.0;
      this.direction = Direction.N;
    }

    public CoordNode(int x, int y, Double v, Direction d)
    {
      this.x = x;
      this.y = y;
      this.value = v;
      this.direction = d;
    }

    public String toString()
    {
      return "("+ x + "," + y + ") " + value + " [" + direction.name() + "]";
    }
  }

  public class Path
  {
    private Coordinate start;
    private ArrayList<Direction> path;

    public Path(Coordinate start)
    {
      this.start = start;
      this.path = new ArrayList<Direction>();
    }

    public Coordinate getStart()
    {
      return start;
    }

    public Boolean addStep(Direction d)
    {
      return path.add(d);
    }
    public void addStep(int index, Direction d)
    {
      path.add(index, d);
    }

    public ArrayList<Direction> getPath()
    {
      return path;
    }

    public Boolean verifyPath()
    {
      Coordinate c = new Coordinate(start.x, start.y);
      Iterator<Direction> iter = path.iterator();
      while (iter.hasNext())
      {
        if (!verifyCoordRange(c))
        {
          System.out.println("ERROR - Invalid path with coord : " + c.toString());
          return false;
        }
        takeStep(iter.next(), c, false);
      }
      return true;
    }
  }

  public Boolean addPath(Path p)
  {
    if (p == null)
    {
      System.out.println("ERROR - Cannot add null path in addPath()");
      return false;
    }
    if (!p.verifyPath())
    {
      System.out.println("ERROR - Cannot add invalid path in addPath()");
      return false;
    }
    Coordinate c = new Coordinate (p.getStart().x, p.getStart().y);
    Iterator<Direction> iter = p.getPath().iterator();

    while (iter.hasNext())
    {
      // Get ArrayList handle of existing paths on this coordinate
      if (paths.get(coordinates[c.x][c.y]) == null)
      {
        System.out.println("ERROR - Corrupted path map or invalid coordinate : " + c.toString());
        return false;
      }
      ArrayList<Path> list = paths.get(coordinates[c.x][c.y]);
      // Check if current path is already in this coordinate
      if (!list.contains(p))
      {
        // If we aren't able to add this path
        if (!list.add(p))
        {
          System.out.println("ERROR - Unable to add new path on : " + c.toString());
          return false;
        }
      }
      // Update coordinate based on path
      takeStep(iter.next(), c, false);
    }
    return true;
  }

  private void takeStep(Direction d, Coordinate coord, Boolean reverse)
  {
    switch (d)
    {
      case E:
        coord.x += reverse ? -1 : 1;
        break;
      case W:
        coord.x += reverse ? 1 : -1;
        break;
      case S:
        coord.y += reverse ? -1 : 1;
        break;
      case N:
        coord.y += reverse ? 1 : -1;
        break;
      case SE:
        coord.x += reverse ? -1 : 1;
        coord.y += reverse ? -1 : 1;
        break;
      case SW:
        coord.x += reverse ? 1 : -1;
        coord.y += reverse ? -1 : 1;
        break;
      case NE:
        coord.x += reverse ? -1 : 1;
        coord.y += reverse ? 1 : -1;
        break;
      case NW:
        coord.x += reverse ? 1 : -1;
        coord.y += reverse ? 1 : -1;
        break;
      default:
        break;
    }
    return;
  }

  private Path findShortestPath(Coordinate start, Coordinate end)
  {
    if (start == null || end == null)
    {
      return null;
    }
    if (!verifyCoordRange(start) || !verifyCoordRange(end))
    {
      return null;
    }
    System.out.println("Start : " + start.toString() + " => End : " + end.toString());
    // Initialize min_heap with override that compares CoordNodes
    PriorityQueue<CoordNode> min_heap = new PriorityQueue<CoordNode>(100, new Comparator<CoordNode>()
    {
      @Override
      public int compare(CoordNode a, CoordNode b) {
        return (int)(a.value - b.value);
      }
    });

    // Initialize visited grid
    CoordNode[][] v_grid = new CoordNode[width][height];
    for (int i = 0; i < width; i++)
    {
      for (int j = 0; j < height; j++)
      {
        v_grid[i][j] = new CoordNode(coordinates[i][j]);
      }
    }
    // Start min_heap with start location
    min_heap.add(v_grid[start.x][start.y]);

    int x;
    int y;

    while (min_heap.peek() != null)
    {
      CoordNode c = min_heap.poll();
      x = c.x;
      y = c.y;

      // System.out.println("c.toString() " + c.toString());
      // System.out.println("v_grid[x][y] " + v_grid[x][y]);
      // System.out.println(Arrays.toString(min_heap.toArray()));

      // Have we visited this node already? Only continue if our path
      // is the cheapest path on this node.
      if (v_grid[x][y].value > 0.0 && v_grid[x][y].value > c.value)
      {
        v_grid[x][y].value = c.value;
        v_grid[x][y].direction = c.direction;
      }
      else if (v_grid[x][y].value > 0.0)
      {
        continue;
      }

      // Update V grid with current node's value and direction
      v_grid[x][y].value = c.value;
      v_grid[x][y].direction = c.direction;

      // Got to where we needed. The shortest path is found by traversing
      // the directions to get here backwards
      if (x == end.x && y == end.y)
      {
        break;
      }

      // If we get here, we need to keep going, so attempt to go in every
      // direction
      Double new_val;
      if (verifyCoordRange(x + 1, y))
      {
        // Get travel time for the next node then update the next tile's value
        // <next tile cheapest> = <cheapest cost to get here> + <cost to get to next tile>
        new_val = v_grid[x][y].value + getTile(x + 1, y).getTravelTime();
        // Update direction taken and add to heap
        min_heap.add(new CoordNode(x + 1, y, new_val, Direction.E));
      }
      if (verifyCoordRange(x - 1, y))
      {
        new_val = v_grid[x][y].value + getTile(x - 1, y).getTravelTime();
        min_heap.add(new CoordNode(x - 1, y, new_val, Direction.W));
      }
      if (verifyCoordRange(x, y + 1))
      {
        new_val = v_grid[x][y].value + getTile(x, y + 1).getTravelTime();
        min_heap.add(new CoordNode(x, y + 1, new_val, Direction.S));
      }
      if (verifyCoordRange(x, y - 1))
      {
        new_val = v_grid[x][y].value + getTile(x, y - 1).getTravelTime();
        min_heap.add(new CoordNode(x, y - 1, new_val, Direction.N));
      }
      // DO DIAGONALS
      if (verifyCoordRange(x + 1, y + 1))
      {
        new_val = v_grid[x][y].value + getTile(x + 1, y + 1).getTravelTime() * hypotenuse;
        min_heap.add(new CoordNode(x + 1, y + 1, new_val, Direction.SE));
      }
      if (verifyCoordRange(x + 1, y - 1))
      {
        new_val = v_grid[x][y].value + getTile(x + 1, y - 1).getTravelTime() * hypotenuse;
        min_heap.add(new CoordNode(x + 1, y - 1, new_val, Direction.NE));
      }
      if (verifyCoordRange(x - 1, y + 1))
      {
        new_val = v_grid[x][y].value + getTile(x - 1, y + 1).getTravelTime() * hypotenuse;
        min_heap.add(new CoordNode(x - 1, y + 1, new_val, Direction.SW));
      }
      if (verifyCoordRange(x - 1, y - 1))
      {
        new_val = v_grid[x][y].value + getTile(x - 1, y - 1).getTravelTime() * hypotenuse;
        min_heap.add(new CoordNode(x - 1, y - 1, new_val, Direction.NW));
      }
    }

    // Start from the end tile and reverse traverse to create the path
    Path path = new Path(start);
    Coordinate coord = new Coordinate(end.x, end.y);
    // x = end.x;
    // y = end.y;
    System.out.println("Value of path found = " + v_grid[end.x][end.y].value);
    while (!(coord.x == start.x && coord.y == start.y) && verifyCoordRange(coord))
    {
      Direction step = v_grid[coord.x][coord.y].direction;
      path.addStep(0, step);
      takeStep(step, coord, true);
    }
    return path;
  }

  // Send the given population along the path, updating the Tiles
  private void travelPath(Path path, Population pop)
  {
    if (path == null)
    {
      System.out.println("ERROR - Invalid path on travelPath()");
      return;
    }
    if (pop == null)
    {
      System.out.println("ERROR - Invalid pop on travelPath()");
      return;
    }

    int pop_num = pop.getPopulation();
    Iterator<Direction> iter = path.getPath().iterator();
    Coordinate coord = path.getStart();
    Direction dir;
    Tile tile;

    // Iterate through tiles based on directions in the path
    while (iter.hasNext() && verifyCoordRange(coord))
    {
      tile = getTile(coord);
      if (tile == null)
      {
        System.out.println("ERROR - Invalid tile on travelPath() + " + coord.toString());
        break;
      }
      // Update Tile's pop traveled
      tile.migrate(pop_num);
      // Move coordinate based on direction in path
      dir = iter.next();
      takeStep(dir, coord, false);
    }
    return;
  }

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
    p.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.PEASANT), 1000);

    tile.printTile();
    // tile.printNeighbors();
    manager.getTile(neighbor_E).printTile();
    // manager.getTile(neighbor_E).printNeighbors();

    State s = new State(1, manager);
    manager.consumeTile(s, center);
    manager.printStateCoordinates(s);
    Order move_100_wheat = new Order(s, 1, center, neighbor_E, Resource.WHEAT, 100);
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
    move_100_humans.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.PEASANT), 250);
    Order move_100_human_peasants = new Order(s, 1, center, neighbor_E, move_100_humans);
    s.addOrder(move_100_human_peasants);
    manager.update();
    tile.printTile();
    east_tile.printTile();
    // p2.pushCreature()

    // Test shortest path algorithm
    System.out.println("===== BEGIN SHORTEST PATH TEST =====");
    int n_x_n = 50;
    TileManager manager_2 = new TileManager(n_x_n, n_x_n, "shortest_path.png");
    // The construction of the map where G is grassland and M is mountain.
    // G G G G G
    // M M M G G
    // G M G G M
    // G M G M M
    // G G G G G
    Path man_2_path = manager_2.findShortestPath(new Coordinate(0,0), new Coordinate(49,49));
    System.out.println("Path len  = " + man_2_path.getPath().size());
    Iterator<Direction> iter = man_2_path.getPath().iterator();
    while (iter.hasNext())
    {
      System.out.print(iter.next() + " ");
    } System.out.println("\n");

    manager_2.travelPath(man_2_path, p);
    man_2_path = manager_2.findShortestPath(new Coordinate(0,0), new Coordinate(49,49));
    System.out.println("Path len  = " + man_2_path.getPath().size());
    iter = man_2_path.getPath().iterator();
    while (iter.hasNext())
    {
      System.out.print(iter.next() + " ");
    } System.out.println("\n");

    manager_2.travelPath(man_2_path, p);
    manager_2.travelPath(man_2_path, p);
    manager_2.travelPath(man_2_path, p);
    manager_2.travelPath(man_2_path, p);
    man_2_path = manager_2.findShortestPath(new Coordinate(0,0), new Coordinate(49,49));
    System.out.println("Path len  = " + man_2_path.getPath().size());
    iter = man_2_path.getPath().iterator();
    while (iter.hasNext())
    {
      System.out.print(iter.next() + " ");
    } System.out.println("\n");

    System.out.println(man_2_path.verifyPath());
    System.out.println(man_2_path.verifyPath());
    System.out.println(manager_2.addPath(man_2_path));

    man_2_path = manager_2.findShortestPath(new Coordinate(20,10), new Coordinate(49,49));
    System.out.println("Path len  = " + man_2_path.getPath().size());
    iter = man_2_path.getPath().iterator();
    while (iter.hasNext())
    {
      System.out.print(iter.next() + " ");
    } System.out.println("\n");

    System.out.println("TESTS PASSED : [" + passes + "/7]");
  }
}
