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
package cn.topiam.employee.common.repository.app.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import cn.topiam.employee.common.entity.app.po.AppAccountPO;
import cn.topiam.employee.common.enums.app.AppProtocol;
import cn.topiam.employee.common.enums.app.AppType;

/**
 * @author TopIAM
 * Created by support@topiam.cn on  2022/2/13 22:25
 */
public class AppAccountPoMapper implements RowMapper<AppAccountPO> {

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
    public AppAccountPO mapRow(ResultSet rs, int rowNum) throws SQLException {
        AppAccountPO appAccount = new AppAccountPO();
        appAccount.setId(rs.getLong("id_"));
        appAccount.setAppId(rs.getLong("app_id"));
        appAccount.setUserId(rs.getLong("user_id"));
        appAccount.setAccount(rs.getString("account_"));
        appAccount.setUsername(rs.getString("username_"));
        appAccount.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
        appAccount.setAppName(rs.getString("app_name"));
        appAccount.setAppType(AppType.getType(rs.getString("app_type")));
        appAccount.setAppTemplate(rs.getString("app_template"));
        appAccount.setAppProtocol(AppProtocol.getType(rs.getString("app_protocol")));
        return appAccount;
    }
}
