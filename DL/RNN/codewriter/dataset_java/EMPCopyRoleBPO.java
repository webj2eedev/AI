package com.dw.emp.empcopyrole;

import java.util.HashMap;
import java.util.Map;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class EMPCopyRoleBPO extends BPO{
	/**
	 * 查找目标操作员
	 * @param para
	 * @return
	 * @throws Exception
	 * @author zy
	 */
	public DataObject queryTargetEmp(DataObject para) throws Exception{
		DataObject vdo = DataObject.getInstance();
		String searchkey = para.getString("searchkey","");
		String empno = para.getString("empno");
		de.clearSql();
		de.addSql("select a.hrbelong from  odssu.empinfor a where a.empno =:empno ");
		de.setString("empno", empno);
		DataStore hebelongds = de.query();
		String hrbelong = hebelongds.getString(0,"hrbelong");
		
		//当前dbid
		String dbid = GlobalNames. DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		//当前的dbid对应的根节点
		de.clearSql();
		de.addSql("select b.orgno from odssu.ir_dbid_org a,odssu.orginfor b where dbid = :dbid and a.orgno = b.orgno  and b.sleepflag='0' ");
		de.setString("dbid", dbid);
		DataStore orgds = de.query();
		if(orgds == null || orgds.rowCount() == 0){
			return DataObject.getInstance();
		}
		String rootOrg = "";
		if(orgds!= null && orgds.rowCount()!= 0){
			rootOrg = orgds.getString(0, "orgno");
		}
		
		de.clearSql();
		de.addSql("select b.orgno                                             ");
		de.addSql("  from odssu.orginfor b                                    ");
		de.addSql(" where b.orgno in (select a.belongorgno                    ");
		de.addSql("                     from odssu.ir_org_closure a           ");
		de.addSql("                    where a.orgno = :orgno)                ");
		de.addSql("   and b.belongorgno = :rootorg                            ");
		de.setString("orgno", hrbelong);
		de.setString("rootorg", rootOrg);
		DataStore targetEMPds = de.query();

		if(targetEMPds==null || targetEMPds.rowCount()==0) {
			return DataObject.getInstance();
		}
		String belongorgno = targetEMPds.getString(0, "orgno");
		de.clearSql();
	    de.addSql(" select distinct a.empno, a.empname, a.loginname, d.orgname                                    ");
	    de.addSql("   from odssu.empinfor a ,                                                             ");
	    de.addSql("       odssu.ir_emp_org b,                                                             ");
	    de.addSql("       odssu.ir_org_closure c,                                                         ");
	    de.addSql("       odssu.orginfor d                                                                ");
	    de.addSql("  where a.empno = b.empno                                                              ");
	    de.addSql("    and c.orgno = b.orgno                                                              ");
	    de.addSql("    and a.sleepflag = '0'                                                              ");
		de.addSql("    and c.belongorgno = :belongorgno                                                   ");
		de.addSql("    and a.hrbelong = d.orgno                                                           ");
		de.addSql("    and a.empno != :empno                                                              ");
		de.setString("belongorgno", belongorgno);
		de.setString("empno", empno);
		if(searchkey!=null && (!searchkey.equals(""))) {
			de.addSql("    and (a.loginname like :searchkey or a.empname like :searchkey)                  ");
			de.setString("searchkey", "%"+searchkey.toUpperCase()+"%");
		}
		
		DataStore EMPds = de.query();

		vdo.put("empds",EMPds);
		return vdo;
	}
	/**
	 * 获取操作员勾选的角色
	 * @param para
	 * @return
	 * @throws AppException
	 * @author zy
	 */
	public DataObject querySelectedRoleAllApp(DataObject para) throws AppException {
		DataObject vdo = DataObject.getInstance();
		String piid = para.getString("piid");
		String empno = para.getString("empno");
		de.clearSql();
		de.addSql(" select a.roleno, b.rolename, a.orgno,c.orgname, '0' ishave, d.appname                              ");
		de.addSql("   from odssuws.emp4role_detal a,                                                                   ");
		de.addSql("        odssu.roleinfor        b,                                                                   ");
		de.addSql("        odssu.orginfor         c,                                                                   ");
		de.addSql("        odssu.appinfo          d                                                                    ");
		de.addSql("  where a.roleno = b.roleno                                                                         ");
		de.addSql("    and a.orgno = c.orgno                                                                           ");
		de.addSql("    and b.appid = d.appid                                                                           ");
		de.addSql("    and a.piid = :piid                                                                              ");
		de.addSql("    and a.empno = :empno                                                                            ");

		de.setString("piid", piid);
		de.setString("empno", empno);

		DataStore ds = de.query();
		vdo.put("selectedrole",ds);
		return vdo;
	}

	
	/**
	 * 查找不同应用系统下的角色
	 * @param para
	 * @return
	 * @throws AppException
	 * @author zy
	 */
	public DataObject queryEmpRoleByApp(DataObject para) throws AppException {
		String empno = para.getString("empno");
		String targetEmpNo = para.getString("targetempno");
		String targetempname = para.getString("targetempname");
		String appid = para.getString("appid");
		String roleno = para.getString("queryrole","");
		
		//查询当前操作员与目标操作员不一样的角色
		de.clearSql();
		de.addSql("   select  a.roleno,a.rolename ,b.orgno,c.orgname, '0' ishave                                ");
		de.addSql("    from odssu.roleinfor a, odssu.ir_emp_org_all_role b, odssu.orginfor c                    ");
		de.addSql("   where a.roleno = b.roleno                                                                 ");
		de.addSql("     and b.empno = :targetempno                                                              ");
		de.addSql("     and b.orgno = c.orgno                                                                   "); 
		de.addSql("     and  not  exists(                                                                       ");
		de.addSql("	select 1 from odssu.ir_emp_org_all_role e                                                   ");
		de.addSql("		where e.empno = :empno                                                                  ");
		de.addSql("		and b.empno=:targetempno                                                                ");
		de.addSql("		and e.orgno = b.orgno                                                                   ");
		de.addSql("		and e.roleno = b.roleno )                                                               ");
		de.addSql(" and a.appid = :appid                                                                    ");
		de.setString("appid", appid);
		if(!roleno.equals("")) {
			de.addSql(" and b.roleno in(select g.roleno from odssu.roleinfor g                                   ");
			de.addSql("                     where g.roleno  like :roleno or g.rolename like :roleno        )     ");
			de.setString("roleno", "%"+roleno+"%");		
		}
		de.setString("targetempno", targetEmpNo);
		de.setString("empno", empno);
		
		DataStore empRoleds = de.query();
		
		//查询当前操作员与目标操作员同样的角色
		de.clearSql();
		de.addSql("   select   a.roleno,a.rolename ,b.orgno,c.orgname, '1' ishave                               ");
		de.addSql("    from odssu.roleinfor a, odssu.ir_emp_org_all_role b, odssu.orginfor c                    ");
		de.addSql("   where a.roleno = b.roleno                                                                 ");
		de.addSql("     and b.empno = :targetempno                                                              ");
		de.addSql("     and b.orgno = c.orgno                                                                   "); 
		de.addSql("     and  exists(                                                                            ");
		de.addSql("	select 1  from odssu.ir_emp_org_all_role e                                                  ");
		de.addSql("		where e.empno = :empno                                                                  ");
		de.addSql("		and b.empno=:targetempno                                                                ");
		de.addSql("		and e.orgno = b.orgno                                                                   ");
		de.addSql("		and e.roleno = b.roleno )                                                               ");
		de.addSql(" and a.appid = :appid                                                                    ");
		de.setString("appid", appid);
		if(!roleno.equals("")) {
			de.addSql(" and b.roleno in(select g.roleno from odssu.roleinfor g                                   ");
			de.addSql("                     where g.roleno  like :roleno or g.rolename like :roleno        )     ");
			de.setString("roleno", "%"+roleno+"%");
		}
		de.setString("targetempno", targetEmpNo);
		de.setString("empno", empno);
		DataStore vds = de.query();
		
		//获取操作员勾选的角色
		DataObject chooseRole = querySelectedRole(para);
		DataStore chooseRoleds = DataStore.getInstance(); 
		chooseRoleds = chooseRole.getDataStore("selectedrole");
		chooseRoleds.combineDatastore(vds);
	
		//合并所有角色
		empRoleds.combineDatastore(vds);
// 		empRoleds.remainDistinct();
 		
 		//勾选
		getSelectRowRole(empRoleds,chooseRoleds);
 		DataObject vdo =DataObject.getInstance();
		empRoleds.multiSort("_row_selected:desc,rolename:asc");
		vdo.put("haverolenum",vds.rowCount());
		vdo.put("emproleds",empRoleds);
		vdo.put("empnonow",empno);
		vdo.put("targetempname",targetempname);
		vdo.put("targetempno",targetEmpNo);
		vdo.put("appid",appid);
		vdo.put("selectedrole",chooseRoleds);
		return vdo;
	}
	/**
	 * 获取操作员勾选的角色
	 * @param para
	 * @return
	 * @throws AppException
	 * @author zy
	 */
	public DataObject querySelectedRole(DataObject para) throws AppException {
		DataObject vdo = DataObject.getInstance();
		String piid = para.getString("piid");
		String empno = para.getString("empno");
		String appid = para.getString("appid");
		de.clearSql();
		de.addSql(" select a.roleno, b.rolename, a.orgno,c.orgname, '0' ishave                                         ");
		de.addSql("   from odssuws.emp4role_detal a,                                                                   ");
		de.addSql("        odssu.roleinfor        b,                                                                   ");
		de.addSql("        odssu.orginfor         c                                                                    ");
		de.addSql("  where a.roleno = b.roleno                                                                         ");
		de.addSql("    and a.orgno = c.orgno                                                                           ");
		de.addSql("    and a.piid = :piid                                                                              ");
		de.addSql("    and a.empno = :empno                                                                            ");
		de.addSql("    and b.appid = :appid                                                                            ");
		de.setString("appid", appid);
		de.setString("piid", piid);
		de.setString("empno", empno);

		DataStore ds = de.query();
		vdo.put("selectedrole",ds);
		return vdo;
	}
	
	/**
	 * 将全部中已选的数据勾选
	 * @param roleds
	 * @param rolechoosedds
	 * @throws AppException
	 * @author zy
	 */
	public void getSelectRowRole(DataStore roleds,DataStore rolechoosedds) throws AppException{
		Map<String, String> map=new HashMap<String, String>();
		
		for(int i=0 ;i<rolechoosedds.size();++i) {
			String rolenochoose = rolechoosedds.getString(i,"roleno");
			String orgnochoose = rolechoosedds.getString(i,"orgno");
			String key = rolenochoose + orgnochoose;
			map.put(key, key);
		}
		
		for(int i =0 ;i<roleds.size();i++){
			DataObject currentrole = roleds.get(i);
			String leftroleno = currentrole.getString("roleno");
			String leftorgno = currentrole.getString("orgno");
			String key = leftroleno+leftorgno;
			if(map.containsKey(key)) {
				currentrole.put("_row_selected", true);
			}else {
				currentrole.put("_row_selected", false);
			}
		}
	}
	/**
	 * 添加全选的role
	 * @param para
	 * @throws AppException
	 * @throws BusinessException
	 * @author zy
	 */
	public void addAllEmpOrgWithRole(DataObject para) throws AppException, BusinessException {
		String empno = para.getString("empno");
		String piid = para.getString("piid");
		String gridname = para.getString("gridname");
		DataStore allRole = para.getDataStore(gridname);
		
		DataObject role = DataObject.getInstance();
		for(int i=0;i<allRole.rowCount();++i) {
			String orgno = allRole.getString(i, "orgno");
			String roleno = allRole.getString(i, "roleno");
			String ishave = allRole.getString(i, "ishave");
			if("1".equals(ishave)) {
				continue;
			}
			
			role.put("empno",empno);
			role.put("piid",piid);
			role.put("orgno",orgno);
			role.put("roleno",roleno);
			addEmpOrgRole(role);
		}
	}
	
	/**
	 * 添加一个角色信息
	 * @param para
	 * @throws AppException
	 * @throws BusinessException
	 * @author zy
	 */
	public void addEmpOrgRole(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		DataStore ds = isSelected(piid,orgno,roleno);
		
		//判断角色是否在工单表中存在。
		if(ds.isEmpty()){
			de.clearSql();
			de.addSql("insert  into  odssuws.emp4role_detal(piid,orgno,roleno,opflag,empno)           ");
			de.addSql("                               values(:piid, :orgno , :roleno , '(+)',:empno)  ");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			de.setString("empno", empno);
			de.update();
		}
	}
	
	/**
	 * 查看工单表中的标记
	 * @param piid
	 * @param orgno
	 * @param roleno
	 * @return
	 * @throws AppException
	 * @author zy
	 */
	public DataStore isSelected(String piid,String orgno,String roleno) throws AppException{
		DE de =DE.getInstance();
		de.clearSql();
		de.addSql("select 1 from odssuws.emp4role_detal               ");		
		de.addSql("  where piid = :piid                               ");
		de.addSql("   and orgno = :orgno                              ");
		de.addSql("   and  roleno = :roleno                           ");
		de.setString("roleno",roleno);
		de.setString("piid",piid);
		de.setString("orgno",orgno);
		DataStore ds = de.query();
		return ds;
		
	}
	/**
	 * 判断角色是否为操作员原来有的角色
	 * @param empno
	 * @param orgno
	 * @param roleno
	 * @return
	 * @throws AppException
	 * @author zy
	 */
	public boolean isRoleExists(String empno,String orgno,String roleno) throws AppException {
		
		de.clearSql();
		de.addSql("select   1                                                 "); 
		de.addSql("from     odssu.ir_emp_org_all_role  a  ,                   "); 
		de.addSql("         odssu.roleinfor   c                               "); 
		de.addSql("where    a.empno = :empno                                  "); 
		de.addSql("         and   a.orgno  = :orgno                           ");
		de.addSql("         and   a.roleno  =  c.roleno 					  "); 
		de.addSql("         and   c.sleepflag = '0'                           ");
		de.addSql("         and   c.roleno = :roleno                          ");
		de.addSql("         and   a.isFormal = '1'                            ");
		de.setString("empno",empno);
		de.setString("orgno", orgno);
		de.setString("roleno", roleno);
		DataStore vds = de.query();
		
		if(vds == null || vds.rowCount() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * 删除一个新添加的角色
	 * @param para
	 * @throws AppException
	 * @throws BusinessException
	 * @author zy
	 */
	public void delEmpOrgRole(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
		
		DE de = DE.getInstance();
		
		de.clearSql();
		de.addSql("delete  from     odssuws.emp4role_detal                          ");
		de.addSql("	where piid = :piid                                              ");
		de.addSql("   and orgno = :orgno                                            ");
		de.addSql("   and roleno = :roleno                                          ");
		de.addSql("   and empno = :empno                                            ");
		de.setString("piid", piid);
		de.setString("orgno", orgno);
		de.setString("roleno", roleno);
		de.setString("empno", empno);
		de.update();
		de.commit();
		
	}
	/**
	 * 取消全选的角色
	 * @author zy
	 * @param para
	 * @throws AppException
	 * @throws BusinessException
	 */
	public void delAllEmpOrgWithRole(DataObject para) throws AppException, BusinessException {
		String piid = para.getString("piid");
		String empno = para.getString("empno");
		String gridname = para.getString("gridname");
		DataStore roleds = para.getDataStore(gridname);
		
		for(int i=0;i<roleds.rowCount();++i) {
			DataObject vdo = DataObject.getInstance();
			String orgno = roleds.getString(i, "orgno");
			String roleno = roleds.getString(i, "roleno");
			String ishave = roleds.getString(i, "ishave");
			if("1".equals(ishave)) {
				continue;
			}
			vdo.put("piid", piid);
			vdo.put("empno", empno);
			vdo.put("orgno", orgno);
			vdo.put("roleno", roleno);
			delEmpOrgRole(vdo);
		}
	}
}
