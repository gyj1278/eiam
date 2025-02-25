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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 查询组织架构树结果
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2020/8/11 21:27
 */
@Data
@Schema(description = "查询组织架构树结果")
public class OrganizationTreeResult implements Serializable {

    @Serial
    private static final long            serialVersionUID = 5599721546299678344L;
    /**
     * 主键ID
     */
    @Parameter(description = "ID")
    private String                       id;

    /**
     * 名称
     */
    @Parameter(description = "名称")
    private String                       name;

    /**
     * 父级
     */
    @Parameter(description = "父级")
    private String                       parentId;

    /**
     * 显示路径
     */
    @Parameter(description = "显示路径")
    private String                       displayPath;

    /**
     * 编码
     */
    @Parameter(description = "编码")
    private String                       code;

    /**
     * 外部ID
     */
    @Parameter(description = "外部ID")
    private String                       externalId;

    /**
     * 类型
     */
    @Parameter(description = "类型")
    private String                       type;

    /**
     * 来源
     */
    @Parameter(description = "数据来源")
    private String                       dataOrigin;

    /**
     * 排序
     */
    @Parameter(description = "排序")
    private Integer                      order;

    /**
     * 是否启用
     */
    @Parameter(description = "是否启用")
    private Boolean                      enabled;

    /**
     * 是否叶子节点
     */
    @JsonProperty(value = "isLeaf")
    @Parameter(description = "是否叶子节点")
    private Boolean                      leaf;

    /**
     * 子级列表
     */
    @Parameter(description = "子级列表")
    private List<OrganizationTreeResult> children;
}
