package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface StudentMergeRepository extends CrudRepository<StudentMergeEntity, UUID>, JpaSpecificationExecutor<StudentMergeEntity> {
  List<StudentMergeEntity> findStudentMergeEntityByStudentID(UUID studentID);

  List<StudentMergeEntity> findStudentMergeEntityByStudentIDAndStudentMergeDirectionCode(UUID studentID, String studentMergeDirectionCode);
}
