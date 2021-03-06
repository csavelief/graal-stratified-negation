package fr.lirmm.graphik;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Set;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import com.google.errorprone.annotations.Var;

import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.GraphOfRuleDependencies;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.forward_chaining.SccChase;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.util.graph.scc.StronglyConnectedComponentsGraph;

class Window extends JFrame {
  private static final long serialVersionUID = -4228059556541657661L;

  private final JMenuBar menu;
  private final JTextArea displayZone;
  private final JLabel infoNode;
  private final boolean master;
  private View view;
  private JScrollPane scroll;
  private DefaultLabeledGraphOfRuleDependencies grd;
  private Graph grdDisp;
  private Viewer viewer;
  private StronglyConnectedComponentsGraph<Rule> scc;
  private Graph sccDisp;

  public Window(boolean master) {
    /* Initialize the Window */

    super();

    this.master = master;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setTitle("Dependencies Viewer");
    setName("Dependencies Viewer");
    setPreferredSize(new Dimension(1024, 720));

    /* Initialize the Menu */
    menu = new JMenuBar();
    menu.setPreferredSize(new Dimension(360, 30));
    menu.setMaximumSize(new Dimension(360, 30));
    menu.setLayout(new GridLayout(1, 6));
    menu.setVisible(true);

    /* File */
    JMenu fileMenu = new JMenu("File");
    fileMenu.setPreferredSize(new Dimension(120, 30));
    fileMenu.setMaximumSize(new Dimension(120, 30));
    fileMenu.setVisible(true);
    JMenuItem chooser = new JMenuItem("Open");
    chooser.addActionListener(e -> openFile());
    fileMenu.add(chooser);


    JMenuItem quit = new JMenuItem("Quit");
    quit.setMaximumSize(new Dimension(120, 30));
    quit.addActionListener(e -> fermer());
    quit.setVisible(true);
    fileMenu.add(quit);
    menu.add(fileMenu);


    /* Tool */
    JMenu toolMenu = new JMenu("Tools");
    toolMenu.setPreferredSize(new Dimension(120, 30));
    toolMenu.setMaximumSize(new Dimension(120, 30));
    toolMenu.setVisible(true);
    JMenuItem rulesText = new JMenuItem("Print Rules");

    rulesText.addActionListener(e -> printRules());

    toolMenu.add(rulesText);

    JMenuItem grdText = new JMenuItem("Print GRD");
    grdText.addActionListener(e -> printGRD());
    toolMenu.add(grdText);

    JMenuItem grdVisu = new JMenuItem("Display GRD");
    grdVisu.addActionListener(e -> displayGRD());
    toolMenu.add(grdVisu);

    JMenuItem sccText = new JMenuItem("Print SCC");
    sccText.addActionListener(e -> printSCC());
    toolMenu.add(sccText);

    JMenuItem sccVisu = new JMenuItem("Display SCC");
    sccVisu.addActionListener(e -> displaySCC());
    toolMenu.add(sccVisu);

    JMenu forwardChaining = new JMenu("Forward Chaining");

    JMenuItem fcFromFile = new JMenuItem("From file");
    fcFromFile.addActionListener(e -> launchForwardChainingFromFile());
    forwardChaining.add(fcFromFile);
    /*
     * fcFromDB = new JMenuItem("From DataBase"); fcFromDB.addActionListener(new ActionListener() {
     * public void actionPerformed(ActionEvent e) {
     * 
     * } }); forwardChaining.add(fcFromDB);
     */

    toolMenu.add(forwardChaining);

    toolMenu.insert("-----", 1);
    toolMenu.insert("-----", 4);
    toolMenu.insert("-----", 7);
    menu.add(toolMenu);


    /* Save */
    JMenu saveMenu = new JMenu("Save");
    saveMenu.setPreferredSize(new Dimension(120, 30));
    saveMenu.setMaximumSize(new Dimension(120, 30));
    saveMenu.setVisible(true);

    JMenuItem saveRules = new JMenuItem("Save Rules");
    saveRules.addActionListener(e -> exportRules());
    saveMenu.add(saveRules);

    JMenuItem saveGRD = new JMenuItem("Save GRD");
    saveGRD.addActionListener(e -> exportGRD());
    saveMenu.add(saveGRD);

    JMenuItem saveSCC = new JMenuItem("Save SCC Graph");
    saveSCC.addActionListener(e -> exportSCC());
    saveMenu.add(saveSCC);

    JMenuItem saveFC = new JMenuItem("Save Forward Chaining");
    saveFC.addActionListener(e -> exportFC());
    saveMenu.add(saveFC);

    saveMenu.insert("-----", 1);
    saveMenu.insert("-----", 3);
    saveMenu.insert("-----", 5);

    menu.add(saveMenu);

    menu.add(Box.createHorizontalGlue());
    menu.add(Box.createHorizontalGlue());
    menu.add(Box.createHorizontalGlue());


    /* Initialize the info zone */

    JToolBar info = new JToolBar("Information");
    info.setMaximumSize(new Dimension(1024, 30));
    info.setFloatable(false);
    info.setLayout(new GridLayout(1, 6));
    info.setVisible(true);

    infoNode = new JLabel(" ");
    infoNode.setVisible(true);

    info.add(infoNode);

    /* Build the GUI */

    setLayout(new BorderLayout());

    add(menu, BorderLayout.NORTH);
    add(info, BorderLayout.SOUTH);

    pack();
    setVisible(true);
    setLocationRelativeTo(null);

    this.displayZone = new JTextArea();
  }

