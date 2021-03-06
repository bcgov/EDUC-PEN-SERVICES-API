--Create student merge source code table
CREATE TABLE STUDENT_MERGE_SOURCE_CODE (
                                           MERGE_SOURCE_CODE VARCHAR2(10) NOT NULL,
                                           LABEL VARCHAR2(30),
                                           DESCRIPTION VARCHAR2(255),
                                           DISPLAY_ORDER NUMBER DEFAULT 1 NOT NULL,
                                           EFFECTIVE_DATE DATE NOT NULL,
                                           EXPIRY_DATE DATE NOT NULL,
                                           CREATE_USER VARCHAR2(32) NOT NULL,
                                           CREATE_DATE DATE DEFAULT SYSDATE NOT NULL,
                                           UPDATE_USER VARCHAR2(32) NOT NULL,
                                           UPDATE_DATE DATE DEFAULT SYSDATE NOT NULL,
                                           CONSTRAINT STUDENT_MERGE_SOURCE_CODE_PK PRIMARY KEY (MERGE_SOURCE_CODE)
);
COMMENT ON TABLE STUDENT_MERGE_SOURCE_CODE IS 'Student merged source code lists the standard codes for the source of the information that resulted in two Student records being merged as duplicates.';

-- Student merge source codes
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('SCHOOL','School Identified','School identified the need to merge two duplicate students',1,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('MINISTRY','Ministry Identified','Ministry identified the need to merge two duplicate students',2,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('STUDENT','Student Identified','Student (usually adult) identified the issue with their data',3,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('BM','Backwards Merge Fix','Backwards Merge Fix',4,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('DE','De-merged','De-merged',5,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('DR','PEN Master Duplicate Report','PEN Master Duplicate Report',6,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('IF','Iffy Twin','Iffy Twin',7,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('MA','Mass Merge','Mass Merge',8,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('MI','Ministry Identified','Ministry Identified',9,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('PR','PEN Request Identified','PEN Request Identified',10,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('SC','School Identified','School Identified',11,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('SD','SLD Duplicate Report','SLD Duplicate Report',12,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('SR','Slow Duplicate Report','Slow Duplicate Report',13,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('TX','Trax Identified','Trax Identified',14,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));

--Create student merge direction code table
CREATE TABLE STUDENT_MERGE_DIRECTION_CODE (
                                              MERGE_DIRECTION_CODE VARCHAR2(10) NOT NULL,
                                              LABEL VARCHAR2(30),
                                              DESCRIPTION VARCHAR2(255),
                                              DISPLAY_ORDER NUMBER DEFAULT 1 NOT NULL,
                                              EFFECTIVE_DATE DATE NOT NULL,
                                              EXPIRY_DATE DATE NOT NULL,
                                              CREATE_USER VARCHAR2(32) NOT NULL,
                                              CREATE_DATE DATE DEFAULT SYSDATE NOT NULL,
                                              UPDATE_USER VARCHAR2(32) NOT NULL,
                                              UPDATE_DATE DATE DEFAULT SYSDATE NOT NULL,
                                              CONSTRAINT STUDENT_MERGE_DIRECTION_CODE PRIMARY KEY (MERGE_DIRECTION_CODE)
);
COMMENT ON TABLE STUDENT_MERGE_DIRECTION_CODE IS 'Student merge direction code lists the standard codes for the direction of merge operation.';

-- Student merge direction codes
INSERT INTO STUDENT_MERGE_DIRECTION_CODE (MERGE_DIRECTION_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('FROM','Merged from','The Student record was merged to the other record. This Student record is deprecated as a result of a merge.',1,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));
INSERT INTO STUDENT_MERGE_DIRECTION_CODE (MERGE_DIRECTION_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE,CREATE_USER,CREATE_DATE,UPDATE_USER,UPDATE_DATE)
VALUES ('TO','Merged to','The Student record was the target of, or survivor of the merge. Also referred to as the True PEN.',2,to_date('2020-01-01','YYYY-MM-DD'),to_date('2099-12-31','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2020-08-14','YYYY-MM-DD'),'IDIR/MINYANG',to_date('2019-08-14','YYYY-MM-DD'));


--Create student merge table
CREATE TABLE STUDENT_MERGE (
                               STUDENT_MERGE_ID RAW(16) NOT NULL,
                               STUDENT_ID RAW(16) NOT NULL,
                               MERGE_STUDENT_ID RAW(16) NOT NULL,
                               STUDENT_MERGE_DIRECTION_CODE VARCHAR2(10) NOT NULL,
                               STUDENT_MERGE_SOURCE_CODE VARCHAR2(10) NOT NULL,
                               CREATE_USER  VARCHAR2(32) NOT NULL,
                               CREATE_DATE  DATE DEFAULT SYSDATE NOT NULL,
                               UPDATE_USER  VARCHAR2(32) NOT NULL,
                               UPDATE_DATE  DATE DEFAULT SYSDATE NOT NULL,
                               CONSTRAINT STUDENT_MERGE_PK PRIMARY KEY (STUDENT_MERGE_ID)
);
COMMENT ON TABLE STUDENT_MERGE IS 'Contains the IDs of Student records that were merged together, and distinguishes the MergedTo record (known as the True PEN) and the MergedFrom record. These result from merge actions taken by users.';
-- Column Comments
COMMENT ON COLUMN STUDENT_MERGE.STUDENT_MERGE_ID IS 'Unique surrogate key for each record. GUID value must be provided during insert.';
COMMENT ON COLUMN STUDENT_MERGE.STUDENT_ID IS 'Foreign key to the Student record that has this merge record.';
COMMENT ON COLUMN STUDENT_MERGE.MERGE_STUDENT_ID IS 'Foreign key to the Student record that was the target of, or survivor of the merge when the merge direction was "to". Also referred to as the True PEN. The Student record was merged to the other record when the merge direction was "from". This Student record is deprecated as a result of a merge.';
COMMENT ON COLUMN STUDENT_MERGE.STUDENT_MERGE_DIRECTION_CODE IS 'Code specify the direction of the merge operation.';
COMMENT ON COLUMN STUDENT_MERGE.STUDENT_MERGE_SOURCE_CODE IS 'Code specify the source of the information that resulted in two Student records being merged as duplicates.';

ALTER TABLE STUDENT_MERGE
    ADD CONSTRAINT STUDENT_MERGE_DIRECTION_CODE_FK FOREIGN KEY (STUDENT_MERGE_DIRECTION_CODE) REFERENCES STUDENT_MERGE_DIRECTION_CODE (MERGE_DIRECTION_CODE);

ALTER TABLE STUDENT_MERGE
    ADD CONSTRAINT STUDENT_MERGE_SOURCE_CODE_FK FOREIGN KEY (STUDENT_MERGE_SOURCE_CODE) REFERENCES STUDENT_MERGE_SOURCE_CODE (MERGE_SOURCE_CODE);

ALTER TABLE STUDENT_MERGE
    ADD CONSTRAINT STUDENT_ID_MERGE_STUDENT_ID_UK UNIQUE (STUDENT_ID, MERGE_STUDENT_ID);

