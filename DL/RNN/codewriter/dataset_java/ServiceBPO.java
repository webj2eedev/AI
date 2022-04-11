package com.dw.service;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.XMLUtil;
import com.dareway.framework.workFlow.BPO;
import com.dareway.framework.dbengine.DE;

public class ServiceBPO extends BPO{

	/**
	 * 方法简介：获取DBID列表
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月21日
	 */
	public DataObject getOdsDbid(DataObject para) throws AppException {
		// 定义变量

		DE de = DE.getInstance();
		DataStore dbidds = DataStore.getInstance();
		DataObject result = DataObject.getInstance();

		// 进行数据库查询，返回查询到的DBID
		de.clearSql();
  		de.addSql(" select dbid from odssu.dbidinfo ");
		dbidds = de.query();

		result.put("ds_dbid", dbidds);

		return result;

	}

	/**
	 * 方法简介：修改全部function
	 * 
	 * @author fandq
	 * @throws Exception
	 * @date 创建时间 2015年8月21日
	 */
	public DataObject setAllFnid(DataObject para) throws Exception {
		System.out.println("同步所有FN开始!");
		// 定义变量

		DE de = DE.getInstance();
		
		String xml = para.getString("inparado");

		para = XMLUtil.xmlStringToDataObject(xml);

		// 获取数据
		String appid = para.getString("appid");
		DataStore ds_fn_bussiness_scope = para.getDataStore("ds_fn_bussiness_scope");
		DataStore ds_fn_roletype = para.getDataStore("ds_fn_roletype");
		DataStore ds_db_appFunction = para.getDataStore("ds_db_appFunction");
		DataStore ds_fn_Folder = para.getDataStore("ds_fn_Folder");
		DataStore ds_appFunction = para.getDataStore("ds_appFunction");

		// 进行参数校验
		if (appid == null || appid.equals("")) {
			throw new BusinessException("调用setAllFnid服务出错，传入的appid为空。");
		}
		if (ds_appFunction == null || ds_appFunction.rowCount() == 0) {
			throw new Exception("function 基本信息不能为空！");
		}
		if (ds_fn_roletype == null) {
			throw new Exception("缺少角色关系");
		}
		if (ds_db_appFunction == null) {
			throw new Exception("缺少DBID关系");
		}
		if (ds_fn_Folder == null) {
			throw new Exception("缺少目录关系");
		}
		if (ds_fn_bussiness_scope == null) {
			throw new Exception("缺少业务范畴关系");
		}

		// 向数据库中插入相应的信息
		if (ds_appFunction.rowCount() != 0) {

			// 首先删除数据库中现有的function信息
			delFnbusinessscopeAboutAppid(appid);
			delFnroletypeAboutAppid(appid);
			delDbappfunctionAboutAppid(appid);
			delFnfolderAboutAppid(appid);
			delAppfunctionAboutAppid(appid);
			// 向基本信息表中插入信息
			de.clearSql();
  			de.addSql(" insert into odssu.appfunction (functionid,functionname,pdid,fnfolderid,appid)");
  			de.addSql("  values (	 :functionid 	 ,   :functionname 	 ,  :pdid  ,  :fnfolderid,  :appid)   ");
			DataStore vds = DataStore.getInstance();

  			for (int i = 0; i < ds_appFunction.rowCount(); i++) {
				String functionid = ds_appFunction.getString(i, "functionid");
				String functionname = ds_appFunction.getString(i, "functionname");
				String pdid = ds_appFunction.getString(i, "pdid");
				String fnfolderid = ds_appFunction.getString(i, "fnfolderid");
				
				vds.put(i, "functionid", functionid);
				vds.put(i, "functionname", functionname);
				vds.put(i, "pdid", pdid);
				vds.put(i, "fnfolderid", fnfolderid);
				vds.put(i, "appid", appid);
			}
  			de.batchUpdate(vds);
  			
			// 业务范畴联系
			if (ds_fn_bussiness_scope.rowCount() != 0) {
				// 向功能与业务范畴表中插入信息
				de.clearSql();
  				de.addSql(" insert into odssu.fn_business_scope (functionid,scopeid)");
  				de.addSql("  values (	 :functionid 	 ,   :scopeid 	 )");
  				vds.clear();
  				
				for (int i = 0; i < ds_fn_bussiness_scope.rowCount(); i++) {
					String functionid = ds_fn_bussiness_scope.getString(i, "functionid");
					String scopeid = ds_fn_bussiness_scope.getString(i, "scopeid");
					vds.put(i, "functionid", functionid);
					vds.put(i, "scopeid", scopeid);
				}
	  			de.batchUpdate(vds);
			}
			// 角色类型联系
			if (ds_fn_roletype.rowCount() != 0) {
				// 向功能与角色类型表中插入信息
				de.clearSql();
				vds.clear();
  				de.addSql(" insert into odssu.fn_roletype (functionid,roletypeno)");
  				de.addSql("  values (	 :functionid 	 ,   :roletypeno 	 )");
				for (int i = 0; i < ds_fn_roletype.rowCount(); i++) {
					String functionid = ds_fn_roletype.getString(i, "functionid");
					String roletypeno = ds_fn_roletype.getString(i, "roletypeno");
					vds.put(i, "functionid", functionid);
					vds.put(i, "roletypeno", roletypeno);
				}
	  			de.batchUpdate(vds);
			}
			// DBID联系
			if (ds_db_appFunction.rowCount() != 0) {
				// 向功能与DBID表中插入信息
				de.clearSql();
				vds.clear();
  				de.addSql(" insert into odssu.db_appfunction (functionid,dbid)");
  				de.addSql("  values (	 :functionid 	 ,   :dbid 	 )");
				for (int i = 0; i < ds_db_appFunction.rowCount(); i++) {
					String dbid = ds_db_appFunction.getString(i, "dbid");
					String functionid = ds_db_appFunction.getString(i, "functionid");
					vds.put(i, "functionid", functionid);
					vds.put(i, "dbid", dbid);

				}
	  			de.batchUpdate(vds);
			}
			// 修改目录联系
			if (ds_fn_Folder.rowCount() != 0) {
				// 向目录联系表中插入信息
				de.clearSql();
				vds.clear();
				de.addSql(" insert into odssu.fn_folder (fnfolderid,pfnfolderid,folderlabel)");
  				de.addSql("  values (	 :folderid 	 ,   :pfnfolderid 	 ,:folderlabel  ) ");
				for (int i = 0; i < ds_fn_Folder.rowCount(); i++) {
					String folderid = ds_fn_Folder.getString(i, "fnfolderid");
					String pfnfolderid = ds_fn_Folder.getString(i, "pfnfolderid");
					String folderlabel = ds_fn_Folder.getString(i, "folderlabel");
					vds.put(i, "folderid", folderid);
					vds.put(i, "pfnfolderid", pfnfolderid);
					vds.put(i, "folderlabel", folderlabel);
				}
	  			de.batchUpdate(vds);
			}

			// 清理fn与role联系
			de.clearSql();
  			de.addSql(" delete from odssu.role_function_manual a ");
  			de.addSql(" where not exists  ");
  			de.addSql(" ( select * from odssu.appfunction b where a.functionid = b.functionid ) ");
			de.update();
		}
		System.out.println("同步所有FN完成!");
		return DataObject.getInstance();
	}

