package name.ovesh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;
import name.ovesh.content.Blob;
import name.ovesh.content.ContentDef;
import name.ovesh.content.CustomContentDef;
import name.ovesh.content.DependentDupReferenceContentDef;
import name.ovesh.content.DupReferenceContentDef;
import name.ovesh.content.OracleSequenceContentDef;
import name.ovesh.content.PostgresSequenceContentDef;
import name.ovesh.content.ReferenceContentDef;
import ml.options.OptionSet;
import ml.options.Options;
import ml.options.Options.Multiplicity;
import ml.options.Options.Separator;
import net.miginfocom.swt.MigLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class TestDataCreator {
	
	private Shell shell;
	private Table table;
	private Button editBtn;
	private Hashtable<String, Vector<Object>> allEnteredReferencedVals = new Hashtable<String, Vector<Object>>();
	private Hashtable<String, TreeSet<String>> allEnteredPrimaryKeyVals = new Hashtable<String, TreeSet<String>>();
	private Hashtable<String, Integer> currentSeqMaxVals = new Hashtable<String, Integer>();
	private Hashtable<String, TableDef> tableDefs = new Hashtable<String, TableDef>();
	@SuppressWarnings("unchecked")
	public static Vector<Class> customContentClasses = new Vector<Class>();
	public static final Random random = new Random();
	private static final int MAX_ATTEMPT_COUNT = 100000;
	private Logger logger;
	private boolean needSave = false;
	private String projFilePath = null;
	private final String appTitle = "Test Data Creator";
	private Vector<Element> referencingFieldEls = new Vector<Element>();
	private Display display = null;
	private ProgressBar progressBar = null;
	private Shell progressDlg = null;
	private int totalRows = 0;
	private int rowsSoFar = 0;
	private static ExportFormat EXPORT_FORMAT = ExportFormat.SQL;
	private static boolean COMMAND_LINE_MODE = false;
	
	public static class ExportFormat{
		private ExportFormat(){}
		
		public static ExportFormat SQL = new ExportFormat();
		public static ExportFormat CTL = new ExportFormat();
	}

	public static void main(String args[]) {
		TestDataCreator myself = new TestDataCreator();
		myself.go(args);
	}
	
	private void go(String args[]){
		initLogger();
		initCustomContentClasses();
		random.setSeed(System.currentTimeMillis());
		
		logger.debug("Starting application");
		if(args.length > 0){
			Options opt = new Options(args, 0);
			OptionSet os = opt.getSet();
			os.addOption("in", Separator.EQUALS, Multiplicity.ONCE);
			os.addOption("out", Separator.EQUALS, Multiplicity.ONCE);
			os.addOption("log", Separator.EQUALS, Multiplicity.ZERO_OR_ONE);
			if(!opt.check(Options.DEFAULT_SET, true, false)){
				System.out.println("USAGE: -in=<input tdc file> -out=<generated file>");
				return;
			}
			if(os.isSet("in") && os.isSet("out")){
				COMMAND_LINE_MODE = true;
				// TODO configure logger with "log" option
				fileOpen(os.getOption("in").getResultValue(0));
				try {
					generateData(os.getOption("out").getResultValue(0));
				} catch (IOException e) {
					logger.error("caught exception", e);
					System.err.println("fatal exception caught: " + e);
				}
				return;
			}
		}
		
		display = new Display();
		shell = new Shell(display);
		Image img = new Image(display, "icon.gif");
		shell.setImage(img);
		MigLayout ml = new MigLayout("nogrid");
		shell.setLayout(ml);
		shell.setText(appTitle);
		shell.addListener(SWT.Close, new Listener(){
			public void handleEvent(Event ev) {
				if(needSave){
					if(!promptSave())
						ev.doit = false;
				}
			}
		});
		
		initOpenBtn();
		initSaveBtn();
		initSaveAsBtn();
		initAddBtn();
		initEditBtn();
		initImportBtn();
		initGenerateBtn();
		initTableList();
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
	private void initLogger() {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(TestDataCreator.class);
	}

	private void initCustomContentClasses() {
		File dir = new File("plugins");
		String[] classFiles = dir.list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.endsWith(".class");
			}});
		URLClassLoader cl = null;
		try {
			cl = new URLClassLoader(new URL[]{dir.toURL()});
		} catch (MalformedURLException e3) {
			TestDataCreator.showErrMsg(shell, "Failed to load custom content classes: " + e3.getMessage());
			logger.error("Failed to load custom content classes", e3);
			return;
		}
		for(int i = 0; i < classFiles.length; i++){
			Class<? extends Object> c = null;
			Object o = null;
			try {
				c = cl.loadClass(classFiles[i].substring(0, classFiles[i].length() - ".class".length()));
				o = c.newInstance();
			} catch (Exception e2) {
				logger.error("Failed to load custom content classes", e2);
				continue;
			}

			if(!CustomContentDef.class.isInstance(o)){
				logger.error("Class " + c.getName() + " does not implement CustomContentDef");
				continue;
			}
			try {
				// see if class has a default no-parameter constructor
				c.getConstructor();
			} catch (Exception e1) {
				// it doesn't have one, so it must implement the private methods readObject() and 
				// writeObject() so it can be serialized (so that projects can be saved)
				try {
					c.getDeclaredMethod("readObject", ObjectInputStream.class);
				} catch (Exception e) {
					logger.error("Class " + c.getName() + 
							" does not have an default constructor, nor does it implement a private function readObject(ObjectInputStream)");
					continue;
				}
				try {
					c.getDeclaredMethod("writeObject", ObjectOutputStream.class);
				} catch (Exception e) {
					logger.error("Class " + c.getName() + 
							" does not have an default constructor, nor does it implement a private function writeObject(ObjectOutputStream)");
					continue;
				}
			}
			try {
				c.getDeclaredMethod("toString");
			} catch (Exception e) {
				logger.error("Class " + c.getName() + " does not override toString()");
				continue;
			}
			
			customContentClasses.add(c);
		}
	}

	private void initOpenBtn() {
		Button btn = new Button (shell, SWT.PUSH);
		btn.setText ("Open");
		btn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				fileOpen();
			}
		});
	}
	
	private void fileOpen(){
		if(needSave){
			if(!promptSave())
				return;
		}
		tableDefs.clear();
		referencingFieldEls.clear();
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setFilterExtensions(new String[]{"*.tdc"});
		fd.open();
		String filePath = fd.getFileName();
		if(filePath == null || filePath.length() == 0)
			return;
		filePath = fd.getFilterPath() + File.separator + filePath;
		fileOpen(filePath);
		setNeedSave(false);
		projFilePath = filePath;
		shell.setText(appTitle + " - " + filePath);
		populateTableList();
	}
	
	private void fileOpen(String filePath){
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filePath);
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(fis);
			Element root = doc.getRootElement();
			List<?> tdEls = root.getChildren("tableDef");
			for(int i = 0, size = tdEls.size(); i < size; i++){
				Element tdEl = (Element)tdEls.get(i);
				TableDef td = tableDefFromXml(tdEl);
				tableDefs.put(td.getName(), td);
			}
			for(int i = 0, size = referencingFieldEls.size(); i < size; i++){
				Element cdEl = referencingFieldEls.get(i);
				Element fieldDefEl = cdEl.getParentElement();
				Element tableDefEl = fieldDefEl.getParentElement();
				TableDef referencingTable = tableDefs.get(tableDefEl.getAttributeValue("name"));
				FieldDef referencingField = referencingTable.getFieldDef(fieldDefEl.getAttributeValue("name"));
				TableDef referencedTable = tableDefs.get(cdEl.getAttributeValue("table"));
				String className = cdEl.getAttributeValue("class");
				if("name.ovesh.content.DupReferenceContentDef".equals(className)) {
					referencingField.setContentDef(new DupReferenceContentDef(referencingTable,referencingField, referencedTable, 
							referencedTable.getFieldDef(cdEl.getAttributeValue("field"))));
				} else if("name.ovesh.content.DependentDupReferenceContentDef".equals(className)){
					FieldDef dependField = referencingTable.getFieldDef(cdEl.getAttributeValue("dependField"));
					FieldDef dependReferenceField = referencedTable.getFieldDef(cdEl.getAttributeValue("dependReferenceField"));
					referencingField.setContentDef(new DependentDupReferenceContentDef(dependField, dependReferenceField, referencingTable, referencingField, referencedTable, 
							referencedTable.getFieldDef(cdEl.getAttributeValue("field"))));
				} else {
					// TODO check if field is referencing itself
					referencingField.setContentDef(new ReferenceContentDef(referencedTable, 
							referencedTable.getFieldDef(cdEl.getAttributeValue("field"))));
				}
			}
			fis.close();
		} catch (Exception e) {
			try {
				fis.close();
			} catch (IOException e1) {}
			showErrMsg(shell, "Error reading from file: " + e.getMessage());
			logger.error("Error reading from file", e);
		}
	}
	
	private TableDef tableDefFromXml(Element tdEl) throws Exception{
		TableDef res = new TableDef();
		res.setName(tdEl.getAttributeValue("name"));
		res.setRequestedRowNum(Integer.parseInt(tdEl.getAttributeValue("requestedRowNum")));
		List<?> fdEls = tdEl.getChildren("fieldDef");
		for(int i = 0, size = fdEls.size(); i < size; i++){
			Element fdEl = (Element)fdEls.get(i);
			res.addFieldDef(fieldDefFromXml(fdEl));
		}
		ArrayList<FieldDef> primaryKeys = new ArrayList<FieldDef>();
		for(int i = 1; true; i++){
			String primaryKeyFieldName = tdEl.getAttributeValue("primaryKey" + i);
			if(primaryKeyFieldName == null)
				break;
			FieldDef primaryKeyField = res.getFieldDef(primaryKeyFieldName);
			if(primaryKeyField == null){
				throw new Exception("Specified primary key (" + primaryKeyFieldName + 
					") not defined for table " + res.getName());
			}
			primaryKeys.add(primaryKeyField);
		}
		res.setPrimaryKeys(primaryKeys);
		
		return res;
	}
	
	private FieldDef fieldDefFromXml(Element fdEl) throws Exception{
		FieldDef res = new FieldDef();
		res.setName(fdEl.getAttributeValue("name"));
		res.setSize(Integer.parseInt(fdEl.getAttributeValue("size")));
		res.setPrecision(Integer.parseInt(fdEl.getAttributeValue("precision")));
		res.setType(Integer.parseInt(fdEl.getAttributeValue("type")));
		res.setReferenced(Boolean.valueOf(fdEl.getAttributeValue("referenced")));
		res.setNullOk(Boolean.valueOf(fdEl.getAttributeValue("nullOk")));
		res.setNullRatio(Integer.parseInt(fdEl.getAttributeValue("nullRatio")));
		Element cdEl = fdEl.getChild("contentDef");
		if(cdEl != null){
			ContentDef cd = contentDefFromXml(cdEl);
			if(cd instanceof CustomContentDef)
				((CustomContentDef)cd).setSize(res.getSize());
			res.setContentDef(cd);
		}
		return res;
	}
	
	private ContentDef contentDefFromXml(Element cdEl) throws Exception{
		String className = cdEl.getAttributeValue("class");
		try {
			Class<?> cdClass = Class.forName(className);
			if(!(ContentDef.class.isAssignableFrom(cdClass)))
				throw new Exception("Specified class type (" +
					cdClass.getName() + ") not ContentDef");
			if(ReferenceContentDef.class.isAssignableFrom(cdClass)){
				referencingFieldEls.add(cdEl);
				return null;
			}
			try {
				return (ContentDef)cdClass.getMethod("fromXml", Element.class).invoke(null, cdEl);
			} catch (Exception e) {
				// attempting argument-less constructor
				return (ContentDef)cdClass.newInstance();
			}
		} catch (Exception e) {
			showErrMsg(shell, "Error reading from file: " + e.getMessage());
			logger.error("Error reading from file", e);
			throw e;
		}
	}
	
	private void setNeedSave(boolean needSaveArg){
		needSave = needSaveArg;
		String shellText = shell.getText();
		if(needSave){
			if(!shellText.endsWith(" *"))
				shell.setText(shell.getText() + " *");
		} else{
			if(shellText.endsWith(" *")){
				shell.setText(shellText.substring(0, shellText.length() - 2));
			}
		}
	}

	// returns true if the user clicked OK
	private boolean promptSave() {
		MessageBox msg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		msg.setMessage("There are unsaved changes that will be discarded. Continue?");
		return (msg.open() == SWT.OK);
	}
	
	private void initSaveAsBtn() {
		Button btn = new Button (shell, SWT.PUSH);
		btn.setText ("Save As");
		btn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				saveAs();
			}});
	}
	
	private void initSaveBtn() {
		Button btn = new Button (shell, SWT.PUSH);
		btn.setText ("Save");
		btn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				if(projFilePath == null)
					saveAs();
				else
					save(projFilePath);
			}
		});
	}
	
	private void saveAs(){
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		fd.setFilterExtensions(new String[]{"*.tdc"});
		fd.open();
		String filePath = fd.getFileName();
		if(filePath == null || filePath.length() == 0)
			return;
		filePath = fd.getFilterPath() + File.separator + filePath;
		
		File file = new File(filePath);
		if(file.exists()){
			MessageBox msg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			msg.setMessage("There selected file will be overwritten. Continue?");
			if(msg.open() == SWT.CANCEL)
				return;
		}
		
		save(filePath);
		projFilePath = filePath;
	}
	
	private void save(String filePath){
		try {
			Document doc = new Document(new Element("tdcProj"));
			Element root = doc.getRootElement();
			for(Iterator<TableDef> it = tableDefs.values().iterator(); it.hasNext();){
				TableDef td = it.next();
				root.addContent(td.toXml());
			}
			FileOutputStream fos = new FileOutputStream(filePath);
			XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
			xo.output(doc, fos);
			fos.close();
			shell.setText(appTitle + " - " + filePath);
			setNeedSave(false);
		} catch (IOException e) {
			showErrMsg(shell, "Error writing to file: " + e.getMessage());
			logger.error("Error writing to file", e);
		}
	}

	private void initAddBtn(){
		Button btn = new Button (shell, SWT.PUSH);
		btn.setText ("Add table");
		btn.setToolTipText("Define a new table structure manually");
		btn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				createTableDefDlg();
			}});
	}
	
	private void initEditBtn(){
		editBtn = new Button (shell, SWT.PUSH);
		editBtn.setText ("Edit");
		editBtn.setToolTipText("Edit the selected table structure");
		editBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				if(table.getSelectionCount() != 1)
					return;
				editTableDef((TableDef)table.getSelection()[0].getData());
			}});
		editBtn.setEnabled(false);
	}
	
	private void initImportBtn(){
		Button btn = new Button (shell, SWT.PUSH);
		btn.setText ("Import");
		btn.setToolTipText("Import table structure from CREATE sql files");
		btn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				File dbFile = null;
				try {
					dbFile = File.createTempFile("tmp.", ".db", new File(System.getProperty("java.io.tmpdir")));
				} catch (Exception e1) {
					logger.error("Could not create temporary file", e1);
					showErrMsg(shell, "Could not create temporary file");
					return;
				}
				String dbName = dbFile.getAbsolutePath();
				runScripts(getScriptFiles(), dbName);
				importTableDefs(dbName);
				dbFile.delete();
				setNeedSave(true);
				populateTableList();
			}});
	}
	
	private void initGenerateBtn(){
		Button btn = new Button (shell, SWT.PUSH);
		btn.setText ("Generate!");
		btn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {
				try {
					generateData();
					allEnteredReferencedVals = new Hashtable<String, Vector<Object>>();
					allEnteredPrimaryKeyVals = new Hashtable<String, TreeSet<String>>();
				} catch (IOException e) {
					showErrMsg(shell, "Error writing to file: " + e.getMessage());
					logger.error("Error writing to file", e);
				}
			}});
		btn.setLayoutData("wrap");
	}

	private void initTableList(){
		table = new Table(shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
//		table.setLinesVisible(true);
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setWidth(400);
		table.addListener(SWT.MouseDoubleClick, new Listener(){
			public void handleEvent(Event event) {
				Point pt = new Point(event.x, event.y);
				TableItem item = table.getItem(pt);
				if (item == null)
					return;
				editTableDef((TableDef)item.getData());
			}
		});
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event ev) {
				editBtn.setEnabled(table.getSelectionCount() != 1);
			}
		});
		table.setLayoutData("width 500, height 400");
	}
	
	
	private void populateTableList(){
		table.removeAll();
		// sort the list by name
		// NOTE: it would be more correct to use TreeMap for tableDefs,
		// but this code keeps backwards-compatiblity with old projects
		TreeSet<String> keys = new TreeSet<String>(tableDefs.keySet());
		for(Iterator<String> it = keys.iterator(); it.hasNext();){
			TableDef tableDef = tableDefs.get(it.next());
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(tableDef);
			item.setText(tableDef.getName());
		}
	}

	private void editTableDef(TableDef tableDef) {
		Shell createTableDlg = new Shell(shell, SWT.RESIZE | SWT.CLOSE | SWT.TITLE | SWT.MAX | SWT.APPLICATION_MODAL);
		new UpdateTableDef(tableDef, createTableDlg, this);
	}
	
	private void createTableDefDlg(){
		Shell createTableDlg = new Shell(shell, SWT.RESIZE | SWT.CLOSE | SWT.TITLE | SWT.MAX | SWT.APPLICATION_MODAL);
		new CreateTableDef(createTableDlg, this);
	}
	
	public void putTable(String oldName, TableDef tableDef){
		String newName = tableDef.getName();
		if(!oldName.equals(newName)){
			tableDefs.remove(oldName);
		}
		tableDefs.put(newName, tableDef);
		setNeedSave(true);
		populateTableList();
	}
	
	public boolean containsTable(String tableName){
		return tableDefs.containsKey(tableName);
	}
	
	public static void showErrMsg(Shell parent, String msg){
		if(COMMAND_LINE_MODE){
			System.err.println(msg);
			return;
		}
		MessageBox errMsg = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK);
		errMsg.setMessage(msg);
		errMsg.open();
	}
	
	private String[] getScriptFiles(){
		FileDialog fd = new FileDialog(shell, SWT.MULTI | SWT.OPEN);
		fd.setFilterExtensions(new String[]{"*.sql"});
		fd.open();
		String[] res = fd.getFileNames();
		if(res == null || res.length == 0)
			return new String[]{};
		String parent = fd.getFilterPath();
		for(int i = 0; i < res.length; i++){
			res[i] = parent + File.separator + res[i];
		}
		return res;
	}
	
	private void runScripts(String[] filePaths, String dbName){
		Runtime runtime = Runtime.getRuntime();
		String cmd = null;
		for(int i = 0; i < filePaths.length; i++){
			Process process = null;
			try {
				cmd = "sqlite3.exe " + dbName + " < " + filePaths[i];
				logger.debug("cmd: " + cmd);
				process = runtime.exec(new String[]{"cmd", "/c", cmd});
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while((line = br.readLine()) != null)
					logger.debug(line);
				br.close();
				isr.close();
				is.close();
				process.waitFor();
			} catch (Exception e) {
				logger.error("Could not run sql scripts.Command: " + cmd, e);
				showErrMsg(shell, "Could not run sql scripts.\nCommand: " + cmd);
				return;
			}
			process.destroy();
		}
	}
	
	private void importTableDefs(String dbName) {
		Process process = null;
		Runtime runtime = Runtime.getRuntime();
		Vector<String> tableNames = new Vector<String>();
		try {
			process = runtime.exec("sqlite3.exe " + dbName);
			logger.debug("cmd: sqlite3.exe " + dbName);
			Class.forName("org.sqlite.JDBC");
			Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbName);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select NAME from SQLITE_MASTER where TYPE='table' order by NAME");
			while(rs.next()){
				tableNames.add(rs.getString("NAME"));
			}
			rs.close();
			stmt.close();
			
			for(int i = 0, size = tableNames.size(); i < size; i++){
				String tableName = tableNames.get(i).toUpperCase();
				if(tableDefs.containsKey(tableName)){
					MessageBox confMsg = new MessageBox(shell, 
							SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					confMsg.setMessage("A table with the name " + tableName + 
							" is already defined. Overwrite?");
					if(confMsg.open() == SWT.CANCEL)
						continue;
				}
				
				stmt = con.createStatement();
				rs = stmt.executeQuery("pragma table_info(" + tableName + ")");
				TableDef curTableDef = new TableDef();
				curTableDef.setName(tableName);
				while(rs.next()){
					FieldDef fd = new FieldDef();
					fd.setName(rs.getString("name"));
					String typeStr = rs.getString("type");
					int idx = -1;
					if((idx = typeStr.indexOf("(")) != -1){
						fd.setType(typeStr.substring(0, idx).trim());
						String insideParenteses = typeStr.substring(idx + 1, typeStr.length() - 1);
						int idx2 = -1;
						if((idx2 = insideParenteses.indexOf(",")) != -1){
							fd.setSize(Integer.parseInt(insideParenteses.substring(0, idx2)));
							fd.setPrecision(Integer.parseInt(insideParenteses.substring(idx2 + 1)));
						} else
							fd.setSize(Integer.parseInt(insideParenteses));
					}
					else
						fd.setType(typeStr);
					curTableDef.addFieldDef(fd);
				}
				rs.close();
				stmt.close();
				tableDefs.put(tableName, curTableDef);
			}
			
			con.close();
			process.destroy();
		} catch (Exception e) {
			logger.error("Could not get table definitions", e);
			showErrMsg(shell, "Could not get table definitions: \n" + e.getMessage());
		}
	}
	
	public TableDef[] getTableDefs(){
		return tableDefs.values().toArray(new TableDef[tableDefs.size()]);
	}
	
	private void generateData() throws IOException {
		FileDialog fileDlg = new FileDialog(shell, SWT.SAVE);
		fileDlg.setFilterExtensions(new String[]{"*.sql", "*.ctl"});
		fileDlg.setFilterNames(new String[]{"SQL insert statements (*.sql)", "Oracle SQL*Loader control file (*.ctl)"});
		fileDlg.open();
		String fileName = fileDlg.getFileName();
		if(fileName == null || fileName.length() == 0)
			return;
		if(fileName.lastIndexOf('.') == -1){
			fileName += ".sql"; // default to sql files
		}
		String filePath = fileDlg.getFilterPath() + File.separator + fileName;
		generateData(filePath);
	}
	
	private void generateData(String filePath) throws IOException{
		boolean isSql = ".sql".equalsIgnoreCase(filePath.substring(filePath.lastIndexOf('.')));
		EXPORT_FORMAT = isSql? ExportFormat.SQL: ExportFormat.CTL;
		File file = new File(filePath);
		if((file.exists() && !file.canWrite()) || (!file.exists() && !file.getParentFile().canWrite())){
			TestDataCreator.showErrMsg(shell, "Cannot write to file: " + file.getAbsolutePath());
			return;
		}
		currentSeqMaxVals = new Hashtable<String, Integer>();
		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter fw = new OutputStreamWriter(fos, "SJIS");

		addProgressBar();
		Vector<TableDef> tableQueue =  createTableQueue();
		// generate data for queue
		if(isSql){
			for(int i = 1; tableQueue.size() > 0; i++){
				generateDataSql(tableQueue.remove(0), fw);
			}
		} else{
			DecimalFormat df = getDecFormat();
			writeControlDefinition(fw, tableQueue, df);
			for(int i = 1; tableQueue.size() > 0; i++){
				generateDataCtl(tableQueue.remove(0), fw, df.format(i));
			}
		}
		
		disposeProgressBar();
		fw.close();
		showErrMsg(shell, "Date Generation complete!");
	}
	
	private Vector<TableDef> createTableQueue(){
		Vector<TableDef> res = new Vector<TableDef>();
		while(res.size() < tableDefs.size()){
			for(Enumeration<TableDef> en = tableDefs.elements(); en.hasMoreElements(); ){
				TableDef td = en.nextElement();
				if(res.contains(td))
					continue;
				boolean hasMissingReference = false;
				for(int i = 0, size = td.getFieldNum(); i < size; i++){
					FieldDef fd = td.getFieldDef(i);
					if(fd.getContentDef() instanceof ReferenceContentDef &&
							!res.contains(((ReferenceContentDef)fd.getContentDef()).getTableDef())){
						hasMissingReference = true;
						logger.debug("table "+td.getName()+" is missing reference " + ((ReferenceContentDef)fd.getContentDef()).getTableDef().getName()+"."+((ReferenceContentDef)fd.getContentDef()).getFieldDef().getName());
						break;
					}
				}
				if(hasMissingReference)
					continue;
				res.add(td);
			}
		}
		
		return res;
	}
	
	private Shell addProgressBar(){
		if(!COMMAND_LINE_MODE){
			// TODO add cancel button (same as "close")
			progressDlg = new Shell(shell, SWT.TITLE | SWT.APPLICATION_MODAL);
			Image img = new Image(display, "blank.gif");
			progressDlg.setImage(img);
			progressDlg.setSize(200, 100);
			progressDlg.setText("Progress");
			progressBar = new ProgressBar(progressDlg, SWT.SMOOTH | SWT.HORIZONTAL);
			progressBar.setBounds(20, 40, 160, 20);
			progressBar.setMinimum(0);
			totalRows = 0;
			for(Enumeration<TableDef> en = tableDefs.elements(); en.hasMoreElements(); ){
				totalRows += en.nextElement().getRequestedRowNum();
			}
			progressBar.setMaximum(totalRows);
			rowsSoFar = 0;
			
			progressDlg.open();
			return progressDlg;
		}
		
		System.out.print("+");
		return null;
	}
	
	private void updateProgressBar(){
		if(!COMMAND_LINE_MODE){
			progressBar.setSelection(rowsSoFar);
			return;
		}
		
		System.out.print(((rowsSoFar % 50 == 0)? "\r\n": "") + "+");
	}

	
	private void disposeProgressBar(){
		if(!COMMAND_LINE_MODE){
			progressDlg.dispose();
			return;
		}
		
		System.out.println("");
	}

	private DecimalFormat getDecFormat(){
		String format = "";
		for(int i = 0, digNum = Integer.toString(tableDefs.size()).length(); i < digNum; i++)
			format += "0";
		return new DecimalFormat(format);
	}
	
	private void writeControlDefinition(OutputStreamWriter fw, Vector<TableDef> finishedTables, DecimalFormat df) throws IOException{
		fw.write("options(errors=99999999999999) \n");
		fw.write("load data infile * \n");
		for(int j = 0, size = finishedTables.size(); j < size; j++){
			TableDef td = finishedTables.get(j);
			String tableName = td.getName();
			fw.write("into table " + tableName + " when tab='tab" + df.format(j + 1) + "'\n");
			fw.write("fields terminated by \",\" optionally enclosed by '\\'' \n");
			fw.write("(tab filler position(1),");
			FieldDef[] fields = td.getFields();
			orderFields(fields);
			for(int i = 0; i < fields.length; i++){
				fw.write(fields[i].getName());
				int type = fields[i].getType();
				ContentDef cd = fields[i].getContentDef();
				if(type == Types.CHAR || type == Types.LONGVARCHAR || type == Types.VARCHAR || type == Types.CLOB){
					if(fields[i].getSize() > 100){
						fw.write(" char(" + (fields[i].getSize() * 2) + ")");
					}
					fw.write(" \"replace(replace(:" + fields[i].getName() + 
							", '\\r\\n', chr(10)), '\\,', chr(44))\"");
				}
				else if(type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP){
					fw.write(" date \"YYYY/MM/DD\"");
				}
				else if(type == Types.BLOB && cd instanceof Blob){
					fw.write(" LOBFILE (" + ((Blob)cd).getFileNameField() + ") TERMINATED BY EOF");
				}
				if(cd instanceof OracleSequenceContentDef || cd instanceof PostgresSequenceContentDef){
					fw.write(" \"" + cd.generateContent() + "\"");
				}
				if(i < fields.length - 1)
					fw.write(",");
			}
			fw.write(") \n");
		}
		fw.write("begindata \n");
	}
	
	private void generateDataSql(TableDef td, OutputStreamWriter fw) throws IOException{
		String tableName = td.getName();
		FieldDef[] fields = td.getFields();
		orderFields(fields);
		String fieldList = "";
		for(int i = 0; i < fields.length; i++){
			fieldList += fields[i].getName();
			if(i < fields.length - 1)
				fieldList += ",";
		}
		for(int j = 0; j < fields.length; j++){
			if(fields[j].isReferenced()){
				String key = td.getName() + "." + fields[j].getName();
				if(!allEnteredReferencedVals.containsKey(key)){
					allEnteredReferencedVals.put(key, new Vector<Object>());
				}
			}
		}
		for(int i = 0, total = td.getRequestedRowNum(); i < total; i++){
			HashMap<FieldDef, String> row = attemptDataGeneration(fields, td, i);
			if(row == null) // given up trying to generate more data
				return;
			fw.write("insert into " + tableName + " (" + fieldList + ") values (");
			for(int j = 0; j < fields.length; j++){
				String data = row.get(fields[j]);
				if(data == null)
					fw.write("null");
				else{
					int type = fields[j].getType();
					boolean needsQuote = (type == Types.CHAR || type == Types.LONGVARCHAR || type == Types.VARCHAR || type == Types.CLOB);
					boolean isVeryLong = fields[j].getSize() > 100;
					if(needsQuote){
						if(isVeryLong) fw.write("\n");
						if(needsQuote) fw.write("'");
						
						int maxLineSize = 700;
						while(data.length() > maxLineSize){
							String curData = data.substring(0, maxLineSize);
							curData = curData.replaceAll("\\r\\n", "'  || CHR(13) || CHR(10) || '")
							.replaceAll("\\r", "'  || CHR(13) || '")
							.replaceAll("\\n", "'  || CHR(10) || '");
							fw.write(curData + "'||\n'");
							data = data.substring(maxLineSize);
						}
						data = data.replaceAll("\\r\\n", "'  || CHR(13) || CHR(10) || '")
						.replaceAll("\\r", "'  || CHR(13) || '")
						.replaceAll("\\n", "'  || CHR(10) || '");
					}
					fw.write(data);
					
					if(fields[j].isReferenced()){
						String key = td.getName() + "." + fields[j].getName();
						ContentDef cd = fields[j].getContentDef();
						if(cd instanceof OracleSequenceContentDef || cd instanceof PostgresSequenceContentDef){
							Vector<Object> set = allEnteredReferencedVals.get(key);
							set.add(set.size() > 0? (Integer)set.lastElement() + 1: 1);
						} else{
							allEnteredReferencedVals.get(key).add(data);
						}
					}
					if(needsQuote) fw.write("'");
					if(isVeryLong) fw.write("\n");
				}
				if(j < fields.length - 1) fw.write(",");
			}
			fw.write(");\n");
			if(i % 1000 == 0)
				fw.write("commit;\n");
			
			rowsSoFar++;
			updateProgressBar();
		}
		fw.write("\n\n\n\n");
		fw.write("commit;\n");
	}
	
	private void generateDataCtl(TableDef td, OutputStreamWriter fw, String tblIdx) throws IOException{
		System.out.println("\r\nStarting table " + td.getName());
		FieldDef[] fields = td.getFields();
		orderFields(fields);
		for(int j = 0; j < fields.length; j++){
			if(fields[j].isReferenced()){
				String key = td.getName() + "." + fields[j].getName();
				if(!allEnteredReferencedVals.containsKey(key)){
					allEnteredReferencedVals.put(key, new Vector<Object>());
				}
			}
		}
		for(int i = 0, total = td.getRequestedRowNum(); i < total; i++){
			HashMap<FieldDef, String> row = attemptDataGeneration(fields, td, i);
			if(row == null) // given up trying to generate more data
				return;
			fw.write("tab" + tblIdx + ",");
			for(int j = 0; j < fields.length; j++){
				if(fields[j].getType() == Types.BLOB && fields[j].getContentDef() instanceof Blob){
					continue; // skip the comma
				}
				String data = row.get(fields[j]);
				if(data == null)
					fw.write("");
				else{
					int type = fields[j].getType();
					boolean needsQuote = (type == Types.CHAR || type == Types.LONGVARCHAR || type == Types.VARCHAR || type == Types.CLOB);
					if(needsQuote) fw.write("'");
					
					data = data.replaceAll("\\r\\n|\\r|\\n", "\\\\r\\\\n")
					.replaceAll(",", "\\\\,");
					ContentDef cd = fields[j].getContentDef();
					if(!(cd instanceof OracleSequenceContentDef) && !(cd instanceof PostgresSequenceContentDef))
						fw.write(data);
					
					if(fields[j].isReferenced()){
						String key = td.getName() + "." + fields[j].getName();
						if(cd instanceof OracleSequenceContentDef || cd instanceof PostgresSequenceContentDef){
							Vector<Object> set = allEnteredReferencedVals.get(key);
							set.add(set.size() > 0? (Integer)set.lastElement() + 1: 1);
						} else{
							allEnteredReferencedVals.get(key).add(data);
						}
					}
					if(needsQuote) fw.write("'");
				}
				if(j < fields.length - 1) fw.write(",");
			}
			fw.write("\n");
			
			rowsSoFar++;
			updateProgressBar();
		}
		System.out.println("\r\nFinished table " + td.getName());
	}
	
	private void orderFields(FieldDef[] fields) {
		int j = fields.length - 1;
		for(int i = fields.length -1 ; i >= 0 ; i--) {
			if(fields[i].getContentDef() instanceof DependentDupReferenceContentDef) {
				FieldDef tmp = fields[i];
				fields[i] = fields[j];
				fields[j] = tmp;
				j--;
			}
		}
	}
	
	private HashMap<FieldDef, String> attemptDataGeneration(FieldDef[] fields, TableDef tabledef, int currentRow){
		if("T_INFORMATION".equals(tabledef.getName())){
			logger.debug("currentRow="+currentRow);
		}
		ArrayList<FieldDef> primaryKeys = tabledef.getPrimaryKeys();
		for(int i = 0; i < MAX_ATTEMPT_COUNT; i++){
			if("T_INFORMATION".equals(tabledef.getName())){
				logger.debug("attempt="+i);
			}
			HashMap<FieldDef, String> row = new HashMap<FieldDef, String>();
			for(int j = 0; j < fields.length; j++){
				row.put(fields[j], null);
				if(fields[j].isNullOk() && random.nextInt(100) < fields[j].getNullRatio()){
					row.put(fields[j], null);
				} 
				else{
					ContentDef cd = fields[j].getContentDef();
					Object data = null;
					if(cd == null) {
						data = "";
					} else if(cd instanceof DependentDupReferenceContentDef) {
						//get value of the column this column is dependent on
						FieldDef tmp = ((DependentDupReferenceContentDef)cd).getDependFieldDef();
						int k = 0;
						while(k < fields.length && !tmp.getName().equals(fields[k].getName())) k++;
						String dependData = row.get(fields[k]);
						data = getReferencedContent((DependentDupReferenceContentDef)cd, dependData); 
					} else if(cd instanceof ReferenceContentDef) {
						data = getReferencedContent((ReferenceContentDef)cd); 
					} else {
						data = cd.generateContent();						
					}
					row.put(fields[j], data.toString());
				}
			}
			if(primaryKeys.size() == 0) // table with no primary keys
				return row;
			
			StringBuffer primaryKeyVal = new StringBuffer();
			for(int j = 0, size = primaryKeys.size(); j < size; j++){
				FieldDef fd = primaryKeys.get(j);
				ContentDef cd = fd.getContentDef();
				if(cd instanceof OracleSequenceContentDef || cd instanceof PostgresSequenceContentDef){
					Integer curMaxSeqVal = currentSeqMaxVals.get(tabledef.getName());
					if(curMaxSeqVal == null){
						curMaxSeqVal = 1;
					}
					primaryKeyVal.append(curMaxSeqVal);
					currentSeqMaxVals.put(tabledef.getName(), curMaxSeqVal + 1);
				}
				else
					primaryKeyVal.append(row.get(fd));
				if(j < size - 1)
					primaryKeyVal.append("_");
			}
			TreeSet<String> enteredPrimaryKeyVals = allEnteredPrimaryKeyVals.get(tabledef.getName());
			if(enteredPrimaryKeyVals == null){
				enteredPrimaryKeyVals = new TreeSet<String>();
				allEnteredPrimaryKeyVals.put(tabledef.getName(), enteredPrimaryKeyVals);
			}
			// primary key was already used, try again
			if(enteredPrimaryKeyVals.contains(primaryKeyVal.toString()))
				continue;
			// found unused primary key
			enteredPrimaryKeyVals.add(primaryKeyVal.toString());
			
			return row;
		}
//		MessageBox errMsg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
//		errMsg.setMessage("Could not generate new primary key for \ntable " + 
//				tabledef.getName() + " row number " + currentRow + " after " + 
//				MAX_ATTEMPT_COUNT + " tries. \n" +
//						"Try again?");
//		if(errMsg.open() == SWT.YES)
//			return attemptDataGeneration(fields, tabledef, currentRow);
		return null;
	}
	
	private Object getReferencedContent(ReferenceContentDef cd){
		Vector<Object> enteredVals = 
			allEnteredReferencedVals.get(cd.getTableDef().getName() + "." + cd.getFieldDef().getName());
		assert(enteredVals != null);
		if(enteredVals == null){
			System.err.println("could not find key " + 
					cd.getTableDef().getName() + "." + cd.getFieldDef().getName() + " in allEnteredReferencedVals. " +
							"Did you forget to set referenced=\"true\"?");
			logger.error("enteredVals == null!");
		}
		int idx;
		if(cd instanceof DupReferenceContentDef) {
			String key = ((DupReferenceContentDef)cd).getDestTableDef().getName() + "." + ((DupReferenceContentDef)cd).getDestFieldDef().getName() + "." + cd.getTableDef().getName() + "." + cd.getFieldDef().getName();
			idx = DupReferenceContentDef.copyIndex.get(key);
			DupReferenceContentDef.copyIndex.put(key, idx+1);
		} else {
			idx = random.nextInt(enteredVals.size());
		}

		return enteredVals.get(idx % enteredVals.size());
	}
	
	private Object getReferencedContent(DependentDupReferenceContentDef cd, String dependData){
		Vector<Object> enteredVals = 
			allEnteredReferencedVals.get(cd.getTableDef().getName() + "." + cd.getFieldDef().getName());
		assert(enteredVals != null);
		if(enteredVals == null){
			System.err.println("could not find key " + 
					cd.getTableDef().getName() + "." + cd.getFieldDef().getName() + " in allEnteredReferencedVals. " +
							"Did you forget to set referenced=\"true\"?");
			logger.error("enteredVals == null!");
		}
		String key = ((DupReferenceContentDef)cd).getDestTableDef().getName() + "." + ((DupReferenceContentDef)cd).getDestFieldDef().getName() + "." + cd.getTableDef().getName() + "." + cd.getFieldDef().getName();
		int idx = DupReferenceContentDef.copyIndex.get(key);
		DupReferenceContentDef.copyIndex.put(key, idx+1);

		FieldDef dependRefField = cd.getDependReferenceFieldDef();
		Vector<Object> tmp = allEnteredReferencedVals.get(cd.getTableDef().getName() + "." + dependRefField.getName());

		Vector<Object> resultPool = new Vector<Object>();
		Iterator<Object> it = tmp.iterator();
		int i = 0;
		while(it.hasNext()) {
			Object curr = it.next(); 
			if(curr.toString().equals(dependData))
				resultPool.add(enteredVals.get(i));
			i++;
		}
		
		return resultPool.get(idx % resultPool.size());
	}
	
	public static ExportFormat getExportFormat(){
		return EXPORT_FORMAT;
	}
}
