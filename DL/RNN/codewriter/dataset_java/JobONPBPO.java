package com.dw.job;

import java.util.Random;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class JobONPBPO extends BPO{
	public DataObject queryJobInfor(final DataObject para) throws Exception {
		String jobmc = para.getString("jobmc");
		if(jobmc == null||"".equals(jobmc)){
			jobmc = "%";
		}else {
			jobmc = "%"+jobmc.toUpperCase()+"%";
		}
		de.clearSql();
		DataObject vdo = DataObject.getInstance();
		DataStore vds;

		de.clearSql();
  		de.addSql("  select a.jobno,a.jobname,a.jobdetail ");
  		de.addSql("    from odssu.jobinfor a              ");
  		de.addSql("   where upper(a.jobno) like :jobmc    ");
  		de.addSql("      or upper(a.jobname) like :jobmc  ");
  		de.addSql("   order by a.joborder                    ");
		de.setString("jobmc", jobmc);
		vds = de.query();
		vdo.put("jobds", vds);
		return vdo;

	}
	public DataObject queryJobEMP(final DataObject para) throws Exception {
		String jobno = para.getString("jobno");
		String empkey = para.getString("empkey");
		
		if(empkey == null || "".equals(empkey)){
			empkey = "%";
		}else {
			empkey = "%"+empkey.toUpperCase()+"%";
		}
		de.clearSql();
		DataObject vdo = DataObject.getInstance();
		DataStore vds;
		
		de.clearSql();
		de.addSql(" select b.empno,b.empname,b.loginname ,c.orgname  ");
		de.addSql("   from odssu.emp_job a ,odssu.empinfor b, odssu.orginfor c ");
		de.addSql("  where a.jobno = :jobno  ");
		de.addSql("    and a.empno = b.empno    ");
		de.addSql("    and c.orgno = a.orgno ");
		de.addSql("    and (b.empno like :empkey  or b.empname like :empkey or b.loginname like :empkey) ");
		de.setString("jobno", jobno);
		de.setString("empkey", empkey);
		vds = de.query();
		
		vdo.put("empds", vds);
		return vdo;
		
	}
	public DataObject delJob(DataObject para) throws Exception {
		String jobno = para.getString("jobno");
	
		DataObject vdo = DataObject.getInstance();
		DataStore vds;
		
		de.clearSql();
		de.addSql(" select 1 from odssu.emp_job a where a.jobno = :jobno  ");
		de.setString("jobno", jobno);
		vds = de.query();
		if(vds != null&&vds.rowCount()>0) {
			vdo.put("result", "职务下存在操作员，不允许删除！");
			return vdo;
		}
		de.clearSql();
		de.addSql(" delete from odssu.jobinfor where jobno = :jobno ");
		de.setString("jobno", jobno);
		de.update();
		
		vdo.put("result", "删除成功！");
		return vdo;
		
	}
	public DataObject setJobOrder(DataObject para) throws Exception {
		DataStore jobds = para.getDataStore("gridjob");
		
		for(int i =0;i<jobds.rowCount();i++) {
			String jobno = jobds.getString(i,"jobno");
		
			de.clearSql();
			de.addSql(" update odssu.jobinfor ");
			de.addSql("    set joborder = :joborder ");
			de.addSql("  where jobno = :jobno ");
			de.setString("jobno", jobno);
			de.setInt("joborder", i+1);
			de.update();
		}
		return null;
	}
	public DataObject addJob(DataObject para) throws Exception {
		String jobname = para.getString("jobname");
		String jobdetail = para.getString("jobdetail");
		DataObject vdo = DataObject.getInstance();
		DataStore vds;
		de.clearSql();
		de.addSql("select 1 from odssu.jobinfor a where a.jobname = :jobname ");
		de.setString("jobname", jobname);
		vds = de.query();
		if(vds != null&&vds.rowCount()>1) {
			throw new AppException("职务已存在！");
		}
		//生成职务编号 当前时间精确到毫秒+三位随机数
		String jobno = "";
		
		do {
			jobno = getJobno();
			vds = DataStore.getInstance();
			de.clearSql();
			de.addSql(" select 1 from odssu.jobinfor a where a.jobno = :jobno  ");
			de.setString("jobno", jobno);
			vds = de.query();
		}while(vds != null&&vds.rowCount()>1);
		de.clearSql();
		de.addSql(" select nvl(max(joborder),0) joborder from odssu.jobinfor ");
		de.setString("jobno", jobno);
		vds = de.query();
		
		int order = vds.getInt(0, "joborder");
		
		de.clearSql();
		de.addSql(" insert into odssu.jobinfor (jobno,jobname,jobdetail,joborder) ");
		de.addSql(" values (:jobno,:jobname,:jobdetail,:joborder)");
		de.setString("jobno", jobno);
		de.setString("jobname", jobname);
		de.setString("jobdetail", jobdetail);
		de.setInt("joborder", order+1);
		de.update();
		
		vdo.put("result", "新增职务成功！");
		return vdo;
		
	}
	private String getJobno() {
		String jobnohead = "J"+System.currentTimeMillis();
		Random random = new Random();  
		int jobnotail = random.nextInt(999);
		String jobno = jobnohead + String.format("%03d", jobnotail); 
		return jobno;
	}
	
}
