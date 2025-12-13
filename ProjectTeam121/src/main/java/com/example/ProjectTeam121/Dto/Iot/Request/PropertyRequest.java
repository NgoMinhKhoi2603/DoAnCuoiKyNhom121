package com.example.ProjectTeam121.Dto.Iot.Request;

import com.example.ProjectTeam121.Dto.Enum.PropertyDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PropertyRequest {
    @NotBlank(message = "Tên thuộc tính không được để trống")
    @Size(max = 100, message = "Tên thuộc tính không quá 100 ký tự")
    private String name;

    @Size(max = 20, message = "Đơn vị tính không quá 20 ký tự")
    private String unit;

    @NotNull(message = "Kiểu dữ liệu không được để trống")
    private PropertyDataType dataType;
}