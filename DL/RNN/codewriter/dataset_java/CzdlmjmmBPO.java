package com.dw.odssu.ws.emp.czdlmjmm;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class CzdlmjmmBPO extends BPO {

	/**
	 * 
	 * 描述:重置登陆名名及密码调用
	 * 
	 * @Description:
	 * @author 叶军
	 * @DataObject
	 * @date 2014-10-15
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public final DataObject fwresetLogAndPass(final DataObject para)
			throws AppException {
		String piid = para.getString("piid");
  		de.clearSql();
		DataObject vdo = DataObject.getInstance();
		DataStore vds;

		if ("".equals(piid.trim()) || piid == null) {
			throw new AppException("获取piid时为空");
		}

		de.clearSql();
  		de.addSql("  select c.piid,c.empno,e.empname  ");
  		de.addSql("   from odssu.empinfor e,          ");
  		de.addSql("        odssuws.czdlmjmm c         ");
  		de.addSql("  where e.empno = c.empno          ");
  		de.addSql("    and c.piid = :piid                 ");
		de.setString("piid", piid);
		vds = de.query();
		if (vds.rowCount() == 0) {
			throw new AppException("查询流程实例编号为" + piid + "的工单内容时为空");
		}
        
		vdo.put("empinfor", vds);
		return vdo;
	}

}
