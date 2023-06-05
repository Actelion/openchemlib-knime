package com.actelion.research.knime.nodes.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.actelion.research.chem.IsomericSmilesCreator;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.gui.JDrawArea;
import com.actelion.research.gui.JDrawPanel;
import com.actelion.research.gui.editor.SwingEditorPanel;

public class JExtendedDrawPanel extends JPanel  {

    //private JDrawPanel mDrawPanel;
	private SwingEditorPanel mSwingEditorPanel;

    private JPanel     mTop;
    private JPanel     mTop_Right;

    private JMenuBar   jmb_Main;
    private JMenu      jm_Tools;

    public JExtendedDrawPanel() {
        reinit();
    }

    private void reinit() {
        this.removeAll();

        StereoMolecule mi = new StereoMolecule();
        mi.setFragment(true);
        this.mSwingEditorPanel = new SwingEditorPanel(mi);

        this.setLayout(new BorderLayout());
        this.add(this.mSwingEditorPanel, BorderLayout.CENTER);

        this.mTop = new JPanel();
        this.mTop_Right = new JPanel();

        this.mTop.setLayout(new BorderLayout());
        this.add(mTop,BorderLayout.NORTH);
        this.mTop.add(this.mTop_Right,BorderLayout.EAST);

        // init Menu
        initToolsMenu();
        this.mTop_Right.setLayout(new FlowLayout(FlowLayout.RIGHT));
        this.jmb_Main = new JMenuBar();
        this.mTop_Right.add(this.jmb_Main);
        this.jmb_Main.add(this.jm_Tools);
    }

    private void initToolsMenu() {
        this.jm_Tools = new JMenu("Tools");
        JMenu export_expanded = new JMenu("Export..");
        JMenuItem copy_idcode  = new JMenuItem("Copy IDCode");
        JMenuItem copy_smiles  = new JMenuItem("Copy Smiles");
        
        JMenuItem paste_any = new JMenuItem("Paste..");
        
        

        this.jm_Tools.add(export_expanded);
        export_expanded.add(copy_idcode);
        export_expanded.add(copy_smiles);
        
        this.jm_Tools.add(paste_any);

        copy_idcode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String idc = mSwingEditorPanel.getDrawArea().getMolecule().getIDCode();
                StringSelection stringSelection = new StringSelection(idc);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });
        copy_smiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //String idc = mDrawPanel.getDrawArea().getMolecule().getIDCode();
                IsomericSmilesCreator isc = new IsomericSmilesCreator(mSwingEditorPanel.getDrawArea().getMolecule());
                String smiles = isc.getSmiles();
                StringSelection stringSelection = new StringSelection(smiles);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });
    }


    public SwingEditorPanel getDrawPanel() {
        return this.mSwingEditorPanel;
    }

    private class JExtendedDrawPanelBar {

    }

}
