package com.webank.wecube.core.dto;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.ResourceItem;
import com.webank.wecube.core.interceptor.UsernameStorage;
import com.webank.wecube.core.service.resource.ResourceAvaliableStatus;
import com.webank.wecube.core.service.resource.ResourceItemType;
import com.webank.wecube.core.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ResourceItemDto {
    private Integer id;
    private String name;
    private String type;
    private String additionalProperties;
    private Integer resourceServerId;
    private Boolean isAllocated = true;
    private String purpose;
    private String status;
    private ResourceServerDto resourceServer;

    public static ResourceItemDto fromDomain(ResourceItem resourceItem) {
        ResourceItemDto resourceItemDto = new ResourceItemDto();
        resourceItemDto.setId(resourceItem.getId());
        resourceItemDto.setName(resourceItem.getName());
        resourceItemDto.setType(resourceItem.getType());
        resourceItemDto.setAdditionalProperties(resourceItem.getAdditionalProperties());
        resourceItemDto.setResourceServerId(resourceItem.getResourceServerId());
        if (resourceItem.getResourceServer() != null) {
            resourceItemDto.setResourceServer(ResourceServerDto.fromDomain(resourceItem.getResourceServer()));
        }
        resourceItemDto.setIsAllocated(resourceItem.getIsAllocated() != null && resourceItem.getIsAllocated() == 1 ? true : false);
        resourceItemDto.setPurpose(resourceItem.getPurpose());
        resourceItemDto.setStatus(resourceItem.getStatus());
        return resourceItemDto;
    }

    public static ResourceItem toDomain(ResourceItemDto resourceItemDto, ResourceItem existedResourceItem) {
        ResourceItem resourceItem = existedResourceItem;
        if (resourceItem == null) {
            resourceItem = new ResourceItem();
        }

        if (resourceItemDto.getId() != null) {
            resourceItem.setId(resourceItemDto.getId());
        }

        if (resourceItemDto.getName() != null) {
            resourceItem.setName(resourceItemDto.getName());
        }

        if (resourceItemDto.getType() != null) {
            validateItemType(resourceItemDto.getType());
            resourceItem.setType(resourceItemDto.getType());
        }

        if (resourceItemDto.getAdditionalProperties() != null) {
            resourceItem.setAdditionalProperties(resourceItemDto.getAdditionalProperties());
        }

        if (resourceItemDto.getResourceServerId() != null) {
            resourceItem.setResourceServerId(resourceItemDto.getResourceServerId());
        }

        if (resourceItemDto.getIsAllocated() != null) {
            resourceItem.setIsAllocated(resourceItemDto.getIsAllocated() != null && resourceItemDto.getIsAllocated() ? 1 : 0);
        }

        if (resourceItemDto.getPurpose() != null) {
            resourceItem.setPurpose(resourceItemDto.getPurpose());
        }

        if (resourceItemDto.getStatus() != null) {
            validateItemStatus(resourceItemDto.getStatus());
            resourceItem.setStatus(resourceItemDto.getStatus());
        }

        updateSystemFieldsWithDefaultValues(resourceItem);

        return resourceItem;
    }

    private static void updateSystemFieldsWithDefaultValues(ResourceItem resourceItem) {
        if (resourceItem.getAdditionalProperties() == null) {
            String defaultAdditionalProperties = null;
            if (ResourceItemType.fromCode(resourceItem.getType()) == ResourceItemType.MYSQL_DATABASE) {
                defaultAdditionalProperties = generateMysqlDatabaseDefaultAccount(resourceItem);
            }
            resourceItem.setAdditionalProperties(defaultAdditionalProperties);
        }

        if (resourceItem.getStatus() == null) {
            resourceItem.setStatus(ResourceAvaliableStatus.CREATED.getCode());
        }

        if (resourceItem.getCreatedBy() == null) {
            resourceItem.setCreatedBy(UsernameStorage.getIntance().get());
        }

        if (resourceItem.getCreatedDate() == null) {
            resourceItem.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        }

        resourceItem.setUpdatedBy(UsernameStorage.getIntance().get());
        resourceItem.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
    }

    private static String generateMysqlDatabaseDefaultAccount(ResourceItem resourceItem) {
        String defaultAdditionalProperties;
        Map<Object, Object> map = new HashMap<>();
        map.put("username", resourceItem.getName());
        map.put("password", resourceItem.getName());
        defaultAdditionalProperties = JsonUtils.toJsonString(map);
        return defaultAdditionalProperties;
    }

    private static void validateItemType(String itemType) {
        if (ResourceItemType.fromCode(itemType) == ResourceItemType.NONE) {
            throw new WecubeCoreException(String.format("Unsupported resource item type [%s].", itemType));
        }
    }

    private static void validateItemStatus(String status) {
        if (ResourceAvaliableStatus.fromCode(status) == ResourceAvaliableStatus.NONE) {
            throw new WecubeCoreException(String.format("Unsupported resource item status [%s].", status));
        }
    }
}
