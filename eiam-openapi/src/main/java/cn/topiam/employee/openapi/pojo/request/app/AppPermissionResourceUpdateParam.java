/*
 * eiam-openapi - Employee Identity and Access Management Program
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
package cn.topiam.employee.openapi.pojo.request.app;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

/**
 * 资源修改参数
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2020/8/26 21:46
 */
@Data
@Schema(description = "修改资源入参")
public class AppPermissionResourceUpdateParam implements Serializable {
    @Serial
    private static final long               serialVersionUID = 6021548372386059064L;
    /**
     * ID
     */
    @Schema(accessMode = READ_ONLY)
    @NotBlank(message = "ID不能为空")
    private String                          id;
    /**
     * 名称
     */
    @Schema(description = "资源名称")
    @NotBlank(message = "资源名称不能为空")
    private String                          name;
    /**
     * 描述
     */
    @Schema(description = "资源描述")
    @NotBlank(message = "资源描述不能为空")
    private String                          desc;

    /**
     * 资源权限
     */
    @Schema(description = "资源权限")
    @NotNull(message = "资源权限不能为空")
    private List<AppPermissionsActionParam> actions;
}
