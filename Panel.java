import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.Component;
import java.awt.event.*;
import java.lang.*;
import java.util.*;

public class Panel extends JPanel implements ActionListener
{
  // Button event types
  public enum ButtonType
  {
    NEXT, KINGDOM, CITYSTATE, REGION
  }
  // Initialization of the primary variables
  public static final Random rand = new Random();
  public static final int button_height = 50;
  public static final int width = 50;
  public static final int height = 25;
  public static final int scale = 10;

  public TileManager tm = new TileManager(width, height, "ismaia_base.png");
  public JFrame frame;
  public JPanel panel;
  public JButton button2;

  public JPanel buildUI()
  {
    frame = new JFrame("Pulchra");
    panel = new JPanel();
    frame.add(panel);
    panel.addMouseListener(new PanelListener());
    JButton button = new JButton("This is a button.");
    button2 = new JButton("Hello");

    frame.setSize(width * scale, height * scale);
    // frame.setResizable(false);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // frame.setLocationRelativeTo(null);

    // frame.setContentPane(panel);

    panel.setLayout(new FlowLayout());
    panel.add(button);

    button.addActionListener(this);

    frame.setVisible(true);

    // frame.add(panel);

    return panel;
  }

  public JFrame getFrame ()
  {
    return frame;
  }

  // private void initialize_buttons(Panel panel)
  // {
  //   // Set up buttons
  //   for (int i = 0; i < 1; i++)
  //   {
  //     JButton button = new JButton("NEXT");
  //     button.setPreferredSize(new Dimension(button_height, button_height));
  //     // button.setAlignmentX(Component.RIGHT_ALIGNMENT);
  //     button.setOpaque(true);
  //     button.setBorder(null);
  //     // button.setVisible(false);
  //     button.setContentAreaFilled(false);
  //     button.setBorderPainted(false);
  //     button.addActionListener(this);
  //     panel.add(button, BorderLayout.LINE_END);
  //   }
  //   JLabel button_label = new JLabel("Vertical Buttons");
  //   // button_label.setAlignmentX(Component.RIGHT_ALIGNMENT);
  //   panel.add(button_label, BorderLayout.PAGE_START);
  // }

  public void update()
  {
    tm.update();
  }

  public Coordinate[][] getMap()
  {
    return tm.getMap();
  }

  public void printMap()
  {
    Coordinate[][] coordinates = getMap();
    Graphics g = panel.getGraphics();

    for (int x = 0; x < width; x++)
    {
      for (int y = 0; y < height; y++)
      {
        g.setColor(tm.getTile(coordinates[x][y]).getColor());
        g.fillRect(x * scale, y * scale + button_height, scale, scale);
      }
    }
  }

  // Helper method to draw pixels
  private void drawPixel(int x, int y, Graphics g, int factor)
  {
    g.fillRect(x * factor, y * factor, factor, factor);
  }

  public void actionPerformed(ActionEvent e)
  {
    String command = e.getActionCommand();
    System.out.println("An action has been triggered");
    switch (command)
    {
      case "NEXT":
        tm.update();
      case "REFRESH":
        printMap();
      default:
        break;
    }
  } 

  private class PanelListener implements MouseListener
  {
    @Override
    public void mouseClicked(MouseEvent event)
    {
      int x = (event.getX()) / scale;
      int y = (event.getY() - button_height) / scale;
      System.out.println("Someone just clicked at: " + x + "," + y);
      Tile tile = tm.getTile(new Coordinate(x, y));
      if (tile != null)
      {
        tile.printTile();
      }
    }
    @Override
    public void mouseEntered(MouseEvent arg0) {}
    @Override
    public void mouseExited(MouseEvent arg0) {}
    @Override
    public void mousePressed(MouseEvent arg0) {}
    @Override
    public void mouseReleased(MouseEvent arg0) {}
  }

  public static void main(String[] args)
  {
    int max_sim_time = 100000;

    Panel mother_panel = new Panel();
    JPanel panel = mother_panel.buildUI();
    JFrame frame = mother_panel.getFrame();

    Thread t = new Thread()
    {
      public void run()
      {
        try
        {
          for (int i=0; i < 100; i++)
          {
            mother_panel.actionPerformed(new ActionEvent(mother_panel, 1, "REFRESH"));
            Thread.sleep(1000);
          }
          Thread.sleep(max_sim_time);
          System.out.println("Maximum simulation time achieved. Shutting down...");
          Thread.sleep(2000);
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    };
    t.run();
    try
    {
      t.join();
    } catch (Exception e) {}
    System.exit(0);
  }
}
