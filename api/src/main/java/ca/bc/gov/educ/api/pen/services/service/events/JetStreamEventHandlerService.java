package ca.bc.gov.educ.api.pen.services.service.events;

import ca.bc.gov.educ.api.pen.services.repository.ServicesEventRepository;
import ca.bc.gov.educ.api.pen.services.struct.v1.ChoreographedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.pen.services.constants.EventStatus.MESSAGE_PUBLISHED;


/**
 * This class will process events from Jet Stream, which is used in choreography pattern.
 */
@Service
@Slf4j
public class JetStreamEventHandlerService {

  /**
   * The Event repository.
   */
  private final ServicesEventRepository eventRepository;


  /**
   * Instantiates a new Stan event handler service.
   *
   * @param eventRepository the ServicesEventRepository event repository
   */
  @Autowired
  public JetStreamEventHandlerService(final ServicesEventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional
  public void updateEventStatus(final ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      final var eventID = UUID.fromString(choreographedEvent.getEventID());
      final var eventOptional = this.eventRepository.findById(eventID);
      if (eventOptional.isPresent()) {
        final var event = eventOptional.get();
        event.setEventStatus(MESSAGE_PUBLISHED.toString());
        event.setUpdateDate(LocalDateTime.now());
        this.eventRepository.save(event);
      }
    }
  }
}
