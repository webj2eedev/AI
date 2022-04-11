package com.dw.vap.org;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;
import com.dareway.framework.dbengine.DE;

public class OrgVAPTreeBPO extends BPO{

	/**
	 * 机构的上级机构信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-26
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject querySupOrgInfor(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
  		de.clearSql();
    	de.addSql(" select o1.orgno,o1.orgname,o1.displayname,o1.fullname, ");
  		de.addSql("        o1.sleepflag,o1.orgtype, ");
  		de.addSql("        o1.belongorgno , o1.orgnamepy , o1.fullnamepy , o1.displaynamepy , o1.createdate ");
  		de.addSql(" from   odssu.orginfor o left outer join odssu.orginfor o1 ");
  		de.addSql("        on o.belongorgno = o1.orgno ");
  		de.addSql(" where  o.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgds", vds);
		return vdo;
	}

	/**
	 * 查询机构的基本信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-15
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageOrgBaseInforJsp(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String orgtype, typename, sleepflag = "";
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
  		de.clearSql();
    	de.addSql(" select o.orgno,o.orgname,o.displayname,o.fullname, ");
  		de.addSql("        o.sleepflag,o.orgtype, ");
  		de.addSql("        o.belongorgno,o1.orgname belongorgname ");
  		de.addSql(" from   odssu.orginfor o left outer join odssu.orginfor o1 ");
  		de.addSql("        on o.belongorgno = o1.orgno ");
  		de.addSql(" where  o.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		if (vds.rowCount() > 0) {
			orgtype = vds.getString(0, "orgtype");

			typename = OdssuUtil.getOrgTypeNameByTypeNo(orgtype);
			vds.put(0, "typename", typename);

			sleepflag = vds.getString(0, "sleepflag");
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgds", vds);
		vdo.put("sleepflag", sleepflag);
		return vdo;
	}

	/**
	 * 查询机构的直接下属机构
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-15
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageOrgSubOrgJsp(DataObject para) throws Exception {
		String sleepflag;
		String orgno = para.getString("orgno");
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
    	de.clearSql();
  		de.addSql(" select * ");
  		de.addSql(" from  odssu.orginfor ");
  		de.addSql(" where orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore orgds = de.query();
		if (orgds.rowCount() == 0) {
			this.bizException("没找到此机构的信息请检查！");
		}
		sleepflag = orgds.getString(0, "sleepflag");

		de.clearSql();
  		de.addSql(" select o.orgno,o.orgname,o.displayname,o.fullname,  ");
  		de.addSql("        o.sleepflag,o.orgtype,t.typename  ");
  		de.addSql(" from   odssu.orginfor o left outer join odssu.org_type t  ");
  		de.addSql("        on o.orgtype = t.typeno ");
  		de.addSql(" where o.belongorgno = :orgno   ");
  		de.addSql(" order by o.orgtype desc,o.orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		String displayname = orgds.getString(0, "displayname");

		DataObject vdo = DataObject.getInstance();

		vdo.put("sleepflag", sleepflag);
		vdo.put("displayname", displayname);
		vdo.put("dw", vds);

		return vdo;
	}


	/**
	 * 查询机构定义的角色
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-15
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageOrgDefRole(DataObject para) throws Exception {
		String orgno = para.getString("orgno");

		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
    	de.clearSql();
  		de.addSql(" select * ");
  		de.addSql(" from  odssu.orginfor ");
  		de.addSql(" where orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore orgds = de.query();
		if (orgds.rowCount() == 0) {
			this.bizException("没找到此机构的信息请检查！");
		}
		String sleepflag = orgds.getString(0, "sleepflag");
		String orgtype = orgds.getString(0, "orgtype");

		de.clearSql();
  		de.addSql(" select t.typenature from odssu.org_type t where t.typeno = :orgtype ");
		de.setString("orgtype", orgtype);
		DataStore vdsnature = de.query();

		if (vdsnature.rowCount() == 0) {
			this.bizException("没有找到机构类型【" + orgtype + "】的机构性质。");
		}
		String orgnature = vdsnature.getString(0, "typenature");

		String isdanwei = "0";
		if (OdssuContants.danwei.equals(orgnature)) {
			isdanwei = "1";
		}
    	de.clearSql();
  		de.addSql(" select r.roleno,r.rolename,r.displayname, ");
  		de.addSql("        r.sleepflag,r.rolenature,r.roletype,t.typename ");
  		de.addSql(" from   odssu.roleinfor r left outer join odssu.role_type t ");
  		de.addSql("        on t.typeno = r.roletype  ");
  		de.addSql(" where  r.deforgno = :orgno ");
  		de.addSql(" order by r.roleno ");
		de.setString("orgno", orgno);

		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("roleds", vds);
		vdo.put("sleepflag", sleepflag);
		vdo.put("isdanwei", isdanwei);
		return vdo;
	}

	/**
	 * 查询机构直属的人员
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-15
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageOrgDirEmpJsp(DataObject para) throws Exception {
		String orgno = para.getString("orgno");

		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select e.empno,e.empname,e.rname,e.gender, ");
  		de.addSql("        e.sleepflag,e.officetel, ");
  		de.addSql("        e.mphone,e.email,e.loginname ");
  		de.addSql(" from   odssu.empinfor e, ");
  		de.addSql("        odssu.ir_grant g ");
  		de.addSql(" where  e.empno = g.cidenttno and g.pidenttno = :orgno and g.cidentttype = '0' ");
  		de.addSql(" order by e.empno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("dw", vds);
		return vdo;
	}

	/**
	 * 查询该机构下的所有干系人的信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageOrgEmpOuterRoleJsp(DataObject para) throws Exception {

		String orgno = para.getString("orgno");

		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空!");
		}
		DE de = DE.getInstance();
    	de.clearSql();
    	
    	de.addSql(" select a.empno , b.empname , a.roleno ,c.rolename ");
  		de.addSql("  from  odssu.ir_emp_outer_unduty_role a , odssu.empinfor b , odssu.roleinfor c ");
  		de.addSql(" where  a.orgno = :orgno and a.empno = b.empno and a.roleno = c.roleno   ");
  		de.addSql(" order by a.empno ");
		de.setString("orgno", orgno);

		DataStore empds = de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("empds", empds);

		return vdo;
	}

	/**
	 * 回调时，获取新增下级机构的机构编号
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-25
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getxzxsEmpNo(DataObject para) throws Exception {
		String piid = para.getString("piid");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("流程实例编号不能为空！");
		}
		DE de = DE.getInstance();
    	de.clearSql();
    	de.addSql(" select  b.orgno ");
  		de.addSql("  from  odssuws.jgjbxxxzwzb a , odssu.orginfor  b ");
  		de.addSql("  where  a.piid = :piid and a.orgname = b.orgname  ");
		de.setString("piid", piid);

		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("流程实例所对应的新增的下级机构的编号不存在！");
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgno", vds.getString(0, "orgno"));
		return vdo;
	}


	/**
	 * 获取业务受理机构编号
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-28
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getYwbljgByOrgno(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String orgno = para.getString("orgno");

		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}

		String pBiz = OdssuUtil.getYwbljgByOrgno(orgno);

		vdo.put("pBiz", pBiz);

		return vdo;

	}

	/**
	 * 根据角色定义机构获取角色新增流程的业务隶属机构
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-9
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getPbizDefOrgno4RoleAdd(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		boolean flag = true;

		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
    	de.clearSql();
  		de.addSql(" select t.typenature ");
  		de.addSql(" from   odssu.orginfor o, ");
  		de.addSql("        odssu.org_type t ");
  		de.addSql(" where  o.orgtype = t.typeno and o.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("没有找到机构【" + orgno + "】的机构性质。");
		}
		String typenature = vds.getString(0, "typenature");
		String pBiz = "";
		if (OdssuContants.danwei.equals(typenature) == false) {
			flag = false;
		} else {
			pBiz = OdssuUtil.getYwbljgByOrgno(orgno);
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("flag", flag);
		vdo.put("pbiz", pBiz);
		return vdo;
	}

	/**
	 * 通过操作员编号获取业务经办机构ID
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-29
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getYwbljgByEmpno(DataObject para) throws Exception {

		String empno = para.getString("empno");

		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("人员编号为空！");
		}
		DE de = DE.getInstance();
    	de.clearSql();
    	de.addSql("  select empno , orgno  ");
  		de.addSql("   from   odssu.ir_emp_org ");
  		de.addSql("  where  empno = :empno ");
		de.setString("empno", empno);

		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("人员所属的机构编号为空！");
		}

		String orgno = vds.getString(0, "orgno");
		para.put("orgno", orgno);

		DataObject vdo = getYwbljgByOrgno(para);

		return vdo;
	}

	/**
	 * debug ---- 机构基本信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-15
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageDebugOrgBaseInforJsp(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
  		de.clearSql();
    	de.addSql(" select * from odssu.orginfor where orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("orgInfords", vds);
		return vdo;
	}

	
	/**
	 * debug -- 机构直属人员
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-1
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageDebugOrgDirEmpJsp(DataObject para) throws Exception {
		String orgno = para.getString("orgno");

		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select e.empname empinfor_empname , g.empno ir_emp_org_empno, nvl(g.ishrbelong,0) ir_emp_org_ishrbelong");
  		de.addSql(" from   odssu.empinfor e right outer join odssu.ir_emp_org g ");
  		de.addSql("        on e.empno = g.empno  ");
  		de.addSql(" where  g.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", vds);
		return vdo;
	}

	/**
	 * debug -- 机构下的所有机构、人员、角色信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-2
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageDebugOrgGrantSubInfor(DataObject para) throws Exception {
		String orgno = para.getString("orgno");

		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select  g.cidenttno ir_grant_cidenttno , g.cidentttype ir_grant_cidentttype  , g.post ir_grant_post , g.sn ir_grant_sn , g.ishrbelong ir_grant_ishrbelong ");
  		de.addSql(" from    odssu.ir_grant g ");
  		de.addSql(" where g.pidenttno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("subds", vds);
		return vdo;
	}

	/**
	 * debug----机构直属上级相关信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageDebugOrgGrantSupInfor(DataObject para) throws Exception {
		String orgno = para.getString("orgno");

		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select  g.pidenttno ir_grant_pidenttno , g.pidentttype ir_grant_pidentttype ");
  		de.addSql(" from    odssu.ir_grant g ");
  		de.addSql(" where g.cidenttno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("supds", vds);
		return vdo;
	}

	/**
	 * 查询机构新增是否记账完成
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-9-1
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject checkOrgAddIfSaveSuccess(DataObject para) throws Exception {
		String piid = para.getString("piid");
		DE de = DE.getInstance();
    	de.clearSql();
  		de.addSql(" select jgxzwzbjzbz,orgno,orgname from odssuws.jgjbxxxzwzb where piid = :piid  ");
		de.setString("piid", piid);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("没有找到piid的【" + piid + "】的流程的信息，请检查!");
		}
		String jzbz = vds.getString(0, "jgxzwzbjzbz");
		String orgno = vds.getString(0, "orgno");
		String orgname = vds.getString(0, "orgname");

		if ("1".equals(jzbz)) {

			de.clearSql();
  			de.addSql(" select orgno from odssu.orginfor where orgname = :orgname ");
			de.setString("orgname", orgname);
			DataStore vds1 = de.query();

			if (vds1.rowCount() == 0) {
				this.bizException("没有找到机构名称为【" + orgname + "】的机构的信息。");
			}
			orgno = vds1.getString(0, "orgno");
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("jzbz", jzbz);
		vdo.put("orgno", orgno);
		vdo.put("orgname", orgname);
		return vdo;
	}
}
