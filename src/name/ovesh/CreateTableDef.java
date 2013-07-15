package name.ovesh;

import java.util.ArrayList;
import java.util.Vector;
import name.ovesh.content.ContentDef;
import name.ovesh.content.EditContentDefDlg;
import name.ovesh.content.TextContentDef;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class CreateTableDef {
	
	private Shell dialog;
	private TestDataCreator mainApp;
	protected Table table;
	private TableItem curSelectedItem;
	private Combo combo;
	private TableEditor fieldNameEditor;
	// TODO handle placement of editors when table is scrolled
	private Spinner fieldSizeSpinner;
	private Spinner fieldPrecisionSpinner;
	protected Text tableNameTxt;
	protected Spinner requestedRowNumSpinner;
	private Button removeBtn;
	private Vector<Button> primaryKeyChecks = new Vector<Button>();
	protected static final int NAME_COL_IDX = 1;
	protected static final int TYPE_COL_IDX = 2;
	protected static final int PRIMARY_KEY_COL_IDX = 3;
	protected static final int SIZE_COL_IDX = 4;
	protected static final int PRECISION_COL_IDX = 5;
	protected static final int CONTENT_DEF_COL_IDX = 6;
	protected TableDef tableDef = new TableDef();
	protected ArrayList<FieldDef> primaryKeys = new ArrayList<FieldDef>();
	
	public CreateTableDef(Shell inDialog, TestDataCreator mainApp){
		dialog = inDialog;
		dialog.setMinimumSize(500, 400);
		this.mainApp = mainApp;
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		dialog.setLayout(gridLayout);

		dialog.setText("Create Table Definition");
		dialog.setSize(500, 400);
		
		initRemoveBtn();
		initAddBtn();
		initTableNameTxt();
		initRequestedRowNumSpinner();
		initColListTable();
		initOKBtn();
		
		// prevent dialog from closing on ESCAPE
		dialog.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					e.doit = false;
				}
			}
		});

		dialog.open();
	}

	private void initOKBtn(){
		Button okBtn = new Button (dialog, SWT.PUSH);
		okBtn.setText ("OK");
		okBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				String oldName = tableDef.getName();
				String newName = tableNameTxt.getText();
				if(!oldName.equals(newName) && mainApp.containsTable(newName)){
					TestDataCreator.showErrMsg(dialog, 
							"A table with the name " + tableNameTxt.getText() + " is already registered.");
					return;
				}
				tableDef.setName(tableNameTxt.getText());
				tableDef.setRequestedRowNum(requestedRowNumSpinner.getSelection());
				FieldDef[] fields = tableDef.getFields();
				for(int i = 0; i < fields.length; i++){
					ContentDef cd = fields[i].getContentDef();
					if(cd instanceof TextContentDef){
						((TextContentDef)cd).setMaxSize(fields[i].getSize());
					}
				}
				tableDef.setPrimaryKeys(primaryKeys);
				mainApp.putTable(oldName, tableDef);
				dialog.close();
			}});
	}
	
	private void initRemoveBtn(){
		removeBtn = new Button (dialog, SWT.PUSH);
		removeBtn.setText ("Remove");
		removeBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				Vector<Integer> checkedIndices = new Vector<Integer>();
				for(int i = 0, size = table.getItemCount(); i < size; i++){
					TableItem curItem = table.getItem(i);
					if(curItem.getChecked()){
						checkedIndices.add(i);
						FieldDef fd = (FieldDef)curItem.getData();
						tableDef.removeField(fd);
					}
				}
				int[] indices = new int[checkedIndices.size()];
				for(int i = 0; i < indices.length; i++){
					indices[i] = checkedIndices.get(i);
				}
				table.remove(indices);
				for(int i = checkedIndices.size() - 1; i >= 0; i--){
					Button btn = primaryKeyChecks.remove(checkedIndices.get(i).intValue());
					btn.setVisible(false);
					btn.dispose();
				}
				reinitPrimaryCheckboxes();
			}});
		removeBtn.setEnabled(false);
		removeBtn.setLayoutData(new GridData());
	}
	
	private void initAddBtn(){
		Button btn = new Button (dialog, SWT.PUSH);
		btn.setText ("Add Column");
		btn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				// only allow to add new columns if the last field is edited or no
				// fields are added yet
				boolean allowAddRow = false;
				int size = table.getItemCount();
				allowAddRow = allowAddRow || size == 0;
				if(size > 0){
					TableItem prevItem = table.getItem(size - 1);
					allowAddRow = allowAddRow || 
						(prevItem.getText(NAME_COL_IDX).trim().length() > 0 && 
							prevItem.getText(TYPE_COL_IDX).trim().length() > 0);
				}
				if(allowAddRow){
					TableItem item = new TableItem(table, SWT.NONE);
					FieldDef fd = new FieldDef();
					tableDef.addFieldDef(fd);
					item.setData(fd);
					addPrimaryKeyCheckbox(item);
				}
			}});
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = false;
		gd.grabExcessVerticalSpace = false;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		btn.setLayoutData(gd);
	}
	
	private void initTableNameTxt() {
		Label lbl = new Label(dialog, SWT.NONE);
		lbl.setText("Table name: ");
		lbl.setLayoutData(new GridData());
		
		tableNameTxt = new Text(dialog, SWT.BORDER);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 2;
		tableNameTxt.setLayoutData(gd);
	}

	private void initRequestedRowNumSpinner() {
		Label lbl = new Label(dialog, SWT.NONE);
		lbl.setText("Requested row number: ");
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = false;
		gd.grabExcessVerticalSpace = false;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		lbl.setLayoutData(gd);
		requestedRowNumSpinner = new Spinner(dialog, SWT.BORDER);
		requestedRowNumSpinner.setMaximum(99999999);
	}
	
	private void initColListTable() {
		table = new Table(dialog, 
				SWT.CHECK | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gd.horizontalSpan = 3;
		table.setLayoutData(gd);
		table.setItemCount(0);
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText("");
		column.setWidth(20);
		column = new TableColumn(table, SWT.NONE);
		column.setText("Name");
		column.setWidth(100);
		column = new TableColumn(table, SWT.NONE);
		column.setText("Type");
		column.setWidth(100);
		column = new TableColumn(table, SWT.NONE);
		column.setText("PK  ");
		column.setWidth(40);
		column.setAlignment(SWT.RIGHT);
		column.setResizable(false);
		column = new TableColumn(table, SWT.NONE);
		column.setText("Size");
		column.setWidth(40);
		column = new TableColumn(table, SWT.NONE);
		column.setText("Precision");
		column.setWidth(40);
		column = new TableColumn(table, SWT.NONE);
		column.setText("Desired Content");
		column.setWidth(200);
		
		fieldNameEditor = new TableEditor(table);
		//The editor must have the same size as the cell and must
		//not be any smaller than 50 pixels.
		fieldNameEditor.horizontalAlignment = SWT.LEFT;
		fieldNameEditor.grabHorizontal = true;
		fieldNameEditor.minimumWidth = 50;
		
		initTableCombo();
		initFieldSizeSpinner();
		initFieldPrecisionSpinner();
		
		table.addListener(SWT.MouseUp, new Listener(){

			public void handleEvent(Event ev) {
		
				// Identify the selected row and cell
				Point pt = new Point(ev.x, ev.y);
				curSelectedItem = table.getItem(pt);
				if (curSelectedItem == null) return;
				int curSelectedColIdx = 0;
				for (int i = 1; i < CONTENT_DEF_COL_IDX + 1; i++) {
					Rectangle rect = curSelectedItem.getBounds(i);
					if (rect.contains(pt)) {
						curSelectedColIdx = i;
					}
				}
				
				hideDisplayedEditors();
				
				if(curSelectedColIdx == NAME_COL_IDX){
					if(isPreviousLineFilled(curSelectedItem))
						showFieldNameEditor();
				} else if(curSelectedColIdx == TYPE_COL_IDX){
					if(curSelectedItem.getText(NAME_COL_IDX).trim().length() > 0){
						combo.setBounds(curSelectedItem.getBounds(curSelectedColIdx));
						combo.setVisible(true);
					}
				}
				else if(isCurLineFilled()){
					switch(curSelectedColIdx){
					case(SIZE_COL_IDX): {
						showSpinner(fieldSizeSpinner, SIZE_COL_IDX);
						break;
					}
					case(PRECISION_COL_IDX): {
						showSpinner(fieldPrecisionSpinner, PRECISION_COL_IDX);
						break;
					}
					}
				}
			}});
		
		// handle double click on Content Edit column
		table.addListener(SWT.MouseDoubleClick, new Listener(){
			public void handleEvent(Event ev) {
				// Identify the selected row and cell
				Point pt = new Point(ev.x, ev.y);
				curSelectedItem = table.getItem(pt);
				if(curSelectedItem == null || 
					!curSelectedItem.getBounds(CONTENT_DEF_COL_IDX).contains(pt) || 
					!isCurLineFilled())
					return;
				editContentDefDlg(tableDef, (FieldDef)curSelectedItem.getData());
			}
		});
		
		table.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				boolean isChecked = false;
				for(int i = 0, size = table.getItemCount(); i < size; i++){
					if(table.getItem(i).getChecked()){
						isChecked = true;
						break;
					}
				}
				removeBtn.setEnabled(isChecked);
			}});
		
		// move the checkboxes when any column is resized
		for(int i = 0, size = table.getColumnCount(); i < size; i++){
			TableColumn col = table.getColumn(i);
			col.addControlListener(new ControlAdapter(){
				public void controlResized(ControlEvent e) {
					reinitCheckboxPositions();
				}
			});
		}
		initEscape();
	}
	
	private void initEscape(){
		Control[] widgets = dialog.getChildren();
		for(int i = 0; i < widgets.length; i++){
			addEscapeListener(widgets[i]);
		}
		widgets = table.getChildren();
		for(int i = 0; i < widgets.length; i++){
			addEscapeListener(widgets[i]);
		}
	}
	
	private void addEscapeListener(Control c){
		c.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent event) {
				if (event.character == SWT.ESC) {					
					hideDisplayedEditors();
				}
			}});
	}
	
	private void reinitCheckboxPositions(){
		for(int i = 0, size = primaryKeyChecks.size(); i < size; i++){
			TableItem item = table.getItem(i);
			Rectangle r = item.getBounds(PRIMARY_KEY_COL_IDX);
			primaryKeyChecks.get(i).setBounds(r.x, r.y, 15, r.height);
		}
	}
	
	// Clean up any previous editor/combo/spinner control
	private void hideDisplayedEditors(){
		Control oldEditor = fieldNameEditor.getEditor();
		if (oldEditor != null) oldEditor.dispose();
		if(combo.isVisible())
			combo.setVisible(false);
		if(fieldSizeSpinner.isVisible())
			fieldSizeSpinner.setVisible(false);
		if(fieldPrecisionSpinner.isVisible())
			fieldPrecisionSpinner.setVisible(false);
	}

	private void showFieldNameEditor() {
		// The control that will be the editor must be a child of the Table
		Text newEditor = new Text(table, SWT.NONE);
		newEditor.setText(curSelectedItem.getText(NAME_COL_IDX));
		addEscapeListener(newEditor);
		newEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent ev2) {
				Text text = (Text)fieldNameEditor.getEditor();
				fieldNameEditor.getItem().setText(NAME_COL_IDX, text.getText());
				((FieldDef)curSelectedItem.getData()).setName(text.getText());
			}
		});
		newEditor.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					Text text = (Text)fieldNameEditor.getEditor();
					fieldNameEditor.getItem().setText(NAME_COL_IDX, text.getText());
					((FieldDef)curSelectedItem.getData()).setName(text.getText());
					text.dispose();
				}
			}
		});
		newEditor.selectAll();
		newEditor.setFocus();
		fieldNameEditor.setEditor(newEditor, curSelectedItem, NAME_COL_IDX);
	}

	private void showSpinner(Spinner spinner, int colIdx) {
		int val = 0;
		if(curSelectedItem.getText(colIdx).length() > 0)
			val = Integer.parseInt(curSelectedItem.getText(colIdx));
		spinner.setSelection(val);
		spinner.setBounds(curSelectedItem.getBounds(colIdx));
		spinner.setVisible(true);
	}
		
	private void initTableCombo() {
		combo = new Combo (table, SWT.READ_ONLY);
		combo.setItems (new String [] {"INTEGER", "REAL", "TEXT", "TIMESTAMP", "BLOB"});
		combo.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent ev) {
				curSelectedItem.setText(TYPE_COL_IDX, combo.getItem(combo.getSelectionIndex()));
				((FieldDef)curSelectedItem.getData()).setType(combo.getItem(combo.getSelectionIndex()));
				combo.setVisible(false);
			}});
	}
	
	private void initFieldSizeSpinner(){
		fieldSizeSpinner = new Spinner(table, SWT.NONE);
		fieldSizeSpinner.setMaximum(2001);
		
		fieldSizeSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent ev2) {
				curSelectedItem.setText(SIZE_COL_IDX, Integer.toString(fieldSizeSpinner.getSelection()));
				((FieldDef)curSelectedItem.getData()).setSize(fieldSizeSpinner.getSelection());
			}
		});
		
		fieldSizeSpinner.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					curSelectedItem.setText(SIZE_COL_IDX, Integer.toString(fieldSizeSpinner.getSelection()));
					((FieldDef)curSelectedItem.getData()).setSize(fieldSizeSpinner.getSelection());
					fieldSizeSpinner.setVisible(false);
				}
			}
		});
	}
	
	private void initFieldPrecisionSpinner(){
		fieldPrecisionSpinner = new Spinner(table, SWT.NONE);
		
		fieldPrecisionSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent ev2) {
				curSelectedItem.setText(PRECISION_COL_IDX, Integer.toString(fieldPrecisionSpinner.getSelection()));
				((FieldDef)curSelectedItem.getData()).setPrecision(fieldPrecisionSpinner.getSelection());
			}
		});
		
		fieldPrecisionSpinner.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					curSelectedItem.setText(PRECISION_COL_IDX, Integer.toString(fieldPrecisionSpinner.getSelection()));
					((FieldDef)curSelectedItem.getData()).setPrecision(fieldPrecisionSpinner.getSelection());
					fieldPrecisionSpinner.setVisible(false);
				}
			}
		});
	}
	
	private boolean isPreviousLineFilled(TableItem item){
		TableItem prevItem = null;
		for(int i = 1, size = table.getItemCount(); i < size; i++){
			TableItem cur = table.getItem(i);
			if(cur == item)
				prevItem = table.getItem(i - 1);
		}
		return (prevItem == null || prevItem.getText(NAME_COL_IDX).trim().length() > 0);
	}
	
	private boolean isCurLineFilled(){
		return (curSelectedItem.getText(NAME_COL_IDX).trim().length() > 0 &&
				curSelectedItem.getText(TYPE_COL_IDX).trim().length() > 0);
	}
	
	private void editContentDefDlg(TableDef tableDef, FieldDef fieldDef){
		Shell editContentDefDlg = new Shell(dialog, SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL | SWT.RESIZE);
		new EditContentDefDlg(editContentDefDlg, tableDef, fieldDef, this);
	}
	
	public void populateTable(){
		table.removeAll();
		for(int i = 0, size = tableDef.getFieldNum(); i < size; i++){
			FieldDef fd = tableDef.getFieldDef(i);
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(fd);
			item.setText(NAME_COL_IDX, fd.getName());
			item.setText(TYPE_COL_IDX, FieldDef.sqlTypeToString(fd.getType()));
			int fldSize = fd.getSize();
			if(fldSize != -1)
				item.setText(SIZE_COL_IDX, Integer.toString(fldSize));
			ContentDef cd = fd.getContentDef();
			if(cd != null)
				item.setText(CONTENT_DEF_COL_IDX, cd.toString());
		}
		reinitPrimaryCheckboxes();
	}
	
	private void addPrimaryKeyCheckbox(TableItem item){
		Button primaryKeyCheckbox = new Button(table, SWT.CHECK);
		Rectangle r = item.getBounds(PRIMARY_KEY_COL_IDX);
		primaryKeyCheckbox.setBounds(r.x, r.y, 15, r.height);
		primaryKeyCheckbox.setSelection(false);
		primaryKeyCheckbox.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent ev) {
				int idx = primaryKeyChecks.indexOf(ev.widget);
				TableItem item = table.getItem(idx);
				if(((Button)ev.widget).getSelection()){ // primary key added
					primaryKeys.add((FieldDef)item.getData());
					item.setText(PRIMARY_KEY_COL_IDX, "(" + primaryKeys.size() + ")");
				}
				else{
					primaryKeys.remove(item.getData());
					reinitPrimaryCheckboxes();
				}
			}});
		primaryKeyChecks.add(primaryKeyCheckbox);
	}
	
	private void reinitPrimaryCheckboxes(){
		// remove all text near checkboxes
		for(int i = 0, size = table.getItemCount(); i < size; i++){
			TableItem item = table.getItem(i);
			item.setText(PRIMARY_KEY_COL_IDX, "");
		}
		// remove all checkboxes
		for(int i = 0, size = primaryKeyChecks.size(); i < size; i++){
			primaryKeyChecks.get(i).dispose();
		}
		primaryKeyChecks.removeAllElements();
		
		for(int i = 0, size = table.getItemCount(); i < size; i++){
			addPrimaryKeyCheckbox(table.getItem(i));
		}
		for(int i = 0, size = primaryKeys.size(); i < size; i++){
			FieldDef cur = primaryKeys.get(i);
			for(int j = 0, size2 = table.getItemCount(); j < size2; j++){
				TableItem item = table.getItem(j);
				if(cur.equals(item.getData())){
					primaryKeyChecks.get(j).setSelection(true);
					item.setText(PRIMARY_KEY_COL_IDX, "(" + (i + 1) + ")");
					break;
				}
			}
		}
		reinitCheckboxPositions();
	}

	public TestDataCreator getMainApp() {
		return mainApp;
	}
}
