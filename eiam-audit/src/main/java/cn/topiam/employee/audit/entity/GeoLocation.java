/*
 * eiam-audit - Employee Identity and Access Management Program
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
package cn.topiam.employee.audit.entity;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import cn.topiam.employee.common.geo.maxmind.enums.GeoLocationProvider;

import lombok.Builder;
import lombok.Data;

/**
 * 地理位置
 *
 * @author TopIAM
 * Created by support@topiam.cn on  2022/11/5 23:31
 */
@Data
@Builder
public class GeoLocation implements Serializable {

    @Serial
    private static final long   serialVersionUID = -1144169992714000310L;

    /**
     * IP
     */
    @Field(type = FieldType.Ip, name = "ip")
    private String              ip;

    /**
     * continent code
     */
    @Field(type = FieldType.Keyword, name = "continent_code")
    private String              continentCode;

    /**
     * continent Name
     */
    @Field(type = FieldType.Text, name = "continent_code")
    private String              continentName;

    /**
     * 国家code
     */
    @Field(type = FieldType.Keyword, name = "country_code")
    private String              countryCode;

    /**
     * 国家名称
     */
    @Field(type = FieldType.Text, name = "country_name")
    private String              countryName;

    /**
     * 省份code
     */
    @Field(type = FieldType.Keyword, name = "province_code")
    private String              provinceCode;

    /**
     * 省份
     */
    @Field(type = FieldType.Text, name = "province_name")
    private String              provinceName;

    /**
     * 城市code
     */
    @Field(type = FieldType.Keyword, name = "city_code")
    private String              cityCode;

    /**
     * 城市名称
     */
    @Field(type = FieldType.Text, name = "city_name")
    private String              cityName;

    /**
     * 地理坐标
     */
    @GeoPointField
    private GeoPoint            point;

    /**
     * 提供商
     */
    @Field(type = FieldType.Keyword, name = "provider")
    private GeoLocationProvider provider;

}
