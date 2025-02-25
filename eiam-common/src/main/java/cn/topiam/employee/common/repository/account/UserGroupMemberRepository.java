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
package cn.topiam.employee.common.repository.account;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cn.topiam.employee.common.entity.account.UserGroupMemberEntity;

/**
 * 用户组成员
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2021/11/30 03:04
 */
@Repository
public interface UserGroupMemberRepository extends JpaRepository<UserGroupMemberEntity, Long>,
                                           QuerydslPredicateExecutor<UserGroupMemberEntity>,
                                           UserGroupMemberRepositoryCustomized {

    /**
     * 根据用户组ID和用户ID删除
     *
     * @param groupId {@link String}
     * @param userId  {@link String}
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 根据用户id所有用户组关联信息
     *
     * @param userId {@link Long}
     *
     * @return {@link List}
     */
    List<UserGroupMemberEntity> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID 批量删除关联关系
     *
     * @param userIds {@link String}
     */
    @Modifying
    @Query(value = "DELETE  FROM user_group_member WHERE user_id IN :userIds", nativeQuery = true)
    void deleteAllByUserId(@Param("userIds") Iterable<Long> userIds);

    /**
     * 根据用户ID 删除关联关系
     *
     * @param id {@link Long}
     */
    @Modifying
    @Query(value = "DELETE  FROM user_group_member WHERE user_id = :id", nativeQuery = true)
    void deleteByUserId(@Param("id") Long id);
}
