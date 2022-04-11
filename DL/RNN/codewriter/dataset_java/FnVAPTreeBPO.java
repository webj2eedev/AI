package com.dw.hsuods.vap.fn;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class FnVAPTreeBPO extends BPO{

	/**
	 * @描述：转向流程任务一览的Leaf页面
	 * @param para
	 * @return
	 * @throws Exception 2015-6-12
	 */
	public DataObject queryfnylInfor(DataObject para) throws Exception {
		DataStore dsfn = this.queryGnrwLike(para).getDataStore("fn");
		DataObject result = DataObject.getInstance();
		result.put("fn", dsfn);
		return result;
	}

	/**
	 * @描述：点击Fn目录的节点，展示Fn
	 * @param para
	 * @return
	 * @throws Exception 2015-6-11
	 */
	public DataObject queryGnrwByFolder(DataObject para) throws Exception {
		String folderid = para.getString("folderid");
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		de.clearSql();
  		de.addSql("select distinct a.functionid, a.functionname ,a.pdid ");
  		de.addSql("from odssu.appfunction a, ");
  		de.addSql("     odssu.db_appfunction b  ");
  		de.addSql("where a.functionid=b.functionid ");
  		de.addSql("      and a.fnfolderid=:folderid ");
  		de.addSql("      and b.dbid=:dbid ");
		this.de.setString("folderid", folderid);
		this.de.setString("dbid", dbid);
		DataStore dsfn = this.de.query();

		DataObject result = DataObject.getInstance();
		result.put("fn", dsfn);
		return result;
	}

	/**
	 * 方法简介：适用的岗位类型
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月19日
	 */
	public DataObject queryfnGwlb(DataObject para) throws Exception {
		// 从前台获取功能任务ID
		String functionid = para.getString("functionid");
  		de.clearSql();
		DataStore vds = DataStore.getInstance();
		DataStore dsgwlb = DataStore.getInstance();
		DataObject result = DataObject.getInstance();

		// 根据fnid获取功能适用的岗位类别
		de.clearSql();
  		de.addSql(" select distinct roletypeno from odssu.fn_roletype where functionid  = :functionid ");
		this.de.setString("functionid", functionid);
		vds = this.de.query();

		// 遍历结果集将类型名称取出放入stringbuffer中
		StringBuffer stringBF = new StringBuffer();
		stringBF.setLength(0);
		if(vds != null && vds.rowCount() != 0){
			for (int i = 0; i < vds.rowCount(); i++) {
				stringBF.append(OdssuUtil.getRoleTypeNameByTypeNo(vds.getString(i, "roletypeno"))
						+ "\n");
			}
		}
		dsgwlb.put(0, "gwlb",stringBF.toString());

		result.put("lbds", dsgwlb);

		return result;

	}

	/**
	 * 方法简介：查询流程业务范畴信息
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryfnYwfc(DataObject para) throws AppException, BusinessException {
		String functionid = para.getString("functionid");
  		de.clearSql();
		if (functionid == null || "".equals(functionid.trim())) {
			throw new AppException("传入的functionid为空");
		}

		DataStore dsbusscope = DataStore.getInstance();
		StringBuffer pubstr = new StringBuffer();
		// 养老收
		DataObject ylvdo = DataObject.getInstance();
		ylvdo.put("ywmc", "养老收");
		ylvdo.put("busscope1", "101");
		ylvdo.put("selected1", "1");
		ylvdo.put("busscope2", "201");
		ylvdo.put("selected2", "1");
		ylvdo.put("busscope3", "701");
		ylvdo.put("selected3", "1");
		dsbusscope.addRow(ylvdo);
		pubstr.append("('101','201','701',");

		// 医疗收
		DataObject medigetvdo = DataObject.getInstance();
		medigetvdo.put("ywmc", "医疗收");
		medigetvdo.put("busscope1", "102");
		medigetvdo.put("selected1", "1");
		medigetvdo.put("busscope2", "202");
		medigetvdo.put("selected2", "1");
		medigetvdo.put("busscope3", "801");
		medigetvdo.put("selected3", "1");
		dsbusscope.addRow(medigetvdo);
		pubstr.append("'102','202','801',");
		// 工伤收
		DataObject gsgetvdo = DataObject.getInstance();
		gsgetvdo.put("ywmc", "工伤收");
		gsgetvdo.put("busscope1", "103");
		gsgetvdo.put("selected1", "1");
		gsgetvdo.put("busscope2", "203");
		gsgetvdo.put("selected2", "1");
		gsgetvdo.put("busscope3", "");
		gsgetvdo.put("selected3", "0");
		dsbusscope.addRow(gsgetvdo);
		pubstr.append("'103','203',");
		// 生育收
		DataObject bbgetvdo = DataObject.getInstance();
		bbgetvdo.put("ywmc", "生育收");
		bbgetvdo.put("busscope1", "104");
		bbgetvdo.put("selected1", "1");
		bbgetvdo.put("busscope2", "204");
		bbgetvdo.put("selected2", "1");
		bbgetvdo.put("busscope3", "");
		bbgetvdo.put("selected3", "0");
		dsbusscope.addRow(bbgetvdo);
		pubstr.append("'104','204',");
		// 失业收
		DataObject ljgetvdo = DataObject.getInstance();
		ljgetvdo.put("ywmc", "失业收");
		ljgetvdo.put("busscope1", "105");
		ljgetvdo.put("selected1", "1");
		ljgetvdo.put("busscope2", "205");
		ljgetvdo.put("selected2", "1");
		ljgetvdo.put("busscope3", "");
		ljgetvdo.put("selected3", "0");
		dsbusscope.addRow(ljgetvdo);
		pubstr.append("'105','205',");

		// 养老支
		DataObject ylsetvdo = DataObject.getInstance();
		ylsetvdo.put("ywmc", "养老支");
		ylsetvdo.put("busscope1", "301");
		ylsetvdo.put("selected1", "1");
		ylsetvdo.put("busscope2", "401");
		ylsetvdo.put("selected2", "1");
		ylsetvdo.put("busscope3", "702");
		ylsetvdo.put("selected3", "1");
		dsbusscope.addRow(ylsetvdo);
		pubstr.append("'301','401','702',");
		// 医疗支
		DataObject medisetvdo = DataObject.getInstance();
		medisetvdo.put("ywmc", "医疗支");
		medisetvdo.put("busscope1", "302");
		medisetvdo.put("selected1", "1");
		medisetvdo.put("busscope2", "402");
		medisetvdo.put("selected2", "1");
		medisetvdo.put("busscope3", "802");
		medisetvdo.put("selected3", "1");
		dsbusscope.addRow(medisetvdo);
		pubstr.append("'302','402','802',");
		// 工伤支
		DataObject gssetvdo = DataObject.getInstance();
		gssetvdo.put("ywmc", "工伤支");
		gssetvdo.put("busscope1", "303");
		gssetvdo.put("selected1", "1");
		gssetvdo.put("busscope2", "403");
		gssetvdo.put("selected2", "1");
		gssetvdo.put("busscope3", "");
		gssetvdo.put("selected3", "0");
		dsbusscope.addRow(gssetvdo);
		pubstr.append("'303','403',");
		// 生育支
		DataObject bbsetvdo = DataObject.getInstance();
		bbsetvdo.put("ywmc", "生育支");
		bbsetvdo.put("busscope1", "304");
		bbsetvdo.put("selected1", "1");
		bbsetvdo.put("busscope2", "404");
		bbsetvdo.put("selected2", "1");
		bbsetvdo.put("busscope3", "");
		bbsetvdo.put("selected3", "0");
		dsbusscope.addRow(bbsetvdo);
		pubstr.append("'304','404',");
		// 失业支
		DataObject sysetvdo = DataObject.getInstance();
		sysetvdo.put("ywmc", "失业支");
		sysetvdo.put("busscope1", "305");
		sysetvdo.put("selected1", "1");
		sysetvdo.put("busscope2", "405");
		sysetvdo.put("selected2", "1");
		sysetvdo.put("busscope3", "");
		sysetvdo.put("selected3", "0");
		dsbusscope.addRow(sysetvdo);
		pubstr.append("'305','405')");

		dsbusscope = this.dealYwfcData(dsbusscope, functionid);

		// 其他业务范畴
		DataStore dsotherscope = DataStore.getInstance();
		de.clearSql();
  		de.addSql(" select a.scopeno,a.scopename,'1' selected ");
  		de.addSql("   from odssu.business_scope a ");
  		de.addSql("  where a.scopeno not in " + pubstr.toString() + " ");
		dsotherscope = de.query();
		if(dsotherscope != null && dsotherscope.rowCount() !=0){
			dsotherscope = this.dealOtherYwfcData(dsotherscope, functionid, pubstr);
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("vdsywfc", dsbusscope);
		vdo.put("dsotherscope", dsotherscope);
		return vdo;

	}

	/**
	 * 方法简介：处理其他业务范畴
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	private DataStore dealOtherYwfcData(DataStore dsotherscope,
			String functionid, StringBuffer pubstr) throws AppException, BusinessException {
  		de.clearSql();
  		de.addSql(" select distinct a.scopeid scopeno                 ");
  		de.addSql("   from odssu.fn_business_scope a   ");
  		de.addSql("   where a.functionid = :functionid               ");
  		de.addSql("  and  a.scopeid not in " + pubstr.toString() + " ");
		de.setString("functionid", functionid);
		DataStore dsOrgScope = de.query();
		if(dsOrgScope != null && dsOrgScope.rowCount()!= 0){
			for (DataObject vdo : dsOrgScope) {
				String scopeno = vdo.getString("scopeno");
				int j = dsotherscope.find(" scopeno == " + scopeno);
				if (j >= 0) {
					dsotherscope.put(j, "selected", "2");
				}
			}
		}
		return dsotherscope;
	}

	/**
	 * 方法简介：处理业务范畴
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	private DataStore dealYwfcData(DataStore dsscope, String functionid) throws AppException, BusinessException {
  		de.clearSql();
  		de.addSql(" select  distinct a.scopeid scopeno                 ");
  		de.addSql("   from odssu.fn_business_scope a   ");
  		de.addSql("   where a.functionid = :functionid               ");
		de.setString("functionid", functionid);
		DataStore dsOrgScope = de.query();
		if(dsOrgScope != null && dsOrgScope.rowCount()!=0){
			for (DataObject vdo : dsOrgScope) {
				String scopeno = vdo.getString("scopeno");
				int j = dsscope.find(" busscope1 == " + scopeno);
				if (j >= 0) {
					dsscope.put(j, "selected1", "2");
				} else {
					j = dsscope.find("busscope2 == " + scopeno);
					if (j >= 0) {
						dsscope.put(j, "selected2", "2");
					} else {
						j = dsscope.find("busscope3 == " + scopeno);
						if (j >= 0) {
							dsscope.put(j, "selected3", "2");
						} else {
							// 暂时break掉 其他业务范畴类型
							continue;
							// this.bizException("该机构配置业务范畴出错");
						}
					}
	
				}
			}
		}
		return dsscope;
	}

	/**
	 * 方法简介：加载责任岗位页面grid数据
	 * 
	 * @author fandq
	 * @throws Exception
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject loadZrgw(DataObject para) throws Exception {
		// 获取流程任务ID
		String functionid = para.getString("functionid");
  		de.clearSql();

		// 查询流程任务对应的岗位信息
		de.clearSql();
  		de.addSql(" select distinct a.roleno,'1' zyfw,b.rolename,null faceorg,null inorg,b.deforgno, b.roletype, b.jsgn ");
  		de.addSql(" from  odssu.role_function_manual a,  ");
  		de.addSql("  odssu.roleinfor b ");
  		de.addSql(" where a.roleno = b.roleno ");
  		de.addSql("   and functionid = :functionid ");
		this.de.setString("functionid", functionid);
		DataStore roleds = this.de.query();
		DataStore result = DataStore.getInstance();
		// 根据查询出的岗位编号查询其对应的定义机构，是否全局。。。
		if(roleds!= null && roleds.rowCount() != 0){
			for (int i = 0; i < roleds.rowCount(); i++) {
				if (roleds.getString(i, "rolename") == null
						|| roleds.getString(i, "rolename").equals("")) {
					continue;
				}
				String roleno = roleds.getString(i, "roleno");
				String deforg = roleds.getString(i, "deforgno");
				// 角色的角色类型，即流程使适用的岗位类型
				String roletype = roleds.getString(i, "roletype");
	
				// 定义机构、岗位编号、岗位名称
				if (deforg == null || deforg.equals("")) {
					roleds.put(i, "deforg", "");
				} else {
	
					roleds.put(i, "deforg", getOrgName(deforg));
				}
				if (roleds.getString(i, "jsgn") != null
						&& !roleds.getString(i, "jsgn").equals("")) {
					if (deforg == null || deforg.equals("")) {
						roleds.put(i, "zyfw", "0");
					}
					// 岗位为全局岗位
					else if (OdssuUtil.getOrgTypeByOrgNo(deforg)
						.equals(OdssuContants.ORGTYPE_DSRSXT)
							|| OdssuUtil.getOrgTypeByOrgNo(deforg)
								.equals(OdssuContants.ORGTYPE_SRSXT)) {
						// 全局岗位标志
						roleds.put(i, "zyfw", "2");
						// 处科室全局内岗
						if (roletype.equals("HS_RSJKSNPGWL")) {
							roleds.put(i, "faceorg", "处科室");
						}
						// 人社站全局内岗
						if (roletype.equals("HS_RSZNPGWL")) {
							roleds.put(i, "faceorg", "村社区");
						}
						// 人社所全局内岗
						if (roletype.equals("HS_RSSNPGWL")) {
							roleds.put(i, "faceorg", "乡镇街道");
						}
						// 全局柜员岗位类
						if (roletype.equals("HS_GYL")) {
							roleds.put(i, "faceorg", "地市局");
						}
						// 全局外岗
						if (roletype.equals("HS_QDSYWGLL")
								|| roletype.equals("HS_JBJYWJBL")) {
							roleds.put(i, "faceorg", "业务经办机构");
						}
	
						// 专用内岗
					} else if (roleds.getString(i, "jsgn").equals("4")) {
						// 处科室内岗
						if (roletype.equals("HS_RSJKSNPGWL")) {
							roleds.put(i, "faceorg", "处科室");
						}
						// 人社站内岗
						if (roletype.equals("HS_RSZNPGWL")) {
							roleds.put(i, "faceorg", "村社区");
						}
						// 人社所内岗
						if (roletype.equals("HS_RSSNPGWL")) {
							roleds.put(i, "faceorg", "乡镇街道");
						}
						// 专用外岗
					} else if (roleds.getString(i, "jsgn").equals("3")) {
						// 根据岗位编号获取外岗faceorg和inorg
						de.clearSql();
  						de.addSql(" select b.orgname faceorg,c.orgname inorg   ");
  						de.addSql(" from odssu.outer_duty a,                   ");
  						de.addSql("      odssu.orginfor b,                     ");
  						de.addSql("      odssu.orginfor c                      ");
  						de.addSql(" where a.roleno = :roleno                         ");
  						de.addSql("  and a.faceorgno = b.orgno                 ");
  						de.addSql("  and a.inorgno = c.orgno                   ");
						this.de.setString("roleno", roleno);
						DataStore ds = this.de.query();
						if (ds!= null && ds.rowCount() > 0) {
	
							roleds.put(i, "faceorg", ds.getString(0, "faceorg"));
							roleds.put(i, "inorg", ds.getString(0, "inorg"));
						}
					}
				}
			
				result.put(result.rowCount(), "roleno", roleds.getString(i, "roleno"));
				result.put(result.rowCount() - 1, "zyfw", roleds.getString(i, "zyfw"));
				result.put(result.rowCount() - 1, "rolename", roleds.getString(i, "rolename"));
				result.put(result.rowCount() - 1, "faceorg", roleds.getString(i, "faceorg"));
				result.put(result.rowCount() - 1, "inorg", roleds.getString(i, "inorg"));
				result.put(result.rowCount() - 1, "deforg", roleds.getString(i, "deforg"));
			}
		}
		DataObject res = DataObject.getInstance();

		res.put("ds", result);

		return res;

	}

	private String getOrgName(String orgno) throws AppException {
  		de.clearSql();
		String orgname = " ";

		de.clearSql();
  		de.addSql(" select orgname from odssu.orginfor where orgno = :orgno ");
		this.de.setString("orgno", orgno);
		DataStore ds = this.de.query();
		if (ds != null && ds.rowCount() != 0) {
			orgname = ds.getString(0, "orgname");
		}

		return orgname;

	}

	/**
	 * @描述：流程任务
	 * @param para
	 * @return
	 * @throws Exception 2015-6-12
	 */
	public DataObject queryGnrwLike(DataObject para) throws Exception {
		String label = para.getString("label", "");
		String fnlabel = "%" + label + "%";
		DataObject result = DataObject.getInstance();
		// 查询机构下允许的fn：查询条件 DBID 
		de.clearSql();
  		de.addSql("select distinct a.functionid , a.functionname , a.pdid,a.fnfolderid folderid, h.folderlabel  ");
  		de.addSql("from odssu.appfunction a, ");
		de.addSql("     odssu.fn_folder h ");
		de.addSql("      where a.fnfolderid=h.fnfolderid ");
		if (label != null && !label.equals("")) {
  			de.addSql("  and (a.functionname like :fnlabel or h.folderlabel like :fnlabel or a.functionid like  :fnlabel)");
  			de.setString("fnlabel", fnlabel);
		}
		DataStore dsfn = this.de.query();
		result.put("fn", dsfn);
		return result;
	}
}