  private Window(GraphOfRuleDependencies graphOfRuleDependencies, boolean master) {
    /* Initialize the Window */
    this(master);

    this.grd = (DefaultLabeledGraphOfRuleDependencies) graphOfRuleDependencies;
    this.grdDisp = null;
    this.scc = this.grd.getStronglyConnectedComponentsGraph();
    this.sccDisp = null;

    this.infoNode.setText("Ready\n");

    displayGRD();
  }

  public static String getRulesText(Iterable<Rule> rules) {
    StringBuilder s = new StringBuilder("====== RULE SET ======\n");
    for (Rule r : rules) {
      s.append(r.toString());
      s.append('\n');
    }
    return s.toString();
  }

  public static String getGRDText(DefaultLabeledGraphOfRuleDependencies grd) {
    StringBuilder s = new StringBuilder("======== GRD =========\n");
    for (Rule r1 : grd.getRules()) {
      for (Rule r2 : grd.getTriggeredRules(r1)) {
        s.append("[");
        s.append(r1.getLabel());
        s.append("] ={+}=> [");
        s.append(r2.getLabel());
        s.append("]\n");
      }
      for (Rule r2 : grd.getInhibitedRules(r1)) {
        s.append("[");
        s.append(r1.getLabel());
        s.append("] ={-}=> [");
        s.append(r2.getLabel());
        s.append("]\n");
      }
    }
    return s.toString();
  }

  public static String getSCCText(StronglyConnectedComponentsGraph<Rule> scc) {
    StringBuilder s = new StringBuilder("======== SCC =========\n");
    for (int i = 0; i < scc.getNbrComponents(); i++) {
      @Var
      boolean first = true;
      s.append("C");
      s.append(i);
      s.append(" = {");
      for (Rule r : scc.getComponent(i)) {
        if (first)
          first = false;
        else
          s.append(", ");
        s.append(r.getLabel());
      }
      s.append("}\n");
    }
    return s.toString();
  }

  public static String getGSCCText(Graph sccDisp) {
    StringBuilder s = new StringBuilder("======== SCC GRAPH =========\n");
    for (Iterator<Node> itNode = sccDisp.getNodeIterator(); itNode.hasNext();) {
      Node n = itNode.next();
      for (Edge e : n.getEachLeavingEdge()) {
        s.append(n.getId());

        if ((char) e.getAttribute("label") == '+')
          s.append(" =+=> ");
        else
          s.append(" =-=> ");

        s.append(e.getTargetNode().getId());
        s.append("\n");
      }
    }
    return s.toString();
  }

