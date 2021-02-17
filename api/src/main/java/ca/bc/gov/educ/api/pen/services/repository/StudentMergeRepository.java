package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.StudentMergeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Student merge repository.
 */
public interface StudentMergeRepository extends JpaRepository<StudentMergeEntity, UUID>, JpaSpecificationExecutor<StudentMergeEntity> {
  /**
   * Find student merge entity by student id list.
   *
   * @param studentID the student id
   * @return the list
   */
  List<StudentMergeEntity> findStudentMergeEntityByStudentID(UUID studentID);

  /**
   * Find student merge entity by student id and student merge direction code list.
   *
   * @param studentID                 the student id
   * @param studentMergeDirectionCode the student merge direction code
   * @return the list
   */
  List<StudentMergeEntity> findStudentMergeEntityByStudentIDAndStudentMergeDirectionCode(UUID studentID, String studentMergeDirectionCode);

  /**
   * Find student merge entity by student id and merge student id optional.
   *
   * @param studentID      the student id
   * @param mergeStudentID the merge student id
   * @return the optional
   */
  Optional<StudentMergeEntity> findStudentMergeEntityByStudentIDAndMergeStudentID(UUID studentID, UUID mergeStudentID);

  /**
   * Find by student id and merge student id and student merge direction code optional.
   *
   * @param studentID          the student id
   * @param mergeStudentID     the merge student id
   * @param mergeDirectionCode the merge direction code
   * @return the optional
   */
  Optional<StudentMergeEntity> findByStudentIDAndMergeStudentIDAndStudentMergeDirectionCode(UUID studentID, UUID mergeStudentID, String mergeDirectionCode);
}
