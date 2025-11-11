package com.pardal.app.entity.dto;

import lombok.Data;

@Data
public class AuditDto
{
   private String event;
   private String user;
   private String date;
   private String locale;
   private String details;
}
