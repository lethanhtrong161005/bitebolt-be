package com.bitebolt.audit.repository;

import com.bitebolt.audit.document.AuditLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Elasticsearch Repository for AuditLogDocument.
 */
@Repository
public interface AuditLogElasticsearchRepository extends ElasticsearchRepository<AuditLogDocument, String> {
}