  public static String getSaturationFromFile(String src,
      DefaultLabeledGraphOfRuleDependencies grd) {
    KBBuilder kbb = new KBBuilder();
    Utils.readKB(kbb, null, src);

    KnowledgeBase kb = kbb.build();

    SccChase<AtomSet> chase = new SccChase<>(grd, kb.getFacts());
    try {
      chase.execute();
    } catch (ChaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return Utils.displayFacts(kb.getFacts());
  }

  /*************************************************/

  private void openFile() {
    JFileChooser c = new JFileChooser(".");
    FileNameExtensionFilter filter = new FileNameExtensionFilter("DLGP files", "dlgp");
    c.setFileFilter(filter);
    int returnVal = c.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      this.clearDrawZone();

      this.infoNode.setText("Computing...");
      this.displayZone.setText("Computing dependencies for " + c.getSelectedFile().getName()
          + " (this may take a while)...");
      this.displayZone.setCaretPosition(0);
      this.displayZone.setEditable(false);
      this.scroll = new JScrollPane(this.displayZone);
      this.add(this.scroll, BorderLayout.CENTER);

      this.pack();
      this.grd = new DefaultLabeledGraphOfRuleDependencies(c.getSelectedFile());
      this.grdDisp = null;
      this.scc = this.grd.getStronglyConnectedComponentsGraph();
      this.sccDisp = null;

      this.infoNode.setText("Ready\n");

      this.pack();
    }
  }

  private void fermer() {
    dispose();
    if (master)
      System.exit(0);
  }

  private void printRules() {
    if (this.grd != null) {
      this.clearDrawZone();

      this.infoNode.setText("Rules : " + grd.getNodeCount());
      this.displayZone.setText(Window.getRulesText(grd.getRules()));
      this.displayZone.setCaretPosition(0);
      this.displayZone.setEditable(false);
      this.scroll = new JScrollPane(this.displayZone);
      this.add(this.scroll, BorderLayout.CENTER);
      this.pack();
    }
  }

  private void printGRD() {
    if (this.grd != null) {
      clearDrawZone();

      this.infoNode.setText("Rules : " + grd.getNodeCount() + " | Edges : " + grd.getEdgeCount());
      if (grd.hasCircuitWithNegativeEdge())
        this.infoNode.setText(this.infoNode.getText() + " | Not stratifiable");
      else
        this.infoNode.setText(this.infoNode.getText() + " | Stratifiable");
      this.displayZone.setText(Window.getGRDText(grd));
      this.displayZone.setCaretPosition(0);
      this.displayZone.setEditable(false);
      this.scroll = new JScrollPane(this.displayZone);
      this.add(this.scroll, BorderLayout.CENTER);
      this.pack();
    }
  }

