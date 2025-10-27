package com.example.ProjectTeam121.Service.Iot;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Iot.Request.PropertyRequest;
import com.example.ProjectTeam121.Dto.Iot.Response.PropertyResponse;
import com.example.ProjectTeam121.Entity.Iot.Property;
import com.example.ProjectTeam121.Mapper.Iot.PropertyMapper;
import com.example.ProjectTeam121.Repository.Iot.PropertyRepository;
import com.example.ProjectTeam121.Service.HistoryService;
import com.example.ProjectTeam121.utils.SecurityUtils;
import com.example.ProjectTeam121.utils.exceptions.ErrorCode;
import com.example.ProjectTeam121.utils.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final HistoryService historyService;

    private Property findById(String id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));
    }

    @Transactional
    public PropertyResponse create(PropertyRequest request) {
        if (propertyRepository.existsByName(request.getName())) {
            throw new ValidationException(ErrorCode.PROPERTY_NAME_EXISTS, "Property name already exists");
        }
        Property property = propertyMapper.toEntity(request);
        Property savedProperty = propertyRepository.save(property);

        historyService.saveHistory(savedProperty, ActionLog.CREATE, HistoryType.PROPERTY_MANAGEMENT,
                savedProperty.getId(), SecurityUtils.getCurrentUsername());

        return propertyMapper.toResponse(savedProperty);
    }

    @Transactional
    public PropertyResponse update(String id, PropertyRequest request) {
        Property property = findById(id);
        propertyMapper.updateEntityFromRequest(request, property);
        Property updatedProperty = propertyRepository.save(property);

        historyService.saveHistory(updatedProperty, ActionLog.UPDATE, HistoryType.PROPERTY_MANAGEMENT,
                updatedProperty.getId(), SecurityUtils.getCurrentUsername());

        return propertyMapper.toResponse(updatedProperty);
    }

    @Transactional
    public void delete(String id) {
        Property property = findById(id);
        // (Tùy chọn: kiểm tra xem có sensor nào đang dùng property này không)
        propertyRepository.delete(property);

        historyService.saveHistory(property, ActionLog.DELETE, HistoryType.PROPERTY_MANAGEMENT,
                property.getId(), SecurityUtils.getCurrentUsername());
    }

    @Transactional(readOnly = true)
    public PropertyResponse getById(String id) {
        return propertyMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<PropertyResponse> getAll(Pageable pageable) {
        return propertyRepository.findAll(pageable).map(propertyMapper::toResponse);
    }
}