package ca.bc.gov.educ.api.pen.services.service;

import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationFieldCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueSeverityCodeEntity;
import ca.bc.gov.educ.api.pen.services.model.PenRequestBatchValidationIssueTypeCodeEntity;
import ca.bc.gov.educ.api.pen.services.repository.PenRequestBatchValidationFieldCodeRepository;
import ca.bc.gov.educ.api.pen.services.repository.PenRequestBatchValidationIssueSeverityCodeRepository;
import ca.bc.gov.educ.api.pen.services.repository.PenRequestBatchValidationIssueTypeCodeRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Service
public class PenRequestBatchStudentValidationIssueCodesService {

    /**
     * The Pen request batch student validation issue field code repository.
     */
    @Getter(PRIVATE)
    private final PenRequestBatchValidationFieldCodeRepository fieldCodeRepository;

    /**
     * The Pen request batch student validation issue field code repository.
     */
    @Getter(PRIVATE)
    private final PenRequestBatchValidationIssueSeverityCodeRepository severityCodeRepository;

    /**
     * The Pen request batch student validation issue field code repository.
     */
    @Getter(PRIVATE)
    private final PenRequestBatchValidationIssueTypeCodeRepository typeCodeRepository;

    /**
     * Instantiates a new Pen request batch student validation issue code service.
     *
     * @param fieldCodeRepository                  Pen request batch student validation issue field code repository
     * @param severityCodeRepository                Pen request batch student validation issue severity code repository
     * @param typeCodeRepository                  Pen request batch student validation issue type code repository
     */
    @Autowired
    public PenRequestBatchStudentValidationIssueCodesService(PenRequestBatchValidationFieldCodeRepository fieldCodeRepository, PenRequestBatchValidationIssueTypeCodeRepository typeCodeRepository, PenRequestBatchValidationIssueSeverityCodeRepository severityCodeRepository) {
        this.fieldCodeRepository = fieldCodeRepository;
        this.typeCodeRepository = typeCodeRepository;
        this.severityCodeRepository = severityCodeRepository;
    }

    /**
     * Gets all pen request batch student validation issue field codes.
     *
     * @return the all pen request batch student validation issue field codes
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<PenRequestBatchValidationFieldCodeEntity> getAllPrbValidationFieldCodes() {
        return getFieldCodeRepository().findAll();
    }

    /**
     * Gets all pen request batch student validation issue severity codes.
     *
     * @return the all pen request batch student validation issue severity codes
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<PenRequestBatchValidationIssueSeverityCodeEntity> getAllPrbValidationIssueSeverityCodes() {
        return getSeverityCodeRepository().findAll();
    }

    /**
     * Gets all pen request batch student validation issue type codes.
     *
     * @return the all pen request batch student validation issue type codes
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<PenRequestBatchValidationIssueTypeCodeEntity> getAllPrbValidationIssueTypeCodes() {
        return getTypeCodeRepository().findAll();
    }
}