  private void displayGRD() {
    if (this.grd != null) {
      clearDrawZone();

      if (this.grdDisp == null)
        this.grdDisp = DefaultGraphOfRuleDependenciesViewer.instance().getGraph(grd);

      this.infoNode.setText("Rules : " + grd.getNodeCount() + " | Edges : " + grd.getEdgeCount());
      if (grd.hasCircuitWithNegativeEdge())
        this.infoNode.setText(this.infoNode.getText() + " | Not stratifiable");
      else
        this.infoNode.setText(this.infoNode.getText() + " | Stratifiable");

      this.viewer = new Viewer(this.grdDisp, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
      this.viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
      this.viewer.enableAutoLayout();

      this.view = this.viewer.addDefaultView(false);
      this.view.addMouseListener(new MouseListener() {

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseClicked(MouseEvent e) {
          GraphicElement n =
              view.findNodeOrSpriteAt(e.getX(), e.getY() + menu.getMaximumSize().getHeight());
          if (n != null) {
            System.out.println("Node : " + n.label);
            infoNode.setText(grdDisp.getNode(n.label).getAttribute("rule"));
          } else {
            infoNode.setText("Rules : " + grd.getNodeCount() + " | Edges : " + grd.getEdgeCount());
            if (grd.hasCircuitWithNegativeEdge())
              infoNode.setText(infoNode.getText() + " | Not stratifiable");
            else
              infoNode.setText(infoNode.getText() + " | Stratifiable");
          }
        }
      });

      ((JPanel) this.view).addMouseWheelListener(e -> {
        if (e.getWheelRotation() < 0) {
          System.out.println("wheel up");

          @Var
          double cX = view.getCamera().getMetrics().lengthToGu(e.getPoint().getX(), Units.PX);
          cX = e.getPoint().getX() / view.getCamera().getMetrics().ratioPx2Gu;
          System.out.println("ancien : " + view.getCamera().getViewCenter().toString());
          @Var
          double cY = view.getCamera().getMetrics().lengthToGu(e.getPoint().getY(), Units.PX);
          cY = e.getPoint().getY() / view.getCamera().getMetrics().ratioPx2Gu;
          double cZ = view.getCamera().getViewCenter().z;
          view.getCamera().setViewCenter(cX, cY, cZ);
          System.out.println("nouveau : " + view.getCamera().getViewCenter().toString());
        } else if (e.getWheelRotation() > 0)
          System.out.println("wheel down");

      });

      add((Component) this.view, BorderLayout.CENTER);
      pack();
    }
  }

  private void printSCC() {
    System.out.println(Window.getSCCText(this.scc));
    if (this.grd != null) {
      if (this.sccDisp == null)
        this.sccDisp = DefaultGraphOfRuleDependenciesViewer.instance().getSCCGraph(this.grd);
      this.infoNode.setText("Strongly Connected Components : " + sccDisp.getNodeCount()
          + " | Edges : " + sccDisp.getEdgeCount());
      if (grd.hasCircuitWithNegativeEdge())
        this.infoNode.setText(this.infoNode.getText() + " | Not stratifiable");
      else
        this.infoNode.setText(this.infoNode.getText() + " | Stratifiable");
      this.clearDrawZone();
      this.displayZone.setText(Window.getSCCText(this.scc) + Window.getGSCCText(this.sccDisp));
      this.displayZone.setCaretPosition(0);
      this.displayZone.setEditable(false);
      this.scroll = new JScrollPane(this.displayZone);


      this.add(this.scroll, BorderLayout.CENTER);
      this.pack();
    }
  }

  private void displaySCC() {
    if (this.grd != null) {
      clearDrawZone();

      if (this.sccDisp == null)
        this.sccDisp = DefaultGraphOfRuleDependenciesViewer.instance().getSCCGraph(this.grd);

      this.infoNode.setText("Strongly Connected Components : " + sccDisp.getNodeCount()
          + " | Edges : " + sccDisp.getEdgeCount());
      if (grd.hasCircuitWithNegativeEdge())
        this.infoNode.setText(this.infoNode.getText() + " | Not stratifiable");
      else
        this.infoNode.setText(this.infoNode.getText() + " | Stratifiable");


      this.viewer = new Viewer(this.sccDisp, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
      this.viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
      this.viewer.enableAutoLayout();

      this.view = this.viewer.addDefaultView(false);
      this.view.addMouseListener(new MouseListener() {

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseClicked(MouseEvent e) {

          GraphicElement n =
              view.findNodeOrSpriteAt(e.getX(), e.getY() + menu.getMaximumSize().getHeight());
          if (n != null) {
            infoNode.setText(sccDisp.getNode(n.label).getAttribute("c"));

            Set<Rule> s = scc.getComponent(Integer.parseInt(n.label.replaceAll("C", "")));
            System.out.println(s);

            new Window(grd.getSubGraph(s), false);
          } else
            infoNode.setText("Strongly Connected Components : " + sccDisp.getNodeCount()
                + " | Edges : " + sccDisp.getEdgeCount());
        }
      });

      ((JPanel) this.view).addMouseWheelListener(e -> {
        if (e.getWheelRotation() < 0) {
          System.out.println("wheel up");

          @Var
          double cX = view.getCamera().getMetrics().lengthToGu(e.getPoint().getX(), Units.PX);
          cX = e.getPoint().getX() / view.getCamera().getMetrics().ratioPx2Gu;
          System.out.println("ancien : " + view.getCamera().getViewCenter().toString());
          @Var
          double cY = view.getCamera().getMetrics().lengthToGu(e.getPoint().getY(), Units.PX);
          cY = e.getPoint().getY() / view.getCamera().getMetrics().ratioPx2Gu;
          double cZ = view.getCamera().getViewCenter().z;
          view.getCamera().setViewCenter(cX, cY, cZ);
          System.out.println("nouveau : " + view.getCamera().getViewCenter().toString());
        } else if (e.getWheelRotation() > 0)
          System.out.println("wheel down");

      });

      add((Component) this.view, BorderLayout.CENTER);
      pack();
    }
  }

  private void launchForwardChainingFromFile() {
    if (this.grd != null) {
      if (!this.grd.hasCircuitWithNegativeEdge()) {
        JFileChooser c = new JFileChooser(".");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("DLGP files", "dlgp");
        c.setFileFilter(filter);
        c.setDialogTitle("Open a Fact Base");
        int returnVal = c.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
          this.clearDrawZone();
          this.displayZone
              .setText(Window.getSaturationFromFile(c.getSelectedFile().getPath(), grd));
          this.displayZone.setCaretPosition(0);
          this.scroll = new JScrollPane(this.displayZone);
          this.add(this.scroll, BorderLayout.CENTER);
          this.pack();
        }
      } else {
        JOptionPane.showMessageDialog(this, "Impossible, the rules are not stratifiable",
            "Impossible", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void exportRules() {
    if (this.grd != null) {
      JFileChooser c = new JFileChooser(".");
      FileNameExtensionFilter filter = new FileNameExtensionFilter("DLGP files", "dlgp");
      c.setFileFilter(filter);
      c.setDialogTitle("Export Rules");
      int returnVal = c.showOpenDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        try (Writer fw = Files.newBufferedWriter(c.getSelectedFile().toPath(), UTF_8)) {
          fw.write(Window.getRulesText(grd.getRules()));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


  private void exportGRD() {
    if (this.grd != null) {
      JFileChooser c = new JFileChooser(".");
      FileNameExtensionFilter filter = new FileNameExtensionFilter("DLGP files", "dlgp");
      c.setFileFilter(filter);
      c.setDialogTitle("Export Graph of Rules Dependencies");
      int returnVal = c.showOpenDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        try (Writer fw = Files.newBufferedWriter(c.getSelectedFile().toPath(), UTF_8)) {
          fw.write(Window.getGRDText(grd));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


  private void exportSCC() {
    if (this.grd != null) {
      if (this.sccDisp == null)
        this.sccDisp = DefaultGraphOfRuleDependenciesViewer.instance().getSCCGraph(this.grd);

      JFileChooser c = new JFileChooser(".");
      FileNameExtensionFilter filter = new FileNameExtensionFilter("DLGP files", "dlgp");
      c.setFileFilter(filter);
      c.setDialogTitle("Export Graph of Strongly Connected Components");
      int returnVal = c.showOpenDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        try (Writer fw = Files.newBufferedWriter(c.getSelectedFile().toPath(), UTF_8)) {
          fw.write(Window.getSCCText(this.scc));
          fw.write(Window.getGSCCText(this.sccDisp));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void exportFC() {
    if (this.grd != null) {
      if (!this.grd.hasCircuitWithNegativeEdge()) {
        /* Computation */
        @Var
        JFileChooser c = new JFileChooser(".");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("DLGP files", "dlgp");
        c.setFileFilter(filter);
        c.setDialogTitle("Open a Facts Base");
        @Var
        int returnVal = c.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
          /* Exportation */
          c = new JFileChooser(".");
          c.setFileFilter(filter);
          c.setDialogTitle("Export Saturated Facts Base");
          returnVal = c.showOpenDialog(this);

          if (returnVal == JFileChooser.APPROVE_OPTION) {
            try (Writer fw = Files.newBufferedWriter(c.getSelectedFile().toPath(), UTF_8)) {
              fw.write(Window.getSaturationFromFile(c.getName(), grd));
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      } else {
        JOptionPane.showMessageDialog(this, "Impossible, the rules are not stratifiable",
            "Impossible", JOptionPane.ERROR_MESSAGE);
      }
    }
  }


  private void clearDrawZone() {
    if (this.view != null)
      this.remove((Component) this.view);
    if (this.scroll != null)
      this.remove(this.scroll);

    this.repaint();
  }
}

