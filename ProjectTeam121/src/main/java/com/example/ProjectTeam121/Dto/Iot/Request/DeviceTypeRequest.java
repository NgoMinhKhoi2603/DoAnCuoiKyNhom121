package com.example.ProjectTeam121.Dto.Iot.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceTypeRequest {
    @NotBlank(message = "Tên loại thiết bị không được để trống")
    @Size(max = 100, message = "Tên loại thiết bị không quá 100 ký tự")
    private String name;

    @Size(max = 100, message = "Tên hãng sản xuất không quá 100 ký tự")
    private String manufacturer;

    private String description;

    @Size(max = 50, message = "Danh mục không quá 50 ký tự")
    private String category;
}