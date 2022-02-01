package ru.gosuslugi.pgu.fs.pgu.client;

import ru.gosuslugi.pgu.core.lk.model.PersonIdentifier;

public interface PguEmailClient {
    Boolean sendEmailInvitationToParticipant(Long orderId, PersonIdentifier participant);
}
