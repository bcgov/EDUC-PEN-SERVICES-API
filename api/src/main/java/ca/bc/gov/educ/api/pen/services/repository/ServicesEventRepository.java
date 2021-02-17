package ca.bc.gov.educ.api.pen.services.repository;

import ca.bc.gov.educ.api.pen.services.model.ServicesEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServicesEventRepository extends JpaRepository<ServicesEvent, UUID> {
  List<ServicesEvent> findByEventStatus(String status);
}
