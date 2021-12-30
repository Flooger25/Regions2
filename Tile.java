import java.util.*;
import java.awt.Color;

public class Tile
{
  // Valid directions are N, S, E, W
  //    [ ]
  // [] [X] []
  //    [ ]
  //
  // ...or NW, NE, W, E, SW, SE
  //  []   []
  // [] [X] []
  //  []   []
  //
  public enum Direction
  {
    NW, NE, W, E, SW, SE, N, S
  }
  // Geographic tile type
  public enum TileType
  {
    FOREST, GRASSLAND, SEA, MOUNTAIN
  }
  // Resource types
  public enum Resource
  {
    WHEAT, WOOD, STONE, GEMS, METAL
  }

  public static int max_neighbors = 6;
  // Size of tile in km
  public static final int tile_size = 1;
  // Valid numbers 0-10
  private int infrastructure;
  private TileType type;
  private Color color;
  private Coordinate coord;
  private Dictionary<Resource, Integer> resources;
  private Map<Direction, Tile> neighbors;
  private Population pop;

  public Tile(TileType type, Coordinate c)
  {
    this.type = type;
    this.coord = c;
    this.infrastructure = 0;
    this.pop = new Population();
    initialize(false);
  }

  public Tile(Color c, Coordinate coord)
  {
    this.type = TileType.GRASSLAND;
    this.coord = coord;
    this.color = c;
    this.infrastructure = 0;
    this.pop = new Population();
    initialize(true);
  }

  private final void initialize(Boolean provide_color)
  {
    neighbors = new Hashtable<Direction, Tile>();
    resources = new Hashtable<Resource, Integer>();
    // Define tile type based on color provided
    if (provide_color)
    {
      setTypeFromColor();
    }
    instantiate_resources(false);
  }

  // Instantiate the presence of resources in the tile
  private void instantiate_resources(Boolean provide_color)
  {
    Random rand = new Random();
    switch (type)
    {
      case FOREST:
        if (provide_color) color = Color.WHITE;
        resources.put(Resource.WOOD, rand.nextInt(900));
        resources.put(Resource.STONE, rand.nextInt(90));
        resources.put(Resource.WHEAT, rand.nextInt(10));
        break;
      case GRASSLAND:
        if (provide_color) color = Color.GREEN;
        resources.put(Resource.WHEAT, rand.nextInt(900));
        resources.put(Resource.STONE, rand.nextInt(90));
        resources.put(Resource.WOOD, rand.nextInt(10));
        break;
      case MOUNTAIN:
        if (provide_color) color = Color.GRAY;
        resources.put(Resource.STONE, rand.nextInt(900));
        resources.put(Resource.METAL, rand.nextInt(90));
        resources.put(Resource.GEMS, rand.nextInt(10));
        break;
      case SEA:
        if (provide_color) color = Color.BLUE;
      default:
        break;
    }
  }

  private void setTypeFromColor()
  {
    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();
    // System.out.println("Eddie " + r + " " + g + " " + b);
    // if (r > g && r > b) return Color.RED;
    if (g > r && g > b) type = TileType.GRASSLAND;
    else if (b > g && b > r) type = TileType.SEA;
    else type = TileType.MOUNTAIN;
  }

  public Color getTileColor()
  {
    switch (type)
    {
      case FOREST:
      case GRASSLAND:
        return Color.GREEN;
      case MOUNTAIN:
        return Color.GRAY;
      case SEA:
        return Color.BLUE;
      default:
        break;
    }
    return null;
  }

  public String toString()
  {
    return type.name() + " " + coord.toString();
  }

  public void printTile()
  {
    System.out.println(toString());
    System.out.println(resources.toString());
    pop.printPopulation();
  }

  public Color getColor()
  {
    return color;
  }

  public Coordinate getCoordinate()
  {
    return coord;
  }

  public Dictionary<Resource, Integer> getResources()
  {
    return resources;
  }

  public int getResourceQuantity(Resource r)
  {
    return resources.get(r);
  }

  public int extractResource(Resource r, int quantity)
  {
    int num_available;
    // This resource doesn't exist here, so return 0
    if (resources.get(r) == null)
    {
      return 0;
    }
    num_available = resources.get(r);
    // We're extracting a quantity less than we have, so update the remaining
    if (quantity < num_available)
    {
      resources.put(r, num_available - quantity);
      return quantity;
    }
    // Values are the same, so remove the resource
    else if (quantity == num_available)
    {
      resources.remove(r);
      return num_available;
    }
    return 0;
  }

  public void addResource(Resource r, int quantity)
  {
    if (resources.get(r) == null)
    {
      resources.put(r, quantity);
    }
    else
    {
      resources.put(r, resources.get(r) + quantity);
    }
  }

  public Boolean setNeighbor(Direction d, Tile t)
  {
    if (neighbors.get(d) != null)
    {
      System.out.println("ERROR - Direction %s already assigned!" + d);
      return false;
    }
    if (t == null || t == this)
    {
      System.out.println("WARNING - Invalid Tile, cannot assign as neighbor");
      return false;
    }
    neighbors.put(d, t);
    return true;
  }

  public void printNeighbors()
  {
    System.out.println("Neighbors for tile - " + toString() + " are as follows: ");
    for (Map.Entry<Direction, Tile> entry : neighbors.entrySet())
    {
      System.out.println(entry.getKey().name() + " - " + entry.getValue().toString());
    }
  }

  // Return enumerated list of resources available in the tile
  public Enumeration getResourcesAvailable()
  {
    if (resources == null)
    {
      System.out.println("ERROR - No resources found!");
      return null;
    }
    return resources.keys();
  }

  // As infrastructure increases, so does the average km/h
  // Return how long, in minutes, to traverse a tile based on
  //  infrastructure level
  public int getTravelTime()
  {
    return (int)(tile_size * (1 / (infrastructure + 1) * 60)); // minutes/hr
  }

  public int getCostOfUpgrade()
  {
    if (infrastructure < 10)
    {
      return (int)(100 * Math.pow(1.9, infrastructure + 1));
    }
    System.out.println("WARNING : Cannot upgrade infrastructure beyond 10");
    return -1;
  }

  public int getCostOfMaintenance()
  {
    if (infrastructure > 0)
    {
      return (int)(100 * Math.pow(1.2, infrastructure));
    }
    return 0;
  }

  public Population getPopulation()
  {
    return pop;
  }

  public void update(Boolean upgrade, Boolean maintain)
  {
    if (upgrade && infrastructure < 10) infrastructure++;
    else if (!maintain)
    {
      // Natural decay
      infrastructure -= 1;
      if (infrastructure < 0) infrastructure = 0;
    }
    // Receive events
    // Gather resources
    // Modify population based on resource
    // 
  }
  // Just test stuff
  public static void main(String[] args)
  {
    int passes = 0;
    Coordinate c = new Coordinate(1,1);
    Tile tile = new Tile(Tile.TileType.GRASSLAND, c);
    System.out.println(tile.getTravelTime());
    System.out.println(tile.getCostOfUpgrade());
    System.out.println(tile.getCostOfMaintenance());

    tile.printTile();

    System.out.println("TESTS PASSED : [" + passes + "/7]");
  }
}
