/*
 * eiam-authentication-wechat - Employee Identity and Access Management Program
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
package cn.topiam.employee.authentication.wechat;

import java.io.Serial;

import javax.validation.constraints.NotBlank;

import cn.topiam.employee.authentication.common.config.IdentityProviderConfig;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 微信开放平台扫码登录
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2021/12/9 22:07 21:
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WeChatIdpScanCodeConfig extends IdentityProviderConfig {
    @Serial
    private static final long serialVersionUID = -5831048603320371078L;
    /**
     * 客户端id
     */
    @NotBlank(message = "应用AppId不能为空")
    private String            appId;

    /**
     * 客户端Secret
     */
    @NotBlank(message = "应用AppId不能为空")
    private String            appSecret;
}
