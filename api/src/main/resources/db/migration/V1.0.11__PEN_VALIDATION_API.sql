CREATE TABLE PEN_SERVICES_MACRO_TYPE_CODE
(
    PEN_SERVICES_MACRO_TYPE_CODE          VARCHAR2(10)           NOT NULL,
    LABEL                                 VARCHAR2(30),
    DESCRIPTION                           VARCHAR2(255),
    DISPLAY_ORDER                         NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE                        DATE                   NOT NULL,
    EXPIRY_DATE                           DATE                   NOT NULL,
    CREATE_USER                           VARCHAR2(32)           NOT NULL,
    CREATE_DATE                           DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                           VARCHAR2(32)           NOT NULL,
    UPDATE_DATE                           DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_SERVICES_MACRO_TYPE_CODE_PK PRIMARY KEY (PEN_SERVICES_MACRO_TYPE_CODE)
);
COMMENT ON TABLE PEN_SERVICES_MACRO_TYPE_CODE IS 'Macro Type Code indicates the supported types of text macros.';

CREATE TABLE PEN_SERVICES_MACRO
(
    PEN_SERVICES_MACRO_ID          RAW(16)              NOT NULL,
    MACRO_CODE                     VARCHAR2(10)         NOT NULL,
    MACRO_TEXT                     VARCHAR2(4000)       NOT NULL,
    MACRO_TYPE_CODE                VARCHAR2(10)         NOT NULL,
    CREATE_USER                    VARCHAR2(32)         NOT NULL,
    CREATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                    VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_SERVICES_MACRO_PK PRIMARY KEY (PEN_SERVICES_MACRO_ID)
);
COMMENT ON TABLE PEN_SERVICES_MACRO IS 'List of text macros used as standard messages by PEN Staff when completing tasks.';
COMMENT ON COLUMN PEN_SERVICES_MACRO.MACRO_CODE IS 'A short text string that identifies the macro and when identified will be replaced by the macro text.';
COMMENT ON COLUMN PEN_SERVICES_MACRO.MACRO_TEXT IS 'A standard text string that will be substituted for the macro code by the application.';
COMMENT ON COLUMN PEN_SERVICES_MACRO.MACRO_TYPE_CODE IS 'A code value indicating the type, or class, of the text macro.';

ALTER TABLE PEN_SERVICES_MACRO
    ADD CONSTRAINT UQ_SERVICES_MACRO_ID_CODE_TYPE UNIQUE (MACRO_CODE, MACRO_TYPE_CODE);
ALTER TABLE PEN_SERVICES_MACRO
    ADD CONSTRAINT FK_SERVICES_MACRO_TYPE_CODE FOREIGN KEY (MACRO_TYPE_CODE) REFERENCES PEN_SERVICES_MACRO_TYPE_CODE (PEN_SERVICES_MACRO_TYPE_CODE);

INSERT INTO PEN_SERVICES_MACRO_TYPE_CODE (PEN_SERVICES_MACRO_TYPE_CODE, LABEL, DESCRIPTION,
                                          DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE,
                                          UPDATE_USER, UPDATE_DATE)
VALUES ('MERGE', 'Merge Reason Macro', 'Macros used when merging PENs', 1,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO PEN_SERVICES_MACRO (PEN_SERVICES_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'MID',
        'Merged Due to Ministry Identified Duplicate.' ,
        'MERGE', 'IDIR/MINYANG', to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO PEN_SERVICES_MACRO (PEN_SERVICES_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'SID',
        'Merged Due to School Identified Duplicate.' ,
        'MERGE', 'IDIR/MINYANG', to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));