package com.habits.habittracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingsRequest {
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean smsEnabled;
    private String notificationTime;
}
