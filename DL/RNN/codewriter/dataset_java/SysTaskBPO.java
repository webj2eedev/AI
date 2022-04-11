package com.dw.res.systask;

import java.util.ArrayList;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.log.LogHandler;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.convert.allpdlist.PdListConvert;
import com.dw.convert.dpbypd.DPWithPDConvert;
import com.dw.convert.singlepd.SinglePDConvert;

public class SysTaskBPO extends BPO{
	
	
	/**************************SYS查询分页*********************************************/
	public DataObject sysQueryGetRowCount(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String key = para.getString("key","");
		PdListConvert convert = new PdListConvert();
		convert.setPDRES();
		DataStore resds = convert.getRESListByKey(key);
		
		String folderno = para.getString("folderno");
		if(folderno.equals("wfproot")) {
			
			de.clearSql();
			de.addSql(" select pdid resourceid from odssu.pd_resfolder ");
			DataStore pdfolderds = de.query();
			resds.remainNotIn(pdfolderds, "resourceid");
			
			de.clearSql();
			de.addSql(" select a.functionid resourceid from odssu.appfunction a ");
			de.addSql("  where a.pdid is null  ");
			de.addSql("    and a.appid in (select c.appid from odssu.appinfo c)");
			de.addSql("    and a.functionid not in (select b.fnid from odssu.fn_resfolder b) ");
			de.addSql("    and (a.functionid like :key or a.functionname like :key) ");
			de.setString("key", "%"+key+"%");
			DataStore fnds = de.query();
			
			int rowCount = resds.rowCount() + fnds.rowCount();
			
			vdo.put("row_count", rowCount);
			
			return vdo;
		}
		de.clearSql();
		de.addSql(" select pdid resourceid from odssu.pd_resfolder ");
		if(!folderno.equals("root") && !"".equals(folderno)) {//查询全部
			de.addSql("  where folderid = :folderid ");
			de.setString("folderid", folderno);
		}
		DataStore pdfolderds = de.query();
		resds.remainIn(pdfolderds, "resourceid");
		
		de.clearSql();
		de.addSql(" select fnid resourceid from odssu.fn_resfolder a, odssu.appfunction b  ");
		de.addSql("  where a.fnid = b.functionid  ");
		de.addSql("    and b.pdid is null ");
		de.addSql("    and b.appid in (select c.appid from odssu.appinfo c)");
		de.addSql("    and (b.functionid like :key or b.functionname like :key) ");
		de.setString("key", "%"+key+"%");
		if(!folderno.equals("root") && !"".equals(folderno)) {//查询全部
			de.addSql("  and a.folderid = :folderid ");
			de.setString("folderid", folderno);
		}
		DataStore fnds = de.query();
		
		int rowCount = resds.rowCount() + fnds.rowCount();

		vdo.put("row_count", rowCount);
		
		return vdo;
	}

	public DataObject sysQueryGetPageRows(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		int g_startRowNumber = para.getInt("g_startRowNumber");
		int g_endRowNumber = para.getInt("g_endRowNumber");	
		String key = para.getString("key","");
		PdListConvert convert = new PdListConvert();
		convert.setPDRES();
		DataStore resds = convert.getRESListByKey(key);
		
		String folderno = para.getString("folderno");
		if(folderno.equals("wfproot")) {
			
			de.clearSql();
			de.addSql(" select pdid resourceid from odssu.pd_resfolder ");
			DataStore pdfolderds = de.query();
			resds.remainNotIn(pdfolderds, "resourceid");
			
			de.clearSql();
			de.addSql(" select a.functionid resourceid ,a.functionname resourcename , a.appid , b.appname,'功能' zylx,'调整目录' tzml ");
			de.addSql("   from odssu.appfunction a ,odssu.appinfo b ");
			de.addSql("  where a.functionid not in (select c.fnid from odssu.fn_resfolder c) ");
			de.addSql("    and a.pdid is null ");
			de.addSql("    and a.appid = b.appid ");
			de.addSql("    and (a.functionid like :key or a.functionname like :key )");
			de.setString("key","%"+ key+"%");
			DataStore fnds = de.query();
			
			
			resds.combineDatastore(fnds);
			
			if (resds.isEmpty()) {
				vdo.put("vds", DataStore.getInstance());
			} else {
				DataStore subvds = resds.subDataStore(g_startRowNumber - 1, g_endRowNumber);
				vdo.put("vds", subvds);
			}
			return vdo;
		}
		de.clearSql();
		de.addSql(" select pdid resourceid from odssu.pd_resfolder ");
		if(!folderno.equals("root") && !"".equals(folderno)) {//查询全部
			de.addSql("  where folderid = :folderid ");
			de.setString("folderid", folderno);
		}
		DataStore pdfolderds = de.query();
		resds.remainIn(pdfolderds, "resourceid");
		
		de.clearSql();
		de.addSql(" select b.functionid resourceid ,b.functionname resourcename , b.appid , c.appname,'功能' zylx,'调整目录' tzml  ");
		de.addSql("   from odssu.fn_resfolder a, odssu.appfunction b , odssu.appinfo c ");
		de.addSql("  where a.fnid = b.functionid  ");
		de.addSql("    and b.pdid is null ");
		de.addSql("    and b.appid = c.appid  ");
		de.addSql("    and (b.functionid like :key or b.functionname like :key) ");
		de.setString("key", "%"+key+"%");
		if(!folderno.equals("root") && !"".equals(folderno)) {//查询全部
			de.addSql("  and a.folderid = :folderid ");
			de.setString("folderid", folderno);
		}
		DataStore fnds = de.query();
		
		resds.combineDatastore(fnds);
		
		if (resds.isEmpty()) {
			vdo.put("vds", DataStore.getInstance());
			return vdo;
		}
		DataStore subvds = resds.subDataStore(g_startRowNumber - 1, g_endRowNumber);
		vdo.put("vds", subvds);
		return vdo;
	}

