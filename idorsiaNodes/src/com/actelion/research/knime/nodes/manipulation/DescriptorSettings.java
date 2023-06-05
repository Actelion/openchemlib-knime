package com.actelion.research.knime.nodes.manipulation;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.gui.VerticalFlowLayout;
import com.actelion.research.knime.utils.DescriptorHelpers;

public class DescriptorSettings {

	//public static final String PROPERTY_DESCRIPOR_COLUMN = "OCLDescriptor";
	
	public static final String NAME_DESCRIPTOR_COLUMN    = "descriptorColumn";
	public static final String NAME_DESCRIPTOR_SHORTNAME = "descriptorShortName";
	
	public String m_Column = null;
	public String m_DescriptorShortName = null;
	
	
	
	public DescriptorInfo getDescriptorInfo() {
		String shortInfo = this.m_DescriptorShortName;
        if (shortInfo != null) {
            return DescriptorHelpers.getDescriptorInfoByShortName(shortInfo);
        }
        return null;
	}
	
	public String getColumnName() {
		return this.m_Column;
	}
	
	public void setColumnName(String col_name) {
		this.m_Column = col_name;
	}
	
	public void setDescriptorShortName(String short_name) {
		this.m_DescriptorShortName = short_name;
	}
	
	
	public void saveSettings(String descriptor_id, NodeSettingsWO settings) {
		String field_descriptor_col       = this.NAME_DESCRIPTOR_COLUMN+"_"+descriptor_id;
		String field_descriptor_shortname = this.NAME_DESCRIPTOR_SHORTNAME+"_"+descriptor_id;
		settings.addString(field_descriptor_col,this.m_Column);
		settings.addString(field_descriptor_shortname,this.m_DescriptorShortName);	
	}
	
	public void loadSettings(String descriptor_id, NodeSettingsRO settings) {
		String field_descriptor_col       = this.NAME_DESCRIPTOR_COLUMN+"_"+descriptor_id;
		String field_descriptor_shortname = this.NAME_DESCRIPTOR_SHORTNAME+"_"+descriptor_id;
		this.m_Column = settings.getString(field_descriptor_col,null);
		this.m_DescriptorShortName = settings.getString(field_descriptor_shortname,null);
	}
	
	/**
	 * can be called to create initial settings from the input port
	 * 
	 * return false if it did not find a descriptor column
	 * 
	 * 
	 */
	public boolean tryConfigure(DataTableSpec dscTableSpec) {
		
		if(this.m_Column!=null) {
			// then we are done..
			return true;
		}
		
		AvailableDescriptors descriptors = findDescriptorColumns(dscTableSpec);
		if(!descriptors.getAvailableDescriptors().isEmpty()) {
			this.m_Column = descriptors.getAvailableDescriptors().get(0)[0];
			this.m_DescriptorShortName = descriptors.getAvailableDescriptors().get(0)[1];
		} 
		
		return false;
	}
	
	
	public static AvailableDescriptors findDescriptorColumns(DataTableSpec dscTableSpec) {
		
		// 1. find all columns with descriptor property
		List<String[]> descr_cols = new ArrayList<>();
		for( int zi=0;zi < dscTableSpec.getNumColumns();zi++) {
			if(dscTableSpec.getColumnSpec(zi).getProperties().containsProperty(com.actelion.research.knime.utils.SpecHelper.DESCRIPTOR_INFO)) {
				descr_cols.add(new String[] {dscTableSpec.getColumnSpec(zi).getName(),dscTableSpec.getColumnSpec(zi).getProperties().getProperty(com.actelion.research.knime.utils.SpecHelper.DESCRIPTOR_INFO)});	
			} 
		}
		return new AvailableDescriptors(descr_cols);
	}
	
	public static class AvailableDescriptors {
		
		// first is column Name, second is Descriptro short name
		private List<String[]> m_descriptors;
		
		public AvailableDescriptors(List<String[]> descriptors) {	
			this.m_descriptors = descriptors;
		}
		public List<String[]> getAvailableDescriptors() {return m_descriptors;}
		public List<DescriptorColumnField> getAvailableDescriptorsAsDCF() {
			ArrayList<DescriptorColumnField> dcfs = new ArrayList<>();
			this.getAvailableDescriptors().stream().forEachOrdered( ci -> dcfs.add(new DescriptorColumnField(ci[0], ci[1]) ));
			return dcfs;
		}
		
		public List<String> getAllDescriptorShortNames() {
			Set<String> dscs = new HashSet<>();
			this.getAvailableDescriptors().stream().forEachOrdered( ci -> dscs.add(ci[1]));
			return new ArrayList<>(dscs);
		}
	}
	
	public static class DescriptorSettingsGUI extends JPanel {
		private DescriptorSettings m_DescriptorSettings = new DescriptorSettings();
		private AvailableDescriptors m_AvailableDescriptors;
		
		private Set<String> m_restrictToSpecific = null;
		
		private boolean m_enabled = true;
		
		//private JPanel jp_main;
		private JComboBox<DescriptorColumnField> jc_descriptors;
		
		private String descriptor_id = null;
		private String full_propname_descriptor_col = null;
		private String full_propname_descriptor_shortname = null;
		
		
		public DescriptorSettingsGUI(String descriptor_id) {
			this.descriptor_id = descriptor_id;
			if(this.descriptor_id!=null) {
				this.full_propname_descriptor_col=DescriptorSettings.NAME_DESCRIPTOR_COLUMN+"_"+descriptor_id;
				this.full_propname_descriptor_shortname=DescriptorSettings.NAME_DESCRIPTOR_COLUMN+"_"+descriptor_id;
			}
			reinit();
		}
		
