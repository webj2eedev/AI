package com.dw.role.rolefolder;

import org.apache.commons.lang3.StringUtils;

import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.log.LogHandler;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.ClosureUtil;

public class RoleFolderBPO extends BPO{

	/**
	 * 方法简介：保存新增目录 赵伟华  2020-01-13
	 */
	public DataObject saveAddTreeNode(DataObject para) throws Exception{
		String pfolderid = para.getString("pfolderid");
		String folderlabel = para.getString("folderlabel");
		String deforgno = "", collect_query = "0";
		String folderid = de.getNextVal("ODSSU.SEQ_ROLE_FOLDER");

		//根据pfolderid查询deforgno
  		de.clearSql();
  		de.addSql(" select deforgno ");
  		de.addSql(" from odssu.role_folder ");
  		de.addSql(" where folderid = :folderid ");
  		de.setString("folderid", pfolderid);
		DataStore deforgnods = de.query();
		if(deforgnods.rowCount() > 0) {
			deforgno = deforgnods.getString(0, "deforgno");
		}else {
			LogHandler.log("未获取到父目录["+pfolderid+"]对应的deforgno，请检查");
		}
		
		de.clearSql();
  		de.addSql(" insert into odssu.role_folder (pfolderid, folderid, folderlabel, deforgno, collect_query) ");
  		de.addSql(" values (:pfolderid, :folderid, :folderlabel, :deforgno, :collect_query) ");
		de.setString("pfolderid",pfolderid);
		de.setString("folderid",folderid);
		de.setString("folderlabel",folderlabel);
		de.setString("deforgno",deforgno);
		de.setString("collect_query",collect_query);
		de.update();
		
		de.clearSql();
		de.addSql(" insert into odssu.ir_rolefolder_closure (folderid,pfolderid)");
		de.addSql(" values (:folderid,:folderid)");
		de.setString("folderid", folderid);
		de.update();
		
		de.clearSql();
		de.addSql(" insert into odssu.ir_rolefolder_closure (folderid,pfolderid) ");
		de.addSql(" select :folderid , a.pfolderid ");
		de.addSql("   from odssu.ir_rolefolder_closure a ");
		de.addSql("  where a.folderid = :pfolderid ");
		de.setString("folderid", folderid);
		de.setString("pfolderid", pfolderid);
		de.update();
		
		de.clearSql();
		de.addSql(" insert into odssu.emp_rolefolder(empno,folderid,operatorauth,approvalauth) ");
		de.addSql(" values (:empno, :folderid,'1','1') ");
		de.setString("empno", this.getUser().getUserid());
		de.setString("folderid", folderid);
		de.update();
		
		return DataObject.getInstance();
	}
	
	/**
	 * 方法简介：查询修改目录 赵伟华  2020-01-13
	 */
	public DataObject queryModTreeNode(DataObject para) throws Exception{
		String folderid = para.getString("folderid");
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select folderid, folderlabel ");
  		de.addSql(" from odssu.role_folder ");
  		de.addSql(" where folderid = :folderid ");
  		de.setString("folderid", folderid);
		DataStore folderds = de.query();
		if(folderds == null || folderds.rowCount() ==0) {
			throw new BusinessException("角色目录编号【"+folderid+"】无效！");
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("folderds", folderds);
		return vdo;
	}
	/**
	 * 方法简介：保存修改目录 赵伟华  2020-01-13
	 */
	public DataObject saveModTreeNode(DataObject para) throws Exception{
		String folderid = para.getString("folderid");
		String newfolderlabel = para.getString("newfolderlabel");

		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" update odssu.role_folder set folderlabel = :newfolderlabel ");
  		de.addSql(" where folderid = :folderid ");
		de.setString("newfolderlabel",newfolderlabel);
		de.setString("folderid",folderid);
		de.update();
		
		return DataObject.getInstance();
	}
	
