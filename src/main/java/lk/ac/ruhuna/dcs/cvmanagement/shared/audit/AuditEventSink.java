package lk.ac.ruhuna.dcs.cvmanagement.shared.audit;

/** Persistence port implemented by the BMD-012 Audit Log module. */
public interface AuditEventSink {

    void persist(AuditEvent event);
}

