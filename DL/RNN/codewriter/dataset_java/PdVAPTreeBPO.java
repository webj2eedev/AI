package com.dw.hsuods.vap.pd;

import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;
import com.dw.util.multiSortUtil.MultiSortUtil;

public class PdVAPTreeBPO extends BPO{
	/**
	 * 方法简介：加载流程类别
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryPdlb(DataObject para) throws Exception {
		// 获取前台传过来的流程定义编号
		String pdid = para.getString("pdid");
		DataStore flagds = DataStore.getInstance();
		DataObject result = DataObject.getInstance();

		// 从数据库表中查出流程信息
		de.clearSql();
  		de.addSql(" select standardflag from bpzone.process_define");
  		de.addSql(" where pdid = :pdid ");
		de.setString("pdid", pdid);
		flagds = de.query();

		if(flagds == null || flagds.rowCount() ==0 ){
			throw new AppException("从数据库表中查出流程信息时出错，pdid为【"+pdid+"】。");
		}
		String standardflag = flagds.getString(0, "standardflag");
		result.put("standardflag", standardflag);

		return result;
	}

	/**
	 * 方法简介：加载流程目录
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryPdInfor(DataObject para) throws Exception {
		// 获取前台传过来的流程定义编号
		String pdid = para.getString("pdid");
  		de.clearSql();
		DataStore pdinfords = DataStore.getInstance();
		DataObject result = DataObject.getInstance();

		// 从数据库表中查出流程信息
		de.clearSql();
  		de.addSql(" select b.pdid,b.pdlabel,b.pdalias,b.standardflag,null lbsy");
  		de.addSql(" from  bpzone.process_define b  ");
  		de.addSql(" where b.pdid = :pdid ");
		this.de.setString("pdid", pdid);
		pdinfords = this.de.query();
		if(pdinfords == null || pdinfords.rowCount() == 0){
			throw new AppException("从数据库表中查出流程信息时出错，pdid为【"+pdid+"】。");
		}
		// 标准专用流程
		if (pdinfords.getString(0, "standardflag").equals("1")) {
			// 搜索DBID
			String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
			de.clearSql();
  			de.addSql(" select dbid from bpzone.pd_dbid where pdid = :pdid ");
			this.de.setString("pdid", pdid);
			DataStore dbidds = this.de.query();
			if (dbidds.find("dbid == " + dbid) != -1) {
				pdinfords.put(0, "lbsy", "0");
			} else {
				pdinfords.put(0, "lbsy", "1");
			}
		}
		// 客户化流程
		if (pdinfords.getString(0, "standardflag").equals("2")) {
			// 搜索流程对应的标准流程信息
			de.clearSql();
  			de.addSql(" select c.pdid,c.pdlabel,c.pdalias ");
  			de.addSql(" from bpzone.process_define a, ");
  			de.addSql("      bpzone.process_define c ");
  			de.addSql(" where a.pdid = :pdid ");
  			de.addSql(" and a.standard_pdid = c.pdid ");
			this.de.setString("pdid", pdid);
			DataStore standardpdds = this.de.query();
			if (standardpdds.rowCount() == 0) {
				throw new Exception("为找到该客户化流程的标准流程信息，请检查数据！");
			}
			pdinfords.put(0, "standardpd", standardpdds.getString(0, "pdalias")
					+ "(" + standardpdds.getString(0, "pdid") + ")");
		}

		result.put("pdinfords", pdinfords);

		return result;
	}

	/**
	 * 方法简介：加载流程流程图
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryPdBpmn(DataObject para) throws Exception {
		// 获取前台传过来的流程定义编号
		String pdid = para.getString("pdid");
		DataObject result = DataObject.getInstance();

		DataStore vds = DataStore.getInstance();
  		de.clearSql();
  		de.addSql(" select a.dpmnpng                            ");
  		de.addSql("   from bpzone.process_define_in_activiti a, ");
  		de.addSql("        bpzone.process_define b              ");
  		de.addSql("  where a.pdaid = b.pdaid                    ");
  		de.addSql("    and b.pdid = :pdid                           ");
		this.de.setString("pdid", pdid);

		vds = this.de.query();

		if(vds == null || vds.rowCount() ==0){
			throw new AppException("查询数据库时出错，返回的DataStore为空。");
		}
		// Blob png_content = (Blob) vds.getObject(0, "dpmnpng");
		byte[] png_content = vds.getByteArray(0, "dpmnpng");

		// 获取blob对象的byte流
		result.put("imageStream", png_content);

		// InputStream bpmnStream = ProcessUtil.getProcessDiagramByPdid(pdid);

		// result.put("imageStream", bpmnStream);

		return result;
	}



	/**
	 * 方法简介：查询流程业务范畴信息
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryPdYwfc(DataObject para) throws AppException, BusinessException {
		String pdid = para.getString("pdid");
		if (pdid == null || "".equals(pdid.trim())) {
			throw new AppException("传入的pdid为空");
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

		dsbusscope = this.dealYwfcData(dsbusscope, pdid);

		// 其他业务范畴
		DataStore dsotherscope = DataStore.getInstance();
  		de.clearSql();
		de.clearSql();
  		de.addSql(" select a.scopeno,a.scopename,'1' selected ");
  		de.addSql("   from odssu.business_scope a ");
  		de.addSql("  where a.scopeno not in " + pubstr.toString() + " ");
		dsotherscope = de.query();

		dsotherscope = this.dealOtherYwfcData(dsotherscope, pdid,pubstr);

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
	private DataStore dealOtherYwfcData(DataStore dsotherscope, String pdid,StringBuffer pubstr) throws AppException, BusinessException {
  		de.clearSql();
  		de.addSql(" select a.ywlxid scopeno                 ");
  		de.addSql("   from bpzone.process_businesstype a,   ");
  		de.addSql("        bpzone.process_define b          ");
  		de.addSql("   where a.pdaid = b.pdaid               ");
  		de.addSql("     and b.pdid = :pdid                     ");
  		de.addSql("  and  a.ywlxid not in " + pubstr.toString() + " ");
		de.setString("pdid", pdid);
		DataStore dsOrgScope = de.query();
		for (DataObject vdo : dsOrgScope) {
			String scopeno = vdo.getString("scopeno");
			int j = dsotherscope.find(" scopeno == " + scopeno);
			if (j >= 0) {
				dsotherscope.put(j, "selected", "2");
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
	private DataStore dealYwfcData(DataStore dsscope, String pdid) throws AppException, BusinessException {
  		de.clearSql();
  		de.addSql(" select a.ywlxid scopeno                 ");
  		de.addSql("   from bpzone.process_businesstype a,   ");
  		de.addSql("        bpzone.process_define b          ");
  		de.addSql("   where a.pdaid = b.pdaid               ");
  		de.addSql("     and b.pdid = :pdid                    ");
		de.setString("pdid", pdid);
		DataStore dsOrgScope = de.query();
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
		return dsscope;
	}

	/**
	 * 方法简介：查询普适流程流程的客户化流程信息
	 * 
	 * @author fandq
	 * @throws Exception
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryStandardPdInfor(DataObject para) throws Exception {
		// 获取参数
		String pdid = para.getString("pdid");
		DataStore pdds = DataStore.getInstance();
		DataObject result = DataObject.getInstance();
  		de.clearSql();

		// 根据pdid获取其客户化流程以及客户化流程对应的机构信息
		de.clearSql();
  		de.addSql(" select  distinct a.customed_pdid pdid,b.pdlabel,b.pdalias    ");
  		de.addSql(" from bpzone.pd_customed a,                                 ");
  		de.addSql("      bpzone.process_define b                              ");
  		de.addSql(" where a.standard_pdid = :pdid                                  ");
  		de.addSql("   and a.customed_pdid = b.pdid                           ");
		this.de.setString("pdid", pdid);

		pdds = this.de.query();

		// 将返回结果保存到dataobject中
		result.put("customeds", pdds);
		result.put("pdinfords", queryPdInfor(para).getDataStore("pdinfords"));

		return result;

	}

	/**
	 * 方法简介：查询标准流程的客户话流程机构信息
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject loadCustomedOrgData(DataObject para) throws AppException {
		// 获取参数
		String customedpdid = para.getString("customedpdid");
		DataStore pdds = DataStore.getInstance();
		DataObject result = DataObject.getInstance();
  		de.clearSql();

		// 根据标准流程ID和客户化流程ID获取其客户化流程以及客户化流程对应的机构信息
		de.clearSql();
  		de.addSql(" select  distinct a.orgno,b.orgname ");
  		de.addSql(" from bpzone.pd_customed a,   ");
  		de.addSql("      odssu.custome_footstone b   ");
  		de.addSql(" where a.orgno = b.orgno   ");
  		de.addSql(" and   a.customed_pdid = :customedpdid   ");
  		de.addSql(" order by a.orgno    ");
		this.de.setString("customedpdid", customedpdid);
		pdds = this.de.query();

		// 将返回结果保存到dataobject中
		result.put("customeorgds", pdds);

		return result;

	}

	public DataObject queryPdcusInfor(DataObject para) throws Exception {
		// 获取流程定义ID
		String pdid = para.getString("pdid");

		DataObject result = DataObject.getInstance();

		// 流程基本信息
		result.put("pdinfords", queryPdInfor(para).getDataStore("pdinfords"));
		// 流程适用的机构信息
		para.put("customedpdid", pdid);
		result.put("customeorgds", loadCustomedOrgData(para).getDataStore("customeorgds"));

		return result;
	}

	/**
	 * 方法简介：加载流程任务页面dp数据
	 * 
	 * @author fandq
	 * @throws AppException
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject queryPddp(DataObject para) throws AppException {
		// 获取变量
		String pdid = para.getString("pdid");
		DataStore dpds = DataStore.getInstance();// 保存全部dp
		DataStore dpads = DataStore.getInstance();// 保存隶属当前活动版本的流程的dp

		de.clearSql();
		DataObject result = DataObject.getInstance();

		// 根据pdid获取流程下所有dp
		de.clearSql();
		de.addSql(" select distinct '1' lslc,dptdid,dptdlabel  ");
		de.addSql("   from bpzone.dutyposition_task   ");
		de.addSql("  where pdid = :pdid                   ");
		de.addSql("    and nvl(status,'0') <> '2' ");
		this.de.setString("pdid", pdid);
		dpds = this.de.query();

		// 获取当前活动的流程版本的dp
		de.clearSql();
  		de.addSql(" select distinct a.dptdid        ");
  		de.addSql("   from bpzone.task_point a,      ");
  		de.addSql("        bpzone.process_define b   ");
  		de.addSql("  where a.pdaid = b.pdaid         ");
  		de.addSql("    and b.pdid = :pdid                ");
		this.de.setString("pdid", pdid);
		dpads = this.de.query();

		// 将两个结果进行比较，获取“√”逻辑
		for (int i = 0; i < dpds.rowCount(); i++) {
			// 将dpds中也在dpads中的，lslc设置为2（即隶属活动流程）
			if (dpads.find("dptdid == " + dpds.getString(i, "dptdid")) >= 0) {
				dpds.put(i, "lslc", "2");
			}

		}

		result.put("dpds", dpds);

		return result;

	}

	/**
	 * 方法简介：加载流程任务页面grid数据
	 * 
	 * @author fandq
	 * @throws Exception
	 * @date 创建时间 2015年8月17日
	 */
	public DataObject loadPDDP(DataObject para) throws Exception {
		// 获取流程任务ID
		String dptdid = para.getString("dptdid");
		String pdid = para.getString("pdid");
  		de.clearSql();
		DataStore lbds = DataStore.getInstance();
		DataStore roletypeds = DataStore.getInstance();
		StringBuffer roletypebf = new StringBuffer();

		// 查询流程任务对应的岗位信息
		de.clearSql();
  		de.addSql(" select distinct a.roleid roleno,'1' zyfw, b.rolename, null faceorg, null inorg, b.deforgno, b.roletype, b.jsgn ");
  		de.addSql(" from  bpzone.dutyposition_task_role a, ");
  		de.addSql(" odssu.roleinfor b ");
  		de.addSql(" where a.roleid = b. roleno ");
  		de.addSql("   and dptdid = :dptdid ");
  		de.addSql("   and pdid = :pdid ");
		this.de.setString("dptdid", dptdid);
		this.de.setString("pdid", pdid);
		DataStore roleds = this.de.query();
		
		DataStore result  = DataStore.getInstance();

		// 根据查询出的岗位编号查询其对应的定义机构，作用范围
		for (int i = 0; i < roleds.rowCount(); i++) {
			
			if (roleds.getString(i, "rolename")==null||roleds.getString(i, "rolename").equals("")) {
				continue;
			}
			String roleno = roleds.getString(i, "roleno");
			String deforg = roleds.getString(i,"deforgno");
			// 角色的角色类型，即流程使适用的岗位类型
			String roletype = roleds.getString(i,"roletype");
			
			// 定义机构、岗位编号、岗位名称
			if (deforg == null || deforg.equals("")) {
				roleds.put(i, "deforg", "");
			} else {

				roleds.put(i, "deforg", OdssuUtil.getOrgNameByOrgno(deforg));
			}
			if (roleds.getString(i,"jsgn") != null
					&& !roleds.getString(i,"jsgn").equals("")) {
				
				// 全地市岗位
				if (OdssuUtil.isRsxt(deforg)) {
					// 全地市岗位标志
					roleds.put(i, "zyfw", "2");
					// 适用于处科室
					if (roletype.equals("HS_RSJKSNPGWL")) {
						roleds.put(i, "faceorg", "处科室");
					}
					// 适用于人社站
					if (roletype.equals("HS_RSZNPGWL")) {
						roleds.put(i, "faceorg", "村社区");
					}
					// 适用于人社所
					if (roletype.equals("HS_RSSNPGWL")) {
						roleds.put(i, "faceorg", "乡镇街道");
					}
					// 适用于柜员
					if (roletype.equals("HS_GYL")) {
						roleds.put(i, "faceorg", "地市局");
					}
					// 适用于经办机构
					if (roletype.equals("HS_QDSYWGLL") || roletype.equals("HS_JBJYWJBL")) {
						roleds.put(i, "faceorg", "业务经办机构");
					}

					// 科室岗位
				} else if(OdssuUtil.isRscks(deforg)){
					roleds.put(i, "zyfw", "0");
					if (roleds.getString(i,"jsgn").equals("4")) {
						de.clearSql();
  						de.addSql(" select a.orgname faceorg,a.orgname inorg ");
  						de.addSql("   from odssu.inner_duty a ");
  						de.addSql("  where a.roleno = :roleno ");
						this.de.setString("roleno", roleno);
						DataStore ds = this.de.query();
						if (ds.rowCount() > 0) {
							roleds.put(i, "faceorg", ds.getString(0, "faceorg"));
							roleds.put(i, "inorg", ds.getString(0, "inorg"));
						}
						
					}
					if (roleds.getString(i,"jsgn").equals("3")) {
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
						if (ds.rowCount() > 0) {

							roleds.put(i, "faceorg", ds.getString(0, "faceorg"));
							roleds.put(i, "inorg", ds.getString(0, "inorg"));
						}
					}
				}else {
					if (roleds.getString(i,"jsgn").equals("4")) {
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
					}
					if (roleds.getString(i,"jsgn").equals("3")) {
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
						if (ds.rowCount() > 0) {

							roleds.put(i, "faceorg", ds.getString(0, "faceorg"));
							roleds.put(i, "inorg", ds.getString(0, "inorg"));
						}
					}
				}
			}
			result.put(result.rowCount(), "roleno", roleds.getString(i, "roleno"));
			result.put(result.rowCount()-1, "zyfw", roleds.getString(i, "zyfw"));
			result.put(result.rowCount()-1, "rolename", roleds.getString(i, "rolename"));
			result.put(result.rowCount()-1, "faceorg", roleds.getString(i, "faceorg"));
			result.put(result.rowCount()-1, "inorg", roleds.getString(i, "inorg"));
			result.put(result.rowCount()-1, "deforg", roleds.getString(i, "deforg"));
		}

		de.clearSql();
  		de.addSql(" select roletypeid , roletypelabel  roletypename");
  		de.addSql(" from bpzone.dproletype             ");
  		de.addSql(" where pdid = :pdid                     ");
  		de.addSql("   and dptdid = :dptdid                   ");
		this.de.setString("pdid", pdid);
		this.de.setString("dptdid", dptdid);
		roletypeds = this.de.query();
		// dp适用的岗位类别
		for (int j = 0; j < roletypeds.rowCount(); j++) {
			roletypebf.append(roletypeds.getString(j, "roletypename") + "\n");
		}
		lbds.put(0, "gwlb", roletypebf.toString());

		DataObject res = DataObject.getInstance();
		result = MultiSortUtil.multiSortDS(result, "rolename:desc");
		res.put("ds", result);
		res.put("lbds", lbds);

		return res;

	}


}
