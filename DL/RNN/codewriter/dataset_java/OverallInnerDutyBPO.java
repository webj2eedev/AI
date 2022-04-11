package com.dw.hsuods.ws.org.overallInnerDuty;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class OverallInnerDutyBPO extends BPO{
	
	/**
	 * 方法简介.加载人社系统页面处/科室、人社所、人社站leaf页面数据
	 * @author fandq
	 * @date 创建时间 2015年8月10日
	 */
	public DataObject loadInnerDutyInfor(DataObject para) throws AppException {
		String orgtype = para.getString("orgtype");
		String orgno = para.getString("orgno");
		if (orgtype == null || orgtype.isEmpty()) {
			throw new AppException("获取到的orgtype为空");
		}
		de.clearSql();
  		de.addSql("select distinct a.roleno,a.rolename");
  		de.addSql("   from odssu.inner_duty a where a.orgtype = :orgtype and a.deforgno = :orgno order by a.rolename");
		this.de.setString("orgtype", orgtype);
		this.de.setString("orgno", orgno);
		DataStore orgtypeds = this.de.query();
		DataObject vdo = DataObject.getInstance();
		vdo.put("innerdutyds", orgtypeds);
		return vdo;
	}

}