	public DataObject sysQueryGetAllRows(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String key = para.getString("key","");
		String folderno = para.getString("folderno");
		
		PdListConvert convert = new PdListConvert();
		convert.setPDRES();
		DataStore resds = convert.getRESListByKey(key);
		
		if(folderno.equals("wfproot")) {//查询未分配权限
			de.clearSql();
			de.addSql(" select pdid resourceid from odssu.pd_resfolder ");
			DataStore pdfolderds = de.query();
			
			resds.remainNotIn(pdfolderds, "resourceid");
			
			de.clearSql();
			de.addSql(" select a.functionid resourceid from odssu.appfunction a ");
			de.addSql("  where a.pdid is null  ");
			de.addSql("    and a.appid in (select c.appid from odssu.appinfo c)");
			de.addSql("    and a.functionid not in (select b.fnid from odssu.fn_resfolder b) ");
			de.addSql("    and (a.functionid like :key or a.functionname like :key) ");
			de.setString("key", "%"+key+"%");
			DataStore fnds = de.query();
			
			resds.combineDatastore(fnds);
			
			vdo.put("syslist", resds);
			return vdo;
		}
		de.clearSql();
		de.addSql(" select pdid resourceid from odssu.pd_resfolder ");
		if(!folderno.equals("root") && !"".equals(folderno)) {//查询全部
			de.addSql("  where folderid = :folderid ");
			de.setString("folderid", folderno);
		}
		DataStore pdfolderds = de.query();
		resds.remainIn(pdfolderds, "resourceid");
		
		de.clearSql();
		de.addSql(" select fnid resourceid from odssu.fn_resfolder a, odssu.appfunction b  ");
		de.addSql("  where a.fnid = b.functionid  ");
		de.addSql("    and b.pdid is null ");
		de.addSql("    and b.appid in (select c.appid from odssu.appinfo c)");
		de.addSql("    and (b.functionid like :key or b.functionname like :key) ");
		de.setString("key", "%"+key+"%");
		if(!folderno.equals("root") && !"".equals(folderno)) {//查询全部
			de.addSql("  and a.folderid = :folderid ");
			de.setString("folderid", folderno);
		}
		DataStore fnds = de.query();
		
		resds.combineDatastore(fnds);
		
		vdo.put("syslist", resds);
		return vdo;
	}
	
