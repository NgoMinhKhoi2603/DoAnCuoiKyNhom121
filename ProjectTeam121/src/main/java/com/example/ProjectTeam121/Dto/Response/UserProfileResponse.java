package com.example.ProjectTeam121.Dto.Response;

import com.example.ProjectTeam121.Dto.Enum.UnitEnum;
import lombok.Data;

@Data
public class UserProfileResponse {

    private String fullName;
    private String email;
    private UnitEnum unit;
    private String avatar;


    public String getDisplayName() {
        return fullName;
    }

    // Hiển thị text đẹp cho FE
    public String getUnitLabel() {
        if (unit == null) return null;
        switch (unit) {
            case CNTT: return "Khoa Công Nghệ Thông Tin";
            case DTVT: return "Khoa Điện Tử Viễn Thông";
            default: return unit.name();
        }
    }
}
