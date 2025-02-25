/*
 * eiam-common - Employee Identity and Access Management Program
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
package cn.topiam.employee.common.entity.app.po;

import org.opensaml.security.credential.Credential;

import cn.topiam.employee.common.entity.app.AppSaml2ConfigEntity;
import cn.topiam.employee.common.enums.app.AuthorizationType;
import cn.topiam.employee.common.enums.app.InitLoginType;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2022/8/23 20:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppSaml2ConfigPO extends AppSaml2ConfigEntity {
    /**
     * APP CODE
     */
    private String            appCode;

    /**
     * 模版
     */
    private String            appTemplate;

    /**
     * 客户端ID
     */
    private String            clientId;

    /**
     * 客户端秘钥
     */
    private String            clientSecret;

    /**
     * SSO 发起方
     */
    private InitLoginType     initLoginType;

    /**
     * SSO 登录链接
     */
    private String            initLoginUrl;

    /**
     * 授权范围
     */
    private AuthorizationType authorizationType;

    /**
     * IDP 签名证书
     */
    private Credential        idpSignCert;

    /**
     * IDP 加密证书
     */
    private Credential        idpEncryptCert;
}