		/**
		 * NOTE: return null in case that this is not enabled.
		 * @return 
		 */
		public DescriptorSettings getDescriptorSettings() {
			if(this.m_enabled) {
				return this.m_DescriptorSettings;
			}
			else {
				return null;
			}
		}
		
		
		// with null the filtering can be reset
		public void setShowOnlySpecific(String shortname) {
			// first check if nothing changes, then we do nothing..
			//if(shortname==null && this.m_restrictToSpecific==null) {return;}
			//if(shortname!=null && this.m_restrictToSpecific!=null) {
			//	if(shortname.equals(this.m_restrictToSpecific)) {
			//		return;
			//	}
			//}
			//this.m_restrictToSpecific = shortname;
			//this.reinit();
			this.setShowOnlySpecific(new HashSet<String>(Collections.singleton(shortname)));
		}
		
		// with null the filtering can be reset
		public void setShowOnlySpecific(Set<String> descriptor_shortnames) {
			// first check if nothing changes, then we do nothing..
			if(descriptor_shortnames==null && this.m_restrictToSpecific==null) {return;}
			if(descriptor_shortnames!=null && this.m_restrictToSpecific!=null) {
				if(descriptor_shortnames.equals(this.m_restrictToSpecific)) {
					return;
				}
			}
			
			this.m_restrictToSpecific = descriptor_shortnames;
			this.reinit();
		}
		
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			this.m_enabled = enabled;
			this.jc_descriptors.setEnabled(enabled);
		}
		
		private void reinit() {
			this.removeAll();
			this.setLayout(new FlowLayout());
			JLabel jl_a = new JLabel("Descriptors ");
			this.add(jl_a);
			if(this.m_AvailableDescriptors == null) {
				this.m_AvailableDescriptors = new AvailableDescriptors(new ArrayList<>());
			}
			
			List<DescriptorColumnField> cols = this.m_AvailableDescriptors.getAvailableDescriptorsAsDCF();
			
			if(this.m_restrictToSpecific!=null) {
				cols = cols.stream().filter( ci -> this.m_restrictToSpecific.contains(ci.dscShort)).collect(Collectors.toList());
			}
			DescriptorColumnField[] dcfs = new DescriptorColumnField[cols.size()];
			cols.toArray(dcfs);
			this.jc_descriptors = new JComboBox<DescriptorColumnField>(dcfs);
			this.add(jc_descriptors);
			
			this.jc_descriptors.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_DescriptorSettings.setColumnName(((DescriptorColumnField) jc_descriptors.getSelectedItem() ).col);
					m_DescriptorSettings.setDescriptorShortName(((DescriptorColumnField) jc_descriptors.getSelectedItem() ).dscShort);
				}
			});
			this.revalidate();
			this.repaint();
		}
		
		public void initFromSettings(NodeSettingsRO settings, DataTableSpec spec) throws NotConfigurableException {
			this.initFromSettings(settings,spec,false);
		}
		
		
		/**
		 * 
		 * @param settings
		 * @param spec
		 * @throws NotConfigurableException
		 */
		public void initFromSettings(NodeSettingsRO settings, DataTableSpec spec, boolean show_only_specific) throws NotConfigurableException {
		//public void initFromSettings(NodeSettingsRO settings, DataTableSpec spec, Set<String> restrict_to_dscs) throws NotConfigurableException {	
			String descr_col  = null;
			String descr_type = null;
			
			if(this.full_propname_descriptor_col!=null) {
				descr_col  = settings.getString(this.full_propname_descriptor_col,null);
			}
			if(this.full_propname_descriptor_shortname!=null) {
				descr_type = settings.getString(this.full_propname_descriptor_shortname,null);
			}
			this.m_AvailableDescriptors = findDescriptorColumns(spec);
			
			// handle the init for showing only specific types of descriptors slightly differently:
			// i.e. set the restriction before reinit:
			if(descr_col!=null && show_only_specific) {
				m_restrictToSpecific = Collections.singleton(descr_type);
			}
			//this.m_restrictToSpecific = restrict_to_dscs;
			
			this.reinit();
			
			if(descr_col!=null) {
				//TODO set combo box to correct selection:
				int idx_a = -1;
				for(int zi=0;zi<this.jc_descriptors.getModel().getSize();zi++) {
					if(this.jc_descriptors.getModel().getElementAt(zi).col.equals(descr_col)) {
						idx_a = zi; break;
					}
				}
				if(idx_a>=0) {
					this.jc_descriptors.setSelectedIndex(idx_a);
				}
				else if(this.jc_descriptors.getModel().getSize()>0){
					this.jc_descriptors.setSelectedIndex(0);
				}
			}
			else {
				if(this.jc_descriptors.getModel().getSize()>0){
					this.jc_descriptors.setSelectedIndex(0);
				}
			}
		}
	}
	
	// just for the combobox..
	public static class DescriptorColumnField {
		public final String col;
		public final String dscShort;
		public DescriptorColumnField(String col, String dscs) {
			this.col = col;
			this.dscShort = dscs;
		}
		public String toString() {
			if(col==null) {return "?";}
			if(dscShort!=null) {
				return this.col+"["+dscShort+"]";
			}
			else {
				return this.col+"[?]";
			}
		}
	}
	
}
