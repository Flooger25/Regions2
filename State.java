import java.util.*;
import java.awt.Color;

public class State {

  public LinkedList<int[]> border = new LinkedList<int[]>();
  public LinkedList<int[]> area = new LinkedList<int[]>();
  public int x,y;
  public Color color;
  public int ID;
  public int[] origin = new int[2];
  public int age = 0;
  public LinkedList<State> warStates = new LinkedList<State>();
  public Demographics demo;

  public State(int x, int y, Color c, int ID) {
    this.x = x;
    this.y = y;
    this.color = c;
    this.ID = ID;

    demo = new Demographics(ID, x, y);

    origin[0] = x;
    origin[1] = y;
    int[] temp = {x,y};
    border.add(temp);
    area.add(temp);
  }

  public Boolean stabilityStatus() {
    // TODO get stability from Demographics instance
    Random rand = new Random();
    if (rand.nextInt(100+age) < 3) {
      return false;
    }
    return true;
  }

  public void update(Region r) {
    Random rand = new Random();
    int[][] grid = r.grid;
    age += 1;

    // If we're the last state, we do not need to try to expand competitively
    if (r.states.size() > 1 && r.age>1000 && rand.nextInt(10) < 8) {
      demo.update();
      return;
    }
    // Expand land
    expand_border(r, 1, null);
    // Expand into warring country
    for (int i=0; i<warStates.size(); i++) {
      State s = warStates.get(i);
      if (s == null) {
        warStates.remove(s);
      }
      else if (rand.nextInt(10) < 1) {
        //Make peace
        s.warStates.remove(this);
        warStates.remove(s);
      } else {
        // System.out.println("What's taking so long 1");
        expand_border(r, s.ID, s);
        if (s.area.size() < area.size() * 0.5) {
          expand_border(r, s.ID, s);
        } else if (s.area.size() < area.size() * 0.25) {
          expand_border(r, s.ID, s); expand_border(r, s.ID, s); expand_border(r, s.ID, s);
        }
      }
    }
    demo.update();
  }

  public void makePeaceWithAll() {
    while (warStates.size() > 0) {
      warStates.pop().warStates.remove(this);
    }
  }

  public void clearArea(int[][] grid) {
    area.clear();
    border.clear();
    for (int i=1; i<grid.length; i++) {
      for (int j=1; j<grid[0].length; j++) {
        if (grid[i][j] == ID) {
          grid[i][j] = 1;
        }
      }
    }
  }

  public Boolean expansion_logic(Region r, State s, int x, int y) {
    int[][] grid = r.grid;
    Boolean changed = false;
    int[] newc = {x, y};
    // Make sure new coordinate is not in our area already
    if (!isInArea(newc)) {
      Tile newT = null;
      // Check if the tile belongs to the Region
      if ((newT = r.r_demo.hasTile(newc)) != null) {
        demo.expand_into_tile(newT);
        r.r_demo.tiles.remove(newT);
        border.add(newc);
        area.add(newc);
        r.grid[x][y] = ID;
        changed = true;
      }
      // No one has the tile, therefore we found it
      else if (r.r_demo.hasTile(newc) == null &&
              (newT = demo.hasTile(newc)) == null &&
              s == null && grid[x][y] ==  1) {
        newT = demo.found_tile(newc[0], newc[1]);
        if (newT != null) {
          border.add(newc);
          area.add(newc);
          r.grid[x][y] = ID;
          changed = true;
        }
      }
      // Otherwise it's in the state we're at war with
      else if (s != null) {
        if ((newT = s.demo.hasTile(newc)) != null) {
          demo.expand_into_tile(newT);
          s.demo.tiles.remove(newT);
          border.add(newc);
          area.add(newc);
          r.grid[x][y] = ID;
          changed = true;
        }
      }
      if (s != null) {
        s.demo.tiles.remove(newT);
        s.removeInArea(newc);
        s.removeInBorder(newc);
      }
    }
    return changed;
  }

