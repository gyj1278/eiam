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
package cn.topiam.employee.common.repository.app;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cn.topiam.employee.common.entity.app.AppPermissionPolicyEntity;

/**
 * @author TopIAM
 * Created by support@topiam.cn on  2021/11/4 22:44
 */
@Repository
public interface AppPermissionPolicyRepository extends AppPermissionPolicyRepositoryCustomized,
                                               CrudRepository<AppPermissionPolicyEntity, Long>,
                                               PagingAndSortingRepository<AppPermissionPolicyEntity, Long>,
                                               QuerydslPredicateExecutor<AppPermissionPolicyEntity> {
    /**
     * 按主体 ID 删除所有
     *
     * @param subjectIds {@link String}
     */
    void deleteAllBySubjectIdIn(Collection<String> subjectIds);

    /**
     * 按客体 ID 删除所有
     *
     * @param objectIds {@link String}
     */
    void deleteAllByObjectIdIn(Collection<Long> objectIds);

    /**
     * 根据主体删除所有
     *
     * @param objectId
     */
    void deleteAllByObjectId(Long objectId);

    /**
     * 更新启用/禁用
     *
     * @param id     {@link Serializable}
     * @param status {@link Boolean}
     * @return {@link  Integer}
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "UPDATE AppPermissionResourceEntity set enabled =:status WHERE id =:id")
    Integer updateStatus(@Param(value = "id") Long id, @Param(value = "status") Boolean status);
}