	/**
	 * 方法简介：查询新增目录 赵伟华  2020-03-09
	 */
	public DataObject queryAddTreeNode(DataObject para) throws Exception{
		DataStore folderds = DataStore.getInstance();
		String folderid = para.getString("folderid");
		if(folderid.equals("root")) {
			LogHandler.log("从导航目录下新增根目录");
			folderds.put(0, "folderid", de.getNextVal("ODSSU.SEQ_RES_FOLDER"));//序列需新建
		}else {
			DE de = DE.getInstance();
	  		de.clearSql();
	  		de.addSql(" select 1 ");
	  		de.addSql(" from odssu.res_folder ");
	  		de.addSql(" where folderid = :folderid ");
	  		de.setString("folderid", folderid);
			folderds = de.query();
			if(folderds.rowCount() == 0) {
				LogHandler.log("根据目录ID["+folderid+"]未能查询到对应的目录信息，请检查核对");
			}else {
				folderds.put(0, "folderid", de.getNextVal("ODSSU.SEQ_RES_FOLDER"));
			}
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("folderds", folderds);
		return vdo;
	}
	
	/**
	 * 方法简介：保存新增目录 赵伟华  2020-03-09
	 */
	public DataObject saveAddTreeNode(DataObject para) throws Exception{
		DE de = DE.getInstance();
		String folderid = para.getString("folderid");
		String folderlabel = para.getString("folderlabel");
		if(folderlabel == null || folderlabel.equals("")) {
			throw new BusinessException("传入的目录名称为空，请检查");
		}
		String pfolderid = para.getString("pfolderid");
		if(pfolderid == null || pfolderid.equals("")) {
			throw new BusinessException("传入的父目录ID为空，请检查");
		}
		if(pfolderid.equals("root")) {//新增根目录
			de.clearSql();
			de.addSql(" insert into odssu.res_folder (folderid, folderlabel, collect_query) ");
			de.addSql(" values (:folderid, :folderlabel, :collect_query) ");
			de.setString("folderid",folderid);
			de.setString("folderlabel",folderlabel);
			de.setString("collect_query","1");
			de.update();
		}else {//新增子目录
			de.clearSql();
			de.addSql(" insert into odssu.res_folder (pfolderid, folderid, folderlabel, collect_query) ");
			de.addSql(" values (:pfolderid, :folderid, :folderlabel, :collect_query) ");
			de.setString("pfolderid",pfolderid);
			de.setString("folderid",folderid);
			de.setString("folderlabel",folderlabel);
			de.setString("collect_query","0");
			de.update();
		}
		
		return null;
	}
	
	/**
	 * 方法简介：查询修改目录 赵伟华  2020-03-09
	 */
	public DataObject queryModTreeNode(DataObject para) throws Exception{
		String folderid = para.getString("folderid");
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select folderid, folderlabel ");
  		de.addSql(" from odssu.res_folder ");
  		de.addSql(" where folderid = :folderid ");
  		de.setString("folderid", folderid);
		DataStore folderds = de.query();
		if(folderds.rowCount() == 0) {
			LogHandler.log("根据目录ID["+folderid+"]未能查询到对应的目录信息，请检查核对");
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("folderds", folderds);
		return vdo;
	}
	/**
	 * 方法简介：保存修改目录 赵伟华  2020-03-09
	 */
	public DataObject saveModTreeNode(DataObject para) throws Exception{
		String folderid = para.getString("folderid");
		String newfolderlabel = para.getString("newfolderlabel");

		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" update odssu.res_folder set folderlabel = :newfolderlabel ");
  		de.addSql(" where folderid = :folderid ");
		de.setString("newfolderlabel",newfolderlabel);
		de.setString("folderid",folderid);
		de.update();
		
		return null;
	}
	
	/**
	 * 方法简介：检验是否可以删除目录 赵伟华  2020-03-09
	 */
	public DataObject checkDelTreeNode(DataObject para) throws AppException, BusinessException {
		String folderid = para.getString("folderid");
		if (folderid == null || folderid.equals("")) {
			throw new BusinessException("未获取到目录节点！");
		}
		
		String passkey = "yes";
		DE de = DE.getInstance();
		//校验是否为根目录
  		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql(" from odssu.res_folder ");
  		de.addSql(" where pfolderid = :pfolderid and collect_query = '1' ");
  		de.setString("pfolderid", folderid);
		DataStore rootds = de.query();
		if(rootds.rowCount() > 0) {
			passkey = "root";
		}else {
			//校验是否存在下级目录
	  		de.clearSql();
	  		de.addSql(" select 1 ");
	  		de.addSql(" from odssu.res_folder ");
	  		de.addSql(" where pfolderid = :pfolderid ");
	  		de.setString("pfolderid", folderid);
			DataStore pfolderds = de.query();
			if(pfolderds.rowCount() > 0) {
				passkey = "xjml";
			}else {
				//检验是否存在流程任务
		  		de.clearSql();
		  		de.addSql(" select 1 ");
		  		de.addSql(" from odssu.pd_resfolder ");
		  		de.addSql(" where folderid = :folderno ");
		  		de.setString("folderno", folderid);
				DataStore pdds = de.query();
				//检验是否存在功能任务
		  		de.clearSql();
		  		de.addSql(" select 1 ");
		  		de.addSql(" from odssu.fn_resfolder ");
		  		de.addSql(" where folderid = :folderno ");
		  		de.setString("folderno", folderid);
				DataStore fnds = de.query();
				if(pdds.rowCount() > 0 && fnds.rowCount() > 0) {
					passkey = "lcrwandgnrw";
				}else if(pdds.rowCount() > 0 && fnds.rowCount() == 0) {
					passkey = "lcrw";
				}else if(pdds.rowCount() == 0 && fnds.rowCount() > 0) {
					passkey = "gnrw";
				}
			}
		}
		
		DataObject result = DataObject.getInstance();
		result.put("passkey", passkey);
		return result;
	}
	/**
	 * 方法简介：删除目录 赵伟华  2020-03-09
	 */
	public DataObject delTreeNode(DataObject para) throws Exception{
		String folderid = para.getString("folderid");

		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" delete from odssu.res_folder ");
  		de.addSql(" where folderid = :folderid ");
		de.setString("folderid",folderid);
		de.update();
		
		return null;
	}
	
	/**
	 * 方法简介：保存调整目录 赵伟华  2020-03-09
	 */
	public DataObject saveModTreePnode(DataObject para) throws AppException,BusinessException{
		DE de = DE.getInstance();
		String folderid = para.getString("folderid");
		if(folderid == null || folderid.equals("")) {
			throw new AppException("未获取到目录节点！");
		}
		String pfolderid = para.getString("pfolderid");
		if(pfolderid == null || pfolderid.equals("")) {
			throw new AppException("未获取到父目录节点！");
		}
		
		if(pfolderid.equals("root")) {//变更为父目录
	  		de.clearSql();
	  		de.addSql(" select folderid, folderlabel ");
	  		de.addSql(" from odssu.res_folder ");
	  		de.addSql(" where folderid = :folderid ");
	  		de.setString("folderid", folderid);
			DataStore sysds = de.query();
			if(sysds == null || sysds.rowCount() == 0) {
				throw new AppException("未获取到目录节点信息！");
			}else {
				String folderlabel = sysds.getString(0, "folderlabel");
				//先删后插
				de.clearSql();
		  		de.addSql(" delete from odssu.res_folder ");
		  		de.addSql(" where folderid = :folderid ");
				de.setString("folderid",folderid);
				de.update();

				de.clearSql();
				de.addSql(" insert into odssu.res_folder (folderid, folderlabel, collect_query) ");
				de.addSql(" values (:folderid, :folderlabel, :collect_query) ");
				de.setString("folderid",folderid);
				de.setString("folderlabel",folderlabel);
				de.setString("collect_query","1");
				de.update();
			}
		}else {
			de.clearSql();
			de.addSql("update odssu.res_folder set pfolderid = :pfolderid , collect_query = :collect_query where folderid = :folderid ");
			de.setString("pfolderid", pfolderid);
			de.setString("folderid", folderid);
			de.setString("collect_query", "0");
			de.update();
		}
		
		return null;
	}

