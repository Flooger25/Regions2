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
  public static final int width = 500;
  public static final int height = 250;

  public TileManager tm = new TileManager(width, height);

  // public PanelListener listener; // = new PanelListener();

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

    frame.setSize(width, height);
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

  public void actionPerformed(ActionEvent e)
  {
    String command = e.getActionCommand();
    System.out.println("Someone just FUCKING PRESSED ME! " + command);
    switch (command)
    {
      case "NEXT":
        System.out.println("In next case");
        tm.update();
      default:
        break;
    }
    Graphics g = panel.getGraphics();
    g.setColor(Color.RED);
    int i = rand.nextInt(width);
    int j = rand.nextInt(height);
    // drawPixel(i, j, g, 10);
    g.fillRect(i, j, 10, 10);

    // for (int i = 0; i < width; i++)
    // {
    //   for (int j = 0; j < height; j++)
    //   {
    //     drawPixel(i, j, g, 1);
    //   }
    // }

  } 

  private class PanelListener implements MouseListener
  {
    @Override
    public void mouseClicked(MouseEvent event)
    {
      int x = event.getX();
      int y = event.getY() - button_height;
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

  // Called inside panel.repaint() I think?
  // @Override
  public void paintComponent(Graphics g)
  {
    System.out.println("inside repaint");
    super.paintComponent(g);
    System.out.println("inside repaint");
    // Get high-level map
    // Coordinate[][] map = getMap();
    // Color color;
    Random rand = new Random();
    Color color = new Color(rand.nextInt(256),rand.nextInt(256),rand.nextInt(256));
    g.setColor(color);
    int i = rand.nextInt(256);
    int j = rand.nextInt(256);
    drawPixel(i, j, g, 100);
    // Draw geography
    // for (int i = 0; i < width; i++)
    // {
    //   for (int j = 0; j < height; j++)
    //   {
    //     color = tm.getState(tm.getCoordinate(i, j)).getColor();
    //     // No one owns this particular coordinate
    //     if (color == null)
    //     {
    //       g.setColor(Color.GREEN);
    //     }
    //     else {
    //       g.setColor(color);
    //     }
    //     drawPixel(i, j, g, 100);
    //   }
    // }
  }
  // Helper method to draw pixels
  private void drawPixel(int x, int y, Graphics g, int factor)
  {
    g.fillRect(x * factor, y * factor, factor, factor);
  }

  public static void main(String[] args)
  {
    // int width = (int)(500*1.0);
    // int height = (int)(250*1.0);

    // JFrame frame = new JFrame("Pulchra");
    // Panel panel = new Panel();

    // // Set up buttons
    // frame.setSize(width, height + button_height);
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // frame.setVisible(true);
    // // frame.add(panel);

    // // Set up panel
    // panel.addMouseListener(new PanelListener());
    // frame.add(panel);

    Panel mother_panel = new Panel();
    JPanel panel = mother_panel.buildUI();
    JFrame frame = mother_panel.getFrame();

    // JFrame mainFrame = new JFrame("Field");
    // mainFrame.setSize(width+100,height+100);
    // mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // mainFrame.setVisible(true);
    // Panel panel = new Panel();
    // mainFrame.add(panel);

    Thread t = new Thread()
    {
      public void run()
      {
        try {
          long startTime = System.currentTimeMillis();
          for (int i = 0; i < 10000; i++)
          {
            // mother_panel.repaint();
            // panel.repaint();
            // frame.repaint();
            System.out.println("Iteration: " + i);
            Thread.sleep(500);
          }
          long totaltime = System.currentTimeMillis() - startTime;
          System.out.println("Total time: " + totaltime/1000 + "s");
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
    // frame.setVisible(false);
    System.exit(0);
  }
}