  public void expand_border(Region r, int type, State s) {
    int border_size = border.size();
    Random rand = new Random();
    int[][] grid = r.grid;

    for (int i=0; i<border_size; i++) {
      // Get and delete first element of the border LL
      int[] coords = null;
      coords = border.pop();
      Boolean change = false;

      // Cannot expand into the wilderness without enough people
      Tile border_tile = demo.hasTile(coords);
      if (border_tile != null) {
        // TODO Change how a new Tile is created when populations can migrate
        if (border_tile.population < 180) {
          border.add(coords);
          continue;
        }
      }
      // First check if the border tile should even exist
      if (coords[0]+1 < grid.length && coords[1]+1 < grid[0].length && coords[0]-1 > 0) {
        // If we're surrounded by water and/or our own land, just get rid of the lonely border
        if ((grid[coords[0]+1][coords[1]+1] == 0 || grid[coords[0]+1][coords[1]+1] == ID) &&
            (grid[coords[0]+1][coords[1]-1] == 0 || grid[coords[0]+1][coords[1]-1] == ID) &&
            (grid[coords[0]+1][coords[1]] == 0   || grid[coords[0]+1][coords[1]] == ID) &&
            (grid[coords[0]-1][coords[1]+1] == 0 || grid[coords[0]-1][coords[1]+1] == ID) &&
            (grid[coords[0]-1][coords[1]-1] == 0 || grid[coords[0]-1][coords[1]-1] == ID) &&
            (grid[coords[0]-1][coords[1]] == 0   || grid[coords[0]-1][coords[1]] == ID) &&
            (grid[coords[0]][coords[1]+1] == 0   || grid[coords[0]][coords[1]+1] == ID) &&
            (grid[coords[0]][coords[1]-1] == 0   || grid[coords[0]][coords[1]-1] == ID)) {
            continue;
        }
      }
      // First check limits, then expand specific pixel
      if (coords[0]+1 < grid.length && coords[1] < grid[0].length) {
        if (grid[coords[0]+1][coords[1]] == type) {
          change = expansion_logic(r, s, coords[0]+1, coords[1]);
        }
      }
      if (coords[0]-1 > 0 && coords[1] < grid[0].length) {
        if (grid[(coords[0]-1)][coords[1]] == type) {
          change = expansion_logic(r, s, coords[0]-1, coords[1]);
        }
      }
      if (coords[0] > 0 && coords[1]+1 < grid[0].length) {
        if (grid[coords[0]][coords[1]+1] == type) {
          change = expansion_logic(r, s, coords[0], coords[1]+1);
        }
      }
      if (coords[0] > 0 && coords[1]-1 > 0) {
        if (grid[coords[0]][coords[1]-1] == type) {
          change = expansion_logic(r, s, coords[0], coords[1]-1);
        }
      }
      // Diagonals
      if (coords[0]+1 < grid.length && coords[1]+1 < grid[0].length) {
        if (grid[coords[0]+1][coords[1]+1] == type) {
          change = expansion_logic(r, s, coords[0]+1, coords[1]+1);
        }
      }
      if (coords[0]-1 > 0 && coords[1]-1 > 0) {
        if (grid[coords[0]-1][coords[1]-1] == type) {
          change = expansion_logic(r, s, coords[0]-1, coords[1]-1);
        }
      }
      if (coords[0]-1 > 0 && coords[1]+1 < grid[0].length) {
        if (grid[coords[0]-1][coords[1]+1] == type) {
          change = expansion_logic(r, s, coords[0]-1, coords[1]+1);
        }
      }
      if (coords[0]+1 < grid.length && coords[1]-1 > 0) {
        if (grid[coords[0]+1][coords[1]-1] == type) {
          change = expansion_logic(r, s, coords[0]+1, coords[1]-1);
        }
      }
      if (!change) {
        border.add(coords);
      }
    }
  }

  public Boolean isInArea(int[] coord) {
    int size = area.size();
    for (int i=0; i<size; i++) {
      int[] piece = area.pop();
      if (piece[0] == coord[0] && piece[1] == coord[1]) {
        area.add(piece);
        return true;
      }
      area.add(piece);
    }
    return false;
  }

  public Boolean isInBorder(int[] coord) {
    int size = border.size();
    for (int i=0; i<size; i++) {
      int[] piece = border.pop();
      if (piece[0] == coord[0] && piece[1] == coord[1]) {
        border.add(piece);
        return true;
      }
      border.add(piece);
    }
    return false;
  }

