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
package cn.topiam.employee.common.entity.app;

import java.io.Serial;

import javax.persistence.*;

import cn.topiam.employee.common.enums.PermissionActionType;
import cn.topiam.employee.support.repository.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 应用权限
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2021/11/2 21:05
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@Table(name = "`app_permission_action`")
public class AppPermissionActionEntity extends BaseEntity<Long> {

    @Serial
    private static final long           serialVersionUID = -3954680915360748087L;

    /**
     * 权限值
     */
    @Column(name = "value_")
    private String                      value;
    /**
     * 描述
     */
    @Column(name = "name_")
    private String                      name;

    /**
     * 权限类型
     */
    @Column(name = "type_")
    private PermissionActionType        type;

    /**
     * 资源
     */
    @ManyToOne
    @JoinColumn(name = "resource_id")
    private AppPermissionResourceEntity resource;
}
