/*
 * eiam-identity-source-core - Employee Identity and Access Management Program
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
package cn.topiam.employee.identitysource.core.domain;

import java.io.Serializable;
import java.util.List;

import org.springframework.util.CollectionUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2022/2/28 23:03
 */
@Data
@NoArgsConstructor
public class Dept implements Serializable {

    /**
     * 部门id
     */
    private String     deptId;

    /**
     * 部门父id
     */
    private String     parentId;

    /**
     * 部门名称
     */
    private String     name;

    /**
     * 部门排序
     */
    private Long       order;

    /**
     * 子节点
     */
    private List<Dept> children;

    public boolean isLeaf() {
        return CollectionUtils.isEmpty(children);
    }
}
