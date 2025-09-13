package com.pardal.app.entity.Dto;

import com.pardal.app.entity.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;


@AllArgsConstructor
@NoArgsConstructor
public class TicketsDto implements Serializable {
    Long id;
    @NotNull
    Company company;
    @NotNull
    User createdByUser;
    Agent assignedAgent;
    Product product;
    @NotNull
    Category category;
    Subcategory subcategory;
    @NotNull
    Priority priority;
    @NotNull
    Status currentStatus;
    @NotNull
    SlaPlan sLAPlan;
    @NotNull
    @Size(max = 200)
    String title;
    String description;
    @Size(max = 40)
    String channel;
    @Size(max = 60)
    String device;
    Instant createdAt;
    Instant firstResponseAt;
    Instant closedAt;
}