package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.ActionLog;
import com.example.ProjectTeam121.Dto.Enum.HistoryType;
import com.example.ProjectTeam121.Dto.Request.ExportFilterRequest;
import com.example.ProjectTeam121.Dto.Response.ExportFilterResponse;
import com.example.ProjectTeam121.Entity.ExportFilter;
import com.example.ProjectTeam121.Entity.User;
import com.example.ProjectTeam121.Mapper.ExportFilterMapper;
import com.example.ProjectTeam121.Repository.ExportFilterRepository;
import com.example.ProjectTeam121.Repository.UserRepository;
import com.example.ProjectTeam121.utils.SecurityUtils;
import com.example.ProjectTeam121.utils.exceptions.ErrorCode;
import com.example.ProjectTeam121.utils.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportFilterService {

    private final ExportFilterRepository exportFilterRepository;
    private final UserRepository userRepository;
    private final ExportFilterMapper exportFilterMapper;
    private final HistoryService historyService; // Tùy chọn: nếu muốn ghi log

    private User getCurrentUser() {
        String email = SecurityUtils.getCurrentUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public ExportFilterResponse create(ExportFilterRequest request) {
        User user = getCurrentUser();

        ExportFilter filter = exportFilterMapper.toEntity(request);
        filter.setUser(user);

        ExportFilter savedFilter = exportFilterRepository.save(filter);

        // Ghi log (Tùy chọn, nếu bạn chưa định nghĩa HistoryType.FILTER thì có thể bỏ qua hoặc thêm vào enum)
        // historyService.saveHistory(savedFilter, ActionLog.CREATE, HistoryType.USER_MANAGEMENT, savedFilter.getId().toString(), user.getEmail());

        return exportFilterMapper.toResponse(savedFilter);
    }

    @Transactional(readOnly = true)
    public List<ExportFilterResponse> getMyFilters() {
        String email = SecurityUtils.getCurrentUsername();
        return exportFilterRepository.findByUser_Email(email)
                .stream()
                .map(exportFilterMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExportFilterResponse update(Long id, ExportFilterRequest request) {
        User user = getCurrentUser();
        ExportFilter filter = exportFilterRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy bộ lọc"));

        // Chỉ cho phép user sở hữu sửa bộ lọc của mình
        if (!filter.getUser().getId().equals(user.getId())) {
            throw new ValidationException(ErrorCode.ACCESS_DENIED, "Bạn không có quyền sửa bộ lọc này");
        }

        exportFilterMapper.updateEntityFromRequest(request, filter);
        ExportFilter updatedFilter = exportFilterRepository.save(filter);

        return exportFilterMapper.toResponse(updatedFilter);
    }

    @Transactional
    public void delete(Long id) {
        User user = getCurrentUser();
        ExportFilter filter = exportFilterRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy bộ lọc"));

        if (!filter.getUser().getId().equals(user.getId())) {
            throw new ValidationException(ErrorCode.ACCESS_DENIED, "Bạn không có quyền xóa bộ lọc này");
        }

        exportFilterRepository.delete(filter);
    }
}