	public DataObject checkModTreePnode(DataObject para) throws AppException{
		DataObject result = DataObject.getInstance();
		String folderid = para.getString("folderid");
		String sfwgml = "0";
		
		de.clearSql();
		de.addSql("select 1 from odssu.fn_resfolder a where a.folderid = :folderid ");
		de.setString("folderid", folderid);
		DataStore fnds = de.query();
		
		de.clearSql();
		de.addSql("select 1 from odssu.pd_resfolder a where a.folderid = :folderid ");
		de.setString("folderid", folderid);
		DataStore pdds = de.query();
		
		String pfolderid = para.getString("pfolderid");
		if(pfolderid.equals("root")) {
			sfwgml = "2";
		}else if(fnds.rowCount() > 0 || pdds.rowCount() > 0) {
			sfwgml = "1";
		}
		
		result.put("sfwgml", sfwgml);
		return result;
	}
	/**
	 * 方法简介：转换跳转模式 
	 */
	public DataObject adjustFolderMode(DataObject para) throws AppException,BusinessException{
		String userid = this.getUser().getUserid();
		de.clearSql();
		de.addSql(" select 1  ");
		de.addSql("   from odssu.emp_personalization a");
		de.addSql("  where empno = :userid");
		de.setString("userid", userid);
		DataStore flag = de.query();
		if(flag.rowCount()== 0) {
			de.clearSql();
			de.addSql("  insert into odssu.emp_personalization ");
			de.addSql(" (empno,adjustfoldermode) ");
			de.addSql("  VALUES(:userid,1-adjustfoldermode)");
			de.setString("userid", userid);
			de.update();
			return null;
		}else {
			de.clearSql();
			de.addSql(" update odssu.emp_personalization ");
			de.addSql("    set adjustfoldermode = 1-adjustfoldermode");
			de.addSql("  where empno = :userid");
			de.setString("userid", userid);
			de.update();
			return null;
		}
	}
	/**
	 * 方法简介：查询跳转模式
	 */
	public DataObject queryAdjustFolderMode(DataObject para) throws AppException,BusinessException{
		String userid = this.getUser().getUserid();
		DataObject result = DataObject.getInstance();
		
		de.clearSql();
		de.addSql(" select adjustfoldermode  ");
		de.addSql("   from odssu.emp_personalization a");
		de.addSql("  where empno = :userid");
		de.setString("userid", userid);
		DataStore mode=de.query();
		
		if(mode!= null &&mode.rowCount()>0) {
			result = mode.get(0);
		}else {
			result.put("adjustfoldermode", 0);
		}
		return result;

	}

// 查询是否有权限调整目录
	public DataObject checkUserPermission(DataObject para) throws AppException{
		DataObject result = DataObject.getInstance();
		String userid = this.getUser().getUserid();
		de.clearSql();
		de.addSql(" select 1 ");
		de.addSql("   from odssu.ir_emp_org_all_role ");
		de.addSql("  where empno = :userid ");
		de.addSql("    and orgno = :orgno ");
		de.addSql("    and roleno = :roleno ");
		de.setString("userid", userid);
		de.setString("orgno", OdssuContants.ORGROOT);
		de.setString("roleno", OdssuContants.ROLE_ODS_DUTY_ASSIGNER);
		DataStore tempds = de.query();
		if((tempds !=null &&tempds.rowCount() > 0)) {
			result.put("flag", "true");
		}else {
			result.put("flag", "false");
		}
		return result;
	}
	
