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
package cn.topiam.employee.console.service.app;

import cn.topiam.employee.common.enums.CheckValidityType;
import cn.topiam.employee.console.pojo.query.app.AppResourceListQuery;
import cn.topiam.employee.console.pojo.result.app.AppPermissionResourceGetResult;
import cn.topiam.employee.console.pojo.result.app.AppPermissionResourceListResult;
import cn.topiam.employee.console.pojo.save.app.AppPermissionResourceCreateParam;
import cn.topiam.employee.console.pojo.update.app.AppPermissionResourceUpdateParam;
import cn.topiam.employee.support.repository.page.domain.Page;
import cn.topiam.employee.support.repository.page.domain.PageModel;

/**
 * <p>
 * 资源权限 服务类
 * </p>
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2020-08-10
 */
public interface AppPermissionResourceService {
    /**
     * 获取资源列表
     *
     * @param page  {@link PageModel}
     * @param query {@link AppResourceListQuery}
     * @return {@link AppPermissionResourceListResult}
     */
    Page<AppPermissionResourceListResult> getPermissionResourceList(PageModel page,
                                                                    AppResourceListQuery query);

    /**
     * 获取资源
     *
     * @param id {@link String}
     * @return {@link AppPermissionResourceGetResult}
     */
    AppPermissionResourceGetResult getPermissionResource(String id);

    /**
     * 删除资源
     *
     * @param id {@link String}
     * @return {@link Boolean}
     */
    Boolean deletePermissionResource(String id);

    /**
     * 启用/禁用
     *
     * @param id      {@link Long}
     * @param enabled {@link Boolean}
     * @return {@link Boolean}
     */
    Boolean updateStatus(Long id, boolean enabled);

    /**
     * 创建资源
     *
     * @param param {@link AppPermissionResourceCreateParam}
     * @return {@link Boolean}
     */
    Boolean createPermissionResource(AppPermissionResourceCreateParam param);

    /**
     * 更新资源
     *
     * @param param {@link AppPermissionResourceUpdateParam}
     * @return {@link Boolean}
     */
    Boolean updatePermissionResource(AppPermissionResourceUpdateParam param);

    /**
     * 参数有效性验证
     *
     * @param type     {@link CheckValidityType}
     * @param value    {@link String}
     * @param appId {@link Long}
     * @param id       {@link Long}
     * @return {@link Boolean}
     */
    Boolean permissionResourceParamCheck(CheckValidityType type, String value, Long appId, Long id);
}
