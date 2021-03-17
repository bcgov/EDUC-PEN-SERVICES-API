INSERT INTO PEN_REQUEST_BATCH_VALIDATION_ISSUE_TYPE_CODE (CODE, LABEL, LEGACY_LABEL, DESCRIPTION, DISPLAY_ORDER,
                                                          EFFECTIVE_DATE,
                                                          EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                                          UPDATE_DATE)
VALUES ('REPEATCHARS', 'Field contains repeated characters', 'C6, C8, C11, C13, C77, C78',
        'Field contains repeated characters', 200, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'),
        'IDIR/JOCOX',
        to_date('2020-11-06', 'YYYY-MM-DD'),
        'IDIR/JOCOX',
        to_date('2020-11-06', 'YYYY-MM-DD'));