	/**
	 * 方法简介：检验是否可以删除目录 赵伟华  2020-01-13
	 */
	public DataObject checkDelTreeNode(DataObject para) throws AppException, BusinessException {
		String folderid = para.getString("folderid");
		if (folderid == null || folderid.equals("")) {
			bizException("传入的folderid为空，请检查");
		}
		
		String passkey = "yes";
		DE de = DE.getInstance();
		//校验是否为根目录
  		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql(" from odssu.role_folder ");
  		de.addSql(" where pfolderid = :pfolderid and collect_query = '1' ");
  		de.setString("pfolderid", folderid);
		DataStore rootds = de.query();
		if(rootds.rowCount() > 0) {
			passkey = "root";
		}else {
			//校验是否存在下级目录
	  		de.clearSql();
	  		de.addSql(" select 1 ");
	  		de.addSql(" from odssu.role_folder ");
	  		de.addSql(" where pfolderid = :pfolderid ");
	  		de.setString("pfolderid", folderid);
			DataStore pfolderds = de.query();
			if(pfolderds.rowCount() > 0) {
				passkey = "xjml";
			}else {
				//检验是否存在角色
		  		de.clearSql();
		  		de.addSql(" select 1 ");
		  		de.addSql("   from odssu.roleinfor ");
		  		de.addSql("  where folderno = :folderno ");
		  		de.addSql("    and sleepflag = '0' ");
		  		de.setString("folderno", folderid);
				DataStore roleds = de.query();
				if(roleds.rowCount() > 0) {
					passkey = "js";
				}
			}
		}
		
		DataObject result = DataObject.getInstance();
		result.put("passkey", passkey);
		return result;
	}
	/**
	 * 方法简介：删除目录 赵伟华  2020-01-13
	 */
	public DataObject delTreeNode(DataObject para) throws Exception{
		String folderid = para.getString("folderid");

		DE de = DE.getInstance();
		
		de.clearSql();
  		de.addSql(" delete from odssu.role_folder ");
  		de.addSql("  where folderid = :folderid ");
		de.setString("folderid",folderid);
		de.update();
		
		de.clearSql();
		de.addSql("delete from odssu.emp_rolefolder ");
		de.addSql(" where folderid = :folderid ");
		de.setString("folderid",folderid);
		de.update();
		
		de.clearSql();
		de.addSql(" delete from odssu.ir_rolefolder_closure ");
		de.addSql("  where folderid = :folderid ");
		de.setString("folderid",folderid);
		de.update();
		
		return DataObject.getInstance();
	}
	public DataObject chooseEmpAddFolderAuth(DataObject para) throws Exception{
		String searchkey = para.getString("searchkey");
		
		if(StringUtils.isBlank(searchkey)){
			searchkey = "%";
		}else {
			searchkey = "%"+searchkey.toUpperCase()+"%";
		}
		
		
		de.clearSql();
		de.addSql(" select a.empno , a.empname , a.loginname ,b.orgname ");
		de.addSql("   from odssu.empinfor a , odssu.orginfor b ");
		de.addSql("  where a.sleepflag = '0' ");
		de.addSql("    and a.hrbelong = b.orgno ");
		de.addSql("    and (a.empno like :searchkey or a.loginname like :searchkey ");
		de.addSql("     or a.loginname like :searchkey or a.empname like :searchkey )");
		de.setString("searchkey",searchkey);
		DataStore empds = de.query();
	
		DataObject result = DataObject.getInstance();
		result.put("empds", empds);
		
		return result;
	}
	
