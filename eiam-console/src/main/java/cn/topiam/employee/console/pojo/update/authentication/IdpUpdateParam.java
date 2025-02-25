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
package cn.topiam.employee.console.pojo.update.authentication;

import java.io.Serial;
import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson2.JSONObject;

import cn.topiam.employee.common.enums.IdentityProviderType;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 认证源修改参数入参
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2020/8/21 21:21
 */
@Data
@Schema(description = "认证源修改参数")
public class IdpUpdateParam implements Serializable {
    @Serial
    private static final long    serialVersionUID = -1440230086940289961L;
    /**
     * ID
     */
    @NotBlank(message = "ID不能为空")
    @Schema(description = "ID")
    private String               id;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称")
    private String               name;

    /**
     * 平台
     */
    @NotNull(message = "平台不能为空")
    @Schema(description = "平台")
    private IdentityProviderType type;

    /**
     * 配置
     */
    @NotNull(message = "配置JSON不能为空")
    @Schema(description = "配置JSON")
    private JSONObject           config;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String               remark;

    /**
     * 是否显示
     */
    @Schema(description = "是否显示")
    private Boolean              displayed;
}
