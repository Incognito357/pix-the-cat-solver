package com.incognito.pix.the.cat.solver.gui;

import static org.apache.commons.text.WordUtils.capitalizeFully;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.incognito.pix.the.cat.solver.models.Cell;
import com.incognito.pix.the.cat.solver.models.Grid;
import com.incognito.pix.the.cat.solver.models.Level;
import com.incognito.pix.the.cat.solver.models.World;
import com.incognito.pix.the.cat.solver.models.enums.CellType;
import com.incognito.pix.the.cat.solver.models.serialization.PointKeyDeserializer;
import com.incognito.pix.the.cat.solver.models.serialization.PointKeySerializer;
import com.incognito.pix.the.cat.solver.optimization.LevelSolutionFactory;
import com.incognito.pix.the.cat.solver.optimization.planning.LevelSolution;
import com.incognito.pix.the.cat.solver.optimization.planning.ScoreCalculator;
import com.incognito.pix.the.cat.solver.optimization.planning.Standstill;
import com.incognito.pix.the.cat.solver.optimization.planning.Visit;
import com.incognito.pix.the.cat.solver.optimization.planning.VisitNearbyDistanceMeter;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.heuristic.selector.common.nearby.NearbySelectionConfig;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.chained.SubChainSelectorConfig;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Editor extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(Editor.class);

    private JComboBox<String> cmbCells;
    private JList<String> lstLevels;
    private JPanel pnlRoot;
    private EditorPanel pnlEditor;
    private JSpinner numWidth;
    private JSpinner numHeight;
    private JComboBox<String> cmbMode;
    private JButton btnNew;
    private JButton btnExport;
    private JButton btnImport;
    private JButton btnSwap;
    private JButton btnRotClock;
    private JButton btnRotAnti;
    private JButton btnSolve;
    private JSpinner solutionStep;

    private NameDialog dlgName = new NameDialog();
    private DefaultListModel<String> lstLevelsModel = new DefaultListModel<>();
    private transient World world = new World();
    private ObjectMapper objectMapper = new ObjectMapper();

    private LevelSolution pathSolution = null;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            logger.error("Could not set look and feel!", e);
        }

        new Editor();
    }

    public Editor() {
        $$$setupUI$$$();

        setTitle("Editor");
        setContentPane(pnlRoot);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        pack();
        setVisible(true);
        dlgName.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        SimpleModule module = new SimpleModule();
        module.addKeySerializer(Point.class, new PointKeySerializer());
        module.addKeyDeserializer(Point.class, new PointKeyDeserializer());
        objectMapper.registerModule(module);

//        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test_new.lvl")) {
//            importWorld(is);
//        } catch (IOException e) {
//            logger.error("Could not load default world!", e);
//        }
    }

    private void createUIComponents() {
        String[] items = new String[CellType.values().length];
        int i = 0;
        for (CellType cellType : CellType.values()) {
            items[i++] = capitalizeFully(cellType.name(), '_').replace("_", " ");
        }

        lstLevels = new JList<>(lstLevelsModel);
        cmbCells = new JComboBox<>(items);
        cmbMode = new JComboBox<>();
        numWidth = new JSpinner(new SpinnerNumberModel(EditorPanel.DEFAULT_WIDTH, 1, 100, 1));
        numHeight = new JSpinner(new SpinnerNumberModel(EditorPanel.DEFAULT_HEIGHT, 1, 100, 1));
        btnNew = new JButton();
        btnExport = new JButton();
        btnImport = new JButton();
        btnSwap = new JButton();
        btnRotClock = new JButton();
        btnRotAnti = new JButton();
        btnSolve = new JButton();
        solutionStep = new JSpinner(new SpinnerNumberModel(-1, -1, 100, 1));

        lstLevels.addListSelectionListener(this::lstLevelsSelected);
        cmbCells.addActionListener(this::cmbCellsSelected);
        cmbMode.addActionListener(this::cmbModeSelected);
        numWidth.addChangeListener(e -> pnlEditor.setGridWidth((int) numWidth.getValue()));
        numHeight.addChangeListener(e -> pnlEditor.setGridHeight((int) numHeight.getValue()));
        btnNew.addActionListener(this::btnNewClicked);
        btnExport.addActionListener(this::btnExportClicked);
        btnImport.addActionListener(this::btnImportClicked);
        btnSwap.addActionListener(e -> pnlEditor.swap());
        btnRotClock.addActionListener(e -> pnlEditor.rotate(true));
        btnRotAnti.addActionListener(e -> pnlEditor.rotate(false));
        btnSolve.addActionListener(this::btnSolveClicked);
        solutionStep.addChangeListener(this::onSolutionStepChanged);
    }

    private void onSolutionStepChanged(ChangeEvent changeEvent) {
        if (pnlEditor != null) {
            pnlEditor.setSolutionStep((int) solutionStep.getValue());
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        pnlRoot = new JPanel();
        pnlRoot.setLayout(new GridBagLayout());
        pnlRoot.setMinimumSize(new Dimension(1000, 400));
        pnlRoot.setPreferredSize(new Dimension(1000, 600));
        pnlRoot.setRequestFocusEnabled(true);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setContinuousLayout(false);
        splitPane1.setDividerLocation(30);
        splitPane1.setLastDividerLocation(10);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        pnlRoot.add(splitPane1, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        splitPane1.setRightComponent(panel1);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Cell");
        panel2.add(label1);
        panel2.add(cmbCells);
        final JLabel label2 = new JLabel();
        label2.setText("Mode");
        panel2.add(label2);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Draw");
        defaultComboBoxModel1.addElement("Erase");
        defaultComboBoxModel1.addElement("Select");
        cmbMode.setModel(defaultComboBoxModel1);
        panel2.add(cmbMode);
        final JLabel label3 = new JLabel();
        label3.setText("Size");
        panel2.add(label3);
        final JLabel label4 = new JLabel();
        label4.setText("X:");
        panel2.add(label4);
        panel2.add(numWidth);
        final JLabel label5 = new JLabel();
        label5.setText("Y:");
        panel2.add(label5);
        panel2.add(numHeight);
        btnNew.setText("New");
        panel2.add(btnNew);
        btnExport.setText("Export");
        panel2.add(btnExport);
        btnImport.setText("Import");
        panel2.add(btnImport);
        btnSwap.setText("Egg <-> Target");
        panel2.add(btnSwap);
        btnRotClock.setMinimumSize(new Dimension(30, 30));
        btnRotClock.setPreferredSize(new Dimension(30, 30));
        btnRotClock.setText("↻");
        panel2.add(btnRotClock);
        btnRotAnti.setPreferredSize(new Dimension(30, 30));
        btnRotAnti.setText("↺");
        panel2.add(btnRotAnti);
        btnSolve.setText("Solve");
        panel2.add(btnSolve);
        solutionStep.setEnabled(false);
        panel2.add(solutionStep);
        pnlEditor = new EditorPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(pnlEditor, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPane1.setLeftComponent(scrollPane1);
        lstLevels.setMinimumSize(new Dimension(30, 0));
        lstLevels.setPreferredSize(new Dimension(30, 0));
        scrollPane1.setViewportView(lstLevels);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return pnlRoot;
    }

    private void btnNewClicked(ActionEvent e) {
        dlgName.setLocationRelativeTo(this);
        dlgName.setVisible(true);
        if (dlgName.getName() == null) {
            return;
        }
        String name = dlgName.getName();
        lstLevelsModel.addElement(name);
        Grid<Cell> g = new Grid<>();
        List<Cell> row = new ArrayList<>();
        for (int x = 0; x < EditorPanel.DEFAULT_WIDTH; x++) {
            row.add(new Cell(CellType.EMPTY));
        }
        for (int y = 0; y < EditorPanel.DEFAULT_HEIGHT; y++) {
            g.addRow(row);
        }
        Level level = new Level(name, g);
        world.addTail(level);
        pnlEditor.setLevel(level);
        numWidth.setValue(g.getWidth());
        numHeight.setValue(g.getHeight());
        lstLevels.setSelectedIndex(lstLevelsModel.size() - 1);
    }

    private void btnExportClicked(ActionEvent e) {
        JFileChooser dlgSave = new JFileChooser();
        if (dlgSave.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = dlgSave.getSelectedFile();
        List<Level> export = new ArrayList<>();
        for (Level l : world) {
            export.add(l);
        }
        try {
            objectMapper.writeValue(file, export);
        } catch (IOException ex) {
            logger.error("Could not export world!", ex);
        }
    }

    private void btnImportClicked(ActionEvent e) {
        JFileChooser dlgOpen = new JFileChooser();
        if (dlgOpen.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = dlgOpen.getSelectedFile();
        world.clear();
        lstLevels.clearSelection();
        lstLevelsModel.clear();
        try (FileInputStream is = new FileInputStream(file)) {
            importWorld(is);
        } catch (IOException ex) {
            logger.error("Could not load world!", ex);
        }
    }

    private void importWorld(InputStream is) throws IOException {
        List<Level> imported = objectMapper.readValue(is, new TypeReference<List<Level>>() {});
        imported.forEach(level -> {
            lstLevelsModel.addElement(level.getName());
            world.addTail(level);
        });
        if (!world.isEmpty()) {
            lstLevels.setSelectedIndex(0);
        }
    }

    private void lstLevelsSelected(ListSelectionEvent e) {
        int index = lstLevels.getSelectedIndex();
        if (index == -1) {
            return;
        }
        Level l = world.get(index);
        Grid<Cell> grid = l.getGrid();
        pnlEditor.setLevel(l);
        numWidth.setValue(grid.getWidth());
        numHeight.setValue(grid.getHeight());
    }

    private void cmbCellsSelected(ActionEvent e) {
        if (cmbCells.getSelectedItem() != null) {
            String val = ((String) cmbCells.getSelectedItem()).toUpperCase();
            pnlEditor.setDrawMode(CellType.valueOf((val.replace(' ', '_'))));
        }
    }

    private void cmbModeSelected(ActionEvent e) {
        if (cmbMode.getSelectedItem() != null) {
            pnlEditor.setClickMode((EditorPanel.ClickMode.valueOf(((String) cmbMode.getSelectedItem()).toUpperCase())));
        }
    }

    private void btnSolveClicked(ActionEvent e) {
        if (world != null && !world.isEmpty()) {
            SolverConfig solverConfig = new SolverConfig();
            solverConfig.withTerminationConfig(new TerminationConfig().withUnimprovedMinutesSpentLimit(1L));
            solverConfig.withSolutionClass(LevelSolution.class);
            solverConfig.withEntityClasses(Standstill.class, Visit.class);
            solverConfig.withScoreDirectorFactory(new ScoreDirectorFactoryConfig().withEasyScoreCalculatorClass(ScoreCalculator.class));

            ChangeMoveSelectorConfig changeMoveSelectorConfig = new ChangeMoveSelectorConfig();
            changeMoveSelectorConfig.setEntitySelectorConfig(selectorConfigFactory("changeMoveSelector"));
            changeMoveSelectorConfig.setValueSelectorConfig(nearbySelectorFactory(changeMoveSelectorConfig.getEntitySelectorConfig().getId()));
            SubChainChangeMoveSelectorConfig subChainChangeMoveSelectorConfig = new SubChainChangeMoveSelectorConfig();
            subChainChangeMoveSelectorConfig.setSubChainSelectorConfig(new SubChainSelectorConfig());
            subChainChangeMoveSelectorConfig.setSelectReversingMoveToo(true);
            SubChainSwapMoveSelectorConfig subChainSwapMoveSelectorConfig = new SubChainSwapMoveSelectorConfig();
            subChainSwapMoveSelectorConfig.setSelectReversingMoveToo(true);
            solverConfig.withPhases(
                    new ConstructionHeuristicPhaseConfig()
                            .withConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT),
                    new LocalSearchPhaseConfig()
                            .withMoveSelectorConfig(new UnionMoveSelectorConfig(Arrays.asList(
                                    changeMoveSelectorConfig,
                                    subChainChangeMoveSelectorConfig,
                                    subChainSwapMoveSelectorConfig)))
                            .withForagerConfig(new LocalSearchForagerConfig().withAcceptedCountLimit(1))
                            .withAcceptorConfig(new AcceptorConfig().withLateAcceptanceSize(200)));
            SolverFactory<LevelSolution> solverFactory = SolverFactory.create(solverConfig);
            Solver<LevelSolution> solver = solverFactory.buildSolver();
            solver.addEventListener(solution -> {
                logger.info("New best solution found: {}\n\tPath: {}", solution.getNewBestScore(), solution.getNewBestSolution().getPath());
                if (((HardSoftScore) solution.getNewBestScore()).isFeasible()) {
                    pnlEditor.setSolution(solution.getNewBestSolution());
                    SwingUtilities.invokeLater(() -> {
                        pnlEditor.repaint();
                        solutionStep.setModel(new SpinnerNumberModel((int) solutionStep.getValue(), -1, solution.getNewBestSolution().getPath().size() - 1, 1));
                        solutionStep.setEnabled(true);
                    });
                }
            });
            CompletableFuture<LevelSolution> future = CompletableFuture.supplyAsync(() ->
                    solver.solve(LevelSolutionFactory.create(world)));
            future.whenComplete((solution, ex) -> {
                if (ex != null) {
                    logger.error("Could not solve", ex);
                } else {
                    logger.info("Solving complete. Best found: {}\n\tPath: {}", solution.getScore(), solution.getPath());
                }
            });
        }
    }

    private EntitySelectorConfig selectorConfigFactory(String id) {
        EntitySelectorConfig entitySelectorConfig = new EntitySelectorConfig();
        entitySelectorConfig.setId(id);
        return entitySelectorConfig;
    }

    private ValueSelectorConfig nearbySelectorFactory(String id) {
        ValueSelectorConfig valueSelectorConfig = new ValueSelectorConfig();
        NearbySelectionConfig nearbySelectionConfig = new NearbySelectionConfig();
        EntitySelectorConfig entitySelectorConfig = new EntitySelectorConfig();
        entitySelectorConfig.setMimicSelectorRef(id);
        nearbySelectionConfig.setOriginEntitySelectorConfig(entitySelectorConfig);
        nearbySelectionConfig.setNearbyDistanceMeterClass(VisitNearbyDistanceMeter.class);
        valueSelectorConfig.setNearbySelectionConfig(nearbySelectionConfig);
        return valueSelectorConfig;
    }
}