	/**
	 * 方法简介：保存调整目录 赵伟华  2020-03-09
	 */
	public DataObject saveModTreeSysNode(DataObject para) throws AppException,BusinessException{
		DE de = DE.getInstance();
		String folderid = para.getString("folderid");
		if(folderid == null || folderid.equals("")) {
			throw new AppException("未获取到新目录节点！");
		}
		String lxbh = para.getString("lxbh");
		String zylx = para.getString("zylx");
		
		if(zylx.equals("功能")) {//功能任务
	  		de.clearSql();
	  		de.addSql(" select 1 ");
	  		de.addSql(" from odssu.fn_resfolder ");
	  		de.addSql(" where fnid = :lxbh ");
	  		de.setString("lxbh", lxbh);
			DataStore sysds = de.query();
			if(sysds == null || sysds.rowCount() == 0) {
				de.clearSql();
				de.addSql(" insert into odssu.fn_resfolder (folderid, fnid) ");
				de.addSql(" values (:folderid, :lxbh) ");
				de.setString("folderid",folderid);
				de.setString("lxbh",lxbh);
				de.update();
			}else {
				de.clearSql();
				de.addSql("update odssu.fn_resfolder set folderid = :folderid where fnid = :lxbh ");
				de.setString("folderid", folderid);
				de.setString("lxbh", lxbh);
				de.update();
			}
		}else if(zylx.equals("流程")) {//流程任务
	  		de.clearSql();
	  		de.addSql(" select 1 ");
	  		de.addSql(" from odssu.pd_resfolder ");
	  		de.addSql(" where pdid = :lxbh ");
	  		de.setString("lxbh", lxbh);
			DataStore sysds = de.query();
			if(sysds == null || sysds.rowCount() == 0) {
				de.clearSql();
				de.addSql(" insert into odssu.pd_resfolder (folderid, pdid) ");
				de.addSql(" values (:folderid, :lxbh) ");
				de.setString("folderid",folderid);
				de.setString("lxbh",lxbh);
				de.update();
			}else {
				de.clearSql();
				de.addSql("update odssu.pd_resfolder set folderid = :folderid where pdid = :lxbh ");
				de.setString("folderid", folderid);
				de.setString("lxbh", lxbh);
				de.update();
			}
		}else {
			throw new BusinessException("未获取到资源类型，请检查");
		}
		
		return null;
	}
	/**
	 * 方法简介：查看权限 赵伟华  2020-03-11
	 */
	public DataObject queryAuthority(DataObject para) throws Exception{
		String folderid = para.getString("folderid");
		
		DE de = DE.getInstance();
  		de.clearSql();
		de.addSql(" select DISTINCT b.empno, b.empname, b.loginname, m.orgname ");
		de.addSql("   from odssu.ir_emp_org_all_role a, odssu.empinfor b, odssu.orginfor m");
		de.addSql("  where a.orgno = :orgno ");
		de.addSql("    and a.roleno = :roleno ");
  		de.addSql("    and a.empno = b.empno ");
  		de.addSql("    and b.hrbelong = m.orgno ");
  		de.addSql("    and b.sleepflag = '0'  ");
  		de.addSql("    and m.sleepflag = '0' ");
  		de.addSql("  order by b.empno ");
		de.setString("orgno", OdssuContants.ORGROOT);
		de.setString("roleno", OdssuContants.ROLE_ODS_DUTY_ASSIGNER);
		DataStore tempds = de.query();
		if(tempds.rowCount() == 0) {
			LogHandler.log("根据目录ID["+folderid+"]未能查询到有权调整的操作员信息，请检查");
		}

		String foldername = "";
		if(folderid.equals("root")) {
			foldername = "资源导航";
		}else {
			de.clearSql();
			de.addSql(" select a.folderlabel from odssu.res_folder a where a.folderid = :folderid ");
			de.setString("folderid", folderid);
			DataStore folderds = de.query();
			if(folderds.rowCount() == 0) {
				bizException("根据目录ID["+folderid+"]未能查询到目录信息，请检查");
			}else {
				foldername = folderds.getString(0, "folderlabel");
			}
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("folderds", tempds);
		vdo.put("foldername", foldername);
		return vdo;
	}
	
	/**
	 * 加载功能任务数据
	 */
	public DataObject getFnResourceInfo(DataObject para) throws AppException{
		String functionid = para.getString("functionid");
		String functionname = getFunctionLabel(para).getString("functionname");
		String strorgtypename = "";
		String isnjjs;
		
		de.clearSql();
		de.addSql(" select t.roleno ");
		de.addSql("   from odssu.role_function_manual t ");
		de.addSql("  where t.functionid =:functionid ");
		de.addSql("    and exists (select 1 from odssu.roleinfor a where t.roleno = a.roleno and a.sleepflag = '0' ) ");
		de.addSql("    and t.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("functionid", functionid);
		DataStore roleds = de.query();
		
		de.clearSql(); 
		de.addSql(" select distinct r.empno ");
		de.addSql("   from odssu.role_function_manual t, ");
		de.addSql("        odssu.ir_emp_org_all_role r ");
		de.addSql("  where t.functionid=:functionid ");
		de.addSql("    and t.roleno = r.roleno ");
		de.addSql("    and exists (select 1 from odssu.roleinfor b where r.roleno = b.roleno and b.sleepflag = '0') ");
		de.addSql("    and exists(select 1 from odssu.empinfor a where a.empno = r.empno and a.sleepflag = '0') ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("functionid", functionid);
		DataStore operatords = de.query();
		
		de.clearSql();
		de.addSql("select c.typename orgtypename,b.orgtypeno  ");
		de.addSql("  from odssu.fn_roletype a,odssu.ir_org_role_type b,odssu.org_type c ");
		de.addSql(" where a.roletypeno = b.roletypeno ");
		de.addSql("   and b.ORGTYPENO = c.TYPENO ");
		de.addSql("   and a.functionid=:functionid  ");
		de.setString("functionid", functionid);
		DataStore orgtypename = de.query();
		if(orgtypename !=null && orgtypename.rowCount() >0) {
			strorgtypename = orgtypename.getString(0,"orgtypename");
			for(int i=1;i<orgtypename.rowCount();i++) {
				strorgtypename = strorgtypename+"，"+orgtypename.getString(i,"orgtypename");
			}
		}
		de.clearSql();
		de.addSql(" select 1 from odssu.fn_roletype a where a.functionid = :functionid ");
		de.setString("functionid", functionid);
		DataStore fnroletypeds = de.query();
		if(fnroletypeds == null || fnroletypeds.rowCount() ==0) {
			isnjjs = "true";
		}else {
			isnjjs = "false";
		}

		String appid = "", appname = "";
		de.clearSql();
		de.addSql("select a.appid ");
		de.addSql("  from odssu.appfunction a ");
		de.addSql(" where a.functionid = :functionid   ");
		de.setString("functionid", functionid);
		DataStore appidds = de.query();
		if(appidds !=null && appidds.rowCount() >0) {
			appid = appidds.getString(0,"appid");
			if(appid != null && !appid.equals("")) {
				de.clearSql();
				de.addSql("select a.appname ");
				de.addSql("  from odssu.appinfo a ");
				de.addSql(" where a.appid = :appid   ");
				de.setString("appid", appid);
				DataStore appnameds = de.query();
				if(appnameds !=null && appnameds.rowCount() >0) {
					appname = appnameds.getString(0,"appname");
				}
			}
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("rolenum", roleds.rowCount()+"");
		vdo.put("operatornum", operatords.rowCount()+"");
		vdo.put("orgtypename", strorgtypename);
		vdo.put("orgtypeno", orgtypename);
		vdo.put("functionname", functionname);
		vdo.put("isnjjs", isnjjs);
		vdo.put("appid", appid);
		vdo.put("appname", appname);
		return vdo;
	}
	public DataObject getFunctionLabel(DataObject para)throws AppException{
		String functionid = para.getString("functionid");
		
		de.clearSql();
		de.addSql(" select a.functionname from odssu.appfunction a where a.functionid = :functionid ");
		de.setString("functionid", functionid);
		DataStore vds = de.query();
		
		if(vds == null || vds.rowCount() ==0) {
			throw new AppException("获取功能任务名称失败，functionid："+functionid);
		}
		return vds.get(0);
	}

	public DataObject getFnAllRole(DataObject para) throws AppException{
		String functionid = para.getString("functionid");
		
		de.clearSql();
		de.addSql(" select t.roleno ,r.rolename  ");
		de.addSql("   from odssu.role_function_manual t ,");
		de.addSql("        odssu.roleinfor r ");
		de.addSql("  where t.functionid=:functionid ");
		de.addSql("    and t.roleno = r.roleno ");
		de.addSql("    and r.sleepflag = '0'   ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("functionid", functionid);
		DataStore roleds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("rolelist", roleds);
		return vdo;
	}
	public DataObject getFnEMPInfo(DataObject para) throws AppException{
		String roleid = para.getString("roleid");
		
		de.clearSql();
		de.addSql(" select b.empno,b.LOGINNAME,b.EMPNAME ,a.ROLENO,r.rolename,a.ORGNO,t.ORGNAME ");
		de.addSql("   from odssu.ir_emp_org_all_role a left join odssu.roleinfor r on a.ROLENO = r.roleno ,  ");
		de.addSql("        odssu.empinfor b , odssu.orginfor t ");
		de.addSql("  where a.empno = b.empno ");
		de.addSql("    and a.ORGNO = t.orgno ");
		de.addSql("    and b.sleepflag = '0' ");
		de.addSql("    and t.sleepflag = '0' ");
		de.addSql("    and a.roleno = :roleid ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("roleid", roleid);
		DataStore vds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("emplist", combineDSCol(vds));
		return vdo;
	}

	/**
	 * 查询流程基本信息 zwh 2020-03-10
	 */
	public DataObject getPdBaseInfo(DataObject para) throws Exception {
		String pdid = para.getString("pdid");
		
		String appid = para.getString("appid");
		de.clearSql();
		de.addSql(" select appname from odssu.appinfo where appid = :appid ");
		de.setString("appid", appid);
		DataStore appnameds = de.query();
		
		if(appnameds == null || appnameds.rowCount() ==0) {
			throw new BusinessException("应用编号为："+appid+"未注册！");
		}
		String appname = appnameds.getString(0, "appname");
		SinglePDConvert convert = new SinglePDConvert(pdid);
		
		para.put("appname", appname);
		para.put("pdlabel", convert.getPDLabel());
		return para;
	}
	/**
	 * 加载流程任务数据
	 * @throws BusinessException 
	 */
	public DataObject getPdResourceInfo(DataObject para) throws AppException, BusinessException{
		String pdid = para.getString("pdid");
		String dptdid = para.getString("dptdid");
		String toccode = para.getString("toccode");
		String strorgtypename = "";
		String isnjjs;
		
		de.clearSql();
		de.addSql(" select t.roleno ");
		de.addSql("   from odssu.dutyposition_task_role t, ");
		de.addSql("        odssu.roleinfor r ");
		de.addSql("  where t.pdid=:pdid ");
		de.addSql("    and t.dptdid=:dptdid  ");
		de.addSql("    and t.toccode= :toccode ");
		de.addSql("    and t.roleno = r.roleno ");
		de.addSql("    and r.sleepflag = '0'   ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("pdid", pdid);
		de.setString("dptdid", dptdid);
		de.setString("toccode", toccode);
		DataStore roleds = de.query();
		
		de.clearSql(); 
		de.addSql(" select distinct r.empno ");
		de.addSql("   from odssu.dutyposition_task_role t, ");
		de.addSql("        odssu.ir_emp_org_all_role r ");
		de.addSql("  where t.pdid=:pdid ");
		de.addSql("    and t.dptdid=:dptdid  ");
		de.addSql("    and t.toccode= :toccode ");
		de.addSql("    and t.roleno = r.roleno ");
		de.addSql("    and exists (select 1 from odssu.roleinfor b where r.roleno = b.roleno and b.sleepflag = '0' )");
		de.addSql("    and exists(select 1 from odssu.empinfor a where a.empno = r.empno and a.sleepflag = '0') ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("pdid", pdid);
		de.setString("dptdid", dptdid);
		de.setString("toccode", toccode);
		DataStore operatords = de.query();
		
		DPWithPDConvert dpconvert = new DPWithPDConvert(pdid);
		ArrayList<String> roletypelist = dpconvert.getDPRoletype(dptdid);
		
		de.clearSql();
		de.addSql("select c.typename orgtypename,b.orgtypeno ");
		de.addSql("  from odssu.ir_org_role_type b,odssu.org_type c ");
		de.addSql(" where b.roletypeno in (:roletypeno) ");
		de.addSql("   and b.ORGTYPENO = c.TYPENO ");
		de.setStringList("roletypeno", roletypelist);
		DataStore orgtypename = de.query();
		if(orgtypename !=null && orgtypename.rowCount() >0) {
			strorgtypename = orgtypename.getString(0,"orgtypename");
			for(int i=1;i<orgtypename.rowCount();i++) {
				strorgtypename = strorgtypename+"，"+orgtypename.getString(i,"orgtypename");
			}
		}
		String firstroletype = roletypelist.get(0);
		if("none".equals(firstroletype)) {
			isnjjs = "true";
		}else {
			isnjjs = "false";
		}
		
		DataObject label = getProcessInfo(pdid,dptdid,toccode);
		SinglePDConvert pdconvert = new SinglePDConvert(pdid);

		String appid = pdconvert.getAppid();
		String  appname = "";
		
		de.clearSql();
		de.addSql("select a.appname ");
		de.addSql("  from odssu.appinfo a ");
		de.addSql(" where a.appid = :appid   ");
		de.setString("appid", appid);
		DataStore appnameds = de.query();
		if(appnameds !=null && appnameds.rowCount() >0) {
			appname = appnameds.getString(0,"appname");
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("rolenum", roleds.rowCount()+"");
		vdo.put("operatornum", operatords.rowCount()+"");
		vdo.put("orgtypename", strorgtypename);
		vdo.put("orgtypeno", orgtypename);
		vdo.put("dplabel", label.getString("dptdlabel"));
		vdo.put("pdlabel", label.getString("pdlabel"));
		vdo.put("isnjjs", isnjjs);
		vdo.put("appid", appid);
		vdo.put("appname", appname);
		return vdo;
	}
	
	public DataObject getPdAllRole(DataObject para) throws AppException{
		String pdid = para.getString("pdid");
		String dptdid = para.getString("dptdid");
		String toccode = para.getString("toccode");
		
		de.clearSql();
		de.addSql(" select t.roleno roleid ,r.rolename ");
		de.addSql("   from odssu.dutyposition_task_role t , ");
		de.addSql("        odssu.roleinfor r  ");
		de.addSql("  where t.pdid=:pdid ");
		de.addSql("    and t.dptdid=:dptdid  ");
		de.addSql("    and t.toccode= :toccode ");
		de.addSql("    and t.roleno = r.roleno ");
		de.addSql("    and r.sleepflag = '0'   ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("pdid", pdid);
		de.setString("dptdid", dptdid);
		de.setString("toccode", toccode);
		DataStore roleds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("rolelist", roleds);
		return vdo;
	}
	public DataObject getPdEMPInfo(DataObject para) throws AppException{
		String roleid = para.getString("roleid");
		
		de.clearSql();
		de.addSql(" select b.empno,b.LOGINNAME,b.EMPNAME ,a.ROLENO,r.rolename,a.ORGNO,t.ORGNAME ");
		de.addSql("   from odssu.ir_emp_org_all_role a left join odssu.roleinfor r on a.ROLENO = r.roleno ,  ");
		de.addSql("        odssu.empinfor b , odssu.orginfor t ");
		de.addSql("  where a.empno = b.empno ");
		de.addSql("    and a.ORGNO = t.orgno ");
		de.addSql("    and b.sleepflag = '0'  ");
		de.addSql("    and a.roleno = :roleid ");
		de.setString("roleid", roleid);
		DataStore vds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("emplist", combineDSCol(vds));
		return vdo;
	}
	
	public DataObject getProcessInfo(String pdid,String dptdid,String toccode) throws AppException, BusinessException {
		DataObject vdo = DataObject.getInstance();
		
		SinglePDConvert pdconvert = new SinglePDConvert(pdid);
		String pdlabel = pdconvert.getPDLabel();
		
		DPWithPDConvert dpconvert = new DPWithPDConvert(pdid);
		vdo = dpconvert.getDP(dptdid,toccode);
		
		if(vdo == null) {
			throw new AppException("获取流程任务名称失败，业务流程任务编号："+pdid+"  流程任务编号："+dptdid+"  toccode："+toccode);
		}
		vdo.put("pdlabel", pdlabel);
		
		return vdo;
	}
	public DataObject getProcessLabel(DataObject para) throws AppException, BusinessException {
		String pdid = para.getString("pdid");
		String dptdid = para.getString("dptdid");
		String toccode = para.getString("toccode");
		
		return getProcessInfo(pdid,dptdid,toccode);
	}

	public DataStore combineDSCol(DataStore para) throws AppException {
		DataStore newDS = DataStore.getInstance();
		for(DataObject empdo :para) {
			String empno = empdo.getString("empno");
			String orgno = empdo.getString("orgno");
			String orgname = empdo.getString("orgname");
			int index = newDS.find("empno == "+empno);
			
			if(index>=0) {
				String temporgno = newDS.getString(index, "orgno");
				String temporgname = newDS.getString(index, "orgname");
				
				newDS.put(index, "orgno", temporgno+','+orgno);
				newDS.put(index, "orgname", temporgname+','+orgname);
			}else {
				newDS.addRow(empdo);
			}
		}
		return newDS;
	}
	
	public DataObject getTreeKey(DataObject para) throws AppException{
		int nodenum = para.getInt("sn"); 
		de.clearSql();
		de.addSql("select a.fnfolderid,a.folderlabel ");
		de.addSql("  from odssu.fn_folder a ");
		de.addSql(" where a.pfnfolderid is null ");
		DataStore folderds = de.query();
		
		if(folderds == null || folderds.rowCount() <nodenum +1) {
			throw new AppException("未获取到树节点！");
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("key", folderds.getString(nodenum, "fnfolderid"));
		return vdo;
	}
	
	/**
	 * 获取当前目录下的资源信息
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject getSysFolderInfo(DataObject para) throws AppException{
		DataObject vdo = DataObject.getInstance();
		String folderid = para.getString("folderno");
		String folderpath = getSysFolderPath(folderid);
		DataObject numdo = getPdNum(folderid);
		int pdnum = numdo.getInt("pdnum");
		int dpnum = numdo.getInt("dpnum");
		String result = "目录["+folderpath+"]下共有"+pdnum+"项业务流程，共有"+dpnum+"项流程任务";
		vdo.put("result", result);
		return vdo;
	}
	/**
	 * 查询当前目录的路径，当前为根目录时返回"";
	 * @param folderid
	 * @return
	 * @throws AppException
	 */
	public String getSysFolderPath(String folderid) throws AppException{
		String folderpath = "";
		DataStore vds = DataStore.getInstance();
		de.clearSql();
		de.addSql("select a.P_CLASSIFICATION_NO,a.CLASSIFICATION_LABEL ");
		de.addSql("  from bpzone.biz_classification a ");
		de.addSql(" where a.CLASSIFICATION_NO = :folderid ");
		de.setString("folderid", folderid);
		vds = de.query();
		if(vds == null || vds.rowCount() ==0) {
			throw new AppException("无效的CLASSIFICATION_NO："+folderid);
		}
		folderpath = vds.getString(0, "CLASSIFICATION_LABEL");
		folderid = vds.getString(0, "P_CLASSIFICATION_NO");
		while(true) {
			vds = DataStore.getInstance();
			de.clearSql();
			de.addSql("select a.P_CLASSIFICATION_NO,a.CLASSIFICATION_LABEL ");
			de.addSql("  from bpzone.biz_classification a ");
			de.addSql(" where a.CLASSIFICATION_NO = :folderid ");
			de.addSql("   and a.CLASSIFICATION_NO <> 'root' ");
			de.setString("folderid", folderid);
			vds = de.query();
			if (vds == null ||vds.rowCount() ==0) {
				return folderpath;
			}
			folderpath = vds.getString(0, "CLASSIFICATION_LABEL")+"→"+folderpath;
			folderid = vds.getString(0, "P_CLASSIFICATION_NO");
		}
	}
	public DataObject getPdNum (String folderid) throws AppException {
		int pdnum = 0;
		int dpnum = 0;
		de.clearSql();
		de.addSql("select a.CLASSIFICATION_NO,a.CLASSIFICATION_LABEL ");
		de.addSql("  from bpzone.biz_classification a ");
		de.addSql(" where a.P_CLASSIFICATION_NO = :folderid ");
		de.setString("folderid", folderid);
		DataStore vds = de.query();
		if(vds == null || vds.rowCount() == 0) {
			pdnum = pdnum+getCurrentPdNum(folderid);
			dpnum = dpnum+getCurrentDpNum(folderid);
		}else {
			for(int rownum = 0; rownum<vds.rowCount();rownum++) {
				DataObject temp = getPdNum(vds.getString(rownum, "CLASSIFICATION_NO"));
				pdnum = pdnum +temp.getInt("pdnum");
				dpnum = dpnum +temp.getInt("dpnum");
			}
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("pdnum", pdnum);
		vdo.put("dpnum", dpnum);
		return vdo;
	}
	public int getCurrentPdNum(String folderid) throws AppException{
		de.clearSql();
		de.addSql("select t.pdid ");
		de.addSql("  from bpzone.process_define t ");
		de.addSql(" where t.CLASSIFICATIONBH = :folderid ");
		de.setString("folderid", folderid);
		DataStore vds = de.query();
		if(vds == null || vds.rowCount() ==0 ) {
			return 0;
		}else {
			return vds.rowCount();
		}
	}
	public int getCurrentDpNum(String folderid) throws AppException{
		de.clearSql();
		de.addSql("select distinct t.pdid,a.dptdid ");
		de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a ");
		de.addSql(" where t.CLASSIFICATIONBH like :folderid ");
		de.addSql("   and a.pdid = t.pdid ");
		de.setString("folderid", folderid);
		DataStore vds = de.query();
		if(vds == null || vds.rowCount() ==0 ) {
			return 0;
		}else {
			return vds.rowCount();
		}
	}
}
