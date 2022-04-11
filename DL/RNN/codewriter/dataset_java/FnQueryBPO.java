package com.dw.hsuods.vap.fn.query;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class FnQueryBPO extends BPO{
	/**
	 * @描述：流程任务
	 * @param para
	 * @return
	 * @throws Exception 2015-6-12
	 */
	public DataObject queryGnrwLike(DataObject para) throws Exception {
		String label = para.getString("label", "");
		DataObject result = DataObject.getInstance();
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String fnlabel = "%" + label + "%";
		// 查询机构下允许的fn：查询条件 DBID 
		de.clearSql();
  		de.addSql("select distinct a.functionid , a.functionname ");
  		de.addSql("  from odssu.appfunction a, ");
  		de.addSql("       odssu.db_appfunction b, ");
  		de.addSql("       odssu.fn_roletype c ");
  		de.addSql(" where a.functionid=b.functionid ");
  		de.addSql("   and b.dbid=:dbid ");
  		de.addSql("   and a.functionid = c.functionid ");
  		de.addSql("   and a.pdid is null  ");
		if (label != null && !label.equals("")) {
  			de.addSql("  and (a.functionname like :fnlabel or a.functionid like :fnlabel )");
  			this.de.setString("fnlabel", fnlabel);
		}
		this.de.setString("dbid", dbid);
		DataStore dsfn = this.de.query();
		result.put("fn", dsfn);
		return result;
	}
	/**
	 * 方法简介 ： 通过functionid获取到oiplabel
	 *@author 郑海杰   2016年4月12日
	 */
	public DataObject getFnOipLabel(DataObject para) throws Exception{
		String functionid = para.getString("functionid");
		
		//根据functionID从数据库中获取对应的folderlabel
		de.clearSql();
  		de.addSql(" select b.functionname               ");
  		de.addSql(" from odssu.appfunction b           ");
  		de.addSql(" where b.functionid = :functionid             ");
		this.de.setString("functionid", functionid);
		DataStore vds = this.de.query();
		if(vds == null || vds.rowCount() == 0){
			this.bizException("获取FunctionID为【"+functionid+"】的功能任务信息失败。");
		}
		String functionname = vds.getString(0, "functionname");
		
		DataObject result = DataObject.getInstance();
		
		result.put("oiplabel", functionname);
		return result;
	}
	/**
	 * 方法简介 ：加载责任岗位页面grid数据 
	 *@author 郑海杰   2016年4月12日
	 * @throws AppException 
	 */
	public DataObject loadZrgw(DataObject para) throws AppException {
		// 获取流程任务ID

		de.clearSql();
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String functionid = para.getString("functionid");
		de.clearSql();
  		de.addSql("  select distinct c.cfsno orgno, c.orgname ");
  		de.addSql("          from odssu.fn_roletype      a, ");
  		de.addSql("               odssu.role_type        b, ");
  		de.addSql("               odssu.orginfor         c, ");
  		de.addSql("               odssu.ir_org_role_type d ");
  		de.addSql("         where a.roletypeno = d.roletypeno ");
  		de.addSql("           and a.roletypeno = b.typeno ");
  		de.addSql("           and d.orgtypeno = c.orgtype ");
  		de.addSql("           and a.functionid = :functionid ");
  		de.addSql("           and b.jsgn = :jsgn ");
  		de.addSql("           and c.orgno in (select nn.orgno ");
  		de.addSql("                             from odssu.ir_dbid_org mm, odssu.ir_org_closure nn ");
  		de.addSql("                            where mm.orgno = nn.belongorgno ");
  		de.addSql("                              and mm.dbid = :dbid) ");
  		de.addSql("        union all ");
  		de.addSql("        select distinct p.orgno, p.orgname ");
  		de.addSql("          from odssu.fn_roletype        m, ");
  		de.addSql("               odssu.ir_org_role_type   n, ");
  		de.addSql("               odssu.orginfor           p, ");
  		de.addSql("               odssu.org_business_scope q, ");
  		de.addSql("               odssu.fn_business_scope  r, ");
  		de.addSql("               odssu.role_type          s ");
  		de.addSql("         where m.functionid = :functionid ");
  		de.addSql("           and m.roletypeno = s.typeno ");
  		de.addSql("           and s.jsgn = :jsgn_ ");
  		de.addSql("           and m.roletypeno = n.roletypeno ");
  		de.addSql("           and n.orgtypeno = p.orgtype ");
  		de.addSql("           and p.orgno = q.orgno ");
  		de.addSql("           and m.functionid = r.functionid ");
  		de.addSql("           and q.scopeno = r.scopeid ");
  		de.addSql("           and p.orgno in (select y.orgno ");
  		de.addSql("                             from odssu.ir_dbid_org x, odssu.ir_org_closure y ");
  		de.addSql("                            where x.orgno = y.belongorgno ");
  		de.addSql("                              and x.dbid = :dbid) ");
		this.de.setString("functionid",functionid);
		this.de.setString("jsgn",OdssuContants.JSGN_INNERDUTY);
		this.de.setString("dbid",dbid);
		this.de.setString("jsgn_",OdssuContants.JSGN_OUTERDUTY);
		DataStore resultVds = this.de.query();

		DataObject result = DataObject.getInstance();
		result.put("orgvds", resultVds);
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
		if(dsotherscope!=null &&  dsotherscope.rowCount()!=0){
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
		if(dsOrgScope != null && dsOrgScope.rowCount() != 0){
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
		if(dsOrgScope != null && dsOrgScope.rowCount() != 0){
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
	 * 方法简介 ：Fn适用的岗位类型 
	 *@author 郑海杰   2016年4月12日
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
		if(vds != null && vds.rowCount() != 0) {
			for (int i = 0; i < vds.rowCount(); i++) {
				stringBF.append(OdssuUtil.getRoleTypeNameByTypeNo(vds.getString(i, "roletypeno")) + "\n");
			}
		}
		dsgwlb.put(0, "gwlb",stringBF.toString());

		result.put("lbds", dsgwlb);

		return result;
	}
	/**
	 * 方法简介 ：根据orgno和functionid来查询 有权的岗位及岗位人员信息 
	 *@author 郑海杰   2016年4月14日
	 */
	public DataObject queryDutyAndEmpByOrgno(DataObject para) throws AppException{
		String functionid = para.getString("functionid");
		String orgno = para.getString("orgno");
		String orgtype = getOrgTypeInCFSByOrgno(orgno);
  		de.clearSql();
		DataStore dutyAndEmpVds = DataStore.getInstance();
		//内岗
		if(OdssuUtil.isRealOrg(orgtype)){
			de.clearSql();
  			de.addSql(" select distinct b.orgname,b.rolename,b.orgno,b.roleno ");
  			de.addSql("   from odssu.role_function_manual a, ");
  			de.addSql("        odssu.inner_duty b ");
  			de.addSql("  where a.functionid = :functionid ");
  			de.addSql("    and a.roleno = b.roleno ");
  			de.addSql("    and b.orgno in (select c.orgno from odssu.orginfor c where c.cfsno = :orgno) ");
  			de.addSql("  order by b.orgno,b.roleno ");
			this.de.setString("functionid", functionid);
			this.de.setString("orgno", orgno);
			dutyAndEmpVds = this.de.query();
			if(dutyAndEmpVds != null && dutyAndEmpVds.rowCount()!=0){
				for(int i = 0; i < dutyAndEmpVds.size(); i++){
					String orgno_temp = dutyAndEmpVds.getString(i, "orgno");
					String roleno_temp = dutyAndEmpVds.getString(i, "roleno");
					de.clearSql();
  					de.addSql(" select b.empname,a.formalflag ");
  					de.addSql("   from odssu.emp_inner_duty a,");
  					de.addSql("        odssu.empinfor b ");
  					de.addSql("  where a.orgno = :orgno_temp ");
  					de.addSql("    and a.roleno = :roleno_temp ");
  					de.addSql("    and a.empno = b.empno ");
					this.de.setString("orgno_temp", orgno_temp);
					this.de.setString("roleno_temp", roleno_temp);
					DataStore dutyEmpVds = this.de.query();
					String dutyEmpNameStr = converDutyEmpVdsToDuytEmpStr(dutyEmpVds);
					dutyAndEmpVds.put(i, "empnamestr", dutyEmpNameStr);
				}
			}
		}
		//外岗
		if(OdssuUtil.yxface(orgtype)){
			de.clearSql();
  			de.addSql(" select distinct b.roleno,b.rolename,d.orgno,d.orgname,c.dutyno ");
  			de.addSql("   from odssu.role_function_manual a, ");
  			de.addSql("        odssu.roleinfor b, ");
  			de.addSql("        odssu.outer_duty c, ");
  			de.addSql("        odssu.orginfor d ");
  			de.addSql("  where a.functionid = :functionid ");
  			de.addSql("    and a.roleno = b.roleno ");
  			de.addSql("    and b.roleno = c.roleno ");
  			de.addSql("    and c.faceorgno = :orgno ");
  			de.addSql("    and c.inorgno = d.orgno ");
  			de.addSql("   order by d.orgno,b.roleno ");
			this.de.setString("functionid", functionid);
			this.de.setString("orgno", orgno);
			dutyAndEmpVds =  this.de.query();
			if(dutyAndEmpVds != null && dutyAndEmpVds.rowCount()!=0){
				for(int i = 0; i < dutyAndEmpVds.size(); i++){
					String dutyno = dutyAndEmpVds.getString(i, "dutyno");
					de.clearSql();
  					de.addSql(" select b.empname,a.formalflag ");
  					de.addSql("   from odssu.emp_outer_duty a,");
  					de.addSql("        odssu.empinfor b ");
  					de.addSql("  where a.dutyno = :dutyno ");
  					de.addSql("    and a.empno = b.empno ");
					this.de.setString("dutyno", dutyno);
					DataStore dutyEmpVds = this.de.query();
					String dutyEmpNameStr = converDutyEmpVdsToDuytEmpStr(dutyEmpVds);
					dutyAndEmpVds.put(i, "empnamestr", dutyEmpNameStr);
				}
			}
		}
		DataObject result = DataObject.getInstance();
		result.put("dutyandempds", dutyAndEmpVds);
		return result;
	}
	private String converDutyEmpVdsToDuytEmpStr(DataStore dutyEmpVds) throws AppException{
		if(dutyEmpVds == null || dutyEmpVds.size() == 0){
			return "";
		}
		StringBuffer sqlBF = new StringBuffer();
		for(int i = 0; i < dutyEmpVds.size(); i++){
			String empname = dutyEmpVds.getString(i, "empname");
			String formalflag = dutyEmpVds.getString(i, "formalflag");
			if("0".equals(formalflag)){
				sqlBF.append(empname + "【临时代岗】");
			}else{
				sqlBF.append(empname);
			}
			if(i < dutyEmpVds.size() - 1){
				sqlBF.append(",");
			}
		}
		return sqlBF.toString();
	}
	private String getOrgTypeInCFSByOrgno(String orgno) throws AppException{
  		de.clearSql();
  		de.addSql(" select orgtype ");
  		de.addSql("   from odssu.custome_footstone a ");
  		de.addSql("  where a.orgno = :orgno ");
		this.de.setString("orgno", orgno);
		DataStore orgTypeVds = this.de.query();
		String orgtype = "";
		if(orgTypeVds != null && orgTypeVds.size() > 0){
			orgtype = orgTypeVds.getString(0, "orgtype");
		}
		return orgtype;
	}
}
