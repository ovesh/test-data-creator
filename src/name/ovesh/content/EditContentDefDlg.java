package name.ovesh.content;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import name.ovesh.CreateTableDef;
import name.ovesh.FieldDef;
import name.ovesh.TableDef;
import name.ovesh.TestDataCreator;
import net.miginfocom.swt.MigLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class EditContentDefDlg {
	private Shell dialog;
	private TableDef tableDef;
	private FieldDef fieldDef;
	private Combo combo;
	private Button allowNullValsBtn;
	private Spinner nullValuesRatioSpinner;
	private static final int EMAIL_OPT_IDX = 0;
	private static final int EMAIL_LIST_OPT_IDX = 1;
	private static final int CONSTS_OPT_IDX = 2;
	private static final int NUMERIC_RANGE_OPT_IDX = 3;
	private static final int DATE_RANGE_OPT_IDX = 4;
	private static final int SEQUENCE_OPT_IDX = 5;
	private static final int REFERENCE_OPT_IDX = 6;
	private static final int EN_STRING_OPT_IDX = 7;
	private static final int JP_STRING_OPT_IDX = 8;
	private static final int CUSTOM_OPT_IDX = 9;
	private static final int BLOB_OPT_IDX = 10;
	private static final int DUP_REFERENCE_OPT_IDX = 11;
	private static final int DEP_DUP_REFERENCE_OPT_IDX = 12;
	private Composite blankPanel;
	private Composite constsPanel;
	private Composite numRangePanel;
	private Composite dateRangePanel;
	private Composite sequencePanel;
	private Composite refPanel;
	private Composite customPanel;
	private Composite blobPanel;
	private Text constsTxt;
	private Spinner numMinValSp;
	private Spinner numMaxValSp;
	private Button paddedBtn;
	private DateTime dateMinValDt;
	private DateTime dateMaxValDt;
	private Text dateFormat;
	private Button pgsqlBtn;
	private Button oraBtn;
	private Text seqNameTxt;
	private Combo refTableNameCombo;
	private Combo refFieldNameCombo;
	private Combo customClassNameCombo;
	private CustomContentDef customContentDef;
	private Combo blobFileNameFieldCombo;
	private CreateTableDef parentDlg;
	
	public EditContentDefDlg(Shell inDialog, TableDef tableDef, FieldDef fieldDef, CreateTableDef parentDlg){
		dialog = inDialog;
		this.tableDef = tableDef;
		this.fieldDef = fieldDef;
		this.parentDlg = parentDlg;
		
		MigLayout ml = new MigLayout("nogrid");
		dialog.setLayout(ml);
		dialog.setText("Content Definition");
		dialog.setSize(290, 270);
		
		initNullOkCheckbox();
		initNullOkRatioSpinner();
		initCombo();
		
		Composite mainPanel = new Composite(dialog, SWT.NONE);
		mainPanel.setLayout(new FormLayout());
		mainPanel.setLayoutData("width 400, height 400, wrap");
		
		blankPanel = new Composite(mainPanel, SWT.NONE);
		blankPanel.setLayout(new MigLayout("nogrid"));
		blankPanel.setVisible(false);
		initConstsPanel(mainPanel);
		initNumRangePanel(mainPanel);
		initDateRangePanel(mainPanel);
		initSequencePanel(mainPanel);
		initRefPanel(mainPanel);
		initCustomPanel(mainPanel);
		initBlobPanel(mainPanel);

		initOKBtn();

		dialog.open();
	}

	private void initNullOkCheckbox() {
		allowNullValsBtn = new Button (dialog, SWT.CHECK);
		allowNullValsBtn.setText("Allow NULL values");
		allowNullValsBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				nullValuesRatioSpinner.setEnabled(allowNullValsBtn.getSelection());
			}});
		allowNullValsBtn.setSelection(fieldDef.isNullOk());
	}

	/**
	 * 
	 */
	private void initNullOkRatioSpinner() {
		Label l = new Label(dialog, SWT.NONE);
		l.setText("  (");
		nullValuesRatioSpinner = new Spinner(dialog, SWT.BORDER);
		nullValuesRatioSpinner.setMaximum(100);
		nullValuesRatioSpinner.setSelection(fieldDef.getNullRatio());
		nullValuesRatioSpinner.setEnabled(fieldDef.isNullOk());
		l = new Label(dialog, SWT.NONE);
		l.setText("% of rows)");
		l.setLayoutData("wrap");
	}

	private void initOKBtn(){
		Button btn = new Button (dialog, SWT.PUSH);
		btn.setText ("OK");
		btn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				ContentDef cd = null;
				int selIdx = combo.getSelectionIndex();
				switch(selIdx){
				case(EMAIL_OPT_IDX): {
					cd = new EmailContentDef();
					break;
				}
				case(EMAIL_LIST_OPT_IDX): {
					cd = new EmailListContentDef();
					break;
				}
				case(CONSTS_OPT_IDX): {
					String[] vals = constsTxt.getText().split(",");
					cd = new ConstContentDef(new Vector<String>(Arrays.asList(vals)));
					break;
				}
				case(NUMERIC_RANGE_OPT_IDX): {
					int minVal = numMinValSp.getSelection();
					int maxVal = numMaxValSp.getSelection();
					if(minVal > maxVal){
						TestDataCreator.showErrMsg(dialog, "Maximum value must be larger than the minimum value");
						return;
					}
					if(paddedBtn.getSelection()){
						cd = new PaddedIntContentDef(minVal, maxVal);
					} else{
						cd = new NumericRangeContentDef(minVal, maxVal, fieldDef.getPrecision());
					}
					break;
				}
				case(DATE_RANGE_OPT_IDX): {
					Calendar cal = Calendar.getInstance();
					cal.set(dateMinValDt.getYear(), dateMinValDt.getMonth(), dateMinValDt.getDay());
					Date minDate = cal.getTime();
					cal.set(dateMaxValDt.getYear(), dateMaxValDt.getMonth(), dateMaxValDt.getDay());
					Date maxDate = cal.getTime();
					if(minDate.getTime() >= maxDate.getTime()){
						TestDataCreator.showErrMsg(dialog, "Maximum date must be larger than the minimum value");
						return;
					}
					cd = new DateRangeContentDef(minDate, maxDate, dateFormat.getText(), fieldDef.getType());
					break;
				}
				case(SEQUENCE_OPT_IDX): {
					if(seqNameTxt.getText().trim().length() == 0){
						TestDataCreator.showErrMsg(dialog, "Please fill in the sequence name");
						return;
					}
					cd = pgsqlBtn.getSelection()? 
							new PostgresSequenceContentDef(seqNameTxt.getText()):
								new OracleSequenceContentDef(seqNameTxt.getText());
					break;
				}
				case(REFERENCE_OPT_IDX): {
					int idx = refTableNameCombo.getSelectionIndex();
					if(idx == -1){
						TestDataCreator.showErrMsg(dialog, "Please select a table");
						return;
					}
					TableDef td = ((TableDef[])refTableNameCombo.getData())[idx];
					idx = refFieldNameCombo.getSelectionIndex();
					if(idx == -1){
						TestDataCreator.showErrMsg(dialog, "Please select a field");
						return;
					}
					FieldDef fd = ((FieldDef[])refFieldNameCombo.getData())[idx];
					fd.setReferenced(true);
					cd = new ReferenceContentDef(td, fd);
					break;
				}
				case(DUP_REFERENCE_OPT_IDX): {
					int idx = refTableNameCombo.getSelectionIndex();
					if(idx == -1){
						TestDataCreator.showErrMsg(dialog, "Please select a table");
						return;
					}
					TableDef td = ((TableDef[])refTableNameCombo.getData())[idx];
					idx = refFieldNameCombo.getSelectionIndex();
					if(idx == -1){
						TestDataCreator.showErrMsg(dialog, "Please select a field");
						return;
					}
					FieldDef fd = ((FieldDef[])refFieldNameCombo.getData())[idx];
					fd.setReferenced(true);
					cd = new DupReferenceContentDef(tableDef, fieldDef, td, fd);
					break;
				}
				case(DEP_DUP_REFERENCE_OPT_IDX): {
					int idx = refTableNameCombo.getSelectionIndex();
					if(idx == -1){
						TestDataCreator.showErrMsg(dialog, "Please select a table");
						return;
					}
					TableDef td = ((TableDef[])refTableNameCombo.getData())[idx];
					idx = refFieldNameCombo.getSelectionIndex();
					if(idx == -1){
						TestDataCreator.showErrMsg(dialog, "Please select a field");
						return;
					}
					FieldDef fd = ((FieldDef[])refFieldNameCombo.getData())[idx];
					fd.setReferenced(true);
					cd = new DependentDupReferenceContentDef(null,null,tableDef, fieldDef, td, fd);
					break;
				}

				case(EN_STRING_OPT_IDX): {
					cd = new EnglishStringContentDef();
					break;
				}
				case(JP_STRING_OPT_IDX): {
					cd = new JapaneseStringContentDef();
					break;
				}
				case(CUSTOM_OPT_IDX): {
					if(customContentDef == null){
						TestDataCreator.showErrMsg(dialog, "Please select a class file");
						return;
					}
					cd = customContentDef;
					break;
				}
				case(BLOB_OPT_IDX): {
					int idx = blobFileNameFieldCombo.getSelectionIndex();
					if(idx == -1){
						TestDataCreator.showErrMsg(dialog, "Please select a field");
						return;
					}
					cd = new Blob();
					((Blob)cd).setFileNameField(blobFileNameFieldCombo.getItem(idx));
					break;
				}
				}
				fieldDef.setContentDef(cd);
				fieldDef.setNullOk(allowNullValsBtn.getSelection());
				fieldDef.setNullRatio(allowNullValsBtn.getSelection()? nullValuesRatioSpinner.getSelection(): 0);
				dialog.close();
				parentDlg.populateTable();
			}});
	}
	
	private void initCombo(){
		combo = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(new String[]{"Email", "Email list", "Constants", "Numeric Range", 
				"Date Range", "Sequence", "Reference", "English String", "Japanese String",
				"Custom Class", "BLOB", "Duplicated Reference"});
		ContentDef cd = fieldDef.getContentDef();
		if(cd instanceof EmailContentDef)
			combo.select(EMAIL_OPT_IDX);
		else if(cd instanceof EmailListContentDef)
			combo.select(EMAIL_LIST_OPT_IDX);
		else if(cd instanceof ConstContentDef)
			combo.select(CONSTS_OPT_IDX);
		else if(cd instanceof NumericRangeContentDef || cd instanceof PaddedIntContentDef)
			combo.select(NUMERIC_RANGE_OPT_IDX);
		else if(cd instanceof DateRangeContentDef)
			combo.select(DATE_RANGE_OPT_IDX);
		else if(cd instanceof OracleSequenceContentDef || cd instanceof PostgresSequenceContentDef)
			combo.select(SEQUENCE_OPT_IDX);
		else if(cd instanceof ReferenceContentDef)
			combo.select(REFERENCE_OPT_IDX);
		else if(cd instanceof DupReferenceContentDef)
			combo.select(DUP_REFERENCE_OPT_IDX);
		else if(cd instanceof DependentDupReferenceContentDef)
			combo.select(DEP_DUP_REFERENCE_OPT_IDX);
		else if(cd instanceof EnglishStringContentDef)
			combo.select(EN_STRING_OPT_IDX);
		else if(cd instanceof JapaneseStringContentDef)
			combo.select(JP_STRING_OPT_IDX);
		else if(cd instanceof CustomContentDef)
			combo.select(CUSTOM_OPT_IDX);
		else if(cd instanceof Blob)
			combo.select(BLOB_OPT_IDX);
		combo.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				int selIdx = combo.getSelectionIndex();
				switch(selIdx){
				case(EMAIL_OPT_IDX): {
					togglePanel(blankPanel);
					break;
				}
				case(EMAIL_LIST_OPT_IDX): {
					togglePanel(blankPanel);
					break;
				}
				case(CONSTS_OPT_IDX): {
					togglePanel(constsPanel);
					break;
				}
				case(NUMERIC_RANGE_OPT_IDX): {
					togglePanel(numRangePanel);
					break;
				}
				case(DATE_RANGE_OPT_IDX): {
					togglePanel(dateRangePanel);
					break;
				}
				case(SEQUENCE_OPT_IDX): {
					togglePanel(sequencePanel);
					break;
				}
				case(REFERENCE_OPT_IDX): 
				case(DUP_REFERENCE_OPT_IDX): 
				case(DEP_DUP_REFERENCE_OPT_IDX): 
					togglePanel(refPanel);
					break;
				case(EN_STRING_OPT_IDX): {
					togglePanel(blankPanel);
					break;
				}
				case(JP_STRING_OPT_IDX): {
					togglePanel(blankPanel);
					break;
				}
				case(CUSTOM_OPT_IDX): {
					togglePanel(customPanel);
					break;
				}
				case(BLOB_OPT_IDX): {
					togglePanel(blobPanel);
					break;
				}
				}
			}});
		combo.setLayoutData("wrap");
	}
	
	private void initConstsPanel(Composite mainPanel){
		constsPanel = new Composite(mainPanel, SWT.NONE);
		constsPanel.setLayout(new MigLayout("nogrid"));
		Label label = new Label(constsPanel, SWT.NONE);
		label.setText("Possible values: ");
		constsTxt = new Text(constsPanel, SWT.BORDER);
		constsTxt.setLayoutData("wrap");
		if(fieldDef.getContentDef() instanceof ConstContentDef){
			ConstContentDef cd = (ConstContentDef)fieldDef.getContentDef();
			StringBuffer allowedValsStr = new StringBuffer();
			Vector<String> allowedVals = cd.getAllowedVals();
			for(Iterator<String> it = allowedVals.iterator(); it.hasNext();){
				allowedValsStr.append(it.next()).append(",");
			}
			// snip off last comma
			allowedValsStr.deleteCharAt(allowedValsStr.length() - 1);
			constsTxt.setText(allowedValsStr.toString());
			constsPanel.setVisible(true);
		}
		else
			constsPanel.setVisible(false);
		label = new Label(constsPanel, SWT.NONE);
		label.setText("(comma separated)");
	}

	private void initNumRangePanel(Composite mainPanel) {
		numRangePanel = new Composite(mainPanel, SWT.NONE);
		numRangePanel.setLayout(new MigLayout());
		Label label = new Label(numRangePanel, SWT.NONE);
		label.setText("Minimum Value: ");
		numMinValSp = new Spinner(numRangePanel, SWT.BORDER);
		numMinValSp.setLayoutData("width 50, wrap");
		numMinValSp.setMinimum(-9999999);
		numMinValSp.setMaximum(9999999);
		label = new Label(numRangePanel, SWT.NONE);
		label.setText("Maximum Value: ");
		numMaxValSp = new Spinner(numRangePanel, SWT.BORDER);
		numMaxValSp.setLayoutData("width 50, wrap");
		numMaxValSp.setMinimum(-9999999);
		numMaxValSp.setMaximum(9999999);
		paddedBtn = new Button(numRangePanel, SWT.CHECK);
		paddedBtn.setText("Pad number with zeros");
		if(fieldDef.getContentDef() instanceof NumericRangeContentDef){
			NumericRangeContentDef cd = (NumericRangeContentDef)fieldDef.getContentDef();
			numMinValSp.setSelection((int)cd.getMinVal());
			numMaxValSp.setSelection((int)cd.getMaxVal());
			paddedBtn.setSelection(false);
			numRangePanel.setVisible(true);
		}
		else if(fieldDef.getContentDef() instanceof PaddedIntContentDef){
			PaddedIntContentDef cd = (PaddedIntContentDef)fieldDef.getContentDef();
			numMinValSp.setSelection(cd.getMinVal());
			numMaxValSp.setSelection(cd.getMaxVal());
			paddedBtn.setSelection(true);
			numRangePanel.setVisible(true);
		}
		else
			numRangePanel.setVisible(false);
	}
	
	private void initDateRangePanel(Composite mainPanel) {
		dateRangePanel = new Composite(mainPanel, SWT.NONE);
		dateRangePanel.setLayout(new MigLayout());
		Label label = new Label(dateRangePanel, SWT.NONE);
		label.setText("Minimum Value: ");
		dateMinValDt = new DateTime(dateRangePanel, SWT.DATE | SWT.BORDER);
		dateMinValDt.setLayoutData("wrap");
		label = new Label(dateRangePanel, SWT.NONE);
		label.setText("Maximum Value: ");
		dateMaxValDt = new DateTime(dateRangePanel, SWT.DATE | SWT.BORDER);
		dateMaxValDt.setLayoutData("wrap");
		label = new Label(dateRangePanel, SWT.NONE);
		label.setText("Date format: ");
		dateFormat = new Text(dateRangePanel, SWT.BORDER);
		dateFormat.setText("yyyy/MM/dd");
		if(fieldDef.getContentDef() instanceof DateRangeContentDef){
			DateRangeContentDef cd = (DateRangeContentDef)fieldDef.getContentDef();
			Calendar cal = Calendar.getInstance();
			cal.setTime(cd.getMinVal());
			dateMinValDt.setYear(cal.get(Calendar.YEAR));
			dateMinValDt.setMonth(cal.get(Calendar.MONTH + 1));
			dateMinValDt.setDay(cal.get(Calendar.DAY_OF_MONTH));
			cal.setTime(cd.getMaxVal());
			dateMaxValDt.setYear(cal.get(Calendar.YEAR));
			dateMaxValDt.setMonth(cal.get(Calendar.MONTH + 1));
			dateMaxValDt.setDay(cal.get(Calendar.DAY_OF_MONTH));
			dateRangePanel.setVisible(true);
		}
		else
			dateRangePanel.setVisible(false);
	}
	
	private void initSequencePanel(Composite mainPanel) {
		sequencePanel = new Composite(mainPanel, SWT.NONE);
		sequencePanel.setLayout(new MigLayout());
		Label label = new Label(sequencePanel, SWT.NONE);
		label.setText("Style: ");
		pgsqlBtn = new Button(sequencePanel, SWT.RADIO);
		pgsqlBtn.setText("Postgres");
		pgsqlBtn.setSelection(true);
		oraBtn = new Button(sequencePanel, SWT.RADIO);
		oraBtn.setText("Oracle");
		oraBtn.setLayoutData("wrap");
		label = new Label(sequencePanel, SWT.NONE);
		label.setText("Sequence Name: ");
		seqNameTxt = new Text(sequencePanel, SWT.BORDER);
		seqNameTxt.setLayoutData("span 2, width 120");
		ContentDef cd = fieldDef.getContentDef();
		if(cd instanceof OracleSequenceContentDef ||
				cd instanceof PostgresSequenceContentDef){
			pgsqlBtn.setSelection(cd instanceof OracleSequenceContentDef);
			pgsqlBtn.setSelection(cd instanceof PostgresSequenceContentDef);
			sequencePanel.setVisible(true);
		}
		else
			sequencePanel.setVisible(false);
	}
	
	private void initCustomPanel(Composite mainPanel){
		customPanel = new Composite(mainPanel, SWT.NONE);
		customPanel.setLayout(new MigLayout());
		Label label = new Label(customPanel, SWT.NONE);
		label.setText("Class: ");
		customClassNameCombo = new Combo(customPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
		for(int i = 0, size = TestDataCreator.customContentClasses.size(); i < size; i++){
			customClassNameCombo.add(TestDataCreator.customContentClasses.get(i).getName());
		}
		customClassNameCombo.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				try {
					customContentDef = (CustomContentDef)TestDataCreator.customContentClasses.get(customClassNameCombo.getSelectionIndex()).newInstance();
				} catch (Exception e) {
					TestDataCreator.showErrMsg(dialog, "Failed to instantiate custom class");
					e.printStackTrace();
				}
			}});
		ContentDef cd = fieldDef.getContentDef();
		if(cd instanceof CustomContentDef){
			for(int i = 0, size = customClassNameCombo.getItemCount(); i < size; i++){
				if(customClassNameCombo.getItem(i).equals(cd.getClass().getName())){
					customClassNameCombo.select(i);
					break;
				}
			}
			customPanel.setVisible(true);
		}
		else
			customPanel.setVisible(false);
	}
	
	private void initBlobPanel(Composite mainPanel){
		blobPanel = new Composite(mainPanel, SWT.NONE);
		blobPanel.setLayout(new MigLayout());
		Label label = new Label(blobPanel, SWT.NONE);
		label.setText("File Name Field: ");
		blobFileNameFieldCombo = new Combo(blobPanel, SWT.DROP_DOWN | SWT.READ_ONLY);;
		FieldDef[] fields = tableDef.getFields();
		for(int i = 0; i < fields.length; i++){
			blobFileNameFieldCombo.add(fields[i].getName());
		}
		if(fieldDef.getContentDef() instanceof Blob){
			Blob cd = (Blob)fieldDef.getContentDef();
			String fileNameField = cd.getFileNameField();
			for(int i = 0, size = blobFileNameFieldCombo.getItemCount(); i < size; i++){
				if(blobFileNameFieldCombo.getItem(i).equals(fileNameField)){
					blobFileNameFieldCombo.select(i);
					break;
				}
			}
			refPanel.setVisible(true);
		}
		else
			blobPanel.setVisible(false);
	}
	
	private void initRefPanel(Composite mainPanel) {
		refPanel = new Composite(mainPanel, SWT.NONE);
		refPanel.setLayout(new MigLayout());
		Label label = new Label(refPanel, SWT.NONE);
		label.setText("Table: ");
		refTableNameCombo = new Combo(refPanel, SWT.DROP_DOWN | SWT.READ_ONLY);
		TableDef[] tableDefs = parentDlg.getMainApp().getTableDefs();
		refTableNameCombo.setData(tableDefs);
		for(int i = 0; i < tableDefs.length; i++){
			refTableNameCombo.add(tableDefs[i].getName());
		}
		refTableNameCombo.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent ev) {
				refFieldNameCombo.removeAll();
				TableDef td = ((TableDef[])refTableNameCombo.getData())[refTableNameCombo.getSelectionIndex()];
				FieldDef[] fieldDefs = td.getFields();
				refFieldNameCombo.setData(fieldDefs);
				for(int i = 0; i < fieldDefs.length; i++){
					refFieldNameCombo.add(fieldDefs[i].getName());
				}
			}});
		refTableNameCombo.setLayoutData("wrap");
		label = new Label(refPanel, SWT.NONE);
		label.setText("Field: ");
		refFieldNameCombo = new Combo(refPanel, SWT.DROP_DOWN | SWT.READ_ONLY);;
		if(fieldDef.getContentDef() instanceof ReferenceContentDef){
			ReferenceContentDef cd = (ReferenceContentDef)fieldDef.getContentDef();
			TableDef td = cd.getTableDef();
			FieldDef fd = cd.getFieldDef();
			for(int i = 0, size = refTableNameCombo.getItemCount(); i < size; i++){
				if(refTableNameCombo.getItem(i).equals(td.getName())){
					refTableNameCombo.select(i);
					break;
				}
			}
			refFieldNameCombo.removeAll();
			FieldDef[] fieldDefs = td.getFields();
			refFieldNameCombo.setData(fieldDefs);
			for(int i = 0; i < fieldDefs.length; i++){
				refFieldNameCombo.add(fieldDefs[i].getName());
			}
			for(int i = 0, size = refFieldNameCombo.getItemCount(); i < size; i++){
				if(refFieldNameCombo.getItem(i).equals(fd.getName())){
					refFieldNameCombo.select(i);
					break;
				}
			}
			refPanel.setVisible(true);
		}
		else
			refPanel.setVisible(false);
	}
	
	private void togglePanel(Composite panel){
		blankPanel.setVisible(false);
		constsPanel.setVisible(false);
		numRangePanel.setVisible(false);
		dateRangePanel.setVisible(false);
		sequencePanel.setVisible(false);
		refPanel.setVisible(false);
		customPanel.setVisible(false);
		blobPanel.setVisible(false);
		
		panel.setVisible(true);
	}
}
