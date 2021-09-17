INSERT INTO PEN_REQUEST_BATCH_VALIDATION_ISSUE_TYPE_CODE (CODE, LABEL, LEGACY_LABEL, DESCRIPTION, DISPLAY_ORDER,
                                                          EFFECTIVE_DATE,
                                                          EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                                          UPDATE_DATE)
VALUES ('SAMENAME', 'Legal Surname and Given are same.', 'C6, C8, C11, C13, C77, C78',
        'Legal Surname and Given are same.', 210, sysdate,
        to_date('2199-12-31', 'YYYY-MM-DD'),
        'IDIR/OMISHRA',
        sysdate,
        'IDIR/OMISHRA',
        sysdate);
