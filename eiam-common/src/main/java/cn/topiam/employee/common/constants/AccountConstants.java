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
package cn.topiam.employee.common.constants;

import static cn.topiam.employee.support.constant.EiamConstants.API_PATH;

/**
 * 账户常量
 *
 * @author TopIAM
 * Created by support@topiam.cn on 2020/7/26 19:07
 */
public final class AccountConstants {

    /**
     * 用户API
     */
    public final static String   USER_PATH                  = API_PATH + "/user";
    /**
     * 组织机构API
     */
    public final static String   ORGANIZATION_PATH          = API_PATH + "/organization";
    /**
     * 用户组API
     */
    public final static String   USER_GROUP_PATH            = API_PATH + "/user_group";
    /**
     * 身份源API
     */
    public final static String   IDENTITY_SOURCE_PATH       = API_PATH + "/identity_source";

    /**
     * 组名称
     */
    public static final String   ACCOUNT_API_DOC_GROUP_NAME = "系统账户";

    /**
     * ACCOUNT_API_PATHS
     */
    public static final String[] ACCOUNT_API_PATHS          = { USER_PATH + "/**",
                                                                ORGANIZATION_PATH + "/**",
                                                                USER_GROUP_PATH + "/**",
                                                                IDENTITY_SOURCE_PATH + "/**" };
    /**
     * 身份源缓存 cacheName
     */
    public static final String   IDS_CACHE_NAME             = "ids";

    /**
     * user 缓存 cacheName
     */
    public static final String   USER_CACHE_NAME            = "user";

    /**
     * 根部门 ID
     */
    public static final String   ROOT_DEPT_ID               = "root";

    /**
     * 根部门名称
     */
    public static final String   ROOT_DEPT_NAME             = "TopIAM Employee";

}
