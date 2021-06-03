INSERT INTO PEN_REQUEST_BATCH_VALIDATION_ISSUE_TYPE_CODE (CODE, LABEL, LEGACY_LABEL, DESCRIPTION, DISPLAY_ORDER,
                                                          EFFECTIVE_DATE,
                                                          EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                                          UPDATE_DATE)
VALUES ('DOBTOOYOUNG', 'DOBTOOYOUNG', 'BTY', 'Individual is too young to attend school', 190,
        sysdate,
        to_date('2099-12-31', 'YYYY-MM-DD'),
        'IDIR/OMISHRA',
        sysdate,
        'IDIR/OMISHRA',
        sysdate);
