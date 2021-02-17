package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentMergeRepository extends JpaRepository<StudentMergeEntity, UUID>, JpaSpecificationExecutor<StudentMergeEntity> {
  List<StudentMergeEntity> findStudentMergeEntityByStudentID(UUID studentID);

  List<StudentMergeEntity> findStudentMergeEntityByStudentIDAndStudentMergeDirectionCode(UUID studentID, String studentMergeDirectionCode);

  Optional<StudentMergeEntity> findStudentMergeEntityByStudentIDAndMergeStudentID(UUID studentID, UUID mergeStudentID);

  Optional<StudentMergeEntity> findByStudentIDAndMergeStudentIDAndStudentMergeDirectionCode(UUID studentID, UUID mergeStudentID, String mergeDirectionCode);
}
