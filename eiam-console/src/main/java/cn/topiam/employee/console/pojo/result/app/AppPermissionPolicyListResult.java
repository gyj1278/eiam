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
package cn.topiam.employee.console.pojo.result.app;

import cn.topiam.employee.common.enums.PolicyEffect;
import cn.topiam.employee.common.enums.PolicyObjectType;
import cn.topiam.employee.common.enums.PolicySubjectType;

import lombok.Data;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2022/9/9 23:33
 */
@Schema(description = "获取授权列表")
@Data
public class AppPermissionPolicyListResult {
    /**
     * ID
     */
    @Parameter(description = "id")
    private String            id;

    /**
     * 授权主体id
     */
    @Parameter(description = "授权主体id")
    private String            subjectId;

    /**
     * 授权主体名称
     */
    @Parameter(description = "授权主体名称")
    private String            subjectName;

    /**
     * 权限主体类型（用户、角色、分组、组织机构）
     */
    @Parameter(description = "授权主体类型")
    private PolicySubjectType subjectType;

    /**
     * 权限客体ID
     */
    @Parameter(description = "授权客体id")
    private Long              objectId;

    /**
     * 权限客体名菜
     */
    @Parameter(description = "授权客体名称")
    private String            objectName;

    /**
     * 权限客体类型（权限、角色）
     */
    @Parameter(description = "授权客体类型")
    private PolicyObjectType  objectType;

    /**
     * 授权作用
     */
    @Parameter(description = "授权作用")
    private PolicyEffect      effect;
}
