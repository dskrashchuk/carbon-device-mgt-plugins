/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.iot.util.DeviceTypes;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.util.IotDeviceManagementUtil;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceManagement {

	private static Log log = LogFactory.getLog(DeviceManagement.class);
    private static boolean serverReady = false;
    private PrivilegedCarbonContext ctx;
	private String tenantDomain;

	public DeviceManagement(String tenantDomain){
		this.tenantDomain=tenantDomain;
		PrivilegedCarbonContext.startTenantFlow();
		ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantDomain(tenantDomain, true);
	}

    public static boolean isServerReady() {
        return serverReady;
    }

    public static void setServerReady(boolean serverReady) {
        DeviceManagement.serverReady = serverReady;
    }

	public boolean isExist(String owner, DeviceIdentifier deviceIdentifier)
			throws DeviceManagementException {

		DeviceManagementProviderService dmService = getDeviceManagementService();
		if (dmService.isEnrolled(deviceIdentifier)) {
			Device device=dmService.getDevice(deviceIdentifier);
				if (device.getEnrolmentInfo().getOwner().equals(owner)) {
					return true;
				}
		}

		return false;
	}

	public DeviceManagementProviderService getDeviceManagementService() {

		DeviceManagementProviderService dmService;
		dmService =(DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
		return dmService;
	}

	public void endTenantFlow(){

		PrivilegedCarbonContext.endTenantFlow();
	}



	public Device[] getActiveDevices(String username)
			throws DeviceManagementException {


		List<Device> devices = getDeviceManagementService().getDevicesOfUser(
				username);
		List<Device> activeDevices = new ArrayList<>();
		if (devices != null) {
			for (Device device : devices) {
				if (device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
					activeDevices.add(device);
				}
			}
		}
		return activeDevices.toArray(new Device[]{});
	}

	public int getActiveDeviceCount(String username)
			throws DeviceManagementException {


		List<Device> devices = getDeviceManagementService().getDevicesOfUser(username);


		if (devices != null) {
			List<Device> activeDevices = new ArrayList<>();
			for (Device device : devices) {
				if (device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
					activeDevices.add(device);
				}
			}
			return activeDevices.size();
		}
		return 0;
	}

	public ZipArchive getSketchArchive(String archivesPath, String templateSketchPath, Map contextParams)
			throws DeviceManagementException {
		/*  create a context and add data */

		try {
			return IotDeviceManagementUtil.getSketchArchive(archivesPath, templateSketchPath,
			                                                contextParams);
		} catch (IOException e) {
			throw new DeviceManagementException("Zip File Creation Failed",e);
		}
	}

	public DeviceTypes[] getDeviceTypes(int tenantId)
			throws DeviceManagementDAOException {


		List<DeviceType> deviceTypes = DeviceManagementDAOFactory.getDeviceTypeDAO().getDeviceTypes(tenantId);
		DeviceTypes dTypes[] = new DeviceTypes[deviceTypes.size()];
		int iter = 0;
		for (DeviceType type : deviceTypes) {

			DeviceTypes dt = new DeviceTypes();
			dt.setName(type.getName());
			dTypes[iter] = dt;
			iter++;

		}
		return dTypes;


	}

}
