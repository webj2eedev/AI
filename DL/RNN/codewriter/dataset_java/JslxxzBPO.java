package com.dw.odssu.ws.role.jslxxz;

import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.framework.dbengine.DE;

/**
 * 角色基本信息新增 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-13
 */
public final class JslxxzBPO extends BPO{
	
	/**
	 * 角色基本信息新增
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject saveRoleTypeInfor(DataObject para) throws Exception {
		DE de = DE.getInstance();
		String typeno , typename , minlength , maxlength , allowletter , allowunderline , comments;
		
		typeno = para.getString("typeno");
		typename = para.getString("typename");
		minlength = para.getString("minlength");
		maxlength = para.getString("maxlength");
		allowletter = para.getString("allowletter");
		allowunderline = para.getString("allowunderline");
		comments = para.getString("comments");
		
		if(typeno == null || "".equals(typeno)){
			this.bizException("机构类型编号不能为空！！");
		}
  		de.clearSql();
  		de.addSql(" insert into odssu.role_type( typeno, typename ,  minlength , maxlength ,allowletter , allowunderline , comments ) ");
  		de.addSql("                     values( :typeno , :typename, :minlength, :maxlength , :allowletter , :allowunderline , :comments )");
		de.setString("typeno", typeno);
		de.setString("typename", typename);
		de.setString("minlength", minlength);
		de.setString("maxlength", maxlength);
		de.setString("allowletter", allowletter);
		de.setString("allowunderline", allowunderline);
		de.setString("comments", comments);
		int result = de.update();
		
		if(result == 0){
			this.bizException("新增角色类型信息保存失败！！");
		}
		
		return null;
	}
	/**
	 * 查询角色类型基本信息
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryRoleTypeInfor(DataObject para) throws Exception {
		String typeno = para.getString("typeno");
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select typeno,typename,minlength,maxlength,comments,");
  		de.addSql("        allowletter,allowunderline ");
  		de.addSql(" from odssu.role_type");
  		de.addSql(" where typeno = :typeno ");
		de.setString("typeno", typeno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("roletypeds", vds);
		return vdo;
	}
	
	/**
	 * 保存编辑信息
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject saveRoleTypeEditInfor(DataObject para) throws Exception {
		
		String typeno , typename , minlength , maxlength , allowletter , allowunderline , comments;
		
		typeno = para.getString("typeno");
		typename = para.getString("typename");
		minlength = para.getString("minlength");
		maxlength = para.getString("maxlength");
		allowletter = para.getString("allowletter");
		allowunderline = para.getString("allowunderline");

		comments = para.getString("comments");
		
		if(typeno == null || "".equals(typeno)){
			this.bizException("机构类型编号不能为空！！");
		}
		DE de = DE.getInstance();
 		de.clearSql();
  		de.addSql(" update odssu.role_type set typename = :typename , minlength = :minlength   , maxlength = :maxlength ,");
  		de.addSql("            allowletter = :allowletter , allowunderline = :allowunderline ,  comments = :comments  ");
  		de.addSql("  where typeno = :typeno  ");
		de.setString("typename", typename);
		de.setString("minlength", minlength);
		de.setString("maxlength", maxlength);
		de.setString("allowletter", allowletter);
		de.setString("allowunderline", allowunderline);
		de.setString("comments", comments);
		de.setString("typeno", typeno);
		
		int result = de.update();
		
		if(result == 0){
			this.bizException("更新角色类型信息保存失败！！");
		}
		
		return null;
	}
	
	/**
	 * 检测角色类型能否被删除
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject roleTypeDeleteCheck(DataObject para) throws Exception{
		String typeno , flag = "true" ; 
		DE de = DE.getInstance();

		typeno = para.getString("typeno");
		
		if(typeno == null || "".equals(typeno)){
			this.bizException("角色类型编号为空！");
		}
  		de.clearSql();
  		de.addSql(" select 1 from odssu.ir_org_role_type ");
  		de.addSql(" where roletypeno = :typeno ");
		de.setString("typeno", typeno);
		
		
		DataStore vds = de.query();
		DataObject vdo = DataObject.getInstance();
		
		if(vds.rowCount() == 0){
			flag = "false";
			vdo.put("flag", flag);
			return vdo;
		}
		vdo.put("flag", flag);
		
		return  vdo;
	}
	
	/**
	 * 删除角色类型信息
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject roleTypeDelete(DataObject para) throws Exception{
		String typeno  ; 
		DE de = DE.getInstance();

		typeno = para.getString("typeno");
		
		if(typeno == null || "".equals(typeno)){
			this.bizException("角色类型编号为空！");
		}
  		de.clearSql();
  		de.addSql(" delete from odssu.role_type ");
  		de.addSql(" where typeno = :typeno ");
		de.setString("typeno", typeno);
		
		int result = de.update();
		
		if(result == 0){
			this.bizException("删除失败！！");
		}
		DataObject vdo = DataObject.getInstance();
		
		
		return  vdo;
	}
}
