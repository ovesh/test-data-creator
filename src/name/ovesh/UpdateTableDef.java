package name.ovesh;

import java.util.ArrayList;
import org.eclipse.swt.widgets.Shell;

public class UpdateTableDef extends CreateTableDef {

	@SuppressWarnings("unchecked")
	public UpdateTableDef(TableDef tableDef, Shell dialog, TestDataCreator mainApp) {
		super(dialog, mainApp);
		
		this.tableDef = tableDef;
		primaryKeys = (ArrayList<FieldDef>)tableDef.getPrimaryKeys().clone();
		
		dialog.setText("Edit Table Definition");
		tableNameTxt.setText(tableDef.getName());
		requestedRowNumSpinner.setSelection(tableDef.getRequestedRowNum());
		populateTable();
	}

}
