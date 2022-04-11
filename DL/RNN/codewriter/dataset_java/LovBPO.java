package com.dw.odssu.lov;

import com.dareway.framework.dbengine.DE;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.ParaUtil;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

/**
 * odssu系统的lov窗口
 * 
 * @author liuy
 * @version 1.0
 * @date2014-05-06
 */
public final class LovBPO extends BPO{
	/**
	 * 
	 * @author liuy
	 * @date 创建时间 2014-05-16
	 * @since V1.0 修改：叶军 按机构状态正常-注销和机构简称排序
	 */
	public final DataObject lovForOrgnoSelectInViewFunction(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String orgno = para.getString("orgno");
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");
    	de.clearSql();
		de.addSql(" select sleepflag,orgno,displayname,orgname ");
  		de.addSql("   from odssu.orginfor ");
  		de.addSql("  where (orgno like :orgno or orgname like :orgno or orgnamepy like :orgno or displayname like :orgno  ");
  		de.addSql("        or displaynamepy like :orgno or fullname like :orgno or fullnamepy like :orgno) ");
  		de.addSql("        and sleepflag='0' ");
  		de.addSql(" order by sleepflag ,orgno,displayname  ");
		de.setString("orgno", orgno);
		DataStore orgds = de.query();
		vdo.put("orgds", orgds);
		return vdo;
	}

	/**
	 * 人员信息的Lov窗口
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-10-14
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject lovForEmpInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();

		String username = para.getString("username");
		username = ((username == null || "".equals(username)) ? "%" : "%" + username + "%");
		username = username.toUpperCase();

		de.clearSql();
  		de.addSql(" select a.sleepflag,a.empno,a.loginname username,a.empname,b.orgname        ");
  		de.addSql("   from odssu.empinfor a left outer join	odssu.orginfor b			 ");
  		de.addSql("        on a.hrbelong = b.orgno                  ");
  		de.addSql("  where (a.loginname like :username or a.idcardno like :username or a.empnamepy like :username or a.empname like :username or a.rname like :username ");
  		de.addSql("         or a.rnamepy like :username ) ");
  		de.addSql("    order by a.sleepflag,a.empno          ");
		de.setString("username", username);
		de.setQueryScope(100);
		DataStore empds = de.query();

		vdo.put("empds", empds);
		return vdo;
	}
	
	/**
	 *批量新增人员干系角色  - 选择人员 lov窗口
	 * 
	 * @Description:
	 * @author nty
	 * @date 2016-9-21
	 */
	public final DataObject lovForChooseEmp(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();

		String empname = para.getString("empname");
		empname = ((empname == null || "".equals(empname)) ? "%" : "%" + empname + "%");
		empname = empname.toUpperCase();

		de.clearSql();
  		de.addSql(" select a.sleepflag,a.empno,a.loginname username,a.empname,b.orgname        ");
  		de.addSql("   from odssu.empinfor a left outer join	odssu.orginfor b			 ");
  		de.addSql("        on a.hrbelong = b.orgno                  ");
  		de.addSql("  where (a.loginname like :empname or a.idcardno like :empname or a.empnamepy like :empname or a.empname like :empname or a.rname like :empname ");
  		de.addSql("         or a.rnamepy like :empname or empno like :empname ) ");
  		de.addSql("    and a.sleepflag = '0'         ");
  		de.addSql("  order by a.sleepflag,a.empno     ");
		de.setString("empname", empname);
		de.setQueryScope(100);
		DataStore empds = de.query();

		vdo.put("empds", empds);
		return vdo;
	}
	
	/**
	 * 选择机构信息--注销的也能选出来
	 * 
	 * @author liuy
	 * @date 创建时间 2014-05-16
	 * @since V1.0 修改：叶军 按机构状态正常-注销和机构简称排序
	 */
	public final DataObject lovForOrgInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String orgtype, orgtypename;
		String orgno = para.getString("orgno");
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");

		de.clearSql();
  		de.addSql(" select *  	");
  		de.addSql("   from odssu.orginfor ");
  		de.addSql("  where (orgno like :orgno or orgname like :orgno or orgnamepy like :orgno or displayname like :orgno  ");
  		de.addSql("        or displaynamepy like :orgno or fullname like :orgno or fullnamepy like :orgno) ");
  		de.addSql(" order by sleepflag ,orgno,displayname  ");
		de.setString("orgno", orgno);
		de.setQueryScope(100);
		DataStore orgds = de.query();
		for (int i = 0; i < orgds.rowCount(); i++) {
			orgtypename = "";
			orgtype = orgds.getString(i, "orgtype");
			if (!"".equals(orgtype) && orgtype != null) {
				orgtypename = OdssuUtil.getOrgTypeNameByTypeNo(orgtype);
			}
			orgds.put(i, "orgtypename", orgtypename);
		}