	/**
	 * 方法简介：修改单行function
	 * 
	 * @author fandq
	 * @throws Exception
	 * @date 创建时间 2015年8月21日
	 */
	public DataObject setSingleFnid(DataObject para) throws Exception {
		// 定义变量

		DE de = DE.getInstance();

		// 获取数据 ，将xml串改为dataobject
		String xml = para.getString("inparado");
		para = XMLUtil.xmlStringToDataObject(xml);

		// 获取数据
		String functionid = para.getString("functionid");
		String functionname = para.getString("functionname");
		String pdid = para.getString("pdid");
		String fnfolderid = para.getString("fnfolderid");
		String appid = para.getString("appid");

		DataStore ds_fn_bussiness_scope = para.getDataStore("ds_fn_bussiness_scope");
		DataStore ds_fn_roletype = para.getDataStore("ds_fn_roletype");
		DataStore ds_db_appFunction = para.getDataStore("ds_db_appFunction");
		DataStore ds_related_FnFolder = para.getDataStore("ds_related_FnFolder");

		// 进行数据校验
		if (functionid == null || functionid.equals("")) {
			throw new Exception("functionid不能为空！");
		}
		if (functionname == null || functionid.equals("")) {
			throw new Exception("functionname不能为空！");
		}
		if (ds_fn_roletype == null) {
			throw new Exception("缺少角色关系");
		}
		if (ds_db_appFunction == null) {
			throw new Exception("缺少DBID关系");
		}
		if (ds_related_FnFolder == null) {
			throw new Exception("缺少目录关系");
		}
		if (ds_fn_bussiness_scope == null) {
			throw new Exception("缺少业务范畴关系");
		}

		// 删除表中信息
		// 基本信息
		if (functionid != null && !functionid.equals("")
				&& functionname != null && !functionname.equals("")) {
			// 删除基本信息表中信息
			delTableInfo("odssu.appfunction", functionid);
		}
		// 业务范畴联系
		if (ds_fn_bussiness_scope.rowCount() != 0) {
			// 删除功能与业务范畴关联表中信息
			delTableInfo("odssu.fn_business_scope", functionid);
		}
		// 角色类型联系
		if (ds_fn_roletype.rowCount() != 0) {
			// 删除功能与角色类型关联表中信息
			delTableInfo("odssu.fn_roletype", functionid);
		}
		// DBID联系
		if (ds_db_appFunction.rowCount() != 0) {
			// 删除功能与DBID关联表中信息
			delTableInfo("odssu.db_appfunction", functionid);
		}

		// 首先判断时流程相关还是流程无关
		de.clearSql();
  		de.addSql(" select 1 from odsv.notbpfn_view where functionid = :functionid ");
		de.setString("functionid", functionid);
		DataStore vds = de.query();

		// BP相关
		if (vds.rowCount() > 0 || (pdid != null && !pdid.equals(""))) {
			// DBID联系
			if (ds_db_appFunction.rowCount() != 0) {
				// 向功能与DBID表中插入信息
				DataStore vps = DataStore.getInstance();
				de.clearSql();
  				de.addSql(" insert into odssu.db_appfunction (functionid,dbid)");
  				de.addSql("  values (	 :functionid  ,   :dbid  )");
				for (int i = 0; i < ds_db_appFunction.rowCount(); i++) {
					String dbid = ds_db_appFunction.getString(i, "dbid");
					vps.put(i, "functionid", functionid);
					vps.put(i, "dbid", dbid);
				}
	  			de.batchUpdate(vps);
	  		}
			// BP无关
		} else {
			// 查看DBID是否合适(传过来的DBID list与本地DBID list 是否有交集)
			// 进行数据库查询，返回查询到的DBID
			de.clearSql();
  			de.addSql(" select dbid from odssu.dbidinfo ");
			DataStore dbidds = de.query();
			dbidds.retainAll(ds_db_appFunction);
			// 没有交集，表示DBID不合适
			if (dbidds == null || dbidds.rowCount() == 0) {
				throw new Exception("DBID不合适！");
				// 有交集，将交集DBID插入数据库
			} else {
				// 向功能与DBID表中插入信息
				de.clearSql();
				DataStore vps = DataStore.getInstance();
  				de.addSql(" insert into odssu.db_appfunction (functionid,dbid)");
  				de.addSql("  values (	 :functionid 	 ,   :dbid 	 )");
				for (int i = 0; i < dbidds.rowCount(); i++) {
					String dbid = dbidds.getString(i, "dbid");
					vps.put(i, "functionid", functionid);
					vps.put(i, "dbid", dbid);

				}
	  			de.batchUpdate(vps);
			}

		}

		// 向数据库表中插入相关数据
		// 基本信息
		de.clearSql();
  		de.addSql(" insert into odssu.appfunction (functionid,functionname,pdid,fnfolderid，appid)");
  		de.addSql("  values (	 :functionid 	 ,  :functionname 	 ,:pdid	 ,  :fnfolderid,:appid)");
		de.setString("functionid", functionid);
		de.setString("functionname", functionname);
		de.setString("pdid", pdid);
		de.setString("fnfolderid", fnfolderid);
		de.setString("appid", appid);

		de.update();

		// 业务范畴联系
		if (ds_fn_bussiness_scope.rowCount() != 0) {
			// 向功能与业务范畴表中插入信息
			DataStore vps = DataStore.getInstance();
			de.clearSql();
  			de.addSql(" insert into odssu.fn_business_scope (functionid,scopeid)");
  			de.addSql("  values (	 :functionid 	 ,   :scopeid 	 )");
			for (int i = 0; i < ds_fn_bussiness_scope.rowCount(); i++) {
				String scopeid = ds_fn_bussiness_scope.getString(i, "scopeid");
				vps.put(i, "functionid", functionid);
				vps.put(i, "scopeid", scopeid);
			}
  			de.batchUpdate(vps);
		}
		// 角色类型联系
		if (ds_fn_roletype.rowCount() != 0) {
			// 向功能与角色类型表中插入信息
			de.clearSql();
			DataStore vps = DataStore.getInstance();
  			de.addSql(" insert into odssu.fn_roletype (functionid,roletypeno)");
  			de.addSql("  values (	 :functionid 	 ,   :roletypeno 	 )");
			for (int i = 0; i < ds_fn_roletype.rowCount(); i++) {
				String roletypeno = ds_fn_roletype.getString(i, "roletypeno");
				vps.put(i, "functionid", functionid);
				vps.put(i, "roletypeno", roletypeno);
			}
  			de.batchUpdate(vps);
		}

		// 修改目录联系
		if (ds_related_FnFolder.rowCount() != 0) {
			// 删除当前目录中对应的链信息
			de.clearSql();
			DataStore vps = DataStore.getInstance();
  			de.addSql(" delete from odssu.fn_folder where fnfolderid = :folderid ");
			for (int i = 0; i < ds_related_FnFolder.rowCount(); i++) {
				String folderid = ds_related_FnFolder.getString(i, "fnfolderid");
				vps.put(i, "folderid", folderid);
			}
  			de.batchUpdate(vps);

			// 向目录联系表中插入信息
  			vps.clear();
			de.clearSql();
  			de.addSql(" insert into odssu.fn_folder (fnfolderid,pfnfolderid,folderlabel)");
  			de.addSql("  values (	 :folderid 	 ,   :pfnfolderid 	 ,:folderlabel  ) ");
			for (int i = 0; i < ds_related_FnFolder.rowCount(); i++) {
				String folderid = ds_related_FnFolder.getString(i, "fnfolderid");
				String pfnfolderid = ds_related_FnFolder.getString(i, "pfnfolderid");
				String folderlabel = ds_related_FnFolder.getString(i, "folderlabel");
				vps.put(i, "folderid", folderid);
				vps.put(i, "pfnfolderid", pfnfolderid);
				vps.put(i, "folderlabel", folderlabel);
			}
  			de.batchUpdate(vps);
		}

		return DataObject.getInstance();
	}

