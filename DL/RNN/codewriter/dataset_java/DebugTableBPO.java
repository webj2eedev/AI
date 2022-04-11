package com.dw.pub.debugtable;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.framework.dbengine.DE;

public class DebugTableBPO extends BPO{
	public DataObject empdebug(DataObject para) throws AppException {
		String empno = para.getString("empno");
		String table = para.getString("table");
		
		DE de = DE.getInstance();				//lzpmark
  		de.clearSql();
  		de.addSql(" select * ");
  		de.addSql("   from odssu."+table);
  		de.addSql(" where empno = :empno ");
		de.setString("empno", empno);
		DataStore tableInfo = de.query();
		
    	de.clearSql();
    	de.addSql("  select data_type, column_name from sys.dba_tab_columns ");
    	de.addSql(" where table_name=:table and owner=:owner    order by column_id  ");
		de.setString("table", table);
		de.setString("owner", "ODSSU");
		DataStore tableStructure = de.query();

		DataObject result = DataObject.getInstance();
		result.put("tableinfo", tableInfo);
		result.put("tablestructure", tableStructure);
		return result;
	}

	public DataObject orgdebug(DataObject para) throws AppException {
		String orgno = para.getString("orgno");
		String table = para.getString("table");
		
		DE de = DE.getInstance();			//lzpmark
  		de.clearSql();
  		de.addSql(" select * ");
  		de.addSql("   from odssu."+table);
  		de.addSql(" where orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore tableInfo = de.query();
    	
		de.clearSql();
    	de.addSql("  select data_type, column_name from sys.dba_tab_columns ");
    	de.addSql(" where table_name=:table and owner=:owner    order by column_id  ");
		de.setString("table", table);
		de.setString("owner", "ODSSU");
		DataStore tableStructure = de.query();

		DataObject result = DataObject.getInstance();
		result.put("tableinfo", tableInfo);
		result.put("tablestructure", tableStructure);
		return result;
	}
	
	public DataObject outdutyFace(DataObject para) throws AppException {
		String orgno = para.getString("orgno");
		String table = para.getString("table");
		
		DE de = DE.getInstance();				//lzpmark
  		de.clearSql();
  		de.addSql(" select * ");
  		de.addSql("   from odssu."+table);
  		de.addSql(" where faceorgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore tableInfo = de.query();
		
    	de.clearSql();
    	de.addSql("  select data_type, column_name from sys.dba_tab_columns ");
    	de.addSql(" where table_name=:table and owner=:owner    order by column_id  ");
		de.setString("table", table);
		de.setString("owner", "ODSSU");
		DataStore tableStructure = de.query();

		DataObject result = DataObject.getInstance();
		result.put("tableinfo", tableInfo);
		result.put("tablestructure", tableStructure);
		return result;
	}
	
	public DataObject outdutyIn(DataObject para) throws AppException {
		String orgno = para.getString("orgno");
		String table = para.getString("table");
		
		DE de = DE.getInstance();			//lzpmark
  		de.clearSql();
  		de.addSql(" select * ");
  		de.addSql("   from odssu."+table);
  		de.addSql(" where inorgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore tableInfo = de.query();
		
    	de.clearSql();
    	de.addSql("  select data_type, column_name from sys.dba_tab_columns ");
    	de.addSql(" where table_name=:table and owner=:owner    order by column_id  ");
		de.setString("table", table);
		de.setString("owner", "ODSSU");
		DataStore tableStructure = de.query();

		DataObject result = DataObject.getInstance();
		result.put("tableinfo", tableInfo);
		result.put("tablestructure", tableStructure);
		return result;
	}
	
	public DataObject roleinforInOrg(DataObject para) throws AppException {
		String orgno = para.getString("orgno");
		String table = para.getString("table");
		
		DE de = DE.getInstance();			//lzpmark
  		de.clearSql();
  		de.addSql(" select * ");
  		de.addSql("   from odssu."+table);
  		de.addSql(" where deforgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore tableInfo = de.query();
		
		de.clearSql();
    	de.addSql("  select data_type, column_name from sys.dba_tab_columns ");
    	de.addSql(" where table_name=:table and owner=:owner    order by column_id  ");
		de.setString("table", table);
		de.setString("owner", "ODSSU");
		DataStore tableStructure = de.query();

		DataObject result = DataObject.getInstance();
		result.put("tableinfo", tableInfo);
		result.put("tablestructure", tableStructure);
		return result;
	}
}
