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
package cn.topiam.employee.console.pojo.save.account;

import java.io.Serial;
import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户创建请求入参
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2020/8/11 23:16
 */
@Data
@Schema(description = "创建用户分组入参")
public class UserGroupCreateParam implements Serializable {
    @Serial
    private static final long serialVersionUID = -6044649488381303849L;
    /**
     * 用户组名称
     */
    @Schema(description = "用户组名称")
    @NotBlank(message = "用户组名称不能为空")
    private String            name;

    /**
     * 用户组编码
     */
    @Schema(description = "用户组编码")
    @NotBlank(message = "用户组编码不能为空")
    private String            code;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String            remark;
}
