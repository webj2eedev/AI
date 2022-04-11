package com.dareway.service;

import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class PhoneBookServiceBPO extends BPO{
	
	/**
	 * 描述：获取下级机构
	 * author: sjn
	 * date: 2017年6月15日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject getChildOrg(DataObject para) throws AppException {
		String orgno = para.getString("orgno");
		if (orgno == null || orgno.equals("")) {
			throw new AppException("传入的机构编号为空，请重新传入！");
		}
		DE de = DE.getInstance();
		//判断机构是否存在
		de.clearSql();
  		de.addSql("select a.displayname orgname ");
  		de.addSql("  from odssu.orginfor a ");
  		de.addSql(" where a.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();
		if (vds == null || vds.rowCount() <= 0) {
			throw new AppException("传入的机构["+orgno+"]不存在，请重新传入！");
		}
		DataStore empds = DataStore.getInstance();
		DataStore orgds = DataStore.getInstance();
		DataStore childorgds = DataStore.getInstance();
		DataStore postds = DataStore.getInstance();
		DataObject vdo = DataObject.getInstance();
		//获取所有下级机构
		de.clearSql();
  		de.addSql("select a.orgno, a.displayname orgname ");
  		de.addSql("  from odssu.orginfor a ");
  		de.addSql(" where a.belongorgno = :orgno ");
  		de.addSql("  order by a.orgsn ");
		de.setString("orgno",orgno);
		orgds = de.query();
		for(int i=0;i<orgds.rowCount();i++){
			String childorgno=orgds.getString(i, "orgno");
			de.clearSql();
  			de.addSql("select 1 ");
  			de.addSql("  from odssu.orginfor a ");
  			de.addSql(" where a.belongorgno = :childorgno ");
			de.setString("childorgno",childorgno);
			childorgds = de.query();
			if(childorgds.rowCount()==0){
				orgds.put(i, "have_child_org", false);
			}else{
				orgds.put(i, "have_child_org", true);
			}
		}
		//获取机构下直属人员
		de.clearSql();
  		de.addSql("select a.empno, a.empname, a.loginname, a.empnamepy, a.mphone,c.orgno,c.displayname orgname ");
  		de.addSql("  from odssu.empinfor a, odssu.ir_emp_org b,odssu.orginfor c ");
  		de.addSql(" where a.empno = b.empno ");
  		de.addSql("   and a.sleepflag  = '0' ");
  		de.addSql("   and a.hrbelong  = c.orgno ");
  		de.addSql("   and b.orgno = :orgno ");
  		de.addSql(" order by b.empsn,a.empnamepy ");
		de.setString("orgno",orgno);
		empds = de.query();
		for(int i=0;i<empds.rowCount();i++){
			String hrbelong = empds.getString(i, "orgno");
			String empno = empds.getString(i, "empno");
			de.clearSql();
  			de.addSql("select r.displayname post ");
  			de.addSql("   from odssu.roleinfor r, odssu.ir_emp_inner_unduty_role e ");
  			de.addSql("  where r.roleno = e.roleno ");
  			de.addSql("    and e.empno = :empno ");
  			de.addSql("    and e.orgno = :hrbelong ");
  			de.addSql("    and r.jsgn = '1' ");
			de.setString("empno",empno);
			de.setString("hrbelong",hrbelong);
			postds = de.query();
			if (postds.rowCount() == 0) {
				empds.put(i, "post", "");
			}else if(postds.rowCount() == 1){
				empds.put(i, "post", postds.getString(0, "post"));
			}else{
				String post=postds.getString(0, "post");
				for(int k=1;k<postds.rowCount();k++){
					post=post+"、"+postds.getString(k, "post");
				}
				empds.put(i, "post", post);
			}	
			
		}
		//获取当前机构的名称
		String orgname = vds.getString(0, "orgname");

		vdo.put("orgname", orgname);
		vdo.put("empds", empds);
		vdo.put("orgds", orgds);

		return vdo;
	}

	/**
	 * 描述：获取人员详细信息
	 * author: sjn
	 * date: 2017年6月15日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getEmpInfor(DataObject para) throws Exception {
		String empno = para.getString("empno");
		if (empno == null || empno.equals("")) {
			throw new AppException("传入的人员编号为空，请重新传入！");
		}
		empno = empno.toUpperCase().replaceAll(" ", "");
		DE de = DE.getInstance();
		//获取人员详细信息
		de.clearSql();
  		de.addSql("select a.empno, a.empname, a.officetel, a.mphone, a.email,a.empnamepy ,b.orgno ,b.displayname orgname ");
  		de.addSql("  from odssu.empinfor a,odssu.orginfor b   ");
  		de.addSql(" where a.empno = :empno  and a.hrbelong=b.orgno ");
		de.setString("empno",empno);
		DataStore empds = de.query();
		if (empds == null || empds.rowCount()<=0) {
			throw new AppException("查询不到该人员信息");
		}
		String orgno = empds.getString(0, "orgno");
		String post = "";
		de.clearSql();
  		de.addSql("select r.displayname post ");
  		de.addSql("   from odssu.roleinfor r, odssu.ir_emp_inner_unduty_role e ");
  		de.addSql("  where r.roleno = e.roleno ");
  		de.addSql("    and e.empno = :empno ");
  		de.addSql("    and e.orgno = :orgno ");
  		de.addSql("    and r.jsgn = '1' ");
		de.setString("empno",empno);
		de.setString("orgno",orgno);
		DataStore postds = de.query();
		if (postds.rowCount() > 0) {
		    if(postds.rowCount() == 1){
		    	post = postds.getString(0, "post");
			}else{
				post=postds.getString(0, "post");
				for(int k=1;k<postds.rowCount();k++){
					post=post+"、"+postds.getString(k, "post");
				}
			}	
		}
			
		String empname = empds.getString(0, "empname");
		String officetel = empds.getString(0, "officetel");
		String mphone = empds.getString(0, "mphone");
		String email = empds.getString(0, "email");
		String empnamepy = empds.getString(0, "empnamepy");
		String orgname = empds.getString(0, "orgname");
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("empno", empno);
		vdo.put("empname", empname);
		vdo.put("officetel", officetel);
		vdo.put("mphone", mphone);
		vdo.put("email", email);
		vdo.put("empnamepy", empnamepy);
		vdo.put("orgno", orgno);
		vdo.put("orgname", orgname);
		vdo.put("post", post);

		return vdo;
	}

	/**
	 * 描述：获取机构下人员
	 * author: sjn
	 * date: 2017年6月15日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getOrgEmp(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		if (orgno == null || orgno.equals("")) {
			throw new AppException("传入的机构编号为空，请重新传入！");
		}
		DE de = DE.getInstance();
		//判断机构是否存在
		de.clearSql();
  		de.addSql("select 1 ");
  		de.addSql("  from odssu.orginfor a ");
  		de.addSql(" where a.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();
		if (vds == null || vds.rowCount() <= 0) {
			throw new AppException("传入的机构["+orgno+"]不存在，请重新传入！");
		}
		//获取机构下人员
		de.clearSql();
  		de.addSql("select a.empno, a.empname, a.loginname, a.empnamepy, a.mphone, c.orgno,c.displayname orgname ");
  		de.addSql("  from odssu.empinfor a, odssu.ir_emp_org b ,odssu.orginfor c ");
  		de.addSql(" where a.empno = b.empno ");
  		de.addSql("   and a.sleepflag  = '0' ");
  		de.addSql("   and a.hrbelong  = c.orgno  ");
  		de.addSql("   and b.orgno = :orgno ");
  		de.addSql(" order by b.empsn,a.empnamepy ");
		de.setString("orgno",orgno);
		DataStore empds = de.query();
		for(int i=0;i<empds.rowCount();i++){
			String hrbelong = empds.getString(i, "orgno");
			String empno = empds.getString(i, "empno");
			de.clearSql();
  			de.addSql("select r.displayname post ");
  			de.addSql("   from odssu.roleinfor r, odssu.ir_emp_inner_unduty_role e ");
  			de.addSql("  where r.roleno = e.roleno ");
  			de.addSql("    and e.empno = :empno ");
  			de.addSql("    and e.orgno = :hrbelong ");
  			de.addSql("    and r.jsgn = '1' ");
			de.setString("empno",empno);
			de.setString("hrbelong",hrbelong);
			DataStore postds = de.query();
			if (postds.rowCount() == 0) {
				empds.put(i, "post", "");
			}else if(postds.rowCount() == 1){
				empds.put(i, "post", postds.getString(0, "post"));
			}else{
				String post=postds.getString(0, "post");
				for(int k=1;k<postds.rowCount();k++){
					post=post+"、"+postds.getString(k, "post");
				}
				empds.put(i, "post", post);
			}	
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", empds);
		
		return vdo;
	}

	/**
	 * 描述：分页方式获取所有人员信息
	 * author: sjn
	 * date: 2017年6月15日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getPageEmp(DataObject para) throws AppException {
		int pageint = para.getInt("pageint");
		if (pageint <= 0) {
			throw new AppException("传入的页码必须大于等于一，请重新传入！");
		}
		int itemint = para.getInt("itemint");
		if (itemint <= 0) {
			throw new AppException("每页显示人数必须大于等于一，请重新传入！");
		}
		int orderint = para.getInt("orderint");
		if (orderint!=1&&orderint!=2) {
			throw new AppException("传入的排序方式应为1或2，请重新传入！");
		}
		int rownum = pageint*itemint;
		int rn = rownum-itemint;
		DE de = DE.getInstance();
  		de.clearSql();
		//分页方式获取所有人员信息
		de.clearSql();
  		de.addSql("select empno,empname,loginname,empnamepy,mphone,orgno,orgname ");
  		de.addSql("  from (select b.empno, b.empname, b.loginname, b.empnamepy,b.mphone,b.orgno,b.orgname ");
  		de.addSql("          from (select a.empno, a.empname, a.loginname,a.empnamepy,a.mphone,t.orgno,t.displayname orgname ");
  		de.addSql("                  from odssu.empinfor a,odssu.orginfor t ");
  		de.addSql("                 where a.sleepflag  = '0' ");
  		de.addSql("                 and a.hrbelong = t.orgno  ");
		if(orderint==1){
  			de.addSql("                 order by a.empnamepy ");
		}
		if(orderint==2){
  			de.addSql("                 order by t.orgno,a.empnamepy ");
		}
  		de.addSql("  ) b ) ");
  		de.setQueryScope(rn,rownum);
		DataStore empds = de.query();
		if (empds != null && empds.rowCount() > 0) {
			for(int i=0;i<empds.rowCount();i++){
				String hrbelong = empds.getString(i, "orgno");
				String empno = empds.getString(i, "empno");
				de.clearSql();
				de.addSql("select r.displayname post ");
				de.addSql("   from odssu.roleinfor r, odssu.ir_emp_inner_unduty_role e ");
				de.addSql("  where r.roleno = e.roleno ");
				de.addSql("    and e.empno = :empno ");
				de.addSql("    and e.orgno = :hrbelong ");
				de.addSql("    and r.jsgn = '1' ");
				de.setString("empno",empno);
				de.setString("hrbelong",hrbelong);
				DataStore postds = de.query();
				if (postds.rowCount() == 0) {
					empds.put(i, "post", "");
				}else if(postds.rowCount() == 1){
					empds.put(i, "post", postds.getString(0, "post"));
				}else{
					String post=postds.getString(0, "post");
					for(int k=1;k<postds.rowCount();k++){
						post=post+"、"+postds.getString(k, "post");
					}
					empds.put(i, "post", post);
				}	
			}
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", empds);

		return vdo;
	}

	/**
	 * 描述：根据传入条件查询人员信息
	 * author: sjn
	 * date: 2017年6月15日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryEmp(DataObject para) throws AppException {
		String condition = para.getString("condition");
		int orderint = para.getInt("orderint");
		if (condition == null || condition.equals("")) {
			throw new AppException("传入的查询条件为空，请重新传入！");
		}
		if (orderint!=1&&orderint!=2) {
			throw new AppException("传入的排序方式应为1或2，请重新传入！");
		}
		//转成大写去空格、加模糊查询条件
		condition = condition.toUpperCase().replaceAll(" ", "");
		condition =  "%" + condition + "%";
		DE de = DE.getInstance();
		// 根据传入条件模糊查询人员信息
		de.clearSql();
  		de.addSql("select a.empno, a.empname, a.loginname,a.empnamepy,a.mphone,b.orgno,b.displayname orgname ");
  		de.addSql("  from odssu.empinfor a,odssu.orginfor b  ");
  		de.addSql(" where (upper(a.empno) like :condition or upper(a.empname) like :condition or upper(a.empnamepy) like :condition or ");
  		de.addSql("       a.officetel like :condition or a.mphone like :condition or upper(a.email) like :condition or ");
  		de.addSql("       upper(a.loginname) like :condition or upper(a.rname) like :condition or upper(a.rnamepy) like :condition) ");
  		de.addSql("   and a.sleepflag  = '0' ");
  		de.addSql("   and a.hrbelong  = b.orgno ");
		if (orderint == 1) {
  			de.addSql("  order by a.empnamepy ");
		}
		if (orderint == 2) {
  			de.addSql("  order by b.orgno,a.empnamepy ");
		}
		de.setString("condition", condition);

		DataStore empds = de.query();
		for(int i=0;i<empds.rowCount();i++){
			String hrbelong = empds.getString(i, "orgno");
			String empno = empds.getString(i, "empno");
			de.clearSql();
  			de.addSql("select r.displayname post ");
  			de.addSql("   from odssu.roleinfor r, odssu.ir_emp_inner_unduty_role e ");
  			de.addSql("  where r.roleno = e.roleno ");
  			de.addSql("    and e.empno = :empno ");
  			de.addSql("    and e.orgno = :hrbelong ");
  			de.addSql("    and r.jsgn = '1' ");
			de.setString("empno",empno);
			de.setString("hrbelong",hrbelong);
			DataStore postds = de.query();
			if (postds.rowCount() == 0) {
				empds.put(i, "post", "");
			}else if(postds.rowCount() == 1){
				empds.put(i, "post", postds.getString(0, "post"));
			}else{
				String post=postds.getString(0, "post");
				for(int k=1;k<postds.rowCount();k++){
					post=post+"、"+postds.getString(k, "post");
				}
				empds.put(i, "post", post);
			}	
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", empds);
		
		return vdo;
	}
}