	/**
	 * 方法简介：查看权限 赵伟华  2020-01-13
	 */
	public DataObject queryAuthority(DataObject para) throws Exception{
		String folderid = para.getString("folderid");
		
		DE de = DE.getInstance();
		
  		de.clearSql();
  		de.addSql(" select a.empno, a.loginname ,a.empname , '从上级目录继承的权限' authorigin ,");
  		de.addSql("        b.operatorauth, b.approvalauth ,'权限说明' showdescription  ");
  		de.addSql("   from odssu.empinfor a , odssu.emp_rolefolder b ");
  		de.addSql("  where a.empno = b.empno ");
  		de.addSql("    and b.folderid in (select c.pfolderid from odssu.ir_rolefolder_closure c  ");
  		de.addSql("                        where c.folderid = :folderid      ");
  		de.addSql("                          and c.pfolderid <> :folderid ) ");
  		de.setString("folderid", folderid);
		DataStore superempds = de.query();
		
		de.clearSql();
		de.addSql(" select a.empno , a.loginname , a.empname , '对本目录的权限' authorigin ,");
		de.addSql("        b.operatorauth, b.approvalauth , '权限说明' showdescription");
		de.addSql("   from odssu.empinfor a , odssu.emp_rolefolder b ");
		de.addSql("  where a.empno = b.empno ");
		de.addSql("    and b.folderid = :folderid ");
		de.setString("folderid", folderid);
		DataStore empds = de.query();
		
		superempds = dealEmpAuth(superempds);
		superempds.combineDatastore(empds);

		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", superempds);
		return vdo;
	}
	private DataStore dealEmpAuth(DataStore para) throws AppException {
		
		DataStore resultds = DataStore.getInstance();
		
		for(int i = 0 ; i < para.rowCount() ; i++) {
			
			DataObject otherauth = para.get(i);
			String empno = otherauth.getString("empno");
			int rownum = resultds.find("empno == "+empno );
			
			if(rownum> -1) {
				
				DataObject currentauth = resultds.get(rownum);
				
				if("0".equals(currentauth.getString("operatorauth"))&&"1".equals(otherauth.getString("operatorauth"))) {
					currentauth.put("operatorauth", "1");
					continue;
				}
				if("0".equals(currentauth.getString("approvalauth"))&&"1".equals(otherauth.getString("approvalauth"))) {
					currentauth.put("approvalauth", "1");
				}
				
			}else {
				resultds.addRow(otherauth);
			}
			
			
		}
		
		return resultds;
		
	}
	public DataObject addOperaterAuth(DataObject para) throws AppException, BusinessException {
		
		String userid = this.getUser().getUserid();
		String empno = para.getString("empno");
		String folderid = para.getString("folderid");
		
		if(!hasRoleFolderApprovalAuth(userid,folderid)) {
			throw new BusinessException("没有权限！");
		}
		de.clearSql();
		de.addSql("select operatorauth from odssu.emp_rolefolder where empno = :empno and folderid = :folderid ");
		de.setString("empno", empno);
		de.setString("folderid", folderid);
		DataStore empds = de.query();
		
		if(empds == null || empds.rowCount() == 0 ) {
			de.clearSql();
			de.addSql(" insert into odssu.emp_rolefolder (empno,folderid,operatorauth)");
			de.addSql(" values (:empno,:folderid , '1' )");
			de.setString("empno", empno);
			de.setString("folderid", folderid);
			de.update();
			
			return null;
		}
		String operatorauth = empds.getString(0, "operatorauth");
		
		if(operatorauth.equals("1")) {
			return null;
		}
		
		de.clearSql();
		de.addSql(" update odssu.emp_rolefolder set operatorauth = '1' ");
		de.addSql("  where empno = :empno and folderid = :folderid ");
		de.setString("empno", empno);
		de.setString("folderid", folderid);
		de.update();
		
		
		return null;
	}
	

	public DataObject addApprovalAuth(DataObject para) throws AppException, BusinessException {
		
		String userid = this.getUser().getUserid();
		String empno = para.getString("empno");
		String folderid = para.getString("folderid");
		
		if(!hasRoleFolderApprovalAuth(userid,folderid)) {
			throw new BusinessException("没有权限！");
		}
		de.clearSql();
		de.addSql("select approvalauth from odssu.emp_rolefolder where empno = :empno and folderid = :folderid ");
		de.setString("empno", empno);
		de.setString("folderid", folderid);
		DataStore empds = de.query();
		
		if(empds == null || empds.rowCount() == 0 ) {
			de.clearSql();
			de.addSql(" insert into odssu.emp_rolefolder (empno,folderid,approvalauth)");
			de.addSql(" values (:empno,:folderid , '1' )");
			de.setString("empno", empno);
			de.setString("folderid", folderid);
			de.update();
			
			return null;
		}
		String approvalauth = empds.getString(0, "approvalauth");
		
		if(approvalauth.equals("1")) {
			return null;
		}
		
		de.clearSql();
		de.addSql(" update odssu.emp_rolefolder set approvalauth = '1' ");
		de.addSql("  where empno = :empno and folderid = :folderid ");
		de.setString("empno", empno);
		de.setString("folderid", folderid);
		de.update();
		
		
		return null;
	}
	public DataObject revokeOperaterAuth(DataObject para) throws AppException, BusinessException {
		
		String userid = this.getUser().getUserid();
		String folderid = para.getString("folderid");
		String empno = para.getString("empno");
		
		if(!hasRoleFolderApprovalAuth(userid,folderid)) {
			throw new BusinessException("没有权限！");
		}
		
		de.clearSql();
		de.addSql(" update odssu.emp_rolefolder set operatorauth = '0' ");
		de.addSql("  where empno = :empno and folderid = :folderid ");
		de.setString("empno", empno);
		de.setString("folderid", folderid);
		int uprows = de.update();
		
		if(uprows == 0) {
			return null;
		}
		
		de.clearSql();
		de.addSql(" delete from odssu.emp_rolefolder  ");
		de.addSql("  where empno = :empno  ");
		de.addSql("    and operatorauth = '0' ");
		de.addSql("    and approvalauth = '0' ");
		de.setString("empno", empno);
		de.update();
		
		return null;
	}
	public DataObject revokeApprovalAuth(DataObject para) throws AppException, BusinessException {
		
		String userid = this.getUser().getUserid();
		String folderid = para.getString("folderid");
		String empno = para.getString("empno");
		
		if(!hasRoleFolderApprovalAuth(userid,folderid)) {
			throw new BusinessException("没有权限！");
		}
		
		de.clearSql();
		de.addSql(" update odssu.emp_rolefolder set approvalauth = '0' ");
		de.addSql("  where empno = :empno and folderid = :folderid ");
		de.setString("empno", empno);
		de.setString("folderid", folderid);
		int uprows = de.update();
		
		if(uprows == 0) {
			return null;
		}
		
		de.clearSql();
		de.addSql(" delete from odssu.emp_rolefolder  ");
		de.addSql("  where empno = :empno  ");
		de.addSql("    and operatorauth = '0' ");
		de.addSql("    and approvalauth = '0' ");
		de.setString("empno", empno);
		de.update();
		
		return null;
	}
	private boolean hasRoleFolderApprovalAuth(String userid,String folderid) throws AppException {
		
		de.clearSql();
		de.addSql(" select 1  ");
		de.addSql("   from odssu.emp_rolefolder a ");
		de.addSql("  where a.empno = :empno  ");
		de.addSql("    and a.approvalauth = '1' ");
		de.addSql("    and a.folderid in (select b.pfolderid from odssu.ir_rolefolder_closure b ");
		de.addSql("                        where b.folderid = :folderid) ");
		de.setString("empno", userid);
		de.setString("folderid", folderid);
		DataStore empauthds = de.query();
		
		if(empauthds == null || empauthds.rowCount() == 0) {
			return false;
		}
		return true;
		
	}
	
