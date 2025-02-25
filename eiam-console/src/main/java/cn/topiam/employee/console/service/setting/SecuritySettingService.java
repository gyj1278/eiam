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
package cn.topiam.employee.console.service.setting;

import cn.topiam.employee.console.pojo.result.setting.SecurityBasicConfigResult;
import cn.topiam.employee.console.pojo.result.setting.SecurityCaptchaConfigResult;
import cn.topiam.employee.console.pojo.result.setting.SecurityMfaConfigResult;
import cn.topiam.employee.console.pojo.save.setting.SecurityBasicSaveParam;
import cn.topiam.employee.console.pojo.save.setting.SecurityCaptchaSaveParam;
import cn.topiam.employee.console.pojo.save.setting.SecurityMfaSaveParam;

/**
 * <p>
 * 安全设置表 服务类
 * </p>
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2020-10-01
 */
public interface SecuritySettingService extends SettingService {

    /**
     * 获取配置
     *
     * @return {@link SecurityBasicConfigResult}
     */
    SecurityBasicConfigResult getBasicConfig();

    /**
     * 保存配置
     *
     * @param param {@link SecurityBasicSaveParam}
     * @return {@link Boolean}
     */
    Boolean saveBasicConfig(SecurityBasicSaveParam param);

    /**
     * 获取验证码配置
     *
     * @return  {@link SecurityCaptchaConfigResult}
     */
    SecurityCaptchaConfigResult getCaptchaProviderConfig();

    /**
     * 保存行为验证码
     *
     * @param param {@link SecurityCaptchaSaveParam}
     * @return {@link Boolean}
     */
    Boolean saveCaptchaProviderConfig(SecurityCaptchaSaveParam param);

    /**
     * 禁用行为验证码
     *
     * @return {@link Boolean}
     */
    Boolean disableCaptchaProvider();

    /**
     * 获取MFA配置
     *
     * @return {@link SecurityMfaConfigResult}
     */
    SecurityMfaConfigResult getMfaConfig();

    /**
     * 保存行为验证码
     *
     * @param param {@link SecurityMfaSaveParam}
     * @return {@link Boolean}
     */
    Boolean saveMfaConfig(SecurityMfaSaveParam param);

}
