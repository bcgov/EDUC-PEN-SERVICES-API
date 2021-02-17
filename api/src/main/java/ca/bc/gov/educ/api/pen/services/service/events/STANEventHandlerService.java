package ca.bc.gov.educ.api.pen.services.service.events;

import ca.bc.gov.educ.api.pen.services.repository.ServicesEventRepository;
import ca.bc.gov.educ.api.pen.services.struct.v1.ChoreographedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.EventStatus.MESSAGE_PUBLISHED;


/**
 * This class will process events from STAN, which is used in choreography pattern, where messages are published if a student is created or updated.
 */
@Service
@Slf4j
public class STANEventHandlerService {

  private final ServicesEventRepository eventRepository;


  /**
   * Instantiates a new Stan event handler service.
   *
   * @param eventRepository the ServicesEventRepository event repository
   */
  @Autowired
  public STANEventHandlerService(ServicesEventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional
  public void updateEventStatus(ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      var eventID = UUID.fromString(choreographedEvent.getEventID());
      var eventOptional = eventRepository.findById(eventID);
      if (eventOptional.isPresent()) {
        var event = eventOptional.get();
        event.setEventStatus(MESSAGE_PUBLISHED.toString());
        eventRepository.save(event);
      }
    }
  }
}