	public DataObject delSingleFnid(DataObject para) throws AppException {
		DE de = DE.getInstance();
		
		// 获取数据 ，将xml串改为dataobject
		String xml = para.getString("inparado");
		para = XMLUtil.xmlStringToDataObject(xml);

		// 获取变量functionid
		String functionid = para.getString("functionid");

		// 删除表中对应的数据
		if (functionid != null && !functionid.equals("")) {
			delTableInfo("odssu.appfunction", functionid);
			delTableInfo("odssu.fn_business_scope", functionid);
			delTableInfo("odssu.fn_roletype", functionid);
			delTableInfo("odssu.db_appfunction", functionid);
		}

		// 清理fn与role联系
		de.clearSql();
  		de.addSql(" delete from odssu.role_function_manual a ");
  		de.addSql(" where not exists  ");
  		de.addSql(" ( select * from odssu.appfunction b where a.functionid = b.functionid ) ");
		de.update();

		return DataObject.getInstance();
	}

	/**
	 * 方法简介：删除原有的表中的数据
	 * 
	 * @author fandq
	 * @throws AppException
	 * @date 创建时间 2015年8月21日
	 */
	private void delTableInfo(String tablename, String functionid) throws AppException {
		// 定义变量

		DE de = DE.getInstance();
		
		// 删除appfunction表中数据
		de.clearSql();
  		de.addSql(" delete from " + tablename);			//lzpmark
		if (functionid != null && !functionid.equals("")) {
    			de.addSql(" where functionid = :functionid");
    			de.setString("functionid", functionid);
		}
		de.update();

	}

