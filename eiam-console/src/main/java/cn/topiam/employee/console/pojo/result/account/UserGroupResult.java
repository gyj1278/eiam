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
package cn.topiam.employee.console.pojo.result.account;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 查询用户分组详情
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2020/8/11 23:22
 */
@Data
@Schema(description = "根据ID查询用户分组结果")
public class UserGroupResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -5144879825451360221L;
    /**
     * 用户ID
     */
    @Parameter(description = "用户组ID")
    private String            id;
    /**
     * 用户组名称
     */
    @Parameter(description = "用户组名称")
    private String            name;

    /**
     * 用户组编码
     */
    @Parameter(description = "用户组编码")
    private String            code;

    /**
     * 备注
     */
    @Parameter(description = "备注")
    private String            remark;
}
