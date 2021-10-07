INSERT INTO PEN_REQUEST_BATCH_VALIDATION_ISSUE_TYPE_CODE (CODE, LABEL, LEGACY_LABEL, DESCRIPTION, DISPLAY_ORDER,
                                                          EFFECTIVE_DATE,
                                                          EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                                          UPDATE_DATE)
VALUES ('NUMBERNAME', 'Field contains a number.', 'C',
        'Field contains a number.', 220, sysdate,
        to_date('2199-12-31', 'YYYY-MM-DD'),
        'IDIR/OMISHRA',
        sysdate,
        'IDIR/OMISHRA',
        sysdate);
