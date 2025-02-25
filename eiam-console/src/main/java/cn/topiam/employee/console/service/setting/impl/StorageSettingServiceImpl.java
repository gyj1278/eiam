/*
 * eiam-console - Employee Identity and Access Management Program
 * Copyright © 2020-2022 TopIAM (support@topiam.cn)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cn.topiam.employee.console.service.setting.impl;

import org.springframework.stereotype.Service;

import cn.topiam.employee.common.entity.setting.SettingEntity;
import cn.topiam.employee.common.repository.setting.SettingRepository;
import cn.topiam.employee.console.converter.setting.StorageSettingConverter;
import cn.topiam.employee.console.pojo.result.setting.StorageProviderConfigResult;
import cn.topiam.employee.console.pojo.save.setting.StorageConfigSaveParam;
import cn.topiam.employee.console.service.setting.StorageSettingService;
import cn.topiam.employee.support.context.ApplicationContextHelp;
import static cn.topiam.employee.core.setting.constant.StorageProviderSettingConstants.STORAGE_BEAN_NAME;
import static cn.topiam.employee.core.setting.constant.StorageProviderSettingConstants.STORAGE_PROVIDER_KEY;

/**
 * 存储设置接口
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2021/11/1 21:43
 */
@Service
public class StorageSettingServiceImpl extends SettingServiceImpl implements StorageSettingService {

    /**
     * 更改存储启用禁用
     *
     * @return {@link Boolean}
     */
    @Override
    public Boolean disableStorage() {
        removeSetting(STORAGE_PROVIDER_KEY);
        // refresh
        ApplicationContextHelp.refresh(STORAGE_BEAN_NAME);
        return Boolean.TRUE;
    }

    /**
     * 保存存储配置
     *
     * @param param {@link StorageConfigSaveParam}
     * @return {@link Boolean}
     */
    @Override
    public Boolean saveStorageConfig(StorageConfigSaveParam param) {
        SettingEntity entity = storageSettingsConverter.storageConfigSaveParamToEntity(param);
        Boolean setting = saveSetting(entity);
        ApplicationContextHelp.refresh(STORAGE_BEAN_NAME);
        return setting;
    }

    /**
     * 获取存储配置
     *
     * @return {@link StorageProviderConfigResult}
     */
    @Override
    public StorageProviderConfigResult getStorageConfig() {
        SettingEntity entity = getSetting(STORAGE_PROVIDER_KEY);
        return storageSettingsConverter.entityToStorageProviderConfig(entity);
    }

    private final StorageSettingConverter storageSettingsConverter;

    public StorageSettingServiceImpl(SettingRepository settingsRepository,
                                     StorageSettingConverter storageSettingsConverter) {
        super(settingsRepository);
        this.storageSettingsConverter = storageSettingsConverter;
    }
}
