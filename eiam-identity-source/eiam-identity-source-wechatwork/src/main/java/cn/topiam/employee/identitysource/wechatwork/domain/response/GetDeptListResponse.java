/*
 * eiam-identity-source-wechatwork - Employee Identity and Access Management Program
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
package cn.topiam.employee.identitysource.wechatwork.domain.response;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2022-02-11 23:56
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GetDeptListResponse extends BaseResponse {
    private List<Department> department;
}
