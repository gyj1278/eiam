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

import java.io.Serializable;

import cn.topiam.employee.common.enums.app.AppProtocol;
import cn.topiam.employee.common.enums.app.AppType;

import lombok.Data;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 应用列表返回
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2020/9/27 19:29
 */
@Data
@Schema(description = "应用列表返回")
public class AppListResult implements Serializable {
    /**
     * ID
     */
    @Parameter(description = "ID")
    private String      id;
    /**
     * 应用名称
     */
    @Parameter(description = "应用名称")
    private String      name;

    /**
     * 应用类型
     */
    @Parameter(description = "应用类型")
    private AppType     type;

    /**
     * 应用图标
     */
    @Parameter(description = "应用图标")
    private String      icon;

    /**
     * 模板
     */
    @Parameter(description = "模板")
    private String      template;

    /**
     * 协议
     */
    @Parameter(description = "协议")
    private AppProtocol protocol;

    /**
     * 是否启用
     */
    @Parameter(description = "是否启用")
    private Boolean     enabled;

    /**
     * 备注
     */
    @Parameter(description = "备注")
    private String      remark;
}