	/**
	 * 方法简介.删除某一个应用下的appfunction
	 * 
	 * @author fandq
	 * @date 创建时间 2015年12月17日
	 */
	private void delAppfunctionAboutAppid(String appid) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
    	de.addSql("delete from odssu.appfunction where appid = :appid ");
		de.setString("appid", appid);
		de.update();

	}

	/**
	 * 方法简介.删除某一个应用下的fnid相关的scope
	 * 
	 * @author fandq
	 * @date 创建时间 2015年12月17日
	 */
	private void delFnbusinessscopeAboutAppid(String appid) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("delete from odssu.fn_business_scope a ");
  		de.addSql(" where exists (select 1 ");
  		de.addSql("          from odssu.appfunction b ");
  		de.addSql("         where a.functionid = b.functionid ");
  		de.addSql("           and b.appid = :appid) ");
		de.setString("appid", appid);
		de.update();
	}

	/**
	 * 方法简介.删除关于一个appid的function的相关roletype
	 * 
	 * @author fandq
	 * @date 创建时间 2015年12月17日
	 */
	private void delFnroletypeAboutAppid(String appid) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("delete from odssu.fn_roletype a ");
  		de.addSql(" where exists (select 1 ");
  		de.addSql("          from odssu.appfunction b ");
  		de.addSql("         where a.functionid = b.functionid ");
  		de.addSql("           and b.appid = :appid) ");
		de.setString("appid", appid);
		de.update();
	}

	/**
	 * 方法简介.删除关于一个appid的function的相关dbid
	 * 
	 * @author fandq
	 * @date 创建时间 2015年12月17日
	 */
	private void delDbappfunctionAboutAppid(String appid) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("delete from odssu.db_appfunction a ");
  		de.addSql(" where exists (select 1 ");
  		de.addSql("          from odssu.appfunction b ");
  		de.addSql("         where a.functionid = b.functionid ");
  		de.addSql("           and b.appid = :appid) ");
		de.setString("appid", appid);
		de.update();
	}

	private void delFnfolderAboutAppid(String appid) throws AppException {
		// 获取appid相关function的根目录id

		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select fnfolderid ");
  		de.addSql("  from odssu.fn_folder fo ");
  		de.addSql(" where fo.fnfolderid in (select b.pfnfolderid ");
  		de.addSql("                           from odssu.appfunction a, odssu.fn_folder b ");
  		de.addSql("                          where a.fnfolderid = b.fnfolderid ");
  		de.addSql("                            and a.appid = :appid) ");
  		de.addSql("   and fo.pfnfolderid is null ");
		de.setString("appid", appid);
		DataStore tmpds = de.query();

		if (tmpds != null && tmpds.rowCount() != 0) {

			for (int j = 0; j < tmpds.rowCount(); j++) {

				String rootFolderId = tmpds.getString(j, "fnfolderid");

				de.clearSql();
				de.addSql("select a.fnfolderid ");
				de.addSql("from odssu.fn_folder a ");
				de.addSql("start with a.fnfolderid = :fnfolderid ");
				de.addSql("connect by  a.pfnfolderid = prior a.fnfolderid ");
				de.setString("fnfolderid", rootFolderId);
				DataStore tmpds1 = de.query();

				for (int i = 0; i < tmpds1.rowCount(); i++) {
					String fnfolderid = tmpds1.getString(i, "fnfolderid");
  					de.clearSql();
  					de.addSql(" delete from odssu.fn_folder where fnfolderid =:fnfolderid");
  					de.setString("fnfolderid", fnfolderid);
  					de.update();
				}
			}
		}

	}

}