	public DataObject recalculateRoleFolderClosure(DataObject para)throws Exception {
		
		ClosureUtil.dealIr_RoleFolder_Closure();
		
		return null;
	}
	public DataObject hasEmpOperatorRoleAuth(DataObject para)throws Exception {
		
		String folderid = para.getString("folderid");
		String deforgno = para.getString("deforgno");
		
		String empno = this.getUser().getUserid();
		
		if("null".equals(folderid)) {

			folderid = getFolderidByDefOrgno(deforgno);
			
		}
		if(StringUtils.isNotBlank(folderid)) {
			de.clearSql();
			de.addSql(" select 1 from odssu.emp_rolefolder a  ");
			de.addSql("  where a.empno = :empno ");
			de.addSql("    and a.operatorauth = '1' ");
			de.addSql("    and a.folderid in (select b.pfolderid  ");
			de.addSql("                         from odssu.ir_rolefolder_closure b ");
			de.addSql("                        where b.folderid = :folderid) ");
			de.setString("empno", empno);
			de.setString("folderid", folderid);
		}else {
			de.clearSql();
			de.addSql(" select 1 ");
			de.addSql("   from odssu.ir_emp_org_all_role a ");
			de.addSql("  where a.orgno = 'ORGROOT'   ");
			de.addSql("  and a.empno = :empno ");
			de.setString("empno", empno);
		}
		DataStore authds = de.query();
		
		DataObject result = DataObject.getInstance();
		
		if(authds == null || authds.rowCount() ==0 ) {
			result.put("flag", "false");
			return result;
		}
		
		result.put("flag", "true");
		return result;
	}
	private String getFolderidByDefOrgno(String deforgno) throws Exception {
		
		de.clearSql();
		de.addSql(" select a.folderid ");
		de.addSql("   from odssu.role_folder a ");
		de.addSql("  where a.deforgno = :deforgno ");
		de.addSql("    and a.collect_query = '1' ");
		de.setString("deforgno", deforgno);
		DataStore folderds = de.query();
		
		if(folderds == null || folderds.rowCount() == 0) {
			if("ORGROOT".equals(deforgno)) {
				return null;
			}
		}
		if(folderds == null || folderds.rowCount() == 0) {
			throw new BusinessException("未获取到当前机构【"+deforgno+"】的角色目录编号！") ;
		}
		String folderid = folderds.getString(0, "folderid");
		
		return folderid;
	}
}
