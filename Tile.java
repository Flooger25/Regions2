import java.util.*;

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

  public enum TileType
  {
    FOREST, GRASSLAND, SEA, MOUNTAIN
  }

  public enum Resource
  {
    WHEAT, WOOD, STONE, GEMS, METAL
  }

  public static int max_neighbors = 6;

  private TileType type;
  private Coordinate coord;
  private Dictionary<Resource, Integer> resources;
  private Dictionary<Direction, Tile> neighbors;
  // private Population pop;

  public Tile(TileType type, Coordinate c)
  {
    this.type = type;
    coord = c;
    neighbors = new Hashtable<Direction, Tile>();
    resources = new Hashtable<Resource, Integer>();
    instantiate_resources(resources);
  }

  public Tile(TileType type, int x, int y)
  {
    this.type = type;
    coord = new Coordinate(x, y);
    neighbors = new Hashtable<Direction, Tile>();
    resources = new Hashtable<Resource, Integer>();
    instantiate_resources(resources);
  }

  public Boolean setNeighbor(Direction d, Tile t)
  {
    if (neighbors.get(d) != null)
    {
      System.out.println("ERROR - Direction %s already assigned!" + d);
      return false;
    }
    neighbors.put(d, t);
    return true;
  }

  // Return enumerated list of resources available in the tile
  public Enumeration getResources()
  {
    if (resources == null)
    {
      System.out.println("ERROR - No resources found!");
      return null;
    }
    return resources.keys();
  }

  // Instantiate the presence of resources in the tile
  private void instantiate_resources(Dictionary<Resource, Integer> res)
  {
    Random rand = new Random();
    switch (type)
    {
      case FOREST:
        res.put(Resource.WOOD, rand.nextInt(900));
        res.put(Resource.STONE, rand.nextInt(90));
        res.put(Resource.WHEAT, rand.nextInt(10));
        break;
      case GRASSLAND:
        res.put(Resource.WHEAT, rand.nextInt(900));
        res.put(Resource.STONE, rand.nextInt(90));
        res.put(Resource.WOOD, rand.nextInt(10));
        break;
      case MOUNTAIN:
        res.put(Resource.STONE, rand.nextInt(900));
        res.put(Resource.METAL, rand.nextInt(90));
        res.put(Resource.GEMS, rand.nextInt(10));
        break;
      default:
        break;
    }
  }
}