		vdo.put("orgds", orgds);
		return vdo;
	}

	/**
	 * 选择机构信息--注销的不能选出来（新增机构时选择上级机构时有用）
	 * 
	 * @author liuy
	 * @date 创建时间 2014-05-16
	 * @since V1.0
	 */
	public final DataObject lovForOrgNoInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String orgno = para.getString("orgno");
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");
    	de.clearSql();
		de.addSql(" select *  	");
  		de.addSql("   from odssu.orginfor ");
  		de.addSql("  where (orgno like :orgno  ");
  		de.addSql("         or orgname like :orgno  ");
  		de.addSql("         or fullname like :orgno ");
  		de.addSql("         or displayname like :orgno ");
  		de.addSql("         or orgnamepy like :orgno ");
  		de.addSql("         or displaynamepy like:orgno or fullnamepy like :orgno) ");
  		de.addSql("    and sleepflag = '0'       ");
  		de.addSql("    order by sleepflag,orgno ");
  		de.setString("orgno", "%"+orgno+"%");
  		de.setQueryScope(200);
		DataStore orgds = de.query();

		vdo.put("orgds", orgds);
		return vdo;
	}

	/**
	 * 选择角色信息--注销的也能选出来
	 * 
	 * @author liuy
	 * @date 创建时间 2014-05-16
	 * @since V1.0
	 */
	public final DataObject lovForRoleInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String roleno = para.getString("roleno");
		roleno = ((roleno == null || "".equals(roleno)) ? "%" : "%" + roleno
				+ "%");
    	de.clearSql();
		de.addSql(" select *  ");
  		de.addSql("   from odssu.roleinfor ");
  		de.addSql("  where  (roleno like :roleno or rolename like :roleno or rolenamepy like :roleno) ");
  		de.addSql("  order by sleepflag,roleno        ");
		de.setString("roleno", roleno);
		de.setQueryScope(100);
		DataStore roleds = de.query();

		vdo.put("roleds", roleds);
		return vdo;
	}

	/**
	 * 选择角色信息--只选出正常的
	 * 
	 * @author liuy
	 * @date 创建时间 2014-05-16
	 * @since V1.0
	 */
	public final DataObject lovForRoleNoInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
  		de.clearSql();
		String roleno = para.getString("roleno");
		roleno = ((roleno == null || "".equals(roleno)) ? "%" : "%" + roleno
				+ "%");
		de.addSql(" select *  ");
  		de.addSql("   from odssu.roleinfor ");
  		de.addSql("  where  (roleno like :roleno or rolename like :roleno or rolenamepy like :roleno)  ");
  		de.addSql("    and sleepflag = '0'");
		de.setString("roleno", roleno);
		de.setQueryScope(200);
		DataStore roleds = de.query();

		vdo.put("roleds", roleds);
		return vdo;
	}

	/**
	 * 选择机构类型
	 * 
	 * @author liuy
	 * @date 创建时间 2014-05-13
	 * @since V1.0
	 */
	public final DataObject lovForOrgType(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
  		de.clearSql();

		String typeno = para.getString("orgtype");
		typeno = ParaUtil.addParaWithPercent(typeno);
		typeno = ((typeno == null || "".equals(typeno)) ? "%" : "%" + typeno
				+ "%");    	
		de.addSql(" select distinct typeno orgtype, typename,comments ");
  		de.addSql("   from odssu.org_type  ");
  		de.addSql("  where typeno like :typeno  or typename like :typeno  ");
  		de.addSql("    order by typeno     ");
		de.setString("typeno", typeno);
		DataStore vds = de.query();

		vdo.put("orgtypeds", vds);
		return vdo;

	}

	/**
	 * 选择机构信息
	 * 
	 * @author liuy
	 * @date 创建时间 2014-05-13
	 * @since V1.0
	 */
	public final DataObject lovForOrgNo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
  		de.clearSql();

		String belongorgno = para.getString("belongorgno");
		belongorgno = ((belongorgno == null || "".equals(belongorgno)) ? "%" : "%"
				+ belongorgno + "%");    	
		de.addSql(" select distinct orgno belongorgno, orgname belongorgname  ");
  		de.addSql("   from odssu.orginfor ");
  		de.addSql("  where (orgno like :belongorgno or orgname like :belongorgno or orgnamepy like :belongorgno)  ");
  		de.addSql("    and sleepflag = '0' ");
		de.setString("belongorgno", belongorgno);
		DataStore vds = de.query();

		vdo.put("vds", vds);
		return vdo;

	}

	/**
	 * 选择角色类型
	 * 
	 * @author liuy
	 * @date 创建时间 2014-05-13
	 * @since V1.0
	 */
	public final DataObject lovForRoleType(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
  		de.clearSql();

		String typeno = para.getString("roletype");
		typeno = ((typeno == null || "".equals(typeno)) ? "%" : "%" + typeno
				+ "%");    	
		de.addSql(" select distinct typeno roletype, typename  ");
  		de.addSql("   from odssu.role_type ");
  		de.addSql("  where typeno like :typeno ");
  		de.addSql("  order by typeno ");
		de.setString("typeno", typeno);
		DataStore vds = de.query();

		vdo.put("vds", vds);
		return vdo;

	}
	/**
	 * 
	 * 方法简介.
	 * @author 叶军     2015-4-15
	 */
	public final DataObject lovForYwztInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();

		String ywztbh = para.getString("ywztbh");
		de.clearSql();
  		de.addSql(" select ywztbh, ywztmc");
  		de.addSql("   from (select a.orgno ywztbh, a.orgname ywztmc, '1' bz");
  		de.addSql("           from odssu.orginfor a");
  		de.addSql("          where a.sleepflag = '0'");
  		de.addSql("         union");
  		de.addSql("         select b.empno ywztbh, b.empname ywztmc, '0' bz");
  		de.addSql("           from odssu.empinfor b");
  		de.addSql("          where b.sleepflag = '0') temp");
  		de.addSql("  where temp.ywztbh like :ywztbh or temp.ywztmc like :ywztmc");
  		de.addSql("  order by bz ");
		de.setString("ywztbh", ywztbh+"%");
		de.setString("ywztmc", ywztbh+"%");
		DataStore ywztvds = de.query();
	    vdo.put("ywztvds", ywztvds);
		return vdo;
	}
	
	public final DataObject lovForKs(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String inorgno = para.getString("inorgno");
		String orgname = para.getString("orgname");
		String orgnameUpper = "";
		if(orgname!=null&&!orgname.equals("")){
			orgnameUpper = orgname.toUpperCase();
		}
		orgname = "%"+orgname+"%";
		orgnameUpper = "%"+orgnameUpper+"%";
		de.clearSql();
  		de.addSql("select distinct a.orgno, a.orgname, a.displayname, a.fullname ");
  		de.addSql("  from odssu.orginfor a, odssu.ir_org_closure b, odssu.org_type c ");
  		de.addSql(" where a.orgno = b.orgno ");
  		de.addSql("   and (a.orgno like :orgname or upper(orgname) like :orgnameupper or upper(orgnamepy) like :orgnameupper or ");
  		de.addSql("       upper(displayname) like :orgnameupper or upper(displaynamepy) like :orgnameupper or ");
  		de.addSql("       upper(fullname) like :orgnameupper or upper(fullnamepy) like :orgnameupper) ");
  		de.addSql("   and a.orgtype = c.typeno ");
  		de.addSql("   and c.yxin = '1' ");
  		de.addSql("   and b.belongorgno in ");
  		de.addSql("       (select m.orgno ");
  		de.addSql("          from odssu.orginfor m, odssu.org_type n, odssu.ir_org_closure o ");
  		de.addSql("         where m.orgno = o.belongorgno ");
  		de.addSql("           and m.orgtype = n.typeno ");
  		de.addSql("           and n.typenature = 'B' ");
  		de.addSql("           and o.orgno = :inorgno) ");
		de.setString("orgname", orgname);
		de.setString("orgnameupper", orgnameUpper);
		de.setString("inorgno", inorgno);
		DataStore ksds = de.query();
		ksds.sort("orgno");
	    vdo.put("ksds", ksds);
		return vdo;
	}
	public final DataObject lovForKs_379900(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String inorgno = para.getString("inorgno");
		String orgname = para.getString("orgname");
		String orgnameUpper = "";
		if(orgname!=null&&!orgname.equals("")){
			orgnameUpper = orgname.toUpperCase();
		}
		orgname = "%"+orgname+"%";
		orgnameUpper = "%"+orgnameUpper+"%";
		de.clearSql();
  		de.addSql(" select a.orgno, a.orgname,a.displayname,a.fullname ");
  		de.addSql("   from odssu.orginfor a, ");
  		de.addSql("        odssu.ir_org_closure b ");
  		de.addSql("  where a.orgno=b.orgno ");
  		de.addSql("    and (a.orgno like :orgname or upper(orgname) like :orgnameupper or upper(orgnamepy) like :orgnameupper or upper(displayname) like :orgnameupper  ");
  		de.addSql("         or upper(displaynamepy) like :orgnameupper or upper(fullname) like :orgnameupper or upper(fullnamepy) like :orgnameupper) ");
  		de.addSql("    and a.orgtype in ( 'HSDOMAIN_RSCKS','HSDOMAIN_SRST','HSDOMAIN_QXRSJ','HSDOMAIN_DSRSJ','HS_DS_EJDW_YWJG','HS_QX_EJDW_YWJG','HS_ST_EJDW_JBJG' ,'HS_DS_EJDW','HS_QX_EJDW','HS_ST_EJDW') ");					//二级单位
		de.addSql("    and b.belongorgno=(select a.orgno ");
  		de.addSql("                         from odssu.orginfor a, ");
  		de.addSql("                              odssu.ir_org_closure b ");
  		de.addSql("  					  where b.belongorgno=a.orgno and ");
  		de.addSql("                              b.orgno= :inorgno and ");
  		de.addSql("                              a.orgtype in ('HSDOMAIN_SRST','HSDOMAIN_QXRSJ','HSDOMAIN_DSRSJ')) ");
		if(!OdssuUtil.isSysAdmin(this.getUser().getUserid())){
  			de.addSql("    and a.orgno in " + OdssuUtil.queryAuthorityInorg(this.getUser().getUserid()));	//lzpmark
		}
		de.setString("orgname", orgname);
		de.setString("orgnameupper", orgnameUpper);
		de.setString("inorgno", inorgno);
		DataStore ksds = de.query();
		ksds.sort("orgno");
	    vdo.put("ksds", ksds);
		return vdo;
	}
	public final DataObject lovForFaceOrg(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String orgno = para.getString("orgno");
		String orgname = para.getString("orgname");
		String orgnameUpper = "";
		if(orgname!=null&&!orgname.equals("")){
			orgnameUpper = orgname.toUpperCase();
		}
		orgname = "%"+orgname+"%";
		orgnameUpper = "%"+orgnameUpper+"%";
		de.clearSql();
  		de.addSql(" select a.orgno, a.orgname,a.displayname,a.fullname ");
  		de.addSql("   from odssu.orginfor a,                           ");
  		de.addSql("        odssu.dept_outer_duty_org b                 ");
  		de.addSql("  where a.orgno = b.faceorgno                       ");
  		de.addSql("    and (a.orgno like :orgname                             ");
  		de.addSql("        or upper(a.orgname) like :orgnameupper                  ");
  		de.addSql("        or upper(a.orgnamepy) like :orgnameupper                ");
  		de.addSql("        or upper(a.displayname) like :orgnameupper              ");
  		de.addSql(" 		  or upper(a.displaynamepy) like :orgnameupper            ");
  		de.addSql("        or upper(a.fullname) like :orgnameupper                 ");
  		de.addSql("        or upper(a.fullnamepy) like :orgnameupper)              ");
  		de.addSql("    and b.deptno = :orgno				                  ");
		de.setString("orgname", orgname);
		de.setString("orgnameupper", orgnameUpper);
		de.setString("orgno", orgno);
		DataStore ksds = de.query();
		
		//将科室本身加入待选机构
		de.clearSql();
  		de.addSql(" select orgname,orgno,displayname,fullname ");
  		de.addSql(" from odssu.orginfor  ");
  		de.addSql(" where orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore orginfords = de.query();
		if(orginfords == null  || orginfords.rowCount() == 0 ){
			this.bizException("找不到机构编号为【"+orgno+"】的机构信息！");
		}
		ksds.put(ksds.rowCount(), "orgno", orginfords.getString(0, "orgno"));
		ksds.put(ksds.rowCount()-1, "orgname", orginfords.getString(0, "orgname"));
		ksds.put(ksds.rowCount()-1, "displayname", orginfords.getString(0, "displayname"));
		ksds.put(ksds.rowCount()-1, "fullname", orginfords.getString(0, "fullname"));
		
		ksds.sort("orgno");
		vdo.put("ksds", ksds);
		return vdo;
	}
	
	/**
	 * 方法简介.查询授予岗位界面的EMP
	 * 
	 * @author Lsy Jun 12, 2015
	 * @param
	 * @return
	 * @throws
	 */
	public final DataObject lovForEmpInGrant(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
  		de.clearSql();
		String empname = para.getString("empname");
		String userid = para.getString("userid");
		empname = ((empname == null || "".equals(empname)) ? "%" : "%" + empname + "%");
		empname = empname.toUpperCase();
		
		//临时代岗的时候，用户只想看到，本科室的人员。
		//所以，在展示代岗人员时，先给用户展示本机构的，然后，再接着展示其他机构的
		
		de.addSql(" select distinct b.sleepflag,b.loginname username,b.empno,b.empname,d.orgname ");
  		de.addSql("   from odssu.empinfor a, ");
  		de.addSql("        odssu.empinfor b, ");
  		de.addSql("        odssu.orginfor d ");
  		de.addSql("  where a.empno = :userid ");
  		de.addSql("    and a.hrbelong = b.hrbelong ");
  		de.addSql("    and b.empno <> a.empno ");
  		de.addSql("    and a.hrbelong = d.orgno ");
  		de.addSql("    and (upper(b.loginname) like :empname or upper(b.empnamepy) like :empname or b.empname like :empname or b.rname like :empname  ");
  		de.addSql("    or upper(b.rnamepy) like :empname ) ");
		de.setString("userid", userid);
		de.setString("empname", empname);
		DataStore empVdsHrBelongOrg = de.query();
		  
		de.clearSql();
  		de.addSql(" select distinct c.sleepflag,c.loginname username,c.empno,c.empname,d.orgname ");
  		de.addSql("   from odssu.ir_emp_org a, ");
  		de.addSql("        odssu.empinfor c, ");
  		de.addSql("        odssu.orginfor d ");
  		de.addSql(" where a.empno = :userid ");
  		de.addSql("   and a.ishrbelong = '0'");
  		de.addSql("   and c.hrbelong = a.orgno ");
  		de.addSql("   and c.empno <> a.empno ");
  		de.addSql("   and a.orgno = d.orgno ");
  		de.addSql("   and (upper(c.loginname) like :empname or upper(c.empnamepy) like :empname or c.empname like :empname or c.rname like :empname  ");
  		de.addSql("    or upper(c.rnamepy) like :empname ) ");
		de.setString("userid", userid);
		de.setString("empname", empname);
		DataStore empVdsNotHrBelongOrg = de.query();
		
		de.clearSql();
  		de.addSql(" select d.sn,a.sleepflag,a.loginname username,a.empno,a.empname,c.orgname ");
  		de.addSql("   from odssu.empinfor a,  ");
  		de.addSql("  	  odssu.ir_emp_org b, ");
  		de.addSql("        odssu.orginfor c,");
  		de.addSql("  	  odssu.org_type d ");
  		de.addSql("  where b.empno =  :userid ");
  		de.addSql("    and a.hrbelong <> b.orgno ");
  		de.addSql("    and a.sleepflag = '0' ");
  		de.addSql("    and a.hrbelong = c.orgno ");
  		de.addSql("    and c.orgtype = d.typeno ");
  		de.addSql("    and (upper(a.loginname) like :empname or upper(a.empnamepy) like :empname or a.empname like :empname or a.rname like :empname  ");
  		de.addSql("    or upper(a.rnamepy) like :empname ) ");
  		de.addSql("  order by d.sn,c.orgno ");
		de.setString("userid", userid);
		de.setString("empname", empname);
		de.setQueryScope(100);
		DataStore empds = de.query();
		
		DataStore resultVds = DataStore.getInstance();
		resultVds.combineDatastore(empVdsHrBelongOrg);
		resultVds.combineDatastore(empVdsNotHrBelongOrg);
		resultVds.combineDatastore(empds);
		
		vdo.put("empds", resultVds);
		return vdo;
	}
}
