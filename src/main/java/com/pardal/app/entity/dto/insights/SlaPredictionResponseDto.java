package com.pardal.app.entity.dto.insights;

public record SlaPredictionResponseDto(
        Integer subcategoryId,
        String subcategoryName,
        Double averageRiskProbability
) {
}
