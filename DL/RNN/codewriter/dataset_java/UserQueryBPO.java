package com.dw.hsuods.op;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.CurrentUser;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;

public class UserQueryBPO extends BPO{

	public DataObject queryUserInfor( DataObject para) throws AppException {
		// 当前登录的用户
		CurrentUser user = this.getUser();
		String userId = user.getUserid();
		DataStore vds;
		DataObject vdo = DataObject.getInstance();
		//Date sysdate = DateUtil.getDBTime();

		if (userId == null || "".equals(userId.trim())) {
	 		throw new AppException("获取当前登录的用户ID为空");
		}
		de.clearSql();
		de.addSql(" select empname yhxm,d2c(:dbtime,'yyyy-mm-dd hh24:mi:ss') dlsj ");
		de.addSql("   from odssu.empinfor ");
		de.addSql("  where empno = :empno ");
		de.setDateTime("dbtime", DateUtil.getDBTime());
		de.setString("empno", userId);
		vds = de.query();
		
		if (vds == null || vds.rowCount() == 0) {
			throw new AppException("获取ID为" + userId + "用户信息出错");
		}
		vdo.put("userinfor", vds);
		return vdo;
	}

}