  public Boolean removeInArea(int[] coord) {
    int size = area.size();
    for (int i=0; i<size; i++) {
      int[] piece = area.pop();
      if (piece[0] == coord[0] && piece[1] == coord[1]) {
        return true;
      } else {
        area.add(piece);
      }
    }
    return false;
  }

  public Boolean removeInBorder(int[] coord) {
    int size = border.size();
    for (int i=0; i<size; i++) {
      int[] piece = border.pop();
      if (piece[0] == coord[0] && piece[1] == coord[1]) {
        return true;
      } else {
        border.add(piece);
      }
    }
    return false;
  }

  public void contract_border(int[][] grid) {

    int border_size = border.size();
    // System.out.println("Shrinky dink: " + border_size);

    // for (int i=0; i<area.size(); i++) {
    //   System.out.println("In Area: " + area.get(i)[0]+","+area.get(i)[1]);
    // }

    for (int i=0; i<border_size; i++) {

      // Get and delete first element of the LL
      int[] coords = border.pop();
      removeInArea(coords);

      // System.out.println("Coords: " + coords[0]+","+coords[1]+" "+isInArea(coords)+" "+area.size()+" "+removeInArea(coords));

      // First check limits, then expand specific pixel
      if (coords[0]+1 < grid.length && coords[1] < grid[0].length) {
        if (grid[coords[0]+1][coords[1]] == ID) {
          int[] newc = {coords[0]+1,coords[1]};
          border.add(newc);
          // Remove from the area what we popped from the border
          area.remove(newc);
          grid[coords[0]+1][coords[1]] = 1;
        }
      }
      if (coords[0]-1 >= 0 && coords[1] < grid[0].length) {
        if (grid[(coords[0]-1)][coords[1]] == ID) {
          int[] newc = {coords[0]-1,coords[1]};
          border.add(newc);
          area.remove(newc);
          grid[coords[0]-1][coords[1]] = 1;
        }
      }
      if (coords[0] >= 0 && coords[1]+1 < grid[0].length) {
        if (grid[coords[0]][coords[1]+1] == ID) {
          int[] newc = {coords[0],coords[1]+1};
          border.add(newc);
          area.remove(newc);
          grid[coords[0]][coords[1]+1] = 1;
        }
      }
      if (coords[0] >= 0 && coords[1]-1 >= 0) {
        if (grid[coords[0]][coords[1]-1] == ID) {
          int[] newc = {coords[0],coords[1]-1};
          border.add(newc);
          area.remove(newc);
          grid[coords[0]][coords[1]-1] = 1;
        }
      }
      // Diagonals
      if (coords[0]+1 < grid.length && coords[1]+1 < grid[0].length) {
        if (grid[coords[0]+1][coords[1]+1] == ID) {
          int[] newc = {coords[0]+1,coords[1]+1};
          border.add(newc);
          area.remove(newc);
          grid[coords[0]+1][coords[1]+1] = 1;
        }
      }
      if (coords[0]-1 >= 0 && coords[1]-1 >= 0) {
        if (grid[coords[0]-1][coords[1]-1] == ID) {
          int[] newc = {coords[0]-1,coords[1]-1};
          border.add(newc);
          area.remove(newc);
          grid[coords[0]-1][coords[1]-1] = 1;
        }
      }
      if (coords[0]-1 >= 0 && coords[1]+1 < grid[0].length) {
        if (grid[coords[0]-1][coords[1]+1] == ID) {
          int[] newc = {coords[0]-1,coords[1]+1};
          border.add(newc);
          area.remove(newc);
          grid[coords[0]-1][coords[1]+1] = 1;
        }
      }
      if (coords[0]+1 < grid.length && coords[1]-1 >= 0) {
        if (grid[coords[0]+1][coords[1]-1] == ID) {
          int[] newc = {coords[0]+1,coords[1]-1};
          border.add(newc);
          area.remove(newc);
          grid[coords[0]+1][coords[1]-1] = 1;
        }
      }

      // System.out.println("Area After: " + area.size());

    }

  }

}
