package com.bitebolt.audit.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 * Elasticsearch Document representation of Audit Event.
 * Supports enterprise-grade full-text search across traceId, actorId, actorEmail, actorName, action, and details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "audit_logs", createIndex = false)
public class AuditLogDocument {

  @Id
  private String id;

  @Field(type = FieldType.Keyword)
  private String traceId;

  @Field(type = FieldType.Keyword)
  private String actorId;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String actorEmail;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String actorName;

  @Field(type = FieldType.Keyword)
  private String action;

  @Field(type = FieldType.Keyword)
  private String status;

  @Field(type = FieldType.Keyword)
  private String service;

  @Field(type = FieldType.Keyword)
  private String actorIp;

  @Field(type = FieldType.Keyword)
  private String resourceType;

  @Field(type = FieldType.Keyword)
  private String resourceId;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String details;

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private Instant createdAt;
}
