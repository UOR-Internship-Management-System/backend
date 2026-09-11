package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

/**
 * A single positional record of string cell values, source-format agnostic.
 *
 * <p>Lets the CSV and Excel ingestion paths share one set of row-validation and row-parsing rules.
 */
interface LedgerRow {

    String get(int index);

    int size();
}
