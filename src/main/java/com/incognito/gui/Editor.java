package com.incognito.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.incognito.models.Cell;
import com.incognito.models.Grid;
import com.incognito.models.Level;
import com.incognito.models.World;
import com.incognito.models.enums.CellType;
import com.incognito.models.serialization.PointKeyDeserializer;
import com.incognito.models.serialization.PointKeySerializer;

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
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.text.WordUtils.capitalizeFully;

public class Editor extends JFrame {
    private JComboBox cmbCells;
    private JList lstLevels;
    private JPanel pnlRoot;
    private EditorPanel pnlEditor;
    private JSpinner numWidth;
    private JSpinner numHeight;
    private JComboBox cmbMode;
    private JButton btnNew;
    private JButton btnExport;
    private JButton btnImport;
    private JButton btnSwap;
    private JButton btnRotClock;
    private JButton btnRotAnti;

    private NameDialog dlgName = new NameDialog();
    private DefaultListModel<String> lstLevelsModel = new DefaultListModel<>();
    private World world = new World();
    private ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
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
    }

    private void createUIComponents() {
        String[] items = new String[CellType.values().length];
        int i = 0;
        for (CellType cellType : CellType.values()) {
            items[i++] = capitalizeFully(cellType.name(), new char[]{'_'}).replace("_", " ");
        }

        cmbCells = new JComboBox(items);
        cmbMode = new JComboBox();
        numWidth = new JSpinner(new SpinnerNumberModel(EditorPanel.DEFAULT_WIDTH, 1, 100, 1));
        numHeight = new JSpinner(new SpinnerNumberModel(EditorPanel.DEFAULT_HEIGHT, 1, 100, 1));
        btnNew = new JButton();
        btnExport = new JButton();
        btnImport = new JButton();
        btnSwap = new JButton();
        btnRotClock = new JButton();
        btnRotAnti = new JButton();
        lstLevels = new JList(lstLevelsModel);

        numWidth.addChangeListener(e -> pnlEditor.setGridWidth((int) numWidth.getValue()));
        numHeight.addChangeListener(e -> pnlEditor.setGridHeight((int) numHeight.getValue()));
        cmbCells.addActionListener(e -> {
            String val = ((String) cmbCells.getSelectedItem()).toUpperCase();
            pnlEditor.setDrawMode(CellType.valueOf((val.replace(' ', '_'))));
        });
        cmbMode.addActionListener(e -> pnlEditor.setClickMode((EditorPanel.ClickMode.valueOf(((String) cmbMode.getSelectedItem()).toUpperCase()))));
        btnNew.addActionListener(e -> {
            dlgName.setLocationRelativeTo(this);
            dlgName.setVisible(true);
            if (dlgName.getName() != null) {
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
        });
        btnExport.addActionListener(e -> {
            JFileChooser dlgSave = new JFileChooser();
            if (dlgSave.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = dlgSave.getSelectedFile();
                try {
                    List<Level> export = new ArrayList<>();
                    for (Level l : world) {
                        export.add(l);
                    }
                    objectMapper.writeValue(file, export);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        btnImport.addActionListener(e -> {
            JFileChooser dlgOpen = new JFileChooser();
            if (dlgOpen.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = dlgOpen.getSelectedFile();
                world.clear();
                lstLevels.clearSelection();
                lstLevelsModel.clear();
                try (FileInputStream in = new FileInputStream(file)) {
                    List<Level> imported = objectMapper.readValue(in, new TypeReference<List<Level>>() {
                    });
                    imported.forEach(level -> {
                        lstLevelsModel.addElement(level.getName());
                        world.addTail(level);
                    });
                    if (world.size() > 0) lstLevels.setSelectedIndex(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        btnSwap.addActionListener(e -> pnlEditor.swap());
        btnRotClock.addActionListener(e -> pnlEditor.rotate(true));
        btnRotAnti.addActionListener(e -> pnlEditor.rotate(false));
        lstLevels.addListSelectionListener(e -> {
            int index = lstLevels.getSelectedIndex();
            if (index != -1) {
                for (Level l : world) {
                    if (index == 0) {
                        pnlEditor.setLevel(l);
                        numWidth.setValue(l.getGrid().getWidth());
                        numHeight.setValue(l.getGrid().getHeight());
                        break;
                    }
                    index--;
                }
            }
        });
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
        pnlRoot.setPreferredSize(new Dimension(800, 600));
        pnlRoot.setRequestFocusEnabled(true);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setContinuousLayout(false);
        splitPane1.setDividerLocation(0);
        splitPane1.setLastDividerLocation(9);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        pnlRoot.add(splitPane1, gbc);
        splitPane1.setLeftComponent(lstLevels);
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
        btnRotClock.setText("↻");
        panel2.add(btnRotClock);
        btnRotAnti.setText("↺");
        panel2.add(btnRotAnti);
        pnlEditor = new EditorPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(pnlEditor, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return pnlRoot;
    }

}
