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
package cn.topiam.employee.common.repository.account.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import cn.topiam.employee.common.entity.account.UserEntity;
import cn.topiam.employee.common.enums.DataOrigin;
import cn.topiam.employee.common.enums.UserStatus;

/**
 * @author TopIAM
 * Created by support@topiam.cn on  2022/2/13 22:25
 */
@SuppressWarnings("DuplicatedCode")
public class UserEntityMapper implements RowMapper<UserEntity> {
    /**
     * Implementations must implement this method to map each row of data
     * in the ResultSet. This method should not call {@code next()} on
     * the ResultSet; it is only supposed to map values of the current row.
     *
     * @param rs     the ResultSet to map (pre-initialized for the current row)
     * @param rowNum the number of the current row
     * @return the result object for the current row (may be {@code null})
     * @throws SQLException if an SQLException is encountered getting
     *                      column values (that is, there's no need to catch SQLException)
     */
    @Override
    public UserEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        //@formatter:off
        UserEntity user = new UserEntity();
        user.setId(rs.getLong("id_"));
        user.setUsername(rs.getString("username_"));
        user.setPassword(rs.getString("password_"));
        user.setEmail(rs.getString("email_"));
        user.setPhone(rs.getString("phone_"));
        user.setPhoneAreaCode(rs.getString("phone_area_code"));
        user.setFullName(rs.getString("full_name"));
        user.setNickName(rs.getString("nick_name"));
        user.setAvatar(rs.getString("avatar_"));
        user.setStatus(UserStatus.getStatus(rs.getString("status_")));
        user.setDataOrigin(DataOrigin.getType(rs.getString("data_origin")));
        user.setEmailVerified(rs.getBoolean("email_verified"));
        user.setSharedSecret(rs.getString("shared_secret"));
        user.setTotpBind(rs.getBoolean("totp_bind"));
        user.setAuthTotal(rs.getLong("auth_total"));
        user.setLastAuthIp(rs.getString("last_auth_ip"));
        user.setLastAuthTime(ObjectUtils.isNotEmpty(rs.getTimestamp("last_auth_time")) ? rs.getTimestamp("last_auth_time").toLocalDateTime() : null);
        user.setExpand(rs.getString("expand_"));
        user.setExternalId(rs.getString("external_id"));
        user.setIdentitySourceId(rs.getLong("identity_source_id"));
        user.setExpireDate(ObjectUtils.isNotEmpty(rs.getTimestamp("expire_date")) ? rs.getDate("expire_date").toLocalDate() : null);
        user.setLastAuthTime(ObjectUtils.isNotEmpty(rs.getTimestamp("last_auth_time")) ? rs.getTimestamp("last_auth_time").toLocalDateTime() : null);
        //额外数据
        user.setCreateBy(rs.getString("create_by"));
        user.setCreateTime(ObjectUtils.isNotEmpty(rs.getTimestamp("create_time")) ? rs.getTimestamp("create_time").toLocalDateTime() : null);
        user.setUpdateBy(rs.getString("update_by"));
        user.setUpdateTime(ObjectUtils.isNotEmpty(rs.getTimestamp("update_time")) ? rs.getTimestamp("update_time").toLocalDateTime() : null);
        user.setRemark(rs.getString("remark_"));
        return user;
        //@formatter:on
    }
}
